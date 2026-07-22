import 'package:flutter/material.dart';
import '../screens/cluster_screen.dart';

class ClusterCard extends StatelessWidget {
  final _ClusterResult result;
  final int rank;

  const ClusterCard({super.key, required this.result, required this.rank});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            decoration: BoxDecoration(
              color: _getRankColor().withOpacity(0.05),
              borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(12),
                topRight: Radius.circular(12),
              ),
            ),
            child: Row(
              children: [
                Container(
                  width: 24,
                  height: 24,
                  decoration: BoxDecoration(
                    color: _getRankColor(),
                    shape: BoxShape.circle,
                  ),
                  alignment: Alignment.center,
                  child: Text(
                    '$rank',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                CircleAvatar(
                  radius: 12,
                  backgroundColor: Theme.of(context).primaryColor.withOpacity(0.1),
                  child: Text(
                    result.model.vendorName[0],
                    style: TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).primaryColor,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '${result.model.vendorName} - ${result.model.name}',
                        style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                      ),
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: _getSpeedColor(),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    '${result.responseTimeMs}ms',
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.w600,
                      color: _getSpeedTextColor(),
                    ),
                  ),
                ),
              ],
            ),
          ),
          // Content
          Padding(
            padding: const EdgeInsets.all(16),
            child: SelectableText(
              result.content,
              style: TextStyle(
                fontSize: 14,
                color: result.isError ? Colors.red : const Color(0xFF333333),
                height: 1.5,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Color _getRankColor() {
    switch (rank) {
      case 1: return const Color(0xFFFFD700);
      case 2: return const Color(0xFFC0C0C0);
      case 3: return const Color(0xFFCD7F32);
      default: return const Color(0xFF999999);
    }
  }

  Color _getSpeedColor() {
    if (result.responseTimeMs < 2000) return Colors.green.shade50;
    if (result.responseTimeMs < 5000) return Colors.orange.shade50;
    return Colors.red.shade50;
  }

  Color _getSpeedTextColor() {
    if (result.responseTimeMs < 2000) return Colors.green.shade700;
    if (result.responseTimeMs < 5000) return Colors.orange.shade700;
    return Colors.red.shade700;
  }
}