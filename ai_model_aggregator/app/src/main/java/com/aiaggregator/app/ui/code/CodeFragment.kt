package com.aiaggregator.app.ui.code

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.aiaggregator.app.R
import com.aiaggregator.app.data.ApiService
import com.aiaggregator.app.data.VendorConfig
import com.aiaggregator.app.models.AiModel
import com.aiaggregator.app.models.Vendor
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class CodeFragment : Fragment() {

    private lateinit var apiService: ApiService
    private var selectedModel: AiModel? = null
    private var selectedLanguage = "Python"
    private var generatedCode = ""
    private var isGenerating = false
    private val handler = Handler(Looper.getMainLooper())

    private val languages = listOf(
        "Python", "Java", "JavaScript", "C++", "C", "Go", "Rust",
        "Swift", "Kotlin", "PHP", "SQL", "HTML", "CSS", "Shell",
        "JSON", "XML", "YAML"
    )
    private val languageExtensions = mapOf(
        "C++" to "cpp", "C" to "c", "Java" to "java",
        "Python" to "py", "JavaScript" to "js", "PHP" to "php",
        "SQL" to "sql", "JSON" to "json", "HTML" to "html",
        "CSS" to "css", "Go" to "go", "Rust" to "rs",
        "Swift" to "swift", "Kotlin" to "kt", "Shell" to "sh",
        "XML" to "xml", "YAML" to "yaml"
    )

    // ── View references ──
    private lateinit var langChipsContainer: LinearLayout
    private lateinit var btnSelectModel: MaterialButton
    private lateinit var selectedModelText: TextView
    private lateinit var codeDescInput: TextInputEditText
    private lateinit var btnGenerate: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var resultHeader: LinearLayout
    private lateinit var btnCopy: MaterialButton
    private lateinit var btnExport: MaterialButton
    private lateinit var btnSaveLocal: MaterialButton
    private lateinit var codeOutputScroll: ScrollView
    private lateinit var codeOutput: TextView
    private lateinit var emptyState: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = ApiService(requireContext())

        bindViews(view)
        setupLanguageChips()
        setupModelSelector()
        setupButtons()
    }

    private fun bindViews(view: View) {
        langChipsContainer = view.findViewById(R.id.lang_chips)
        btnSelectModel = view.findViewById(R.id.btn_select_model)
        selectedModelText = view.findViewById(R.id.selected_model_text)
        codeDescInput = view.findViewById(R.id.code_desc_input)
        btnGenerate = view.findViewById(R.id.btn_generate)
        progressBar = view.findViewById(R.id.progress_bar)
        resultHeader = view.findViewById(R.id.result_header)
        btnCopy = view.findViewById(R.id.btn_copy)
        btnExport = view.findViewById(R.id.btn_export)
        btnSaveLocal = view.findViewById(R.id.btn_save_local)
        codeOutputScroll = view.findViewById(R.id.code_output_scroll)
        codeOutput = view.findViewById(R.id.code_output)
        emptyState = view.findViewById(R.id.empty_state)
    }

    // ═══════════════════════════════════════════
    //  Language Chips
    // ═══════════════════════════════════════════

    private fun setupLanguageChips() {
        val ctx = requireContext()
        languages.forEach { lang ->
            val chip = Chip(ctx).apply {
                text = lang
                isCheckable = true
                isChecked = lang == selectedLanguage
                chipBackgroundColor = ContextCompat.getColorStateList(ctx, R.color.colorChipBg)
                setTextColor(
                    ContextCompat.getColorStateList(
                        ctx,
                        if (lang == selectedLanguage) R.color.colorChipSelectedText
                        else R.color.colorChipUnselectedText
                    )
                )
                setOnClickListener {
                    selectedLanguage = lang
                    refreshChipStates()
                }
            }
            langChipsContainer.addView(chip)
        }
    }

    private fun refreshChipStates() {
        val ctx = requireContext()
        for (i in 0 until langChipsContainer.childCount) {
            val chip = langChipsContainer.getChildAt(i) as? Chip ?: continue
            val isSelected = chip.text == selectedLanguage
            chip.isChecked = isSelected
            chip.chipBackgroundColor = ContextCompat.getColorStateList(ctx, R.color.colorChipBg)
            chip.setTextColor(
                ContextCompat.getColorStateList(
                    ctx,
                    if (isSelected) R.color.colorChipSelectedText else R.color.colorChipUnselectedText
                )
            )
        }
    }

    // ═══════════════════════════════════════════
    //  Model Selector (vendor → model, like Home)
    // ═══════════════════════════════════════════

    private fun setupModelSelector() {
        btnSelectModel.setOnClickListener { showVendorPicker() }
    }

    private fun showVendorPicker() {
        val vendors = VendorConfig.vendors
        val names = vendors.map { "${it.name} (${it.nameEn})" }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("选择厂商")
            .setItems(names) { _, which ->
                showModelPicker(vendors[which])
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showModelPicker(vendor: Vendor) {
        val models = VendorConfig.modelsByVendor[vendor.id]
            ?.filter { it.supportsCode }
            ?: emptyList()

        if (models.isEmpty()) {
            Toast.makeText(requireContext(), "${vendor.name} 暂无可用代码模型", Toast.LENGTH_SHORT).show()
            return
        }

        val names = models.mapIndexed { _, m ->
            val badge = when {
                m.isLatest && m.isBestPerformance -> " ⭐最新·最强"
                m.isLatest -> " ⭐最新"
                m.isBestPerformance -> " 最强"
                else -> ""
            }
            "${m.name}$badge"
        }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("${vendor.name} 模型")
            .setItems(names) { _, which ->
                selectedModel = models[which]
                updateModelButton()
            }
            .setNegativeButton("返回", null)
            .show()
    }

    private fun updateModelButton() {
        val model = selectedModel
        if (model != null) {
            btnSelectModel.text = "${model.vendorName} · ${model.name}"
            btnSelectModel.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            )
            selectedModelText.text = "${model.vendorName} - ${model.name}"
        } else {
            btnSelectModel.text = "选择模型"
            btnSelectModel.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.colorTextSecondary)
            )
            selectedModelText.text = "未选择模型"
        }
    }

    // ═══════════════════════════════════════════
    //  Buttons
    // ═══════════════════════════════════════════

    private fun setupButtons() {
        btnGenerate.setOnClickListener { generateCode() }
        btnCopy.setOnClickListener { copyCode() }
        btnExport.setOnClickListener { exportCode() }
        btnSaveLocal.setOnClickListener { saveToLocal() }
    }

    // ═══════════════════════════════════════════
    //  Code Generation
    // ═══════════════════════════════════════════

    private fun generateCode() {
        val desc = codeDescInput.text.toString().trim()
        if (desc.isEmpty()) {
            Toast.makeText(requireContext(), "请输入代码需求描述", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedModel == null) {
            Toast.makeText(requireContext(), "请先选择模型", Toast.LENGTH_SHORT).show()
            return
        }
        if (isGenerating) return

        isGenerating = true
        generatedCode = ""

        emptyState.visibility = View.GONE
        codeOutputScroll.visibility = View.VISIBLE
        resultHeader.visibility = View.VISIBLE
        codeOutput.text = "正在生成 ${selectedLanguage} 代码..."
        btnGenerate.isEnabled = false
        btnGenerate.text = "生成中..."
        progressBar.visibility = View.VISIBLE

        val prompt = buildString {
            appendLine("你是一个专业的${selectedLanguage}程序员。请根据以下需求生成代码：")
            appendLine()
            appendLine("需求：$desc")
            appendLine()
            appendLine("要求：")
            appendLine("1. 只输出代码，不要包含额外的解释文字")
            appendLine("2. 代码要完整可运行")
            appendLine("3. 添加必要的注释")
            appendLine("4. 遵循${selectedLanguage}的最佳实践")
            appendLine()
            appendLine("请生成代码：")
        }

        apiService.sendMessage(
            model = selectedModel!!,
            prompt = prompt,
            temperature = 0.3,
            maxTokens = 4096,
            onChunk = { chunk ->
                generatedCode += chunk
                handler.post { codeOutput.text = generatedCode }
            },
            onComplete = { response ->
                if (generatedCode.isEmpty()) generatedCode = response
                handler.post {
                    codeOutput.text = generatedCode
                    finishGeneration()
                }
            },
            onError = { error ->
                handler.post {
                    codeOutput.text = "生成失败: $error"
                    finishGeneration()
                }
            }
        )
    }

    private fun finishGeneration() {
        isGenerating = false
        btnGenerate.isEnabled = true
        btnGenerate.text = "生成代码"
        progressBar.visibility = View.GONE
    }

    // ═══════════════════════════════════════════
    //  Copy & Export
    // ═══════════════════════════════════════════

    private fun copyCode() {
        if (generatedCode.isEmpty()) {
            Toast.makeText(requireContext(), "没有可复制的代码", Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("generated_code", generatedCode))
        Toast.makeText(requireContext(), "代码已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    private fun exportCode() {
        if (generatedCode.isEmpty()) {
            Toast.makeText(requireContext(), "没有可导出的代码", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val ext = languageExtensions[selectedLanguage] ?: "txt"
            val file = File(requireContext().cacheDir, "generated_code.$ext")
            file.writeText(generatedCode)

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
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

    private fun saveToLocal() {
        val code = generatedCode
        if (code.isEmpty()) {
            Toast.makeText(requireContext(), "没有可保存的代码", Toast.LENGTH_SHORT).show()
            return
        }
        val lang = selectedLanguage
        val ext = languageExtensions[lang] ?: "txt"

        try {
            val filename = "generated_${System.currentTimeMillis()}.$ext"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.RELATIVE_PATH, "Download/AI_Code")
                }
                val uri = requireContext().contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    requireContext().contentResolver.openOutputStream(it)?.use { os ->
                        os.write(code.toByteArray())
                    }
                    Toast.makeText(requireContext(), "已保存到 Downloads/AI_Code/$filename", Toast.LENGTH_SHORT).show()
                }
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AI_Code")
                dir.mkdirs()
                val file = File(dir, filename)
                file.writeText(code)
                Toast.makeText(requireContext(), "已保存到 ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}