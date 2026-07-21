import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:share_plus/share_plus.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:io';
import '../data/vendors_config.dart';
import '../data/api_service.dart';
import '../models/ai_model.dart';
import '../widgets/model_card.dart';

class CodeGenerationScreen extends StatefulWidget {
  const CodeGenerationScreen({super.key});

  @override
  State<CodeGenerationScreen> createState() => _CodeGenerationScreenState();
}

class _CodeGenerationScreenState extends State<CodeGenerationScreen> {
  AiModel? _selectedModel;
  String _selectedLanguage = 'Python';
  final _descController = TextEditingController();
  final _streamController = StreamController<String>();
  String _generatedCode = '';
  bool _isGenerating = false;
  bool _isStreaming = false;

  static const _languages = [
    'Python', 'Java', 'JavaScript', 'C++', 'C', 'Go', 'Rust', 'Swift',
    'Kotlin', 'PHP', 'SQL', 'HTML', 'CSS', 'Shell', 'JSON', 'XML', 'YAML',
  ];

  static const _languageExtensions = {
    'Python': '.py', 'Java': '.java', 'JavaScript': '.js', 'C++': '.cpp',
    'C': '.c', 'Go': '.go', 'Rust': '.rs', 'Swift': '.swift',
    'Kotlin': '.kt', 'PHP': '.php', 'SQL': '.sql', 'HTML': '.html',
    'CSS': '.css', 'Shell': '.sh', 'JSON': '.json', 'XML': '.xml', 'YAML': '.yaml',
  };

  @override
  void dispose() {
    _descController.dispose();
    _streamController.close();
    super.dispose();
  }

  Future<void> _generateCode() async {
    final desc = _descController.text.trim();
    if (desc.isEmpty || _selectedModel == null) return;

    setState(() {
      _isGenerating = true;
      _isStreaming = true;
      _generatedCode = '';
    });

    final streamCtrl = StreamController<String>();
    streamCtrl.stream.listen((chunk) {
      setState(() {
        _generatedCode += chunk;
      });
    });

    final result = await context.read<ApiService>().generateCode(
      model: _selectedModel!,
      language: _selectedLanguage,
      description: desc,
      streamController: streamCtrl,
    );

    await streamCtrl.close();

    setState(() {
      if (_generatedCode.isEmpty) _generatedCode = result;
      _isGenerating = false;
      _isStreaming = false;
    });
  }

  String _getFileName() {
    final ext = _languageExtensions[_selectedLanguage] ?? '.txt';
    final sanitized = _descController.text.length > 20
        ? '${_descController.text.substring(0, 20)}'
        : _descController.text;
    final safe = sanitized.replaceAll(RegExp(r'[^\w\u4e00-\u9fff]'), '_');
    return '${safe}_code$ext';
  }

  Future<void> _exportCode() async {
    if (_generatedCode.isEmpty) return;
    try {
      final dir = await getApplicationDocumentsDirectory();
      final file = File('${dir.path}/${_getFileName()}');
      await file.writeAsString(_generatedCode);
      await Share.shareXFiles([XFile(file.path)], text: '生成的代码');
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('导出失败: $e')),
        );
      }
    }
  }

  Future<void> _copyToClipboard() async {
    // Using clipboard
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('代码已复制到剪贴板')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('代码生成')),
      body: Column(
        children: [
          // Model selector
          _buildModelSelector(),
          // Language selector
          _buildLanguageSelector(),
          // Input area
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: TextField(
              controller: _descController,
              decoration: const InputDecoration(
                hintText: '描述你想要生成的代码...',
                prefixIcon: Icon(Icons.description_outlined),
              ),
              maxLines: 3,
              minLines: 2,
            ),
          ),
          // Generate button
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: _selectedModel != null && !_isGenerating ? _generateCode : null,
                icon: const Icon(Icons.auto_awesome),
                label: const Text('生成代码'),
              ),
            ),
          ),
          // Output area
          Expanded(
            child: _generatedCode.isNotEmpty
                ? Column(
                    children: [
                      // Action bar
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: [
                            TextButton.icon(
                              onPressed: _copyToClipboard,
                              icon: const Icon(Icons.copy, size: 18),
                              label: const Text('复制'),
                            ),
                            const SizedBox(width: 8),
                            TextButton.icon(
                              onPressed: _exportCode,
                              icon: const Icon(Icons.file_download_outlined, size: 18),
                              label: const Text('导出'),
                            ),
                          ],
                        ),
                      ),
                      // Code display
                      Expanded(
                        child: Container(
                          width: double.infinity,
                          margin: const EdgeInsets.all(16),
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: const Color(0xFF1E1E1E),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: SingleChildScrollView(
                            child: SelectableText(
                              _generatedCode,
                              style: const TextStyle(
                                fontFamily: 'monospace',
                                fontSize: 13,
                                color: Color(0xFFD4D4D4),
                                height: 1.5,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  )
                : Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.code, size: 64, color: Colors.grey.shade300),
                        const SizedBox(height: 16),
                        Text(
                          '选择模型和语言，输入需求描述\n点击生成代码',
                          textAlign: TextAlign.center,
                          style: TextStyle(color: Colors.grey.shade500, fontSize: 14),
                        ),
                      ],
                    ),
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildModelSelector() {
    final codeModels = VendorsConfig.getCodeModels();
    return Container(
      height: 50,
      color: Colors.white,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        children: VendorsConfig.vendors.map((vendor) {
          final models = VendorsConfig.modelsByVendor[vendor.id] ?? [];
          final cModels = models.where((m) => m.supportsCode).toList();
          if (cModels.isEmpty) return const SizedBox.shrink();
          return Padding(
            padding: const EdgeInsets.only(right: 8),
            child: PopupMenuButton<AiModel>(
              tooltip: vendor.name,
              offset: const Offset(0, 40),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: _selectedModel?.vendorId == vendor.id
                      ? Theme.of(context).primaryColor.withOpacity(0.1)
                      : const Color(0xFFF0F2F5),
                  borderRadius: BorderRadius.circular(20),
                  border: _selectedModel?.vendorId == vendor.id
                      ? Border.all(color: Theme.of(context).primaryColor, width: 1.5)
                      : null,
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(vendor.name, style: TextStyle(
                      fontSize: 13, fontWeight: FontWeight.w500,
                      color: _selectedModel?.vendorId == vendor.id
                          ? Theme.of(context).primaryColor : const Color(0xFF666666),
                    )),
                    const Icon(Icons.arrow_drop_down, size: 18, color: Color(0xFF999999)),
                  ],
                ),
              ),
              onSelected: (model) => setState(() => _selectedModel = model),
              itemBuilder: (_) => cModels.map((model) {
                final isSelected = _selectedModel?.id == model.id;
                return PopupMenuItem<AiModel>(
                  value: model,
                  child: ModelListItem(model: model, isSelected: isSelected),
                );
              }).toList(),
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildLanguageSelector() {
    return Container(
      height: 44,
      color: Colors.white,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        children: _languages.map((lang) {
          final isSelected = _selectedLanguage == lang;
          return Padding(
            padding: const EdgeInsets.only(right: 8),
            child: ChoiceChip(
              label: Text(lang),
              selected: isSelected,
              onSelected: (_) => setState(() => _selectedLanguage = lang),
              selectedColor: Theme.of(context).primaryColor.withOpacity(0.15),
              labelStyle: TextStyle(
                fontSize: 12,
                color: isSelected ? Theme.of(context).primaryColor : const Color(0xFF666666),
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
              ),
            ),
          );
        }).toList(),
      ),
    );
  }
}