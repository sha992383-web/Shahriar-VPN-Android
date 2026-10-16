package com.shahriar.vpn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahriar.vpn.core.VpnEventBus
import com.shahriar.vpn.ui.theme.TerminalGreen
import com.shahriar.vpn.ui.theme.TextDim

/**
 * ⚡ SHAHRIAR VPN — terminal-style log screen
 */
@Composable
fun LogScreen() {
    val logList by VpnEventBus.logLines.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // terminal header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Color(0xFF101020),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                "shahriar@core:~$ tail -f shahriar.log",
                color = TextDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // terminal body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Color(0xFF07070F),
                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
        ) {
            if (logList.isEmpty()) {
                Text(
                    "// منتظر رویدادها ...",
                    color = TextDim.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    reverseLayout = true // newest on top
                ) {
                    items(logList.asReversed()) { line ->
                        Text(
                            line,
                            color = TerminalGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
