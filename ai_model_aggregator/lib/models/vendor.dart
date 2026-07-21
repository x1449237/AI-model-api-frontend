class Vendor {
  final String id;
  final String name;
  final String nameEn;
  final String logo;
  final String category; // domestic / international
  final String baseUrl;

  const Vendor({
    required this.id,
    required this.name,
    required this.nameEn,
    required this.logo,
    required this.category,
    required this.baseUrl,
  });

  Map<String, dynamic> toMap() => {
        'id': id,
        'name': name,
        'nameEn': nameEn,
        'logo': logo,
        'category': category,
        'baseUrl': baseUrl,
      };
}