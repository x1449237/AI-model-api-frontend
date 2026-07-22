import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:uuid/uuid.dart';
import '../data/vendors_config.dart';
import '../data/api_service.dart';
import '../data/database_helper.dart';
import '../models/ai_model.dart';
import '../models/conversation.dart';
import '../models/message.dart';
import '../widgets/model_card.dart';
import '../widgets/chat_bubble.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  AiModel? _selectedModel;
  String _conversationId = '';
  final _messageController = TextEditingController();
  final _scrollController = ScrollController();
  final _streamController = StreamController<String>();
  List<Message> _messages = [];
  bool _isStreaming = false;
  String _streamingText = '';
  double _temperature = 0.7;
  double _topP = 0.9;
  int _maxTokens = 2048;
  bool _showParams = false;

  @override
  void initState() {
    super.initState();
    _startNewConversation();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ApiService>().loadApiKeys();
    });
  }

  void _startNewConversation() {
    _conversationId = const Uuid().v4();
    _messages = [];
    _streamingText = '';
  }

  Future<void> _sendMessage() async {
    final text = _messageController.text.trim();
    if (text.isEmpty || _selectedModel == null) return;

    _messageController.clear();
    final userMsg = Message(
      conversationId: _conversationId,
      role: 'user',
      content: text,
      modelId: _selectedModel!.id,
      modelName: _selectedModel!.name,
      vendorName: _selectedModel!.vendorName,
      timestamp: DateTime.now(),
    );

    setState(() {
      _messages.add(userMsg);
      _isStreaming = true;
      _streamingText = '';
    });

    await DatabaseHelper.instance.insertMessage(userMsg);

    // Save conversation
    final existingConvs = await DatabaseHelper.instance.getConversations();
    if (!existingConvs.any((c) => c.id == _conversationId)) {
      await DatabaseHelper.instance.insertConversation(Conversation(
        id: _conversationId,
        title: text.length > 30 ? '${text.substring(0, 30)}...' : text,
        modelId: _selectedModel!.id,
        modelName: _selectedModel!.name,
        vendorName: _selectedModel!.vendorName,
        type: 'text',
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      ));
    } else {
      await DatabaseHelper.instance.updateConversationTime(_conversationId, DateTime.now());
    }

    final streamCtrl = StreamController<String>();
    final stopwatch = Stopwatch()..start();

    streamCtrl.stream.listen((chunk) {
      setState(() {
        _streamingText += chunk;
      });
      _scrollToBottom();
    });

    final response = await context.read<ApiService>().sendMessage(
      model: _selectedModel!,
      prompt: text,
      temperature: _temperature,
      topP: _topP,
      maxTokens: _maxTokens,
      streamController: streamCtrl,
    );

    stopwatch.stop();
    await streamCtrl.close();

    final assistantMsg = Message(
      conversationId: _conversationId,
      role: 'assistant',
      content: _streamingText.isNotEmpty ? _streamingText : response,
      modelId: _selectedModel!.id,
      modelName: _selectedModel!.name,
      vendorName: _selectedModel!.vendorName,
      timestamp: DateTime.now(),
      responseTimeMs: stopwatch.elapsedMilliseconds,
    );

    await DatabaseHelper.instance.insertMessage(assistantMsg);
    await DatabaseHelper.instance.updateConversationTime(_conversationId, DateTime.now());

    setState(() {
      if (_streamingText.isNotEmpty) {
        _messages.add(assistantMsg);
      } else {
        _messages.add(assistantMsg);
      }
      _isStreaming = false;
      _streamingText = '';
    });
    _scrollToBottom();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    _streamController.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('AI聚合助手'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_comment_outlined),
            tooltip: '新对话',
            onPressed: () {
              setState(() => _startNewConversation());
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Model selector
          _buildModelSelector(),
          // Parameter bar
          _buildParamsBar(),
          // Chat messages
          Expanded(child: _buildChatArea()),
          // Input area
          _buildInputArea(),
        ],
      ),
    );
  }

  Widget _buildModelSelector() {
    return Container(
      height: 50,
      color: Colors.white,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        children: [
          ...VendorsConfig.vendors.map((vendor) {
            final models = VendorsConfig.modelsByVendor[vendor.id] ?? [];
            final textModels = models.where((m) => m.supportsCode && !m.supportsImage).toList();
            if (textModels.isEmpty) return const SizedBox.shrink();
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
                      Text(
                        vendor.name,
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w500,
                          color: _selectedModel?.vendorId == vendor.id
                              ? Theme.of(context).primaryColor
                              : const Color(0xFF666666),
                        ),
                      ),
                      const Icon(Icons.arrow_drop_down, size: 18, color: Color(0xFF999999)),
                    ],
                  ),
                ),
                onSelected: (model) {
                  setState(() {
                    _selectedModel = model;
                    _startNewConversation();
                  });
                },
                itemBuilder: (_) => textModels.map((model) {
                  final isSelected = _selectedModel?.id == model.id;
                  return PopupMenuItem<AiModel>(
                    value: model,
                    child: ModelListItem(model: model, isSelected: isSelected),
                  );
                }).toList(),
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildParamsBar() {
    if (!_showParams) return const SizedBox.shrink();
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      color: Colors.white,
      child: Column(
        children: [
          Row(
            children: [
              const Text('Temperature: ', style: TextStyle(fontSize: 12, color: Color(0xFF666666))),
              Expanded(
                child: Slider(
                  value: _temperature,
                  min: 0,
                  max: 1,
                  divisions: 20,
                  onChanged: (v) => setState(() => _temperature = v),
                ),
              ),
              Text(_temperature.toStringAsFixed(2), style: const TextStyle(fontSize: 12)),
            ],
          ),
          Row(
            children: [
              const Text('Top-P: ', style: TextStyle(fontSize: 12, color: Color(0xFF666666))),
              Expanded(
                child: Slider(
                  value: _topP,
                  min: 0,
                  max: 1,
                  divisions: 20,
                  onChanged: (v) => setState(() => _topP = v),
                ),
              ),
              Text(_topP.toStringAsFixed(2), style: const TextStyle(fontSize: 12)),
            ],
          ),
          Row(
            children: [
              const Text('Max Tokens: ', style: TextStyle(fontSize: 12, color: Color(0xFF666666))),
              Expanded(
                child: Slider(
                  value: _maxTokens.toDouble(),
                  min: 256,
                  max: 8192,
                  divisions: 31,
                  onChanged: (v) => setState(() => _maxTokens = v.round()),
                ),
              ),
              Text('$_maxTokens', style: const TextStyle(fontSize: 12)),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildChatArea() {
    return GestureDetector(
      onTap: () => FocusScope.of(context).unfocus(),
      child: ListView.builder(
        controller: _scrollController,
        padding: const EdgeInsets.symmetric(vertical: 8),
        itemCount: _messages.length + (_isStreaming ? 1 : 0),
        itemBuilder: (context, index) {
          if (index < _messages.length) {
            return ChatBubble(message: _messages[index]);
          }
          return ChatBubble(
            message: Message(
              conversationId: _conversationId,
              role: 'assistant',
              content: _streamingText,
              modelId: _selectedModel?.id ?? '',
              modelName: _selectedModel?.name ?? '',
              vendorName: _selectedModel?.vendorName ?? '',
              timestamp: DateTime.now(),
            ),
            isStreaming: true,
          );
        },
      ),
    );
  }

  Widget _buildInputArea() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      color: Colors.white,
      child: SafeArea(
        child: Row(
          children: [
            IconButton(
              icon: Icon(
                _showParams ? Icons.tune : Icons.tune_outlined,
                color: _showParams ? Theme.of(context).primaryColor : const Color(0xFF999999),
              ),
              onPressed: () => setState(() => _showParams = !_showParams),
            ),
            Expanded(
              child: TextField(
                controller: _messageController,
                decoration: InputDecoration(
                  hintText: _selectedModel != null
                      ? '向 ${_selectedModel!.name} 发送消息...'
                      : '请先选择模型',
                  suffixIcon: IconButton(
                    icon: Icon(
                      Icons.send_rounded,
                      color: _selectedModel != null && !_isStreaming
                          ? Theme.of(context).primaryColor
                          : const Color(0xFFCCCCCC),
                    ),
                    onPressed: _selectedModel != null && !_isStreaming ? _sendMessage : null,
                  ),
                ),
                onSubmitted: (_) => _selectedModel != null && !_isStreaming ? _sendMessage() : null,
                maxLines: 3,
                minLines: 1,
              ),
            ),
          ],
        ),
      ),
    );
  }
}