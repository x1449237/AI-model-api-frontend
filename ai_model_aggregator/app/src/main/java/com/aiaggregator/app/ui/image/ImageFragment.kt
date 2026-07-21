package com.aiaggregator.app.ui.image

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class ImageFragment : Fragment() {

    private var selectedModel: AiModel? = null
    private val generatedImages = mutableListOf<String>()
    private var isGenerating = false
    private val handler = Handler(Looper.getMainLooper())
    private val gson = Gson()

    private var referenceImageUri: Uri? = null

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
    private lateinit var btnSelectRefImage: MaterialButton
    private lateinit var refImagePreview: ImageView
    private lateinit var btnClearRefImage: Button
    private lateinit var resultArea: FrameLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var imageGrid: RecyclerView
    private lateinit var imageAdapter: ImageAdapter

    // ── Image picker launcher ──
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    // ── Scale/zoom state ──
    private var scaleFactor = 1.0f
    private var currentScale = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                referenceImageUri = uri
                refImagePreview.setImageURI(uri)
                refImagePreview.visibility = View.VISIBLE
                btnClearRefImage.visibility = View.VISIBLE
            }
        }
    }

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
        btnSelectRefImage = view.findViewById(R.id.btn_select_ref_image)
        refImagePreview = view.findViewById(R.id.ref_image_preview)
        btnClearRefImage = view.findViewById(R.id.btn_clear_ref_image)
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
        val ratioAdapter = android.widget.ArrayAdapter(
            ctx,
            android.R.layout.simple_spinner_item,
            aspectRatios
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        ratioSpinner.adapter = ratioAdapter

        // Image count (1-4)
        val counts = (1..4).map { it.toString() }
        val countAdapter = android.widget.ArrayAdapter(
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
        imageAdapter = ImageAdapter(
            generatedImages,
            onSaveClick = { url -> saveImageToGallery(url) },
            onShareClick = { url -> shareImage(url) },
            onImageClick = { url -> showZoomPreview(url) }
        )
        imageGrid.layoutManager = GridLayoutManager(requireContext(), 2)
        imageGrid.adapter = imageAdapter
    }

    // ═══════════════════════════════════════════
    //  Buttons
    // ═══════════════════════════════════════════

    private fun setupButtons() {
        btnGenerate.setOnClickListener { generateImages() }
        btnSelectRefImage.setOnClickListener { selectReferenceImage() }
        btnClearRefImage.setOnClickListener { clearReferenceImage() }
    }

    // ═══════════════════════════════════════════
    //  Reference Image Selection
    // ═══════════════════════════════════════════

    private fun selectReferenceImage() {
        imagePickerLauncher.launch("image/*")
    }

    private fun clearReferenceImage() {
        referenceImageUri = null
        refImagePreview.setImageDrawable(null)
        refImagePreview.visibility = View.GONE
        btnClearRefImage.visibility = View.GONE
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

        // Build base URL
        val baseUrl = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image"
        var url = "$baseUrl?prompt=$encodedPrompt&image_size=$imageSize"

        // If reference image is selected, encode it as base64 and add to URL
        if (referenceImageUri != null) {
            try {
                val base64Image = encodeImageToBase64(referenceImageUri!!)
                if (base64Image != null) {
                    val encodedImage = java.net.URLEncoder.encode(base64Image, "UTF-8")
                    url += "&image_url=$encodedImage"
                }
            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(requireContext(), "读取参考图片失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    finishGeneration()
                }
                return
            }
        }

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
    //  Encode image to Base64
    // ═══════════════════════════════════════════

    private fun encodeImageToBase64(uri: Uri): String? {
        val inputStream: InputStream = requireContext().contentResolver.openInputStream(uri)
            ?: return null
        val bytes = inputStream.use { it.readBytes() }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    // ═══════════════════════════════════════════
    //  Save Image to Gallery
    // ═══════════════════════════════════════════

    private fun saveImageToGallery(imageUrl: String) {
        Thread {
            try {
                val bitmap = Glide.with(requireContext().applicationContext)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get()

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "ai_image_${System.currentTimeMillis()}.jpg")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AI聚合助手")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }

                    val uri = requireContext().contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                    )

                    if (uri != null) {
                        requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                        }
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        requireContext().contentResolver.update(uri, contentValues, null, null)

                        handler.post {
                            Toast.makeText(requireContext(), "已保存到相册", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val path = MediaStore.Images.Media.insertImage(
                        requireContext().contentResolver,
                        bitmap,
                        "ai_image_${System.currentTimeMillis()}",
                        "AI聚合助手生成"
                    )
                    handler.post {
                        if (path != null) {
                            Toast.makeText(requireContext(), "已保存到相册", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "保存失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(requireContext(), "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // ═══════════════════════════════════════════
    //  Share Image
    // ═══════════════════════════════════════════

    private fun shareImage(imageUrl: String) {
        Thread {
            try {
                val bitmap = Glide.with(requireContext().applicationContext)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get()

                val cacheDir = File(requireContext().cacheDir, "shared_images")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val file = File(cacheDir, "share_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }

                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                handler.post {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "分享图片"))
                }
            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(requireContext(), "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // ═══════════════════════════════════════════
    //  Zoom Preview Dialog
    // ═══════════════════════════════════════════

    private fun showZoomPreview(imageUrl: String) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val zoomView = ZoomableImageView(requireContext())

        dialog.setContentView(zoomView)
        dialog.show()

        Glide.with(requireContext())
            .load(imageUrl)
            .into(zoomView)
    }

    // ═══════════════════════════════════════════
    //  ZoomableImageView (inner class)
    // ═══════════════════════════════════════════

    inner class ZoomableImageView(context: android.content.Context) : androidx.appcompat.widget.AppCompatImageView(context) {

        private var scaleFactor = 1.0f
        private val matrix = Matrix()
        private val NONE = 0
        private val DRAG = 1
        private val ZOOM = 2
        private var mode = NONE
        private var lastTouchX = 0f
        private var lastTouchY = 0f
        private var startX = 0f
        private var startY = 0f
        private val minScale = 1.0f
        private val maxScale = 5.0f

        private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                mode = ZOOM
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                var scaleFactor = detector.scaleFactor
                val currentScale = getCurrentScale()
                if (currentScale * scaleFactor < minScale) {
                    scaleFactor = minScale / currentScale
                }
                if (currentScale * scaleFactor > maxScale) {
                    scaleFactor = maxScale / currentScale
                }
                matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                imageMatrix = matrix
                return true
            }
        })

        private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val currentScale = getCurrentScale()
                if (currentScale > minScale) {
                    matrix.reset()
                    imageMatrix = matrix
                } else {
                    matrix.postScale(2.0f, 2.0f, e.x, e.y)
                    imageMatrix = matrix
                }
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // Close the dialog by finding the parent dialog
                var parent = parent
                while (parent != null) {
                    if (parent is Dialog) {
                        parent.dismiss()
                        break
                    }
                    parent = (parent as? View)?.parent
                }
                return true
            }
        })

        init {
            scaleType = ScaleType.MATRIX
            setBackgroundColor(android.graphics.Color.BLACK)
            setOnTouchListener { _, event ->
                scaleDetector.onTouchEvent(event)
                gestureDetector.onTouchEvent(event)

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        mode = DRAG
                        lastTouchX = event.x
                        lastTouchY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (mode == DRAG) {
                            val dx = event.x - lastTouchX
                            val dy = event.y - lastTouchY
                            matrix.postTranslate(dx, dy)
                            imageMatrix = matrix
                            lastTouchX = event.x
                            lastTouchY = event.y
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                        mode = NONE
                    }
                }
                true
            }
        }

        private fun getCurrentScale(): Float {
            val values = FloatArray(9)
            matrix.getValues(values)
            return values[Matrix.MSCALE_X]
        }
    }

    // ═══════════════════════════════════════════
    //  ImageAdapter
    // ═══════════════════════════════════════════

    inner class ImageAdapter(
        private val images: List<String>,
        private val onSaveClick: (String) -> Unit,
        private val onShareClick: (String) -> Unit,
        private val onImageClick: (String) -> Unit
    ) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image_result, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val imageUrl = images[position]
            Glide.with(holder.imageView.context)
                .load(imageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .centerCrop()
                .into(holder.imageView)

            holder.imageView.setOnClickListener { onImageClick(imageUrl) }
            holder.btnSave.setOnClickListener { onSaveClick(imageUrl) }
            holder.btnShare.setOnClickListener { onShareClick(imageUrl) }
        }

        override fun getItemCount() = images.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.result_image)
            val btnSave: Button = view.findViewById(R.id.btn_save_img)
            val btnShare: Button = view.findViewById(R.id.btn_share_img)
        }
    }
}