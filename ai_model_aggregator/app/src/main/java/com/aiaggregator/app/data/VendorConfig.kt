package com.aiaggregator.app.data

import com.aiaggregator.app.models.AiModel
import com.aiaggregator.app.models.Vendor

object VendorConfig {
    val vendors = listOf(
        // 国内厂商 (15)
        Vendor("baidu", "百度", "Baidu", "domestic", "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat"),
        Vendor("alibaba", "阿里巴巴", "Alibaba", "domestic", "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"),
        Vendor("deepseek", "深度求索", "DeepSeek", "domestic", "https://api.deepseek.com/v1/chat/completions"),
        Vendor("bytedance", "字节跳动", "ByteDance", "domestic", "https://ark.cn-beijing.volces.com/api/v3/chat/completions"),
        Vendor("tencent", "腾讯", "Tencent", "domestic", "https://hunyuan.tencentcloudapi.com"),
        Vendor("zhipu", "智谱AI", "Zhipu AI", "domestic", "https://open.bigmodel.cn/api/paas/v4/chat/completions"),
        Vendor("iflytek", "科大讯飞", "iFlytek", "domestic", "https://spark-api-open.xf-yun.com/v1/chat/completions"),
        Vendor("sensetime", "商汤", "SenseTime", "domestic", "https://api.sensenova.cn/v1/chat/completions"),
        Vendor("360", "360", "360 AI", "domestic", "https://api.360.cn/v1/chat/completions"),
        Vendor("kunlun", "昆仑万维", "Kunlun", "domestic", "https://api.skywork.cn/v1/chat/completions"),
        Vendor("moonshot", "月之暗面", "Moonshot AI", "domestic", "https://api.moonshot.cn/v1/chat/completions"),
        Vendor("baichuan", "百川智能", "Baichuan", "domestic", "https://api.baichuan-ai.com/v1/chat/completions"),
        Vendor("yi", "零一万物", "01.AI", "domestic", "https://api.lingyiwanwu.com/v1/chat/completions"),
        Vendor("nebula", "智云", "Nebula", "domestic", "https://api.nebula-ai.com/v1/chat/completions"),
        Vendor("cas", "中科院", "CAS", "domestic", "https://api.taichu.cas.cn/v1/chat/completions"),
        // 国外厂商 (5)
        Vendor("openai", "OpenAI", "OpenAI", "international", "https://api.openai.com/v1/chat/completions"),
        Vendor("anthropic", "Anthropic", "Anthropic", "international", "https://api.anthropic.com/v1/messages"),
        Vendor("google", "Google", "Google", "international", "https://generativelanguage.googleapis.com/v1beta/models"),
        Vendor("meta", "Meta", "Meta", "international", "https://api.meta.ai/v1/chat/completions"),
        Vendor("mistral", "Mistral AI", "Mistral AI", "international", "https://api.mistral.ai/v1/chat/completions"),
    )

    val modelsByVendor: Map<String, List<AiModel>> = mapOf(
        "baidu" to listOf(
            AiModel("ernie-4.5-turbo", "ERNIE 4.5 Turbo", "baidu", "百度", 128000, isLatest = true, isBestPerformance = true, description = "文心最新旗舰，128K上下文"),
            AiModel("ernie-4.0-turbo", "ERNIE 4.0 Turbo", "baidu", "百度", 128000),
            AiModel("ernie-3.5", "ERNIE 3.5", "baidu", "百度", 8192),
            AiModel("ernie-speed", "ERNIE-Speed", "baidu", "百度", 8192),
            AiModel("ernie-image", "ERNIE Image", "baidu", "百度", 0, supportsCode = false, supportsImage = true, description = "文心一格"),
        ),
        "alibaba" to listOf(
            AiModel("qwen-max", "Qwen-max", "alibaba", "阿里巴巴", 32768, isBestPerformance = true),
            AiModel("qwen-plus", "Qwen-plus", "alibaba", "阿里巴巴", 32768),
            AiModel("qwen-turbo", "Qwen-turbo", "alibaba", "阿里巴巴", 8192),
            AiModel("qwen3-8b", "Qwen3-8B(Thinking)", "alibaba", "阿里巴巴", 32768, description = "推理增强"),
            AiModel("qwen-image-2", "Qwen-Image-2.0-Pro", "alibaba", "阿里巴巴", 0, supportsCode = false, supportsImage = true, description = "通义万相"),
        ),
        "deepseek" to listOf(
            AiModel("deepseek-v4-pro", "DeepSeek-V4-Pro", "deepseek", "深度求索", 1000000, isLatest = true, isBestPerformance = true, description = "1.6T MoE，百万上下文"),
            AiModel("deepseek-v4-flash", "DeepSeek-V4-Flash", "deepseek", "深度求索", 1000000, isLatest = true, description = "轻量化版本"),
            AiModel("deepseek-v3", "DeepSeek-V3", "deepseek", "深度求索", 65536),
            AiModel("deepseek-r1", "DeepSeek-R1", "deepseek", "深度求索", 65536, description = "推理模型"),
        ),
        "bytedance" to listOf(
            AiModel("doubao-thinking-pro", "Doubao-1.5-thinking-pro", "bytedance", "字节跳动", 32768, isLatest = true),
            AiModel("doubao-pro", "Doubao-pro", "bytedance", "字节跳动", 32768, isBestPerformance = true),
            AiModel("doubao-lite", "Doubao-lite", "bytedance", "字节跳动", 8192),
            AiModel("doubao-speed", "Doubao-Speed", "bytedance", "字节跳动", 8192),
            AiModel("doubao-seedream", "Doubao-Seedream 5.0", "bytedance", "字节跳动", 0, supportsCode = false, supportsImage = true, description = "4K图像生成"),
        ),
        "tencent" to listOf(
            AiModel("hunyuan-turbo", "Hunyuan-Turbo", "tencent", "腾讯", 32768, isLatest = true),
            AiModel("hunyuan-pro", "Hunyuan-Pro", "tencent", "腾讯", 32768, isBestPerformance = true),
            AiModel("hunyuan-lite", "Hunyuan-Lite", "tencent", "腾讯", 8192),
            AiModel("hunyuan-vision", "Hunyuan-Vision", "tencent", "腾讯", 8192),
            AiModel("hunyuan-image", "Hunyuan Image 3.0", "tencent", "腾讯", 0, supportsCode = false, supportsImage = true),
        ),
        "zhipu" to listOf(
            AiModel("glm-4-plus", "GLM-4-Plus", "zhipu", "智谱AI", 128000, isBestPerformance = true),
            AiModel("glm-4", "GLM-4", "zhipu", "智谱AI", 128000),
            AiModel("glm-3-turbo", "GLM-3-Turbo", "zhipu", "智谱AI", 32768),
            AiModel("glm-4v", "GLM-4V", "zhipu", "智谱AI", 8192),
            AiModel("cogview-4", "CogView-4", "zhipu", "智谱AI", 0, supportsCode = false, supportsImage = true, isLatest = true, description = "可生成汉字"),
            AiModel("cogview-3-plus", "CogView-3-Plus", "zhipu", "智谱AI", 0, supportsCode = false, supportsImage = true),
            AiModel("cogview-3-flash", "CogView-3-Flash", "zhipu", "智谱AI", 0, supportsCode = false, supportsImage = true),
        ),
        "iflytek" to listOf(
            AiModel("spark-max", "Spark Max", "iflytek", "科大讯飞", 32768, isBestPerformance = true),
            AiModel("spark-pro", "Spark Pro", "iflytek", "科大讯飞", 16384),
            AiModel("spark-lite", "Spark Lite", "iflytek", "科大讯飞", 8192),
            AiModel("spark-v3.5", "Spark V3.5", "iflytek", "科大讯飞", 8192),
        ),
        "sensetime" to listOf(
            AiModel("sensenova-v6", "SenseNovaV6Reasoner", "sensetime", "商汤", 32768, isLatest = true),
            AiModel("sensenova-v5", "SenseNovaV5", "sensetime", "商汤", 32768, isBestPerformance = true),
            AiModel("sensenova-v4", "SenseNovaV4", "sensetime", "商汤", 16384),
            AiModel("sensechat", "SenseChat", "sensetime", "商汤", 8192),
        ),
        "360" to listOf(
            AiModel("360zhinao2", "360zhinao2-0.1.5", "360", "360", 32768, isLatest = true),
            AiModel("360gpt-pro", "360GPT-Pro", "360", "360", 32768, isBestPerformance = true),
            AiModel("360gpt-s2", "360GPT-S2", "360", "360", 16384),
            AiModel("360gpt-s1", "360GPT-S1", "360", "360", 8192),
        ),
        "kunlun" to listOf(
            AiModel("skywork-13b", "Skywork-13B", "kunlun", "昆仑万维", 8192, isBestPerformance = true),
            AiModel("skywork-7b", "Skywork-7B", "kunlun", "昆仑万维", 8192),
            AiModel("skywork-moe", "Skywork-MoE", "kunlun", "昆仑万维", 8192),
            AiModel("skywork-4b", "Skywork-4B", "kunlun", "昆仑万维", 4096),
        ),
        "moonshot" to listOf(
            AiModel("moonshot-v1-128k", "Moonshot-V1-128K", "moonshot", "月之暗面", 128000, isLatest = true, isBestPerformance = true),
            AiModel("moonshot-v1-32k", "Moonshot-V1-32K", "moonshot", "月之暗面", 32768),
            AiModel("moonshot-v1-8k", "Moonshot-V1-8K", "moonshot", "月之暗面", 8192),
            AiModel("moonshot-v1-4k", "Moonshot-V1-4K", "moonshot", "月之暗面", 4096),
        ),
        "baichuan" to listOf(
            AiModel("baichuan4", "Baichuan4", "baichuan", "百川智能", 32768, isLatest = true, isBestPerformance = true),
            AiModel("baichuan3", "Baichuan3", "baichuan", "百川智能", 32768),
            AiModel("baichuan2-13b", "Baichuan2-13B", "baichuan", "百川智能", 8192),
            AiModel("baichuan2-7b", "Baichuan2-7B", "baichuan", "百川智能", 4096),
        ),
        "yi" to listOf(
            AiModel("yi-34b", "Yi-34B", "yi", "零一万物", 16384, isBestPerformance = true),
            AiModel("yi-9b", "Yi-9B", "yi", "零一万物", 8192),
            AiModel("yi-6b", "Yi-6B", "yi", "零一万物", 4096),
            AiModel("yi-vl", "Yi-VL", "yi", "零一万物", 8192),
        ),
        "nebula" to listOf(
            AiModel("nebulacoder-v6", "NebulaCoder-V6", "nebula", "智云", 65536, isLatest = true),
            AiModel("nebulacoder-v5", "NebulaCoder-V5", "nebula", "智云", 32768, isBestPerformance = true),
            AiModel("nebulacoder-v4", "NebulaCoder-V4", "nebula", "智云", 16384),
            AiModel("nebulacoder-v3", "NebulaCoder-V3", "nebula", "智云", 8192),
        ),
        "cas" to listOf(
            AiModel("taichu-3", "太初3.0", "cas", "中科院", 32768, isLatest = true, isBestPerformance = true),
            AiModel("taichu-2", "太初2.0", "cas", "中科院", 16384),
            AiModel("taichu-1.5", "太初1.5", "cas", "中科院", 8192),
            AiModel("taichu-1", "太初1.0", "cas", "中科院", 4096),
        ),
        "openai" to listOf(
            AiModel("gpt-4o", "GPT-4o", "openai", "OpenAI", 128000, isLatest = true, isBestPerformance = true),
            AiModel("gpt-4-turbo", "GPT-4-Turbo", "openai", "OpenAI", 128000),
            AiModel("gpt-3.5-turbo", "GPT-3.5-Turbo", "openai", "OpenAI", 16384),
            AiModel("gpt-4o-mini", "GPT-4o-mini", "openai", "OpenAI", 128000),
            AiModel("dalle-3", "DALL-E 3", "openai", "OpenAI", 0, supportsCode = false, supportsImage = true),
            AiModel("gpt-image-2", "GPT-image-2", "openai", "OpenAI", 0, supportsCode = false, supportsImage = true),
        ),
        "anthropic" to listOf(
            AiModel("claude-3.5-sonnet", "Claude 3.5 Sonnet", "anthropic", "Anthropic", 200000, isLatest = true, isBestPerformance = true),
            AiModel("claude-3-opus", "Claude 3 Opus", "anthropic", "Anthropic", 200000),
            AiModel("claude-3-sonnet", "Claude 3 Sonnet", "anthropic", "Anthropic", 200000),
            AiModel("claude-3-haiku", "Claude 3 Haiku", "anthropic", "Anthropic", 200000),
        ),
        "google" to listOf(
            AiModel("gemini-2.0-pro", "Gemini 2.0 Pro", "google", "Google", 1000000, isLatest = true, isBestPerformance = true),
            AiModel("gemini-1.5-pro", "Gemini 1.5 Pro", "google", "Google", 1000000),
            AiModel("gemini-1.5-flash", "Gemini 1.5 Flash", "google", "Google", 1000000),
            AiModel("gemini-1.0-pro", "Gemini 1.0 Pro", "google", "Google", 32768),
            AiModel("imagen-4", "Imagen 4.0 Ultra", "google", "Google", 0, supportsCode = false, supportsImage = true),
        ),
        "meta" to listOf(
            AiModel("llama-3.1-405b", "Llama 3.1-405B", "meta", "Meta", 128000, isLatest = true, isBestPerformance = true),
            AiModel("llama-3.1-70b", "Llama 3.1-70B", "meta", "Meta", 128000),
            AiModel("llama-3.1-8b", "Llama 3.1-8B", "meta", "Meta", 128000),
            AiModel("llama-3-70b", "Llama 3-70B", "meta", "Meta", 8192),
        ),
        "mistral" to listOf(
            AiModel("mistral-large-2", "Mistral Large 2", "mistral", "Mistral AI", 128000, isLatest = true, isBestPerformance = true),
            AiModel("mistral-medium", "Mistral Medium", "mistral", "Mistral AI", 32768),
            AiModel("mistral-small", "Mistral Small", "mistral", "Mistral AI", 32768),
            AiModel("mistral-embed", "Mistral Embed", "mistral", "Mistral AI", 8192),
        ),
    )

    fun getAllModels(): List<AiModel> = modelsByVendor.values.flatten()
    fun getTextModels() = getAllModels().filter { it.supportsCode && !it.supportsImage }
    fun getCodeModels() = getAllModels().filter { it.supportsCode }
    fun getImageModels() = getAllModels().filter { it.supportsImage }
    fun getVendorById(id: String) = vendors.find { it.id == id }
    fun getModelById(id: String): AiModel? {
        for (models in modelsByVendor.values) {
            models.find { it.id == id }?.let { return it }
        }
        return null
    }
}