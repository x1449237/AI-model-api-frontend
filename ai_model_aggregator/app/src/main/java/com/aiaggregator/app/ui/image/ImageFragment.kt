package com.aiaggregator.app.ui.image

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiaggregator.app.R
import com.aiaggregator.app.data.ApiService
import com.aiaggregator.app.data.VendorConfig
import com.aiaggregator.app.models.AiModel
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip

class ImageFragment : Fragment() {
    private lateinit var apiService: ApiService
    private var selectedModel: AiModel? = null
    private val generatedImages = mutableListOf<String>()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = ApiService(requireContext())

        setupModelChips(view)
        view.findViewById<Button>(R.id.btn_generate_img).setOnClickListener { generateImage(view) }
    }

    private fun setupModelChips(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.img_model_chips)
        VendorConfig.getImageModels().forEach { model ->
            val chip = Chip(requireContext()).apply {
                text = "${model.vendorName} - ${model.name}"
                isCheckable = true
                setOnClickListener {
                    selectedModel = model
                    for (i in 0 until container.childCount) {
                        (container.getChildAt(i) as? Chip)?.isChecked = container.getChildAt(i) == this
                    }
                }
            }
            container.addView(chip)
        }
    }

    private fun generateImage(view: View) {
        val prompt = view.findViewById<EditText>(R.id.img_prompt_input).text.toString().trim()
        if (prompt.isEmpty() || selectedModel == null) {
            Toast.makeText(requireContext(), "请选择模型并输入提示词", Toast.LENGTH_SHORT).show()
            return
        }

        view.findViewById<View>(R.id.empty_img_state).visibility = View.GONE
        view.findViewById<ProgressBar>(R.id.img_progress).visibility = View.VISIBLE

        // Simulate image generation
        handler.postDelayed({
            val imageUrl = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${java.net.URLEncoder.encode(prompt, "UTF-8")}&image_size=square"
            generatedImages.add(imageUrl)
            view.findViewById<ProgressBar>(R.id.img_progress).visibility = View.GONE
            view.findViewById<RecyclerView>(R.id.img_grid).visibility = View.VISIBLE

            val grid = view.findViewById<RecyclerView>(R.id.img_grid)
            grid.layoutManager = GridLayoutManager(requireContext(), 2)
            grid.adapter = ImageAdapter(generatedImages)
        }, 2000)
    }

    inner class ImageAdapter(private val images: List<String>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_result, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.imageView.context).load(images[position]).into(holder.imageView)
        }

        override fun getItemCount() = images.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.result_image)
        }
    }
}