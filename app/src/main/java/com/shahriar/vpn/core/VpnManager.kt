package com.shahriar.vpn.core

import android.content.Context
import android.provider.Settings
import com.shahriar.vpn.util.Constants
import go.Seq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ⚡ SHAHRIAR VPN — Xray core manager
 *
 * Thin, thread-safe wrapper around AndroidLibXrayLite (libv2ray):
 *   • initCoreEnv(filesDir, deviceId)   — once per process
 *   • newCoreController(callback)
 *   • startLoop(configJson, tunFd)      — core owns the tun interface
 *   • stopLoop()
 */
object VpnManager {

    private var controller: CoreController? = null
    private val envInitialized = AtomicBoolean(false)

    // Cumulative traffic (queryAllOutboundTrafficStats resets its counters)
    private var accUp = 0L
    private var accDown = 0L

    // ── Environment ──────────────────────────────────

    fun initEnv(context: Context) {
        if (!envInitialized.compareAndSet(false, true)) return
        try {
            Seq.setContext(context.applicationContext)
            val androidId = Settings.Secure.getString(
                context.contentResolver, Settings.Secure.ANDROID_ID
            )
            val deviceId = UUID.nameUUIDFromBytes(
                (androidId ?: "shahriar-vpn").toByteArray(Charsets.UTF_8)
            ).toString()
            Libv2ray.initCoreEnv(context.filesDir.absolutePath, deviceId)
            VpnEventBus.log("هسته Xray آماده شد ⚡")
        } catch (e: Exception) {
            envInitialized.set(false)
            VpnEventBus.log("خطای آماده‌سازی هسته: ${e.message}")
        }
    }

    fun coreVersion(): String = try {
        Libv2ray.checkVersionX()
    } catch (e: Exception) {
        "نامشخص"
    }

    // ── Lifecycle ────────────────────────────────────

    fun start(config: String, tunFd: Int): Boolean = try {
        val c = controller ?: Libv2ray.newCoreController(callback).also { controller = it }
        c.startLoop(config, tunFd)
        c.isRunning
    } catch (e: Exception) {
        VpnEventBus.log("خطای شروع هسته: ${e.message ?: e.javaClass.simpleName}")
        false
    }

    fun stop(): Boolean {
        val c = controller ?: return false
        return try {
            if (c.isRunning) c.stopLoop()
            true
        } catch (e: Exception) {
            VpnEventBus.log("خطای توقف هسته: ${e.message}")
            false
        }
    }

    fun isRunning(): Boolean = try {
        controller?.isRunning ?: false
    } catch (e: Exception) {
        false
    }

    // ── Diagnostics ──────────────────────────────────

    suspend fun measureDelay(): Long = withContext(Dispatchers.IO) {
        try {
            controller?.measureDelay(Constants.DELAY_TEST_URL) ?: -1L
        } catch (e: Exception) {
            -1L
        }
    }

    /** Poll & accumulate traffic counters (the core resets them on each query). */
    fun queryStats() {
        val c = controller ?: return
        try {
            if (!c.isRunning) return
            val payload = c.queryAllOutboundTrafficStats()
            // Format: tag,direction,value;tag,direction,value;...
            payload.split(';').forEach { entry ->
                if (entry.isBlank()) return@forEach
                val parts = entry.split(',', limit = 3)
                if (parts.size != 3) return@forEach
                val value = parts[2].toLongOrNull() ?: return@forEach
                if (parts[0] != "proxy") return@forEach
                when (parts[1]) {
                    "up" -> accUp += value
                    "down" -> accDown += value
                }
            }
            VpnEventBus.setStats(accUp, accDown)
        } catch (_: Exception) {
        }
    }

    fun resetAccumulatedStats() {
        accUp = 0L
        accDown = 0L
        VpnEventBus.resetStats()
    }

    // ── Core callbacks ───────────────────────────────

    private val callback = object : CoreCallbackHandler {
        override fun startup(): Long {
            VpnEventBus.log("هسته روشن شد")
            return 0
        }

        override fun shutdown(): Long {
            VpnEventBus.log("هسته خاموش شد")
            return 0
        }

        override fun onEmitStatus(l: Long, s: String?): Long {
            s?.takeIf { it.isNotBlank() }?.let {
                VpnEventBus.log(it.take(280))
            }
            return 0
        }
    }
}
