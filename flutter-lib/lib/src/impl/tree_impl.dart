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
  final List<TreeColumnValue> columns = [];
  final List<String> eventNames = [];
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    extractAndRemoveTreeColumns();
    return super.wrap(createTreeWithHeader());
  }

  Widget buildHeader(TreeColumnValue treeColumnValue) {
    final Color textColor = useDarkTheme ? Colors.white70 : Colors.black87;

    return MouseRegion(
      cursor: SystemMouseCursors.click,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 6.0),
        decoration: BoxDecoration(
          color: useDarkTheme ? const Color(0xFF333333) : Colors.grey.shade200,
          border: Border(
            bottom: BorderSide(
              color: useDarkTheme ? Colors.black38 : Colors.grey.shade400,
              width: 1.0,
            ),
          ),
        ),
        child: Text(
          treeColumnValue.text ?? "",
          style: TextStyle(
            color: textColor,
            fontWeight: FontWeight.w500,
            fontSize: 14,
          ),
        ),
      ),
    );
  }

  List<Widget> buildHeaders() {
    return columns
        .map((treeColumnValue) => Expanded(child: buildHeader(treeColumnValue)))
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
        return Container(
          width: maxWidth,
          height: maxHeight,
          color: useDarkTheme ? const Color(0xFF1E1E1E) : Colors.white,
          child: Column(
            children: [
              if (state.headerVisible == true)
                Row(
                  children: buildHeaders(),
                ),
              Expanded(
                child: buildTreeView(),
              )
            ],
          ),
        );
      },
    );
  }

  bool getTreeViewSelectionMode() {
    return StyleBits(state.style).has(SWT.CHECK);
  }

  Widget buildTreeView() {
    return Material(
      color: Colors.transparent,
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: buildTreeItems(state.children, 0),
        ),
      ),
    );
  }

  List<Widget> buildTreeItems(List<WidgetValue>? items, int level) {
    if (items == null || items.isEmpty) return [];

    return items
        .whereType<TreeItemValue>()
        .map((treeItem) => buildTreeItem(treeItem, level))
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

  Widget buildExpandIcon(TreeItemValue treeItem, bool hasChildren) {
    final bool expanded = treeItem.expanded ?? false;

    if (!hasChildren) {
      return const SizedBox(width: 16);
    }

    return MouseRegion(
      cursor: SystemMouseCursors.click,
      child: GestureDetector(
        onTap: () {
          if (expanded) {
            setState(() {
              treeItem.children = [TreeItemValue()];
              treeItem.expanded = false;
            });
            widget.sendTreeCollapse(state, treeItem.id);
          } else {
            widget.sendTreeExpand(state, treeItem.id);
          }
        },
        child: Icon(
          expanded ? Icons.keyboard_arrow_down : Icons.keyboard_arrow_right,
          size: 16,
          color: useDarkTheme ? Colors.white70 : Colors.black54,
        ),
      ),
    );
  }

  Widget buildCheckbox(TreeItemValue treeItem) {
    if (!getTreeViewSelectionMode()) return const SizedBox.shrink();

    final bool checked = treeItem.checked ?? false;
    final bool grayed = treeItem.grayed ?? false;

    return Container(
      margin: const EdgeInsets.only(right: 4),
      child: SizedBox(
        width: 16,
        height: 16,
        child: Checkbox(
          value: checked,
          tristate: grayed,
          activeColor: const Color(0xFF6366F1),
          side: BorderSide(
            color: useDarkTheme ? Colors.white70 : Colors.grey.shade600,
            width: 1.5,
          ),
          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
          onChanged: (value) {
            setState(() {
              treeItem.checked = value ?? false;
            });
            widget.sendSelectionSelection(state, treeItem.id);
          },
        ),
      ),
    );
  }

  Widget buildIcon(TreeItemValue treeItem) {
    final IconData iconData;

    if (treeItem.children != null && treeItem.children!.isNotEmpty) {
      iconData = treeItem.expanded ?? false
          ? Icons.folder_open
          : Icons.folder;
    } else {
      iconData = Icons.insert_drive_file;
    }

    return Container(
      margin: const EdgeInsets.only(right: 4),
      child: Icon(
        iconData,
        size: 16,
        color: useDarkTheme ? Colors.white70 : Colors.grey.shade700,
      ),
    );
  }

  Widget buildTreeItemRow(TreeItemValue treeItem, int level) {
    final List<String> columns = getTreeColumns(treeItem);
    final bool hasChildren = treeItem.children != null && treeItem.children!.isNotEmpty;
    final bool isSelected = treeItem.selected ?? false;
    final Color textColor = useDarkTheme ? Colors.white : Colors.black87;
    final Color bgColor = isSelected
        ? (useDarkTheme ? const Color(0xFF3C3C3C) : const Color(0xFFE8E8FF))
        : Colors.transparent;

    return MouseRegion(
      cursor: SystemMouseCursors.click,
      onEnter: (_) => widget.sendMouseTrackMouseEnter(state, null),
      onExit: (_) => widget.sendMouseTrackMouseExit(state, null),
      child: GestureDetector(
        onTap: () {
          setState(() {
            treeItem.selected = true;
          });
          widget.sendSelectionSelection(state, treeItem.id);
        },
        onDoubleTap: () {
          widget.sendSelectionDefaultSelection(state, treeItem.id);
        },
        child: Container(
          decoration: BoxDecoration(
            color: bgColor,
          ),
          child: Row(
            children: [
              SizedBox(width: 8.0 + level * 16.0),
              buildExpandIcon(treeItem, hasChildren),
              buildCheckbox(treeItem),
              buildIcon(treeItem),
              Expanded(
                child: Container(
                  padding: const EdgeInsets.symmetric(vertical: 4.0),
                  child: Text(
                    columns.isNotEmpty ? columns[0] : "",
                    style: TextStyle(
                      color: textColor,
                      fontSize: 14,
                    ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget buildTreeItem(TreeItemValue treeItem, int level) {
    addTreeItemExpandListener(treeItem);

    final bool expanded = treeItem.expanded ?? false;
    final bool hasChildren = treeItem.children != null && treeItem.children!.isNotEmpty;

    return Focus(
      onFocusChange: (hasFocus) {
        if (hasFocus) {
          widget.sendFocusFocusIn(state, null);
        } else {
          widget.sendFocusFocusOut(state, null);
        }
      },
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          buildTreeItemRow(treeItem, level),
          if (expanded && hasChildren)
            ...buildTreeItems(treeItem.children, level + 1),
        ],
      ),
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