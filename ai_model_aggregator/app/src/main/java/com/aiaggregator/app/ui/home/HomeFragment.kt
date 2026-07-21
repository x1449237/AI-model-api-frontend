package com.aiaggregator.app.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiaggregator.app.R
import com.aiaggregator.app.data.ApiService
import com.aiaggregator.app.data.VendorConfig
import com.aiaggregator.app.models.AiModel
import com.aiaggregator.app.models.ChatMessage
import com.google.android.material.chip.Chip
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = ApiService(requireContext())

        val recycler = view.findViewById<RecyclerView>(R.id.chat_recycler)
        adapter = ChatAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        setupModelChips(view)
        setupParams(view)
        setupInput(view)
    }

    private fun setupModelChips(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.model_chips_container)
        VendorConfig.vendors.forEach { vendor ->
            val models = VendorConfig.modelsByVendor[vendor.id] ?: return@forEach
            val textModels = models.filter { it.supportsCode && !it.supportsImage }
            if (textModels.isEmpty()) return@forEach

            val chip = Chip(requireContext()).apply {
                text = vendor.name
                isCheckable = false
                setChipBackgroundColorResource(android.R.color.transparent)
                setTextColor(resources.getColor(R.color.colorTextSecondary, null))
                setOnClickListener {
                    showModelPicker(vendor, textModels)
                }
            }
            container.addView(chip)
        }
    }

    private fun showModelPicker(vendor: com.aiaggregator.app.models.Vendor, models: List<AiModel>) {
        val names = models.map { it.name }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("选择 ${vendor.name} 模型")
            .setItems(names) { _, which ->
                selectedModel = models[which]
                val input = view?.findViewById<EditText>(R.id.input_message)
                input?.hint = "向 ${selectedModel?.name} 发送消息..."
            }
            .show()
    }

    private fun setupParams(view: View) {
        val paramsPanel = view.findViewById<LinearLayout>(R.id.params_panel)
        view.findViewById<ImageButton>(R.id.btn_params).setOnClickListener {
            paramsPanel.visibility = if (paramsPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        view.findViewById<SeekBar>(R.id.temperature_slider).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    temperature = p / 100.0
                    view.findViewById<TextView>(R.id.temperature_value).text = String.format("%.2f", temperature)
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })

        view.findViewById<SeekBar>(R.id.topp_slider).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    topP = p / 100.0
                    view.findViewById<TextView>(R.id.topp_value).text = String.format("%.2f", topP)
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })

        view.findViewById<SeekBar>(R.id.max_tokens_slider).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    maxTokens = p
                    view.findViewById<TextView>(R.id.max_tokens_value).text = p.toString()
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
    }

    private fun setupInput(view: View) {
        val input = view.findViewById<EditText>(R.id.input_message)
        view.findViewById<ImageButton>(R.id.btn_send).setOnClickListener {
            sendMessage(input.text.toString().trim())
        }
    }

    private fun sendMessage(text: String) {
        if (text.isEmpty() || selectedModel == null || isStreaming) return

        val input = view?.findViewById<EditText>(R.id.input_message)
        input?.setText("")

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
        val streamIdx = messages.size - 1
        adapter.notifyItemInserted(streamIdx)

        val startTime = System.currentTimeMillis()

        apiService.sendMessage(
            model = selectedModel!!,
            prompt = text,
            temperature = temperature,
            topP = topP,
            maxTokens = maxTokens,
            onChunk = { chunk ->
                streamingText += chunk
                messages[streamIdx] = streamMsg.copy(content = streamingText)
                handler.post {
                    adapter.notifyItemChanged(streamIdx)
                    scrollToBottom()
                }
            },
            onComplete = { response ->
                val elapsed = System.currentTimeMillis() - startTime
                messages[streamIdx] = streamMsg.copy(
                    content = if (streamingText.isNotEmpty()) streamingText else response,
                    responseTimeMs = elapsed
                )
                isStreaming = false
                handler.post {
                    adapter.notifyItemChanged(streamIdx)
                    scrollToBottom()
                }
            },
            onError = { error ->
                messages[streamIdx] = streamMsg.copy(
                    content = "请求失败: $error\n\n请在\"我的\"页面配置API Key后重试。",
                    responseTimeMs = System.currentTimeMillis() - startTime
                )
                isStreaming = false
                handler.post {
                    adapter.notifyItemChanged(streamIdx)
                }
            }
        )
    }

    private fun scrollToBottom() {
        view?.findViewById<RecyclerView>(R.id.chat_recycler)?.let {
            it.post { it.smoothScrollToPosition(messages.size - 1) }
        }
    }

    inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(messages[position])
        }

        override fun getItemCount() = messages.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val modelLabel = itemView.findViewById<LinearLayout>(R.id.model_label)
            private val modelInfo = itemView.findViewById<TextView>(R.id.model_info)
            private val bubbleContainer = itemView.findViewById<LinearLayout>(R.id.bubble_container)
            private val bubbleCard = itemView.findViewById<CardView>(R.id.bubble_card)
            private val bubbleText = itemView.findViewById<TextView>(R.id.bubble_text)

            fun bind(msg: ChatMessage) {
                val isUser = msg.role == "user"
                val params = bubbleContainer.layoutParams as LinearLayout.LayoutParams

                if (isUser) {
                    params.gravity = android.view.Gravity.END
                    modelLabel.visibility = View.GONE
                    bubbleCard.setCardBackgroundColor(resources.getColor(R.color.colorUserBubble, null))
                    bubbleText.setTextColor(resources.getColor(android.R.color.white, null))
                    params.setMargins(0, 0, 0, 0)
                } else {
                    params.gravity = android.view.Gravity.START
                    modelLabel.visibility = View.VISIBLE
                    modelInfo.text = "${msg.vendorName} - ${msg.modelName}" +
                            if (msg.responseTimeMs > 0) "  ${msg.responseTimeMs}ms" else ""
                    bubbleCard.setCardBackgroundColor(resources.getColor(R.color.colorAiBubble, null))
                    bubbleText.setTextColor(resources.getColor(R.color.colorOnSurface, null))
                    params.setMargins(0, 0, 0, 0)
                }
                bubbleText.text = msg.content

                if (isStreaming && !isUser && msg == messages.last()) {
                    bubbleText.append(" ▌")
                }
            }
        }
    }
}