package com.aifront.ui.screens.imagegen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aifront.ui.components.EmptyStateView
import com.aifront.ui.theme.*
import com.aifront.viewmodel.ChatViewModel
import com.aifront.viewmodel.ImageGenViewModel
import com.aifront.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGenScreen(
    chatViewModel: ChatViewModel = viewModel(),
    imageGenViewModel: ImageGenViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateToImageModel: () -> Unit = {}
) {
    val selectedModel by chatViewModel.selectedModel.collectAsState()
    val selectedVendor by chatViewModel.selectedVendor.collectAsState()
    val generatedImages by imageGenViewModel.generatedImages.collectAsState()
    val isGenerating by imageGenViewModel.isGenerating.collectAsState()
    val selectedRatio by imageGenViewModel.selectedRatio.collectAsState()
    val imageCount by imageGenViewModel.imageCount.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    var showRatioSelector by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("图片生成", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            actions = { IconButton(onClick = onNavigateToImageModel) { Icon(Icons.Default.Image, contentDescription = "选择图片模型", tint = Blue600) } }
        )

        if (selectedModel != null) {
            Row(modifier = Modifier.fillMaxWidth().background(Blue50).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = Blue600, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${selectedVendor?.name} · ${selectedModel!!.displayName}", fontSize = 13.sp, color = Blue600)
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), color = White, shadowElevation = 1.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(selected = false, onClick = { showRatioSelector = true }, label = { Text(selectedRatio, fontSize = 12.sp) }, leadingIcon = { Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(16.dp)) })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { imageGenViewModel.setImageCount(imageCount - 1) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    Text("$imageCount", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    IconButton(onClick = { imageGenViewModel.setImageCount(imageCount + 1) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("张", fontSize = 12.sp, color = Gray500)
            }
        }

        if (generatedImages.isEmpty() && !isGenerating) {
            EmptyStateView(title = "AI图片生成", subtitle = "选择支持图像生成的模型\n输入提示词，AI将为你生成图片", modifier = Modifier.weight(1f))
        } else {
            if (isGenerating && generatedImages.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Blue600)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在生成图片...", color = Gray500)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (generatedImages.size == 1) 1 else 2),
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(generatedImages.size) { index ->
                        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(generatedImages[index]).crossfade(true).build(),
                                contentDescription = "生成的图片 ${index + 1}",
                                modifier = Modifier.fillMaxWidth().aspectRatio(when (selectedRatio) { "9:16" -> 9f/16f; "16:9" -> 16f/9f; else -> 1f }),
                                contentScale = ContentScale.Crop
                            )
                            Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, generatedImages[index]) }
                                    context.startActivity(Intent.createChooser(shareIntent, "分享图片"))
                                }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Share, contentDescription = "分享", modifier = Modifier.size(16.dp)) }
                            }
                        }
                    }
                }
            }
        }

        if (showRatioSelector) {
            AlertDialog(onDismissRequest = { showRatioSelector = false }, title = { Text("选择比例") }, text = {
                Column {
                    imageGenViewModel.imageRatios.forEach { ratio ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            RadioButton(selected = selectedRatio == ratio, onClick = { imageGenViewModel.setRatio(ratio); showRatioSelector = false })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(ratio, modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { showRatioSelector = false }) { Text("关闭") } })
        }

        Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = White) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = inputPrompt, onValueChange = { inputPrompt = it }, placeholder = { Text("描述你想要的图片...", color = Gray400) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue600, unfocusedBorderColor = Gray300), maxLines = 3, enabled = selectedModel != null && !isGenerating)
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (inputPrompt.isNotBlank() && selectedModel != null) {
                            val vendor = selectedVendor!!
                            val apiKey = settingsViewModel.getApiKey(vendor.id)
                            val customUrl = settingsViewModel.getCustomApiUrl(vendor.id)
                            imageGenViewModel.generateImage(inputPrompt.trim(), selectedModel!!, vendor, apiKey.ifEmpty { "demo-key" }, customUrl)
                            inputPrompt = ""
                        }
                    },
                    modifier = Modifier.size(48.dp), shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (isGenerating) Red500 else Orange500, disabledContainerColor = Gray300),
                    enabled = inputPrompt.isNotBlank() && selectedModel != null
                ) { Icon(if (isGenerating) Icons.Default.Stop else Icons.Default.AutoAwesome, contentDescription = "生成", tint = White) }
            }
        }
    }
}