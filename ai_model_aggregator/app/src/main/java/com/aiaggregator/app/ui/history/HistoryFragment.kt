package com.aiaggregator.app.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.aiaggregator.app.R
import com.aiaggregator.app.data.ApiService
import com.aiaggregator.app.data.VendorConfig
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HistoryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)

        viewPager.adapter = HistoryPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "对话历史"
                else -> "我的"
            }
        }.attach()
    }

    class HistoryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount() = 2
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> HistoryTabFragment()
            else -> MyTabFragment()
        }
    }
}

class HistoryTabFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return TextView(requireContext()).apply {
            text = "暂无对话记录\n\n开始对话后，历史记录将显示在这里"
            gravity = android.view.Gravity.CENTER
            setTextColor(resources.getColor(R.color.colorTextSecondary, null))
            textSize = 14f
            setPadding(32, 32, 32, 32)
        }
    }
}

class MyTabFragment : Fragment() {
    private lateinit var apiService: ApiService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        apiService = ApiService(requireContext())
        val scrollView = ScrollView(requireContext())
        val linearLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 32)
        }
        scrollView.addView(linearLayout)

        // API Key section
        linearLayout.addView(TextView(requireContext()).apply {
            text = "API Key 配置"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 8, 0, 4)
        })
        linearLayout.addView(TextView(requireContext()).apply {
            text = "配置各厂商的API Key后即可使用对应模型"
            textSize = 13f
            setTextColor(resources.getColor(R.color.colorTextSecondary, null))
            setPadding(0, 0, 0, 16)
        })

        VendorConfig.vendors.forEach { vendor ->
            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(resources.getColor(R.color.colorSurface, null))
                setPadding(16, 12, 16, 12)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8 }
            }

            // Vendor header
            val header = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
            val avatar = TextView(requireContext()).apply {
                text = vendor.name.first().toString()
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(resources.getColor(R.color.colorPrimary, null))
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.bg_avatar_small)
                layoutParams = LinearLayout.LayoutParams(32, 32).apply { rightMargin = 12 }
            }
            header.addView(avatar)
            val nameCol = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
            nameCol.addView(TextView(requireContext()).apply {
                text = vendor.name
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            nameCol.addView(TextView(requireContext()).apply {
                text = vendor.nameEn
                textSize = 12f
                setTextColor(resources.getColor(R.color.colorTextSecondary, null))
            })
            header.addView(nameCol)
            card.addView(header)

            // API Key input
            val inputRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 0)
            }
            val input = EditText(requireContext()).apply {
                hint = "输入 ${vendor.name} API Key"
                textSize = 13f
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD or android.text.InputType.TYPE_CLASS_TEXT
                setBackgroundResource(R.drawable.bg_input)
                setPadding(16, 12, 16, 12)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            inputRow.addView(input)
            val saveBtn = Button(requireContext()).apply {
                text = "保存"
                textSize = 13f
                setTextColor(android.graphics.Color.WHITE)
                backgroundTintList = resources.getColorStateList(R.color.colorPrimary, null)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { leftMargin = 8 }
                setOnClickListener {
                    val key = input.text.toString().trim()
                    if (key.isNotEmpty()) {
                        apiService.setApiKey(vendor.id, key)
                        Toast.makeText(requireContext(), "${vendor.name} API Key 已保存", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            inputRow.addView(saveBtn)
            card.addView(inputRow)

            // Show if key exists
            val existingKey = apiService.getApiKey(vendor.id)
            if (existingKey != null) {
                card.addView(TextView(requireContext()).apply {
                    text = "已配置: ${existingKey.take(4)}...${existingKey.takeLast(4)}"
                    textSize = 12f
                    setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                    setPadding(0, 8, 0, 0)
                })
            }

            linearLayout.addView(card)
        }

        // About section
        linearLayout.addView(LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(resources.getColor(R.color.colorSurface, null))
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16 }

            addView(TextView(requireContext()).apply {
                text = "关于"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            val infoItems = listOf(
                "应用名称" to "AI模型聚合助手",
                "版本" to "1.0.0",
                "支持厂商" to "20个",
                "支持模型" to "${VendorConfig.getAllModels().size}+",
                "功能" to "文本生成 / 代码生成 / 图片生成 / 集群对比"
            )
            infoItems.forEach { (label, value) ->
                addView(LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 0)
                    addView(TextView(requireContext()).apply {
                        text = label
                        textSize = 13f
                        setTextColor(resources.getColor(R.color.colorTextSecondary, null))
                        layoutParams = LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT)
                    })
                    addView(TextView(requireContext()).apply { text = value; textSize = 13f })
                })
            }
        })

        return scrollView
    }
}