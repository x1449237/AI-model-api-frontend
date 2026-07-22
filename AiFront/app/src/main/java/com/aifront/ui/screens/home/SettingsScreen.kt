package com.aifront.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aifront.data.model.VendorConfig
import com.aifront.ui.theme.*
import com.aifront.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val apiKeys by settingsViewModel.apiKeys.collectAsState()
    val vendors = VendorConfig.getAllVendors()
    var selectedVendorId by remember { mutableStateOf<String?>(null) }
    var apiKeyInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("API Key 管理", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
        )
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("配置各厂商API Key", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray800, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
                Text("API Key仅存储在本地设备，不会上传到任何服务器", fontSize = 12.sp, color = Gray500, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
            }
            items(vendors) { vendor ->
                val hasKey = settingsViewModel.hasApiKey(vendor.id)
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(vendor.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(vendor.nameEn, fontSize = 12.sp, color = Gray500)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (hasKey) Icons.Default.CheckCircle else Icons.Default.Warning, contentDescription = null, tint = if (hasKey) Green500 else Orange500, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (hasKey) "已配置" else "未配置", fontSize = 12.sp, color = if (hasKey) Green500 else Orange500)
                            }
                        }
                        TextButton(onClick = { selectedVendorId = vendor.id; apiKeyInput = settingsViewModel.getApiKey(vendor.id) }) { Text(if (hasKey) "修改" else "配置", color = Blue600) }
                    }
                }
            }
        }
    }

    if (selectedVendorId != null) {
        val vendor = VendorConfig.getVendorById(selectedVendorId!!)
        AlertDialog(
            onDismissRequest = { selectedVendorId = null },
            title = { Text("配置 ${vendor?.name} API Key") },
            text = {
                Column {
                    OutlinedTextField(value = apiKeyInput, onValueChange = { apiKeyInput = it }, label = { Text("API Key") }, placeholder = { Text("请输入API Key") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(8.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Key将安全存储在本地设备中", fontSize = 12.sp, color = Gray500)
                }
            },
            confirmButton = { TextButton(onClick = { settingsViewModel.setApiKey(selectedVendorId!!, apiKeyInput.trim()); selectedVendorId = null }) { Text("保存", color = Blue600) } },
            dismissButton = {
                Row {
                    if (apiKeyInput.isNotEmpty()) { TextButton(onClick = { settingsViewModel.removeApiKey(selectedVendorId!!); selectedVendorId = null }) { Text("删除", color = Red500) } }
                    TextButton(onClick = { selectedVendorId = null }) { Text("取消") }
                }
            }
        )
    }
}