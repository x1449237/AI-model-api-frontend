package com.aifront.ui.screens.history

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aifront.data.model.Conversation
import com.aifront.data.model.ConversationType
import com.aifront.ui.components.EmptyStateView
import com.aifront.ui.theme.*
import com.aifront.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    chatViewModel: ChatViewModel = viewModel(),
    onConversationClick: (Long) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val conversations by chatViewModel.allConversations.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    var selectedFilter by remember { mutableStateOf("全部") }
    val filters = listOf("全部", "文本对话", "代码生成", "图片生成", "集群对比")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("历史记录", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            actions = { IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, contentDescription = "设置", tint = Gray600) } }
        )

        ScrollableTabRow(selectedTabIndex = filters.indexOf(selectedFilter).coerceAtLeast(0), modifier = Modifier.fillMaxWidth(), edgePadding = 16.dp, divider = {}) {
            filters.forEach { filter ->
                Tab(selected = selectedFilter == filter, onClick = { selectedFilter = filter }, text = { Text(filter, fontSize = 13.sp, color = if (selectedFilter == filter) Blue600 else Gray500) })
            }
        }

        if (conversations.isEmpty()) {
            EmptyStateView(title = "暂无历史记录", subtitle = "开始对话后，记录将自动保存到这里")
        } else {
            val filtered = when (selectedFilter) {
                "文本对话" -> conversations.filter { it.conversationType == ConversationType.TEXT }
                "代码生成" -> conversations.filter { it.conversationType == ConversationType.CODE }
                "图片生成" -> conversations.filter { it.conversationType == ConversationType.IMAGE }
                "集群对比" -> conversations.filter { it.conversationType == ConversationType.CLUSTER }
                else -> conversations
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered) { conversation ->
                    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                    Card(modifier = Modifier.fillMaxWidth().clickable { onConversationClick(conversation.id) }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            val typeIcon = when (conversation.conversationType) { ConversationType.TEXT -> Icons.Default.Chat; ConversationType.CODE -> Icons.Default.Code; ConversationType.IMAGE -> Icons.Default.Image; ConversationType.CLUSTER -> Icons.Default.Compare }
                            val typeColor = when (conversation.conversationType) { ConversationType.TEXT -> Blue600; ConversationType.CODE -> Green500; ConversationType.IMAGE -> Orange500; ConversationType.CLUSTER -> TagBest }
                            Surface(shape = RoundedCornerShape(8.dp), color = typeColor.copy(alpha = 0.1f)) { Icon(typeIcon, contentDescription = null, modifier = Modifier.padding(10.dp), tint = typeColor) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(conversation.title, fontWeight = FontWeight.Medium, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (conversation.modelName != null) Text(conversation.modelName!!, fontSize = 12.sp, color = Gray500)
                                    if (conversation.isClusterMode) Text(" · 集群", fontSize = 12.sp, color = TagBest)
                                }
                                Text("${conversation.messageCount}条消息 · ${dateFormat.format(Date(conversation.updatedAt))}", fontSize = 11.sp, color = Gray400)
                            }
                            IconButton(onClick = { showDeleteDialog = conversation.id }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, contentDescription = "删除", tint = Gray400, modifier = Modifier.size(18.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(onDismissRequest = { showDeleteDialog = null }, title = { Text("确认删除") }, text = { Text("确定要删除这条对话记录吗？此操作不可撤销。") },
            confirmButton = { TextButton(onClick = { showDeleteDialog?.let { chatViewModel.deleteConversation(it) }; showDeleteDialog = null }) { Text("删除", color = Red500) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("取消") } })
    }
}