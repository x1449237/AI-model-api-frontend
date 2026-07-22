import 'dart:async';
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/ai_model.dart';
import 'vendors_config.dart';

class ApiService extends ChangeNotifier {
  final Map<String, String> _apiKeys = {};
  bool _isLoading = false;

  bool get isLoading => _isLoading;

  Future<void> loadApiKeys() async {
    final prefs = await SharedPreferences.getInstance();
    for (final vendor in VendorsConfig.vendors) {
      final key = prefs.getString('api_key_${vendor.id}') ?? '';
      if (key.isNotEmpty) {
        _apiKeys[vendor.id] = key;
      }
    }
    notifyListeners();
  }

  Future<void> setApiKey(String vendorId, String key) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('api_key_$vendorId', key);
    _apiKeys[vendorId] = key;
    notifyListeners();
  }

  String? getApiKey(String vendorId) => _apiKeys[vendorId];

  Future<String> sendMessage({
    required AiModel model,
    required String prompt,
    required double temperature,
    required double topP,
    required int maxTokens,
    StreamController<String>? streamController,
  }) async {
    _isLoading = true;
    notifyListeners();

    final apiKey = _apiKeys[model.vendorId];
    final vendor = VendorsConfig.getVendorById(model.vendorId);

    try {
      final stopwatch = Stopwatch()..start();

      final response = await _callApi(
        vendor: vendor,
        model: model,
        prompt: prompt,
        temperature: temperature,
        topP: topP,
        maxTokens: maxTokens,
        apiKey: apiKey,
        streamController: streamController,
      );

      stopwatch.stop();
      _isLoading = false;
      notifyListeners();

      return response;
    } catch (e) {
      _isLoading = false;
      notifyListeners();
      return '请求失败: ${e.toString()}\n\n请在"我的"页面配置API Key后重试。';
    }
  }

  Future<String> _callApi({
    required dynamic vendor,
    required AiModel model,
    required String prompt,
    required double temperature,
    required double topP,
    required int maxTokens,
    required String? apiKey,
    StreamController<String>? streamController,
  }) async {
    final baseUrl = vendor?.baseUrl ?? 'https://api.openai.com/v1/chat/completions';
    final vendorId = model.vendorId;

    // Build unified request body
    final messages = [
      {'role': 'user', 'content': prompt},
    ];

    final body = {
      'model': model.id,
      'messages': messages,
      'temperature': temperature,
      'top_p': topP,
      'max_tokens': maxTokens,
      'stream': streamController != null,
    };

    final headers = <String, String>{
      'Content-Type': 'application/json',
    };

    // Add auth header based on vendor
    if (vendorId == 'openai') {
      headers['Authorization'] = 'Bearer ${apiKey ?? ''}';
    } else if (vendorId == 'anthropic') {
      headers['x-api-key'] = apiKey ?? '';
      headers['anthropic-version'] = '2023-06-01';
    } else if (vendorId == 'google') {
      // Google uses different URL pattern
    } else {
      headers['Authorization'] = 'Bearer ${apiKey ?? ''}';
    }

    final uri = Uri.parse(_getApiUrl(vendorId, baseUrl, model));

    if (streamController != null) {
      return await _streamRequest(uri, headers, body, streamController);
    } else {
      final response = await http.post(
        uri,
        headers: headers,
        body: jsonEncode(body),
      ).timeout(const Duration(seconds: 30));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return _extractContent(data, vendorId);
      } else {
        return 'HTTP ${response.statusCode}: ${response.body}';
      }
    }
  }

  String _getApiUrl(String vendorId, String baseUrl, AiModel model) {
    switch (vendorId) {
      case 'google':
        return 'https://generativelanguage.googleapis.com/v1beta/models/${model.id}:generateContent';
      default:
        return baseUrl;
    }
  }

  Future<String> _streamRequest(
    Uri uri,
    Map<String, String> headers,
    Map<String, dynamic> body,
    StreamController<String> controller,
  ) async {
    try {
      final request = http.Request('POST', uri);
      request.headers.addAll(headers);
      request.body = jsonEncode(body);

      final client = http.Client();
      final streamedResponse = await client.send(request).timeout(
        const Duration(seconds: 60),
      );

      final buffer = StringBuffer();
      await for (final chunk in streamedResponse.stream.transform(utf8.decoder)) {
        final lines = chunk.split('\n');
        for (final line in lines) {
          if (line.startsWith('data: ') && line.length > 6) {
            final data = line.substring(6);
            if (data == '[DONE]') continue;
            try {
              final json = jsonDecode(data);
              final content = json['choices']?[0]?['delta']?['content'] ?? '';
              if (content.isNotEmpty) {
                buffer.write(content);
                controller.add(content);
              }
            } catch (_) {}
          }
        }
      }
      client.close();
      return buffer.toString();
    } catch (e) {
      controller.addError(e);
      rethrow;
    }
  }

  String _extractContent(Map<String, dynamic> data, String vendorId) {
    switch (vendorId) {
      case 'anthropic':
        return data['content']?[0]?['text'] ?? '';
      case 'google':
        return data['candidates']?[0]?['content']?['parts']?[0]?['text'] ?? '';
      default:
        return data['choices']?[0]?['message']?['content'] ?? '';
    }
  }

  // Generate code
  Future<String> generateCode({
    required AiModel model,
    required String language,
    required String description,
    double temperature = 0.3,
    double topP = 0.9,
    int maxTokens = 4096,
    StreamController<String>? streamController,
  }) async {
    final prompt = '''你是一个专业的$language程序员。请根据以下需求生成代码：

需求：$description

要求：
1. 只输出代码，不要包含额外的解释文字
2. 代码要完整可运行
3. 添加必要的注释
4. 遵循$language的最佳实践

请生成代码：''';

    return sendMessage(
      model: model,
      prompt: prompt,
      temperature: temperature,
      topP: topP,
      maxTokens: maxTokens,
      streamController: streamController,
    );
  }

  // Generate image prompt
  Future<String> generateImage({
    required AiModel model,
    required String prompt,
    required String size,
    int numImages = 1,
  }) async {
    _isLoading = true;
    notifyListeners();

    try {
      final stopwatch = Stopwatch()..start();
      final result = await _callImageApi(model, prompt, size, numImages);
      stopwatch.stop();
      _isLoading = false;
      notifyListeners();
      return result;
    } catch (e) {
      _isLoading = false;
      notifyListeners();
      return '图片生成失败: ${e.toString()}';
    }
  }

  Future<String> _callImageApi(
    AiModel model,
    String prompt,
    String size,
    int numImages,
  ) async {
    // Simulate image generation - in production, call actual vendor APIs
    await Future.delayed(const Duration(seconds: 2));
    return 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${Uri.encodeComponent(prompt)}&image_size=square';
  }
}