import 'package:flutter/material.dart';
import '../swt/treecolumn.dart';
import '../impl/item_impl.dart';
import '../impl/widget_config.dart';

class TreeColumnImpl<T extends TreeColumnSwt, V extends TreeColumnValue>
    extends ItemImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    final String text = state.text ?? "";
    final int? alignment = state.alignment;
    final bool moveable = state.moveable ?? false;
    final bool resizable = state.resizable ?? false;

    final Color textColor = useDarkTheme ? Colors.white70 : Colors.black87;

    return MouseRegion(
      cursor: SystemMouseCursors.click,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 6.0),
        decoration: BoxDecoration(
          color: useDarkTheme ? const Color(0xFF333333) : Colors.grey.shade200,
          border: Border(
            bottom: BorderSide(
              color: useDarkTheme ? Colors.black38 : Colors.grey.shade400,
              width: 1.0,
            ),
          ),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              text,
              style: TextStyle(
                color: textColor,
                fontWeight: FontWeight.w500,
                fontSize: 14,
              ),
            ),
            if (resizable)
              MouseRegion(
                cursor: SystemMouseCursors.resizeLeftRight,
                child: GestureDetector(
                  onHorizontalDragUpdate: (details) {
                    // Resize logic
                    widget.sendControlResize(state, null);
                  },
                  child: Container(
                    width: 4,
                    height: 20,
                    color: Colors.transparent,
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
