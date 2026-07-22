import 'package:flutter/material.dart';
import '../models/ai_model.dart';

class ModelListItem extends StatelessWidget {
  final AiModel model;
  final bool isSelected;

  const ModelListItem({super.key, required this.model, required this.isSelected});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          if (isSelected)
            Icon(Icons.check, size: 16, color: Theme.of(context).primaryColor)
          else
            const SizedBox(width: 16),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  model.name,
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                    color: isSelected ? Theme.of(context).primaryColor : const Color(0xFF333333),
                  ),
                ),
                if (model.description != null)
                  Text(
                    model.description!,
                    style: const TextStyle(fontSize: 11, color: Color(0xFF999999)),
                  ),
              ],
            ),
          ),
          if (model.contextLength > 0)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 2),
              decoration: BoxDecoration(
                color: const Color(0xFFF0F2F5),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(
                '${model.contextLength ~/ 1000}K',
                style: const TextStyle(fontSize: 10, color: Color(0xFF999999)),
              ),
            ),
          if (model.isLatest) ...[
            const SizedBox(width: 4),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 2),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text('最新', style: TextStyle(fontSize: 10, color: Colors.blue.shade700)),
            ),
          ],
          if (model.isBestPerformance) ...[
            const SizedBox(width: 4),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 2),
              decoration: BoxDecoration(
                color: Colors.orange.shade50,
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text('最佳', style: TextStyle(fontSize: 10, color: Colors.orange.shade700)),
            ),
          ],
        ],
      ),
    );
  }
}

class ModelCard extends StatelessWidget {
  final AiModel model;
  final bool isSelected;
  final VoidCallback? onTap;

  const ModelCard({super.key, required this.model, this.isSelected = false, this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 160,
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isSelected
              ? Theme.of(context).primaryColor.withOpacity(0.08)
              : Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected ? Theme.of(context).primaryColor : const Color(0xFFEEEEEE),
            width: isSelected ? 1.5 : 1,
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  radius: 12,
                  backgroundColor: Theme.of(context).primaryColor.withOpacity(0.1),
                  child: Text(
                    model.vendorName[0],
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).primaryColor,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    model.vendorName,
                    style: const TextStyle(fontSize: 11, color: Color(0xFF999999)),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              model.name,
              style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            const Spacer(),
            Wrap(
              spacing: 4,
              children: [
                if (model.contextLength > 0)
                  _buildTag('${model.contextLength ~/ 1000}K', const Color(0xFFF0F2F5), const Color(0xFF666666)),
                if (model.isLatest)
                  _buildTag('最新', Colors.blue.shade50, Colors.blue.shade700),
                if (model.isBestPerformance)
                  _buildTag('最佳', Colors.orange.shade50, Colors.orange.shade700),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTag(String text, Color bg, Color fg) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(text, style: TextStyle(fontSize: 10, color: fg)),
    );
  }
}