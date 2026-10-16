package com.shahriar.vpn

import com.shahriar.vpn.core.LinkParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ⚡ SHAHRIAR VPN — link parser unit tests
 */
class LinkParserTest {

    @Test
    fun `parses vless reality link`() {
        val link = "vless://8f3d5a2c-1234-4e5f-9a8b-7c6d5e4f3a2b@server.example.com:443" +
            "?encryption=none&security=reality&sni=www.google.com&fp=chrome" +
            "&pbk=SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc&sid=a1b2c3d&type=tcp&flow=xtls-rprx-vision" +
            "#Shahriar%20Reality%20Node"
        val p = LinkParser.parse(link)
        assertNotNull(p)
        assertEquals("vless", p!!.type)
        assertEquals("server.example.com", p.address)
        assertEquals(443, p.port)
        assertEquals("Shahriar Reality Node", p.name)
        assertTrue(p.details.contains("\"security\":\"reality\""))
        assertTrue(p.details.contains("SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc"))
    }

    @Test
    fun `parses vmess base64 link`() {
        val json = """{"v":"2","ps":"Test VMess","add":"1.2.3.4","port":"8443","id":"uuid-1234",
            "aid":"0","scy":"auto","net":"ws","host":"cdn.example.com","path":"/ws",
            "tls":"tls","sni":"cdn.example.com","fp":"chrome"}"""
        val b64 = java.util.Base64.getEncoder().encodeToString(json.toByteArray())
        val p = LinkParser.parse("vmess://$b64")
        assertNotNull(p)
        assertEquals("vmess", p!!.type)
        assertEquals("1.2.3.4", p.address)
        assertEquals(8443, p.port)
        assertEquals("Test VMess", p.name)
        assertTrue(p.details.contains("\"net\":\"ws\""))
    }

    @Test
    fun `parses trojan link`() {
        val link = "trojan://s3cretPass@tj.example.com:443?sni=tj.example.com&type=tcp#TrojanNode"
        val p = LinkParser.parse(link)
        assertNotNull(p)
        assertEquals("trojan", p!!.type)
        assertEquals("tj.example.com", p.address)
        assertEquals(443, p.port)
        assertEquals("TrojanNode", p.name)
        assertTrue(p.details.contains("s3cretPass"))
        assertTrue(p.details.contains("\"security\":\"tls\""))
    }

    @Test
    fun `parses ss sip002 link`() {
        // method:password = aes-256-gcm:shahriar2026
        val userB64 = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("aes-256-gcm:shahriar2026".toByteArray())
        val link = "ss://${userB64}@ss.example.com:8388#SS%20Node"
        val p = LinkParser.parse(link)
        assertNotNull(p)
        assertEquals("shadowsocks", p!!.type)
        assertEquals("ss.example.com", p.address)
        assertEquals(8388, p.port)
        assertEquals("SS Node", p.name)
        assertTrue(p.details.contains("aes-256-gcm"))
    }

    @Test
    fun `parses subscription blob with multiple links`() {
        val links = listOf(
            "vless://8f3d5a2c-1234-4e5f-9a8b-7c6d5e4f3a2b@a.example.com:443?security=tls&type=ws&sni=a.com&path=%2Fws#Node-A",
            "trojan://pw@b.example.com:443?sni=b.com#Node-B"
        ).joinToString("\n")
        val blob = java.util.Base64.getEncoder().encodeToString(links.toByteArray())

        val result = LinkParser.parseSubscription(blob)
        assertEquals(2, result.size)
        assertEquals("Node-A", result[0].name)
        assertEquals("Node-B", result[1].name)
    }

    @Test
    fun `rejects garbage`() {
        assertEquals(null, LinkParser.parse("https://www.youtube.com/watch?v=x"))
        assertEquals(null, LinkParser.parse("vless://no-host"))
        assertEquals(null, LinkParser.parse(""))
    }
}
