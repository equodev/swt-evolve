import 'package:flutter/material.dart';
import '../gen/treecolumn.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../impl/item_evolve.dart';
import '../impl/widget_config.dart';
import '../theme/tree_theme_extension.dart';
import 'tree_evolve.dart';

class TreeColumnImpl<T extends TreeColumnSwt, V extends VTreeColumn>
    extends ItemImpl<T, V> {

  bool _isDragging = false;
  double _startDragOffset = 0.0;

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final widgetTheme = Theme.of(context).extension<TreeThemeExtension>();
    
    final int? columnIndex = TreeColumnIndexProvider.of(context);
    final bool isFirstColumn = columnIndex == 0;
    final bool linesVisible = TreeLinesVisibleProvider.of(context);
    final bool isLastColumn = TreeColumnIsLastProvider.of(context);
    
    final String text = state.text ?? "";
    final int? alignment = state.alignment;
    final bool moveable = state.moveable ?? false;
    
    final int width = state.width ?? widgetTheme!.columnDefaultWidth.round();

    final Color textColor = widgetTheme!.columnTextColor;
    final Color bgColor = _isDragging
        ? widgetTheme.columnDraggingBackgroundColor
        : Colors.transparent;
        
    final double leftPadding = isFirstColumn 
        ? widgetTheme!.expandIconSize + 
          widgetTheme.expandIconSpacing + 
          widgetTheme.itemIconSize + 
          widgetTheme.itemIconSpacing
        : 0.0;
    
    final EdgeInsets basePadding = widgetTheme.columnPadding;
    final double extraPadding = 4.0;
    final EdgeInsets adjustedPadding;
    
    if (alignment == null || alignment == SWT.LEFT) {
      adjustedPadding = EdgeInsets.only(
        left: basePadding.left + leftPadding + extraPadding,
        right: basePadding.right,
        top: basePadding.top,
        bottom: basePadding.bottom,
      );
    } else if (alignment == SWT.RIGHT) {
      adjustedPadding = EdgeInsets.only(
        left: basePadding.left + leftPadding,
        right: basePadding.right + extraPadding,
        top: basePadding.top,
        bottom: basePadding.bottom,
      );
    } else {
      adjustedPadding = EdgeInsets.only(
        left: basePadding.left + leftPadding,
        right: basePadding.right,
        top: basePadding.top,
        bottom: basePadding.bottom,
      );
    }

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
          var e = VEvent();
          e.x = widgetTheme!.eventDefaultX;
          e.y = widgetTheme.eventDefaultY;
          e.width = width;
          e.height = widgetTheme.eventDefaultHeight.round();
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
                  final dragDistance =
                      details.localPosition.dx - _startDragOffset;
                  final threshold = widgetTheme!.columnDragThreshold;
                  if (dragDistance.abs() > threshold) {
                    var e = VEvent();
                    e.x = details.localPosition.dx.round();
                    e.y = details.localPosition.dy.round();
                    e.width = width;
                    e.height = widgetTheme.eventDefaultHeight.round();
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
        child: Stack(
          children: [
            Container(
              padding: adjustedPadding,
              decoration: BoxDecoration(
                color: bgColor,
              ),
              child: Row(
                mainAxisAlignment: textAlignment,
                children: [
                  Expanded(
                    child: Text(
                      text,
                      style: TextStyle(
                        color: textColor,
                        fontWeight: widgetTheme!.columnFontWeight,
                        fontSize: widgetTheme.columnFontSize,
                      ),
                      textAlign: _getTextAlign(alignment),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ),
            if (linesVisible && !isLastColumn)
              Positioned(
                right: 0,
                top: widgetTheme!.headerColumnBorderVerticalMargin,
                bottom: widgetTheme!.headerColumnBorderVerticalMargin,
                child: Container(
                  width: widgetTheme.columnBorderWidth,
                  color: widgetTheme.columnRightBorderColor,
                ),
              ),
          ],
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
