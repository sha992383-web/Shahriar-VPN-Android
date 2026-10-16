package com.shahriar.vpn

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.shahriar.vpn.core.VpnManager
import com.shahriar.vpn.ui.ShahriarApp
import com.shahriar.vpn.ui.theme.ShahriarTheme

/**
 * ⚡ SHAHRIAR VPN — entry point
 */
class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                com.shahriar.vpn.core.VpnEventBus.log("اجازه نوتیفیکیشن داده شد")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prepare the Xray environment early
        VpnManager.initEnv(applicationContext)

        // Android 13+: ask for notification permission (nice-to-have)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001
            )
        }

        setContent {
            ShahriarTheme {
                ShahriarApp()
            }
        }
    }
}
