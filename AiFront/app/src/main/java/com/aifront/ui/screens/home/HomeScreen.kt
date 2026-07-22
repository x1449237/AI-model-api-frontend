package com.aifront.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.aifront.ui.components.*
import com.aifront.ui.theme.*
import com.aifront.viewmodel.ChatViewModel
import com.aifront.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    chatViewModel: ChatViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateToVendor: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val selectedModel by chatViewModel.selectedModel.collectAsState()
    val selectedVendor by chatViewModel.selectedVendor.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val streamingContent by chatViewModel.streamingContent.collectAsState()
    val isStreaming by chatViewModel.isStreaming.collectAsState()
    val temperature by chatViewModel.temperature.collectAsState()
    val topP by chatViewModel.topP.collectAsState()
    val maxTokens by chatViewModel.maxTokens.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showParamSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, streamingContent) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text("AI Front", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (selectedModel != null) {
                        Text("${selectedVendor?.name ?: ""} · ${selectedModel!!.displayName}", fontSize = 12.sp, color = Gray400)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            actions = {
                IconButton(onClick = onNavigateToVendor) { Icon(Icons.Default.Business, contentDescription = "选择厂商", tint = Blue600) }
                IconButton(onClick = { showParamSheet = true }) { Icon(Icons.Default.Tune, contentDescription = "参数", tint = Gray600) }
                IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, contentDescription = "设置", tint = Gray600) }
            }
        )

        if (selectedModel != null) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Blue50).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (selectedModel!!.isLatest) ModelTag("最新", TagLatest)
                if (selectedModel!!.isBestPerformance) ModelTag("性能最佳", TagBest)
                if (selectedModel!!.supportsCode) ModelTag("代码", TagCode)
                if (selectedModel!!.supportsImage) ModelTag("图片", TagImage)
                ModelTag("${selectedModel!!.contextLength / 1000}K上下文", Gray600)
            }
        }

        if (messages.isEmpty()) {
            EmptyStateView(title = "开始对话", subtitle = "选择厂商和模型后，输入消息开始对话\n支持流式输出和实时响应")
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(content = message.content, isUser = message.role == "user", modelName = message.modelName, vendorName = message.vendorName, responseTimeMs = message.responseTimeMs)
                }
                if (isStreaming && streamingContent.isNotEmpty()) {
                    item { StreamingIndicator(content = streamingContent) }
                }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = White) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(if (selectedModel == null) "请先选择模型" else "输入消息...", color = Gray400) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue600, unfocusedBorderColor = Gray300),
                    maxLines = 4,
                    enabled = selectedModel != null
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank() && selectedModel != null) {
                            val vendor = selectedVendor!!
                            val apiKey = settingsViewModel.getApiKey(vendor.id)
                            chatViewModel.sendMessage(inputText.trim(), apiKey.ifEmpty { "demo-key" })
                            inputText = ""
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Blue600, disabledContainerColor = Gray300),
                    enabled = inputText.isNotBlank() && selectedModel != null
                ) {
                    Icon(Icons.Default.Send, contentDescription = "发送", tint = White)
                }
            }
        }
    }

    if (showParamSheet) {
        ModalBottomSheet(onDismissRequest = { showParamSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("参数设置", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Temperature: ${"%.1f".format(temperature)}")
                Slider(value = temperature, onValueChange = { chatViewModel.setTemperature(it) }, valueRange = 0f..1f, steps = 9)
                Text("控制输出随机性，越高越有创意", fontSize = 12.sp, color = Gray500)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Top-P: ${"%.1f".format(topP)}")
                Slider(value = topP, onValueChange = { chatViewModel.setTopP(it) }, valueRange = 0f..1f, steps = 9)
                Text("核采样概率，控制输出多样性", fontSize = 12.sp, color = Gray500)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Max Tokens: $maxTokens")
                Slider(value = maxTokens.toFloat(), onValueChange = { chatViewModel.setMaxTokens(it.toInt()) }, valueRange = 256f..16384f, steps = 15)
                Text("最大输出token数", fontSize = 12.sp, color = Gray500)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}