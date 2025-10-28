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

class TreeImpl<T extends TreeSwt, V extends VTree> extends CompositeImpl<T, V> {
  final Map<dynamic, String> treeItemExpanders = {};
  final List<String> eventNames = [];
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    return super.wrap(createTreeView());
  }

  Widget createTreeView() {
    return LayoutBuilder(
      builder: (context, constraints) {
        final double maxWidth = constraints.maxWidth.isFinite
            ? constraints.maxWidth
            : MediaQuery.of(context).size.width;
        final double maxHeight = constraints.maxHeight.isFinite
            ? constraints.maxHeight
            : MediaQuery.of(context).size.height;

        // Extract columns from children
        final columns = getTreeColumns();
        final treeItems = getTreeItems();

        // Column and item extraction complete

        return Focus(
          onKeyEvent: _handleKeyEvent,
          child: Container(
            width: maxWidth,
            height: maxHeight,
            color: useDarkTheme ? const Color(0xFF1E1E1E) : Colors.white,
            child: Column(
              children: [
                // Header row with TreeColumn widgets
                if ((state.headerVisible == true || columns.isNotEmpty) &&
                    columns.isNotEmpty)
                  Container(
                    decoration: BoxDecoration(
                      color: useDarkTheme
                          ? const Color(0xFF2B2B2B)
                          : Colors.grey.shade100,
                      border: Border(
                        bottom: BorderSide(
                          color: useDarkTheme
                              ? Colors.black45
                              : Colors.grey.shade400,
                          width: 2.0,
                        ),
                      ),
                    ),
                    child: Row(
                      children: columns.map((column) {
                        final width = column.width ?? 100;
                        return SizedBox(
                          width: width.toDouble(),
                          child: getWidgetForTreeColumn(column),
                        );
                      }).toList(),
                    ),
                  ),
                // Tree content
                Expanded(
                  child: Material(
                    color: Colors.transparent,
                    child: SingleChildScrollView(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: treeItems,
                      ),
                    ),
                  ),
                )
              ],
            ),
          ),
        );
      },
    );
  }

  List<VTreeColumn> getTreeColumns() {
    List<VTreeColumn> columns = [];

    // First try to get columns from TreeValue.columns (preferred)
    if (state.columns != null && state.columns!.isNotEmpty) {
      columns.addAll(state.columns!);
    }

    // Also check children for TreeColumnValue objects (backward compatibility)
    if (state.items != null) {
      final childColumns = state.items!.whereType<VTreeColumn>().toList();
      columns.addAll(childColumns);
    }

    // Remove duplicates (in case a column appears in both places)
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

    // Get tree items from state.items (filtering for VTreeItem only)
    if (state.items != null && state.items!.isNotEmpty) {
      treeItems.addAll(state.items!.whereType<VTreeItem>());
    }

    // Remove duplicates (in case an item appears in both places)
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

  Widget getWidgetForTreeColumn(VTreeColumn column) {
    return TreeColumnSwt(value: column);
  }

  Widget getWidgetForTreeItem(VTreeItem treeItem, int level) {
    // Set up expansion listener
    setupTreeItemExpandListener(treeItem);

    // Create the item UI wrapper with data needed for rendering
    return TreeItemSwtWrapper(
      treeItem: treeItem,
      level: level,
      isCheckMode: getTreeViewSelectionMode(),
      parentTree: widget,
      parentTreeValue: state,
      treeImpl: this, // Pass tree implementation for selection management
      treeFont: state.font, // Pass tree font for fallback
    );
  }

  bool getTreeViewSelectionMode() {
    return StyleBits(state.style).has(SWT.CHECK);
  }

  void setupTreeItemExpandListener(VTreeItem treeItemValue) {
    String eventName = "TreeItem/${treeItemValue.id}/TreeItem/Expand";

    // Don't set up the same listener twice
    if (treeItemExpanders.containsKey(treeItemValue.id)) {
      return;
    }

    eventNames.add(eventName);
    treeItemExpanders[treeItemValue.id] = eventName;

    EquoCommService.on<VTreeItem>(eventName, (VTreeItem payload) {
      // Received expand event for item ${treeItemValue.id}, setting expanded=true
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
            // Filter out items that are completely empty
            (treeItem.texts != null &&
                treeItem.texts!.any((text) => text.isNotEmpty)) ||
            (treeItem.text != null && treeItem.text!.isNotEmpty) ||
            // Also keep items that have valid children even if text is empty
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

  // Selection management methods using VTree.selection field
  void handleTreeItemSelection(Object itemId,
      {bool isCtrlPressed = false, bool isShiftPressed = false}) {
    final flatIndex = _findFlatIndexForItem(itemId, 0, 0).index;
    print(
        'Tree selection: Item ID=$itemId, FlatIndex=$flatIndex, Ctrl=$isCtrlPressed, Shift=$isShiftPressed');

    final bool isMultiMode = StyleBits(state.style).has(SWT.MULTI);
    final item = _findTreeItemById(itemId);

    if (item != null) {
      setState(() {
        if (isMultiMode) {
          _handleMultiSelection(item, flatIndex, isCtrlPressed, isShiftPressed);
        } else {
          _handleSingleSelection(item, flatIndex);
        }
      });
    }
  }

  void _handleSingleSelection(VTreeItem item, int flatIndex) {
    // Set selection to single item using VTree.selection field
    state.selection = [item];
  }

  void _handleMultiSelection(
      VTreeItem item, int flatIndex, bool isCtrlPressed, bool isShiftPressed) {
    final currentSelection = List<VTreeItem>.from(state.selection ?? []);

    if (isCtrlPressed) {
      // Ctrl+click: toggle selection
      if (currentSelection.any((selected) => selected.id == item.id)) {
        currentSelection.removeWhere((selected) => selected.id == item.id);
      } else {
        currentSelection.add(item);
      }
    } else if (isShiftPressed && currentSelection.isNotEmpty) {
      // Shift+click: range selection
      final lastSelectedItem = currentSelection.last;
      final lastIndex = _findFlatIndexForItem(lastSelectedItem.id, 0, 0).index;
      final rangeItems = _getItemsInRange(lastIndex, flatIndex);
      currentSelection.clear();
      currentSelection.addAll(rangeItems);
    } else {
      // Regular click: single selection (clear others)
      currentSelection.clear();
      currentSelection.add(item);
    }

    state.selection = currentSelection;
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

        // If expanded, process children
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

  bool isMultiSelectionMode() {
    return StyleBits(state.style).has(SWT.MULTI);
  }

  Set<Object> getSelectedItemIds() {
    return Set.from(state.selection?.map((item) => item.id) ?? []);
  }

  void selectAll() {
    if (!isMultiSelectionMode()) return;

    setState(() {
      // Collect all items
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

  // Selection state is now managed through state.selection field

  // Helper method to find flat index of an item
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

        // If expanded, search children
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

  // Getters for selection state
  Object? get selectedItemId =>
      state.selection?.isNotEmpty == true ? state.selection!.last.id : null;

  // Public method to find the flat index of an item
  int findItemIndex(Object itemId) {
    final result = _findFlatIndexForItem(itemId, 0, 0);
    return result.found ? result.index : -1;
  }

  // Hierarchical path methods removed - using flat indexes only

  // Keyboard navigation handler
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
      // Collapse expanded item
      setState(() {
        selectedItem.expanded = false;
      });
      // Send collapse event
      _sendExpandCollapseEvent(selectedItem, false);
    } else {
      // Navigate to parent
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
      // Expand collapsed item
      setState(() {
        selectedItem.expanded = true;
      });
      // Send expand event
      _sendExpandCollapseEvent(selectedItem, true);
    } else if (selectedItem.expanded == true &&
        (selectedItem.items?.isNotEmpty ?? false)) {
      // Navigate to first child
      final firstChildId = selectedItem.items!.first.id;
      handleTreeItemSelection(firstChildId);
    }
  }

  void _handleEnterKey() {
    final selectedItems = state.selection ?? [];
    if (selectedItems.isEmpty) return;

    // Send default selection event
    final selectedItem = selectedItems.last;
    // Create a proper Event object for enter/default selection
    var e = VEvent();
    e.index = _findFlatIndexForItem(selectedItem.id, 0, 0).index;
    e.detail = 0; // No detail for enter key
    e.x = 0;
    e.y = 0;
    e.width = 100;
    e.height = 20;

    // Send DefaultSelection event for Enter key
    widget.sendSelectionDefaultSelection(state, e);
  }

  void _handleSpaceKey() {
    final selectedItems = state.selection ?? [];
    if (selectedItems.isEmpty) return;

    final selectedItem = selectedItems.last;
    if (getTreeViewSelectionMode()) {
      // Handle checkbox toggle in CHECK mode
      setState(() {
        selectedItem.checked = !(selectedItem.checked ?? false);
      });

      // Send selection event with CHECK detail
      var e = VEvent();
      e.index = _findFlatIndexForItem(selectedItem.id, 0, 0).index;
      e.detail = SWT.CHECK; // Indicate this is a checkbox event
      e.x = 0;
      e.y = 0;
      e.width = 100;
      e.height = 20;

      widget.sendSelectionSelection(state, e);
    } else {
      // Regular space selection
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

  // Helper methods for keyboard navigation
  List<Object> _getAllVisibleItemIds() {
    final result = <Object>[];
    _collectVisibleItemIds(state.items ?? [], result);
    return result;
  }

  void _collectVisibleItemIds(List<VWidget> items, List<Object> result) {
    for (final item in items) {
      if (item is VTreeItem) {
        result.add(item.id);
        // Only include children if the item is expanded
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
    var e = VEvent();
    e.index = _findFlatIndexForItem(item.id, 0, 0).index;
    e.detail = 0; // Remove detail for expand/collapse
    e.x = 0;
    e.y = 0;
    e.width = 100;
    e.height = 20;

    if (expanded) {
      widget.sendTreeExpand(state, e);
    } else {
      widget.sendTreeCollapse(state, e);
    }
  }

  @override
  void dispose() {
    super.dispose();
    for (String eventName in eventNames) {
      EquoCommService.remove(eventName);
    }
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

  const TreeItemSwtWrapper({
    Key? key,
    required this.treeItem,
    required this.level,
    required this.isCheckMode,
    required this.parentTree,
    required this.parentTreeValue,
    this.treeImpl,
    this.treeFont,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // Store context data in InheritedWidget
    return TreeItemContextProvider(
      level: level,
      isCheckMode: isCheckMode,
      parentTree: parentTree,
      parentTreeValue: parentTreeValue,
      treeImpl: treeImpl,
      treeFont: treeFont,
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
  final TreeImpl? treeImpl; // Add reference to tree implementation
  final VFont? treeFont; // Add tree font for fallback

  TreeItemContext({
    required this.level,
    required this.isCheckMode,
    required this.parentTree,
    required this.parentTreeValue,
    this.treeImpl,
    this.treeFont,
  });

  static TreeItemContext? of(BuildContext context) {
    final provider =
        context.dependOnInheritedWidgetOfExactType<TreeItemContextProvider>();
    return provider?.context;
  }
}

// InheritedWidget to provide context down the tree
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
    required Widget child,
  })  : context = TreeItemContext(
          level: level,
          isCheckMode: isCheckMode,
          parentTree: parentTree,
          parentTreeValue: parentTreeValue,
          treeImpl: treeImpl,
          treeFont: treeFont,
        ),
        super(key: key, child: child);

  @override
  bool updateShouldNotify(TreeItemContextProvider oldWidget) {
    return context.level != oldWidget.context.level ||
        context.isCheckMode != oldWidget.context.isCheckMode;
  }
}

// Helper class for tree item search results
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
