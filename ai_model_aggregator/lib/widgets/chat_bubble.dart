import 'package:flutter/material.dart';
import '../models/message.dart';

class ChatBubble extends StatelessWidget {
  final Message message;
  final bool isStreaming;

  const ChatBubble({super.key, required this.message, this.isStreaming = false});

  @override
  Widget build(BuildContext context) {
    final isUser = message.role == 'user';

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      child: Column(
        crossAxisAlignment: isUser ? CrossAxisAlignment.end : CrossAxisAlignment.start,
        children: [
          // Model label for assistant
          if (!isUser && message.modelName.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(left: 12, bottom: 4),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  CircleAvatar(
                    radius: 10,
                    backgroundColor: Theme.of(context).primaryColor.withOpacity(0.1),
                    child: Text(
                      message.vendorName.isNotEmpty ? message.vendorName[0] : 'A',
                      style: TextStyle(
                        fontSize: 9,
                        fontWeight: FontWeight.bold,
                        color: Theme.of(context).primaryColor,
                      ),
                    ),
                  ),
                  const SizedBox(width: 6),
                  Text(
                    '${message.vendorName} - ${message.modelName}',
                    style: const TextStyle(fontSize: 11, color: Color(0xFF999999)),
                  ),
                  if (message.responseTimeMs != null && message.responseTimeMs! > 0) ...[
                    const SizedBox(width: 8),
                    Text(
                      '${message.responseTimeMs}ms',
                      style: const TextStyle(fontSize: 10, color: Color(0xFFCCCCCC)),
                    ),
                  ],
                ],
              ),
            ),
          // Bubble
          Row(
            mainAxisAlignment: isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (!isUser) ...[
                // Assistant avatar
                CircleAvatar(
                  radius: 16,
                  backgroundColor: Theme.of(context).primaryColor.withOpacity(0.1),
                  child: Icon(Icons.auto_awesome, size: 16, color: Theme.of(context).primaryColor),
                ),
                const SizedBox(width: 8),
              ],
              Flexible(
                child: Container(
                  constraints: BoxConstraints(
                    maxWidth: MediaQuery.of(context).size.width * 0.75,
                  ),
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  decoration: BoxDecoration(
                    color: isUser
                        ? Theme.of(context).primaryColor
                        : Colors.white,
                    borderRadius: BorderRadius.only(
                      topLeft: const Radius.circular(16),
                      topRight: const Radius.circular(16),
                      bottomLeft: Radius.circular(isUser ? 16 : 4),
                      bottomRight: Radius.circular(isUser ? 4 : 16),
                    ),
                    boxShadow: isUser
                        ? []
                        : [BoxShadow(color: Colors.black.withOpacity(0.04), blurRadius: 4, offset: const Offset(0, 2))],
                  ),
                  child: isStreaming
                      ? Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Flexible(
                              child: Text(
                                message.content,
                                style: TextStyle(
                                  fontSize: 14,
                                  color: isUser ? Colors.white : const Color(0xFF333333),
                                  height: 1.5,
                                ),
                              ),
                            ),
                            const SizedBox(width: 4),
                            const SizedBox(
                              width: 12,
                              height: 12,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF4A90D9)),
                              ),
                            ),
                          ],
                        )
                      : SelectableText(
                          message.content,
                          style: TextStyle(
                            fontSize: 14,
                            color: isUser ? Colors.white : const Color(0xFF333333),
                            height: 1.5,
                          ),
                        ),
                ),
              ),
              if (isUser) ...[
                const SizedBox(width: 8),
                CircleAvatar(
                  radius: 16,
                  backgroundColor: const Color(0xFFE8F0FE),
                  child: const Icon(Icons.person, size: 16, color: Color(0xFF4A90D9)),
                ),
              ],
            ],
          ),
        ],
      ),
    );
  }
}