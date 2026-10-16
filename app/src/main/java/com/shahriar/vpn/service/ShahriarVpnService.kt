package com.shahriar.vpn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.shahriar.vpn.MainActivity
import com.shahriar.vpn.R
import com.shahriar.vpn.core.Profile
import com.shahriar.vpn.core.VpnEventBus
import com.shahriar.vpn.core.VpnManager
import com.shahriar.vpn.core.VpnState
import com.shahriar.vpn.core.XrayConfigBuilder
import com.shahriar.vpn.data.ProfileStore
import com.shahriar.vpn.util.Constants

/**
 * ⚡ SHAHRIAR VPN — the Android VPN service
 *
 * Lifecycle:
 *   1. startForeground immediately (API 34 requirement)
 *   2. establish() the tun interface (mtu 8500 / 26.26.26.1 / 0.0.0.0/0)
 *   3. hand tunFd + xray config to the core via startLoop
 *   4. on stop: stopLoop → close tun → stopSelf
 */
class ShahriarVpnService : VpnService() {

    companion object {
        private const val NOTIF_ID = 0x5348
        private const val CHANNEL_ID = "shahriar_vpn_channel"
    }

    private var tunInterface: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        VpnManager.initEnv(this)
    }

    override fun onRevoke() {
        // Another VPN app took over, or the user revoked the permission
        VpnEventBus.log("دسترسی VPN لغو شد")
        shutdownAll()
    }

    override fun onDestroy() {
        shutdownAll()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION_STOP) {
            shutdownAll()
            return START_NOT_STICKY
        }

        showForeground()

        VpnEventBus.setState(VpnState.CONNECTING)
        VpnEventBus.log("در حال برقراری تونل VPN ...")

        val profile: Profile = ProfileStore.getSelected(this).also {
            if (it == null) {
                VpnEventBus.log("هیچ کانفیگی انتخاب نشده است ❌")
                shutdownAll()
                return START_NOT_STICKY
            }
        }!!

        if (!establishTun()) {
            shutdownAll()
            return START_NOT_STICKY
        }

        val fd = tunInterface?.fd
        if (fd == null || fd <= 0) {
            VpnEventBus.log("تونل TUN نامعتبر است ❌")
            shutdownAll()
            return START_NOT_STICKY
        }

        VpnManager.resetAccumulatedStats()
        val config = XrayConfigBuilder.build(profile, ProfileStore.isBlockQuic(this))
        val started = VpnManager.start(config, fd)

        return if (started && VpnManager.isRunning()) {
            VpnEventBus.setState(VpnState.CONNECTED)
            VpnEventBus.connectedProfile.value = profile
            VpnEventBus.log("اتصال برقرار شد ✅ ${profile.name}")
            START_STICKY
        } else {
            VpnEventBus.log("راه‌اندازی هسته ناموفق بود ❌")
            shutdownAll()
            START_NOT_STICKY
        }
    }

    // ── tun interface ────────────────────────────────

    private fun establishTun(): Boolean {
        return try {
            if (prepare(this) != null) {
                VpnEventBus.log("دسترسی VPN داده نشده است ❌")
                return false
            }

            // Close any stale interface before rebuilding
            tunInterface?.let { old ->
                try { old.close() } catch (_: Exception) {}
                tunInterface = null
            }

            val builder = Builder()
                .setMtu(Constants.VPN_MTU)
                .addAddress(Constants.VPN_ADDRESS, Constants.VPN_PREFIX)
                .addRoute(Constants.VPN_ROUTE, 0)
                .addDnsServer(Constants.VPN_DNS1)
                .addDnsServer(Constants.VPN_DNS2)
                .setSession("Shahriar VPN")
                .addDisallowedApplication(packageName) // never loop our own traffic

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }

            tunInterface = builder.establish()
            if (tunInterface == null) {
                VpnEventBus.log("برقراری تونل TUN ناموفق بود ❌")
                false
            } else {
                true
            }
        } catch (e: Exception) {
            VpnEventBus.log("خطای تونل: ${e.message ?: e.javaClass.simpleName}")
            false
        }
    }

    // ── shutdown ─────────────────────────────────────

    private fun shutdownAll() {
        try { VpnManager.stop() } catch (_: Exception) {}

        // Give the core a moment to release the tun fd, then close it
        try { Thread.sleep(150) } catch (_: InterruptedException) {}

        tunInterface?.let {
            try { it.close() } catch (_: Exception) {}
        }
        tunInterface = null

        VpnEventBus.connectedProfile.value = null
        VpnEventBus.setState(VpnState.DISCONNECTED)

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (_: Exception) {}
    }

    // ── notification ─────────────────────────────────

    private fun showForeground() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notif_channel),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, ShahriarVpnService::class.java).setAction(Constants.ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_shahriar)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setContentIntent(openIntent)
            .addAction(0, getString(R.string.notif_action_stop), stopIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }
}
