package com.aiaggregator.app.ui.history

import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.aiaggregator.app.R
import com.aiaggregator.app.data.ApiService
import com.aiaggregator.app.data.VendorConfig
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

    // ==================== PagerAdapter ====================

    inner class HistoryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> HistoryTabFragment()
            else -> MyTabFragment()
        }
    }
}

// ==================== HistoryTabFragment ====================

class HistoryTabFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx = requireContext()
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 80, 32, 32)
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorBackground))
        }

        // Empty state icon
        val iconView = TextView(ctx).apply {
            text = "📋"
            textSize = 64f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        root.addView(iconView)

        // Empty state title
        root.addView(TextView(ctx).apply {
            text = "暂无对话记录"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
        })

        // Empty state description
        root.addView(TextView(ctx).apply {
            text = "开始对话后，历史记录将显示在这里\n您可以查看、搜索和继续之前的对话"
            textSize = 14f
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextSecondary))
            gravity = Gravity.CENTER
            setLineSpacing(6f, 1f)
        })

        return root
    }
}

// ==================== MyTabFragment ====================

class MyTabFragment : Fragment() {

    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx = requireContext()
        apiService = ApiService(ctx)

        val scrollView = ScrollView(ctx).apply {
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorBackground))
        }

        val rootLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 48)
        }
        scrollView.addView(rootLayout)

        // ==================== Section: API Key 配置 ====================
        rootLayout.addView(buildSectionHeader(ctx, "API Key 配置", "配置各厂商的 API Key 后即可使用对应模型"))

        VendorConfig.vendors.forEach { vendor ->
            rootLayout.addView(buildVendorCard(ctx, vendor))
        }

        // ==================== Section: 主题模式 ====================
        rootLayout.addView(buildSpacer(ctx, 24))
        rootLayout.addView(buildThemeSection(ctx))

        // ==================== Section: 关于 ====================
        rootLayout.addView(buildSpacer(ctx, 24))
        rootLayout.addView(buildAboutSection(ctx))

        return scrollView
    }

    // ---- Section Header ----

    private fun buildSectionHeader(ctx: android.content.Context, title: String, subtitle: String): View {
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 8, 4, 16)
            addView(TextView(ctx).apply {
                text = title
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
                setPadding(0, 0, 0, 4)
            })
            addView(TextView(ctx).apply {
                text = subtitle
                textSize = 13f
                setTextColor(ContextCompat.getColor(ctx, R.color.colorTextSecondary))
            })
        }
    }

    // ---- Vendor Card ----

    private fun buildVendorCard(ctx: android.content.Context, vendor: com.aiaggregator.app.models.Vendor): View {
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorSurface))
            setPadding(
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt()
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (10 * resources.displayMetrics.density).toInt()
            }
        }

        // Header row: avatar + vendor name
        val headerRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
        }

        // Avatar circle
        val avatar = TextView(ctx).apply {
            text = vendor.name.first().toString()
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_avatar_small)
            layoutParams = LinearLayout.LayoutParams(
                (40 * resources.displayMetrics.density).toInt(),
                (40 * resources.displayMetrics.density).toInt()
            ).apply {
                rightMargin = (14 * resources.displayMetrics.density).toInt()
            }
        }
        headerRow.addView(avatar)

        // Name column
        val nameCol = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        nameCol.addView(TextView(ctx).apply {
            text = vendor.name
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
        })
        nameCol.addView(TextView(ctx).apply {
            text = vendor.nameEn
            textSize = 12f
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextHint))
            setPadding(0, 2, 0, 0)
        })
        headerRow.addView(nameCol)

        // Category badge
        val categoryBadge = TextView(ctx).apply {
            text = if (vendor.category == "domestic") "国内" else "海外"
            textSize = 11f
            setTextColor(
                if (vendor.category == "domestic")
                    ContextCompat.getColor(ctx, R.color.colorTagBlueText)
                else
                    ContextCompat.getColor(ctx, R.color.colorTagOrangeText)
            )
            setPadding(
                (10 * resources.displayMetrics.density).toInt(),
                (4 * resources.displayMetrics.density).toInt(),
                (10 * resources.displayMetrics.density).toInt(),
                (4 * resources.displayMetrics.density).toInt()
            )
            setBackgroundColor(
                if (vendor.category == "domestic")
                    ContextCompat.getColor(ctx, R.color.colorTagBlue)
                else
                    ContextCompat.getColor(ctx, R.color.colorTagOrange)
            )
        }
        headerRow.addView(categoryBadge)

        card.addView(headerRow)

        // Divider
        card.addView(View(ctx).apply {
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorDivider))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                bottomMargin = (12 * resources.displayMetrics.density).toInt()
            }
        })

        // API Key input row
        val inputRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val input = EditText(ctx).apply {
            hint = "输入 ${vendor.name} API Key"
            textSize = 13f
            inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            setBackgroundResource(R.drawable.bg_input)
            setPadding(
                (16 * resources.displayMetrics.density).toInt(),
                (12 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (12 * resources.displayMetrics.density).toInt()
            )
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
            setHintTextColor(ContextCompat.getColor(ctx, R.color.colorTextHint))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        inputRow.addView(input)

        val saveBtn = Button(ctx).apply {
            text = "保存"
            textSize = 13f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = (10 * resources.displayMetrics.density).toInt()
            }
            setOnClickListener {
                val key = input.text.toString().trim()
                if (key.isNotEmpty()) {
                    apiService.setApiKey(vendor.id, key)
                    Toast.makeText(ctx, "${vendor.name} API Key 已保存", Toast.LENGTH_SHORT).show()
                    // Show saved indicator
                    showKeyStatus(ctx, card, vendor)
                }
            }
        }
        inputRow.addView(saveBtn)
        card.addView(inputRow)

        // Show existing key status
        showKeyStatus(ctx, card, vendor)

        return card
    }

    private fun showKeyStatus(
        ctx: android.content.Context,
        card: ViewGroup,
        vendor: com.aiaggregator.app.models.Vendor
    ) {
        // Remove old status if exists
        val existingStatus = card.findViewWithTag<View>("key_status_${vendor.id}")
        existingStatus?.let { card.removeView(it) }

        val existingKey = apiService.getApiKey(vendor.id)
        if (existingKey != null && existingKey.length >= 8) {
            val masked = "${existingKey.take(4)}****${existingKey.takeLast(4)}"
            val statusView = LinearLayout(ctx).apply {
                tag = "key_status_${vendor.id}"
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(
                    0,
                    (10 * resources.displayMetrics.density).toInt(),
                    0,
                    0
                )
            }
            statusView.addView(TextView(ctx).apply {
                text = "✅"
                textSize = 14f
            })
            statusView.addView(TextView(ctx).apply {
                text = " 已配置: $masked"
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                setPadding(
                    (4 * resources.displayMetrics.density).toInt(),
                    0,
                    0,
                    0
                )
            })
            card.addView(statusView)
        }
    }

    // ---- Spacer ----

    private fun buildSpacer(ctx: android.content.Context, heightDp: Int): View {
        return View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (heightDp * resources.displayMetrics.density).toInt()
            )
        }
    }

    // ---- Theme Section ----

    private fun buildThemeSection(ctx: android.content.Context): View {
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorSurface))
            setPadding(
                (20 * resources.displayMetrics.density).toInt(),
                (20 * resources.displayMetrics.density).toInt(),
                (20 * resources.displayMetrics.density).toInt(),
                (20 * resources.displayMetrics.density).toInt()
            )
        }

        // Title
        card.addView(TextView(ctx).apply {
            text = "主题模式"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
            setPadding(0, 0, 0, (16 * resources.displayMetrics.density).toInt())
        })

        val options = listOf(
            "跟随系统" to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            "浅色模式" to AppCompatDelegate.MODE_NIGHT_NO,
            "深色模式" to AppCompatDelegate.MODE_NIGHT_YES
        )

        val currentMode = ctx.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
            .getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        val buttonRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        options.forEach { (label, mode) ->
            val isSelected = mode == currentMode
            val btn = Button(ctx).apply {
                text = label
                textSize = 13f
                setTextColor(
                    if (isSelected) android.graphics.Color.WHITE
                    else ContextCompat.getColor(ctx, R.color.colorTextPrimary)
                )
                setBackgroundColor(
                    if (isSelected) ContextCompat.getColor(ctx, R.color.colorPrimary)
                    else ContextCompat.getColor(ctx, R.color.colorSurfaceVariant)
                )
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    leftMargin = if (options.indexOf(Pair(label, mode)) > 0)
                        (8 * resources.displayMetrics.density).toInt()
                    else 0
                    rightMargin = if (options.indexOf(Pair(label, mode)) < options.size - 1)
                        (8 * resources.displayMetrics.density).toInt()
                    else 0
                }
                setOnClickListener {
                    AppCompatDelegate.setDefaultNightMode(mode)
                    ctx.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
                        .edit().putInt("night_mode", mode).apply()
                    // Refresh theme
                    activity?.recreate()
                }
            }
            buttonRow.addView(btn)
        }

        card.addView(buttonRow)
        return card
    }

    // ---- About Section ----

    private fun buildAboutSection(ctx: android.content.Context): View {
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorSurface))
            setPadding(
                (20 * resources.displayMetrics.density).toInt(),
                (20 * resources.displayMetrics.density).toInt(),
                (20 * resources.displayMetrics.density).toInt(),
                (20 * resources.displayMetrics.density).toInt()
            )
        }

        // Title
        card.addView(TextView(ctx).apply {
            text = "关于"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
            setPadding(0, 0, 0, (16 * resources.displayMetrics.density).toInt())
        })

        val totalModels = VendorConfig.getAllModels().size
        val infoItems = listOf(
            "应用名称" to "AI模型聚合助手",
            "版本号" to "1.0.0",
            "支持厂商" to "${VendorConfig.vendors.size} 个",
            "支持模型" to "${totalModels}+ 个",
            "核心功能" to "文本生成 / 代码生成 / 图片生成 / 集群对比"
        )

        infoItems.forEach { (label, value) ->
            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, (8 * resources.displayMetrics.density).toInt(), 0, 0)
            }

            row.addView(TextView(ctx).apply {
                text = label
                textSize = 14f
                setTextColor(ContextCompat.getColor(ctx, R.color.colorTextHint))
                layoutParams = LinearLayout.LayoutParams(
                    (80 * resources.displayMetrics.density).toInt(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })

            row.addView(TextView(ctx).apply {
                text = value
                textSize = 14f
                setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
            })

            card.addView(row)
        }

        // Divider
        card.addView(View(ctx).apply {
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorDivider))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                topMargin = (16 * resources.displayMetrics.density).toInt()
                bottomMargin = (12 * resources.displayMetrics.density).toInt()
            }
        })

        // Footer
        card.addView(TextView(ctx).apply {
            text = "AI Model Aggregator © 2024"
            textSize = 12f
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextHint))
            gravity = Gravity.CENTER
        })

        return card
    }
}