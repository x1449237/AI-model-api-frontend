import '../models/vendor.dart';
import '../models/ai_model.dart';

class VendorsConfig {
  static final List<Vendor> vendors = [
    // ===== 国内厂商 (15) =====
    const Vendor(
      id: 'baidu', name: '百度', nameEn: 'Baidu', logo: 'baidu',
      category: 'domestic', baseUrl: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat',
    ),
    const Vendor(
      id: 'alibaba', name: '阿里巴巴', nameEn: 'Alibaba', logo: 'alibaba',
      category: 'domestic', baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
    ),
    const Vendor(
      id: 'deepseek', name: '深度求索', nameEn: 'DeepSeek', logo: 'deepseek',
      category: 'domestic', baseUrl: 'https://api.deepseek.com/v1/chat/completions',
    ),
    const Vendor(
      id: 'bytedance', name: '字节跳动', nameEn: 'ByteDance', logo: 'bytedance',
      category: 'domestic', baseUrl: 'https://ark.cn-beijing.volces.com/api/v3/chat/completions',
    ),
    const Vendor(
      id: 'tencent', name: '腾讯', nameEn: 'Tencent', logo: 'tencent',
      category: 'domestic', baseUrl: 'https://hunyuan.tencentcloudapi.com',
    ),
    const Vendor(
      id: 'zhipu', name: '智谱AI', nameEn: 'Zhipu AI', logo: 'zhipu',
      category: 'domestic', baseUrl: 'https://open.bigmodel.cn/api/paas/v4/chat/completions',
    ),
    const Vendor(
      id: 'iflytek', name: '科大讯飞', nameEn: 'iFlytek', logo: 'iflytek',
      category: 'domestic', baseUrl: 'https://spark-api-open.xf-yun.com/v1/chat/completions',
    ),
    const Vendor(
      id: 'sensetime', name: '商汤', nameEn: 'SenseTime', logo: 'sensetime',
      category: 'domestic', baseUrl: 'https://api.sensenova.cn/v1/chat/completions',
    ),
    const Vendor(
      id: '360', name: '360', nameEn: '360 AI', logo: '360',
      category: 'domestic', baseUrl: 'https://api.360.cn/v1/chat/completions',
    ),
    const Vendor(
      id: 'kunlun', name: '昆仑万维', nameEn: 'Kunlun', logo: 'kunlun',
      category: 'domestic', baseUrl: 'https://api.skywork.cn/v1/chat/completions',
    ),
    const Vendor(
      id: 'moonshot', name: '月之暗面', nameEn: 'Moonshot AI', logo: 'moonshot',
      category: 'domestic', baseUrl: 'https://api.moonshot.cn/v1/chat/completions',
    ),
    const Vendor(
      id: 'baichuan', name: '百川智能', nameEn: 'Baichuan', logo: 'baichuan',
      category: 'domestic', baseUrl: 'https://api.baichuan-ai.com/v1/chat/completions',
    ),
    const Vendor(
      id: 'yi', name: '零一万物', nameEn: '01.AI', logo: 'yi',
      category: 'domestic', baseUrl: 'https://api.lingyiwanwu.com/v1/chat/completions',
    ),
    const Vendor(
      id: 'nebula', name: '智云', nameEn: 'Nebula', logo: 'nebula',
      category: 'domestic', baseUrl: 'https://api.nebula-ai.com/v1/chat/completions',
    ),
    const Vendor(
      id: 'cas', name: '中科院', nameEn: 'CAS', logo: 'cas',
      category: 'domestic', baseUrl: 'https://api.taichu.cas.cn/v1/chat/completions',
    ),
    // ===== 国外厂商 (5) =====
    const Vendor(
      id: 'openai', name: 'OpenAI', nameEn: 'OpenAI', logo: 'openai',
      category: 'international', baseUrl: 'https://api.openai.com/v1/chat/completions',
    ),
    const Vendor(
      id: 'anthropic', name: 'Anthropic', nameEn: 'Anthropic', logo: 'anthropic',
      category: 'international', baseUrl: 'https://api.anthropic.com/v1/messages',
    ),
    const Vendor(
      id: 'google', name: 'Google', nameEn: 'Google', logo: 'google',
      category: 'international', baseUrl: 'https://generativelanguage.googleapis.com/v1beta/models',
    ),
    const Vendor(
      id: 'meta', name: 'Meta', nameEn: 'Meta', logo: 'meta',
      category: 'international', baseUrl: 'https://api.meta.ai/v1/chat/completions',
    ),
    const Vendor(
      id: 'mistral', name: 'Mistral AI', nameEn: 'Mistral AI', logo: 'mistral',
      category: 'international', baseUrl: 'https://api.mistral.ai/v1/chat/completions',
    ),
  ];

  static final Map<String, List<AiModel>> modelsByVendor = {
    // 1. 百度
    'baidu': [
      const AiModel(id: 'ernie-4.5-turbo', name: 'ERNIE 4.5 Turbo', vendorId: 'baidu', vendorName: '百度', contextLength: 128000, isLatest: true, isBestPerformance: true, description: '文心最新旗舰模型，128K上下文'),
      const AiModel(id: 'ernie-4.0-turbo', name: 'ERNIE 4.0 Turbo', vendorId: 'baidu', vendorName: '百度', contextLength: 128000),
      const AiModel(id: 'ernie-3.5', name: 'ERNIE 3.5', vendorId: 'baidu', vendorName: '百度', contextLength: 8192),
      const AiModel(id: 'ernie-speed', name: 'ERNIE-Speed', vendorId: 'baidu', vendorName: '百度', contextLength: 8192),
      const AiModel(id: 'ernie-image', name: 'ERNIE Image', vendorId: 'baidu', vendorName: '百度', contextLength: 0, supportsCode: false, supportsImage: true, description: '文心一格，图像生成模型'),
    ],
    // 2. 阿里巴巴
    'alibaba': [
      const AiModel(id: 'qwen-max', name: 'Qwen-max', vendorId: 'alibaba', vendorName: '阿里巴巴', contextLength: 32768, isBestPerformance: true),
      const AiModel(id: 'qwen-plus', name: 'Qwen-plus', vendorId: 'alibaba', vendorName: '阿里巴巴', contextLength: 32768),
      const AiModel(id: 'qwen-turbo', name: 'Qwen-turbo', vendorId: 'alibaba', vendorName: '阿里巴巴', contextLength: 8192),
      const AiModel(id: 'qwen3-8b', name: 'Qwen3-8B(Thinking)', vendorId: 'alibaba', vendorName: '阿里巴巴', contextLength: 32768, description: '推理增强模型'),
      const AiModel(id: 'qwen-image-2', name: 'Qwen-Image-2.0-Pro', vendorId: 'alibaba', vendorName: '阿里巴巴', contextLength: 0, supportsCode: false, supportsImage: true, description: '通义万相，图像生成'),
    ],
    // 3. DeepSeek
    'deepseek': [
      const AiModel(id: 'deepseek-v4-pro', name: 'DeepSeek-V4-Pro', vendorId: 'deepseek', vendorName: '深度求索', contextLength: 1000000, isLatest: true, isBestPerformance: true, description: '1.6T MoE，百万上下文'),
      const AiModel(id: 'deepseek-v4-flash', name: 'DeepSeek-V4-Flash', vendorId: 'deepseek', vendorName: '深度求索', contextLength: 1000000, isLatest: true, description: '轻量化版本'),
      const AiModel(id: 'deepseek-v3', name: 'DeepSeek-V3', vendorId: 'deepseek', vendorName: '深度求索', contextLength: 65536),
      const AiModel(id: 'deepseek-r1', name: 'DeepSeek-R1', vendorId: 'deepseek', vendorName: '深度求索', contextLength: 65536, description: '推理模型'),
    ],
    // 4. 字节跳动
    'bytedance': [
      const AiModel(id: 'doubao-thinking-pro', name: 'Doubao-1.5-thinking-pro', vendorId: 'bytedance', vendorName: '字节跳动', contextLength: 32768, isLatest: true),
      const AiModel(id: 'doubao-pro', name: 'Doubao-pro', vendorId: 'bytedance', vendorName: '字节跳动', contextLength: 32768, isBestPerformance: true),
      const AiModel(id: 'doubao-lite', name: 'Doubao-lite', vendorId: 'bytedance', vendorName: '字节跳动', contextLength: 8192),
      const AiModel(id: 'doubao-speed', name: 'Doubao-Speed', vendorId: 'bytedance', vendorName: '字节跳动', contextLength: 8192),
      const AiModel(id: 'doubao-seedream', name: 'Doubao-Seedream 5.0', vendorId: 'bytedance', vendorName: '字节跳动', contextLength: 0, supportsCode: false, supportsImage: true, description: '4K图像生成'),
    ],
    // 5. 腾讯
    'tencent': [
      const AiModel(id: 'hunyuan-turbo', name: 'Hunyuan-Turbo', vendorId: 'tencent', vendorName: '腾讯', contextLength: 32768, isLatest: true),
      const AiModel(id: 'hunyuan-pro', name: 'Hunyuan-Pro', vendorId: 'tencent', vendorName: '腾讯', contextLength: 32768, isBestPerformance: true),
      const AiModel(id: 'hunyuan-lite', name: 'Hunyuan-Lite', vendorId: 'tencent', vendorName: '腾讯', contextLength: 8192),
      const AiModel(id: 'hunyuan-vision', name: 'Hunyuan-Vision', vendorId: 'tencent', vendorName: '腾讯', contextLength: 8192),
      const AiModel(id: 'hunyuan-image', name: 'Hunyuan Image 3.0', vendorId: 'tencent', vendorName: '腾讯', contextLength: 0, supportsCode: false, supportsImage: true),
    ],
    // 6. 智谱AI
    'zhipu': [
      const AiModel(id: 'glm-4-plus', name: 'GLM-4-Plus', vendorId: 'zhipu', vendorName: '智谱AI', contextLength: 128000, isBestPerformance: true),
      const AiModel(id: 'glm-4', name: 'GLM-4', vendorId: 'zhipu', vendorName: '智谱AI', contextLength: 128000),
      const AiModel(id: 'glm-3-turbo', name: 'GLM-3-Turbo', vendorId: 'zhipu', vendorName: '智谱AI', contextLength: 32768),
      const AiModel(id: 'glm-4v', name: 'GLM-4V', vendorId: 'zhipu', vendorName: '智谱AI', contextLength: 8192),
      const AiModel(id: 'cogview-4', name: 'CogView-4', vendorId: 'zhipu', vendorName: '智谱AI', contextLength: 0, supportsCode: false, supportsImage: true, isLatest: true, description: '可生成汉字'),
      const AiModel(id: 'cogview-3-plus', name: 'CogView-3-Plus', vendorId: 'zhipu', vendorName: '智谱AI', contextLength: 0, supportsCode: false, supportsImage: true),
      const AiModel(id: 'cogview-3-flash', name: 'CogView-3-Flash', vendorId: 'zhipu', vendorName: '智谱AI', contextLength: 0, supportsCode: false, supportsImage: true),
    ],
    // 7. 科大讯飞
    'iflytek': [
      const AiModel(id: 'spark-max', name: 'Spark Max', vendorId: 'iflytek', vendorName: '科大讯飞', contextLength: 32768, isBestPerformance: true),
      const AiModel(id: 'spark-pro', name: 'Spark Pro', vendorId: 'iflytek', vendorName: '科大讯飞', contextLength: 16384),
      const AiModel(id: 'spark-lite', name: 'Spark Lite', vendorId: 'iflytek', vendorName: '科大讯飞', contextLength: 8192),
      const AiModel(id: 'spark-v3.5', name: 'Spark V3.5', vendorId: 'iflytek', vendorName: '科大讯飞', contextLength: 8192),
    ],
    // 8. 商汤
    'sensetime': [
      const AiModel(id: 'sensenova-v6', name: 'SenseNovaV6Reasoner', vendorId: 'sensetime', vendorName: '商汤', contextLength: 32768, isLatest: true),
      const AiModel(id: 'sensenova-v5', name: 'SenseNovaV5', vendorId: 'sensetime', vendorName: '商汤', contextLength: 32768, isBestPerformance: true),
      const AiModel(id: 'sensenova-v4', name: 'SenseNovaV4', vendorId: 'sensetime', vendorName: '商汤', contextLength: 16384),
      const AiModel(id: 'sensechat', name: 'SenseChat', vendorId: 'sensetime', vendorName: '商汤', contextLength: 8192),
    ],
    // 9. 360
    '360': [
      const AiModel(id: '360zhinao2', name: '360zhinao2-0.1.5', vendorId: '360', vendorName: '360', contextLength: 32768, isLatest: true),
      const AiModel(id: '360gpt-pro', name: '360GPT-Pro', vendorId: '360', vendorName: '360', contextLength: 32768, isBestPerformance: true),
      const AiModel(id: '360gpt-s2', name: '360GPT-S2', vendorId: '360', vendorName: '360', contextLength: 16384),
      const AiModel(id: '360gpt-s1', name: '360GPT-S1', vendorId: '360', vendorName: '360', contextLength: 8192),
    ],
    // 10. 昆仑万维
    'kunlun': [
      const AiModel(id: 'skywork-13b', name: 'Skywork-13B', vendorId: 'kunlun', vendorName: '昆仑万维', contextLength: 8192, isBestPerformance: true),
      const AiModel(id: 'skywork-7b', name: 'Skywork-7B', vendorId: 'kunlun', vendorName: '昆仑万维', contextLength: 8192),
      const AiModel(id: 'skywork-moe', name: 'Skywork-MoE', vendorId: 'kunlun', vendorName: '昆仑万维', contextLength: 8192),
      const AiModel(id: 'skywork-4b', name: 'Skywork-4B', vendorId: 'kunlun', vendorName: '昆仑万维', contextLength: 4096),
    ],
    // 11. 月之暗面
    'moonshot': [
      const AiModel(id: 'moonshot-v1-128k', name: 'Moonshot-V1-128K', vendorId: 'moonshot', vendorName: '月之暗面', contextLength: 128000, isLatest: true, isBestPerformance: true),
      const AiModel(id: 'moonshot-v1-32k', name: 'Moonshot-V1-32K', vendorId: 'moonshot', vendorName: '月之暗面', contextLength: 32768),
      const AiModel(id: 'moonshot-v1-8k', name: 'Moonshot-V1-8K', vendorId: 'moonshot', vendorName: '月之暗面', contextLength: 8192),
      const AiModel(id: 'moonshot-v1-4k', name: 'Moonshot-V1-4K', vendorId: 'moonshot', vendorName: '月之暗面', contextLength: 4096),
    ],
    // 12. 百川智能
    'baichuan': [
      const AiModel(id: 'baichuan4', name: 'Baichuan4', vendorId: 'baichuan', vendorName: '百川智能', contextLength: 32768, isLatest: true, isBestPerformance: true),
      const AiModel(id: 'baichuan3', name: 'Baichuan3', vendorId: 'baichuan', vendorName: '百川智能', contextLength: 32768),
      const AiModel(id: 'baichuan2-13b', name: 'Baichuan2-13B', vendorId: 'baichuan', vendorName: '百川智能', contextLength: 8192),
      const AiModel(id: 'baichuan2-7b', name: 'Baichuan2-7B', vendorId: 'baichuan', vendorName: '百川智能', contextLength: 4096),
    ],
    // 13. 零一万物
    'yi': [
      const AiModel(id: 'yi-34b', name: 'Yi-34B', vendorId: 'yi', vendorName: '零一万物', contextLength: 16384, isBestPerformance: true),
      const AiModel(id: 'yi-9b', name: 'Yi-9B', vendorId: 'yi', vendorName: '零一万物', contextLength: 8192),
      const AiModel(id: 'yi-6b', name: 'Yi-6B', vendorId: 'yi', vendorName: '零一万物', contextLength: 4096),
      const AiModel(id: 'yi-vl', name: 'Yi-VL', vendorId: 'yi', vendorName: '零一万物', contextLength: 8192),
    ],
    // 14. 智云
    'nebula': [
      const AiModel(id: 'nebulacoder-v6', name: 'NebulaCoder-V6', vendorId: 'nebula', vendorName: '智云', contextLength: 65536, isLatest: true),
      const AiModel(id: 'nebulacoder-v5', name: 'NebulaCoder-V5', vendorId: 'nebula', vendorName: '智云', contextLength: 32768, isBestPerformance: true),
      const AiModel(id: 'nebulacoder-v4', name: 'NebulaCoder-V4', vendorId: 'nebula', vendorName: '智云', contextLength: 16384),
      const AiModel(id: 'nebulacoder-v3', name: 'NebulaCoder-V3', vendorId: 'nebula', vendorName: '智云', contextLength: 8192),
    ],
    // 15. 中科院
    'cas': [
      const AiModel(id: 'taichu-3', name: '太初3.0', vendorId: 'cas', vendorName: '中科院', contextLength: 32768, isLatest: true, isBestPerformance: true),
      const AiModel(id: 'taichu-2', name: '太初2.0', vendorId: 'cas', vendorName: '中科院', contextLength: 16384),
      const AiModel(id: 'taichu-1.5', name: '太初1.5', vendorId: 'cas', vendorName: '中科院', contextLength: 8192),
      const AiModel(id: 'taichu-1', name: '太初1.0', vendorId: 'cas', vendorName: '中科院', contextLength: 4096),
    ],
    // 16. OpenAI
    'openai': [
      const AiModel(id: 'gpt-4o', name: 'GPT-4o', vendorId: 'openai', vendorName: 'OpenAI', contextLength: 128000, isLatest: true, isBestPerformance: true),
      const AiModel(id: 'gpt-4-turbo', name: 'GPT-4-Turbo', vendorId: 'openai', vendorName: 'OpenAI', contextLength: 128000),
      const AiModel(id: 'gpt-3.5-turbo', name: 'GPT-3.5-Turbo', vendorId: 'openai', vendorName: 'OpenAI', contextLength: 16384),
      const AiModel(id: 'gpt-4o-mini', name: 'GPT-4o-mini', vendorId: 'openai', vendorName: 'OpenAI', contextLength: 128000),
      const AiModel(id: 'dalle-3', name: 'DALL-E 3', vendorId: 'openai', vendorName: 'OpenAI', contextLength: 0, supportsCode: false, supportsImage: true),
      const AiModel(id: 'gpt-image-2', name: 'GPT-image-2', vendorId: 'openai', vendorName: 'OpenAI', contextLength: 0, supportsCode: false, supportsImage: true),
    ],
    // 17. Anthropic
    'anthropic': [
      const AiModel(id: 'claude-3.5-sonnet', name: 'Claude 3.5 Sonnet', vendorId: 'anthropic', vendorName: 'Anthropic', contextLength: 200000, isLatest: true, isBestPerformance: true),
      const AiModel(id: 'claude-3-opus', name: 'Claude 3 Opus', vendorId: 'anthropic', vendorName: 'Anthropic', contextLength: 200000),
      const AiModel(id: 'claude-3-sonnet', name: 'Claude 3 Sonnet', vendorId: 'anthropic', vendorName: 'Anthropic', contextLength: 200000),
      const AiModel(id: 'claude-3-haiku', name: 'Claude 3 Haiku', vendorId: 'anthropic', vendorName: 'Anthropic', contextLength: 200000),
    ],
    // 18. Google
    'google': [
      const AiModel(id: 'gemini-2.0-pro', name: 'Gemini 2.0 Pro', vendorId: 'google', vendorName: 'Google', contextLength: 1000000, isLatest: true, isBestPerformance: true),
      const AiModel(id: 'gemini-1.5-pro', name: 'Gemini 1.5 Pro', vendorId: 'google', vendorName: 'Google', contextLength: 1000000),
      const AiModel(id: 'gemini-1.5-flash', name: 'Gemini 1.5 Flash', vendorId: 'google', vendorName: 'Google', contextLength: 1000000),
      const AiModel(id: 'gemini-1.0-pro', name: 'Gemini 1.0 Pro', vendorId: 'google', vendorName: 'Google', contextLength: 32768),
      const AiModel(id: 'imagen-4', name: 'Imagen 4.0 Ultra', vendorId: 'google', vendorName: 'Google', contextLength: 0, supportsCode: false, supportsImage: true, description: 'Google图像生成'),
    ],
    // 19. Meta
    'meta': [
      const AiModel(id: 'llama-3.1-405b', name: 'Llama 3.1-405B', vendorId: 'meta', vendorName: 'Meta', contextLength: 128000, isLatest: true, isBestPerformance: true),
      const AiModel(id: 'llama-3.1-70b', name: 'Llama 3.1-70B', vendorId: 'meta', vendorName: 'Meta', contextLength: 128000),
      const AiModel(id: 'llama-3.1-8b', name: 'Llama 3.1-8B', vendorId: 'meta', vendorName: 'Meta', contextLength: 128000),
      const AiModel(id: 'llama-3-70b', name: 'Llama 3-70B', vendorId: 'meta', vendorName: 'Meta', contextLength: 8192),
    ],
    // 20. Mistral AI
    'mistral': [
      const AiModel(id: 'mistral-large-2', name: 'Mistral Large 2', vendorId: 'mistral', vendorName: 'Mistral AI', contextLength: 128000, isLatest: true, isBestPerformance: true),
      const AiModel(id: 'mistral-medium', name: 'Mistral Medium', vendorId: 'mistral', vendorName: 'Mistral AI', contextLength: 32768),
      const AiModel(id: 'mistral-small', name: 'Mistral Small', vendorId: 'mistral', vendorName: 'Mistral AI', contextLength: 32768),
      const AiModel(id: 'mistral-embed', name: 'Mistral Embed', vendorId: 'mistral', vendorName: 'Mistral AI', contextLength: 8192),
    ],
  };

  static List<AiModel> getAllModels() {
    final allModels = <AiModel>[];
    for (final models in modelsByVendor.values) {
      allModels.addAll(models);
    }
    return allModels;
  }

  static List<AiModel> getTextModels() {
    return getAllModels().where((m) => m.supportsCode && !m.supportsImage).toList();
  }

  static List<AiModel> getCodeModels() {
    return getAllModels().where((m) => m.supportsCode).toList();
  }

  static List<AiModel> getImageModels() {
    return getAllModels().where((m) => m.supportsImage).toList();
  }

  static Vendor? getVendorById(String id) {
    try {
      return vendors.firstWhere((v) => v.id == id);
    } catch (_) {
      return null;
    }
  }

  static AiModel? getModelById(String modelId) {
    for (final models in modelsByVendor.values) {
      try {
        return models.firstWhere((m) => m.id == modelId);
      } catch (_) {}
    }
    return null;
  }
}