import 'package:fluent_ui/fluent_ui.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/swt/treecolumn.dart';
import 'package:swtflutter/src/swt/widget.dart';
import '../impl/composite_impl.dart';
import '../swt/swt.dart';
import '../swt/tree.dart';
import '../swt/treeitem.dart';
import '../comm/comm.dart';

class TreeImpl<T extends TreeSwt, V extends TreeValue>
    extends CompositeImpl<T, V> {
  final List<TreeColumnValue> columns = [];
  final List<String> eventNames = [];

  @override
  Widget build(BuildContext context) {
    extractAndRemoveTreeColumns();
    return super.wrap(createTreeWithHeader());
  }

  List<Expanded> buildHeaders() {
    return columns
        .map((treeColumnValue) =>
            Expanded(child: Row(children: [Text(treeColumnValue.text ?? "")])))
        .toList();
  }

  Widget createTreeWithHeader() {
    return LayoutBuilder(
      builder: (context, constraints) {
        final double maxWidth = constraints.maxWidth.isFinite
            ? constraints.maxWidth
            : MediaQuery.of(context).size.width;
        final double maxHeight = constraints.maxHeight.isFinite
            ? constraints.maxHeight
            : MediaQuery.of(context).size.height;
        return SizedBox(
          width: maxWidth,
          height: maxHeight,
          child: ListView(shrinkWrap: true, children: [
            if (state.headerVisible == true)
              Row(
                children: buildHeaders(),
              ),
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [buildTreeView()],
            )
          ]),
        );
      },
    );
  }

  TreeViewSelectionMode getTreeViewSelectionMode() {
    return StyleBits(state.style).has(SWT.CHECK)
        ? TreeViewSelectionMode.multiple
        : TreeViewSelectionMode.single;
  }

  Widget buildTreeView() {
    onSelectionChanged(Iterable<TreeViewItem> selectedItems) async {
      List<dynamic> selectedIds = selectedItems
          .map((item) => item.value)
          .toList();
      widget.sendSelectionSelection(state, selectedIds);
    }

    return Expanded(
      child: TreeView(
        selectionMode: getTreeViewSelectionMode(),
        items: buildTreeViewItemsWithColumns(state.children),
        onSelectionChanged: onSelectionChanged,
      ),
    );
  }

  List<TreeViewItem> buildTreeViewItemsWithColumns(List<WidgetValue>? items) {
    if (items == null || items.isEmpty) return [];

    return items
        .whereType<TreeItemValue>()
        .map((treeItemDynamic) => buildTreeViewItemWithColumns(treeItemDynamic))
        .toList();
  }

  List<String> getTreeColumns(TreeItemValue treeItemValue) {
    final List<String>? treeItemTexts = treeItemValue.texts;
    if (treeItemTexts != null) {
      return treeItemTexts;
    }
    final String? treeItemText = treeItemValue.text;
    if (treeItemText != null) {
      return [treeItemText];
    }
    return [];
  }

  bool isExpanded(WidgetValue widgetValue) {
    return (widgetValue as TreeItemValue).expanded ?? false;
  }

  TreeViewItem buildTreeViewItemWithColumns(TreeItemValue treeItemValue) {
    addTreeItemExpandListener(treeItemValue);

    final content = super.applyMenu(Row(
      children: buildTreeColumns(getTreeColumns(treeItemValue)),
    ));
    onExpandToggle(_, expanded) async {
      if (expanded) {
        widget.sendTreeExpand(state, treeItemValue.id);
      } else {
        setState(() {
          treeItemValue.children = [TreeItemValue()];
          treeItemValue.expanded = false;
        });
        widget.sendTreeCollapse(state, treeItemValue.id);
      }
    }

    final children = buildTreeViewItemsWithColumns(treeItemValue.children);

    return TreeViewItem(
      value: treeItemValue.id,
      content: content,
      expanded: isExpanded(treeItemValue),
      lazy: isVirtual(),
      onExpandToggle: onExpandToggle,
      children: children,
      selected: treeItemValue.selected
    );
  }

  List<WidgetValue> filterNonEmptyTreeItems(List<WidgetValue>? items) {
    if (items == null) return [];

    List<WidgetValue> newItems = items
        .whereType<TreeItemValue>()
        .where((treeItem) => treeItem.texts != null || treeItem.text != null)
        .toList();

    for (WidgetValue item in newItems) {
      if (isExpanded(item)) {
        item.children = filterNonEmptyTreeItems(item.children);
      }
    }
    return newItems;
  }

  void addTreeItemExpandListener(TreeItemValue treeItemValue) {
    String eventName = "TreeItem/${treeItemValue.id}/TreeItem/Expand";
    eventNames.add(eventName);
    EquoCommService.on<TreeItemValue>(eventName, (TreeItemValue payload) {
      setState(() {
        treeItemValue.children = filterNonEmptyTreeItems(payload.children);
        treeItemValue.expanded = true;
      });
    });
  }

  List<Widget> buildTreeColumns(List<String>? items) {
    if (items == null) return [];
    return items.map((item) => Expanded(child: Text(item))).toList();
  }

  void extractAndRemoveTreeColumns() {
    state.children?.removeWhere((child) {
      if (child is TreeColumnValue) {
        columns.add(child);
        return true;
      }
      return false;
    });
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
