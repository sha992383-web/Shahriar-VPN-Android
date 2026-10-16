package com.shahriar.vpn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahriar.vpn.ui.components.GlitchTitle
import com.shahriar.vpn.ui.components.Scanlines
import com.shahriar.vpn.ui.screens.AboutScreen
import com.shahriar.vpn.ui.screens.ConfigsScreen
import com.shahriar.vpn.ui.screens.HomeScreen
import com.shahriar.vpn.ui.screens.LogScreen
import com.shahriar.vpn.ui.theme.CyberBg
import com.shahriar.vpn.ui.theme.NeonCyan
import com.shahriar.vpn.ui.theme.NeonMagenta
import com.shahriar.vpn.ui.theme.TextDim

private data class Tab(val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("اتصال", Icons.Filled.Home),
    Tab("کانفیگ‌ها", Icons.Filled.List),
    Tab("لاگ", Icons.Filled.Menu),
    Tab("درباره", Icons.Filled.Info)
)

/**
 * ⚡ SHAHRIAR VPN — main app scaffold (cyberpunk shell)
 */
@Composable
fun ShahriarApp() {
    var tab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // ── header ───────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0D1A))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlitchTitle("SHAHRIAR://VPN")
            Spacer(Modifier.weight(1f))
            Text("v1.0", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // ── content + scanline overlay ───────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (tab) {
                0 -> HomeScreen()
                1 -> ConfigsScreen()
                2 -> LogScreen()
                3 -> AboutScreen()
            }
            Scanlines(
                Modifier
                    .fillMaxSize()
                    .padding(top = 0.dp)
            )
        }

        // ── bottom bar ───────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0D1A))
                .padding(vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, item ->
                val selected = index == tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { tab = index },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (selected) NeonCyan else TextDim.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        item.label,
                        color = if (selected) NeonCyan else TextDim.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        Modifier
                            .size(width = 22.dp, height = 3.dp)
                            .background(
                                if (selected) NeonCyan else Color.Transparent,
                                androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
    }
}
