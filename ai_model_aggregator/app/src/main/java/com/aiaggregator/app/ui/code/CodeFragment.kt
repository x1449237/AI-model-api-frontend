package com.aiaggregator.app.ui.code

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.aiaggregator.app.R
import com.aiaggregator.app.data.ApiService
import com.aiaggregator.app.data.VendorConfig
import com.aiaggregator.app.models.AiModel
import com.google.android.material.chip.Chip
import java.io.File

class CodeFragment : Fragment() {
    private lateinit var apiService: ApiService
    private var selectedModel: AiModel? = null
    private var selectedLanguage = "Python"
    private var generatedCode = ""
    private val handler = Handler(Looper.getMainLooper())

    private val languages = listOf("Python", "Java", "JavaScript", "C++", "C", "Go", "Rust", "Swift", "Kotlin", "PHP", "SQL", "HTML", "CSS", "Shell", "JSON", "XML", "YAML")
    private val extensions = mapOf("Python" to ".py", "Java" to ".java", "JavaScript" to ".js", "C++" to ".cpp", "C" to ".c", "Go" to ".go", "Rust" to ".rs", "Swift" to ".swift", "Kotlin" to ".kt", "PHP" to ".php", "SQL" to ".sql", "HTML" to ".html", "CSS" to ".css", "Shell" to ".sh", "JSON" to ".json", "XML" to ".xml", "YAML" to ".yaml")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = ApiService(requireContext())

        setupLanguageChips(view)
        view.findViewById<Button>(R.id.btn_select_model).setOnClickListener { showModelPicker() }
        view.findViewById<Button>(R.id.btn_generate).setOnClickListener { generateCode(view) }
        view.findViewById<Button>(R.id.btn_copy).setOnClickListener { copyCode() }
        view.findViewById<Button>(R.id.btn_export).setOnClickListener { exportCode() }
    }

    private fun setupLanguageChips(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.lang_chips)
        languages.forEach { lang ->
            val chip = Chip(requireContext()).apply {
                text = lang
                isCheckable = true
                isChecked = lang == selectedLanguage
                setOnClickListener {
                    selectedLanguage = lang
                    // Update all chips
                    for (i in 0 until container.childCount) {
                        (container.getChildAt(i) as? Chip)?.isChecked = container.getChildAt(i) == this
                    }
                }
            }
            container.addView(chip)
        }
    }

    private fun showModelPicker() {
        val models = VendorConfig.getCodeModels()
        val names = models.map { "${it.vendorName} - ${it.name}" }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("选择代码模型")
            .setItems(names) { _, which ->
                selectedModel = models[which]
                view?.findViewById<TextView>(R.id.selected_model_text)?.text = "${selectedModel?.vendorName} - ${selectedModel?.name}"
            }
            .show()
    }

    private fun generateCode(view: View) {
        val desc = view.findViewById<EditText>(R.id.code_desc_input).text.toString().trim()
        if (desc.isEmpty() || selectedModel == null) {
            Toast.makeText(requireContext(), "请选择模型并输入需求描述", Toast.LENGTH_SHORT).show()
            return
        }

        view.findViewById<View>(R.id.empty_state).visibility = View.GONE
        view.findViewById<View>(R.id.code_output_scroll).visibility = View.VISIBLE
        view.findViewById<View>(R.id.result_header).visibility = View.VISIBLE
        val output = view.findViewById<TextView>(R.id.code_output)
        output.text = "正在生成..."
        generatedCode = ""

        val prompt = "你是一个专业的${selectedLanguage}程序员。请根据以下需求生成代码：\n\n需求：$desc\n\n要求：\n1. 只输出代码，不要包含额外的解释文字\n2. 代码要完整可运行\n3. 添加必要的注释\n4. 遵循${selectedLanguage}的最佳实践\n\n请生成代码："

        apiService.sendMessage(
            model = selectedModel!!,
            prompt = prompt,
            temperature = 0.3,
            maxTokens = 4096,
            onChunk = { chunk ->
                generatedCode += chunk
                handler.post { output.text = generatedCode }
            },
            onComplete = { response ->
                if (generatedCode.isEmpty()) generatedCode = response
                handler.post { output.text = generatedCode }
            },
            onError = { error ->
                handler.post { output.text = "生成失败: $error" }
            }
        )
    }

    private fun copyCode() {
        if (generatedCode.isEmpty()) return
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("code", generatedCode))
        Toast.makeText(requireContext(), "代码已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    private fun exportCode() {
        if (generatedCode.isEmpty()) return
        try {
            val ext = extensions[selectedLanguage] ?: ".txt"
            val file = File(requireContext().cacheDir, "generated_code$ext")
            file.writeText(generatedCode)

            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "导出代码"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}