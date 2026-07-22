class Message {
  final int? id;
  final String conversationId;
  final String role; // user / assistant
  final String content;
  final String modelId;
  final String modelName;
  final String vendorName;
  final DateTime timestamp;
  final int? responseTimeMs;

  const Message({
    this.id,
    required this.conversationId,
    required this.role,
    required this.content,
    required this.modelId,
    required this.modelName,
    required this.vendorName,
    required this.timestamp,
    this.responseTimeMs,
  });

  Map<String, dynamic> toMap() => {
        'id': id,
        'conversationId': conversationId,
        'role': role,
        'content': content,
        'modelId': modelId,
        'modelName': modelName,
        'vendorName': vendorName,
        'timestamp': timestamp.toIso8601String(),
        'responseTimeMs': responseTimeMs ?? 0,
      };

  factory Message.fromMap(Map<String, dynamic> map) => Message(
        id: map['id'] as int?,
        conversationId: map['conversationId'] as String,
        role: map['role'] as String,
        content: map['content'] as String,
        modelId: map['modelId'] as String,
        modelName: map['modelName'] as String,
        vendorName: map['vendorName'] as String,
        timestamp: DateTime.parse(map['timestamp'] as String),
        responseTimeMs: map['responseTimeMs'] as int?,
      );
}