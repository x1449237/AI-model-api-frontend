package com.aiaggregator.app.ui.cluster

import android.app.AlertDialog
import android.content.DialogInterface
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
import com.aiaggregator.app.data.AppDatabase
import com.aiaggregator.app.data.ClusterRecordEntity
import com.aiaggregator.app.data.VendorConfig
import com.aiaggregator.app.models.AiModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClusterFragment : Fragment() {

    private lateinit var apiService: ApiService
    private val selectedModels = mutableSetOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private val gson = Gson()
    private val db by lazy { AppDatabase.getInstance(requireContext()) }
    private val clusterRecordDao by lazy { db.clusterRecordDao() }

    private var lastPrompt: String = ""
    private var lastResults: List<ClusterResult>? = null

    private data class ClusterResult(
        val model: AiModel,
        val content: String,
        val responseTimeMs: Long,
        val isError: Boolean
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_cluster, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = ApiService(requireContext())

        val recycler = view.findViewById<RecyclerView>(R.id.cluster_model_list)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = ClusterModelAdapter { model, checked ->
            if (checked) {
                if (selectedModels.size < 5) {
                    selectedModels.add(model.id)
                } else {
                    Toast.makeText(requireContext(), "最多选择5个模型", Toast.LENGTH_SHORT).show()
                }
            } else {
                selectedModels.remove(model.id)
            }
            updateStatus(view)
        }

        view.findViewById<MaterialButton>(R.id.btn_run_cluster).setOnClickListener { runCluster(view) }
        view.findViewById<MaterialButton>(R.id.btn_cluster_history).setOnClickListener { showClusterHistory() }
    }

    private fun updateStatus(view: View) {
        val count = selectedModels.size
        val status = view.findViewById<TextView>(R.id.status_text)
        val btn = view.findViewById<MaterialButton>(R.id.btn_run_cluster)

        if (count >= 2) {
            status.text = "已选择 $count 个模型，可以开始集群对比"
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            btn.isEnabled = true
        } else {
            status.text = "请至少选择 2 个模型（已选 $count）"
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary))
            btn.isEnabled = false
        }
    }

    private fun runCluster(view: View) {
        val promptInput = view.findViewById<TextInputEditText>(R.id.cluster_prompt)
        val prompt = promptInput.text.toString().trim()
        if (prompt.isEmpty()) {
            Toast.makeText(requireContext(), "请输入问题", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedModels.size < 2 || isRunning) return

        lastPrompt = prompt
        val models = selectedModels.mapNotNull { VendorConfig.getModelById(it) }
        isRunning = true

        // Hide selection area, show progress
        view.findViewById<View>(R.id.cluster_select_area).visibility = View.GONE
        view.findViewById<View>(R.id.cluster_status).visibility = View.GONE
        view.findViewById<View>(R.id.cluster_prompt_layout).visibility = View.GONE

        val progressBar = view.findViewById<ProgressBar>(R.id.cluster_progress)
        val progressText = view.findViewById<TextView>(R.id.cluster_progress_text)
        progressBar.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        progressText.text = "正在向 ${models.size} 个模型发送请求..."

        val resultsContainer = view.findViewById<LinearLayout>(R.id.cluster_results_container)
        resultsContainer.removeAllViews()

        val results = mutableListOf<ClusterResult>()
        var completed = 0

        models.forEach { model ->
            val startTime = System.currentTimeMillis()
            apiService.sendMessage(
                model = model,
                prompt = prompt,
                temperature = 0.7,
                maxTokens = 2048,
                onComplete = { response ->
                    val elapsed = System.currentTimeMillis() - startTime
                    synchronized(results) {
                        results.add(ClusterResult(model, response, elapsed, false))
                        completed++
                        handler.post {
                            progressText.text = "已完成 $completed / ${models.size} 个模型"
                        }
                        if (completed == models.size) {
                            handler.post {
                                showResults(view, results.sortedBy { it.responseTimeMs })
                                saveClusterRecord()
                            }
                        }
                    }
                },
                onError = { error ->
                    val elapsed = System.currentTimeMillis() - startTime
                    synchronized(results) {
                        results.add(ClusterResult(model, "请求失败: $error", elapsed, true))
                        completed++
                        handler.post {
                            progressText.text = "已完成 $completed / ${models.size} 个模型"
                        }
                        if (completed == models.size) {
                            handler.post {
                                showResults(view, results.sortedBy { it.responseTimeMs })
                                saveClusterRecord()
                            }
                        }
                    }
                }
            )
        }
    }

    private fun saveClusterRecord() {
        if (lastPrompt.isEmpty()) return
        val results = lastResults ?: return

        val modelIds = gson.toJson(selectedModels.toList())
        val resultsList = results.map { result ->
            mapOf(
                "modelName" to result.model.name,
                "vendorName" to result.model.vendorName,
                "content" to result.content,
                "responseTimeMs" to result.responseTimeMs
            )
        }
        val resultsJson = gson.toJson(resultsList)

        Thread {
            runBlocking {
                clusterRecordDao.insertRecord(
                    ClusterRecordEntity(
                        prompt = lastPrompt,
                        modelIds = modelIds,
                        resultsJson = resultsJson
                    )
                )
            }
        }.start()
    }

    private fun showClusterHistory() {
        val db = AppDatabase.getInstance(requireContext())
        Thread {
            val cursor = db.openHelper.readableDatabase.query(
                "SELECT id, prompt, model_ids, results_json, created_at FROM cluster_records ORDER BY created_at DESC"
            )
            val records = mutableListOf<ClusterRecordEntity>()
            while (cursor.moveToNext()) {
                records.add(
                    ClusterRecordEntity(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        prompt = cursor.getString(cursor.getColumnIndexOrThrow("prompt")),
                        modelIds = cursor.getString(cursor.getColumnIndexOrThrow("model_ids")),
                        resultsJson = cursor.getString(cursor.getColumnIndexOrThrow("results_json")),
                        createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
            cursor.close()

            handler.post {
                showClusterHistoryDialog(records)
            }
        }.start()
    }

    private fun showClusterHistoryDialog(records: List<ClusterRecordEntity>) {
        if (records.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("历史记录")
                .setMessage("暂无集群对比记录")
                .setPositiveButton("确定", null)
                .show()
            return
        }

        val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        val items = records.map { record ->
            val preview = if (record.prompt.length > 30) record.prompt.take(30) + "..." else record.prompt
            val date = dateFormat.format(Date(record.createdAt))
            val modelCount = try {
                val list = gson.fromJson<List<String>>(record.modelIds, object : TypeToken<List<String>>() {}.type)
                list.size
            } catch (e: Exception) {
                0
            }
            "$preview\n$date · ${modelCount}个模型"
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("集群对比历史")
            .setItems(items) { _, which ->
                showRecordDetail(records[which])
            }
            .setOnCancelListener { }
            .show()
    }

    private fun showRecordDetail(record: ClusterRecordEntity) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.format(Date(record.createdAt))

        val modelCount = try {
            val list = gson.fromJson<List<String>>(record.modelIds, object : TypeToken<List<String>>() {}.type)
            list.size
        } catch (e: Exception) {
            0
        }

        val resultsList = try {
            gson.fromJson<List<Map<String, Any>>>(
                record.resultsJson,
                object : TypeToken<List<Map<String, Any>>>() {}.type
            )
        } catch (e: Exception) {
            emptyList()
        }

        val sb = StringBuilder()
        sb.append("问题：${record.prompt}\n\n")
        sb.append("时间：$date\n")
        sb.append("模型数：$modelCount\n\n")
        sb.append("━━━ 各模型响应 ━━━\n\n")

        resultsList.forEachIndexed { index, result ->
            val modelName = result["modelName"] as? String ?: "未知"
            val vendorName = result["vendorName"] as? String ?: "未知"
            val content = result["content"] as? String ?: ""
            val responseTime = (result["responseTimeMs"] as? Double)?.toLong() ?: 0L

            sb.append("【${index + 1}】$vendorName · $modelName")
            if (responseTime > 0) {
                sb.append(" (${responseTime}ms)")
            }
            sb.append("\n")
            sb.append(content)
            sb.append("\n\n")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("记录详情")
            .setMessage(sb.toString())
            .setPositiveButton("关闭", null)
            .setNegativeButton("删除") { _, _ ->
                deleteClusterRecord(record)
            }
            .show()
    }

    private fun deleteClusterRecord(record: ClusterRecordEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("确认删除")
            .setMessage("确定要删除这条集群对比记录吗？")
            .setPositiveButton("删除") { _, _ ->
                Thread {
                    runBlocking {
                        clusterRecordDao.deleteRecord(record)
                    }
                    handler.post {
                        Toast.makeText(requireContext(), "已删除", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showResults(view: View, results: List<ClusterResult>) {
        lastResults = results

        view.findViewById<ProgressBar>(R.id.cluster_progress).visibility = View.GONE
        view.findViewById<TextView>(R.id.cluster_progress_text).visibility = View.GONE

        val resultsArea = view.findViewById<ScrollView>(R.id.cluster_results_area)
        resultsArea.visibility = View.VISIBLE

        val container = view.findViewById<LinearLayout>(R.id.cluster_results_container)
        container.removeAllViews()

        // Add a summary header
        val avgTime = if (results.isNotEmpty()) results.map { it.responseTimeMs }.average().toLong() else 0
        val errorCount = results.count { it.isError }
        val summaryCard = MaterialCardView(requireContext()).apply {
            radius = 16f * resources.displayMetrics.density
            cardElevation = 0f
            strokeWidth = 1
            strokeColor = ContextCompat.getColor(requireContext(), R.color.colorDivider)
            setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorSurface))
            setContentPadding(20, 16, 20, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        val summaryLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }
        summaryLayout.addView(TextView(requireContext()).apply {
            text = "集群对比结果"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
            setPadding(0, 0, 0, 8)
        })
        summaryLayout.addView(TextView(requireContext()).apply {
            text = "共 ${results.size} 个模型响应，平均耗时 ${avgTime}ms" +
                    if (errorCount > 0) "（${errorCount} 个失败）" else ""
            textSize = 13f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary))
        })
        summaryCard.addView(summaryLayout)
        container.addView(summaryCard)

        // Render each result card
        results.forEachIndexed { index, result ->
            val card = MaterialCardView(requireContext()).apply {
                radius = 16f * resources.displayMetrics.density
                cardElevation = 0f
                strokeWidth = 1
                strokeColor = if (result.isError)
                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                else
                    ContextCompat.getColor(requireContext(), R.color.colorDivider)
                setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorSurface))
                setContentPadding(20, 16, 20, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
            }

            val contentLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
            }

            // Header row: rank badge + model info + response time
            val headerRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, 12)
            }

            // Rank badge
            val rankBadge = TextView(requireContext()).apply {
                text = "${index + 1}"
                textSize = 13f
                setTextColor(android.graphics.Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                setBackgroundResource(when (index) {
                    0 -> R.drawable.bg_rank_gold
                    1 -> R.drawable.bg_rank_silver
                    2 -> R.drawable.bg_rank_bronze
                    else -> R.drawable.bg_rank_default
                })
                layoutParams = LinearLayout.LayoutParams(
                    (28 * resources.displayMetrics.density).toInt(),
                    (28 * resources.displayMetrics.density).toInt()
                ).apply { rightMargin = (12 * resources.displayMetrics.density).toInt() }
            }
            headerRow.addView(rankBadge)

            // Model info
            val modelInfoLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            modelInfoLayout.addView(TextView(requireContext()).apply {
                text = "${result.model.vendorName} · ${result.model.name}"
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
            })
            modelInfoLayout.addView(TextView(requireContext()).apply {
                val tags = mutableListOf<String>()
                if (result.model.contextLength > 0) tags.add("${result.model.contextLength / 1000}K 上下文")
                if (result.model.isLatest) tags.add("最新")
                if (result.model.isBestPerformance) tags.add("最佳")
                text = tags.joinToString(" · ")
                textSize = 12f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextHint))
                setPadding(0, 2, 0, 0)
            })
            headerRow.addView(modelInfoLayout)

            // Response time badge
            val timeBadge = TextView(requireContext()).apply {
                text = if (result.responseTimeMs >= 1000)
                    String.format("%.1fs", result.responseTimeMs / 1000.0)
                else
                    "${result.responseTimeMs}ms"
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(
                    (12 * resources.displayMetrics.density).toInt(),
                    (4 * resources.displayMetrics.density).toInt(),
                    (12 * resources.displayMetrics.density).toInt(),
                    (4 * resources.displayMetrics.density).toInt()
                )
                setBackgroundColor(when {
                    result.responseTimeMs < 2000 -> android.graphics.Color.parseColor("#2E7D32")
                    result.responseTimeMs < 5000 -> android.graphics.Color.parseColor("#E65100")
                    else -> android.graphics.Color.parseColor("#C62828")
                })
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { leftMargin = 8 }
            }
            headerRow.addView(timeBadge)

            contentLayout.addView(headerRow)

            // Divider
            val divider = View(requireContext()).apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorDivider))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                ).apply { bottomMargin = 12 }
            }
            contentLayout.addView(divider)

            // Content
            val contentText = TextView(requireContext()).apply {
                text = result.content
                textSize = 14f
                setTextColor(
                    if (result.isError)
                        ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                    else
                        ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
                )
                setLineSpacing(6f, 1f)
            }
            contentLayout.addView(contentText)

            card.addView(contentLayout)
            container.addView(card)
        }

        // Add a "重新对比" button at the bottom
        val resetButton = MaterialButton(requireContext()).apply {
            text = "重新对比"
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            cornerRadius = (24 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (48 * resources.displayMetrics.density).toInt()
            ).apply {
                topMargin = (16 * resources.displayMetrics.density).toInt()
                bottomMargin = (16 * resources.displayMetrics.density).toInt()
            }
            setOnClickListener { resetCluster(view) }
        }
        container.addView(resetButton)

        isRunning = false
    }

    private fun resetCluster(view: View) {
        // Restore UI
        view.findViewById<View>(R.id.cluster_select_area).visibility = View.VISIBLE
        view.findViewById<View>(R.id.cluster_status).visibility = View.VISIBLE
        view.findViewById<View>(R.id.cluster_prompt_layout).visibility = View.VISIBLE
        view.findViewById<ScrollView>(R.id.cluster_results_area).visibility = View.GONE

        // Refresh adapter to sync checkbox states
        val recycler = view.findViewById<RecyclerView>(R.id.cluster_model_list)
        recycler.adapter?.notifyDataSetChanged()
        updateStatus(view)
    }

    // ==================== Adapter ====================

    inner class ClusterModelAdapter(
        private val onCheckChanged: (AiModel, Boolean) -> Unit
    ) : RecyclerView.Adapter<ClusterModelAdapter.ViewHolder>() {

        private val allModels = VendorConfig.getAllModels().filter { it.supportsCode && !it.supportsImage }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cluster_model, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(allModels[position])
        }

        override fun getItemCount() = allModels.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val checkbox: CheckBox = itemView.findViewById(R.id.cb_model)
            private val nameText: TextView = itemView.findViewById(R.id.tv_model_name)
            private val vendorText: TextView = itemView.findViewById(R.id.tv_vendor_name)
            private val tagsText: TextView = itemView.findViewById(R.id.tv_tags)

            fun bind(model: AiModel) {
                nameText.text = model.name
                vendorText.text = model.vendorName

                val tags = mutableListOf<String>()
                if (model.contextLength > 0) tags.add("${model.contextLength / 1000}K")
                if (model.isLatest) tags.add("最新")
                if (model.isBestPerformance) tags.add("最佳")
                tagsText.text = tags.joinToString(" · ")

                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = selectedModels.contains(model.id)
                checkbox.setOnCheckedChangeListener { _, checked ->
                    onCheckChanged(model, checked)
                }
            }
        }
    }
}