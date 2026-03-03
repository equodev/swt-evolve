import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart';
import '../gen/font.dart';
import '../impl/composite_evolve.dart';
import '../impl/widget_config.dart';
import '../gen/swt.dart';
import '../gen/tree.dart';
import '../gen/treeitem.dart';
import '../gen/event.dart';
import '../comm/comm.dart';
import '../theme/theme_extensions/tree_theme_extension.dart';
import '../theme/theme_settings/tree_theme_settings.dart';
import 'utils/widget_utils.dart';
import '../gen/rectangle.dart';

class _FlatTreeItem {
  final VTreeItem item;
  final int level;
  const _FlatTreeItem(this.item, this.level);
}

class TreeImpl<T extends TreeSwt, V extends VTree> extends CompositeImpl<T, V> {
  final Map<dynamic, String> treeItemExpanders = {};
  final List<String> eventNames = [];
  TreeThemeExtension? _cachedWidgetTheme;
  List<double>? _cachedEffectiveWidths;
  List<VTreeItem>? _pendingSelection;
  int checkboxUpdateCounter = 0;
  final Map<Object, Map<String, bool>> _pendingCheckboxStates = {};
  Object? _lastSelectedItemId;
  ScrollController? _horizontalController;
  ScrollController? _verticalController;

  @override
  void initState() {
    super.initState();
    _horizontalController = ScrollController();
    _verticalController = ScrollController();
    _registerGetIdFromPointListener();
    _registerGetItemBoundsListener();
  }

  void _registerGetIdFromPointListener() {
    final eventName = "${state.swt}/${state.id}/GetIdFromPoint";
    EquoCommService.onRaw(eventName, (payload) {
      if (payload != null) {
        try {
          final Map<String, dynamic> point = payload is String
              ? (jsonDecode(payload) as Map<String, dynamic>)
              : Map<String, dynamic>.from(payload as Map);
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
    final itemHeight = theme.itemHeight + theme.itemPadding.vertical;
    final columns = getTreeColumns();

    // Find item flat index
    final itemIndex = findItemIndex(itemId);
    if (itemIndex < 0) return Rect.zero;

    // Y position
    double headerOffset = 0.0;
    if ((state.headerVisible == true || columns.isNotEmpty) &&
        columns.isNotEmpty) {
      headerOffset = theme.headerHeight;
    }
    final y = headerOffset + (itemIndex * itemHeight);

    // X position and width
    double x = 0.0;
    double width = state.bounds?.width?.toDouble() ?? 200.0;

    if (columns.isNotEmpty && _cachedEffectiveWidths != null) {
      final effectiveWidths = _cachedEffectiveWidths!;
      final prefix = theme.expandIconSize + theme.expandIconSpacing;
      x = prefix;

      for (int i = 0; i < columnIndex && i < effectiveWidths.length; i++) {
        x += effectiveWidths[i];
      }

      if (columnIndex < effectiveWidths.length) {
        width = effectiveWidths[columnIndex];
      }
    }

    return Rect.fromLTWH(x, y, width, itemHeight);
  }

  @override
  Widget build(BuildContext context) {
    _cachedWidgetTheme = Theme.of(context).extension<TreeThemeExtension>();
    return super.wrap(createTreeView());
  }

  @override
  void setValue(V value) {
    final incomingSelection = value.selection ?? [];
    final hasLocalSelection =
        _pendingSelection != null && _pendingSelection!.isNotEmpty;

    bool selectionsDiffer = false;
    if (hasLocalSelection && incomingSelection.isNotEmpty) {
      final pendingIds = Set.from(_pendingSelection!.map((item) => item.id));
      final incomingIds = Set.from(incomingSelection.map((item) => item.id));
      selectionsDiffer =
          pendingIds.length != incomingIds.length ||
          pendingIds.any((id) => !incomingIds.contains(id)) ||
          incomingIds.any((id) => !pendingIds.contains(id));
    }

    if ((incomingSelection.isEmpty || selectionsDiffer) && hasLocalSelection) {
      value.selection = List<VTreeItem>.from(_pendingSelection!);
      _pendingSelection = null;
      super.setValue(value);
      setState(() {});
    } else {
      if (incomingSelection.isNotEmpty) {
        _pendingSelection = null;
      }
      super.setValue(value);
    }

    if (_pendingCheckboxStates.isNotEmpty && value.items != null) {
      _preserveCheckboxStates(value.items!);
    }
  }

  void _preserveCheckboxStates(List<VWidget> items) {
    for (final item in items) {
      if (item is VTreeItem) {
        final pendingState = _pendingCheckboxStates[item.id];
        if (pendingState != null) {
          item.checked = pendingState['checked'];
          item.grayed = pendingState['grayed'];
        }

        if (item.items != null) {
          _preserveCheckboxStates(item.items!);
        }
      }
    }
  }

  Widget createTreeView() {
    return LayoutBuilder(
      builder: (context, constraints) {
        final widgetTheme = Theme.of(context).extension<TreeThemeExtension>();
        _cachedWidgetTheme = widgetTheme;

        final double screenWidth = MediaQuery.of(context).size.width;
        final double screenHeight = MediaQuery.of(context).size.height;
        final double maxWidth = constraints.maxWidth.isFinite
            ? constraints.maxWidth.clamp(0.0, screenWidth)
            : screenWidth;
        final double maxHeight = constraints.maxHeight.isFinite
            ? constraints.maxHeight.clamp(0.0, screenHeight)
            : screenHeight;

        final columns = getTreeColumns();
        final flatItems = _flattenVisibleItems(state.items, 0);
        final allItems = _collectAllTreeItems(state.items);
        final preferredWidths = columns.isEmpty
            ? <double>[]
            : _computePreferredColumnWidths(
                context,
                columns,
                allItems,
                widgetTheme!,
              );
        final effectiveWidths = columns.isEmpty
            ? null
            : _computeEffectiveColumnWidths(
                columns,
                preferredWidths,
                state.bounds?.width?.toDouble(),
                widgetTheme!,
              );
        _cachedEffectiveWidths = effectiveWidths;

        final backgroundColor = getTreeBackgroundColor(state, widgetTheme!);

        final bool hasVScroll = StyleBits(state.style).has(SWT.V_SCROLL);
        final bool hasHScroll = StyleBits(state.style).has(SWT.H_SCROLL);

        // Build editor overlays if editing is active
        final editorOverlays = _buildEditorOverlays(
          widgetTheme!,
          effectiveWidths,
        );

        return ScrollConfiguration(
          behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
          child: Focus(
            onKeyEvent: _handleKeyEvent,
            child: Stack(
              children: [
                Container(
                  width: maxWidth,
                  height: maxHeight,
                  color: backgroundColor,
                  child: TreeEffectiveColumnWidthsProvider(
                    effectiveWidths: effectiveWidths,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        if ((state.headerVisible == true ||
                                columns.isNotEmpty) &&
                            columns.isNotEmpty)
                          _buildHeaderWithLines(
                            columns,
                            effectiveWidths,
                            widgetTheme!,
                          ),
                        Expanded(
                          child: Material(
                            color: Colors.transparent,
                            child: _buildScrollableContent(
                              flatItems,
                              columns,
                              widgetTheme!,
                              context,
                              hasVScroll,
                              hasHScroll,
                              effectiveWidths,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
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

  List<VTreeItem> _collectAllTreeItems(List<VWidget>? items) {
    if (items == null) return [];
    final List<VTreeItem> result = [];
    for (final item in items) {
      if (item is VTreeItem) {
        result.add(item);
        result.addAll(_collectAllTreeItems(item.items));
      }
    }
    return result;
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

  List<double> _computePreferredColumnWidths(
    BuildContext context,
    List<VTreeColumn> columns,
    List<VTreeItem> allItems,
    TreeThemeExtension theme,
  ) {
    if (columns.isEmpty) return [];
    final isCheckMode = StyleBits(state.style).has(SWT.CHECK);
    final checkboxWidth = isCheckMode
        ? (theme.checkboxSize + theme.checkboxSpacing)
        : 0.0;
    final paddingH = theme.cellMultiColumnPadding.horizontal;
    final prefixBase = theme.expandIconSize + theme.expandIconSpacing;
    final iconSpace = theme.itemIconSize + theme.itemIconSpacing;

    final List<double> maxWidths = List.filled(columns.length, 0.0);

    void visit(List<VWidget>? items, int level) {
      if (items == null) return;
      for (final w in items) {
        if (w is! VTreeItem) continue;
        final item = w;
        final texts = item.texts ?? [];
        final firstText = item.text ?? (texts.isNotEmpty ? texts[0] ?? '' : '');
        final itemFont = item.font ?? state.font;
        final textStyle = getTextStyle(
          context: context,
          font: itemFont,
          textColor: theme.itemTextColor,
          baseTextStyle: theme.itemTextStyle,
        );

        for (int col = 0; col < columns.length; col++) {
          final String cellText = col == 0
              ? firstText
              : (col < texts.length ? texts[col] ?? '' : '');
          final painter = TextPainter(
            text: TextSpan(text: cellText, style: textStyle),
            textDirection: TextDirection.ltr,
            maxLines: 1,
          );
          painter.layout();
          double w = painter.width + paddingH;
          if (col == 0) {
            w += theme.itemIndent * level + prefixBase;
            if (isCheckMode) w += checkboxWidth;
            if (item.image != null) w += iconSpace;
          }
          if (w > maxWidths[col]) maxWidths[col] = w;
        }
        visit(item.items, level + 1);
      }
    }

    visit(state.items, 0);

    final defaultW = theme.columnDefaultWidth.toDouble();
    final minW = theme.columnMinWidth;
    final dividerGap = theme.columnDividerGap;
    for (int i = 0; i < maxWidths.length; i++) {
      if (maxWidths[i] < minW) maxWidths[i] = minW;
      final colMin = columns[i].width?.toDouble() ?? defaultW;
      if (maxWidths[i] < colMin) maxWidths[i] = colMin;
      if (i < maxWidths.length - 1) maxWidths[i] += dividerGap;
    }
    return maxWidths;
  }

  List<double> _computeEffectiveColumnWidths(
    List<VTreeColumn> columns,
    List<double> preferred,
    double? boundsWidth,
    TreeThemeExtension theme,
  ) {
    if (columns.isEmpty || preferred.isEmpty) return [];
    final prefix = theme.expandIconSize + theme.expandIconSpacing;
    if (boundsWidth == null || boundsWidth <= prefix) {
      return List.from(preferred);
    }
    final available = boundsWidth - prefix;
    double sum = preferred.reduce((a, b) => a + b);
    if (sum <= available) {
      final result = List<double>.from(preferred);
      result[result.length - 1] += available - sum;
      return result;
    }
    final scale = available / sum;
    return preferred.map((w) => w * scale).toList();
  }

  List<Widget> getTreeItems() {
    List<VTreeItem> treeItems = [];

    if (state.items != null && state.items!.isNotEmpty) {
      treeItems.addAll(state.items!.whereType<VTreeItem>());
    }

    final uniqueItems = <VTreeItem>[];
    final seenIds = <dynamic>{};
    for (final item in treeItems) {
      if (!seenIds.contains(item.id)) {
        seenIds.add(item.id);
        uniqueItems.add(item);
      }
    }

    return uniqueItems
        .map((treeItem) => getWidgetForTreeItem(treeItem, 0))
        .toList();
  }

  Widget _buildScrollableContent(
    List<_FlatTreeItem> flatItems,
    List<VTreeColumn> columns,
    TreeThemeExtension widgetTheme,
    BuildContext context,
    bool hasVScroll,
    bool hasHScroll,
    List<double>? effectiveWidths,
  ) {
    double? totalWidth = _calculateTotalWidth(
      hasHScroll,
      columns,
      widgetTheme,
      effectiveWidths,
    );
    Widget listViewWithLines = _buildListViewWithColumnLines(
      flatItems,
      columns,
      widgetTheme,
      effectiveWidths,
    );

    if (hasVScroll && hasHScroll) {
      return _buildBothScrollbars(context, listViewWithLines, totalWidth);
    } else if (hasVScroll) {
      return _buildVerticalScrollbar(context, listViewWithLines);
    } else if (hasHScroll) {
      return _buildHorizontalScrollbar(context, listViewWithLines, totalWidth);
    } else {
      return _buildNoScrollbars(context, listViewWithLines);
    }
  }

  double? _calculateTotalWidth(
    bool hasHScroll,
    List<VTreeColumn> columns,
    TreeThemeExtension widgetTheme,
    List<double>? effectiveWidths,
  ) {
    if (!hasHScroll || columns.isEmpty) return null;
    final boundsW = state.bounds?.width?.toDouble();
    if (boundsW != null && boundsW > 0) return boundsW;
    if (effectiveWidths != null && effectiveWidths.length == columns.length) {
      final prefix = widgetTheme.expandIconSize + widgetTheme.expandIconSpacing;
      return prefix + effectiveWidths.reduce((a, b) => a + b);
    }
    double calculatedWidth = 0.0;
    for (final column in columns) {
      calculatedWidth +=
          (column.width ?? widgetTheme.columnDefaultWidth.round()).toDouble();
    }
    return widgetTheme.expandIconSize +
        widgetTheme.expandIconSpacing +
        calculatedWidth;
  }

  Widget _buildListViewWithColumnLines(
    List<_FlatTreeItem> flatItems,
    List<VTreeColumn> columns,
    TreeThemeExtension widgetTheme,
    List<double>? effectiveWidths,
  ) {
    final bool linesVisible = state.linesVisible ?? false;

    Widget listView = ListView.builder(
      controller: _verticalController,
      itemCount: flatItems.length,
      itemBuilder: (context, index) {
        return _buildFlatTreeItemWidget(flatItems[index]);
      },
    );

    if (!linesVisible || columns.isEmpty) {
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

  Widget _buildBothScrollbars(
    BuildContext context,
    Widget listViewWithLines,
    double? totalWidth,
  ) {
    Widget content = listViewWithLines;
    if (totalWidth != null) {
      content = SizedBox(width: totalWidth, child: content);
    }

    return Scrollbar(
      controller: _verticalController,
      thumbVisibility: true,
      notificationPredicate: (notification) => notification.depth == 1,
      child: Scrollbar(
        controller: _horizontalController,
        thumbVisibility: true,
        notificationPredicate: (notification) => notification.depth == 0,
        child: ScrollConfiguration(
          behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
          child: SingleChildScrollView(
            controller: _horizontalController,
            scrollDirection: Axis.horizontal,
            child: content,
          ),
        ),
      ),
    );
  }

  Widget _buildVerticalScrollbar(
    BuildContext context,
    Widget listViewWithLines,
  ) {
    return Scrollbar(
      controller: _verticalController,
      thumbVisibility: true,
      child: ScrollConfiguration(
        behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
        child: listViewWithLines,
      ),
    );
  }

  Widget _buildHorizontalScrollbar(
    BuildContext context,
    Widget listViewWithLines,
    double? totalWidth,
  ) {
    Widget content = listViewWithLines;
    if (totalWidth != null) {
      content = SizedBox(width: totalWidth, child: content);
    }

    return Scrollbar(
      controller: _horizontalController,
      thumbVisibility: true,
      child: ScrollConfiguration(
        behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
        child: SingleChildScrollView(
          controller: _horizontalController,
          scrollDirection: Axis.horizontal,
          child: content,
        ),
      ),
    );
  }

  Widget _buildNoScrollbars(BuildContext context, Widget content) {
    return ScrollConfiguration(
      behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
      child: SingleChildScrollView(
        scrollDirection: Axis.vertical,
        child: SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: content,
        ),
      ),
    );
  }

  Widget _buildHeaderWithLines(
    List<VTreeColumn> columns,
    List<double>? effectiveWidths,
    TreeThemeExtension widgetTheme,
  ) {
    final prefix = widgetTheme.expandIconSize + widgetTheme.expandIconSpacing;
    final defaultWidth = widgetTheme.columnDefaultWidth.round();
    final borderWidth = widgetTheme.columnBorderWidth;

    final List<double> linePositions = [];
    double cumulativeWidth = 0.0;

    return Stack(
      children: [
        Container(
          height: widgetTheme.headerHeight,
          color: Colors.transparent,
          child: TreeLinesVisibleProvider(
            linesVisible: state.linesVisible ?? false,
            child: Row(
              children: [
                ...columns.asMap().entries.map((entry) {
                  final int columnIndex = entry.key;
                  final column = entry.value;
                  final double width =
                      effectiveWidths != null &&
                          columnIndex < effectiveWidths.length
                      ? effectiveWidths[columnIndex]
                      : (column.width ?? defaultWidth).toDouble();
                  cumulativeWidth += width;
                  if (columnIndex < columns.length - 1) {
                    linePositions.add(cumulativeWidth - borderWidth);
                  }
                  if (columnIndex == 0) {
                    return SizedBox(
                      width: width,
                      child: Row(
                        children: [
                          SizedBox(width: prefix),
                          Expanded(
                            child: getWidgetForTreeColumn(column, columnIndex),
                          ),
                        ],
                      ),
                    );
                  }
                  return SizedBox(
                    width: width,
                    child: getWidgetForTreeColumn(column, columnIndex),
                  );
                }),
              ],
            ),
          ),
        ),
        Positioned(
          left: 0,
          right: 0,
          bottom: 0,
          child: Container(height: 1.0, color: widgetTheme.headerBorderColor),
        ),
        if (state.linesVisible == true && linePositions.isNotEmpty)
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
  }

  Widget _buildTreeContentWithLines(
    List<Widget> treeItems,
    List<VTreeColumn> columns,
    TreeThemeExtension widgetTheme,
    List<double>? effectiveWidths,
  ) {
    final bool linesVisible = state.linesVisible ?? false;

    Widget itemsColumn = SizedBox(
      width: double.infinity,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: treeItems,
      ),
    );

    if (!linesVisible || columns.isEmpty) {
      return itemsColumn;
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
        itemsColumn,
        ...linePositions.map(
          (position) => Positioned(
            left: position,
            top: 0,
            bottom: 0,
            child: Container(
              width: widgetTheme.columnBorderWidth,
              color: widgetTheme.columnRightBorderColor,
            ),
          ),
        ),
      ],
    );
  }

  Widget getWidgetForTreeColumn(VTreeColumn column, int columnIndex) {
    final columns = getTreeColumns();
    final bool isLastColumn = columnIndex == columns.length - 1;
    return TreeColumnIndexProvider(
      columnIndex: columnIndex,
      child: TreeColumnIsLastProvider(
        isLastColumn: isLastColumn,
        child: TreeColumnSwt(value: column),
      ),
    );
  }

  Widget getWidgetForTreeItem(VTreeItem treeItem, int level) {
    setupTreeItemExpandListener(treeItem);

    final treeWidth = state.bounds?.width?.toDouble();

    return TreeItemSwtWrapper(
      key: ValueKey(
        'tree_item_${treeItem.id}_${treeItem.checked}_${treeItem.grayed}',
      ),
      treeItem: treeItem,
      level: level,
      isCheckMode: getTreeViewSelectionMode(),
      parentTree: widget,
      parentTreeValue: state,
      treeImpl: this,
      treeFont: state.font,
      treeWidth: treeWidth,
    );
  }

  Widget _buildFlatTreeItemWidget(_FlatTreeItem flatItem) {
    setupTreeItemExpandListener(flatItem.item);

    final treeWidth = state.bounds?.width?.toDouble();

    return TreeItemSwtWrapper(
      key: ValueKey(
        'tree_item_${flatItem.item.id}_${flatItem.item.checked}_${flatItem.item.grayed}',
      ),
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

  void handleTreeItemSelection(
    Object itemId, {
    bool isCtrlPressed = false,
    bool isShiftPressed = false,
  }) {
    if (state.enabled != true) return;
    final item = _findTreeItemById(itemId);
    if (item == null) return;

    final bool isMultiMode = StyleBits(state.style).has(SWT.MULTI);
    setState(() {
      if (isMultiMode) {
        final flatIndex = _findFlatIndexForItem(itemId, 0, 0).index;
        _handleMultiSelection(item, flatIndex, isCtrlPressed, isShiftPressed);
      } else {
        _handleSingleSelection(item, 0);
      }
    });
  }

  void _handleSingleSelection(VTreeItem item, int flatIndex) {
    state.selection = [item];
    _pendingSelection = [item];
    _lastSelectedItemId = item.id;
  }

  void _handleMultiSelection(
    VTreeItem item,
    int flatIndex,
    bool isCtrlPressed,
    bool isShiftPressed,
  ) {
    final currentSelection = List<VTreeItem>.from(state.selection ?? []);

    if (isCtrlPressed) {
      if (currentSelection.any((selected) => selected.id == item.id)) {
        currentSelection.removeWhere((selected) => selected.id == item.id);
      } else {
        currentSelection.add(item);
      }
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

    final itemHeight =
        _cachedWidgetTheme!.itemHeight +
        _cachedWidgetTheme!.itemPadding.vertical;

    // Account for header if visible
    double headerOffset = 0.0;
    final columns = getTreeColumns();
    if ((state.headerVisible == true || columns.isNotEmpty) &&
        columns.isNotEmpty) {
      headerOffset = _cachedWidgetTheme!.headerHeight;
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

  /// Builds editor overlay widgets for all active editors.
  List<Widget> _buildEditorOverlays(
    TreeThemeExtension widgetTheme,
    List<double>? effectiveWidths,
  ) {
    final editors = state.editors;
    if (editors == null || editors.isEmpty) return [];

    final columns = getTreeColumns();
    final itemHeight =
        widgetTheme.itemHeight + widgetTheme.itemPadding.vertical;
    double headerOffset = 0.0;
    if ((state.headerVisible == true || columns.isNotEmpty) &&
        columns.isNotEmpty) {
      headerOffset = widgetTheme.headerHeight;
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
      double editorWidth = state.bounds?.width?.toDouble() ?? 200.0;

      if (columns.isNotEmpty && effectiveWidths != null) {
        for (int i = 0; i < columnIndex && i < effectiveWidths.length; i++) {
          editorX += effectiveWidths[i];
        }

        if (columnIndex < effectiveWidths.length) {
          editorWidth = effectiveWidths[columnIndex];
        }
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
        handleTreeItemSelection(newItemId, isCtrlPressed: isCtrlPressed);
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
        handleTreeItemSelection(newItemId, isCtrlPressed: isCtrlPressed);
      }
    }
  }

  void _navigateLeft() {
    final selectedItems = state.selection ?? [];
    if (selectedItems.isEmpty) return;

    final selectedItem = selectedItems.last;
    if (selectedItem.expanded == true &&
        (selectedItem.items?.isNotEmpty ?? false)) {
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
    if (selectedItem.expanded != true &&
        (selectedItem.items?.isNotEmpty ?? false)) {
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

      widget.sendSelectionSelection(state, e);
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
        handleTreeItemSelection(firstItemId, isCtrlPressed: isCtrlPressed);
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
        handleTreeItemSelection(lastItemId, isCtrlPressed: isCtrlPressed);
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

  List<VTreeItem> _getAllDescendants(VTreeItem item) {
    final List<VTreeItem> descendants = [];
    if (item.items != null) {
      for (final child in item.items!) {
        descendants.add(child);
        descendants.addAll(_getAllDescendants(child));
      }
    }
    return descendants;
  }

  void _addParentChain(VTreeItem item, List<VTreeItem> selection) {
    var currentParentId = _findParentItemId(item.id);
    while (currentParentId != null) {
      final parent = _findTreeItemById(currentParentId);
      if (parent != null &&
          !selection.any((selected) => selected.id == parent.id)) {
        selection.insert(0, parent);
        currentParentId = _findParentItemId(parent.id);
      } else {
        break;
      }
    }
  }

  void handleCheckboxCascade(Object itemId, bool newCheckedState) {
    if (state.enabled != true) return;
    final item = _findTreeItemById(itemId);
    if (item == null) return;

    setState(() {
      checkboxUpdateCounter++;

      item.checked = newCheckedState;
      item.grayed = false;
      _pendingCheckboxStates[itemId] = {
        'checked': newCheckedState,
        'grayed': false,
      };

      if (item.items != null && item.items!.isNotEmpty) {
        final allDescendants = _getAllDescendants(item);
        for (final descendant in allDescendants) {
          descendant.checked = newCheckedState;
          descendant.grayed = false;
          _pendingCheckboxStates[descendant.id] = {
            'checked': newCheckedState,
            'grayed': false,
          };
        }
      }

      final parentId = _findParentItemId(itemId);
      if (parentId != null) {
        _updateParentCheckboxState(parentId);
      }
    });
  }

  void _updateParentCheckboxState(Object parentId) {
    final parent = _findTreeItemById(parentId);
    if (parent == null || parent.items == null) return;

    final children = parent.items!.whereType<VTreeItem>().toList();
    if (children.isEmpty) return;

    final checkedCount = children
        .where((child) => child.checked == true)
        .length;

    parent.checked = checkedCount > 0;
    parent.grayed = false;
    _pendingCheckboxStates[parentId] = {
      'checked': checkedCount > 0,
      'grayed': false,
    };

    final grandParentId = _findParentItemId(parentId);
    if (grandParentId != null) {
      _updateParentCheckboxState(grandParentId);
    }
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

  @override
  void dispose() {
    _horizontalController?.dispose();
    _verticalController?.dispose();
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
  final int checkboxUpdateCounter;
  final double? treeWidth;
  final bool renderChildItems;

  TreeItemContext({
    required this.level,
    required this.isCheckMode,
    required this.parentTree,
    required this.parentTreeValue,
    this.treeImpl,
    this.treeFont,
    this.selection,
    this.checkboxUpdateCounter = 0,
    this.treeWidth,
    this.renderChildItems = true,
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
         checkboxUpdateCounter: treeImpl?.checkboxUpdateCounter ?? 0,
         treeWidth: treeWidth,
         renderChildItems: renderChildItems,
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

    final checkboxChanged =
        oldWidget.context.checkboxUpdateCounter !=
        context.checkboxUpdateCounter;

    return context.level != oldWidget.context.level ||
        context.isCheckMode != oldWidget.context.isCheckMode ||
        selectionChanged ||
        checkboxChanged;
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

class TreeEffectiveColumnWidthsProvider extends InheritedWidget {
  final List<double>? effectiveWidths;

  const TreeEffectiveColumnWidthsProvider({
    super.key,
    required this.effectiveWidths,
    required super.child,
  });

  static List<double>? of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<
          TreeEffectiveColumnWidthsProvider
        >();
    return provider?.effectiveWidths;
  }

  @override
  bool updateShouldNotify(TreeEffectiveColumnWidthsProvider oldWidget) {
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
