package com.aiaggregator.app.ui.image

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiaggregator.app.R
import com.aiaggregator.app.data.VendorConfig
import com.aiaggregator.app.models.AiModel
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class ImageFragment : Fragment() {

    private var selectedModel: AiModel? = null
    private val generatedImages = mutableListOf<String>()
    private var isGenerating = false
    private val handler = Handler(Looper.getMainLooper())
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Aspect ratio mapping ──
    private val aspectRatios = listOf("1:1", "3:4", "4:3", "9:16", "16:9")
    private val sizeMap = mapOf(
        "1:1" to "square",
        "3:4" to "portrait_4_3",
        "4:3" to "landscape_4_3",
        "9:16" to "portrait_16_9",
        "16:9" to "landscape_16_9"
    )

    // ── View references ──
    private lateinit var modelChipsContainer: LinearLayout
    private lateinit var ratioSpinner: Spinner
    private lateinit var numSpinner: Spinner
    private lateinit var promptInput: TextInputEditText
    private lateinit var btnGenerate: MaterialButton
    private lateinit var resultArea: FrameLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var imageGrid: RecyclerView
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupModelChips()
        setupSpinners()
        setupImageGrid()
        setupButtons()
    }

    private fun bindViews(view: View) {
        modelChipsContainer = view.findViewById(R.id.img_model_chips)
        ratioSpinner = view.findViewById(R.id.ratio_spinner)
        numSpinner = view.findViewById(R.id.num_spinner)
        promptInput = view.findViewById(R.id.img_prompt_input)
        btnGenerate = view.findViewById(R.id.btn_generate_img)
        resultArea = view.findViewById(R.id.img_result_area)
        emptyState = view.findViewById(R.id.empty_img_state)
        progressBar = view.findViewById(R.id.img_progress)
        imageGrid = view.findViewById(R.id.img_grid)
    }

    // ═══════════════════════════════════════════
    //  Model Chips
    // ═══════════════════════════════════════════

    private fun setupModelChips() {
        val ctx = requireContext()
        val models = VendorConfig.getImageModels()
        models.forEach { model ->
            val chip = Chip(ctx).apply {
                text = "${model.vendorName} - ${model.name}"
                isCheckable = true
                setOnClickListener {
                    selectedModel = model
                    refreshModelChipStates()
                }
            }
            modelChipsContainer.addView(chip)
        }
    }

    private fun refreshModelChipStates() {
        for (i in 0 until modelChipsContainer.childCount) {
            val chip = modelChipsContainer.getChildAt(i) as? Chip ?: continue
            chip.isChecked = chip == modelChipsContainer.findViewWithTag<Chip>(selectedModel?.let {
                "${it.vendorName} - ${it.name}"
            }) ?: false
        }
    }

    // ═══════════════════════════════════════════
    //  Spinners (Aspect Ratio & Count)
    // ═══════════════════════════════════════════

    private fun setupSpinners() {
        val ctx = requireContext()

        // Aspect ratio
        val ratioAdapter = ArrayAdapter(
            ctx,
            android.R.layout.simple_spinner_item,
            aspectRatios
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        ratioSpinner.adapter = ratioAdapter

        // Image count (1-4)
        val counts = (1..4).map { it.toString() }
        val countAdapter = ArrayAdapter(
            ctx,
            android.R.layout.simple_spinner_item,
            counts
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        numSpinner.adapter = countAdapter
    }

    // ═══════════════════════════════════════════
    //  Image Grid (RecyclerView)
    // ═══════════════════════════════════════════

    private fun setupImageGrid() {
        imageAdapter = ImageAdapter(generatedImages)
        imageGrid.layoutManager = GridLayoutManager(requireContext(), 2)
        imageGrid.adapter = imageAdapter
    }

    // ═══════════════════════════════════════════
    //  Buttons
    // ═══════════════════════════════════════════

    private fun setupButtons() {
        btnGenerate.setOnClickListener { generateImages() }
    }

    // ═══════════════════════════════════════════
    //  Image Generation
    // ═══════════════════════════════════════════

    private fun generateImages() {
        val prompt = promptInput.text.toString().trim()
        if (prompt.isEmpty()) {
            Toast.makeText(requireContext(), "请输入图片描述提示词", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedModel == null) {
            Toast.makeText(requireContext(), "请先选择图像生成模型", Toast.LENGTH_SHORT).show()
            return
        }
        if (isGenerating) return

        isGenerating = true
        generatedImages.clear()

        // Show loading state
        emptyState.visibility = View.GONE
        imageGrid.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        btnGenerate.isEnabled = false
        btnGenerate.text = "生成中..."

        // Get parameters
        val selectedRatio = aspectRatios[ratioSpinner.selectedItemPosition]
        val imageSize = sizeMap[selectedRatio] ?: "square"
        val count = numSpinner.selectedItemPosition + 1

        // Encode prompt
        val encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8")
        val requestCount = if (count > 1) count else 1

        // Build request
        val url = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image" +
                "?prompt=$encodedPrompt&image_size=$imageSize"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post {
                    Toast.makeText(requireContext(), "网络请求失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    finishGeneration()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    handler.post {
                        Toast.makeText(requireContext(), "生成失败: HTTP ${response.code}", Toast.LENGTH_SHORT).show()
                        finishGeneration()
                    }
                    return
                }

                try {
                    // Parse response - try JSON first
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val imageUrl = json?.get("url")?.asString
                        ?: json?.get("image_url")?.asString
                        ?: json?.get("data")?.asString
                        ?: json?.get("result")?.asString

                    if (imageUrl != null) {
                        generatedImages.add(imageUrl)
                        // For multiple images, repeat the same URL or add the original URL
                        if (requestCount > 1) {
                            repeat(requestCount - 1) {
                                generatedImages.add(imageUrl)
                            }
                        }
                    } else {
                        // Try to get from array
                        val images = json?.getAsJsonArray("images")
                            ?: json?.getAsJsonArray("data")
                            ?: json?.getAsJsonArray("urls")
                        if (images != null && images.size() > 0) {
                            for (i in 0 until minOf(images.size(), requestCount)) {
                                val img = images[i]
                                if (img.isJsonObject) {
                                    generatedImages.add(img.asJsonObject.get("url")?.asString ?: img.asString)
                                } else {
                                    generatedImages.add(img.asString)
                                }
                            }
                        } else {
                            // Last resort: treat the response body as a URL
                            val trimmed = body.trim()
                            if (trimmed.startsWith("http")) {
                                generatedImages.add(trimmed)
                            }
                        }
                    }

                    handler.post {
                        if (generatedImages.isEmpty()) {
                            Toast.makeText(requireContext(), "未能解析生成结果", Toast.LENGTH_SHORT).show()
                        }
                        imageGrid.visibility = View.VISIBLE
                        imageAdapter.notifyDataSetChanged()
                        finishGeneration()
                    }
                } catch (e: Exception) {
                    // If not JSON, maybe the body is a direct URL or base64
                    val trimmed = body.trim()
                    if (trimmed.startsWith("http")) {
                        generatedImages.add(trimmed)
                        handler.post {
                            imageGrid.visibility = View.VISIBLE
                            imageAdapter.notifyDataSetChanged()
                            finishGeneration()
                        }
                    } else {
                        handler.post {
                            Toast.makeText(requireContext(), "解析响应失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            finishGeneration()
                        }
                    }
                }
            }
        })
    }

    private fun finishGeneration() {
        isGenerating = false
        progressBar.visibility = View.GONE
        btnGenerate.isEnabled = true
        btnGenerate.text = "生成图片"
    }

    // ═══════════════════════════════════════════
    //  ImageAdapter
    // ═══════════════════════════════════════════

    inner class ImageAdapter(private val images: List<String>) :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image_result, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.imageView.context)
                .load(images[position])
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .centerCrop()
                .into(holder.imageView)
        }

        override fun getItemCount() = images.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.result_image)
        }
    }
}