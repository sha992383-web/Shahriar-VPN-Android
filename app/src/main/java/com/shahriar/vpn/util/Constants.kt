package com.shahriar.vpn.util

/**
 * ⚡ SHAHRIAR VPN — Global constants
 */
object Constants {
    const val TAG = "ShahriarVPN"

    // Service intents
    const val ACTION_STOP = "com.shahriar.vpn.STOP"

    // VPN tun interface
    const val VPN_ADDRESS = "26.26.26.1"
    const val VPN_PREFIX = 30
    const val VPN_ROUTE = "0.0.0.0"
    const val VPN_DNS1 = "1.1.1.1"
    const val VPN_DNS2 = "8.8.8.8"
    const val VPN_MTU = 8500

    // Local SOCKS inbound
    const val SOCKS_PORT = 10808

    // Persistence
    const val PREFS_NAME = "shahriar_prefs"
    const val KEY_PROFILES = "profiles_json"
    const val KEY_SELECTED = "selected_profile_id"
    const val KEY_BLOCK_QUIC = "block_quic"

    // Delay test
    const val DELAY_TEST_URL = "https://www.gstatic.com/generate_204"

    // Private / local ranges that must bypass the tunnel
    val PRIVATE_IPS = listOf(
        "10.0.0.0/8",
        "172.16.0.0/12",
        "192.168.0.0/16",
        "127.0.0.0/8",
        "100.64.0.0/10",
        "169.254.0.0/16",
        "224.0.0.0/4",
        "240.0.0.0/4"
    )

    // Supported link schemes
    val SUPPORTED_SCHEMES = listOf("vmess://", "vless://", "trojan://", "ss://")
}
