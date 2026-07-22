import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../data/database_helper.dart';
import '../data/api_service.dart';
import '../data/vendors_config.dart';
import '../models/conversation.dart';
import '../models/message.dart';
import 'home_screen.dart';

class HistoryScreen extends StatefulWidget {
  const HistoryScreen({super.key});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  List<Conversation> _conversations = [];
  final _apiKeyControllers = <String, TextEditingController>{};
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadData();
  }

  Future<void> _loadData() async {
    final convs = await DatabaseHelper.instance.getConversations();
    setState(() {
      _conversations = convs;
      _isLoading = false;
    });
  }

  @override
  void dispose() {
    _tabController.dispose();
    for (final c in _apiKeyControllers.values) {
      c.dispose();
    }
    super.dispose();
  }

  Future<void> _deleteConversation(Conversation conv) async {
    await DatabaseHelper.instance.deleteConversation(conv.id);
    _loadData();
  }

  Future<void> _loadConversation(Conversation conv) async {
    final messages = await DatabaseHelper.instance.getMessages(conv.id);
    if (!mounted) return;
    // Navigate to home screen with this conversation
    final appState = context.read<AppState>();
    appState.setTab(0);
    // We'd need to load the conversation into the home screen
    // This is a simplified version
  }

  String _maskKey(String key) {
    if (key.length <= 8) return '****';
    return '${key.substring(0, 4)}...${key.substring(key.length - 4)}';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('历史记录'),
        bottom: TabBar(
          controller: _tabController,
          labelColor: Theme.of(context).primaryColor,
          unselectedLabelColor: const Color(0xFF999999),
          tabs: const [
            Tab(text: '对话历史'),
            Tab(text: '我的'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildHistoryTab(),
          _buildMyTab(),
        ],
      ),
    );
  }

  Widget _buildHistoryTab() {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_conversations.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.chat_bubble_outline, size: 64, color: Colors.grey.shade300),
            const SizedBox(height: 16),
            Text('暂无对话记录', style: TextStyle(color: Colors.grey.shade500, fontSize: 14)),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadData,
      child: ListView.builder(
        padding: const EdgeInsets.symmetric(vertical: 8),
        itemCount: _conversations.length,
        itemBuilder: (context, index) {
          final conv = _conversations[index];
          return Dismissible(
            key: Key(conv.id),
            direction: DismissDirection.endToStart,
            background: Container(
              alignment: Alignment.centerRight,
              padding: const EdgeInsets.only(right: 20),
              color: Colors.red,
              child: const Icon(Icons.delete, color: Colors.white),
            ),
            onDismissed: (_) => _deleteConversation(conv),
            child: ListTile(
              leading: CircleAvatar(
                backgroundColor: _getTypeColor(conv.type).withOpacity(0.1),
                child: Icon(_getTypeIcon(conv.type), color: _getTypeColor(conv.type), size: 20),
              ),
              title: Text(conv.title, maxLines: 1, overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
              subtitle: Text(
                '${conv.vendorName} - ${conv.modelName} | ${_formatDate(conv.updatedAt)}',
                style: const TextStyle(fontSize: 12, color: Color(0xFF999999)),
              ),
              trailing: const Icon(Icons.chevron_right, color: Color(0xFFCCCCCC)),
              onTap: () => _loadConversation(conv),
            ),
          );
        },
      ),
    );
  }

  Widget _buildMyTab() {
    final apiService = context.watch<ApiService>();

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        const SizedBox(height: 8),
        const Text('API Key 配置', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        const SizedBox(height: 4),
        const Text('配置各厂商的API Key后即可使用对应模型',
            style: TextStyle(fontSize: 13, color: Color(0xFF999999))),
        const SizedBox(height: 16),
        ...VendorsConfig.vendors.map((vendor) {
          return Card(
            margin: const EdgeInsets.only(bottom: 8),
            child: ExpansionTile(
              leading: CircleAvatar(
                radius: 16,
                backgroundColor: Theme.of(context).primaryColor.withOpacity(0.1),
                child: Text(vendor.name[0],
                    style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.bold,
                        color: Theme.of(context).primaryColor)),
              ),
              title: Text(vendor.name, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
              subtitle: Text(vendor.nameEn, style: const TextStyle(fontSize: 12, color: Color(0xFF999999))),
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  child: Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _getApiKeyController(vendor.id),
                          decoration: InputDecoration(
                            hintText: '输入 ${vendor.name} API Key',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                            isDense: true,
                            contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                          ),
                          obscureText: true,
                          style: const TextStyle(fontSize: 13),
                        ),
                      ),
                      const SizedBox(width: 8),
                      ElevatedButton(
                        onPressed: () {
                          final key = _getApiKeyController(vendor.id).text.trim();
                          if (key.isNotEmpty) {
                            apiService.setApiKey(vendor.id, key);
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text('${vendor.name} API Key 已保存')),
                            );
                          }
                        },
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                        ),
                        child: const Text('保存', style: TextStyle(fontSize: 13)),
                      ),
                    ],
                  ),
                ),
                if (apiService.getApiKey(vendor.id) != null)
                  Padding(
                    padding: const EdgeInsets.only(left: 16, right: 16, bottom: 8),
                    child: Row(
                      children: [
                        Icon(Icons.check_circle, size: 16, color: Colors.green.shade600),
                        const SizedBox(width: 4),
                        Text(
                          '已配置: ${_maskKey(apiService.getApiKey(vendor.id)!)}',
                          style: TextStyle(fontSize: 12, color: Colors.green.shade600),
                        ),
                      ],
                    ),
                  ),
              ],
            ),
          );
        }),
        const SizedBox(height: 24),
        // App info
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('关于', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
                const SizedBox(height: 12),
                _infoRow('应用名称', 'AI模型聚合助手'),
                _infoRow('版本', '1.0.0'),
                _infoRow('支持厂商', '20个'),
                _infoRow('支持模型', '${VendorsConfig.getAllModels().length}+'),
                _infoRow('功能', '文本生成 / 代码生成 / 图片生成 / 集群对比'),
              ],
            ),
          ),
        ),
        const SizedBox(height: 32),
      ],
    );
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          SizedBox(
            width: 80,
            child: Text(label, style: const TextStyle(fontSize: 13, color: Color(0xFF999999))),
          ),
          Expanded(child: Text(value, style: const TextStyle(fontSize: 13))),
        ],
      ),
    );
  }

  TextEditingController _getApiKeyController(String vendorId) {
    if (!_apiKeyControllers.containsKey(vendorId)) {
      _apiKeyControllers[vendorId] = TextEditingController();
    }
    return _apiKeyControllers[vendorId]!;
  }

  Color _getTypeColor(String type) {
    switch (type) {
      case 'text': return const Color(0xFF4A90D9);
      case 'code': return const Color(0xFF6C5CE7);
      case 'image': return const Color(0xFFE17055);
      case 'cluster': return const Color(0xFF00B894);
      default: return const Color(0xFF999999);
    }
  }

  IconData _getTypeIcon(String type) {
    switch (type) {
      case 'text': return Icons.chat_bubble_outline;
      case 'code': return Icons.code;
      case 'image': return Icons.image_outlined;
      case 'cluster': return Icons.compare_arrows;
      default: return Icons.chat_bubble_outline;
    }
  }

  String _formatDate(DateTime dt) {
    final now = DateTime.now();
    final diff = now.difference(dt);
    if (diff.inMinutes < 60) return '${diff.inMinutes}分钟前';
    if (diff.inHours < 24) return '${diff.inHours}小时前';
    if (diff.inDays < 7) return '${diff.inDays}天前';
    return '${dt.month}/${dt.day}';
  }
}