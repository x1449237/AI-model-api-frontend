import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:uuid/uuid.dart';
import '../data/vendors_config.dart';
import '../data/api_service.dart';
import '../data/database_helper.dart';
import '../models/ai_model.dart';
import '../widgets/cluster_card.dart';

class ClusterScreen extends StatefulWidget {
  const ClusterScreen({super.key});

  @override
  State<ClusterScreen> createState() => _ClusterScreenState();
}

class _ClusterScreenState extends State<ClusterScreen> {
  final Set<String> _selectedModelIds = {};
  final _promptController = TextEditingController();
  bool _isRunning = false;
  final List<_ClusterResult> _results = [];
  String _clusterSessionId = '';

  @override
  void dispose() {
    _promptController.dispose();
    super.dispose();
  }

  List<AiModel> _getSelectedModels() {
    final allModels = VendorsConfig.getAllModels();
    return allModels.where((m) => _selectedModelIds.contains(m.id)).toList();
  }

  Future<void> _runCluster() async {
    final prompt = _promptController.text.trim();
    if (prompt.isEmpty || _selectedModelIds.length < 2) return;

    final models = _getSelectedModels();
    if (models.length > 5) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('建议不超过5个模型进行集群对比')),
      );
      return;
    }

    setState(() {
      _isRunning = true;
      _results.clear();
      _clusterSessionId = const Uuid().v4();
    });

    final apiService = context.read<ApiService>();
    final futures = models.map((model) async {
      final stopwatch = Stopwatch()..start();
      try {
        final response = await apiService.sendMessage(
          model: model,
          prompt: prompt,
          temperature: 0.7,
          topP: 0.9,
          maxTokens: 2048,
        );
        stopwatch.stop();
        return _ClusterResult(
          model: model,
          content: response,
          responseTimeMs: stopwatch.elapsedMilliseconds,
          isError: false,
        );
      } catch (e) {
        stopwatch.stop();
        return _ClusterResult(
          model: model,
          content: '请求失败: $e',
          responseTimeMs: stopwatch.elapsedMilliseconds,
          isError: true,
        );
      }
    });

    final results = await Future.wait(futures);

    // Save to database
    for (final result in results) {
      await DatabaseHelper.instance.insertClusterResult(
        clusterSessionId: _clusterSessionId,
        modelId: result.model.id,
        modelName: result.model.name,
        vendorName: result.model.vendorName,
        content: result.content,
        responseTimeMs: result.responseTimeMs,
        timestamp: DateTime.now(),
      );
    }

    // Sort by response time
    results.sort((a, b) => a.responseTimeMs.compareTo(b.responseTimeMs));

    setState(() {
      _results.addAll(results);
      _isRunning = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final allModels = VendorsConfig.getAllModels();
    final domesticVendors = VendorsConfig.vendors.where((v) => v.category == 'domestic').toList();
    final internationalVendors = VendorsConfig.vendors.where((v) => v.category == 'international').toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('集群对比'),
        actions: [
          if (_selectedModelIds.isNotEmpty)
            TextButton(
              onPressed: () => setState(() => _selectedModelIds.clear()),
              child: const Text('清除选择'),
            ),
        ],
      ),
      body: Column(
        children: [
          // Prompt input
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _promptController,
              decoration: InputDecoration(
                hintText: '输入问题，将同时发送给${_selectedModelIds.length}个模型...',
                prefixIcon: const Icon(Icons.question_answer_outlined),
                suffixIcon: _selectedModelIds.length >= 2 && !_isRunning
                    ? TextButton(
                        onPressed: _runCluster,
                        child: Text('发送(${_selectedModelIds.length})', style: TextStyle(
                          color: Theme.of(context).primaryColor,
                          fontWeight: FontWeight.w600,
                        )),
                      )
                    : null,
              ),
              maxLines: 3,
              minLines: 2,
            ),
          ),
          // Model selection area
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            color: _selectedModelIds.length >= 2
                ? Colors.green.shade50
                : Colors.white,
            child: Row(
              children: [
                Icon(
                  _selectedModelIds.length >= 2 ? Icons.check_circle : Icons.info_outline,
                  size: 18,
                  color: _selectedModelIds.length >= 2 ? Colors.green : Colors.orange,
                ),
                const SizedBox(width: 8),
                Text(
                  _selectedModelIds.length >= 2
                      ? '已选择 ${_selectedModelIds.length} 个模型，可以开始集群对比'
                      : '请至少选择 2 个模型（建议不超过 5 个）',
                  style: TextStyle(
                    fontSize: 13,
                    color: _selectedModelIds.length >= 2 ? Colors.green.shade700 : const Color(0xFF666666),
                  ),
                ),
              ],
            ),
          ),
          // Model list
          Expanded(
            child: ListView(
              padding: const EdgeInsets.symmetric(vertical: 8),
              children: [
                const Padding(
                  padding: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  child: Text('国内厂商', style: TextStyle(
                    fontSize: 14, fontWeight: FontWeight.w600, color: Color(0xFF333333),
                  )),
                ),
                ...domesticVendors.map((vendor) => _buildVendorSection(vendor)),
                const Padding(
                  padding: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  child: Text('国外厂商', style: TextStyle(
                    fontSize: 14, fontWeight: FontWeight.w600, color: Color(0xFF333333),
                  )),
                ),
                ...internationalVendors.map((vendor) => _buildVendorSection(vendor)),
              ],
            ),
          ),
          // Results
          if (_isRunning || _results.isNotEmpty)
            Expanded(
              child: _isRunning
                  ? const Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          CircularProgressIndicator(),
                          SizedBox(height: 16),
                          Text('正在并行调用模型...', style: TextStyle(color: Color(0xFF999999))),
                        ],
                      ),
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.all(16),
                      itemCount: _results.length,
                      itemBuilder: (context, index) {
                        return ClusterCard(
                          result: _results[index],
                          rank: index + 1,
                        );
                      },
                    ),
            ),
        ],
      ),
    );
  }

  Widget _buildVendorSection(Vendor vendor) {
    final models = VendorsConfig.modelsByVendor[vendor.id] ?? [];
    return ExpansionTile(
      leading: CircleAvatar(
        radius: 16,
        backgroundColor: Theme.of(context).primaryColor.withOpacity(0.1),
        child: Text(
          vendor.name[0],
          style: TextStyle(
            fontSize: 14, fontWeight: FontWeight.bold,
            color: Theme.of(context).primaryColor,
          ),
        ),
      ),
      title: Text(vendor.name, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
      subtitle: Text(vendor.nameEn, style: const TextStyle(fontSize: 12, color: Color(0xFF999999))),
      children: models.map((model) => CheckboxListTile(
        value: _selectedModelIds.contains(model.id),
        onChanged: (selected) {
          setState(() {
            if (selected == true) {
              if (_selectedModelIds.length < 5) {
                _selectedModelIds.add(model.id);
              } else {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('最多选择5个模型')),
                );
              }
            } else {
              _selectedModelIds.remove(model.id);
            }
          });
        },
        title: Row(
          children: [
            Expanded(
              child: Text(model.name, style: const TextStyle(fontSize: 13)),
            ),
            if (model.isLatest)
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                decoration: BoxDecoration(
                  color: Colors.blue.shade50,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text('最新', style: TextStyle(fontSize: 10, color: Colors.blue.shade700)),
              ),
            if (model.isBestPerformance) ...[
              const SizedBox(width: 4),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                decoration: BoxDecoration(
                  color: Colors.orange.shade50,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text('最佳', style: TextStyle(fontSize: 10, color: Colors.orange.shade700)),
              ),
            ],
          ],
        ),
        subtitle: Text('${model.contextLength > 0 ? '${model.contextLength ~/ 1000}K' : '-'} | ${model.description ?? ''}', style: const TextStyle(fontSize: 11)),
        dense: true,
      )).toList(),
    );
  }
}

class _ClusterResult {
  final AiModel model;
  final String content;
  final int responseTimeMs;
  final bool isError;

  const _ClusterResult({
    required this.model,
    required this.content,
    required this.responseTimeMs,
    required this.isError,
  });
}