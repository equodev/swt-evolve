import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, TargetPlatform;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:swtflutter/src/impl/tree_evolve.dart';
import '../comm/v_registry.dart';
import '../gen/treeitem.dart';
import '../gen/treecolumn.dart';
import '../gen/image.dart';
import '../impl/item_evolve.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import 'color_utils.dart';
import 'utils/image_utils.dart';
import '../theme/theme_extensions/tree_theme_extension.dart';
import '../theme/theme_settings/tree_theme_settings.dart';
import '../gen/button.dart';
import 'utils/double_tap_detector.dart';
import 'utils/dnd_utils.dart';
import 'utils/widget_utils.dart';
import 'owner_draw_overlay.dart';

class TreeItemImpl<T extends TreeItemSwt, V extends VTreeItem>
    extends ItemImpl<T, V> {
  TreeItemContext? _context;

  bool _isHovered = false;
  Offset? _lastTapPosition;
  Offset? _lastTapGlobalPosition;
  final DoubleTapDetector _rowTap = DoubleTapDetector();

  @override
  void setValue(V value) {
    super.setValue(value);
    // The Tree lays every visible row out from one flattened list, so what is under this item is
    // the Tree's to redraw.
    final change = lastChange;
    if (change == null || change.touches('expanded') || change.touches('items')) {
      _context?.treeImpl?.itemStructureChanged();
    }
  }

  Widget _wrapItemForDrag(Widget row) {
    final ctx = _context;
    if (ctx == null) return row;
    final parentTree = ctx.parentTree;
    final parentTreeValue = ctx.parentTreeValue;

    final treeImpl = ctx.treeImpl;
    final content = treeImpl == null
        ? row
        : ValueListenableBuilder<int?>(
            valueListenable: treeImpl.dragHover.notifier,
            builder: (context, hoveredId, child) => hoveredId == state.id
                ? DecoratedBox(
                    decoration: const BoxDecoration(
                      border: Border(top: BorderSide(color: Colors.blue, width: 2)),
                    ),
                    child: child,
                  )
                : child!,
            child: row,
          );

    return wrapDraggable<DndDragPayload>(
      child: content,
      data: DndDragPayload(sourceControlId: parentTreeValue.id, itemId: state.id),
      widget: parentTree,
      state: parentTreeValue,
      dragDetectEvent: () => ctx.treeImpl?.dragDetectPayload() ?? VEvent(),
      onDragStarted: () {
        ctx.treeImpl?.handleTreeItemSelection(state.id, notifyJava: false);
        final e = _createEvent();
        e.count = 1;
        e.button = 1;
        parentTree.sendSelectionSelection(parentTreeValue, e);
      },
    );
  }

  void _sendStartEditing() {
    if (_context?.parentTree != null && _context?.parentTreeValue != null) {
      final e = _createEvent();
      e.x = _lastTapPosition?.dx.round();

      // Calculate the absolute Y position within the Tree
      final widgetTheme = Theme.of(context).extension<TreeThemeExtension>();
      if (widgetTheme != null && _context?.treeImpl != null) {
        final itemIndex = _context!.treeImpl!.findItemIndex(state.id);
        final hasMultiColumn =
            (_context!.treeImpl!.getTreeColumns().length) > 1;
        final itemHeight = hasMultiColumn
            ? widgetTheme.itemHeightWithCols
            : (widgetTheme.itemHeight + widgetTheme.itemPadding.vertical);

        // Calculate header offset
        double headerOffset = 0.0;
        final columns = _context!.treeImpl!.getTreeColumns();
        final headerVisible = _context!.parentTreeValue.headerVisible;
        if ((headerVisible == true || columns.isNotEmpty) &&
            columns.isNotEmpty) {
          headerOffset = hasMultiColumn
              ? widgetTheme.headerHeightWithCols
              : widgetTheme.headerHeight;
        }

        // Calculate absolute Y: header + (itemIndex * itemHeight) + localY
        final absoluteY =
            headerOffset +
            (itemIndex * itemHeight) +
            (_lastTapPosition?.dy ?? 0);
        e.y = absoluteY.round();
      } else {
        e.y = _lastTapPosition?.dy.round();
      }

      e.index = state.id;
      e.count = 2;
      e.button = 1;
      _context!.parentTree.sendEvent(
        _context!.parentTreeValue,
        "Mouse/MouseDoubleClick",
        e,
      );
    }
  }

  void _toggleExpand(bool expanded) {
    final treeImpl = _context?.treeImpl;
    if (treeImpl == null) return;
    final e = _createEvent();
    treeImpl.setItemExpanded(state.id, !expanded);
    if (expanded) {
      _context!.parentTree.sendTreeCollapse(_context!.parentTreeValue, e);
    } else {
      _context!.parentTree.sendTreeExpand(_context!.parentTreeValue, e);
    }
  }

  VEvent _createEvent({int? detail, int? stateMask}) {
    final widgetTheme = Theme.of(context).extension<TreeThemeExtension>();
    var e = VEvent();
    if (_context?.treeImpl != null && widgetTheme != null) {
      e.index = _context!.treeImpl!.findItemIndex(state.id);
      e.detail = detail ?? widgetTheme.eventDefaultDetail;
      e.x = widgetTheme.eventDefaultX;
      e.y = widgetTheme.eventDefaultY;
      e.width = widgetTheme.eventDefaultWidth.round();
      e.height = widgetTheme.eventDefaultHeight.round();
      if (stateMask != null) {
        e.stateMask = stateMask;
      }
    }
    return e;
  }

  void _secondaryActivate({required int button}) {
    final bool enabled = _context?.parentTreeValue.enabled ?? true;
    final bool selected =
        _context?.treeImpl?.isItemSelected(state.id) ?? false;
    if (!enabled) return;
    if (!selected) {
      _context?.treeImpl?.handleTreeItemSelection(state.id, notifyJava: false);
      final e = _createEvent();
      e.count = 1;
      e.button = button;
      _context?.parentTree.sendSelectionSelection(
        _context!.parentTreeValue,
        e,
      );
    }
    if (_lastTapGlobalPosition != null) {
      _context?.treeImpl?.openContextMenu(_lastTapGlobalPosition!);
    }
  }

  @override
  Widget build(BuildContext context) {
    _context = TreeItemContext.of(context);

    final widgetTheme = Theme.of(context).extension<TreeThemeExtension>();
    if (widgetTheme == null) {
      return const SizedBox.shrink();
    }

    if (_context == null) {
      // Standalone mode used by the measure tool — render as a single row without tree context.
      final textColor = getTreeItemTextColor(state, widgetTheme, false, true,
          parentForeground: ParentForegroundScope.of(context));
      final image = _treeColumnImage() ?? state.image;
      return Container(
        padding: widgetTheme.itemPadding,
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (image != null)
              Container(
                margin: EdgeInsets.only(right: widgetTheme.itemIconSpacing),
                child: _buildItemIcon(widgetTheme, true, false, false, false, image),
              ),
            Flexible(
              child: _buildCellText(
                context: context,
                text: state.text ?? "",
                textColor: textColor,
                theme: widgetTheme,
                columnAlignment: null,
                cellPadding: EdgeInsets.zero,
                columnIndex: 0,
              ),
            ),
          ],
        ),
      );
    }

    return tagSemantics(buildTreeItemContent(context));
  }

  /// The icon for the tree column, which is the only column a tree row draws one in.
  VImage? _treeColumnImage() {
    final images = state.images;
    if (images == null || images.isEmpty) return null;
    return images[0];
  }

  Widget buildTreeItemContent(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TreeThemeExtension>();
    if (widgetTheme == null) {
      return Text(state.text ?? "");
    }

    final String text = state.text ?? "";
    final List<String?>? texts = state.texts;
    final bool expanded = state.expanded ?? false;
    final bool hasChildren = state.hasChildItems;
    final bool isCheckMode = _context?.isCheckMode ?? false;
    final bool checked = state.checked ?? false;
    final bool grayed = state.grayed ?? false;
    final int level = _context?.level ?? 0;
    // An owner-drawn cell's icon only ever exists as what the SWT.PaintItem listener drew, which
    // Java reports in images[0]; a cell that set one the ordinary way reports it in both.
    final VImage? image = _treeColumnImage() ?? state.image;

    final bool selected = _context?.treeImpl?.isItemSelected(state.id) ?? false;
    final bool enabled = _context?.parentTreeValue.enabled ?? true;
    final bool nextItemSelected =
        _context?.treeImpl?.isNextItemSelected(state.id) ?? false;

    final textColor = getTreeItemTextColor(
      state,
      widgetTheme,
      selected,
      enabled,
      parentForeground: ParentForegroundScope.of(context),
    );
    final bgColor = getTreeItemBackgroundColor(
      state,
      widgetTheme,
      selected,
      _isHovered && !selected,
      enabled,
    );

    double? totalTreeWidth = _context?.treeWidth;
    final effectiveWidths = TreeEffectiveColumnWidthsProvider.of(context);
    if (totalTreeWidth == null) {
      if (effectiveWidths != null && effectiveWidths.isNotEmpty) {
        final prefix =
            widgetTheme.expandIconSize + widgetTheme.expandIconSpacing;
        totalTreeWidth = prefix + effectiveWidths.reduce((a, b) => a + b);
      } else {
        final columns = _context?.treeImpl?.getTreeColumns() ?? [];
        if (columns.isNotEmpty) {
          double calculatedWidth = 0.0;
          for (final column in columns) {
            calculatedWidth +=
                (column.width ?? widgetTheme.columnDefaultWidth.round())
                    .toDouble();
          }
          totalTreeWidth =
              widgetTheme.expandIconSize +
              widgetTheme.expandIconSpacing +
              calculatedWidth;
        }
      }
    }

    final bool hasMultiColumn =
        (_context?.treeImpl?.getTreeColumns().length ?? 0) > 1;
    final double effectiveItemHeight = hasMultiColumn
        ? widgetTheme.itemHeightWithCols
        : (widgetTheme.itemHeight + widgetTheme.itemPadding.vertical);

    return SizedBox(
      width: totalTreeWidth ?? double.infinity,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _wrapItemForDrag(
            _withOwnerDrawOverlay(_buildItemRow(
              context: context,
              widgetTheme: widgetTheme,
              texts: texts,
              text: text,
              textColor: textColor,
              level: level,
              hasChildren: hasChildren,
              expanded: expanded,
              isCheckMode: isCheckMode,
              checked: checked,
              grayed: grayed,
              enabled: enabled,
              selected: selected,
              image: image,
              bgColor: state.paintedTexts != null ? Colors.transparent : bgColor,
              nextItemSelected: nextItemSelected,
              hasMultiColumn: hasMultiColumn,
              effectiveItemHeight: effectiveItemHeight,
            ),
            background: BoxDecoration(
              color: bgColor,
              borderRadius: hasMultiColumn
                  ? null
                  : BorderRadius.circular(widgetTheme.borderRadius),
            )),
          ),
          if (expanded && hasChildren && (_context?.renderChildItems ?? true))
            ...buildChildItems(),
        ],
      ),
    );
  }

  /// Lays what an owner-drawing Tree paints into this row under the row, and never over its
  /// children.
  ///
  /// Under, not over: the overlay replays the row's SWT.EraseItem drawing, which is the cell
  /// background, and an app that leaves SWT.FOREGROUND set still expects SWT to draw the item's own
  /// text on top of it. Laid over the row, that background hid every cell whose text the row itself
  /// paints. The row's own [background] (hover, selection) goes under the overlay, not with the row,
  /// or it covers the overlay's drawing.
  Widget _withOwnerDrawOverlay(Widget row, {required BoxDecoration background}) {
    // Always a Stack, so the row is not remounted when the overlay arrives.
    return Stack(
      children: [
        if (state.paintedTexts != null)
          Positioned.fill(
            key: ValueKey(state.id),
            child: DecoratedBox(
              decoration: background,
              child: OwnerDrawnRowOverlay(itemId: state.id),
            ),
          ),
        row,
      ],
    );
  }

  List<Widget> buildChildItems() {
    final List<VTreeItem> childItems =
        state.items
            ?.whereType<VTreeItem>()
            .where(
              (childItem) =>
                  (childItem.text != null && childItem.text!.isNotEmpty) ||
                  (childItem.texts != null &&
                      childItem.texts!.any((text) => text?.isNotEmpty == true)) ||
                  childItem.hasChildItems,
            )
            .toList() ??
        [];

    return childItems.map((childItem) {
      return TreeItemContextProvider(
        level: (_context?.level ?? 0) + 1,
        isCheckMode: _context?.isCheckMode ?? false,
        parentTree: _context!.parentTree,
        parentTreeValue: _context!.parentTreeValue,
        treeImpl: _context?.treeImpl,
        treeFont: _context?.treeFont,
        treeWidth: _context?.treeWidth,
        child: TreeItemSwt(
          value: childItem,
          key: ValueKey('tree_child_item_${childItem.id}'),
        ),
      );
    }).toList();
  }

  Widget _buildRowWithColumns(
    BuildContext context,
    List<String?>? texts,
    String text,
    Color textColor,
    TreeThemeExtension widgetTheme,
    int level,
    bool hasChildren,
    bool expanded,
    bool isCheckMode,
    bool checked,
    bool grayed,
    bool enabled,
    bool selected,
    VImage? image,
    bool hasMultiColumn,
  ) {
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];
    final bool hasMultipleColumns = columns.isNotEmpty;

    if (!hasMultipleColumns) {
      return Row(
        children: [
          _buildItemPrefix(
            theme: widgetTheme,
            level: level,
            hasChildren: hasChildren,
            expanded: expanded,
            isCheckMode: isCheckMode,
            checked: checked,
            grayed: grayed,
            enabled: enabled,
            selected: selected,
            image: image,
          ),
          Expanded(
            child: _buildTextContent(
              context,
              texts,
              text,
              textColor,
              widgetTheme,
            ),
          ),
        ],
      );
    }

    final firstColumn = columns[0];
    final effectiveWidths = TreeEffectiveColumnWidthsProvider.of(context);
    final double firstColumnWidth =
        effectiveWidths != null && effectiveWidths.isNotEmpty
        ? effectiveWidths[0]
        : (firstColumn.width ?? widgetTheme.columnDefaultWidth.round())
              .toDouble();

    final String firstColumnText = text.isNotEmpty
        ? text
        : (texts?.isNotEmpty == true ? texts![0] ?? "" : '');

    if (effectiveWidths != null && effectiveWidths.length == columns.length) {
      return Row(
        children: [
          SizedBox(
            width: firstColumnWidth,
            child: Row(
              children: [
                _buildItemPrefix(
                  theme: widgetTheme,
                  level: level,
                  hasChildren: hasChildren,
                  expanded: expanded,
                  isCheckMode: isCheckMode,
                  checked: checked,
                  grayed: grayed,
                  enabled: enabled,
                  selected: selected,
                  image: image,
                  hasMultiColumn: hasMultiColumn,
                ),
                Expanded(
                  child: _buildCellText(
                    context: context,
                    text: firstColumnText,
                    textColor: textColor,
                    theme: widgetTheme,
                    columnAlignment: firstColumn.alignment,
                    cellPadding: widgetTheme.cellMultiColumnPadding,
                    columnIndex: 0,
                    hasMultiColumn: hasMultiColumn,
                  ),
                ),
              ],
            ),
          ),
          ...columns.skip(1).toList().asMap().entries.map<Widget>((entry) {
            final int columnIndex = entry.key + 1;
            final column = entry.value;
            final String columnText = _getColumnText(texts, columnIndex);
            final double w = effectiveWidths[columnIndex];
            return SizedBox(
              width: w,
              child: _buildCellText(
                context: context,
                text: columnText,
                textColor: textColor,
                theme: widgetTheme,
                columnAlignment: column.alignment,
                cellPadding: widgetTheme.cellMultiColumnPadding,
                columnIndex: columnIndex,
                hasMultiColumn: hasMultiColumn,
              ),
            );
          }),
        ],
      );
    }

    return Row(
      children: [
        SizedBox(
          width: firstColumnWidth,
          child: Row(
            children: [
              _buildItemPrefix(
                theme: widgetTheme,
                level: level,
                hasChildren: hasChildren,
                expanded: expanded,
                isCheckMode: isCheckMode,
                checked: checked,
                grayed: grayed,
                enabled: enabled,
                selected: selected,
                image: image,
                hasMultiColumn: hasMultiColumn,
              ),
              Expanded(
                child: _buildCellText(
                  context: context,
                  text: firstColumnText,
                  textColor: textColor,
                  theme: widgetTheme,
                  columnAlignment: firstColumn.alignment,
                  cellPadding: widgetTheme.cellMultiColumnPadding,
                  columnIndex: 0,
                  hasMultiColumn: hasMultiColumn,
                ),
              ),
            ],
          ),
        ),
        Expanded(
          child: _buildOtherColumns(
            context,
            texts,
            textColor,
            widgetTheme,
            columns,
            hasMultiColumn,
          ),
        ),
      ],
    );
  }

  Widget? _buildItemIcon(
    TreeThemeExtension? widgetTheme,
    bool enabled,
    bool selected,
    bool hasChildren,
    bool expanded,
    VImage? image,
  ) {
    // Only show an icon if explicitly provided
    if (image == null) {
      return null;
    }

    final Color iconColor = _getItemIconColor(widgetTheme!, enabled, selected);
    final render = ImageUtils.buildVImageAsync(
      image,
      size: widgetTheme.itemIconSize,
      color: iconColor,
      enabled: enabled,
      constraints: BoxConstraints(
        minWidth: widgetTheme.itemIconSize,
        minHeight: widgetTheme.itemIconSize,
        maxWidth: widgetTheme.itemIconSize,
        maxHeight: widgetTheme.itemIconSize,
      ),
      useBinaryImage: true,
      renderAsIcon: true,
    );

    // No key: a new colour or image swaps the future in place, and the icon already shown stays
    // until the new one is ready instead of the row blinking empty for a frame.
    return FutureBuilder<Widget?>(
      future: render,
      initialData: ImageUtils.resolvedRender(render),
      builder: (context, snapshot) {
        if (snapshot.data != null) {
          return snapshot.data!;
        }
        // Return null if image fails to load
        return SizedBox(
          width: widgetTheme.itemIconSize,
          height: widgetTheme.itemIconSize,
        );
      },
    );
  }

  Color _getItemIconColor(
    TreeThemeExtension theme,
    bool enabled,
    bool selected,
  ) {
    if (!enabled) return theme.itemIconDisabledColor;
    return selected ? theme.itemIconSelectedColor : theme.itemIconColor;
  }

  Widget _buildOtherColumns(
    BuildContext context,
    List<String?>? texts,
    Color textColor,
    TreeThemeExtension widgetTheme,
    List<VTreeColumn> columns,
    bool hasMultiColumn,
  ) {
    return Row(
      children: columns.skip(1).toList().asMap().entries.map<Widget>((entry) {
        final int columnIndex = entry.key + 1;
        final column = entry.value;
        final String columnText = _getColumnText(texts, columnIndex);

        return _buildColumnCell(
          context: context,
          text: columnText,
          textColor: textColor,
          theme: widgetTheme,
          column: column,
          columnIndex: columnIndex,
          hasMultiColumn: hasMultiColumn,
        );
      }).toList(),
    );
  }

  String _getColumnText(List<String?>? texts, int columnIndex) {
    if (texts != null && columnIndex < texts.length) {
      return texts[columnIndex] ?? '';
    }
    return '';
  }

  Widget _buildColumnCell({
    required BuildContext context,
    required String text,
    required Color textColor,
    required TreeThemeExtension theme,
    required VTreeColumn column,
    required int columnIndex,
    required bool hasMultiColumn,
  }) {
    final effectiveWidths = TreeEffectiveColumnWidthsProvider.of(context);
    final double columnWidth =
        effectiveWidths != null && columnIndex < effectiveWidths.length
        ? effectiveWidths[columnIndex]
        : (column.width ?? theme.columnDefaultWidth.round()).toDouble();
    return SizedBox(
      width: columnWidth,
      child: _buildCellText(
        context: context,
        text: text,
        textColor: textColor,
        theme: theme,
        columnAlignment: column.alignment,
        cellPadding: theme.cellMultiColumnPadding,
        columnIndex: columnIndex,
        hasMultiColumn: hasMultiColumn,
      ),
    );
  }

  Widget _buildTextContent(
    BuildContext context,
    List<String?>? texts,
    String text,
    Color textColor,
    TreeThemeExtension widgetTheme,
  ) {
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];

    if (columns.isEmpty) {
      final displayText = text.isNotEmpty
          ? text
          : (texts?.isNotEmpty == true ? texts![0] ?? "" : '');
      return _buildCellText(
        context: context,
        text: displayText,
        textColor: textColor,
        theme: widgetTheme,
        columnAlignment: null,
        cellPadding: widgetTheme.cellPadding,
        columnIndex: 0,
      );
    }

    return _buildTextContentWithLines(
      context,
      texts,
      text,
      textColor,
      widgetTheme,
    );
  }

  Widget _buildTextContentWithLines(
    BuildContext context,
    List<String?>? texts,
    String text,
    Color textColor,
    TreeThemeExtension widgetTheme,
  ) {
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];
    final hasMultiColumn = (columns.length) > 1;

    return Row(
      children: columns.asMap().entries.map<Widget>((entry) {
        final int columnIndex = entry.key;
        final column = entry.value;
        final String columnText = columnIndex == 0
            ? (text.isNotEmpty
                  ? text
                  : (texts?.isNotEmpty == true ? texts![0] ?? "" : ''))
            : _getColumnText(texts, columnIndex);

        return _buildColumnCell(
          context: context,
          text: columnText,
          textColor: textColor,
          theme: widgetTheme,
          column: column,
          columnIndex: columnIndex,
          hasMultiColumn: hasMultiColumn,
        );
      }).toList(),
    );
  }

  Widget _buildExpander({
    required TreeThemeExtension theme,
    required bool hasChildren,
    required bool expanded,
    required bool enabled,
  }) {
    if (!hasChildren) {
      return SizedBox(
        width: theme.expandIconSize,
        height: theme.expandIconSize,
      );
    }

    final Color arrowColor = enabled
        ? theme.expandIconColor
        : theme.expandIconDisabledColor;

    // The arrow carries no gesture of its own: the row's GestureDetector owns the tap
    // and decides by x-position whether it is an expand or a selection (see
    // _buildItemRow). A single handler guarantees the "expander area" the row abdicates
    // and the area that actually toggles are the same rectangle — when they were two
    // widgets the icon only covered 12x12 of a 34px-tall row, so taps in the gap were
    // swallowed by neither and did nothing at all.
    return MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      child: Icon(
        expanded ? Icons.keyboard_arrow_down : Icons.keyboard_arrow_right,
        size: theme.expandIconSize,
        color: arrowColor,
      ),
    );
  }

  Widget? _buildCheckbox({
    required TreeThemeExtension theme,
    required bool isCheckMode,
    required bool checked,
    required bool grayed,
    required bool enabled,
  }) {
    if (!isCheckMode) return null;

    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () {},
      child: Container(
        margin: EdgeInsets.only(right: theme.checkboxSpacing),
        child: _CheckboxButtonWrapper(
          key: ValueKey('checkbox_${state.id}'),
          ownerId: state.id,
          checked: checked,
          grayed: grayed,
          enabled: enabled,
          onChanged: () {
            if (!enabled) return;
            _context?.treeImpl?.handleTreeItemSelection(state.id, notifyJava: false);
            final e = _createEvent(detail: SWT.CHECK);
            _context?.parentTree.sendSelectionSelection(
              _context!.parentTreeValue,
              e,
            );
          },
        ),
      ),
    );
  }

  Widget _buildItemPrefix({
    required TreeThemeExtension theme,
    required int level,
    required bool hasChildren,
    required bool expanded,
    required bool isCheckMode,
    required bool checked,
    required bool grayed,
    required bool enabled,
    required bool selected,
    required VImage? image,
    bool hasMultiColumn = false,
  }) {
    final checkbox = _buildCheckbox(
      theme: theme,
      isCheckMode: isCheckMode,
      checked: checked,
      grayed: grayed,
      enabled: enabled,
    );

    // A columned tree still draws the item's image in the tree column -- that is where native SWT
    // puts it, next to the twistie. _buildItemIcon already yields null when there is no image, so
    // the column count has no say in it.
    final icon = _buildItemIcon(
      theme,
      enabled,
      selected,
      hasChildren,
      expanded,
      image,
    );

    return Row(
      children: [
        SizedBox(width: theme.itemIndent * level),
        _buildExpander(
          theme: theme,
          hasChildren: hasChildren,
          expanded: expanded,
          enabled: enabled,
        ),
        SizedBox(width: theme.expandIconSpacing),
        if (checkbox != null) checkbox,
        if (icon != null)
          Container(
            margin: EdgeInsets.only(right: theme.itemIconSpacing),
            child: icon,
          ),
      ],
    );
  }

  Border? _buildItemRowBorder({
    required TreeThemeExtension widgetTheme,
    required bool selected,
    required bool nextItemSelected,
    required bool hasMultiColumn,
  }) {
    final rowSeparatorBottom = hasMultiColumn
        ? BorderSide(
            color: widgetTheme.rowSeparatorColorWithCols,
            width: widgetTheme.rowSeparatorWidthWithCols,
          )
        : BorderSide.none;
    final selectedSide = BorderSide(
      color: widgetTheme.itemSelectedBorderColor,
      width: widgetTheme.itemSelectedBorderWidth,
    );
    if (selected) {
      return Border(
        left: selectedSide,
        right: selectedSide,
        top: selectedSide,
        bottom: nextItemSelected
            ? (hasMultiColumn ? rowSeparatorBottom : BorderSide.none)
            : selectedSide,
      );
    }
    if (hasMultiColumn) {
      return Border(bottom: rowSeparatorBottom);
    }
    return null;
  }

  Widget _buildItemRow({
    required BuildContext context,
    required TreeThemeExtension widgetTheme,
    required List<String?>? texts,
    required String text,
    required Color textColor,
    required int level,
    required bool hasChildren,
    required bool expanded,
    required bool isCheckMode,
    required bool checked,
    required bool grayed,
    required bool enabled,
    required bool selected,
    required VImage? image,
    required Color bgColor,
    required bool nextItemSelected,
    required bool hasMultiColumn,
    required double effectiveItemHeight,
  }) {
    // Horizontal band of the expander arrow, in row-local coordinates (the tap position
    // is relative to the row's outer box, so the row padding counts). The band spans the
    // full row height on purpose: the arrow icon is only expandIconSize tall and centred,
    // and anything outside it used to be discarded silently instead of toggling.
    final EdgeInsets rowPadding = hasMultiColumn
        ? widgetTheme.itemPaddingWithCols
        : widgetTheme.itemPadding;
    final double expanderHitLeft =
        rowPadding.left + widgetTheme.itemIndent * level;
    final double expanderHitRight =
        expanderHitLeft + widgetTheme.expandIconSize;

    double? totalTreeWidth = _context?.treeWidth;
    final effectiveWidths = TreeEffectiveColumnWidthsProvider.of(context);
    if (totalTreeWidth == null) {
      if (effectiveWidths != null && effectiveWidths.isNotEmpty) {
        final prefix =
            widgetTheme.expandIconSize + widgetTheme.expandIconSpacing;
        totalTreeWidth = prefix + effectiveWidths.reduce((a, b) => a + b);
      } else {
        final columns = _context?.treeImpl?.getTreeColumns() ?? [];
        if (columns.isNotEmpty) {
          double calculatedWidth = 0.0;
          for (final column in columns) {
            calculatedWidth +=
                (column.width ?? widgetTheme.columnDefaultWidth.round())
                    .toDouble();
          }
          totalTreeWidth =
              widgetTheme.expandIconSize +
              widgetTheme.expandIconSpacing +
              calculatedWidth;
        }
      }
    }

    return SizedBox(
      width: totalTreeWidth ?? double.infinity,
      child: MouseRegion(
        cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
        onEnter: (_) {
          setState(() {
            _isHovered = true;
          });
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (!mounted) return;
            _context?.parentTree.sendMouseTrackMouseEnter(
              _context!.parentTreeValue,
              null,
            );
          });
        },
        onExit: (_) {
          setState(() {
            _isHovered = false;
          });
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (!mounted) return;
            _context?.parentTree.sendMouseTrackMouseExit(
              _context!.parentTreeValue,
              null,
            );
          });
        },
        child: GestureDetector(
          behavior: HitTestBehavior.opaque,
          onTapDown: (TapDownDetails details) {
            _lastTapPosition = details.localPosition;
            _lastTapGlobalPosition = details.globalPosition;
          },
          onSecondaryTapDown: (TapDownDetails details) {
            _lastTapPosition = details.localPosition;
            _lastTapGlobalPosition = details.globalPosition;
          },
          onSecondaryTap: () {
            _secondaryActivate(button: 3);
          },
          onTap: () {
            if (!enabled) return;

            // A tap in the expander column toggles this item; the arrow itself carries
            // no gesture, so this band is the single source of truth for what counts as
            // an expand/collapse click — see _buildExpander.
            final Offset? tap = _lastTapPosition;
            if (hasChildren &&
                tap != null &&
                tap.dx >= expanderHitLeft &&
                tap.dx < expanderHitRight) {
              _toggleExpand(expanded);
              return;
            }

            // HardwareKeyboard, not the deprecated RawKeyboard: on the web a modifier
            // released while the page has no focus (Cmd+Tab, any browser shortcut) leaves
            // no keyup for the page, and only HardwareKeyboard is repaired for it — the
            // engine's PointerBinding re-reads the modifier flags off every mouse event
            // and synthesizes the missing key-ups. RawKeyboard's snapshot rides on the
            // legacy key channel, which pointer events never touch, so it stays stuck
            // until the next keystroke and turns every plain click into a toggle click.
            final keyboard = HardwareKeyboard.instance;
            final isControlPressed = keyboard.isControlPressed;

            if (defaultTargetPlatform == TargetPlatform.macOS &&
                isControlPressed) {
              // macOS treats Ctrl+Click as a secondary click (it neither
              // toggles multi-selection — that's Cmd — nor counts toward a
              // double-click): select the item, keeping an existing
              // multi-selection it belongs to, and open the context menu.
              _secondaryActivate(button: 1);
              return;
            }

            if (_rowTap.registerTap() == 2) {
              _sendStartEditing();
              final de = _createEvent();
              de.count = 2;
              de.button = 1;
              _context?.parentTree.sendSelectionDefaultSelection(
                _context!.parentTreeValue,
                de,
              );
              return;
            }

            final isCtrlPressed = isControlPressed || keyboard.isMetaPressed;
            final isShiftPressed = keyboard.isShiftPressed;

            _context?.treeImpl?.handleTreeItemSelection(
              state.id,
              isCtrlPressed: isCtrlPressed,
              isShiftPressed: isShiftPressed,
              notifyJava: false,
            );

            int stateMask = 0;
            if (isCtrlPressed) {
              stateMask |= SWT.CTRL;
            }
            if (isShiftPressed) {
              stateMask |= SWT.SHIFT;
            }
            final e = _createEvent(stateMask: stateMask);
            e.count = 1;
            e.button = 1;

            _context?.parentTree.sendSelectionSelection(
              _context!.parentTreeValue,
              e,
            );

            if (selected) {
              _sendStartEditing();
            }
          },
          child: Container(
            width: double.infinity,
            constraints: BoxConstraints(minHeight: effectiveItemHeight),
            margin: selected
                ? EdgeInsets.only(
                    left: -widgetTheme.itemSelectedBorderWidth,
                    right: -widgetTheme.itemSelectedBorderWidth,
                    top: -widgetTheme.itemSelectedBorderWidth,
                    bottom: nextItemSelected
                        ? 0.0
                        : -widgetTheme.itemSelectedBorderWidth,
                  )
                : null,
            foregroundDecoration: getTreeItemFocusRingDecoration(
              widgetTheme,
              selected: selected,
              treeFocused: _context?.treeFocused ?? false,
            ),
            decoration: BoxDecoration(
              color: bgColor,
              borderRadius: hasMultiColumn
                  ? null
                  : BorderRadius.circular(widgetTheme.borderRadius),
              border: _buildItemRowBorder(
                widgetTheme: widgetTheme,
                selected: selected,
                nextItemSelected: nextItemSelected,
                hasMultiColumn: hasMultiColumn,
              ),
            ),
            padding: hasMultiColumn
                ? widgetTheme.itemPaddingWithCols
                : widgetTheme.itemPadding,
            child: _buildRowWithColumns(
              context,
              texts,
              text,
              textColor,
              widgetTheme,
              level,
              hasChildren,
              expanded,
              isCheckMode,
              checked,
              grayed,
              enabled,
              selected,
              image,
              hasMultiColumn,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildCellText({
    required BuildContext context,
    required String text,
    required Color textColor,
    required TreeThemeExtension theme,
    required int? columnAlignment,
    required EdgeInsets cellPadding,
    required int columnIndex,
    bool hasMultiColumn = false,
  }) {
    // Text the row's owner-draw overlay paints keeps its place and semantics, but is not painted
    // twice.
    final cellTextColor = (state.paintedTexts?.contains(columnIndex) ?? false)
        ? Colors.transparent
        : getForegroundColor(
            foreground: state.foreground,
            defaultColor: textColor,
          );
    final TextStyle baseStyle = hasMultiColumn
        ? (theme.itemTextStyleWithCols ?? theme.itemTextStyle ?? const TextStyle())
        : theme.itemTextStyle ?? const TextStyle();
    final cellTextStyle = getTextStyle(
      context: context,
      font: state.font ?? _context?.treeFont,
      textColor: cellTextColor,
      baseTextStyle: baseStyle,
    );

    Widget textWidget = Text(
      text,
      style: cellTextStyle,
      textAlign: getTextAlignFromStyle(columnAlignment ?? 0, TextAlign.left),
      maxLines: 1,
      overflow: TextOverflow.ellipsis,
    );

    EdgeInsets adjustedPadding = adjustPaddingForAlignment(
      basePadding: cellPadding,
      alignment: columnAlignment,
      extraPadding: 4.0,
    );
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];
    if (columns.isNotEmpty && columnIndex < columns.length - 1) {
      adjustedPadding = adjustedPadding.copyWith(
        right: adjustedPadding.right + theme.columnDividerGap,
      );
    }

    return Container(padding: adjustedPadding, child: textWidget);
  }
}

class _CheckboxButtonWrapper extends StatefulWidget {
  /// The id of the item this checkbox belongs to.
  ///
  /// A synthesized value still needs an identity: state is held per widget, keyed by swt and id, so
  /// every checkbox built with the same placeholder id resolved to one shared object and every row
  /// rendered whichever one mounted first.
  final int ownerId;
  final bool checked;
  final bool grayed;
  final bool enabled;
  final VoidCallback onChanged;

  const _CheckboxButtonWrapper({
    Key? key,
    required this.ownerId,
    required this.checked,
    required this.grayed,
    required this.enabled,
    required this.onChanged,
  }) : super(key: key);

  @override
  State<_CheckboxButtonWrapper> createState() => _CheckboxButtonWrapperState();
}

class _CheckboxButtonWrapperState extends State<_CheckboxButtonWrapper> {
  late VButton buttonValue;

  @override
  void initState() {
    super.initState();
    // The box draws the value held for its channel, not one built here, so that is the one kept.
    buttonValue =
        VRegistry.instance.register(
              VButton.empty()
                ..id = widget.ownerId
                ..style = SWT.CHECK,
            )
            as VButton;
    _followItem();
  }

  void _followItem() {
    buttonValue
      ..selection = widget.checked
      ..grayed = widget.grayed
      ..enabled = widget.enabled;
  }

  @override
  void didUpdateWidget(_CheckboxButtonWrapper oldWidget) {
    super.didUpdateWidget(oldWidget);
    _followItem();
  }

  // The button flips itself on a click; the item's check state is Java's, so the box goes back.
  void _onClicked() {
    widget.onChanged();
    if (mounted) setState(_followItem);
  }

  @override
  Widget build(BuildContext context) {
    return _TreeCheckboxButton(value: buttonValue, onChanged: _onClicked);
  }
}

class _TreeCheckboxButton extends ButtonSwt<VButton> {
  final VoidCallback onChanged;

  const _TreeCheckboxButton({required super.value, required this.onChanged});

  @override
  void sendSelectionSelection(VButton val, VEvent? payload) {
    onChanged();
  }
}

extension VTreeItemChildren on VTreeItem {
  /// Whether the item can be expanded. A virtual item knows how many children it has before any
  /// of them exists, and only creates them once it is open, so the count has to answer too.
  bool get hasChildItems => (items?.isNotEmpty ?? false) || (itemCount ?? 0) > 0;
}
