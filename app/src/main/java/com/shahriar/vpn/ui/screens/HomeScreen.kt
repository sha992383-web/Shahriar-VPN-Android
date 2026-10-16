package com.shahriar.vpn.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahriar.vpn.core.VpnEventBus
import com.shahriar.vpn.core.VpnManager
import com.shahriar.vpn.core.VpnState
import com.shahriar.vpn.data.ProfileStore
import com.shahriar.vpn.service.ShahriarVpnService
import com.shahriar.vpn.ui.components.NeonCard
import com.shahriar.vpn.ui.components.PulseRings
import com.shahriar.vpn.ui.components.StatusBadge
import com.shahriar.vpn.ui.theme.NeonAmber
import com.shahriar.vpn.ui.theme.NeonCyan
import com.shahriar.vpn.ui.theme.NeonGreen
import com.shahriar.vpn.ui.theme.NeonMagenta
import com.shahriar.vpn.ui.theme.NeonPurple
import com.shahriar.vpn.ui.theme.TextDim
import com.shahriar.vpn.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat

private val scope = CoroutineScope(Dispatchers.Main)

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
    bytes < 1024L * 1024 * 1024 -> String.format("%.1f MB", bytes / 1024.0 / 1024.0)
    else -> String.format("%.2f GB", bytes / 1024.0 / 1024.0 / 1024.0)
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val state by VpnEventBus.state.collectAsState()
    val stats by VpnEventBus.stats.collectAsState()
    val connectedProfile by VpnEventBus.connectedProfile.collectAsState()

    // remember the currently selected config name for display
    val selectedName = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        selectedName.value = ProfileStore.getSelected(context)?.name
    }

    // poll traffic stats while connected
    LaunchedEffect(state) {
        while (state == VpnState.CONNECTED) {
            VpnManager.queryStats()
            delay(2000)
        }
    }

    // VPN permission launcher
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            startVpnService(context)
        } else {
            VpnEventBus.log("اجازه ساخت VPN رد شد ❌")
        }
    }

    fun onPowerClick() {
        when (state) {
            VpnState.DISCONNECTED -> {
                val selected = ProfileStore.getSelected(context)
                if (selected == null) {
                    VpnEventBus.log("اول از تب «کانفیگ‌ها» یک سرور اضافه و انتخاب کن ⚠")
                    return
                }
                val prepareIntent = android.net.VpnService.prepare(context)
                if (prepareIntent != null) {
                    vpnPermissionLauncher.launch(prepareIntent)
                } else {
                    startVpnService(context)
                }
            }
            VpnState.CONNECTING -> { /* wait */ }
            VpnState.CONNECTED -> {
                context.startService(
                    Intent(context, ShahriarVpnService::class.java)
                        .setAction(Constants.ACTION_STOP)
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── status block ─────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(8.dp))
            val (statusText, statusColor) = when (state) {
                VpnState.DISCONNECTED -> "قطع" to NeonMagenta
                VpnState.CONNECTING -> "در حال اتصال..." to NeonAmber
                VpnState.CONNECTED -> "وصل" to NeonGreen
            }
            StatusBadge("● $statusText", statusColor)

            Spacer(Modifier.height(6.dp))
            Text(
                text = connectedProfile?.name ?: selectedName.value ?: "سروری انتخاب نشده",
                color = TextDim,
                fontSize = 13.sp
            )
        }

        // ── power button ─────────────────────────────
        Box(contentAlignment = Alignment.Center) {
            PulseRings(
                color = when (state) {
                    VpnState.CONNECTED -> NeonGreen
                    VpnState.CONNECTING -> NeonAmber
                    VpnState.DISCONNECTED -> NeonCyan
                },
                active = state != VpnState.DISCONNECTED,
                modifier = Modifier.size(260.dp)
            )
            CyberPowerButton(
                state = state,
                modifier = Modifier.size(200.dp),
                onClick = ::onPowerClick
            )
        }

        // ── stats ────────────────────────────────────
        NeonCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("↑ ${formatBytes(stats.first)}", color = NeonCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("آپلود", color = TextDim, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("↓ ${formatBytes(stats.second)}", color = NeonGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("دانلود", color = TextDim, fontSize = 11.sp)
                }
            }
        }

        // ── quick actions ────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { openYouTube(context) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonMagenta.copy(alpha = 0.16f),
                    contentColor = NeonMagenta
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("یوتیوب")
            }
            Button(
                onClick = {
                    if (state != VpnState.CONNECTED) {
                        VpnEventBus.log("برای تست پینگ اول وصل شو ⚠")
                        return@Button
                    }
                    scope.launch {
                        VpnEventBus.log("در حال تست پینگ ...")
                        val ms = VpnManager.measureDelay()
                        if (ms >= 0) {
                            VpnEventBus.log("پینگ: ${ms}ms ✅")
                        } else {
                            VpnEventBus.log("تست پینگ ناموفق ❌")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan.copy(alpha = 0.16f),
                    contentColor = NeonCyan
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("تست پینگ")
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

// ── helpers ──────────────────────────────────────────

private fun startVpnService(context: android.content.Context) {
    ContextCompat.startForegroundService(
        context, Intent(context, ShahriarVpnService::class.java)
    )
}

private fun openYouTube(context: android.content.Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://home")))
    } catch (e: Exception) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e2: Exception) {
            VpnEventBus.log("یوتیوب پیدا نشد 😵")
        }
    }
}

// ── the big neon power button ────────────────────────

@Composable
private fun CyberPowerButton(
    state: VpnState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "power")
    val breathe by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "breathe"
    )

    val mainColor = when (state) {
        VpnState.CONNECTED -> NeonGreen
        VpnState.CONNECTING -> NeonAmber
        VpnState.DISCONNECTED -> NeonCyan
    }

    Canvas(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.minDimension * 0.46f * breathe
        val innerR = size.minDimension * 0.36f * breathe

        // outer glow layers
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(mainColor.copy(alpha = 0.22f), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                radius = outerR * 1.5f
            ),
            radius = outerR * 1.5f,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )

        // main ring
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(mainColor, NeonPurple, mainColor)
            ),
            radius = outerR,
            center = androidx.compose.ui.geometry.Offset(cx, cy),
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )

        // inner fill
        drawCircle(
            color = Color(0xFF0D0D1A),
            radius = innerR,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )
        drawCircle(
            color = mainColor.copy(alpha = 0.55f),
            radius = innerR,
            center = androidx.compose.ui.geometry.Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )

        // power glyph (circle + stem)
        val glyphR = innerR * 0.45f
        drawArc(
            color = mainColor,
            startAngle = -60f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(cx - glyphR, cy - glyphR + innerR * 0.08f),
            size = androidx.compose.ui.geometry.Size(glyphR * 2, glyphR * 2),
            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
        )
        drawLine(
            color = mainColor,
            start = androidx.compose.ui.geometry.Offset(cx, cy - glyphR * 1.5f),
            end = androidx.compose.ui.geometry.Offset(cx, cy + glyphR * 0.2f),
            strokeWidth = 7.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
