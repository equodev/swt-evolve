import 'package:flutter/material.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/swt/treecolumn.dart';
import 'package:swtflutter/src/swt/widget.dart';
import '../impl/composite_impl.dart';
import '../impl/widget_config.dart';
import '../swt/swt.dart';
import '../swt/tree.dart';
import '../swt/treeitem.dart';
import '../comm/comm.dart';

class TreeImpl<T extends TreeSwt, V extends TreeValue>
    extends CompositeImpl<T, V> {
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

        return Container(
          width: maxWidth,
          height: maxHeight,
          color: useDarkTheme ? const Color(0xFF1E1E1E) : Colors.white,
          child: Column(
            children: [
              // Header row with TreeColumn widgets
              if (state.headerVisible == true && columns.isNotEmpty)
                Row(
                  children: columns.map((column) =>
                      Expanded(
                        child: getWidgetForTreeColumn(column),
                      )
                  ).toList(),
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
        );
      },
    );
  }

  List<TreeColumnValue> getTreeColumns() {
    if (state.children == null) {
      return [];
    }
    return state.children!
        .whereType<TreeColumnValue>()
        .toList();
  }

  List<Widget> getTreeItems() {
    if (state.children == null) {
      return [];
    }
    return state.children!
        .whereType<TreeItemValue>()
        .map((treeItem) => getWidgetForTreeItem(treeItem, 0))
        .toList();
  }

  Widget getWidgetForTreeColumn(TreeColumnValue column) {
    return TreeColumnSwt(value: column);
  }

  Widget getWidgetForTreeItem(TreeItemValue treeItem, int level) {
    // Set up expansion listener
    setupTreeItemExpandListener(treeItem);

    // Create the item UI wrapper with data needed for rendering
    return TreeItemSwtWrapper(
      treeItem: treeItem,
      level: level,
      isCheckMode: getTreeViewSelectionMode(),
      parentTree: widget,
      parentTreeValue: state,
    );
  }

  bool getTreeViewSelectionMode() {
    return StyleBits(state.style).has(SWT.CHECK);
  }

  void setupTreeItemExpandListener(TreeItemValue treeItemValue) {
    String eventName = "TreeItem/${treeItemValue.id}/TreeItem/Expand";

    // Don't set up the same listener twice
    if (treeItemExpanders.containsKey(treeItemValue.id)) {
      return;
    }

    eventNames.add(eventName);
    treeItemExpanders[treeItemValue.id] = eventName;

    EquoCommService.on<TreeItemValue>(eventName, (TreeItemValue payload) {
      setState(() {
        treeItemValue.children = filterNonEmptyTreeItems(payload.children);
        treeItemValue.expanded = true;
      });
    });
  }

  List<WidgetValue> filterNonEmptyTreeItems(List<WidgetValue>? items) {
    if (items == null) return [];

    List<WidgetValue> newItems = items
        .whereType<TreeItemValue>()
        .where((treeItem) => treeItem.texts != null || treeItem.text != null)
        .toList();

    for (WidgetValue item in newItems) {
      if (item is TreeItemValue && (item.expanded ?? false)) {
        item.children = filterNonEmptyTreeItems(item.children);
      }
    }
    return newItems;
  }

  bool isVirtual() {
    return StyleBits(state.style).has(SWT.VIRTUAL);
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
  final TreeItemValue treeItem;
  final int level;
  final bool isCheckMode;
  final TreeSwt parentTree;
  final TreeValue parentTreeValue;

  const TreeItemSwtWrapper({
    Key? key,
    required this.treeItem,
    required this.level,
    required this.isCheckMode,
    required this.parentTree,
    required this.parentTreeValue,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // Store context data in InheritedWidget
    return TreeItemContextProvider(
      level: level,
      isCheckMode: isCheckMode,
      parentTree: parentTree,
      parentTreeValue: parentTreeValue,
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
  final TreeValue parentTreeValue;

  TreeItemContext({
    required this.level,
    required this.isCheckMode,
    required this.parentTree,
    required this.parentTreeValue,
  });

  static TreeItemContext? of(BuildContext context) {
    final provider = context.dependOnInheritedWidgetOfExactType<TreeItemContextProvider>();
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
    required TreeValue parentTreeValue,
    required Widget child,
  }) : context = TreeItemContext(
    level: level,
    isCheckMode: isCheckMode,
    parentTree: parentTree,
    parentTreeValue: parentTreeValue,
  ),
        super(key: key, child: child);

  @override
  bool updateShouldNotify(TreeItemContextProvider oldWidget) {
    return context.level != oldWidget.context.level ||
        context.isCheckMode != oldWidget.context.isCheckMode;
  }
}