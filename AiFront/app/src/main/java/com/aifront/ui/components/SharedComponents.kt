package com.aifront.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifront.ui.theme.*

@Composable
fun ModelTag(text: String, backgroundColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            color = backgroundColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ChatBubble(
    content: String,
    isUser: Boolean,
    modelName: String? = null,
    vendorName: String? = null,
    responseTimeMs: Long = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (!isUser && modelName != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
                    Text(text = modelName, fontSize = 11.sp, color = Gray500, fontWeight = FontWeight.Medium)
                    if (vendorName != null) Text(text = " · $vendorName", fontSize = 11.sp, color = Gray400)
                    if (responseTimeMs > 0) Text(text = " · ${responseTimeMs}ms", fontSize = 11.sp, color = Gray400)
                }
            }
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) Blue600 else White,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = content,
                    color = if (isUser) White else Gray900,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun StreamingIndicator(content: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(modifier = Modifier.widthIn(max = 320.dp)) {
            Surface(
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                color = White,
                shadowElevation = 1.dp
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = content.ifEmpty { "思考中..." },
                        color = if (content.isEmpty()) Gray400 else Gray900,
                        fontSize = 15.sp, lineHeight = 22.sp
                    )
                    if (content.isEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Blue600)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Gray500, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = subtitle, fontSize = 14.sp, color = Gray400, textAlign = TextAlign.Center)
    }
}