import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:share_plus/share_plus.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:io';
import 'package:http/http.dart' as http;
import '../data/vendors_config.dart';
import '../data/api_service.dart';
import '../models/ai_model.dart';
import '../widgets/model_card.dart';

class ImageGenerationScreen extends StatefulWidget {
  const ImageGenerationScreen({super.key});

  @override
  State<ImageGenerationScreen> createState() => _ImageGenerationScreenState();
}

class _ImageGenerationScreenState extends State<ImageGenerationScreen> {
  AiModel? _selectedModel;
  final _promptController = TextEditingController();
  String _selectedRatio = '1:1';
  int _numImages = 1;
  bool _isGenerating = false;
  final List<String> _generatedImages = [];

  static const _ratios = ['1:1', '16:9', '9:16', '4:3', '3:4'];

  @override
  void dispose() {
    _promptController.dispose();
    super.dispose();
  }

  Future<void> _generateImage() async {
    final prompt = _promptController.text.trim();
    if (prompt.isEmpty || _selectedModel == null) return;

    setState(() {
      _isGenerating = true;
      _generatedImages.clear();
    });

    try {
      final imageUrl = await context.read<ApiService>().generateImage(
        model: _selectedModel!,
        prompt: prompt,
        size: _selectedRatio,
        numImages: _numImages,
      );

      // Download and cache image
      final response = await http.get(Uri.parse(imageUrl));
      if (response.statusCode == 200) {
        final dir = await getApplicationDocumentsDirectory();
        final timestamp = DateTime.now().millisecondsSinceEpoch;
        final file = File('${dir.path}/generated_$timestamp.png');
        await file.writeAsBytes(response.bodyBytes);

        setState(() {
          _generatedImages.add(file.path);
          _isGenerating = false;
        });
      } else {
        // Store the URL for display
        setState(() {
          _generatedImages.add(imageUrl);
          _isGenerating = false;
        });
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('生成失败: $e')),
        );
      }
      setState(() => _isGenerating = false);
    }
  }

  Future<void> _shareImage(String path) async {
    try {
      if (path.startsWith('http')) {
        // Download first then share
        final response = await http.get(Uri.parse(path));
        final dir = await getApplicationDocumentsDirectory();
        final file = File('${dir.path}/share_temp.png');
        await file.writeAsBytes(response.bodyBytes);
        await Share.shareXFiles([XFile(file.path)], text: 'AI生成的图片');
      } else {
        await Share.shareXFiles([XFile(path)], text: 'AI生成的图片');
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('分享失败: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final imageModels = VendorsConfig.getImageModels();

    return Scaffold(
      appBar: AppBar(title: const Text('图片生成')),
      body: Column(
        children: [
          // Model selector
          Container(
            height: 50,
            color: Colors.white,
            child: ListView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              children: imageModels.map((model) {
                final isSelected = _selectedModel?.id == model.id;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: GestureDetector(
                    onTap: () => setState(() => _selectedModel = model),
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                      decoration: BoxDecoration(
                        color: isSelected
                            ? Theme.of(context).primaryColor.withOpacity(0.1)
                            : const Color(0xFFF0F2F5),
                        borderRadius: BorderRadius.circular(20),
                        border: isSelected
                            ? Border.all(color: Theme.of(context).primaryColor, width: 1.5)
                            : null,
                      ),
                      child: Text(
                        '${model.vendorName} - ${model.name}',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w500,
                          color: isSelected
                              ? Theme.of(context).primaryColor
                              : const Color(0xFF666666),
                        ),
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
          ),
          // Ratio selector
          Container(
            height: 44,
            color: Colors.white,
            child: Row(
              children: [
                const Padding(
                  padding: EdgeInsets.only(left: 16),
                  child: Text('比例:', style: TextStyle(fontSize: 13, color: Color(0xFF666666))),
                ),
                ..._ratios.map((ratio) => Padding(
                  padding: const EdgeInsets.only(left: 8),
                  child: ChoiceChip(
                    label: Text(ratio),
                    selected: _selectedRatio == ratio,
                    onSelected: (_) => setState(() => _selectedRatio = ratio),
                  ),
                )),
                const Spacer(),
                const Text('数量:', style: TextStyle(fontSize: 13, color: Color(0xFF666666))),
                DropdownButton<int>(
                  value: _numImages,
                  items: [1, 2, 3, 4].map((n) => DropdownMenuItem(value: n, child: Text('$n'))).toList(),
                  onChanged: (v) => setState(() => _numImages = v ?? 1),
                  underline: const SizedBox(),
                ),
                const SizedBox(width: 12),
              ],
            ),
          ),
          // Prompt input
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _promptController,
              decoration: const InputDecoration(
                hintText: '描述你想要生成的图片...',
                prefixIcon: Icon(Icons.brush_outlined),
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
                onPressed: _selectedModel != null && !_isGenerating ? _generateImage : null,
                icon: const Icon(Icons.image),
                label: const Text('生成图片'),
              ),
            ),
          ),
          const SizedBox(height: 16),
          // Results
          Expanded(
            child: _isGenerating
                ? const Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        CircularProgressIndicator(),
                        SizedBox(height: 16),
                        Text('正在生成图片...', style: TextStyle(color: Color(0xFF999999))),
                      ],
                    ),
                  )
                : _generatedImages.isEmpty
                    ? Center(
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(Icons.image_outlined, size: 64, color: Colors.grey.shade300),
                            const SizedBox(height: 16),
                            Text(
                              '选择图像生成模型，输入提示词\n点击生成图片',
                              textAlign: TextAlign.center,
                              style: TextStyle(color: Colors.grey.shade500, fontSize: 14),
                            ),
                          ],
                        ),
                      )
                    : GridView.builder(
                        padding: const EdgeInsets.all(16),
                        gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: _generatedImages.length == 1 ? 1 : 2,
                          crossAxisSpacing: 8,
                          mainAxisSpacing: 8,
                          childAspectRatio: _selectedRatio == '16:9'
                              ? 16 / 9
                              : _selectedRatio == '9:16'
                                  ? 9 / 16
                                  : 1,
                        ),
                        itemCount: _generatedImages.length,
                        itemBuilder: (context, index) {
                          final path = _generatedImages[index];
                          return GestureDetector(
                            onTap: () => _shareImage(path),
                            child: ClipRRect(
                              borderRadius: BorderRadius.circular(12),
                              child: Stack(
                                fit: StackFit.expand,
                                children: [
                                  path.startsWith('http')
                                      ? Image.network(path, fit: BoxFit.cover)
                                      : Image.file(File(path), fit: BoxFit.cover),
                                  Positioned(
                                    top: 8, right: 8,
                                    child: Container(
                                      padding: const EdgeInsets.all(6),
                                      decoration: BoxDecoration(
                                        color: Colors.black54,
                                        borderRadius: BorderRadius.circular(20),
                                      ),
                                      child: const Icon(Icons.share, color: Colors.white, size: 16),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          );
                        },
                      ),
          ),
        ],
      ),
    );
  }
}