import 'package:flutter/material.dart';
import '../swt/treeitem.dart';
import '../swt/widget.dart';
import '../impl/item_impl.dart';
import '../impl/widget_config.dart';

class TreeItemImpl<T extends TreeItemSwt, V extends TreeItemValue>
    extends ItemImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    final String text = state.text ?? "";
    final List<String>? texts = state.texts;
    final bool expanded = state.expanded ?? false;
    final bool selected = state.selected ?? false;

    final Color textColor = useDarkTheme ? Colors.white : Colors.black87;
    final Color bgColor = selected
        ? (useDarkTheme ? const Color(0xFF3C3C3C) : const Color(0xFFE8E8FF))
        : Colors.transparent;

    return Container(
      color: bgColor,
      child: Row(
        children: [
          if (state.children != null && state.children!.isNotEmpty)
            Icon(
              expanded ? Icons.keyboard_arrow_down : Icons.keyboard_arrow_right,
              size: 16,
              color: useDarkTheme ? Colors.white70 : Colors.black54,
            ),
          const SizedBox(width: 4),
          Icon(
            state.children != null && state.children!.isNotEmpty
                ? (expanded ? Icons.folder_open : Icons.folder)
                : Icons.insert_drive_file,
            size: 16,
            color: useDarkTheme ? Colors.white70 : Colors.grey.shade700,
          ),
          const SizedBox(width: 4),
          Text(
            texts != null && texts.isNotEmpty ? texts[0] : text,
            style: TextStyle(
              color: textColor,
              fontSize: 14,
            ),
          ),
        ],
      ),
    );
  }
}