package com.shahriar.vpn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahriar.vpn.core.VpnManager
import com.shahriar.vpn.ui.components.NeonCard
import com.shahriar.vpn.ui.theme.NeonCyan
import com.shahriar.vpn.ui.theme.NeonMagenta
import com.shahriar.vpn.ui.theme.TextDim
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ⚡ SHAHRIAR VPN — about screen
 */
@Composable
fun AboutScreen() {
    var coreVersion by remember { mutableStateOf("...") }

    LaunchedEffect(Unit) {
        coreVersion = withContext(Dispatchers.IO) { VpnManager.coreVersion() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("⚡", fontSize = 40.sp)
        Text(
            "فیلترشکن شهریار",
            color = NeonCyan,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Text("نسخه 1.0.0", color = TextDim, fontSize = 12.sp)
        Text("هسته: $coreVersion", color = TextDim, fontSize = 11.sp)

        NeonCard {
            Column(Modifier.padding(16.dp)) {
                Text("تکنولوژی", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "این برنامه از هسته Xray استفاده می‌کند و پروتکل‌های VLESS (با پشتیبانی کامل Reality)، VMess، Trojan و Shadowsocks را پشتیبانی می‌کند. ترافیک اینترنت شما داخل یک تونل رمزنگاری‌شده عبور می‌کند.",
                    color = TextDim,
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                )
            }
        }

        NeonCard {
            Column(Modifier.padding(16.dp)) {
                Text("استفاده مسئولانه ⚠", color = NeonMagenta, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "این نرم‌افزار صرفاً برای دسترسی آزاد به اطلاعات و حریم خصوصی شخصی ساخته شده است. استفاده از آن بر عهده خود کاربر است؛ قوانین محلی و مسئولیت اخلاقی را در نظر بگیرید. این پروژه متن‌باز است و تحت مجوز GPL-3.0 منتشر می‌شود.",
                    color = TextDim,
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                )
            }
        }

        NeonCard {
            Column(Modifier.padding(16.dp)) {
                Text("ساخت با ❤", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Kotlin + Jetpack Compose + Xray-Core\nمجوز: GPL-3.0 • ۲۰۲۶",
                    color = TextDim,
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                )
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}
