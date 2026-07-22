package com.aifront.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
    val vendors = remember { VendorConfig.getAllVendors() }
    var selectedVendorId by remember { mutableStateOf<String?>(null) }
    var aiNickname by remember { mutableStateOf("") }
    var apiUrl by remember { mutableStateOf("") }
    var selectedModelId by remember { mutableStateOf<String?>(null) }
    var apiKey by remember { mutableStateOf("") }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var savedSuccess by remember { mutableStateOf(false) }

    val selectedVendor = selectedVendorId?.let { VendorConfig.getVendorById(it) }
    val vendorModels = remember(selectedVendorId) {
        selectedVendorId?.let { VendorConfig.getModelsByVendor(it) } ?: emptyList()
    }

    // Load saved config when vendor changes
    LaunchedEffect(selectedVendorId) {
        if (selectedVendorId != null) {
            aiNickname = settingsViewModel.getAiNickname(selectedVendorId!!)
            apiUrl = settingsViewModel.getEffectiveApiUrl(selectedVendorId!!)
            apiKey = settingsViewModel.getApiKey(selectedVendorId!!)
            selectedModelId = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("添加 AI 好友", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── AI 昵称 ──
            Text(
                "AI 昵称",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = aiNickname,
                onValueChange = { aiNickname = it },
                placeholder = { Text("给AI好友起个名字", color = Gray400) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue600,
                    unfocusedBorderColor = Gray300
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── 选择厂家 ──
            Text(
                "选择厂家",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(vendors) { vendor ->
                    val isSelected = selectedVendorId == vendor.id
                    val modelCount = settingsViewModel.getModelCount(vendor.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedVendorId = vendor.id
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Blue50 else White
                        ),
                        border = if (isSelected)
                            androidx.compose.foundation.BorderStroke(2.dp, Blue600)
                        else
                            androidx.compose.foundation.BorderStroke(1.dp, Gray200)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = vendor.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = vendor.nameEn,
                                fontSize = 11.sp,
                                color = Gray500,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${modelCount} 个模型",
                                fontSize = 12.sp,
                                color = if (isSelected) Blue600 else Gray500,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── API 地址 ──
            Text(
                "API 地址",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = apiUrl,
                onValueChange = { apiUrl = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("https://api.example.com/v1", color = Gray400) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue600,
                    unfocusedBorderColor = Gray300
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "选择厂家后自动填充，也支持自定义第三方代理或自建模型",
                fontSize = 12.sp,
                color = Gray400
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 模型 ──
            Text(
                "模型",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = modelDropdownExpanded,
                onExpandedChange = { modelDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedModelId?.let { id ->
                        vendorModels.find { it.id == id }?.displayName ?: id
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Text(
                            if (selectedVendorId == null) "请先选择厂家" else "请选择模型",
                            color = Gray400
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue600,
                        unfocusedBorderColor = Gray300
                    ),
                    enabled = selectedVendorId != null
                )
                ExposedDropdownMenu(
                    expanded = modelDropdownExpanded,
                    onDismissRequest = { modelDropdownExpanded = false }
                ) {
                    vendorModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(model.displayName, fontWeight = FontWeight.Medium)
                                    Text(model.description, fontSize = 12.sp, color = Gray500)
                                }
                            },
                            onClick = {
                                selectedModelId = model.id
                                modelDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── API Key ──
            Text(
                "API Key",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("sk-...", color = Gray400) },
                visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                        Icon(
                            if (apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (apiKeyVisible) "隐藏" else "显示",
                            tint = Gray500
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue600,
                    unfocusedBorderColor = Gray300
                )
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "API Key 仅保存在本地设备，不会上传到服务器",
                fontSize = 12.sp,
                color = Gray400
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── 保存按钮 ──
            Button(
                onClick = {
                    if (selectedVendorId != null && apiKey.isNotBlank()) {
                        settingsViewModel.setApiKey(selectedVendorId!!, apiKey.trim())
                        settingsViewModel.setCustomApiUrl(selectedVendorId!!, apiUrl.trim())
                        if (aiNickname.isNotBlank()) {
                            settingsViewModel.setAiNickname(selectedVendorId!!, aiNickname.trim())
                        }
                        savedSuccess = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                enabled = selectedVendorId != null && apiKey.isNotBlank()
            ) {
                Text("保存配置", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            if (savedSuccess) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "保存成功！",
                    fontSize = 14.sp,
                    color = Green500,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    savedSuccess = false
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}