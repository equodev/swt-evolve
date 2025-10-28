import 'package:flutter/material.dart';
import '../gen/treecolumn.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../impl/item_evolve.dart';
import '../impl/widget_config.dart';

class TreeColumnImpl<T extends TreeColumnSwt, V extends VTreeColumn>
    extends ItemImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  // Track column drag state
  bool _isDragging = false;
  double _startDragOffset = 0.0;

  @override
  Widget build(BuildContext context) {
    final String text = state.text ?? "";
    final int? alignment = state.alignment;
    final bool moveable = state.moveable ?? false;
    final bool resizable = state.resizable ?? false;
    final int width = state.width ?? 100;

    final Color textColor = useDarkTheme ? Colors.white70 : Colors.black87;
    final Color bgColor = _isDragging
        ? (useDarkTheme ? const Color(0xFF4A4A4A) : Colors.grey.shade300)
        : (useDarkTheme ? const Color(0xFF333333) : Colors.grey.shade200);

    // Determine text alignment
    MainAxisAlignment textAlignment = MainAxisAlignment.start;
    if (alignment != null) {
      switch (alignment) {
        case SWT.CENTER:
          textAlignment = MainAxisAlignment.center;
          break;
        case SWT.RIGHT:
          textAlignment = MainAxisAlignment.end;
          break;
        default:
          textAlignment = MainAxisAlignment.start;
      }
    }

    return MouseRegion(
      cursor: moveable ? SystemMouseCursors.grab : SystemMouseCursors.click,
      child: GestureDetector(
        onTap: () {
          // Send column selection event
          var e = VEvent();
          e.x = 0;
          e.y = 0;
          e.width = width;
          e.height = 20;
          widget.sendSelectionSelection(state, e);
        },
        onLongPressStart: moveable
            ? (details) {
                setState(() {
                  _isDragging = true;
                  _startDragOffset = details.localPosition.dx;
                });
              }
            : null,
        onLongPressMoveUpdate: moveable
            ? (details) {
                if (_isDragging) {
                  // Track drag movement for column reordering
                  final dragDistance =
                      details.localPosition.dx - _startDragOffset;
                  if (dragDistance.abs() > 10) {
                    // Trigger column move event when significant movement detected
                    var e = VEvent();
                    e.x = details.localPosition.dx.round();
                    e.y = details.localPosition.dy.round();
                    e.width = width;
                    e.height = 20;
                    widget.sendControlMove(state, e);
                  }
                }
              }
            : null,
        onLongPressEnd: moveable
            ? (details) {
                setState(() {
                  _isDragging = false;
                  _startDragOffset = 0.0;
                });
              }
            : null,
        child: Container(
          width: width.toDouble(),
          padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 6.0),
          decoration: BoxDecoration(
            color: bgColor,
            border: Border(
              bottom: BorderSide(
                color: useDarkTheme ? Colors.black38 : Colors.grey.shade400,
                width: 1.0,
              ),
              right: BorderSide(
                color: useDarkTheme ? Colors.black26 : Colors.grey.shade300,
                width: 1.0,
              ),
            ),
          ),
          child: Row(
            mainAxisAlignment: textAlignment,
            children: [
              // Column text with proper alignment
              Expanded(
                child: Text(
                  text,
                  style: TextStyle(
                    color: textColor,
                    fontWeight: FontWeight.w500,
                    fontSize: 14,
                  ),
                  textAlign: _getTextAlign(alignment),
                  overflow: TextOverflow.ellipsis,
                ),
              ),

              // Resize handle (if resizable)
              if (resizable)
                MouseRegion(
                  cursor: SystemMouseCursors.resizeLeftRight,
                  child: GestureDetector(
                    onHorizontalDragStart: (details) {
                      _startDragOffset = details.localPosition.dx;
                    },
                    onHorizontalDragUpdate: (details) {
                      final double delta =
                          details.localPosition.dx - _startDragOffset;
                      final int newWidth =
                          (width + delta.round()).clamp(50, 500);

                      if (newWidth != width) {
                        setState(() {
                          state.width = newWidth;
                        });

                        // Send resize event
                        var e = VEvent();
                        e.x = 0;
                        e.y = 0;
                        e.width = newWidth;
                        e.height = 20;
                        widget.sendControlResize(state, e);
                      }
                    },
                    child: Container(
                      width: 4,
                      height: double.infinity,
                      decoration: BoxDecoration(
                        color: Colors.transparent,
                        border: Border(
                          right: BorderSide(
                            color: useDarkTheme
                                ? Colors.white24
                                : Colors.grey.shade500,
                            width: 1.0,
                          ),
                        ),
                      ),
                      child: Container(
                        margin: const EdgeInsets.symmetric(horizontal: 1.5),
                        color: Colors.transparent,
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  TextAlign _getTextAlign(int? alignment) {
    if (alignment == null) return TextAlign.left;

    switch (alignment) {
      case SWT.CENTER:
        return TextAlign.center;
      case SWT.RIGHT:
        return TextAlign.right;
      default:
        return TextAlign.left;
    }
  }
}
