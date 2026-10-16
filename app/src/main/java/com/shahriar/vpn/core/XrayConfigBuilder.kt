package com.shahriar.vpn.core

import com.shahriar.vpn.util.Constants
import org.json.JSONArray
import org.json.JSONObject

/**
 * ⚡ SHAHRIAR VPN — Xray JSON config builder
 *
 * Produces a full Xray-core configuration with:
 *   • tun inbound   (Android VPN fd is handed to the core by startLoop)
 *   • socks inbound (127.0.0.1:10808 for tools/tests)
 *   • proxy outbound built from the selected Profile
 *   • direct + block outbounds, private-range bypass, optional QUIC block
 *
 * Mirrors the template used by well-known Xray clients (v2rayNG).
 */
object XrayConfigBuilder {

    fun build(profile: Profile, blockQuic: Boolean = true): String {
        val d = JSONObject(profile.details)

        val root = JSONObject()
        root.put("stats", JSONObject())

        // ── log ─────────────────────────────────────
        root.put("log", JSONObject().put("loglevel", "warning"))

        // ── policy (level 8 = our traffic class) ────
        root.put(
            "policy",
            JSONObject()
                .put(
                    "levels",
                    JSONObject().put(
                        "8",
                        JSONObject()
                            .put("handshake", 4)
                            .put("connIdle", 300)
                            .put("uplinkOnly", 1)
                            .put("downlinkOnly", 1)
                    )
                )
                .put(
                    "system",
                    JSONObject()
                        .put("statsOutboundUplink", true)
                        .put("statsOutboundDownlink", true)
                )
        )

        // ── inbounds ────────────────────────────────
        val sniffing = JSONObject()
            .put("enabled", true)
            .put("destOverride", JSONArray().put("http").put("tls").put("quic"))

        val socksIn = JSONObject()
            .put("tag", "socks")
            .put("port", Constants.SOCKS_PORT)
            .put("protocol", "socks")
            .put(
                "settings",
                JSONObject().put("auth", "noauth").put("udp", true).put("userLevel", 8)
            )
            .put("sniffing", sniffing)

        val tunIn = JSONObject()
            .put("tag", "tun")
            .put("protocol", "tun")
            .put(
                "settings",
                JSONObject().put("name", "xray0").put("MTU", Constants.VPN_MTU).put("userLevel", 8)
            )
            .put("sniffing", sniffing)

        root.put("inbounds", JSONArray().put(socksIn).put(tunIn))

        // ── outbounds ───────────────────────────────
        val proxy = buildProxyOutbound(d)
        val direct = JSONObject()
            .put("tag", "direct")
            .put("protocol", "freedom")
            .put(
                "streamSettings",
                JSONObject().put(
                    "sockopt",
                    JSONObject()
                        .put("domainStrategy", "UseIP")
                        .put(
                            "happyEyeballs",
                            JSONObject().put("tryDelayMs", 250).put("interleave", 2)
                        )
                )
            )
        val block = JSONObject()
            .put("tag", "block")
            .put("protocol", "blackhole")
            .put("settings", JSONObject().put("response", JSONObject().put("type", "http")))

        root.put("outbounds", JSONArray().put(proxy).put(direct).put(block))

        // ── dns ─────────────────────────────────────
        root.put(
            "dns",
            JSONObject()
                .put("servers", JSONArray().put("1.1.1.1").put("8.8.8.8"))
                .put("queryStrategy", "UseIPv4")
        )

        // ── routing ─────────────────────────────────
        val rules = JSONArray()

        // 1) private / local traffic goes direct
        rules.put(
            JSONObject()
                .put("type", "field")
                .put("ip", JSONArray(Constants.PRIVATE_IPS))
                .put("outboundTag", "direct")
        )

        // 2) optionally kill QUIC so apps fall back to TCP (more resilient)
        if (blockQuic) {
            rules.put(
                JSONObject()
                    .put("type", "field")
                    .put("port", "443")
                    .put("network", "udp")
                    .put("outboundTag", "block")
            )
        }

        // 3) block bittorrent leak-through
        rules.put(
            JSONObject()
                .put("type", "field")
                .put("protocol", JSONArray().put("bittorrent"))
                .put("outboundTag", "block")
        )

        root.put(
            "routing",
            JSONObject().put("domainStrategy", "AsIs").put("rules", rules)
        )

        return root.toString()
    }

    // ────────────────────────────────────────────────
    //  Proxy outbound per protocol
    // ────────────────────────────────────────────────

    private fun buildProxyOutbound(d: JSONObject): JSONObject {
        val type = d.optString("type")
        val out = JSONObject().put("tag", "proxy")

        when (type) {
            "vmess" -> {
                out.put("protocol", "vmess")
                val user = JSONObject()
                    .put("id", d.optString("id"))
                    .put("alterId", d.optInt("aid", 0))
                    .put("security", d.optString("scy", "auto").ifBlank { "auto" })
                out.put(
                    "settings",
                    JSONObject().put(
                        "vnext",
                        JSONArray().put(
                            JSONObject()
                                .put("address", d.optString("add"))
                                .put("port", d.optInt("port"))
                                .put("users", JSONArray().put(user))
                        )
                    )
                )
                out.put("streamSettings", streamSettings(d))
                out.put("mux", JSONObject().put("enabled", false))
            }

            "vless" -> {
                out.put("protocol", "vless")
                val user = JSONObject()
                    .put("id", d.optString("id"))
                    .put("encryption", "none")
                val flow = d.optString("flow", "")
                if (flow.isNotBlank()) user.put("flow", flow)
                out.put(
                    "settings",
                    JSONObject().put(
                        "vnext",
                        JSONArray().put(
                            JSONObject()
                                .put("address", d.optString("add"))
                                .put("port", d.optInt("port"))
                                .put("users", JSONArray().put(user))
                        )
                    )
                )
                out.put("streamSettings", streamSettings(d))
                out.put("mux", JSONObject().put("enabled", false))
            }

            "trojan" -> {
                out.put("protocol", "trojan")
                out.put(
                    "settings",
                    JSONObject().put(
                        "servers",
                        JSONArray().put(
                            JSONObject()
                                .put("address", d.optString("add"))
                                .put("port", d.optInt("port"))
                                .put("password", d.optString("password"))
                        )
                    )
                )
                out.put("streamSettings", streamSettings(d))
            }

            "shadowsocks" -> {
                out.put("protocol", "shadowsocks")
                out.put(
                    "settings",
                    JSONObject().put(
                        "servers",
                        JSONArray().put(
                            JSONObject()
                                .put("address", d.optString("add"))
                                .put("port", d.optInt("port"))
                                .put("method", d.optString("method"))
                                .put("password", d.optString("password"))
                        )
                    )
                )
            }

            else -> throw IllegalArgumentException("Unsupported protocol: $type")
        }

        return out
    }

    // ────────────────────────────────────────────────
    //  streamSettings (tls / reality / ws / grpc / tcp …)
    // ────────────────────────────────────────────────

    private fun streamSettings(d: JSONObject): JSONObject {
        val network = d.optString("net", "tcp").ifBlank { "tcp" }.let {
            if (it == "h2") "http" else it
        }
        val security = when {
            d.optString("security") == "reality" || d.optString("pbk").isNotBlank() -> "reality"
            d.optString("security") == "tls" -> "tls"
            else -> "none"
        }

        val ss = JSONObject().put("network", network).put("security", security)

        val sni = d.optString("sni", "").ifBlank { d.optString("host", "") }
        val fp = d.optString("fp", "").ifBlank { "chrome" }

        if (security == "tls") {
            val tls = JSONObject()
            if (sni.isNotBlank()) tls.put("serverName", sni)
            tls.put("fingerprint", fp)
            val alpn = d.optString("alpn", "")
            if (alpn.isNotBlank()) {
                tls.put("alpn", JSONArray(alpn.split(',').map { it.trim() }.filter { it.isNotEmpty() }))
            }
            ss.put("tlsSettings", tls)
        }

        if (security == "reality") {
            val reality = JSONObject()
            if (sni.isNotBlank()) reality.put("serverName", sni)
            reality.put("fingerprint", fp)
            reality.put("publicKey", d.optString("pbk"))
            reality.put("shortId", d.optString("sid", ""))
            reality.put("spiderX", "/")
            ss.put("realitySettings", reality)
        }

        val host = d.optString("host", "")
        val path = d.optString("path", "")

        when (network) {
            "ws" -> {
                val ws = JSONObject()
                if (path.isNotBlank()) ws.put("path", path)
                if (host.isNotBlank()) {
                    ws.put("headers", JSONObject().put("Host", host))
                }
                ss.put("wsSettings", ws)
            }

            "grpc" -> {
                val grpc = JSONObject()
                if (path.isNotBlank()) grpc.put("serviceName", path)
                ss.put("grpcSettings", grpc)
            }

            "http" -> {
                val http = JSONObject()
                if (path.isNotBlank()) http.put("path", JSONArray().put(path))
                if (host.isNotBlank()) http.put("host", JSONArray(host.split(',')))
                ss.put("httpSettings", http)
            }

            "tcp" -> {
                val headerType = d.optString("headerType", "")
                if (headerType == "http") {
                    val req = JSONObject()
                    if (path.isNotBlank()) req.put("path", JSONArray().put(path))
                    if (host.isNotBlank()) req.put("headers", JSONObject().put("Host", JSONArray(host.split(','))))
                    ss.put(
                        "tcpSettings",
                        JSONObject().put("header", JSONObject().put("type", "http").put("request", req))
                    )
                }
            }
        }

        return ss
    }
}
