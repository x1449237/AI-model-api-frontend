package com.aiaggregator.app.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiaggregator.app.R
import com.aiaggregator.app.data.ApiService
import com.aiaggregator.app.data.AppDatabase
import com.aiaggregator.app.data.ConversationEntity
import com.aiaggregator.app.data.MessageEntity
import com.aiaggregator.app.data.VendorConfig
import com.aiaggregator.app.models.AiModel
import com.aiaggregator.app.models.ChatMessage
import com.aiaggregator.app.models.Vendor
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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

    // Database
    private lateinit var conversationDao: com.aiaggregator.app.data.ConversationDao
    private lateinit var messageDao: com.aiaggregator.app.data.MessageDao
    private var currentConversationId: String = UUID.randomUUID().toString()

    // Cluster mode
    private var isClusterMode = false
    private val clusterSelectedModels = mutableSetOf<String>()

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

        // Init database
        val db = AppDatabase.getInstance(requireContext())
        conversationDao = db.conversationDao()
        messageDao = db.messageDao()

        val recycler = view.findViewById<RecyclerView>(R.id.chat_recycler)
        adapter = ChatAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        setupModelSelector(view)
        setupParamsButton(view)
        setupInput(view)
        setupConversationButtons(view)
        setupClusterMode(view)
        setupTemplatesButton(view)
    }

    // ==================== Conversation Management ====================

    private fun setupConversationButtons(view: View) {
        val btnNewChat = view.findViewById<MaterialButton>(R.id.btn_new_chat)
        val btnHistory = view.findViewById<MaterialButton>(R.id.btn_history)

        btnNewChat.setOnClickListener {
            startNewConversation()
        }

        btnHistory.setOnClickListener {
            showConversationHistory()
        }
    }

    private fun startNewConversation() {
        if (isStreaming) return
        currentConversationId = UUID.randomUUID().toString()
        messages.clear()
        adapter.notifyDataSetChanged()
        updateConversationTitle("新对话")
        updateTotalTokenCount()
    }

    private fun updateConversationTitle(title: String) {
        val titleView = view?.findViewById<TextView>(R.id.conversation_title)
        titleView?.text = title
    }

    private fun showConversationHistory() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            // Use runBlocking to get all conversations via Flow
            val conversations = runBlocking {
                val list = mutableListOf<ConversationEntity>()
                val job = launch {
                    conversationDao.getAllConversations().collect { list.addAll(it) }
                }
                // Cancel after first emission
                handler.postDelayed({ job.cancel() }, 500)
                job.join()
                list
            }

            withContext(Dispatchers.Main) {
                val ctx = requireContext()
                if (conversations.isEmpty()) {
                    AlertDialog.Builder(ctx)
                        .setTitle("对话历史")
                        .setMessage("暂无历史对话")
                        .setPositiveButton("确定", null)
                        .show()
                    return@withContext
                }

                val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                val titles = conversations.map {
                    val date = dateFormat.format(Date(it.updatedAt))
                    val title = if (it.title.length > 20) it.title.take(20) + "..." else it.title
                    "$title  |  ${it.modelName}  |  $date"
                }.toTypedArray()

                val dialog = AlertDialog.Builder(ctx)
                    .setTitle("对话历史")
                    .setItems(titles) { _, which ->
                        loadConversation(conversations[which])
                    }
                    .setPositiveButton("新建对话") { _, _ ->
                        startNewConversation()
                    }
                    .setNegativeButton("取消", null)
                    .create()

                dialog.show()
            }
        }
    }

    private fun loadConversation(conv: ConversationEntity) {
        if (isStreaming) return
        currentConversationId = conv.id
        updateConversationTitle(conv.title)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val entities = messageDao.getMessagesByConversationSync(conv.id)
            val chatMessages = entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    role = entity.role,
                    content = entity.content,
                    modelId = entity.modelId,
                    modelName = entity.modelName,
                    vendorName = entity.vendorName,
                    timestamp = entity.timestamp,
                    responseTimeMs = entity.responseTimeMs,
                    tokenCount = entity.tokenCount
                )
            }

            withContext(Dispatchers.Main) {
                messages.clear()
                messages.addAll(chatMessages)
                adapter.notifyDataSetChanged()
                scrollToBottom()
                updateTotalTokenCount()
            }
        }
    }

    private fun saveConversationToDb(title: String) {
        val conv = ConversationEntity(
            id = currentConversationId,
            title = title,
            modelId = selectedModel?.id ?: "",
            modelName = selectedModel?.name ?: "",
            vendorName = selectedModel?.vendorName ?: "",
            type = if (isClusterMode) "cluster" else "text",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            conversationDao.insertConversation(conv)
        }
    }

    private fun saveMessageToDb(msg: ChatMessage) {
        val entity = MessageEntity(
            id = msg.id,
            conversationId = currentConversationId,
            role = msg.role,
            content = msg.content,
            modelId = msg.modelId,
            modelName = msg.modelName,
            vendorName = msg.vendorName,
            timestamp = msg.timestamp,
            responseTimeMs = msg.responseTimeMs,
            tokenCount = msg.tokenCount
        )
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            messageDao.insertMessage(entity)
        }
    }

    private fun deleteConversation(conv: ConversationEntity) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            conversationDao.deleteConversation(conv)
            if (conv.id == currentConversationId) {
                withContext(Dispatchers.Main) {
                    startNewConversation()
                }
            }
        }
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

        AlertDialog.Builder(requireContext())
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

        AlertDialog.Builder(requireContext())
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

        AlertDialog.Builder(ctx)
            .setTitle("参数设置")
            .setView(container)
            .setPositiveButton("确定", null)
            .show()
    }

    // ==================== Templates ====================

    private fun setupTemplatesButton(view: View) {
        view.findViewById<MaterialButton>(R.id.btn_templates).setOnClickListener {
            showTemplatesDialog()
        }
    }

    private fun showTemplatesDialog() {
        val templates = listOf(
            "翻译成英文" to "请将以下内容翻译成英文：\n",
            "翻译成中文" to "请将以下内容翻译成中文：\n",
            "总结要点" to "请总结以下内容的要点：\n",
            "扩写内容" to "请扩写以下内容，使其更加丰富详细：\n",
            "优化表达" to "请优化以下内容的表达，使其更加流畅专业：\n",
            "代码解释" to "请解释以下代码的功能和逻辑：\n",
            "写邮件" to "请根据以下要点撰写一封专业邮件：\n",
            "头脑风暴" to "请针对以下主题进行头脑风暴，提供多个创意方向：\n"
        )

        val items = templates.map { it.first }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("预设提示词模板")
            .setItems(items) { _, which ->
                val template = templates[which].second
                val input = view?.findViewById<EditText>(R.id.input_message)
                input?.apply {
                    val currentText = text.toString()
                    if (currentText.isNotEmpty()) {
                        setText("$template$currentText")
                    } else {
                        setText(template)
                    }
                    setSelection(text.length)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // ==================== Cluster Mode ====================

    private fun setupClusterMode(view: View) {
        val switchCluster = view.findViewById<Switch>(R.id.switch_cluster_mode)
        val clusterArea = view.findViewById<LinearLayout>(R.id.cluster_mode_area)

        switchCluster.setOnCheckedChangeListener { _, isChecked ->
            isClusterMode = isChecked
            clusterArea.visibility = if (isChecked) View.VISIBLE else View.GONE
            updateSendButton()
            if (isChecked) {
                buildClusterModelChips(view)
            }
        }
    }

    private fun buildClusterModelChips(view: View) {
        val chipsContainer = view.findViewById<LinearLayout>(R.id.cluster_model_chips)
        chipsContainer.removeAllViews()

        val allModels = VendorConfig.getAllModels().filter { it.supportsCode && !it.supportsImage }
        clusterSelectedModels.clear()

        for (model in allModels) {
            val chip = Chip(requireContext()).apply {
                text = "${model.vendorName} · ${model.name}"
                isCheckable = true
                isChecked = false
                chipIconSize = 0f
                textSize = 11f
                chipStrokeWidth = 1f
                setChipStrokeColorResource(R.color.colorDivider)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        if (clusterSelectedModels.size < 5) {
                            clusterSelectedModels.add(model.id)
                        } else {
                            isChecked = false
                            Toast.makeText(requireContext(), "最多选择5个模型", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        clusterSelectedModels.remove(model.id)
                    }
                }
            }
            chipsContainer.addView(chip)
        }
    }

    private fun updateSendButton() {
        val sendBtn = view?.findViewById<MaterialButton>(R.id.btn_send) ?: return
        if (isClusterMode) {
            sendBtn.text = "集群对比"
            sendBtn.setIconResource(android.R.drawable.ic_menu_compass)
            sendBtn.iconTint = ContextCompat.getColorStateList(requireContext(), R.color.colorSendButton)
        } else {
            sendBtn.text = ""
            sendBtn.setIconResource(R.drawable.ic_send)
            sendBtn.iconTint = ContextCompat.getColorStateList(requireContext(), R.color.colorSendButton)
        }
    }

    // ==================== Input ====================

    private fun setupInput(view: View) {
        val input = view.findViewById<EditText>(R.id.input_message)
        val sendBtn = view.findViewById<MaterialButton>(R.id.btn_send)

        sendBtn.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                if (isClusterMode) {
                    sendClusterMessage(text)
                } else {
                    sendMessage(text)
                }
                input.setText("")
            }
        }
    }

    // ==================== Send Message (Normal) ====================

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

        // Save user message to DB
        saveMessageToDb(userMsg)

        // Update conversation title & save
        val title = if (text.length > 30) text.take(30) + "..." else text
        updateConversationTitle(title)
        saveConversationToDb(title)

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
                val finalMsg = streamMsg.copy(
                    content = finalContent,
                    responseTimeMs = elapsed,
                    tokenCount = tokenCount
                )
                messages[streamingIndex] = finalMsg
                isStreaming = false
                stopBlinkCursor()

                // Save assistant message to DB
                saveMessageToDb(finalMsg)

                handler.post {
                    adapter.notifyItemChanged(streamingIndex)
                    scrollToBottom()
                    updateTotalTokenCount()
                }
            },
            onError = { error ->
                val elapsed = System.currentTimeMillis() - startTime
                val friendlyError = parseError(error)
                val errorMsg = streamMsg.copy(
                    content = friendlyError,
                    responseTimeMs = elapsed
                )
                messages[streamingIndex] = errorMsg
                isStreaming = false
                stopBlinkCursor()

                // Save error message to DB
                saveMessageToDb(errorMsg)

                handler.post {
                    adapter.notifyItemChanged(streamingIndex)
                }
            }
        )
    }

    // ==================== Send Cluster Message ====================

    private fun sendClusterMessage(text: String) {
        if (text.isEmpty() || isStreaming) return
        if (clusterSelectedModels.size < 2) {
            Toast.makeText(requireContext(), "请至少选择2个模型进行集群对比", Toast.LENGTH_SHORT).show()
            return
        }

        val models = clusterSelectedModels.mapNotNull { VendorConfig.getModelById(it) }

        // Add user message
        val userMsg = ChatMessage(
            role = "user",
            content = text,
            modelId = "cluster",
            modelName = "集群模式",
            vendorName = "Cluster"
        )
        messages.add(userMsg)
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
        saveMessageToDb(userMsg)

        val title = if (text.length > 30) text.take(30) + "..." else text
        updateConversationTitle(title)
        saveConversationToDb(title)

        isStreaming = true

        models.forEach { model ->
            val startTime = System.currentTimeMillis()
            val streamMsg = ChatMessage(
                role = "assistant",
                content = "",
                modelId = model.id,
                modelName = model.name,
                vendorName = model.vendorName
            )
            messages.add(streamMsg)
            val msgIndex = messages.size - 1
            adapter.notifyItemInserted(msgIndex)

            var accumulatedText = ""

            apiService.sendMessage(
                model = model,
                prompt = text,
                temperature = temperature,
                topP = topP,
                maxTokens = maxTokens,
                onChunk = { chunk ->
                    accumulatedText += chunk
                    messages[msgIndex] = streamMsg.copy(content = accumulatedText)
                    handler.post {
                        adapter.notifyItemChanged(msgIndex)
                        scrollToBottom()
                    }
                },
                onComplete = { response ->
                    val elapsed = System.currentTimeMillis() - startTime
                    val finalContent = if (accumulatedText.isNotEmpty()) accumulatedText else response
                    val tokenCount = estimateTokens(finalContent)
                    val finalMsg = streamMsg.copy(
                        content = finalContent,
                        responseTimeMs = elapsed,
                        tokenCount = tokenCount
                    )
                    messages[msgIndex] = finalMsg
                    saveMessageToDb(finalMsg)
                    handler.post {
                        adapter.notifyItemChanged(msgIndex)
                        scrollToBottom()
                        updateTotalTokenCount()
                    }
                },
                onError = { error ->
                    val elapsed = System.currentTimeMillis() - startTime
                    val friendlyError = parseError(error)
                    val errorMsg = streamMsg.copy(
                        content = "【${model.vendorName} · ${model.name}】\n$friendlyError",
                        responseTimeMs = elapsed
                    )
                    messages[msgIndex] = errorMsg
                    saveMessageToDb(errorMsg)
                    handler.post {
                        adapter.notifyItemChanged(msgIndex)
                    }
                }
            )
        }

        // Set isStreaming to false after a delay (cluster mode sends multiple requests)
        handler.postDelayed({
            isStreaming = false
        }, 500)
    }

    private fun regenerate() {
        if (isStreaming) return
        if (messages.isEmpty()) return

        val lastUserIdx = messages.indexOfLast { it.role == "user" }
        if (lastUserIdx < 0) return

        val lastUserMsg = messages[lastUserIdx]

        while (messages.size > lastUserIdx) {
            messages.removeAt(messages.size - 1)
        }
        adapter.notifyDataSetChanged()

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
                    lp.gravity = Gravity.START
                    modelLabel.visibility = View.VISIBLE

                    val infoParts = mutableListOf<String>()
                    infoParts.add("${msg.vendorName} · ${msg.modelName}")
                    if (msg.responseTimeMs > 0) {
                        infoParts.add("${msg.responseTimeMs}ms")
                    }
                    modelInfo.text = infoParts.joinToString("  ")

                    modelAvatar.text = msg.vendorName.take(1)

                    if (msg.tokenCount > 0 && !isStreaming) {
                        tokenInfo.visibility = View.VISIBLE
                        tokenInfo.text = "≈${msg.tokenCount} tokens"
                    } else {
                        tokenInfo.visibility = View.GONE
                    }

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
        AlertDialog.Builder(ctx)
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