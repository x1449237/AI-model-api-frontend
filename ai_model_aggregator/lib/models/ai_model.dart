class AiModel {
  final String id;
  final String name;
  final String vendorId;
  final String vendorName;
  final int contextLength;
  final bool supportsCode;
  final bool supportsImage;
  final bool isLatest;
  final bool isBestPerformance;
  final String? description;

  const AiModel({
    required this.id,
    required this.name,
    required this.vendorId,
    required this.vendorName,
    required this.contextLength,
    this.supportsCode = true,
    this.supportsImage = false,
    this.isLatest = false,
    this.isBestPerformance = false,
    this.description,
  });

  Map<String, dynamic> toMap() => {
        'id': id,
        'name': name,
        'vendorId': vendorId,
        'vendorName': vendorName,
        'contextLength': contextLength,
        'supportsCode': supportsCode ? 1 : 0,
        'supportsImage': supportsImage ? 1 : 0,
        'isLatest': isLatest ? 1 : 0,
        'isBestPerformance': isBestPerformance ? 1 : 0,
        'description': description ?? '',
      };
}