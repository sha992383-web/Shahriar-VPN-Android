package com.shahriar.vpn.core

import org.json.JSONObject
import java.net.URLDecoder
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * ⚡ SHAHRIAR VPN — universal share-link parser
 *
 * Supports:
 *   vmess://   (base64 JSON)
 *   vless://   (URI + query params, incl. Reality)
 *   trojan://  (URI + query params)
 *   ss://      (SIP002 & legacy base64 forms)
 *   base64 subscription blobs containing any of the above
 *
 * Pure Kotlin/JVM — no Android dependencies — fully unit-testable.
 */
@OptIn(ExperimentalEncodingApi::class)
object LinkParser {

    // ────────────────────────────────────────────────
    //  Base64 helpers (handles url-safe & padding)
    // ────────────────────────────────────────────────

    fun b64Decode(input: String): String? = try {
        var s = input.trim().replace("\n", "").replace("\r", "")
        s = s.replace('-', '+').replace('_', '/')
        val pad = (4 - (s.length % 4)) % 4
        s = s + "=".repeat(pad)
        String(Base64.Default.decode(s), Charsets.UTF_8)
    } catch (_: Exception) {
        null
    }

    // ────────────────────────────────────────────────
    //  Public entry points
    // ────────────────────────────────────────────────

    /** Parse a single share link. Returns null when unsupported/malformed. */
    fun parse(link: String): Profile? {
        val l = link.trim()
        return try {
            when {
                l.startsWith("vmess://") -> parseVmess(l)
                l.startsWith("vless://") -> parseVless(l)
                l.startsWith("trojan://") -> parseTrojan(l)
                l.startsWith("ss://") -> parseSs(l)
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parse a subscription body: either a base64 blob or raw lines
     * containing one link per line.
     */
    fun parseSubscription(body: String): List<Profile> {
        val decoded = if (body.contains("://")) body else b64Decode(body) ?: body
        return decoded.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { parse(it) }
            .distinctBy { "${it.type}|${it.address}|${it.port}|${it.name}" }
            .toList()
    }

    // ────────────────────────────────────────────────
    //  vmess://BASE64(JSON)
    // ────────────────────────────────────────────────

    private fun parseVmess(link: String): Profile? {
        val payload = link.removePrefix("vmess://")
        val json = b64Decode(payload) ?: return null
        val v = JSONObject(json)

        val address = v.optString("add").trim()
        val port = (v.opt("port") as? Number)?.toInt() ?: v.optString("port").toIntOrNull() ?: return null
        val id = v.optString("id").trim()
        if (address.isEmpty() || id.isEmpty()) return null

        val net = v.optString("net", "tcp").ifBlank { "tcp" }
        val tlsRaw = v.optString("tls", "")
        val pbk = v.optString("pbk", "")
        val security = when {
            pbk.isNotBlank() -> "reality"
            tlsRaw.equals("tls", ignoreCase = true) -> "tls"
            else -> "none"
        }

        val details = JSONObject()
            .put("type", "vmess")
            .put("add", address)
            .put("port", port)
            .put("id", id)
            .put("aid", (v.opt("aid") as? Number)?.toInt() ?: v.optString("aid", "0").toIntOrNull() ?: 0)
            .put("scy", v.optString("scy", "auto").ifBlank { "auto" })
            .put("net", net)
            .put("host", v.optString("host", ""))
            .put("path", v.optString("path", ""))
            .put("headerType", v.optString("type", ""))
            .put("security", security)
            .put("sni", v.optString("sni", v.optString("host", "")))
            .put("fp", v.optString("fp", ""))
            .put("alpn", v.optString("alpn", ""))
            .put("pbk", pbk)
            .put("sid", v.optString("sid", ""))
            .put("flow", v.optString("flow", ""))

        val name = v.optString("ps", "").ifBlank { "VMess • $address" }
        return Profile(
            id = newId(),
            name = name,
            type = "vmess",
            address = address,
            port = port,
            rawLink = link,
            details = details.toString()
        )
    }

    // ────────────────────────────────────────────────
    //  vless://uuid@host:port?params#name
    // ────────────────────────────────────────────────

    private fun parseVless(link: String): Profile? {
        val body = link.removePrefix("vless://")
        val (main, query, fragment) = splitUri(body)

        val at = main.lastIndexOf('@')
        if (at <= 0) return null
        val userInfo = main.substring(0, at)
        val (host, port) = parseHostPort(main.substring(at + 1)) ?: return null
        if (userInfo.isBlank() || host.isBlank()) return null

        val q = parseQuery(query)
        val net = q["type"]?.ifBlank { "tcp" } ?: "tcp"
        val security = when (q["security"]) {
            "reality" -> "reality"
            "tls" -> "tls"
            else -> if (q.containsKey("pbk")) "reality" else "none"
        }

        val details = JSONObject()
            .put("type", "vless")
            .put("add", host)
            .put("port", port)
            .put("id", userInfo)
            .put("net", net)
            .put("security", security)
            .put("sni", q["sni"] ?: q["host"] ?: "")
            .put("host", q["host"] ?: "")
            .put("path", q["path"] ?: "")
            .put("fp", q["fp"] ?: "")
            .put("alpn", q["alpn"] ?: "")
            .put("pbk", q["pbk"] ?: "")
            .put("sid", q["sid"] ?: "")
            .put("flow", q["flow"] ?: "")
            .put("headerType", q["headerType"] ?: "")

        val name = fragment?.ifBlank { null } ?: "VLESS • $host"
        return Profile(
            id = newId(),
            name = name,
            type = "vless",
            address = host,
            port = port,
            rawLink = link,
            details = details.toString()
        )
    }

    // ────────────────────────────────────────────────
    //  trojan://password@host:port?params#name
    // ────────────────────────────────────────────────

    private fun parseTrojan(link: String): Profile? {
        val body = link.removePrefix("trojan://")
        val (main, query, fragment) = splitUri(body)

        val at = main.lastIndexOf('@')
        if (at <= 0) return null
        val password = main.substring(0, at)
        val (host, port) = parseHostPort(main.substring(at + 1)) ?: return null

        val q = parseQuery(query)
        val net = q["type"]?.ifBlank { "tcp" } ?: "tcp"
        val security = when (q["security"]) {
            "reality" -> "reality"
            "none" -> "none"
            else -> "tls" // trojan defaults to TLS
        }

        val details = JSONObject()
            .put("type", "trojan")
            .put("add", host)
            .put("port", port)
            .put("password", password)
            .put("net", net)
            .put("security", security)
            .put("sni", q["sni"] ?: q["peer"] ?: q["host"] ?: "")
            .put("host", q["host"] ?: "")
            .put("path", q["path"] ?: "")
            .put("fp", q["fp"] ?: "")
            .put("alpn", q["alpn"] ?: "")
            .put("pbk", q["pbk"] ?: "")
            .put("sid", q["sid"] ?: "")
            .put("headerType", q["headerType"] ?: "")

        val name = fragment?.ifBlank { null } ?: "Trojan • $host"
        return Profile(
            id = newId(),
            name = name,
            type = "trojan",
            address = host,
            port = port,
            rawLink = link,
            details = details.toString()
        )
    }

    // ────────────────────────────────────────────────
    //  ss://  (SIP002 + legacy)
    // ────────────────────────────────────────────────

    private fun parseSs(link: String): Profile? {
        val body = link.removePrefix("ss://")
        val fragment = body.substringAfter('#', "").takeIf { it.isNotEmpty() }?.let { urlDecode(it) }
        val main = body.substringBefore('#').substringBefore('?')

        val method: String
        val password: String
        val host: String
        val port: Int

        if (main.contains('@')) {
            val at = main.lastIndexOf('@')
            val userInfo = main.substring(0, at)
            val (h, p) = parseHostPort(main.substring(at + 1)) ?: return null
            host = h; port = p

            val decodedUser = b64Decode(userInfo)
            if (decodedUser != null && decodedUser.contains(':') && !decodedUser.contains('@')) {
                method = decodedUser.substringBefore(':')
                password = decodedUser.substringAfter(':')
            } else {
                // SIP002 allows plain (percent-encoded) userinfo
                val plain = urlDecode(userInfo)
                if (!plain.contains(':')) return null
                method = plain.substringBefore(':')
                password = plain.substringAfter(':')
            }
        } else {
            // Legacy: ss://base64(method:password@host:port)
            val decoded = b64Decode(main) ?: return null
            val at = decoded.lastIndexOf('@')
            if (at <= 0) return null
            val cred = decoded.substring(0, at)
            val (h, p) = parseHostPort(decoded.substring(at + 1)) ?: return null
            host = h; port = p
            if (!cred.contains(':')) return null
            method = cred.substringBefore(':')
            password = cred.substringAfter(':')
        }

        if (host.isBlank() || method.isBlank()) return null

        val details = JSONObject()
            .put("type", "shadowsocks")
            .put("add", host)
            .put("port", port)
            .put("method", method)
            .put("password", password)

        val name = fragment?.ifBlank { null } ?: "SS • $host"
        return Profile(
            id = newId(),
            name = name,
            type = "shadowsocks",
            address = host,
            port = port,
            rawLink = link,
            details = details.toString()
        )
    }

    // ────────────────────────────────────────────────
    //  URI plumbing
    // ────────────────────────────────────────────────

    /** Splits "main?query#fragment" (fragment & query may be absent). */
    private data class UriParts(val main: String, val query: String, val fragment: String?)

    private fun splitUri(body: String): UriParts {
        val noFragment = if (body.contains('#')) {
            val frag = body.substringAfter('#')
            body.substringBefore('#') to frag
        } else null

        val mainAndQuery = noFragment?.first ?: body
        val fragment = noFragment?.second?.let { urlDecode(it) }

        val main = mainAndQuery.substringBefore('?')
        val query = mainAndQuery.substringAfter('?', "")

        return UriParts(main, query, fragment)
    }

    /** Parses "host:port" incl. [IPv6] bracket form. */
    private fun parseHostPort(s: String): Pair<String, Int>? {
        val str = s.trim()
        return try {
            when {
                str.startsWith("[") -> { // [::1]:443
                    val close = str.indexOf(']')
                    if (close < 0) return null
                    val host = str.substring(1, close)
                    val port = str.substringAfterLast(':', "").toIntOrNull() ?: return null
                    host to port
                }
                str.count { it == ':' } == 1 -> {
                    val host = str.substringBefore(':')
                    val port = str.substringAfter(':').toIntOrNull() ?: return null
                    host to port
                }
                else -> { // bare IPv6 without port — invalid for our links
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Minimal query-string parser (URL-decoded key=value pairs). */
    private fun parseQuery(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        val map = HashMap<String, String>()
        query.split('&').forEach { pair ->
            if (pair.isBlank()) return@forEach
            val k = urlDecode(pair.substringBefore('=')).trim()
            val v = urlDecode(pair.substringAfter('=', ""))
            if (k.isNotEmpty()) map[k] = v
        }
        return map
    }

    private fun urlDecode(s: String): String = try {
        URLDecoder.decode(s, "UTF-8")
    } catch (_: Exception) {
        s
    }

    fun newId(): String =
        java.util.UUID.randomUUID().toString().replace("-", "").take(12)
}
