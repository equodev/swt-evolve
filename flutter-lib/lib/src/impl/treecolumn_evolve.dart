import 'package:flutter/material.dart';
import '../gen/treecolumn.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../impl/item_evolve.dart';
import '../impl/widget_config.dart';
import '../theme/theme_extensions/tree_theme_extension.dart';
import 'tree_evolve.dart';
import 'utils/widget_utils.dart';

class TreeColumnImpl<T extends TreeColumnSwt, V extends VTreeColumn>
    extends ItemImpl<T, V> {

  bool _isDragging = false;
  double _startDragOffset = 0.0;

  double _calculateLeftPadding(bool isFirstColumn, TreeThemeExtension theme) {
    if (!isFirstColumn) return 0.0;
    return theme.expandIconSize + 
           theme.expandIconSpacing + 
           theme.itemIconSize + 
           theme.itemIconSpacing;
  }

  VEvent _createEvent({
    required TreeThemeExtension theme,
    required int width,
    int? x,
    int? y,
  }) {
    var e = VEvent();
    e.x = x ?? theme.eventDefaultX;
    e.y = y ?? theme.eventDefaultY;
    e.width = width;
    e.height = theme.eventDefaultHeight.round();
    return e;
  }

  void _handleDragStart(LongPressStartDetails details) {
    setState(() {
      _isDragging = true;
      _startDragOffset = details.localPosition.dx;
    });
  }

  void _handleDragUpdate(
    LongPressMoveUpdateDetails details,
    TreeThemeExtension theme,
    int width,
  ) {
    if (!_isDragging) return;
    
    final dragDistance = details.localPosition.dx - _startDragOffset;
    if (dragDistance.abs() > theme.columnDragThreshold) {
      final e = _createEvent(
        theme: theme,
        width: width,
        x: details.localPosition.dx.round(),
        y: details.localPosition.dy.round(),
      );
      widget.sendControlMove(state, e);
    }
  }

  void _handleDragEnd() {
    setState(() {
      _isDragging = false;
      _startDragOffset = 0.0;
    });
  }

  Widget _buildColumnContent({
    required TreeThemeExtension theme,
    required String text,
    required Color textColor,
    required EdgeInsets adjustedPadding,
    required MainAxisAlignment textAlignment,
    required TextAlign textAlign,
    required Color bgColor,
  }) {
    return Container(
      padding: adjustedPadding,
      decoration: BoxDecoration(color: bgColor),
      child: Row(
        mainAxisAlignment: textAlignment,
        children: [
          Expanded(
            child: Text(
              text,
              style: (theme.columnTextStyle ?? const TextStyle()).copyWith(
                color: textColor,
              ),
              textAlign: textAlign,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildColumnBorder({
    required TreeThemeExtension theme,
  }) {
    return Positioned(
      right: 0,
      top: theme.headerColumnBorderVerticalMargin,
      bottom: theme.headerColumnBorderVerticalMargin,
      child: Container(
        width: theme.columnBorderWidth,
        color: theme.columnRightBorderColor,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TreeThemeExtension>()!;
    
    final int? columnIndex = TreeColumnIndexProvider.of(context);
    final bool isFirstColumn = columnIndex == 0;
    final bool linesVisible = TreeLinesVisibleProvider.of(context);
    final bool isLastColumn = TreeColumnIsLastProvider.of(context);
    
    final String text = state.text ?? "";
    final int? alignment = state.alignment;
    final bool moveable = state.moveable ?? false;
    final int width = state.width ?? widgetTheme.columnDefaultWidth.round();

    final Color textColor = widgetTheme.columnTextColor;
    final Color bgColor = _isDragging
        ? widgetTheme.columnDraggingBackgroundColor
        : Colors.transparent;
        
    final double leftPadding = _calculateLeftPadding(isFirstColumn, widgetTheme);
    final textAlign = getTextAlignFromStyle(alignment ?? 0, TextAlign.left);
    final textAlignment = getMainAxisAlignmentFromTextAlign(textAlign, MainAxisAlignment.start);
    
    const double extraPadding = 4.0;
    final adjustedPadding = adjustPaddingForAlignment(
      basePadding: widgetTheme.columnPadding,
      alignment: alignment,
      leftPadding: leftPadding,
      extraPadding: extraPadding,
    );

    return MouseRegion(
      cursor: moveable ? SystemMouseCursors.grab : SystemMouseCursors.click,
      child: GestureDetector(
        onTap: () {
          final e = _createEvent(theme: widgetTheme, width: width);
          widget.sendSelectionSelection(state, e);
        },
        onLongPressStart: moveable ? _handleDragStart : null,
        onLongPressMoveUpdate: moveable
            ? (details) => _handleDragUpdate(details, widgetTheme, width)
            : null,
        onLongPressEnd: moveable ? (_) => _handleDragEnd() : null,
        child: Stack(
          children: [
            _buildColumnContent(
              theme: widgetTheme,
              text: text,
              textColor: textColor,
              adjustedPadding: adjustedPadding,
              textAlignment: textAlignment,
              textAlign: textAlign,
              bgColor: bgColor,
            ),
            if (linesVisible && !isLastColumn)
              _buildColumnBorder(theme: widgetTheme),
          ],
        ),
      ),
    );
  }

}
