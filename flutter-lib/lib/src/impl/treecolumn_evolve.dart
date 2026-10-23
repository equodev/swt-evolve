import 'package:flutter/material.dart';
import '../gen/treecolumn.dart';
import '../gen/event.dart';
import '../gen/image.dart';
import '../gen/swt.dart';
import '../impl/item_evolve.dart';
import '../impl/widget_config.dart';
import '../theme/theme_extensions/tree_theme_extension.dart';
import 'tree_evolve.dart';
import 'utils/image_utils.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';

class TreeColumnImpl<T extends TreeColumnSwt, V extends VTreeColumn>
    extends ItemImpl<T, V> {
  bool _isDragging = false;
  Offset? _dragPosition;

  double _calculateLeftPadding(bool isFirstColumn, TreeThemeExtension theme) {
    if (!isFirstColumn) return 0.0;
    return theme.expandIconSize + theme.expandIconSpacing;
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

  void _handleDragStart(DragStartDetails details) {
    setState(() {
      _isDragging = true;
      _dragPosition = details.globalPosition;
    });
  }

  void _handleDragUpdate(DragUpdateDetails details) {
    _dragPosition = details.globalPosition;
  }

  /// A native header drag reorders the columns where it is dropped; Java applies the order and sends
  /// SWT.Move to the columns whose place changed.
  void _handleDragEnd(int Function(Offset globalPosition)? dropIndexAt) {
    final position = _dragPosition;
    setState(() {
      _isDragging = false;
      _dragPosition = null;
    });
    if (position == null || dropIndexAt == null) return;
    widget.sendControlMove(state, VEvent()..index = dropIndexAt(position));
  }

  void _handleDragCancel() {
    setState(() {
      _isDragging = false;
      _dragPosition = null;
    });
  }

  /// The room between a header's image and its text.
  static const double headerImageGap = 4.0;

  Widget _buildHeaderImage(VImage image) {
    final render = ImageUtils.buildVImageAsync(
      image,
      enabled: true,
      useBinaryImage: true,
      renderAsIcon: true,
    );
    return Padding(
      padding: const EdgeInsets.only(right: headerImageGap),
      child: FutureBuilder<Widget?>(
        future: render,
        initialData: ImageUtils.resolvedRender(render),
        builder: (context, snapshot) => snapshot.data ?? const SizedBox.shrink(),
      ),
    );
  }

  Widget _buildColumnContent({
    required BuildContext context,
    required TreeThemeExtension theme,
    required String text,
    required VImage? image,
    required Color textColor,
    required bool uppercase,
    required EdgeInsets adjustedPadding,
    required MainAxisAlignment textAlignment,
    required TextAlign textAlign,
    required Color bgColor,
    required bool hasMultiColumn,
  }) {
    final cell = TreeHeaderCellProvider.of(context);
    TextStyle effectiveStyle = hasMultiColumn
        ? (theme.headerTextStyleWithCols ?? theme.columnTextStyle ?? const TextStyle())
        : (theme.columnTextStyle ?? theme.headerTextStyle ?? const TextStyle())
            .copyWith(color: textColor);
    // Tree.setHeaderForeground is the application's, and wins where its colours are honoured.
    final Color headerColor = getForegroundColor(
      context: context,
      foreground: cell?.foreground,
      defaultColor: effectiveStyle.color ?? textColor,
      minContrast: theme.minTextContrast,
      background: bgColor,
    );
    effectiveStyle = effectiveStyle.copyWith(color: headerColor);
    final int sortDirection = cell?.sortDirection ?? SWT.NONE;
    return Container(
      padding: adjustedPadding,
      decoration: BoxDecoration(color: bgColor),
      child: Row(
        mainAxisAlignment: textAlignment,
        children: [
          if (image != null) _buildHeaderImage(image),
          Expanded(
            child: Text(
              uppercase ? text.toUpperCase() : text,
              style: effectiveStyle,
              textAlign: textAlign,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
          if (sortDirection == SWT.UP || sortDirection == SWT.DOWN)
            Icon(
              sortDirection == SWT.UP ? Icons.keyboard_arrow_up : Icons.keyboard_arrow_down,
              size: (effectiveStyle.fontSize ?? 12) + 2,
              color: headerColor,
            ),
        ],
      ),
    );
  }

  Widget _buildColumnBorder({required TreeThemeExtension theme}) {
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

    final bool hasMultiColumn = TreeHasMultiColumnProvider.of(context);
    final int? columnIndex = TreeColumnIndexProvider.of(context);
    final bool isFirstColumn = columnIndex == 0;
    final bool linesVisible = TreeLinesVisibleProvider.of(context);
    final bool isLastColumn = TreeColumnIsLastProvider.of(context);

    // A single & marks the header's mnemonic and is not drawn; && draws one.
    final String text = stripAccelerators(state.text);
    final int? alignment = state.alignment;
    final bool moveable = state.moveable ?? false;
    final int width = state.width ?? widgetTheme.columnDefaultWidth.round();

    final Color textColor = hasMultiColumn
        ? widgetTheme.columnTextColorWithCols
        : widgetTheme.columnTextColor;
    final Color bgColor = _isDragging
        ? widgetTheme.columnDraggingBackgroundColor
        : Colors.transparent;

    final double leftPadding = _calculateLeftPadding(
      isFirstColumn,
      widgetTheme,
    );
    final textAlign = getTextAlignFromStyle(alignment ?? 0, TextAlign.left);
    final textAlignment = getMainAxisAlignmentFromTextAlign(
      textAlign,
      MainAxisAlignment.start,
    );

    const double extraPadding = 4.0;
    EdgeInsets adjustedPadding = adjustPaddingForAlignment(
      basePadding: widgetTheme.columnPadding,
      alignment: alignment,
      leftPadding: leftPadding,
      extraPadding: extraPadding,
    );
    if (!isLastColumn) {
      adjustedPadding = adjustedPadding.copyWith(
        right: adjustedPadding.right + widgetTheme.columnDividerGap,
      );
    }

    return tagSemantics(MouseRegion(
      cursor: moveable ? SystemMouseCursors.grab : SystemMouseCursors.click,
      child: GestureDetector(
        onTap: () {
          final e = _createEvent(theme: widgetTheme, width: width);
          widget.sendSelectionSelection(state, e);
        },
        onHorizontalDragStart: moveable ? _handleDragStart : null,
        onHorizontalDragUpdate: moveable ? _handleDragUpdate : null,
        onHorizontalDragEnd: moveable
            ? (_) => _handleDragEnd(TreeHeaderCellProvider.of(context)?.dropIndexAt)
            : null,
        onHorizontalDragCancel: moveable ? _handleDragCancel : null,
        child: Stack(
          children: [
            _buildColumnContent(
              context: context,
              theme: widgetTheme,
              text: text,
              image: state.image,
              textColor: textColor,
              uppercase: hasMultiColumn,
              adjustedPadding: adjustedPadding,
              textAlignment: textAlignment,
              textAlign: textAlign,
              bgColor: bgColor,
              hasMultiColumn: hasMultiColumn,
            ),
            if (linesVisible && !isLastColumn && !hasMultiColumn)
              _buildColumnBorder(theme: widgetTheme),
          ],
        ),
      ),
    ));
  }
}
