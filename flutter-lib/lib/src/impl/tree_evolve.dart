import 'dart:async' show scheduleMicrotask;

import 'package:flutter/foundation.dart' show listEquals;
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart' show OverflowBoxFit;
import 'package:flutter/scheduler.dart' show SchedulerBinding, SchedulerPhase;
import '../theme/theme_settings/tree_theme_settings.dart';
import 'package:flutter/services.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/droptarget.dart';
import '../impl/composite_evolve.dart';
import '../impl/widget_config.dart';
import '../gen/swt.dart';
import '../gen/tree.dart';
import '../gen/treeitem.dart';
import '../gen/treeeditor.dart';
import 'treeitem_evolve.dart' show VTreeItemChildren;
import '../gen/event.dart';
import '../comm/comm.dart';
import '../theme/theme_extensions/tree_theme_extension.dart';
import 'utils/column_order.dart';
import 'utils/dnd_utils.dart';
import 'utils/font_utils.dart';
import 'utils/item_list_children.dart';
import 'utils/widget_utils.dart';
import '../gen/rectangle.dart';
import '../testing/tree_test_registry.dart';

class _FlatTreeItem {
  final VTreeItem item;
  final int level;
  const _FlatTreeItem(this.item, this.level);
}

/// An open item with more children than have been loaded, and the row its loaded ones end on.
class _MoreRows {
  final int lastRow;
  final VTreeItem parent;
  final int loaded;
  const _MoreRows(this.lastRow, this.parent, this.loaded);
}

class TreeImpl<T extends TreeSwt, V extends VTree> extends CompositeImpl<T, V> {
  final Map<dynamic, String> treeItemExpanders = {};
  final List<String> eventNames = [];
  TreeThemeExtension? _cachedWidgetTheme;
  List<double>? _cachedEffectiveWidths;
  double? _measuredContentWidth;
  // The width rows are laid out at inside the horizontal scroll: a columned tree's column total
  // (at least the viewport), or a columnless tree's widest row while it scrolls horizontally. Laid
  // out at the viewport width instead, they clip their own text again inside the scrollable.
  double? _hScrollContentWidth;
  double? _cachedHeaderHeight;
  double? _cachedItemHeight;
  List<VTreeItem>? _pendingSelection;
  Object? _lastSelectedItemId;
  ScrollController? _horizontalController;
  // The header's own position, kept at the rows' horizontal one.
  final ScrollController _headerHorizontalController = ScrollController();
  ScrollController? _verticalController;
  // The rows' list moves between parents when a columnless tree starts or stops scrolling
  // horizontally; keyed, it keeps its scroll position instead of starting again at the top.
  final GlobalKey _rowsKey = GlobalKey(debugLabel: 'tree rows');
  final FocusNode _focusNode = FocusNode();
  // The width of the column whose divider is being dragged, as the drag has made it so far.
  double? _resizeStartWidth;
  // The header row and the widths its cells were drawn at, where a dragged header is dropped.
  final GlobalKey _headerRowKey = GlobalKey(debugLabel: 'tree header row');
  List<double>? _headerColumnWidths;

  @override
  bool get wrapsWholeWidgetForDnd => false;

  @override
  void initState() {
    super.initState();
    _horizontalController = ScrollController()
      ..addListener(_followRowsWithHeader)
      ..addListener(_reportScrollOffset);
    _verticalController = ScrollController();
    _verticalController!.addListener(_reportScrollOffset);
    _verticalController!.addListener(_requestVisibleChildren);
    // E2E test hooks. Rows DO carry a per-row Semantics node: each is a TreeItemSwt whose build()
    // ends in tagSemantics. But the node only exists while the row is built — the list virtualises,
    // and a row can lose its node while the widget is live — so these expose what the DOM cannot:
    // map a point to the row id under it (GetIdFromPoint), report a row's bounds (GetItemBounds),
    // and register a handle to read/expand the tree from the test harness. One-time; cheap.
    _registerGetIdFromPointListener();
    _registerGetItemBoundsListener();
    // Remember the key we register under. `state` is reassigned in didUpdateWidget (see
    // WidgetSwtState), so recomputing this string in dispose can produce a DIFFERENT key than the
    // one registered here — the unregister then removes nothing and this handle stays in the
    // registry for the life of the page. queryTreeItemsJson() flattens every registered tree, so a
    // leaked handle keeps serving items of a tree that no longer exists, and a text -> identifier
    // lookup resolves to a dead id. That is what made a shared (SUITE-scoped) app unusable: after a
    // few part closes the registry held four 'Classic Examples' roots and 304 items.
    _testTreeKey = '${state.swt}/${state.id}';
    _testTreeHandle = TestTreeHandle(
      items: _testTreeItems,
      expand: _testExpandTreeItem,
    );
    registerTestTree(_testTreeKey, _testTreeHandle);
    if (state.topItem != null) _scrollToTopItemAfterLayout();
  }

  late final String _testTreeKey;

  late final TestTreeHandle _testTreeHandle;

  void _registerGetIdFromPointListener() {
    final eventName = "${state.swt}/${state.id}/GetIdFromPoint";
    EquoCommService.onRaw(eventName, (payload) {
      if (payload != null) {
        try {
          final Map<String, dynamic> point = Map<String, dynamic>.from(
            payload as Map,
          );
          final x = (point['x'] as num?)?.toDouble() ?? 0.0;
          final y = (point['y'] as num?)?.toDouble() ?? 0.0;

          final itemId = findItemIdAtPosition(x, y);

          // Send response back to Java
          final responseEvent =
              "${state.swt}/${state.id}/GetIdFromPointResponse";
          EquoCommService.sendPayload(
            responseEvent,
            itemId?.toString() ?? "-1",
          );
        } catch (e) {
          print('Error processing GetIdFromPoint: $e');
          final responseEvent =
              "${state.swt}/${state.id}/GetIdFromPointResponse";
          EquoCommService.sendPayload(responseEvent, "-1");
        }
      }
    });
  }

  void _registerGetItemBoundsListener() {
    final eventName = "${state.swt}/${state.id}/GetItemBounds";
    EquoCommService.onRaw(eventName, (payload) {
      final responseEvent = "${state.swt}/${state.id}/GetItemBoundsResponse";
      if (payload == null) {
        EquoCommService.sendPayload(responseEvent, VRectangle().toJson());
        return;
      }
      try {
        String raw = payload.toString();
        if (raw.startsWith('"') && raw.endsWith('"')) {
          raw = raw.substring(1, raw.length - 1);
        }
        final parts = raw.split(",");
        final itemId = int.parse(parts[0]);
        final columnIndex = int.parse(parts[1]);

        final bounds = _calculateItemBounds(itemId, columnIndex);
        final rect = VRectangle()
          ..x = bounds.left.round()
          ..y = bounds.top.round()
          ..width = bounds.width.round()
          ..height = bounds.height.round();
        EquoCommService.sendPayload(responseEvent, rect.toJson());
      } catch (e) {
        print('Error processing GetItemBounds: $e');
        EquoCommService.sendPayload(responseEvent, VRectangle().toJson());
      }
    });
  }

  Rect _calculateItemBounds(int itemId, int columnIndex) {
    if (_cachedWidgetTheme == null) return Rect.zero;

    final theme = _cachedWidgetTheme!;
    final itemHeight =
        _cachedItemHeight ?? (theme.itemHeight + theme.itemPadding.vertical);
    final columns = getTreeColumns();

    // Find item flat index
    final itemIndex = findItemIndex(itemId);
    if (itemIndex < 0) return Rect.zero;

    // Y position
    double headerOffset = 0.0;
    if (state.headerVisible == true) {
      headerOffset = _cachedHeaderHeight ?? theme.headerHeight;
    }
    final y = headerOffset + (itemIndex * itemHeight);

    final treeWidth = state.bounds?.width?.toDouble() ?? 0.0;
    if (columns.isEmpty || _cachedEffectiveWidths == null) {
      final leftGap = treeWidth * theme.itemEdgeGapFraction;
      return Rect.fromLTWH(leftGap, y, treeWidth, itemHeight);
    }
    // A cell is where it is drawn: at the sum of the widths of the columns shown before it.
    final widths = _cachedEffectiveWidths!;
    if (columnIndex >= widths.length) return Rect.fromLTWH(0, y, 0, itemHeight);
    final x = columnLefts(widths, displayOrder)[columnIndex];
    return Rect.fromLTWH(x, y, widths[columnIndex], itemHeight);
  }

  @override
  Widget build(BuildContext context) {
    _cachedWidgetTheme = Theme.of(context).extension<TreeThemeExtension>();
    return super.wrap(ParentForegroundScope(
      foreground: state.foreground,
      child: wrapTreeForDrop(createTreeView()),
    ));
  }

  @override
  void setValue(V value) {
    final pending = _pendingSelection;
    if (pending != null && pending.isNotEmpty) {
      final pendingIds = Set.from(pending.map((item) => item.id));
      final incomingIds = Set.from((value.selection ?? []).map((item) => item.id));
      final confirmed =
          pendingIds.length == incomingIds.length &&
          pendingIds.every((id) => incomingIds.contains(id));
      // A delivery that names the selection is Java changing it, and Java owns the selection: it
      // wins over what the last click painted. Anything else may predate the click, so the click's
      // selection stays on screen until Java confirms or replaces it.
      final javaSetSelection = lastChange?.changed?.contains('selection') ?? false;
      if (confirmed || javaSetSelection) {
        _pendingSelection = null;
      } else {
        value.selection = List<VTreeItem>.from(pending);
      }
    }
    super.setValue(value);
    if (lastChange?.touches('topItem') ?? true) _scrollToTopItemAfterLayout();
  }

  /// Brings the item Java asked for to the top, once the rows it is among are laid out. Flutter
  /// clamps the distance at the end of the list, as a native tree does; a tree already showing
  /// that row at the top does not move.
  void _scrollToTopItemAfterLayout() {
    final target = state.topItem;
    if (target == null) return;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      final controller = _verticalController;
      final rowHeight = _cachedItemHeight;
      if (controller == null || !controller.hasClients) return;
      if (rowHeight == null || rowHeight <= 0) return;
      final row = findItemIndex(target.id);
      if (row < 0) return;
      final position = controller.position;
      if ((position.pixels / rowHeight).floor() == row) return;
      controller.jumpTo(
        (row * rowHeight).clamp(position.minScrollExtent, position.maxScrollExtent),
      );
    });
  }

  Widget createTreeView() {
    return LayoutBuilder(
      builder: (context, constraints) {
        final widgetTheme = Theme.of(context).extension<TreeThemeExtension>();
        _cachedWidgetTheme = widgetTheme;

        final double screenWidth = MediaQuery.of(context).size.width;
        final double screenHeight = MediaQuery.of(context).size.height;

        final columns = getTreeColumns();
        final flatItems = _flattenVisibleItems(state.items, 0);
        _moreRows = isVirtual() ? _rowsWithMoreAfter(flatItems) : const [];
        if (_moreRows.isNotEmpty) {
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (mounted) _requestVisibleChildren();
          });
        }
        final hasMultiColumn = columns.length > 1;
        _cachedHeaderHeight = hasMultiColumn
            ? widgetTheme!.headerHeightWithCols
            : widgetTheme!.headerHeight;
        _cachedItemHeight = hasMultiColumn
            ? widgetTheme!.itemHeightWithCols
            : widgetTheme!.itemHeight + widgetTheme!.itemPadding.vertical;

        final double contentHeight =
            (state.headerVisible == true ? _cachedHeaderHeight! : 0.0) +
            flatItems.length * _cachedItemHeight!;

        final double maxWidth = constraints.maxWidth.isFinite
            ? constraints.maxWidth.clamp(0.0, screenWidth)
            : screenWidth;
        final double maxHeight = constraints.maxHeight.isFinite
            ? constraints.maxHeight.clamp(0.0, screenHeight)
            : contentHeight;

        final effectiveWidths =
            columns.isEmpty ? null : _columnWidths(columns, widgetTheme!);
        _cachedEffectiveWidths = effectiveWidths;
        final edgeGap = maxWidth *
            (hasMultiColumn
                ? widgetTheme!.itemEdgeGapFractionWithCols
                : widgetTheme!.itemEdgeGapFraction);

        final enabled = state.enabled ?? true;
        final themeBackground = enabled
            ? widgetTheme!.backgroundColor
            : widgetTheme!.disabledBackgroundColor;
        // The application's own colour wins when the product owns the theme, the way Table
        // already resolves it (getTableBackgroundColor).
        final backgroundColor = getBackgroundColor(
              background: state.background,
              defaultColor: themeBackground,
              context: context,
            ) ??
            themeBackground;

        final bool hasVScroll = StyleBits(state.style).has(SWT.V_SCROLL);
        final bool hasHScroll = StyleBits(state.style).has(SWT.H_SCROLL);
        _measuredContentWidth = (hasHScroll && columns.isEmpty)
            ? _computeContentWidthWithoutColumns(
                context,
                flatItems,
                widgetTheme!,
              )
            : null;

        // Build editor overlays if editing is active
        final editorOverlays = _buildEditorOverlays(
          widgetTheme!,
          effectiveWidths,
        );
        final childControls = buildChildControlLayer(
          state,
          (state.editors ?? const <VTreeEditor>[]).map((e) => e.editor),
        );

        return ScrollConfiguration(
          behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
          child: Focus(
            focusNode: _focusNode,
            onKeyEvent: _handleKeyEvent,
            // Taking focus on the Flutter side is not enough: SWT tracks its own focus
            // control, and the workbench decides which part is active from it. Without
            // telling Java, clicking the tree leaves focus on whatever held it before —
            // so the part that owns the tree never reactivates, and actions bound to it
            // (opening the double-clicked file) silently do nothing.
            onFocusChange: (hasFocus) {
              if (showsTreeItemFocusRing(widgetTheme)) setState(() {});
              // Called whenever the focus moves within the tree too: a cell editor's field taking
              // it from the rows calls this with true again. The field reports its own focus;
              // reporting the tree's as well would hand the focus back to the tree and close the
              // editor. Only the tree gaining and losing the focus is reported.
              if (hasFocus == _reportedHasFocus) return;
              _reportedHasFocus = hasFocus;
              if (hasFocus) {
                if (!_focusInSentOnPress) widget.sendFocusFocusIn(state, null);
              } else {
                widget.sendFocusFocusOut(state, null);
              }
            },
            child: Stack(
              children: [
                Container(
                  width: maxWidth,
                  height: maxHeight,
                  color: backgroundColor,
                  child: TreeEffectiveColumnWidthsProvider(
                    effectiveWidths: effectiveWidths,
                    edgeGap: edgeGap,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        if (state.headerVisible == true)
                          _buildHeaderWithLines(
                            columns,
                            effectiveWidths,
                            widgetTheme!,
                            hasMultiColumn,
                            maxWidth,
                            edgeGap,
                          ),
                        Expanded(
                          child: Listener(
                            onPointerDown: (_) => _takeFocusOnRowsPress(),
                            child: _buildContentWithEdgeGaps(
                              flatItems,
                              columns,
                              widgetTheme!,
                              context,
                              hasVScroll,
                              hasHScroll,
                              effectiveWidths,
                              maxWidth,
                              hasMultiColumn,
                              backgroundColor,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                if (childControls != null) childControls,
                ...editorOverlays,
              ],
            ),
          ),
        );
      },
    );
  }

  List<VTreeColumn> getTreeColumns() {
    List<VTreeColumn> columns = [];

    if (state.columns != null && state.columns!.isNotEmpty) {
      columns.addAll(state.columns!);
    }

    if (state.items != null) {
      final childColumns = state.items!.whereType<VTreeColumn>().toList();
      columns.addAll(childColumns);
    }

    final uniqueColumns = <VTreeColumn>[];
    final seenIds = <dynamic>{};
    for (final column in columns) {
      if (!seenIds.contains(column.id)) {
        seenIds.add(column.id);
        uniqueColumns.add(column);
      }
    }

    return uniqueColumns;
  }

  /// The column indexes in the order the columns are shown.
  List<int> get displayOrder => columnDisplayOrder(state.columnOrder, getTreeColumns().length);

  List<_MoreRows> _rowsWithMoreAfter(List<_FlatTreeItem> flat) {
    final result = <_MoreRows>[];
    for (var i = 0; i < flat.length; i++) {
      final item = flat[i].item;
      final loaded = item.items?.length ?? 0;
      // Fewer children than last time: the item was repopulated, so its rows are asked for afresh.
      if ((_loadedChildren[item.id] ?? 0) > loaded) _requestedChildren.remove(item.id);
      _loadedChildren[item.id] = loaded;
      if (item.expanded != true || (item.itemCount ?? 0) <= loaded) continue;
      var end = i + 1;
      while (end < flat.length && flat[end].level > flat[i].level) {
        end++;
      }
      result.add(_MoreRows(end - 1, item, loaded));
    }
    return result;
  }

  List<_MoreRows> _moreRows = const [];
  final Map<int, int> _requestedChildren = {};
  final Map<int, int> _loadedChildren = {};

  /// Asks Java for the children of an open item that the viewport, plus half a screenful, reaches
  /// past the ones loaded. The same window Table asks for, which keeps SetData within SWT's budget
  /// of three times the visible rows.
  void _requestVisibleChildren() {
    final controller = _verticalController;
    final rowHeight = _cachedItemHeight;
    if (_moreRows.isEmpty || controller == null || !controller.hasClients) return;
    if (rowHeight == null || rowHeight <= 0) return;
    final position = controller.position;
    final visibleRows = (position.viewportDimension / rowHeight).ceil() + 1;
    final windowEnd =
        ((position.pixels + position.viewportDimension) / rowHeight).ceil() + visibleRows ~/ 2;
    for (final more in _moreRows) {
      if (more.lastRow >= windowEnd) continue;
      final needed = more.loaded + (windowEnd - more.lastRow);
      final id = more.parent.id;
      if ((_requestedChildren[id] ?? 0) >= needed) continue;
      _requestedChildren[id] = needed;
      widget.sendSetDataSetData(state, VEvent()
        ..index = id
        ..end = needed);
    }
  }

  List<_FlatTreeItem> _flattenVisibleItems(List<VWidget>? items, int level) {
    if (items == null) return [];
    final List<_FlatTreeItem> result = [];
    for (final item in items) {
      if (item is VTreeItem) {
        result.add(_FlatTreeItem(item, level));
        if (item.expanded == true && item.items != null) {
          result.addAll(_flattenVisibleItems(item.items!, level + 1));
        }
      }
    }
    return result;
  }

  /// Natural width of the widest visible row. A tree with no columns has no column widths
  /// to sum, so this is the only thing a horizontal scroll range can be derived from.
  double _computeContentWidthWithoutColumns(
    BuildContext context,
    List<_FlatTreeItem> flatItems,
    TreeThemeExtension theme,
  ) {
    final isCheckMode = StyleBits(state.style).has(SWT.CHECK);
    // Everything a row's width is made of: a resize rebuilds the tree without changing any of it.
    final inputs = <Object?>[
      isCheckMode, theme, Theme.of(context), state.font, state.foreground,
      for (final flat in flatItems) ...[
        flat.item.text, flat.item.texts?.firstOrNull, flat.item.font, flat.item.foreground,
        flat.level, flat.item.image != null,
      ],
    ];
    final cached = _contentWidth;
    if (cached != null && listEquals(cached.inputs, inputs)) return cached.width;
    final checkboxWidth = isCheckMode
        ? (theme.checkboxSize + theme.checkboxSpacing)
        : 0.0;
    final prefixBase = theme.expandIconSize + theme.expandIconSpacing;
    final iconSpace = theme.itemIconSize + theme.itemIconSpacing;
    final paddingH = theme.itemPadding.horizontal + theme.cellPadding.horizontal;

    double widest = 0.0;
    for (final flat in flatItems) {
      final item = flat.item;
      final texts = item.texts ?? [];
      final text = item.text ?? (texts.isNotEmpty ? texts[0] ?? '' : '');
      final painter = TextPainter(
        text: TextSpan(
          text: text,
          style: getTextStyle(
            context: context,
            font: cellValue(item.fonts, 0) ?? item.font ?? state.font,
            // The item's own colour, else the Tree's -- the same cascade the font above uses.
          textColor: getForegroundColor(
            foreground: item.foreground ?? state.foreground,
            defaultColor: theme.itemTextColor,
            context: context,
          ),
            baseTextStyle: theme.itemTextStyle,
          ),
        ),
        textDirection: TextDirection.ltr,
        textScaler: FontUtils.textWidgetScaler,
        maxLines: 1,
      );
      painter.layout();
      double width =
          painter.width +
          paddingH +
          prefixBase +
          checkboxWidth +
          theme.itemIndent * flat.level;
      if (item.image != null) width += iconSpace;
      if (width > widest) widest = width;
    }
    _contentWidth = (inputs: inputs, width: widest);
    return widest;
  }

  ({List<Object?> inputs, double width})? _contentWidth;

  /// The widths Java gives the columns, which is what `TreeItem.getBounds(int)` lays the cells out
  /// by: column i starts at the sum of the widths before it. A cell drawn at any other width takes a
  /// click into a different column for JFace's ColumnViewer than the one it shows under the mouse.
  List<double> _columnWidths(List<VTreeColumn> columns, TreeThemeExtension theme) => [
    for (final column in columns)
      (column.width ?? theme.columnDefaultWidth.round()).toDouble(),
  ];

  Widget _buildContentWithEdgeGaps(
    List<_FlatTreeItem> flatItems,
    List<VTreeColumn> columns,
    TreeThemeExtension widgetTheme,
    BuildContext context,
    bool hasVScroll,
    bool hasHScroll,
    List<double>? effectiveWidths,
    double maxWidth,
    bool hasMultiColumn,
    Color backgroundColor,
  ) {
    // A tree with columns draws its edge gap inside column 0, so its cells start at the tree's left
    // edge, where TreeItem.getBounds(int) starts them.
    final gapFraction = hasMultiColumn
        ? widgetTheme.itemEdgeGapFractionWithCols
        : widgetTheme.itemEdgeGapFraction;
    final leftGap = columns.isEmpty ? maxWidth * gapFraction : 0.0;
    final rightGap = leftGap;

    Widget listViewWithLines = _buildListViewWithColumnLines(
      flatItems,
      columns,
      widgetTheme,
      effectiveWidths,
    );

    double? totalWidth = _calculateTotalWidth(
      hasHScroll,
      columns,
      widgetTheme,
      effectiveWidths,
      maxWidth - leftGap - rightGap,
    );
    _hScrollContentWidth = totalWidth;

    Widget scrollableContent;
    if (totalWidth != null) {
      // Columns keep their widths when they add up to more than the tree shows: a tree without
      // H_SCROLL clips them, as natively.
      final Widget horizontal = ScrollConfiguration(
        behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
        child: SingleChildScrollView(
          controller: _horizontalController,
          scrollDirection: Axis.horizontal,
          physics: hasHScroll ? null : const NeverScrollableScrollPhysics(),
          child: SizedBox(width: totalWidth, child: listViewWithLines),
        ),
      );
      scrollableContent = hasHScroll
          ? Scrollbar(
              controller: _horizontalController,
              thumbVisibility: true,
              child: horizontal,
            )
          : horizontal;
    } else {
      scrollableContent = ScrollConfiguration(
        behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
        child: listViewWithLines,
      );
    }

    return Row(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Container(width: leftGap, color: backgroundColor),
        Expanded(
          child: hasVScroll
              ? Scrollbar(
                  controller: _verticalController,
                  thumbVisibility: true,
                  child: ScrollConfiguration(
                    behavior: ScrollConfiguration.of(
                      context,
                    ).copyWith(scrollbars: false),
                    child: scrollableContent,
                  ),
                )
              : ScrollConfiguration(
                  behavior: ScrollConfiguration.of(
                    context,
                  ).copyWith(scrollbars: false),
                  child: scrollableContent,
                ),
        ),
        Container(width: rightGap, color: backgroundColor),
      ],
    );
  }

  /// The width the rows are laid out at inside the horizontal scroll, or null when they take the
  /// viewport's: a tree with columns always lays them out at the columns' total, at least the
  /// viewport; a columnless one only once its widest row is wider than the viewport.
  double? _calculateTotalWidth(
    bool hasHScroll,
    List<VTreeColumn> columns,
    TreeThemeExtension widgetTheme,
    List<double>? effectiveWidths,
    double viewportWidth,
  ) {
    if (columns.isNotEmpty) {
      final widths = effectiveWidths ?? _columnWidths(columns, widgetTheme);
      final total = widths.fold(0.0, (sum, w) => sum + w);
      return total > viewportWidth ? total : viewportWidth;
    }
    if (!hasHScroll) return null;
    final contentWidth = _measuredContentWidth;
    if (contentWidth == null || contentWidth <= viewportWidth) return null;
    return contentWidth;
  }

  Widget _buildListViewWithColumnLines(
    List<_FlatTreeItem> flatItems,
    List<VTreeColumn> columns,
    TreeThemeExtension widgetTheme,
    List<double>? effectiveWidths,
  ) {
    final bool linesVisible = state.linesVisible ?? false;
    final bool hasMultiColumn = columns.length > 1;

    Widget listView = ListView.builder(
      key: _rowsKey,
      controller: _verticalController,
      itemCount: flatItems.length,
      itemBuilder: (context, index) {
        return _buildFlatTreeItemWidget(flatItems[index]);
      },
    );

    if (!linesVisible || columns.isEmpty || hasMultiColumn) {
      return listView;
    }

    final List<double> linePositions = [];
    double cumulativeWidth = 0.0;
    final borderWidth = widgetTheme.columnBorderWidth;

    for (int i = 0; i < columns.length; i++) {
      final double width = effectiveWidths != null && i < effectiveWidths.length
          ? effectiveWidths[i]
          : (columns[i].width ?? widgetTheme.columnDefaultWidth.round())
                .toDouble();

      cumulativeWidth += width;

      if (i < columns.length - 1) {
        linePositions.add(cumulativeWidth - borderWidth);
      }
    }

    return Stack(
      children: [
        listView,
        ...linePositions.map(
          (position) => Positioned(
            left: position,
            top: 0,
            bottom: 0,
            child: IgnorePointer(
              child: Container(
                width: widgetTheme.columnBorderWidth,
                color: widgetTheme.columnRightBorderColor,
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildHeaderWithLines(
    List<VTreeColumn> columns,
    List<double>? effectiveWidths,
    TreeThemeExtension widgetTheme,
    bool hasMultiColumn,
    double maxWidth,
    double edgeGap,
  ) {
    final prefix = widgetTheme.expandIconSize + widgetTheme.expandIconSpacing;
    // Column 0's label keeps its place past the expander; with more than one column the tree's edge
    // gap is inside column 0 too, as it is in the rows.
    final firstLabelInset = prefix + (hasMultiColumn ? edgeGap : 0.0);
    final defaultWidth = widgetTheme.columnDefaultWidth.round();
    final borderWidth = widgetTheme.columnBorderWidth;

    final double headerHeight = hasMultiColumn
        ? widgetTheme.headerHeightWithCols
        : widgetTheme.headerHeight;
    final Color themeHeaderBg = hasMultiColumn
        ? widgetTheme.headerBackgroundColorWithCols
        : widgetTheme.headerBackgroundColor;
    // Tree.setHeaderBackground is a real SWT setter the CSS engine drives, so the application's
    // choice wins over the theme's.
    final Color headerBgColor = getBackgroundColor(
          background: state.headerBackground,
          defaultColor: themeHeaderBg,
          context: context,
        ) ??
        themeHeaderBg;
    final double effectiveHeaderBorderWidth = hasMultiColumn
        ? widgetTheme.headerBorderWidthWithCols
        : widgetTheme.headerBorderWidth;
    final bool showHeaderBorder = effectiveHeaderBorderWidth > 0;

    final List<double> linePositions = [];
    final List<int> order = displayOrder;
    final List<double> columnWidths = [
      for (int i = 0; i < columns.length; i++)
        effectiveWidths != null && i < effectiveWidths.length
            ? effectiveWidths[i]
            : (columns[i].width ?? defaultWidth).toDouble(),
    ];
    _headerColumnWidths = columnWidths;
    double cumulativeWidth = 0.0;

    Widget headerContent = Stack(
      children: [
        Semantics(
          identifier: 'sizeprobe:header',
          child: Container(
            height: headerHeight,
            color: headerBgColor,
            child: TreeLinesVisibleProvider(
              linesVisible: state.linesVisible ?? false,
              child: TreeHasMultiColumnProvider(
                hasMultiColumn: hasMultiColumn,
                child: Row(
                  key: _headerRowKey,
                  children: [
                    // In the column order; the tree column keeps its expander room wherever it is.
                    ...order.asMap().entries.map((entry) {
                      final int position = entry.key;
                      final int columnIndex = entry.value;
                      final column = columns[columnIndex];
                      final double width = columnWidths[columnIndex];
                      cumulativeWidth += width;
                      if (position < order.length - 1) {
                        linePositions.add(cumulativeWidth - borderWidth);
                      }
                      if (columnIndex == 0) {
                        return TreeCellBox(
                          width: width,
                          minContentWidth: firstLabelInset,
                          child: Row(
                            children: [
                              SizedBox(width: firstLabelInset),
                              Expanded(
                                child: getWidgetForTreeColumn(
                                  column,
                                  columnIndex,
                                ),
                              ),
                            ],
                          ),
                        );
                      }
                      return TreeCellBox(
                        width: width,
                        child: getWidgetForTreeColumn(column, columnIndex),
                      );
                    }),
                  ],
                ),
              ),
            ),
          ),
        ),
        if (showHeaderBorder)
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: Container(
              height: effectiveHeaderBorderWidth,
              color: widgetTheme.headerBorderColor,
            ),
          ),
        ..._buildColumnResizeHandles(columns, columnWidths, order, headerHeight, widgetTheme),
        if (state.linesVisible == true &&
            linePositions.isNotEmpty &&
            !hasMultiColumn)
          ...linePositions.map(
            (position) => Positioned(
              left: position,
              top: 0,
              bottom: 0,
              child: Container(
                width: borderWidth,
                color: widgetTheme.columnRightBorderColor,
              ),
            ),
          ),
      ],
    );

    // The header lays its columns on the rows' boundaries, and follows them when they scroll
    // sideways.
    final double contentWidth = cumulativeWidth > maxWidth ? cumulativeWidth : maxWidth;
    return SingleChildScrollView(
      controller: _headerHorizontalController,
      scrollDirection: Axis.horizontal,
      physics: const NeverScrollableScrollPhysics(),
      child: SizedBox(width: contentWidth, child: headerContent),
    );
  }

  /// Native SWT has no sash on a column boundary: the header itself switches to the resize cursor a
  /// few pixels either side of the boundary and drags it. These transparent strips straddle each
  /// boundary of a resizable column, so the grip answers from both sides of it.
  List<Widget> _buildColumnResizeHandles(
    List<VTreeColumn> columns,
    List<double> widths,
    List<int> order,
    double headerHeight,
    TreeThemeExtension theme,
  ) {
    final grip = theme.columnResizeHandleWidth * 2;
    final handles = <Widget>[];
    double boundary = 0.0;
    for (final i in order) {
      boundary += widths[i];
      // SWT's TreeColumn.resizable defaults to true.
      if (columns[i].resizable == false) continue;
      final column = columns[i];
      final renderedWidth = widths[i];
      handles.add(Positioned(
        left: boundary - grip / 2,
        top: 0,
        width: grip,
        height: headerHeight,
        child: MouseRegion(
          cursor: SystemMouseCursors.resizeColumn,
          child: GestureDetector(
            behavior: HitTestBehavior.opaque,
            onHorizontalDragStart: (_) =>
                _resizeStartWidth = (column.width ?? renderedWidth.round()).toDouble(),
            onHorizontalDragUpdate: (details) => _updateColumnResize(column, details.delta.dx),
            onHorizontalDragEnd: (_) => _resizeStartWidth = null,
            onHorizontalDragCancel: () => _resizeStartWidth = null,
          ),
        ),
      ));
    }
    return handles;
  }

  void _updateColumnResize(VTreeColumn column, double deltaX) {
    final start = _resizeStartWidth;
    if (start == null) return;
    final next = (start + deltaX).clamp(0.0, double.infinity);
    _resizeStartWidth = next;
    if (next.round() == column.width) return;
    setState(() => column.width = next.round());
    // Native SWT resizes the column live and sends Resize on every change: JFace relays out from it.
    final e = VEvent()..width = column.width;
    TreeColumnSwt<VTreeColumn>(value: column).sendControlResize(column, e);
  }

  Widget getWidgetForTreeColumn(VTreeColumn column, int columnIndex) {
    final bool isLastColumn = isLastShown(columnIndex);
    final bool sorted = state.sortColumn != null && state.sortColumn!.id == column.id;
    return TreeColumnIndexProvider(
      columnIndex: columnIndex,
      child: TreeColumnIsLastProvider(
        isLastColumn: isLastColumn,
        child: TreeHeaderCellProvider(
          sortDirection: sorted ? (state.sortDirection ?? SWT.NONE) : SWT.NONE,
          foreground: state.headerForeground,
          dropIndexAt: _headerDropIndexAt,
          child: TreeColumnSwt(value: column),
        ),
      ),
    );
  }

  /// Where on the header row a column dragged to [globalPosition] is dropped.
  int _headerDropIndexAt(Offset globalPosition) {
    final box = _headerRowKey.currentContext?.findRenderObject();
    final widths = _headerColumnWidths;
    if (box is! RenderBox || widths == null) return 0;
    return dropDisplayIndex(widths, displayOrder, box.globalToLocal(globalPosition).dx);
  }

  /// Whether column [columnIndex] is the one shown last.
  bool isLastShown(int columnIndex) {
    final order = displayOrder;
    return order.isNotEmpty && order.last == columnIndex;
  }

  Widget _buildFlatTreeItemWidget(_FlatTreeItem flatItem) {
    setupTreeItemExpandListener(flatItem.item);

    final treeWidth = _hScrollContentWidth ?? state.bounds?.width?.toDouble();

    return TreeItemSwtWrapper(
      key: ValueKey('tree_item_${flatItem.item.id}'),
      treeItem: flatItem.item,
      level: flatItem.level,
      isCheckMode: getTreeViewSelectionMode(),
      parentTree: widget,
      parentTreeValue: state,
      treeImpl: this,
      treeFont: state.font,
      treeWidth: treeWidth,
      renderChildItems: false,
    );
  }

  bool getTreeViewSelectionMode() {
    return StyleBits(state.style).has(SWT.CHECK);
  }

  /// An item's expansion or children changed on the item's own channel.
  void itemStructureChanged() {
    if (!mounted) return;
    // Can arrive while a row registers inside this Tree's own build; that rebuild waits a frame.
    if (SchedulerBinding.instance.schedulerPhase == SchedulerPhase.persistentCallbacks) {
      SchedulerBinding.instance.addPostFrameCallback((_) {
        if (mounted) setState(() {});
      });
      return;
    }
    setState(() {});
  }

  void setItemExpanded(Object itemId, bool expanded) {
    final item = _findTreeItemById(itemId);
    if (item == null || (item.expanded ?? false) == expanded) return;
    setState(() {
      item.expanded = expanded;
    });
  }

  void setupTreeItemExpandListener(VTreeItem treeItemValue) {
    String eventName = "TreeItem/${treeItemValue.id}/TreeItem/Expand";

    if (treeItemExpanders.containsKey(treeItemValue.id)) {
      return;
    }

    eventNames.add(eventName);
    treeItemExpanders[treeItemValue.id] = eventName;

    EquoCommService.on<VTreeItem>(eventName, (VTreeItem payload) {
      setState(() {
        treeItemValue.items = filterNonEmptyTreeItems(payload.items);
        treeItemValue.expanded = true;
      });
    });
  }

  List<VTreeItem> filterNonEmptyTreeItems(List<VWidget>? items) {
    if (items == null) return [];

    List<VTreeItem> newItems = items
        .whereType<VTreeItem>()
        .where(
          (treeItem) =>
              (treeItem.texts != null &&
                  treeItem.texts!.any((text) => text?.isNotEmpty == true)) ||
              (treeItem.text != null && treeItem.text!.isNotEmpty) ||
              (treeItem.items != null &&
                  treeItem.items!.whereType<VTreeItem>().any(
                    (child) =>
                        (child.text != null && child.text!.isNotEmpty) ||
                        (child.texts != null &&
                            child.texts!.any((t) => t?.isNotEmpty == true)),
                  )),
        )
        .toList();

    for (VWidget item in newItems) {
      if (item is VTreeItem && (item.expanded ?? false)) {
        item.items = filterNonEmptyTreeItems(item.items);
      }
    }
    return newItems;
  }

  bool isVirtual() {
    return StyleBits(state.style).has(SWT.VIRTUAL);
  }

  /// [notifyJava] defaults to true so keyboard-driven callers emit the SWT Selection
  /// event; row-tap/checkbox sites pass false because they send their own richer event
  /// (button, count, stateMask) — see treeitem_evolve.
  ///
  /// On a multi-selection tree, [toggle] adds the item to the selection or takes it out, and
  /// [isShiftPressed] selects the rows from the anchor to it; with neither, it is selected alone.
  void handleTreeItemSelection(
    Object itemId, {
    bool toggle = false,
    bool isShiftPressed = false,
    bool notifyJava = true,
  }) {
    if (state.enabled != true) return;
    final item = _findTreeItemById(itemId);
    if (item == null) return;
    // The press that started a click already focused the tree (_takeFocusOnRowsPress). Focusing
    // it again here, on the release, would take the focus back from an editor the MouseDown opened.

    final bool isMultiMode = StyleBits(state.style).has(SWT.MULTI);
    setState(() {
      if (isMultiMode) {
        final flatIndex = _findFlatIndexForItem(itemId, 0, 0).index;
        _handleMultiSelection(item, flatIndex, toggle, isShiftPressed);
      } else {
        _handleSingleSelection(item, 0);
      }
    });
    if (notifyJava) {
      widget.sendSelectionSelection(state, withSelection(_createDefaultEvent(itemId)));
    }
  }

  /// [event], carrying the ids of the items this tree shows selected. A modifier click or a
  /// Shift+arrow builds the selection out of more than the row the event names, so Java takes the
  /// selection from here instead of selecting that row alone.
  VEvent withSelection(VEvent event) {
    event.segments = [for (final item in state.selection ?? const <VTreeItem>[]) item.id];
    return event;
  }

  /// Set by a press that focused the tree until Flutter applies that focus, so the focus change it
  /// causes is not reported a second time.
  bool _focusInSentOnPress = false;

  /// Whether the focus was in the tree, its child controls included, when last reported.
  bool _reportedHasFocus = false;

  /// A press on the rows focuses the tree before its MouseDown goes out, as natively: a cell editor
  /// that MouseDown opens takes the focus from the tree, and nothing on the release takes it back.
  ///
  /// The focus is reported on every press, not only on a Flutter focus change. Java owns the SWT
  /// focus holder and moves it on its own — activating an editor part focuses that part's
  /// control — while this tree keeps its Flutter focus, so a later click produces no focus change
  /// here and Java would never learn the tree is focused again. The workbench picks the active
  /// part from the SWT focus holder, so leaving it stale makes every action bound to this tree's
  /// part (opening the double-clicked file) silently do nothing.
  void _takeFocusOnRowsPress() {
    if (state.enabled != true) return;
    final bool hadFocus = _focusNode.hasFocus;
    _focusNode.requestFocus();
    widget.sendFocusFocusIn(state, null);
    if (!hadFocus) {
      _focusInSentOnPress = true;
      // Flutter applies the focus in a microtask requestFocus queued; this one runs after it.
      scheduleMicrotask(() => _focusInSentOnPress = false);
    }
  }

  /// A tree is not a childless composite surface: its focus is [_focusNode], which a press on the
  /// rows takes and a press on the header does not.
  @override
  void takeFocusOnPress() {}

  void _handleSingleSelection(VTreeItem item, int flatIndex) {
    state.selection = [item];
    _pendingSelection = [item];
    _lastSelectedItemId = item.id;
  }

  /// A toggle click moves the anchor a later Shift+click ranges from, as a plain click does; a
  /// Shift+click leaves it where it was.
  void _handleMultiSelection(
    VTreeItem item,
    int flatIndex,
    bool toggle,
    bool isShiftPressed,
  ) {
    final currentSelection = List<VTreeItem>.from(state.selection ?? []);

    if (toggle) {
      if (currentSelection.any((selected) => selected.id == item.id)) {
        currentSelection.removeWhere((selected) => selected.id == item.id);
      } else {
        currentSelection.add(item);
      }
      _lastSelectedItemId = item.id;
    } else if (isShiftPressed && _lastSelectedItemId != null) {
      final lastIndex = _findFlatIndexForItem(_lastSelectedItemId!, 0, 0).index;
      final rangeItems = _getItemsInRange(lastIndex, flatIndex);
      currentSelection.clear();
      currentSelection.addAll(rangeItems);
    } else {
      currentSelection.clear();
      currentSelection.add(item);
      _lastSelectedItemId = item.id;
    }

    state.selection = currentSelection;
    _pendingSelection = List<VTreeItem>.from(currentSelection);
  }

  List<VTreeItem> _getItemsInRange(int startIndex, int endIndex) {
    final result = <VTreeItem>[];
    final rangeStart = startIndex < endIndex ? startIndex : endIndex;
    final rangeEnd = startIndex < endIndex ? endIndex : startIndex;
    _collectItemsInRange(state.items ?? [], 0, rangeStart, rangeEnd, result);
    return result;
  }

  int _collectItemsInRange(
    List<VWidget> items,
    int currentIndex,
    int startIndex,
    int endIndex,
    List<VTreeItem> result,
  ) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (currentIndex >= startIndex && currentIndex <= endIndex) {
          result.add(item);
        }
        currentIndex++;

        if (item.expanded == true && item.items != null) {
          currentIndex = _collectItemsInRange(
            item.items!,
            currentIndex,
            startIndex,
            endIndex,
            result,
          );
        }
      }
    }
    return currentIndex;
  }

  bool get hasFocus => _focusNode.hasFocus;

  bool isItemSelected(Object itemId) {
    return state.selection?.any((item) => item.id == itemId) ?? false;
  }

  bool isNextItemSelected(Object itemId) {
    final currentIndex = findItemIndex(itemId);
    if (currentIndex < 0) return false;

    final nextItem = _getItemAtFlatIndex(currentIndex + 1);
    if (nextItem == null) return false;

    return isItemSelected(nextItem.id);
  }

  VTreeItem? _getItemAtFlatIndex(int targetIndex) {
    if (state.items == null) return null;
    return _getItemAtFlatIndexRecursive(state.items!, 0, targetIndex);
  }

  VTreeItem? _getItemAtFlatIndexRecursive(
    List<VWidget> items,
    int currentIndex,
    int targetIndex,
  ) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (currentIndex == targetIndex) {
          return item;
        }
        currentIndex++;

        if (item.expanded == true && item.items != null) {
          final result = _getItemAtFlatIndexRecursive(
            item.items!,
            currentIndex,
            targetIndex,
          );
          if (result != null) {
            return result;
          }
          currentIndex = _countAllItems(item.items!, currentIndex);
        }
      }
    }
    return null;
  }

  int _countAllItems(List<VWidget> items, int startIndex) {
    int currentIndex = startIndex;
    for (final item in items) {
      if (item is VTreeItem) {
        currentIndex++;
        if (item.expanded == true && item.items != null) {
          currentIndex = _countAllItems(item.items!, currentIndex);
        }
      }
    }
    return currentIndex;
  }

  bool isMultiSelectionMode() {
    return StyleBits(state.style).has(SWT.MULTI);
  }

  Set<Object> getSelectedItemIds() {
    return Set.from(state.selection?.map((item) => item.id) ?? []);
  }

  void selectAll() {
    if (!isMultiSelectionMode()) return;

    setState(() {
      final allItems = <VTreeItem>[];
      _collectAllItems(state.items ?? [], allItems);
      state.selection = allItems;
    });
  }

  void _collectAllItems(List<VWidget> items, List<VTreeItem> result) {
    for (final item in items) {
      if (item is VTreeItem) {
        result.add(item);
        if (item.items != null) {
          _collectAllItems(item.items!, result);
        }
      }
    }
  }

  void deselectAll() {
    setState(() {
      state.selection = [];
    });
  }

  TreeItemSearchResult _findFlatIndexForItem(
    Object itemId,
    int currentIndex,
    int currentLevel,
  ) {
    if (state.items == null) {
      return TreeItemSearchResult(
        found: false,
        index: currentIndex,
        level: currentLevel,
      );
    }

    return _findFlatIndexRecursive(
      state.items!,
      itemId,
      currentIndex,
      currentLevel,
    );
  }

  TreeItemSearchResult _findFlatIndexRecursive(
    List<VWidget> items,
    Object itemId,
    int currentIndex,
    int currentLevel,
  ) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (item.id == itemId) {
          return TreeItemSearchResult(
            found: true,
            index: currentIndex,
            level: currentLevel,
          );
        }

        currentIndex++;

        if (item.expanded == true && item.items != null) {
          final result = _findFlatIndexRecursive(
            item.items!,
            itemId,
            currentIndex,
            currentLevel + 1,
          );
          if (result.found) {
            return result;
          }
          currentIndex = result.index;
        }
      }
    }

    return TreeItemSearchResult(
      found: false,
      index: currentIndex,
      level: currentLevel,
    );
  }

  Object? get selectedItemId =>
      state.selection?.isNotEmpty == true ? state.selection!.last.id : null;

  int findItemIndex(Object itemId) {
    final result = _findFlatIndexForItem(itemId, 0, 0);
    return result.found ? result.index : -1;
  }

  /// Finds the TreeItem ID at the given x, y coordinates.
  /// Returns null if no item is found at that position.
  Object? findItemIdAtPosition(double x, double y) {
    if (_cachedWidgetTheme == null) {
      return null;
    }

    final theme = _cachedWidgetTheme!;
    final itemHeight =
        _cachedItemHeight ?? (theme.itemHeight + theme.itemPadding.vertical);

    // Account for header if visible
    double headerOffset = 0.0;
    final columns = getTreeColumns();
    if (state.headerVisible == true) {
      headerOffset = _cachedHeaderHeight ?? theme.headerHeight;
    }

    // Adjust y for header
    final adjustedY = y - headerOffset;
    if (adjustedY < 0) {
      return null;
    }

    // Calculate which item index this y position corresponds to
    final itemIndex = (adjustedY / itemHeight).floor();

    // Get the item at that flat index
    final item = _getItemAtFlatIndex(itemIndex);
    return item?.id;
  }

  final HoverTracker<int> dragHover = HoverTracker<int>();

  Widget wrapTreeForDrop(Widget content) {
    Offset? localOffset(DragTargetDetails<DndDragPayload> details) {
      final box = context.findRenderObject();
      return box is RenderBox ? box.globalToLocal(details.offset) : null;
    }

    int resolveItemId(DragTargetDetails<DndDragPayload> details) {
      final local = localOffset(details);
      if (local == null) {
        dragHover.update(null);
        return 0;
      }
      final id = findItemIdAtPosition(local.dx, local.dy) as int?;
      dragHover.update(id);
      return id ?? 0;
    }

    Offset resolvePosition(DragTargetDetails<DndDragPayload> details) =>
        localOffset(details) ?? details.offset;

    return wrapDropTarget<DndDragPayload>(
      child: content,
      state: state,
      resolveItemId: resolveItemId,
      resolvePosition: resolvePosition,
      onDrop: (_, __, targetItemId, position) => sendItemDrop(targetItemId, position),
      builder: (context, child, negotiation, isHovering) {
        if (!isHovering) dragHover.update(null);
        return child;
      },
    );
  }

  void sendItemDrop(int? targetItemId, Offset position) {
    final dropTargetId = state.dropTargetId;
    if (dropTargetId == null) return;
    final dropTargetValue = VDropTarget()..id = dropTargetId;
    final event = VEvent()
      ..x = position.dx.round()
      ..y = position.dy.round();
    if (targetItemId != null && targetItemId != 0) event.itemId = targetItemId;
    DropTargetSwt<VDropTarget>(value: dropTargetValue)
        .sendDropdrop(dropTargetValue, event);
  }

  /// Builds editor overlay widgets for all active editors.
  List<Widget> _buildEditorOverlays(
    TreeThemeExtension widgetTheme,
    List<double>? effectiveWidths,
  ) {
    final editors = state.editors;
    if (editors == null || editors.isEmpty) return [];

    final columns = getTreeColumns();
    final itemHeight =
        _cachedItemHeight ??
        (widgetTheme.itemHeight + widgetTheme.itemPadding.vertical);
    double headerOffset = 0.0;
    if (state.headerVisible == true) {
      headerOffset = _cachedHeaderHeight ?? widgetTheme.headerHeight;
    }

    final List<Widget> overlays = [];
    for (final editable in editors) {
      if (editable.editor == null || editable.item == null) continue;

      final editingItemId = editable.item!.id;
      final columnIndex = editable.column ?? 0;

      final itemIndex = findItemIndex(editingItemId);
      if (itemIndex < 0) continue;

      final editorY = headerOffset + (itemIndex * itemHeight);

      double editorX = 0.0;
      double editorWidth = state.bounds?.width?.toDouble() ?? 0.0;

      if (columns.isNotEmpty &&
          effectiveWidths != null &&
          columnIndex < effectiveWidths.length) {
        editorX = columnLefts(effectiveWidths, displayOrder)[columnIndex];
        editorWidth = effectiveWidths[columnIndex];
      }

      final editorWidget = mapWidgetFromValue(editable.editor!);

      overlays.add(
        Positioned(
          left: editorX,
          top: editorY,
          width: editorWidth,
          height: itemHeight,
          child: Container(
            decoration: BoxDecoration(
              color: Colors.white,
              border: Border.all(color: Colors.blue, width: 1),
            ),
            child: editorWidget,
          ),
        ),
      );
    }
    return overlays;
  }

  KeyEventResult _handleKeyEvent(FocusNode node, KeyEvent event) {
    if (event is! KeyDownEvent) {
      return KeyEventResult.ignored;
    }

    if (state.enabled != true) {
      return KeyEventResult.ignored;
    }

    final bool isCtrlPressed =
        HardwareKeyboard.instance.logicalKeysPressed.contains(
          LogicalKeyboardKey.controlLeft,
        ) ||
        HardwareKeyboard.instance.logicalKeysPressed.contains(
          LogicalKeyboardKey.controlRight,
        );
    final bool isShiftPressed =
        HardwareKeyboard.instance.logicalKeysPressed.contains(
          LogicalKeyboardKey.shiftLeft,
        ) ||
        HardwareKeyboard.instance.logicalKeysPressed.contains(
          LogicalKeyboardKey.shiftRight,
        );

    switch (event.logicalKey) {
      case LogicalKeyboardKey.arrowUp:
        _navigateUp(isCtrlPressed, isShiftPressed);
        return KeyEventResult.handled;

      case LogicalKeyboardKey.arrowDown:
        _navigateDown(isCtrlPressed, isShiftPressed);
        return KeyEventResult.handled;

      case LogicalKeyboardKey.arrowLeft:
        _navigateLeft();
        return KeyEventResult.handled;

      case LogicalKeyboardKey.arrowRight:
        _navigateRight();
        return KeyEventResult.handled;

      case LogicalKeyboardKey.space:
        _handleSpaceKey();
        return KeyEventResult.handled;

      case LogicalKeyboardKey.home:
        _navigateToFirst(isCtrlPressed, isShiftPressed);
        return KeyEventResult.handled;

      case LogicalKeyboardKey.end:
        _navigateToLast(isCtrlPressed, isShiftPressed);
        return KeyEventResult.handled;

      default:
        return KeyEventResult.ignored;
    }
  }

  void _navigateUp(bool isCtrlPressed, bool isShiftPressed) {
    final selectedItems = state.selection ?? [];
    if (selectedItems.isEmpty) {
      _navigateToFirst(isCtrlPressed, isShiftPressed);
      return;
    }

    final allItems = _getAllVisibleItemIds();
    final currentIndex = allItems.indexOf(selectedItems.last.id);

    if (currentIndex > 0) {
      final newItemId = allItems[currentIndex - 1];

      if (isMultiSelectionMode() && isShiftPressed) {
        handleTreeItemSelection(newItemId, isShiftPressed: true);
      } else {
        handleTreeItemSelection(newItemId, toggle: isCtrlPressed);
      }
    }
  }

  void _navigateDown(bool isCtrlPressed, bool isShiftPressed) {
    final selectedItems = state.selection ?? [];
    if (selectedItems.isEmpty) {
      _navigateToFirst(isCtrlPressed, isShiftPressed);
      return;
    }

    final allItems = _getAllVisibleItemIds();
    final currentIndex = allItems.indexOf(selectedItems.last.id);

    if (currentIndex >= 0 && currentIndex < allItems.length - 1) {
      final newItemId = allItems[currentIndex + 1];

      if (isMultiSelectionMode() && isShiftPressed) {
        handleTreeItemSelection(newItemId, isShiftPressed: true);
      } else {
        handleTreeItemSelection(newItemId, toggle: isCtrlPressed);
      }
    }
  }

  void _navigateLeft() {
    final selectedItems = state.selection ?? [];
    if (selectedItems.isEmpty) return;

    final selectedItem = selectedItems.last;
    if (selectedItem.expanded == true && selectedItem.hasChildItems) {
      setState(() {
        selectedItem.expanded = false;
      });
      _sendExpandCollapseEvent(selectedItem, false);
    } else {
      final parentId = _findParentItemId(selectedItem.id);
      if (parentId != null) {
        handleTreeItemSelection(parentId);
      }
    }
  }

  void _navigateRight() {
    final selectedItems = state.selection ?? [];
    if (selectedItems.isEmpty) return;

    final selectedItem = selectedItems.last;
    if (selectedItem.expanded != true && selectedItem.hasChildItems) {
      setState(() {
        selectedItem.expanded = true;
      });
      _sendExpandCollapseEvent(selectedItem, true);
    } else if (selectedItem.expanded == true &&
        (selectedItem.items?.isNotEmpty ?? false)) {
      final firstChildId = selectedItem.items!.first.id;
      handleTreeItemSelection(firstChildId);
    }
  }

  VEvent _createDefaultEvent(Object itemId) {
    var e = VEvent();
    e.index = findItemIndex(itemId);
    if (itemId is int) e.itemId = itemId;
    e.detail = _cachedWidgetTheme!.eventDefaultDetail;
    e.x = _cachedWidgetTheme!.eventDefaultX;
    e.y = _cachedWidgetTheme!.eventDefaultY;
    e.width = _cachedWidgetTheme!.eventDefaultWidth.round();
    e.height = _cachedWidgetTheme!.eventDefaultHeight.round();
    return e;
  }

  void _handleSpaceKey() {
    final selectedItems = state.selection ?? [];
    if (selectedItems.isEmpty) return;

    final selectedItem = selectedItems.last;
    if (getTreeViewSelectionMode()) {
      setState(() {
        selectedItem.checked = !(selectedItem.checked ?? false);
      });

      var e = _createDefaultEvent(selectedItem.id);
      e.detail = SWT.CHECK;

      widget.sendSelectionSelection(state, withSelection(e));
    } else {
      handleTreeItemSelection(selectedItem.id);
    }
  }

  void _navigateToFirst(bool isCtrlPressed, bool isShiftPressed) {
    final allItems = _getAllVisibleItemIds();
    if (allItems.isNotEmpty) {
      final firstItemId = allItems.first;

      if (isMultiSelectionMode() && isShiftPressed) {
        handleTreeItemSelection(firstItemId, isShiftPressed: true);
      } else {
        handleTreeItemSelection(firstItemId, toggle: isCtrlPressed);
      }
    }
  }

  void _navigateToLast(bool isCtrlPressed, bool isShiftPressed) {
    final allItems = _getAllVisibleItemIds();
    if (allItems.isNotEmpty) {
      final lastItemId = allItems.last;

      if (isMultiSelectionMode() && isShiftPressed) {
        handleTreeItemSelection(lastItemId, isShiftPressed: true);
      } else {
        handleTreeItemSelection(lastItemId, toggle: isCtrlPressed);
      }
    }
  }

  List<Object> _getAllVisibleItemIds() {
    final result = <Object>[];
    _collectVisibleItemIds(state.items ?? [], result);
    return result;
  }

  void _collectVisibleItemIds(List<VWidget> items, List<Object> result) {
    for (final item in items) {
      if (item is VTreeItem) {
        result.add(item.id);
        if (item.expanded == true && item.items != null) {
          _collectVisibleItemIds(item.items!, result);
        }
      }
    }
  }

  VTreeItem? _findTreeItemById(Object itemId) {
    return _findTreeItemByIdRecursive(state.items ?? [], itemId);
  }

  VTreeItem? _findTreeItemByIdRecursive(List<VWidget> items, Object itemId) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (item.id == itemId) {
          return item;
        }
        if (item.items != null) {
          final found = _findTreeItemByIdRecursive(item.items!, itemId);
          if (found != null) return found;
        }
      }
    }
    return null;
  }

  Object? _findParentItemId(Object childId) {
    return _findParentItemIdRecursive(state.items ?? [], childId, null);
  }

  Object? _findParentItemIdRecursive(
    List<VWidget> items,
    Object childId,
    Object? parentId,
  ) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (item.id == childId) {
          return parentId;
        }
        if (item.items != null) {
          final found = _findParentItemIdRecursive(
            item.items!,
            childId,
            item.id,
          );
          if (found != null) return found;
        }
      }
    }
    return null;
  }

  int _getItemLevel(Object itemId) {
    return _getItemLevelRecursive(state.items ?? [], itemId, 0);
  }

  int _getItemLevelRecursive(
    List<VWidget> items,
    Object itemId,
    int currentLevel,
  ) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (item.id == itemId) {
          return currentLevel;
        }
        if (item.items != null) {
          final level = _getItemLevelRecursive(
            item.items!,
            itemId,
            currentLevel + 1,
          );
          if (level >= 0) return level;
        }
      }
    }
    return -1;
  }

  void _sendExpandCollapseEvent(VTreeItem item, bool expanded) {
    var e = _createDefaultEvent(item.id);
    e.detail = 0;

    if (expanded) {
      widget.sendTreeExpand(state, e);
    } else {
      widget.sendTreeCollapse(state, e);
    }
  }

  List<Map<String, dynamic>> _testTreeItems() {
    final result = <Map<String, dynamic>>[];

    void visit(
      List<VWidget>? items,
      int level,
      bool visible,
      String? parentIdentifier,
    ) {
      if (items == null) return;
      for (final item in items) {
        if (item is! VTreeItem) continue;
        final identifier = treeItemIdentifier(item);
        result.add({
          'treeIdentifier': '${state.swt}/${state.id}',
          'identifier': identifier,
          'parentIdentifier': parentIdentifier,
          'text': treeItemText(item),
          'level': level,
          'expanded': item.expanded == true,
          'visible': visible,
        });
        visit(
          item.items,
          level + 1,
          visible && item.expanded == true,
          identifier,
        );
      }
    }

    visit(state.items, 0, true, null);
    return result;
  }

  bool _testExpandTreeItem(String itemIdentifier) {
    final item = _findTreeItemByIdentifier(itemIdentifier);
    if (item == null) return false;
    if (item.expanded == true) return true;
    _sendExpandCollapseEvent(item, true);
    return true;
  }

  VTreeItem? _findTreeItemByIdentifier(String itemIdentifier) {
    VTreeItem? found;

    void visit(List<VWidget>? items) {
      if (items == null || found != null) return;
      for (final item in items) {
        if (item is! VTreeItem) continue;
        if (treeItemIdentifier(item) == itemIdentifier) {
          found = item;
          return;
        }
        visit(item.items);
      }
    }

    visit(state.items);
    return found;
  }

  /// Java lays out no rows of its own, so every coordinate-to-row lookup it makes -- the hit test
  /// behind getItem(Point), a cell's bounds, and the tooltip a JFace viewer positions from them --
  /// counts from the top of the model. Tell it how far this tree has scrolled so it can take that
  /// off. Rounded to whole pixels and skipped when unchanged, which drops the notifications a
  /// ScrollController fires without movement; an actual drag still reports once per frame, the same
  /// rate a mouse move is throttled to. Reporting every settled position is what keeps the lookups
  /// correct, so it is not throttled further.
  ///
  /// The sideways scroll goes with it, as x: a cell's bounds are in the tree's client area, so
  /// Java takes it off the column positions to map a point to the column now under it.
  int _reportedScrollOffset = 0;
  int _reportedHorizontalOffset = 0;

  void _reportScrollOffset() {
    final controller = _verticalController;
    if (controller == null || !controller.hasClients) return;
    final offset = controller.offset.round();
    final horizontal = _horizontalController;
    final across = horizontal != null && horizontal.hasClients ? horizontal.offset.round() : 0;
    if (offset == _reportedScrollOffset && across == _reportedHorizontalOffset) return;
    _reportedScrollOffset = offset;
    _reportedHorizontalOffset = across;
    widget.sendEvent(state, "Scroll/Scroll", VEvent()..x = across..y = offset);
  }

  void _followRowsWithHeader() {
    final rows = _horizontalController;
    if (rows == null || !rows.hasClients || !_headerHorizontalController.hasClients) return;
    final header = _headerHorizontalController.position;
    final offset = rows.offset.clamp(header.minScrollExtent, header.maxScrollExtent);
    if (offset != header.pixels) header.jumpTo(offset);
  }

  @override
  void dispose() {
    unregisterTestTree(_testTreeKey, _testTreeHandle);
    _horizontalController?.dispose();
    _headerHorizontalController.dispose();
    _verticalController?.dispose();
    _focusNode.dispose();
    for (String eventName in eventNames) {
      EquoCommService.remove(eventName);
    }
    super.dispose();
  }
}

class TreeItemSwtWrapper extends StatelessWidget {
  final VTreeItem treeItem;
  final int level;
  final bool isCheckMode;
  final TreeSwt parentTree;
  final VTree parentTreeValue;
  final TreeImpl? treeImpl;
  final VFont? treeFont;
  final double? treeWidth;
  final bool renderChildItems;

  const TreeItemSwtWrapper({
    Key? key,
    required this.treeItem,
    required this.level,
    required this.isCheckMode,
    required this.parentTree,
    required this.parentTreeValue,
    this.treeImpl,
    this.treeFont,
    this.treeWidth,
    this.renderChildItems = true,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return TreeItemContextProvider(
      level: level,
      isCheckMode: isCheckMode,
      parentTree: parentTree,
      parentTreeValue: parentTreeValue,
      treeImpl: treeImpl,
      treeFont: treeFont,
      treeWidth: treeWidth,
      renderChildItems: renderChildItems,
      child: TreeItemSwt(
        value: treeItem,
        key: ValueKey('tree_item_${treeItem.id}'),
      ),
    );
  }
}

class TreeItemContext {
  final int level;
  final bool isCheckMode;
  final TreeSwt parentTree;
  final VTree parentTreeValue;
  final TreeImpl? treeImpl;
  final VFont? treeFont;
  final List<VTreeItem>? selection;
  final double? treeWidth;
  final bool renderChildItems;
  final bool treeFocused;

  TreeItemContext({
    required this.level,
    required this.isCheckMode,
    required this.parentTree,
    required this.parentTreeValue,
    this.treeImpl,
    this.treeFont,
    this.selection,
    this.treeWidth,
    this.renderChildItems = true,
    this.treeFocused = false,
  });

  static TreeItemContext? of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<TreeItemContextProvider>();
    return provider?.context;
  }
}

class TreeItemContextProvider extends InheritedWidget {
  final TreeItemContext context;

  TreeItemContextProvider({
    Key? key,
    required int level,
    required bool isCheckMode,
    required TreeSwt parentTree,
    required VTree parentTreeValue,
    TreeImpl? treeImpl,
    VFont? treeFont,
    double? treeWidth,
    bool renderChildItems = true,
    required Widget child,
  }) : context = TreeItemContext(
         level: level,
         isCheckMode: isCheckMode,
         parentTree: parentTree,
         parentTreeValue: parentTreeValue,
         treeImpl: treeImpl,
         treeFont: treeFont,
         selection: treeImpl?.state.selection,
         treeWidth: treeWidth,
         renderChildItems: renderChildItems,
         treeFocused: treeImpl?.hasFocus ?? false,
       ),
       super(key: key, child: child);

  @override
  bool updateShouldNotify(TreeItemContextProvider oldWidget) {
    final oldSelection = oldWidget.context.selection ?? [];
    final newSelection = context.selection ?? [];
    final selectionChanged =
        oldSelection.length != newSelection.length ||
        oldSelection.any(
          (item) => !newSelection.any((newItem) => newItem.id == item.id),
        ) ||
        newSelection.any(
          (item) => !oldSelection.any((oldItem) => oldItem.id == item.id),
        );

    return context.level != oldWidget.context.level ||
        context.isCheckMode != oldWidget.context.isCheckMode ||
        selectionChanged ||
        context.treeFocused != oldWidget.context.treeFocused;
  }
}

class TreeItemSearchResult {
  final bool found;
  final int index;
  final int level;

  TreeItemSearchResult({
    required this.found,
    required this.index,
    required this.level,
  });
}

class TreeHasMultiColumnProvider extends InheritedWidget {
  final bool hasMultiColumn;

  const TreeHasMultiColumnProvider({
    super.key,
    required this.hasMultiColumn,
    required super.child,
  });

  static bool of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<TreeHasMultiColumnProvider>();
    return provider?.hasMultiColumn ?? false;
  }

  @override
  bool updateShouldNotify(TreeHasMultiColumnProvider oldWidget) {
    return hasMultiColumn != oldWidget.hasMultiColumn;
  }
}

class TreeEffectiveColumnWidthsProvider extends InheritedWidget {
  final List<double>? effectiveWidths;

  /// The tree's gap at its left edge. A tree with columns draws it inside column 0, ahead of the
  /// row's own padding, so the column still starts at the tree's left edge.
  final double edgeGap;

  const TreeEffectiveColumnWidthsProvider({
    super.key,
    required this.effectiveWidths,
    this.edgeGap = 0.0,
    required super.child,
  });

  static List<double>? of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<
          TreeEffectiveColumnWidthsProvider
        >();
    return provider?.effectiveWidths;
  }

  static double edgeGapOf(BuildContext context) =>
      context
          .dependOnInheritedWidgetOfExactType<TreeEffectiveColumnWidthsProvider>()
          ?.edgeGap ??
      0.0;

  @override
  bool updateShouldNotify(TreeEffectiveColumnWidthsProvider oldWidget) {
    if (edgeGap != oldWidget.edgeGap) return true;
    if (effectiveWidths == null && oldWidget.effectiveWidths == null) {
      return false;
    }
    if (effectiveWidths == null || oldWidget.effectiveWidths == null) {
      return true;
    }
    if (effectiveWidths!.length != oldWidget.effectiveWidths!.length) {
      return true;
    }
    for (int i = 0; i < effectiveWidths!.length; i++) {
      if (effectiveWidths![i] != oldWidget.effectiveWidths![i]) return true;
    }
    return false;
  }
}

class TreeColumnIndexProvider extends InheritedWidget {
  final int columnIndex;

  const TreeColumnIndexProvider({
    super.key,
    required this.columnIndex,
    required super.child,
  });

  static int? of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<TreeColumnIndexProvider>();
    return provider?.columnIndex;
  }

  @override
  bool updateShouldNotify(TreeColumnIndexProvider oldWidget) {
    return columnIndex != oldWidget.columnIndex;
  }
}

class TreeLinesVisibleProvider extends InheritedWidget {
  final bool linesVisible;

  const TreeLinesVisibleProvider({
    super.key,
    required this.linesVisible,
    required super.child,
  });

  static bool of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<TreeLinesVisibleProvider>();
    return provider?.linesVisible ?? false;
  }

  @override
  bool updateShouldNotify(TreeLinesVisibleProvider oldWidget) {
    return linesVisible != oldWidget.linesVisible;
  }
}

/// What the Tree decides about one header cell: the sort indicator it shows and the colour of its text.
class TreeHeaderCellProvider extends InheritedWidget {
  final int sortDirection;
  final VColor? foreground;

  /// The display position a header cell dragged to a global position is dropped at.
  final int Function(Offset globalPosition)? dropIndexAt;

  const TreeHeaderCellProvider({
    super.key,
    required this.sortDirection,
    required this.foreground,
    this.dropIndexAt,
    required super.child,
  });

  static TreeHeaderCellProvider? of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<TreeHeaderCellProvider>();

  @override
  bool updateShouldNotify(TreeHeaderCellProvider oldWidget) =>
      sortDirection != oldWidget.sortDirection || foreground != oldWidget.foreground;
}

class TreeColumnIsLastProvider extends InheritedWidget {
  final bool isLastColumn;

  const TreeColumnIsLastProvider({
    super.key,
    required this.isLastColumn,
    required super.child,
  });

  static bool of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<TreeColumnIsLastProvider>();
    return provider?.isLastColumn ?? false;
  }

  @override
  bool updateShouldNotify(TreeColumnIsLastProvider oldWidget) {
    return isLastColumn != oldWidget.isLastColumn;
  }
}

/// A cell [width] wide, as a tree column lays it out. Its [child] is laid out at least
/// [minContentWidth] plus room for a label wide and clipped to the cell, so a column narrower than
/// what it holds clips it, as natively, instead of overflowing into the next one.
class TreeCellBox extends StatelessWidget {
  const TreeCellBox({
    super.key,
    required this.width,
    this.minContentWidth = 0.0,
    required this.child,
  });

  /// What a cell's label needs past its fixed content: padding, divider gap, an image, a sort arrow.
  static const double labelRoom = 64.0;

  final double width;
  final double minContentWidth;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final needed = minContentWidth + labelRoom;
    final layoutWidth = width < needed ? needed : width;
    return SizedBox(
      width: width,
      child: ClipRect(
        clipBehavior: layoutWidth > width ? Clip.hardEdge : Clip.none,
        child: OverflowBox(
          alignment: AlignmentDirectional.centerStart,
          fit: OverflowBoxFit.deferToChild,
          minWidth: layoutWidth,
          maxWidth: layoutWidth,
          child: child,
        ),
      ),
    );
  }
}
