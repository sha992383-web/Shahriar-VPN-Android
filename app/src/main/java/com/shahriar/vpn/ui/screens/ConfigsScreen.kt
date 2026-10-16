package com.shahriar.vpn.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahriar.vpn.core.LinkParser
import com.shahriar.vpn.core.Profile
import com.shahriar.vpn.core.VpnEventBus
import com.shahriar.vpn.data.ProfileStore
import com.shahriar.vpn.ui.components.NeonCard
import com.shahriar.vpn.ui.theme.NeonCyan
import com.shahriar.vpn.ui.theme.NeonGreen
import com.shahriar.vpn.ui.theme.NeonMagenta
import com.shahriar.vpn.ui.theme.TextDim

private val TYPE_LABELS = mapOf(
    "vmess" to "VMESS",
    "vless" to "VLESS",
    "trojan" to "TROJAN",
    "shadowsocks" to "SS"
)

@Composable
fun ConfigsScreen() {
    val context = LocalContext.current
    var profiles by remember { mutableStateOf(ProfileStore.loadAll(context)) }
    var selectedId by remember { mutableStateOf(ProfileStore.getSelectedId(context)) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (profiles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("◈", fontSize = 42.sp, color = NeonCyan.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))
                Text("هنوز سروری نداری", color = TextDim, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "دکمه + را بزن و لینک کانفیگ (vless/vmess/trojan/ss)\nرا وارد یا یک اشتراک کامل paste کن",
                    color = TextDim.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp, end = 20.dp, bottom = 90.dp, top = 4.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    val isSelected = profile.id == selectedId
                    NeonCard(
                        borderColor = if (isSelected) NeonGreen else NeonCyan.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedId = profile.id
                                ProfileStore.setSelectedId(context, profile.id)
                                VpnEventBus.log("سرور انتخاب شد: ${profile.name}")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    profile.name,
                                    color = if (isSelected) NeonGreen else androidx.compose.ui.graphics.Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${profile.address}:${profile.port}",
                                    color = TextDim,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                TYPE_LABELS[profile.type] ?: profile.type.uppercase(),
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.size(10.dp))
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "حذف",
                                tint = NeonMagenta.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        ProfileStore.remove(context, profile.id)
                                        profiles = ProfileStore.loadAll(context)
                                        selectedId = ProfileStore.getSelectedId(context)
                                        VpnEventBus.log("کانفیگ حذف شد: ${profile.name}")
                                    }
                            )
                        }
                    }
                }
            }
        }

        // add button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = NeonCyan,
            contentColor = androidx.compose.ui.graphics.Color(0xFF0A0A12),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "افزودن کانفیگ")
        }

        // add dialog
        if (showAddDialog) {
            AddConfigDialog(
                onDismiss = { showAddDialog = false },
                onAdded = {
                    profiles = ProfileStore.loadAll(context)
                    selectedId = ProfileStore.getSelectedId(context)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun AddConfigDialog(onDismiss: () -> Unit, onAdded: () -> Unit) {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = androidx.compose.ui.graphics.Color(0xFF141426),
        title = { Text("افزودن کانفیگ", color = NeonCyan, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "لینک کانفیگ یا محتوای اشتراک (base64) را اینجا بچسبان:",
                    color = TextDim,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = TextDim.copy(alpha = 0.4f),
                        cursorColor = NeonCyan
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val text = input.trim()
                if (text.isEmpty()) return@TextButton
                val added = importConfigs(context, text)
                Toast.makeText(
                    context,
                    if (added > 0) "$added کانفیگ اضافه شد ✅" else "کانفیگ معتبری پیدا نشد ❌",
                    Toast.LENGTH_SHORT
                ).show()
                if (added > 0) onAdded()
            }) { Text("افزودن", color = NeonGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("لغو", color = TextDim) }
        }
    )
}

/** Imports single links or a whole subscription blob; returns count added. */
private fun importConfigs(context: android.content.Context, text: String): Int {
    val trimmed = text.trim()
    val isSubscription = !trimmed.contains("://") ||
        trimmed.count { it == '\n' } > 0 && trimmed.lineSequence().any { !it.contains("://") }

    val parsed: List<Profile> = if (isSubscription) {
        LinkParser.parseSubscription(trimmed)
    } else {
        listOfNotNull(LinkParser.parse(trimmed))
    }

    parsed.forEach { ProfileStore.add(context, it) }
    if (parsed.isNotEmpty()) {
        // auto-select the first newly added profile if nothing is selected yet
        if (ProfileStore.getSelectedId(context) == null) {
            ProfileStore.setSelectedId(context, parsed.first().id)
        }
        VpnEventBus.log("${parsed.size} کانفیگ وارد شد ✅")
    }
    return parsed.size
}
