package com.aifront.ui.screens.cluster

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aifront.data.model.VendorConfig
import com.aifront.ui.theme.*
import com.aifront.viewmodel.ChatViewModel
import com.aifront.viewmodel.ClusterViewModel
import com.aifront.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClusterScreen(
    chatViewModel: ChatViewModel = viewModel(),
    clusterViewModel: ClusterViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val isClusterMode by clusterViewModel.isClusterMode.collectAsState()
    val selectedModels by clusterViewModel.selectedModels.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val allModels = clusterViewModel.getEnabledModels()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("集群对比", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            actions = {
                Switch(checked = isClusterMode, onCheckedChange = { clusterViewModel.toggleClusterMode() }, colors = SwitchDefaults.colors(checkedTrackColor = Blue600))
                if (isClusterMode) { Text("集群模式", fontSize = 12.sp, color = if (isClusterMode) Blue600 else Gray400, modifier = Modifier.align(Alignment.CenterVertically)) }
            }
        )

        if (!isClusterMode) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(64.dp), tint = Gray300)
                Spacer(modifier = Modifier.height(16.dp))
                Text("集群对比模式", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray800)
                Spacer(modifier = Modifier.height(8.dp))
                Text("同时调用多个模型回答同一问题\n对比不同模型的效果，择优选用", fontSize = 14.sp, color = Gray500, lineHeight = 22.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("轻触右上角开关开启集群模式", fontSize = 13.sp, color = Blue600)
            }
        } else {
            Surface(modifier = Modifier.fillMaxWidth(), color = Blue50, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("已选 ${selectedModels.size} 个模型", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        if (selectedModels.isNotEmpty()) { TextButton(onClick = { clusterViewModel.clearSelection() }) { Text("清空", color = Red500) } }
                    }
                    if (selectedModels.isEmpty()) { Text("请选择2-5个模型进行对比", fontSize = 12.sp, color = Gray500) }
                    else {
                        LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                            items(selectedModels) { model ->
                                val vendor = VendorConfig.getVendorById(model.vendorId)
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green500, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${vendor?.name} - ${model.displayName}", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Text("可选模型", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            LazyColumn(modifier = Modifier.weight(0.4f).fillMaxWidth(), contentPadding = PaddingValues(horizontal = 12.dp)) {
                items(allModels) { model ->
                    val vendor = VendorConfig.getVendorById(model.vendorId)
                    val isSelected = clusterViewModel.isModelSelected(model.id)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { clusterViewModel.toggleModel(model) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Blue50 else White)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isSelected, onCheckedChange = { clusterViewModel.toggleModel(model) }, colors = CheckboxDefaults.colors(checkedColor = Blue600))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${vendor?.name} - ${model.displayName}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(model.description, fontSize = 11.sp, color = Gray500)
                            }
                        }
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = White) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = inputText, onValueChange = { inputText = it }, placeholder = { Text("输入问题，多个模型将同时回答...", color = Gray400) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue600, unfocusedBorderColor = Gray300), maxLines = 3)
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank() && selectedModels.size >= 2) {
                                val apiKey = settingsViewModel.getApiKey(selectedModels.first().vendorId)
                                chatViewModel.sendMessage(inputText.trim(), apiKey.ifEmpty { "demo-key" }, isClusterMode = true, clusterModels = selectedModels)
                                inputText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp), shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Blue600, disabledContainerColor = Gray300),
                        enabled = inputText.isNotBlank() && selectedModels.size >= 2
                    ) { Icon(Icons.Default.Send, contentDescription = "发送", tint = White) }
                }
            }
        }
    }
}