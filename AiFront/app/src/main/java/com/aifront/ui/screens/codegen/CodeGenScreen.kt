package com.aifront.ui.screens.codegen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aifront.ui.components.EmptyStateView
import com.aifront.ui.theme.*
import com.aifront.viewmodel.CodeGenViewModel
import com.aifront.viewmodel.ChatViewModel
import com.aifront.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeGenScreen(
    chatViewModel: ChatViewModel = viewModel(),
    codeGenViewModel: CodeGenViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateToVendor: () -> Unit = {}
) {
    val selectedModel by chatViewModel.selectedModel.collectAsState()
    val selectedVendor by chatViewModel.selectedVendor.collectAsState()
    val selectedLanguage by codeGenViewModel.selectedLanguage.collectAsState()
    val generatedCode by codeGenViewModel.generatedCode.collectAsState()
    val streamingCode by codeGenViewModel.streamingCode.collectAsState()
    val isGenerating by codeGenViewModel.isGenerating.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("代码生成", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            actions = { IconButton(onClick = onNavigateToVendor) { Icon(Icons.Default.Business, contentDescription = "选择模型", tint = Blue600) } }
        )

        if (selectedModel != null) {
            Row(modifier = Modifier.fillMaxWidth().background(Blue50).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = Blue600, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${selectedVendor?.name} · ${selectedModel!!.displayName}", fontSize = 13.sp, color = Blue600)
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), color = White, shadowElevation = 1.dp) {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                codeGenViewModel.supportedLanguages.forEach { lang ->
                    FilterChip(selected = selectedLanguage == lang, onClick = { codeGenViewModel.setLanguage(lang) }, label = { Text(lang, fontSize = 12.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Blue600, selectedLabelColor = White))
                }
            }
        }

        if (generatedCode.isEmpty() && !isGenerating) {
            EmptyStateView(title = "AI代码生成", subtitle = "输入需求描述，选择编程语言\nAI将为你生成完整可运行的代码", modifier = Modifier.weight(1f))
        } else {
            Column(modifier = Modifier.weight(1f).fillMaxWidth().background(Gray900).verticalScroll(rememberScrollState()).padding(16.dp)) {
                val displayCode = if (isGenerating) streamingCode else generatedCode
                Text(text = displayCode.ifEmpty { "生成中..." }, color = Green500, fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }

        if (generatedCode.isNotEmpty() && !isGenerating) {
            Row(modifier = Modifier.fillMaxWidth().background(White).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", generatedCode))
                    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("复制", fontSize = 13.sp) }
                OutlinedButton(onClick = {
                    val ext = codeGenViewModel.getFileExtension(selectedLanguage)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, generatedCode); putExtra(Intent.EXTRA_SUBJECT, "generated_code$ext") }
                    context.startActivity(Intent.createChooser(shareIntent, "导出代码"))
                }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("分享/导出", fontSize = 13.sp) }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = White) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = inputPrompt, onValueChange = { inputPrompt = it }, placeholder = { Text("描述你要生成的代码...", color = Gray400) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue600, unfocusedBorderColor = Gray300), maxLines = 3, enabled = selectedModel != null && !isGenerating)
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (inputPrompt.isNotBlank() && selectedModel != null) {
                            val vendor = selectedVendor!!
                            val apiKey = settingsViewModel.getApiKey(vendor.id)
                            codeGenViewModel.generateCode(inputPrompt.trim(), selectedModel!!, vendor, apiKey.ifEmpty { "demo-key" })
                            inputPrompt = ""
                        }
                    },
                    modifier = Modifier.size(48.dp), shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (isGenerating) Red500 else Green500, disabledContainerColor = Gray300),
                    enabled = inputPrompt.isNotBlank() && selectedModel != null
                ) { Icon(if (isGenerating) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = if (isGenerating) "停止" else "生成", tint = White) }
            }
        }
    }
}