import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/widget.dart';
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

class TreeImpl<T extends TreeSwt, V extends VTree> extends CompositeImpl<T, V> {
  final Map<dynamic, String> treeItemExpanders = {};
  final List<String> eventNames = [];
  TreeThemeExtension? _cachedWidgetTheme;
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
  }


  @override
  Widget build(BuildContext context) {
    _cachedWidgetTheme = Theme.of(context).extension<TreeThemeExtension>();
    return super.wrap(createTreeView());
  }

  @override
  void setValue(V value) {
    final incomingSelection = value.selection ?? [];
    final hasLocalSelection = _pendingSelection != null && _pendingSelection!.isNotEmpty;
    
    bool selectionsDiffer = false;
    if (hasLocalSelection && incomingSelection.isNotEmpty) {
      final pendingIds = Set.from(_pendingSelection!.map((item) => item.id));
      final incomingIds = Set.from(incomingSelection.map((item) => item.id));
      selectionsDiffer = pendingIds.length != incomingIds.length ||
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
        
        final double maxWidth = constraints.maxWidth.isFinite
            ? constraints.maxWidth
            : MediaQuery.of(context).size.width;
        final double maxHeight = constraints.maxHeight.isFinite
            ? constraints.maxHeight
            : MediaQuery.of(context).size.height;

        final columns = getTreeColumns();
        final treeItems = getTreeItems();

        final backgroundColor = getTreeBackgroundColor(state, widgetTheme!);

        final bool hasVScroll = StyleBits(state.style).has(SWT.V_SCROLL);
        final bool hasHScroll = StyleBits(state.style).has(SWT.H_SCROLL);
        
        return ScrollConfiguration(
          behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
          child: Focus(
            onKeyEvent: _handleKeyEvent,
            child: Container(
              width: maxWidth,
              height: maxHeight,
              color: backgroundColor,
              child: Column(
                children: [
                  if ((state.headerVisible == true || columns.isNotEmpty) &&
                      columns.isNotEmpty)
                    Stack(
                      children: [
                        Container(
                          height: widgetTheme!.headerHeight,
                          color: Colors.transparent,
                          child: TreeLinesVisibleProvider(
                            linesVisible: state.linesVisible ?? false,
                            child: Row(
                              children: columns.asMap().entries.map((entry) {
                                final int columnIndex = entry.key;
                                final column = entry.value;
                                final defaultWidth = widgetTheme!.columnDefaultWidth.round();
                                final int width = column.width ?? defaultWidth;
                                return SizedBox(
                                  width: width.toDouble(),
                                  child: getWidgetForTreeColumn(column, columnIndex),
                                );
                              }).toList(),
                            ),
                          ),
                        ),
                        Positioned(
                          left: 0,
                          right: 0,
                          bottom: 0,
                          child: Container(
                            height: 1.0,
                            color: widgetTheme.headerBorderColor,
                          ),
                        ),
                      ],
                    ),
                  Expanded(
                    child: Material(
                      color: Colors.transparent,
                      child: _buildScrollableContent(
                        treeItems,
                        columns,
                        widgetTheme!,
                        context,
                        hasVScroll,
                        hasHScroll,
                      ),
                    ),
                  )
                ],
              ),
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
    List<Widget> treeItems,
    List<VTreeColumn> columns,
    TreeThemeExtension widgetTheme,
    BuildContext context,
    bool hasVScroll,
    bool hasHScroll,
  ) {
    Widget content = _buildTreeContentWithLines(
      treeItems,
      columns,
      widgetTheme,
    );
    
    double? totalWidth = _calculateTotalWidth(hasHScroll, columns, widgetTheme);
    Widget horizontalContent = _buildHorizontalContent(content, totalWidth);
    
    if (hasVScroll && hasHScroll) {
      return _buildBothScrollbars(context, horizontalContent);
    } else if (hasVScroll) {
      return _buildVerticalScrollbar(context, content);
    } else if (hasHScroll) {
      return _buildHorizontalScrollbar(context, horizontalContent);
    } else {
      return _buildNoScrollbars(context, content);
    }
  }

  double? _calculateTotalWidth(bool hasHScroll, List<VTreeColumn> columns, TreeThemeExtension widgetTheme) {
    if (!hasHScroll || columns.isEmpty) return null;
    double calculatedWidth = 0.0;
    for (final column in columns) {
      calculatedWidth += (column.width ?? widgetTheme.columnDefaultWidth.round()).toDouble();
    }
    return calculatedWidth;
  }

  Widget _buildHorizontalContent(Widget content, double? totalWidth) {
    if (totalWidth != null) {
      return SizedBox(width: totalWidth, child: content);
    }
    return content;
  }

  Widget _buildBothScrollbars(BuildContext context, Widget horizontalContent) {
    Widget horizontalScrollView = SingleChildScrollView(
      controller: _horizontalController,
      scrollDirection: Axis.horizontal,
      child: horizontalContent,
    );
    
    return Scrollbar(
      controller: _verticalController,
      thumbVisibility: true,
      notificationPredicate: (notification) => notification.depth == 0,
      child: Scrollbar(
        controller: _horizontalController,
        thumbVisibility: true,
        notificationPredicate: (notification) => notification.depth == 1,
        child: ScrollConfiguration(
          behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
          child: SingleChildScrollView(
            controller: _verticalController,
            scrollDirection: Axis.vertical,
            child: horizontalScrollView,
          ),
        ),
      ),
    );
  }

  Widget _buildVerticalScrollbar(BuildContext context, Widget content) {
    return Scrollbar(
      controller: _verticalController,
      thumbVisibility: true,
      child: ScrollConfiguration(
        behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
        child: SingleChildScrollView(
          controller: _verticalController,
          scrollDirection: Axis.vertical,
          child: content,
        ),
      ),
    );
  }

  Widget _buildHorizontalScrollbar(BuildContext context, Widget horizontalContent) {
    return Scrollbar(
      controller: _horizontalController,
      thumbVisibility: true,
      child: ScrollConfiguration(
        behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
        child: SingleChildScrollView(
          controller: _horizontalController,
          scrollDirection: Axis.horizontal,
          child: horizontalContent,
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

  Widget _buildTreeContentWithLines(
    List<Widget> treeItems,
    List<VTreeColumn> columns,
    TreeThemeExtension widgetTheme,
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
    
    for (int i = 0; i < columns.length; i++) {
      final column = columns[i];
      final int width = column.width ?? widgetTheme.columnDefaultWidth.round();
      
      cumulativeWidth += width.toDouble();
      
      if (i < columns.length - 1) {
        linePositions.add(cumulativeWidth);
      }
    }
    
    return Stack(
      children: [
        itemsColumn,
        ...linePositions.map((position) => Positioned(
          left: position,
          top: 0,
          bottom: 0,
          child: Container(
            width: widgetTheme.columnBorderWidth,
            color: widgetTheme.columnRightBorderColor,
          ),
        )),
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
      key: ValueKey('tree_item_${treeItem.id}_${treeItem.checked}_${treeItem.grayed}'),
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
        .where((treeItem) =>
            (treeItem.texts != null &&
                treeItem.texts!.any((text) => text.isNotEmpty)) ||
            (treeItem.text != null && treeItem.text!.isNotEmpty) ||
            (treeItem.items != null &&
                treeItem.items!.whereType<VTreeItem>().any((child) =>
                    (child.text != null && child.text!.isNotEmpty) ||
                    (child.texts != null &&
                        child.texts!.any((t) => t.isNotEmpty)))))
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

  void handleTreeItemSelection(Object itemId,
      {bool isCtrlPressed = false, bool isShiftPressed = false}) {
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
      VTreeItem item, int flatIndex, bool isCtrlPressed, bool isShiftPressed) {
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

  int _collectItemsInRange(List<VWidget> items, int currentIndex,
      int startIndex, int endIndex, List<VTreeItem> result) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (currentIndex >= startIndex && currentIndex <= endIndex) {
          result.add(item);
        }
        currentIndex++;

        if (item.expanded == true && item.items != null) {
          currentIndex = _collectItemsInRange(
              item.items!, currentIndex, startIndex, endIndex, result);
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
      List<VWidget> items, int currentIndex, int targetIndex) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (currentIndex == targetIndex) {
          return item;
        }
        currentIndex++;

        if (item.expanded == true && item.items != null) {
          final result = _getItemAtFlatIndexRecursive(
              item.items!, currentIndex, targetIndex);
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
      Object itemId, int currentIndex, int currentLevel) {
    if (state.items == null) {
      return TreeItemSearchResult(
          found: false, index: currentIndex, level: currentLevel);
    }

    return _findFlatIndexRecursive(
        state.items!, itemId, currentIndex, currentLevel);
  }

  TreeItemSearchResult _findFlatIndexRecursive(
      List<VWidget> items, Object itemId, int currentIndex, int currentLevel) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (item.id == itemId) {
          return TreeItemSearchResult(
              found: true, index: currentIndex, level: currentLevel);
        }

        currentIndex++;

        if (item.expanded == true && item.items != null) {
          final result = _findFlatIndexRecursive(
              item.items!, itemId, currentIndex, currentLevel + 1);
          if (result.found) {
            return result;
          }
          currentIndex = result.index;
        }
      }
    }

    return TreeItemSearchResult(
        found: false, index: currentIndex, level: currentLevel);
  }

  Object? get selectedItemId =>
      state.selection?.isNotEmpty == true ? state.selection!.last.id : null;

  int findItemIndex(Object itemId) {
    final result = _findFlatIndexForItem(itemId, 0, 0);
    return result.found ? result.index : -1;
  }

  KeyEventResult _handleKeyEvent(FocusNode node, KeyEvent event) {
    if (event is! KeyDownEvent) {
      return KeyEventResult.ignored;
    }

    final bool isCtrlPressed = HardwareKeyboard.instance.logicalKeysPressed
            .contains(LogicalKeyboardKey.controlLeft) ||
        HardwareKeyboard.instance.logicalKeysPressed
            .contains(LogicalKeyboardKey.controlRight);
    final bool isShiftPressed = HardwareKeyboard.instance.logicalKeysPressed
            .contains(LogicalKeyboardKey.shiftLeft) ||
        HardwareKeyboard.instance.logicalKeysPressed
            .contains(LogicalKeyboardKey.shiftRight);

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

      case LogicalKeyboardKey.enter:
        _handleEnterKey();
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

  void _handleEnterKey() {
    final selectedItems = state.selection ?? [];
    if (selectedItems.isEmpty) return;

    final selectedItem = selectedItems.last;
    var e = _createDefaultEvent(selectedItem.id);
    
    widget.sendSelectionDefaultSelection(state, e);
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
      List<VWidget> items, Object childId, Object? parentId) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (item.id == childId) {
          return parentId;
        }
        if (item.items != null) {
          final found =
              _findParentItemIdRecursive(item.items!, childId, item.id);
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
      if (parent != null && !selection.any((selected) => selected.id == parent.id)) {
        selection.insert(0, parent);
        currentParentId = _findParentItemId(parent.id);
      } else {
        break;
      }
    }
  }

  void handleCheckboxCascade(Object itemId, bool newCheckedState) {
    final item = _findTreeItemById(itemId);
    if (item == null) return;

    setState(() {
      checkboxUpdateCounter++;
      
      item.checked = newCheckedState;
      item.grayed = false;
      _pendingCheckboxStates[itemId] = {'checked': newCheckedState, 'grayed': false};

      if (item.items != null && item.items!.isNotEmpty) {
        final allDescendants = _getAllDescendants(item);
        for (final descendant in allDescendants) {
          descendant.checked = newCheckedState;
          descendant.grayed = false;
          _pendingCheckboxStates[descendant.id] = {'checked': newCheckedState, 'grayed': false};
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

    final checkedCount = children.where((child) => child.checked == true).length;

    parent.checked = checkedCount > 0;
    parent.grayed = false;
    _pendingCheckboxStates[parentId] = {'checked': checkedCount > 0, 'grayed': false};

    final grandParentId = _findParentItemId(parentId);
    if (grandParentId != null) {
      _updateParentCheckboxState(grandParentId);
    }
  }

  int _getItemLevel(Object itemId) {
    return _getItemLevelRecursive(state.items ?? [], itemId, 0);
  }

  int _getItemLevelRecursive(
      List<VWidget> items, Object itemId, int currentLevel) {
    for (final item in items) {
      if (item is VTreeItem) {
        if (item.id == itemId) {
          return currentLevel;
        }
        if (item.items != null) {
          final level =
              _getItemLevelRecursive(item.items!, itemId, currentLevel + 1);
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
  });

  static TreeItemContext? of(BuildContext context) {
    final provider =
        context.dependOnInheritedWidgetOfExactType<TreeItemContextProvider>();
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
    required Widget child,
  })  : context = TreeItemContext(
          level: level,
          isCheckMode: isCheckMode,
          parentTree: parentTree,
          parentTreeValue: parentTreeValue,
          treeImpl: treeImpl,
          treeFont: treeFont,
          selection: treeImpl?.state.selection,
          checkboxUpdateCounter: treeImpl?.checkboxUpdateCounter ?? 0,
          treeWidth: treeWidth,
        ),
        super(key: key, child: child);

  @override
  bool updateShouldNotify(TreeItemContextProvider oldWidget) {
    final oldSelection = oldWidget.context.selection ?? [];
    final newSelection = context.selection ?? [];
    final selectionChanged = oldSelection.length != newSelection.length ||
        oldSelection.any((item) => !newSelection.any((newItem) => newItem.id == item.id)) ||
        newSelection.any((item) => !oldSelection.any((oldItem) => oldItem.id == item.id));
    
    final checkboxChanged = oldWidget.context.checkboxUpdateCounter != context.checkboxUpdateCounter;
    
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

class TreeColumnIndexProvider extends InheritedWidget {
  final int columnIndex;

  const TreeColumnIndexProvider({
    super.key,
    required this.columnIndex,
    required super.child,
  });

  static int? of(BuildContext context) {
    final provider = context.dependOnInheritedWidgetOfExactType<TreeColumnIndexProvider>();
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
    final provider = context.dependOnInheritedWidgetOfExactType<TreeLinesVisibleProvider>();
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
    final provider = context.dependOnInheritedWidgetOfExactType<TreeColumnIsLastProvider>();
    return provider?.isLastColumn ?? false;
  }

  @override
  bool updateShouldNotify(TreeColumnIsLastProvider oldWidget) {
    return isLastColumn != oldWidget.isLastColumn;
  }
}
