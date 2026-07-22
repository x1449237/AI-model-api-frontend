class Conversation {
  final String id;
  final String title;
  final String modelId;
  final String modelName;
  final String vendorName;
  final String type; // text / code / image / cluster
  final DateTime createdAt;
  final DateTime updatedAt;
  final List<String>? clusterModelIds;

  const Conversation({
    required this.id,
    required this.title,
    required this.modelId,
    required this.modelName,
    required this.vendorName,
    required this.type,
    required this.createdAt,
    required this.updatedAt,
    this.clusterModelIds,
  });

  Map<String, dynamic> toMap() => {
        'id': id,
        'title': title,
        'modelId': modelId,
        'modelName': modelName,
        'vendorName': vendorName,
        'type': type,
        'createdAt': createdAt.toIso8601String(),
        'updatedAt': updatedAt.toIso8601String(),
        'clusterModelIds': clusterModelIds?.join(',') ?? '',
      };

  factory Conversation.fromMap(Map<String, dynamic> map) => Conversation(
        id: map['id'] as String,
        title: map['title'] as String,
        modelId: map['modelId'] as String,
        modelName: map['modelName'] as String,
        vendorName: map['vendorName'] as String,
        type: map['type'] as String,
        createdAt: DateTime.parse(map['createdAt'] as String),
        updatedAt: DateTime.parse(map['updatedAt'] as String),
        clusterModelIds: (map['clusterModelIds'] as String?)?.isNotEmpty == true
            ? (map['clusterModelIds'] as String).split(',')
            : null,
      );
}