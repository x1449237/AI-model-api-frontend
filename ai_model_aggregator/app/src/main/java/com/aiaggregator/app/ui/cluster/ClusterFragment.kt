package com.aiaggregator.app.ui.cluster

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.card.MaterialCardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiaggregator.app.R
import com.aiaggregator.app.data.ApiService
import com.aiaggregator.app.data.VendorConfig
import com.aiaggregator.app.models.AiModel

class ClusterFragment : Fragment() {
    private lateinit var apiService: ApiService
    private val selectedModels = mutableSetOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_cluster, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = ApiService(requireContext())

        val recycler = view.findViewById<RecyclerView>(R.id.cluster_model_list)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = ClusterModelAdapter { model, checked ->
            if (checked) {
                if (selectedModels.size < 5) selectedModels.add(model.id)
                else Toast.makeText(requireContext(), "最多选择5个模型", Toast.LENGTH_SHORT).show()
            } else {
                selectedModels.remove(model.id)
            }
            updateStatus(view)
        }

        view.findViewById<Button>(R.id.btn_run_cluster).setOnClickListener { runCluster(view) }
    }

    private data class ClusterResult(val model: AiModel, val content: String, val responseTimeMs: Long, val isError: Boolean)

    private fun updateStatus(view: View) {
        val count = selectedModels.size
        val status = view.findViewById<TextView>(R.id.status_text)
        val btn = view.findViewById<Button>(R.id.btn_run_cluster)
        if (count >= 2) {
            status.text = "已选择 $count 个模型，可以开始集群对比"
            btn.isEnabled = true
        } else {
            status.text = "请至少选择 2 个模型（已选 $count）"
            btn.isEnabled = false
        }
    }

    private fun runCluster(view: View) {
        val prompt = view.findViewById<EditText>(R.id.cluster_prompt).text.toString().trim()
        if (prompt.isEmpty() || selectedModels.size < 2 || isRunning) return

        val models = selectedModels.mapNotNull { VendorConfig.getModelById(it) }
        isRunning = true

        view.findViewById<View>(R.id.cluster_select_area).visibility = View.GONE
        view.findViewById<ProgressBar>(R.id.cluster_progress).visibility = View.VISIBLE

        val resultsContainer = view.findViewById<LinearLayout>(R.id.cluster_results_container)
        resultsContainer.removeAllViews()

        val results = mutableListOf<ClusterResult>()
        var completed = 0

        models.forEachIndexed { index, model ->
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
                        if (completed == models.size) {
                            handler.post {
                                showResults(view, results.sortedBy { it.responseTimeMs })
                            }
                        }
                    }
                },
                onError = { error ->
                    val elapsed = System.currentTimeMillis() - startTime
                    synchronized(results) {
                        results.add(ClusterResult(model, "请求失败: $error", elapsed, true))
                        completed++
                        if (completed == models.size) {
                            handler.post {
                                showResults(view, results.sortedBy { it.responseTimeMs })
                            }
                        }
                    }
                }
            )
        }
    }

    private fun showResults(view: View, results: List<ClusterResult>) {
        view.findViewById<ProgressBar>(R.id.cluster_progress).visibility = View.GONE
        view.findViewById<ScrollView>(R.id.cluster_results_area).visibility = View.VISIBLE

        val container = view.findViewById<LinearLayout>(R.id.cluster_results_container)
        container.removeAllViews()

        results.forEachIndexed { index, result ->
            val card = MaterialCardView(requireContext()).apply {
                radius = 12f * resources.displayMetrics.density
                cardElevation = 2f * resources.displayMetrics.density
                setContentPadding(16, 12, 16, 12)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
            }

            val header = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
            val rankView = TextView(requireContext()).apply {
                text = "${index + 1}"
                textSize = 12f
                setTextColor(android.graphics.Color.WHITE)
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(when (index) {
                    0 -> R.drawable.bg_rank_gold
                    1 -> R.drawable.bg_rank_silver
                    2 -> R.drawable.bg_rank_bronze
                    else -> R.drawable.bg_rank_default
                })
                layoutParams = LinearLayout.LayoutParams(24, 24).apply { rightMargin = 8 }
            }
            header.addView(rankView)

            val info = TextView(requireContext()).apply {
                text = "${result.model.vendorName} - ${result.model.name}"
                textSize = 13f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            header.addView(info)

            val timeView = TextView(requireContext()).apply {
                text = "${result.responseTimeMs}ms"
                textSize = 11f
                setTextColor(if (result.responseTimeMs < 2000) android.graphics.Color.parseColor("#2E7D32")
                    else if (result.responseTimeMs < 5000) android.graphics.Color.parseColor("#E65100")
                    else android.graphics.Color.RED)
            }
            header.addView(timeView)

            card.addView(header)

            val contentView = TextView(requireContext()).apply {
                text = result.content
                textSize = 14f
                setTextColor(if (result.isError) android.graphics.Color.RED else android.graphics.Color.parseColor("#333333"))
                setPadding(0, 8, 0, 0)
                setLineSpacing(4f, 1f)
            }
            card.addView(contentView)

            container.addView(card)
        }

        isRunning = false
    }

    inner class ClusterModelAdapter(
        private val onCheckChanged: (AiModel, Boolean) -> Unit
    ) : RecyclerView.Adapter<ClusterModelAdapter.ViewHolder>() {

        private val allModels = VendorConfig.getAllModels()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cluster_model, parent, false)
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
                tagsText.text = tags.joinToString(" | ")
                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = selectedModels.contains(model.id)
                checkbox.setOnCheckedChangeListener { _, checked -> onCheckChanged(model, checked) }
            }
        }
    }
}