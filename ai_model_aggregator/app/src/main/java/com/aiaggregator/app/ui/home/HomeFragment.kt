package com.aiaggregator.app.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiaggregator.app.R
import com.aiaggregator.app.data.ApiService
import com.aiaggregator.app.data.VendorConfig
import com.aiaggregator.app.models.AiModel
import com.aiaggregator.app.models.ChatMessage
import com.aiaggregator.app.models.Vendor
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {

    private lateinit var apiService: ApiService
    private var selectedModel: AiModel? = null
    private val messages = mutableListOf<ChatMessage>()
    private var isStreaming = false
    private var streamingText = ""
    private var temperature = 0.7
    private var topP = 0.9
    private var maxTokens = 2048
    private lateinit var adapter: ChatAdapter
    private val handler = Handler(Looper.getMainLooper())

    // Blinking cursor
    private var showCursor = true
    private var blinkRunnable: Runnable? = null
    private var streamingIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = ApiService(requireContext())

        val recycler = view.findViewById<RecyclerView>(R.id.chat_recycler)
        adapter = ChatAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        setupModelSelector(view)
        setupParamsButton(view)
        setupInput(view)
    }

    // ==================== Model Selector ====================

    private fun setupModelSelector(view: View) {
        view.findViewById<MaterialButton>(R.id.btn_model_selector).setOnClickListener {
            showVendorPicker()
        }
    }

    private fun showVendorPicker() {
        val vendors = VendorConfig.vendors
        val names = vendors.map { "${it.name} (${it.nameEn})" }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("选择厂商")
            .setItems(names) { _, which ->
                val vendor = vendors[which]
                showModelPicker(vendor)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showModelPicker(vendor: Vendor) {
        val models = VendorConfig.modelsByVendor[vendor.id]
            ?.filter { it.supportsCode && !it.supportsImage }
            ?: emptyList()

        if (models.isEmpty()) {
            Toast.makeText(requireContext(), "${vendor.name} 暂无可用文字模型", Toast.LENGTH_SHORT).show()
            return
        }

        val names = models.mapIndexed { i, m ->
            val badge = when {
                m.isLatest && m.isBestPerformance -> "⭐最新·最强"
                m.isLatest -> "⭐最新"
                m.isBestPerformance -> "最强"
                else -> ""
            }
            if (badge.isNotEmpty()) "${m.name}  $badge" else m.name
        }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("${vendor.name} 模型")
            .setItems(names) { _, which ->
                selectedModel = models[which]
                updateModelButton()
                updateInputHint()
            }
            .setNegativeButton("返回", null)
            .show()
    }

    private fun updateModelButton() {
        val btn = view?.findViewById<MaterialButton>(R.id.btn_model_selector) ?: return
        val model = selectedModel
        if (model != null) {
            btn.text = "${model.vendorName} · ${model.name}"
            btn.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            )
        } else {
            btn.text = "选择模型"
            btn.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.colorTextSecondary)
            )
        }
    }

    private fun updateInputHint() {
        val input = view?.findViewById<EditText>(R.id.input_message)
        input?.hint = if (selectedModel != null) {
            "向 ${selectedModel!!.vendorName} ${selectedModel!!.name} 发送消息..."
        } else {
            "请先选择模型..."
        }
    }

    // ==================== Parameter Bottom Sheet ====================

    private fun setupParamsButton(view: View) {
        view.findViewById<MaterialButton>(R.id.btn_params).setOnClickListener {
            showParamsDialog()
        }
    }

    private fun showParamsDialog() {
        val ctx = requireContext()
        // Build a custom layout for the dialog
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 16)
        }

        // Temperature
        val tempLabel = TextView(ctx).apply {
            text = "Temperature: ${String.format("%.2f", temperature)}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
            setPadding(0, 0, 0, 8)
        }
        container.addView(tempLabel)

        val tempBar = SeekBar(ctx).apply {
            max = 100
            progress = (temperature * 100).toInt()
            setPadding(0, 0, 0, 16)
        }
        tempBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                temperature = p / 100.0
                tempLabel.text = "Temperature: ${String.format("%.2f", temperature)}"
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        container.addView(tempBar)

        // Top-P
        val topPLabel = TextView(ctx).apply {
            text = "Top-P: ${String.format("%.2f", topP)}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
            setPadding(0, 0, 0, 8)
        }
        container.addView(topPLabel)

        val topPBar = SeekBar(ctx).apply {
            max = 100
            progress = (topP * 100).toInt()
            setPadding(0, 0, 0, 16)
        }
        topPBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                topP = p / 100.0
                topPLabel.text = "Top-P: ${String.format("%.2f", topP)}"
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        container.addView(topPBar)

        // Max Tokens
        val maxTokensLabel = TextView(ctx).apply {
            text = "Max Tokens: $maxTokens"
            textSize = 14f
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
            setPadding(0, 0, 0, 8)
        }
        container.addView(maxTokensLabel)

        val maxTokensBar = SeekBar(ctx).apply {
            max = 8000
            progress = maxTokens.coerceAtMost(8000)
            setPadding(0, 0, 0, 16)
        }
        maxTokensBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                maxTokens = if (p < 256) 256 else p
                maxTokensLabel.text = "Max Tokens: $maxTokens"
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        container.addView(maxTokensBar)

        androidx.appcompat.app.AlertDialog.Builder(ctx)
            .setTitle("参数设置")
            .setView(container)
            .setPositiveButton("确定", null)
            .show()
    }

    // ==================== Input ====================

    private fun setupInput(view: View) {
        val input = view.findViewById<EditText>(R.id.input_message)
        val sendBtn = view.findViewById<MaterialButton>(R.id.btn_send)

        sendBtn.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                input.setText("")
            }
        }
    }

    // ==================== Send Message ====================

    private fun sendMessage(text: String) {
        if (text.isEmpty() || selectedModel == null || isStreaming) {
            if (selectedModel == null) {
                Toast.makeText(requireContext(), "请先选择模型", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val userMsg = ChatMessage(
            role = "user",
            content = text,
            modelId = selectedModel!!.id,
            modelName = selectedModel!!.name,
            vendorName = selectedModel!!.vendorName
        )
        messages.add(userMsg)
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()

        isStreaming = true
        streamingText = ""
        val streamMsg = ChatMessage(
            role = "assistant",
            content = "",
            modelId = selectedModel!!.id,
            modelName = selectedModel!!.name,
            vendorName = selectedModel!!.vendorName
        )
        messages.add(streamMsg)
        streamingIndex = messages.size - 1
        adapter.notifyItemInserted(streamingIndex)
        startBlinkCursor()

        val startTime = System.currentTimeMillis()

        apiService.sendMessage(
            model = selectedModel!!,
            prompt = text,
            temperature = temperature,
            topP = topP,
            maxTokens = maxTokens,
            onChunk = { chunk ->
                streamingText += chunk
                messages[streamingIndex] = streamMsg.copy(content = streamingText)
                handler.post {
                    adapter.notifyItemChanged(streamingIndex)
                    scrollToBottom()
                }
            },
            onComplete = { response ->
                val elapsed = System.currentTimeMillis() - startTime
                val finalContent = if (streamingText.isNotEmpty()) streamingText else response
                val tokenCount = estimateTokens(finalContent)
                messages[streamingIndex] = streamMsg.copy(
                    content = finalContent,
                    responseTimeMs = elapsed,
                    tokenCount = tokenCount
                )
                isStreaming = false
                stopBlinkCursor()
                handler.post {
                    adapter.notifyItemChanged(streamingIndex)
                    scrollToBottom()
                    updateTotalTokenCount()
                }
            },
            onError = { error ->
                val elapsed = System.currentTimeMillis() - startTime
                val friendlyError = parseError(error)
                messages[streamingIndex] = streamMsg.copy(
                    content = friendlyError,
                    responseTimeMs = elapsed
                )
                isStreaming = false
                stopBlinkCursor()
                handler.post {
                    adapter.notifyItemChanged(streamingIndex)
                }
            }
        )
    }

    private fun regenerate() {
        if (isStreaming) return
        if (messages.isEmpty()) return

        // Find the last user message
        val lastUserIdx = messages.indexOfLast { it.role == "user" }
        if (lastUserIdx < 0) return

        val lastUserMsg = messages[lastUserIdx]

        // Remove all messages from the last user message onward
        while (messages.size > lastUserIdx) {
            messages.removeAt(messages.size - 1)
        }
        adapter.notifyDataSetChanged()

        // Re-send the last user message
        sendMessage(lastUserMsg.content)
    }

    // ==================== Token Estimation ====================

    private fun estimateTokens(text: String): Int {
        if (text.isEmpty()) return 0
        var chineseChars = 0
        var otherChars = 0
        for (ch in text) {
            if (ch in '\u4e00'..'\u9fff' || ch in '\u3400'..'\u4dbf' ||
                ch in '\uf900'..'\ufaff' || ch in '\u3000'..'\u303f'
            ) {
                chineseChars++
            } else if (!ch.isWhitespace()) {
                otherChars++
            }
        }
        // Chinese: ~1.5 chars per token, English: ~4 chars per token
        val tokens = (chineseChars / 1.5 + otherChars / 4.0).toInt()
        return tokens.coerceAtLeast(1)
    }

    private fun updateTotalTokenCount() {
        val total = messages.sumOf { it.tokenCount }
        val counter = view?.findViewById<TextView>(R.id.token_counter)
        if (total > 0) {
            counter?.visibility = View.VISIBLE
            counter?.text = "≈$total"
        } else {
            counter?.visibility = View.GONE
        }
    }

    // ==================== Error Parsing ====================

    private fun parseError(error: String): String {
        return when {
            error.contains("401") || error.contains("Unauthorized") || error.contains("未授权") ->
                "🔑 API Key 无效或未配置\n\n请在「我的」页面中配置正确的 API Key 后重试。"
            error.contains("403") || error.contains("Forbidden") ->
                "🚫 访问被拒绝\n\n请检查 API Key 权限是否足够。"
            error.contains("429") || error.contains("Too Many") ->
                "⏳ 请求过于频繁\n\n请稍等片刻后再试。"
            error.contains("500") || error.contains("Internal Server") ->
                "⚠️ 服务端错误\n\n模型服务暂时不可用，请稍后重试。"
            error.contains("timeout") || error.contains("超时") || error.contains("Timeout") ->
                "⏰ 请求超时\n\n网络连接较慢或模型响应时间过长，请重试。"
            error.contains("Unable to resolve") || error.contains("No address") ->
                "🌐 网络连接失败\n\n请检查网络连接后重试。"
            error.contains("网络请求失败") ->
                "🌐 网络请求失败: ${error.removePrefix("网络请求失败: ")}\n\n请检查网络连接后重试。"
            else ->
                "❌ 请求失败\n\n$error"
        }
    }

    // ==================== Blinking Cursor ====================

    private fun startBlinkCursor() {
        showCursor = true
        blinkRunnable = object : Runnable {
            override fun run() {
                if (!isStreaming || streamingIndex < 0 || streamingIndex >= messages.size) return
                showCursor = !showCursor
                adapter.notifyItemChanged(streamingIndex)
                handler.postDelayed(this, 500)
            }
        }
        handler.postDelayed(blinkRunnable!!, 500)
    }

    private fun stopBlinkCursor() {
        blinkRunnable?.let { handler.removeCallbacks(it) }
        blinkRunnable = null
        showCursor = false
        if (streamingIndex >= 0 && streamingIndex < messages.size) {
            adapter.notifyItemChanged(streamingIndex)
        }
    }

    // ==================== Scroll ====================

    private fun scrollToBottom() {
        view?.findViewById<RecyclerView>(R.id.chat_recycler)?.let {
            it.post { it.smoothScrollToPosition(messages.size - 1) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopBlinkCursor()
    }

    // ==================== ChatAdapter ====================

    inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            return if (messages[position].role == "user") VIEW_TYPE_USER else VIEW_TYPE_AI
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_message, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(messages[position], position)
        }

        override fun getItemCount() = messages.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val modelLabel: LinearLayout = itemView.findViewById(R.id.model_label)
            private val modelInfo: TextView = itemView.findViewById(R.id.model_info)
            private val modelAvatar: TextView = itemView.findViewById(R.id.model_avatar)
            private val bubbleContainer: LinearLayout = itemView.findViewById(R.id.bubble_container)
            private val bubbleCard: MaterialCardView = itemView.findViewById(R.id.bubble_card)
            private val bubbleText: TextView = itemView.findViewById(R.id.bubble_text)
            private val tokenInfo: TextView = itemView.findViewById(R.id.token_info)
            private val btnCopy: MaterialButton = itemView.findViewById(R.id.btn_copy)

            fun bind(msg: ChatMessage, position: Int) {
                val isUser = msg.role == "user"
                val ctx = itemView.context

                // --- Bubble layout ---
                val lp = bubbleContainer.layoutParams as LinearLayout.LayoutParams
                if (isUser) {
                    // Right-aligned user bubble
                    lp.gravity = Gravity.END
                    modelLabel.visibility = View.GONE
                    tokenInfo.visibility = View.GONE
                    btnCopy.visibility = View.GONE
                    bubbleCard.setCardBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.colorUserBubble)
                    )
                    bubbleText.setTextColor(
                        ContextCompat.getColor(ctx, android.R.color.white)
                    )
                } else {
                    // Left-aligned AI bubble
                    lp.gravity = Gravity.START
                    modelLabel.visibility = View.VISIBLE

                    // Model info: vendor - model | responseTime | tokenCount
                    val infoParts = mutableListOf<String>()
                    infoParts.add("${msg.vendorName} · ${msg.modelName}")
                    if (msg.responseTimeMs > 0) {
                        infoParts.add("${msg.responseTimeMs}ms")
                    }
                    modelInfo.text = infoParts.joinToString("  ")

                    // Avatar initial
                    modelAvatar.text = msg.vendorName.take(1)

                    // Token info
                    if (msg.tokenCount > 0 && !isStreaming) {
                        tokenInfo.visibility = View.VISIBLE
                        tokenInfo.text = "≈${msg.tokenCount} tokens"
                    } else {
                        tokenInfo.visibility = View.GONE
                    }

                    // Copy button (visible only when not streaming and has content)
                    if (msg.content.isNotEmpty() && !isStreaming) {
                        btnCopy.visibility = View.VISIBLE
                        btnCopy.setOnClickListener {
                            copyToClipboard(msg.content)
                        }
                    } else {
                        btnCopy.visibility = View.GONE
                    }

                    bubbleCard.setCardBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.colorAiBubble)
                    )
                    bubbleText.setTextColor(
                        ContextCompat.getColor(ctx, R.color.colorAiBubbleText)
                    )
                }
                bubbleContainer.layoutParams = lp

                // --- Bubble text ---
                var displayText = msg.content
                if (isStreaming && !isUser && position == messages.size - 1 && showCursor) {
                    displayText += " ▌"
                }
                bubbleText.text = displayText

                // --- Long press on AI message to show regenerate option ---
                if (!isUser && !isStreaming && msg.content.isNotEmpty()) {
                    itemView.setOnLongClickListener {
                        showMessageActions(msg)
                        true
                    }
                } else {
                    itemView.setOnLongClickListener(null)
                }
            }

            private fun copyToClipboard(text: String) {
                val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("AI Response", text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(itemView.context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMessageActions(msg: ChatMessage) {
        val ctx = requireContext()
        val options = arrayOf("复制内容", "重新生成")
        androidx.appcompat.app.AlertDialog.Builder(ctx)
            .setTitle("操作")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("AI Response", msg.content)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(ctx, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    }
                    1 -> regenerate()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_AI = 1
    }
}