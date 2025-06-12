import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'dart:io';
import 'package:swtflutter/src/impl/tree_impl.dart';
import '../swt/treeitem.dart';
import '../impl/item_impl.dart';
import '../impl/widget_config.dart';
import 'icons_map.dart';

class TreeItemImpl<T extends TreeItemSwt, V extends TreeItemValue>
    extends ItemImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  // Cache data from parent InheritedWidgets - obtained in build method
  TreeItemContext? _context;

  @override
  Widget build(BuildContext context) {
    // Get the TreeItemContext from the widget tree
    _context = TreeItemContext.of(context);

    // If there's no context, return a basic item
    if (_context == null) {
      return Text(state.text ?? "");
    }

    // Build the tree item with the context
    return buildTreeItemContent(context);
  }

  Widget buildTreeItemContent(BuildContext context) {
    final String text = state.text ?? "";
    final List<String>? texts = state.texts;
    final bool expanded = state.expanded ?? false;
    final bool selected = state.selected ?? false;
    final bool hasChildren = state.children != null && state.children!.isNotEmpty;
    final bool isCheckMode = _context?.isCheckMode ?? false;
    final bool checked = state.checked ?? false;
    final bool grayed = state.grayed ?? false;
    final int level = _context?.level ?? 0;
    final String? image = state.image;

    final Color textColor = useDarkTheme ? Colors.white : Colors.black87;
    final Color bgColor = selected
        ? (useDarkTheme ? const Color(0xFF3C3C3C) : const Color(0xFFE8E8FF))
        : Colors.transparent;

    return MouseRegion(
      cursor: SystemMouseCursors.click,
      onEnter: (_) => _context?.parentTree.sendMouseTrackMouseEnter(_context!.parentTreeValue, null),
      onExit: (_) => _context?.parentTree.sendMouseTrackMouseExit(_context!.parentTreeValue, null),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // TreeItem row
          GestureDetector(
            onTap: () {
              _context?.parentTree.sendSelectionSelection(_context!.parentTreeValue, state.id);
              setState(() {
                state.selected = true;
              });
            },
            onDoubleTap: () {
              _context?.parentTree.sendSelectionDefaultSelection(_context!.parentTreeValue, state.id);
            },
            child: Container(
              decoration: BoxDecoration(
                color: bgColor,
              ),
              child: Row(
                children: [
                  // Indentation based on level
                  SizedBox(width: 8.0 + level * 16.0),

                  // Expand/collapse icon
                  hasChildren
                      ? MouseRegion(
                    cursor: SystemMouseCursors.click,
                    child: GestureDetector(
                      onTap: () {
                        if (expanded) {
                          setState(() {
                            state.expanded = false;
                          });
                          _context?.parentTree.sendTreeCollapse(
                              _context!.parentTreeValue,
                              state.id
                          );
                        } else {
                          if (state.children != null && state.children!.isNotEmpty &&
                              !(state.children!.length == 1 &&
                                  state.children![0] is TreeItemValue &&
                                  (state.children![0] as TreeItemValue).text == null &&
                                  (state.children![0] as TreeItemValue).texts == null)) {
                            setState(() {
                              state.expanded = true;
                            });
                          } else {
                            _context?.parentTree.sendTreeExpand(
                                _context!.parentTreeValue,
                                state.id
                            );
                          }
                        }
                      },
                      child: Icon(
                        expanded ? Icons.keyboard_arrow_down : Icons.keyboard_arrow_right,
                        size: 16,
                        color: useDarkTheme ? Colors.white70 : Colors.black54,
                      ),
                    ),
                  )
                      : const SizedBox(width: 16),

                  // Checkbox (if in check mode)
                  if (isCheckMode)
                    Container(
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
                              state.checked = value ?? false;
                            });
                            _context?.parentTree.sendSelectionSelection(
                                _context!.parentTreeValue,
                                state.id
                            );
                          },
                        ),
                      ),
                    ),

                  // Item icon - Modificado para usar la variable image
                  Container(
                    margin: const EdgeInsets.only(right: 4),
                    child: (image != null)
                        ? (materialIconMap.containsKey(image)
                        ? Icon(
                      getMaterialIconByName(image),
                      size: 16,
                      color: useDarkTheme ? Colors.white70 : Colors.grey.shade700,
                    )
                        : (image.toLowerCase().endsWith('.svg')
                        ? SvgPicture.file(
                      File(image),
                      width: 16,
                      height: 16,
                      color: useDarkTheme ? Colors.white70 : Colors.grey.shade700,
                    )
                        : Image.file(
                      File(image),
                      width: 16,
                      height: 16,
                    )))
                        : Icon(
                      hasChildren
                          ? (expanded ? Icons.folder_open : Icons.folder)
                          : Icons.insert_drive_file,
                      size: 16,
                      color: useDarkTheme ? Colors.white70 : Colors.grey.shade700,
                    ),
                  ),

                  // Text content
                  Expanded(
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 4.0),
                      child: Text(
                        texts != null && texts.isNotEmpty ? texts[0] : text,
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

          // Child items (if expanded)
          if (expanded && hasChildren)
            ...buildChildItems(),
        ],
      ),
    );
  }

  List<Widget> buildChildItems() {
    final List<TreeItemValue> childItems = state.children
        ?.whereType<TreeItemValue>()
        .toList() ?? [];

    return childItems.map((childItem) {
      return TreeItemContextProvider(
        level: (_context?.level ?? 0) + 1,
        isCheckMode: _context?.isCheckMode ?? false,
        parentTree: _context!.parentTree,
        parentTreeValue: _context!.parentTreeValue,
        child: TreeItemSwt(
          value: childItem,
          key: ValueKey('tree_child_item_${childItem.id}'),
        ),
      );
    }).toList();
  }
}