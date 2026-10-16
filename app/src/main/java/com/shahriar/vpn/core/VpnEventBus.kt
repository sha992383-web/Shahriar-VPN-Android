package com.shahriar.vpn.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ⚡ SHAHRIAR VPN — global reactive state bus
 *
 * The UI and the VPN service communicate through this singleton.
 */
enum class VpnState { DISCONNECTED, CONNECTING, CONNECTED }

/** A parsed server profile (from vmess/vless/trojan/ss links). */
data class Profile(
    val id: String,
    val name: String,
    val type: String,      // vmess | vless | trojan | shadowsocks
    val address: String,
    val port: Int,
    val rawLink: String,
    val details: String    // JSON string with all protocol-specific fields
)

object VpnEventBus {

    // ── Connection state ──────────────────────────────
    private val _state = MutableStateFlow(VpnState.DISCONNECTED)
    val state: StateFlow<VpnState> = _state

    // ── Live traffic counters (up, down) in bytes ─────
    private val _stats = MutableStateFlow(Pair(0L, 0L))
    val stats: StateFlow<Pair<Long, Long>> = _stats

    // ── Currently connected profile ───────────────────
    val connectedProfile = MutableStateFlow<Profile?>(null)

    // ── Terminal-style logs (rolling buffer, newest last) ──
    private val _logLines = MutableStateFlow<List<String>>(emptyList())
    val logLines: StateFlow<List<String>> = _logLines

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    fun setState(s: VpnState) {
        _state.value = s
    }

    fun log(message: String) {
        val line = timeFormat.format(Date()) + " ▸ " + message
        _logLines.value = (_logLines.value + line).takeLast(300)
    }

    fun setStats(up: Long, down: Long) {
        _stats.value = Pair(up, down)
    }

    fun resetStats() {
        _stats.value = Pair(0L, 0L)
    }
}
