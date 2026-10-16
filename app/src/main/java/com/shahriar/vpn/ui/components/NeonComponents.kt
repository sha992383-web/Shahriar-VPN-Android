package com.shahriar.vpn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahriar.vpn.ui.theme.CyberGrid
import com.shahriar.vpn.ui.theme.NeonCyan
import com.shahriar.vpn.ui.theme.NeonMagenta

/**
 * ⚡ SHAHRIAR VPN — cyberpunk UI components
 */

// ── Glitch title ─────────────────────────────────────

@Composable
fun GlitchTitle(text: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "glitch")
    val shift by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 260, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchShift"
    )

    Box(modifier = modifier) {
        // magenta ghost
        Text(
            text = text,
            color = NeonMagenta.copy(alpha = 0.55f),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.offset(x = (shift * 1.4f).dp),
        )
        // cyan ghost
        Text(
            text = text,
            color = NeonCyan.copy(alpha = 0.55f),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.offset(x = (-shift * 1.4f).dp),
        )
        // main text
        Text(
            text = text,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

// ── CRT scanlines overlay ────────────────────────────

@Composable
fun Scanlines(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val lineH = 6f
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = CyberGrid.copy(alpha = 0.16f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.2f
            )
            y += lineH
        }
    }
}

// ── Neon card ────────────────────────────────────────

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonCyan.copy(alpha = 0.35f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF141426), Color(0xFF101020))
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
    ) {
        content()
    }
}

// ── Status badge ─────────────────────────────────────

@Composable
fun StatusBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Radar / pulse rings (decorative) ─────────────────

@Composable
fun PulseRings(color: Color, modifier: Modifier = Modifier, active: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val radius by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = if (active) 0.5f else 0.2f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "radius"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = if (active) 0f else 0.2f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "alpha"
    )

    Canvas(modifier = modifier) {
        val r = size.minDimension * radius
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = r,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}
