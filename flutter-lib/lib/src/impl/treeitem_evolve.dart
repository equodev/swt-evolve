import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'dart:io';
import 'package:swtflutter/src/impl/tree_evolve.dart';
import '../gen/treeitem.dart';
import '../gen/treecolumn.dart';
import '../impl/item_evolve.dart';
import '../impl/widget_config.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import 'icons_map.dart';

class TreeItemImpl<T extends TreeItemSwt, V extends VTreeItem>
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
    final bool hasChildren = state.items != null && state.items!.isNotEmpty;
    final bool isCheckMode = _context?.isCheckMode ?? false;
    final bool checked = state.checked ?? false;
    final bool grayed = state.grayed ?? false;
    final int level = _context?.level ?? 0;
    final String? image = null; //state.image;

    // Use centralized selection management from VTree.selection
    final bool selected = _context?.treeImpl?.isItemSelected(state.id) ?? false;

    final Color textColor = selected
        ? (useDarkTheme ? Colors.white : Colors.white)
        : (useDarkTheme ? Colors.white : Colors.black87);
    final Color bgColor = selected
        ? const Color(0xFF2196F3)
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
              // Use centralized selection management
              _context?.treeImpl?.handleTreeItemSelection(state.id);

              // Create a proper Event object for selection
              var e = VEvent();
              if (_context?.treeImpl != null) {
                // Use flat index only
                e.index = _context!.treeImpl!.findItemIndex(state.id);
                e.detail = 0; // Remove level detail
                // Set bounds information to prevent NPE in JFace
                e.x = 0;
                e.y = 0;
                e.width = 100;  // Default width
                e.height = 20;  // Default height for tree item
              }

              _context?.parentTree.sendSelectionSelection(_context!.parentTreeValue, e);
            },
            onDoubleTap: () {
              // Create a proper Event object for double-click selection
              var e = VEvent();
              if (_context?.treeImpl != null) {
                // Use flat index only
                e.index = _context!.treeImpl!.findItemIndex(state.id);
                e.detail = 0; // Remove level detail
                // Set bounds information to prevent NPE in JFace
                e.x = 0;
                e.y = 0;
                e.width = 100;  // Default width
                e.height = 20;  // Default height for tree item
              }

              _context?.parentTree.sendSelectionDefaultSelection(_context!.parentTreeValue, e);
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

                          // Create event with flat index
                          var e = VEvent();
                          if (_context?.treeImpl != null) {
                            // Use flat index only
                            e.index = _context!.treeImpl!.findItemIndex(state.id);
                            e.detail = 0; // Remove level detail
                            // Set bounds information to prevent NPE in JFace
                            e.x = 0;
                            e.y = 0;
                            e.width = 100;  // Default width
                            e.height = 20;  // Default height for tree item
                          }

                          _context?.parentTree.sendTreeCollapse(
                              _context!.parentTreeValue,
                              e
                          );
                        } else {
                          // Don't set expanded state immediately - wait for Java response
                          // setState(() {
                          //   state.expanded = true;
                          // });
                          // Expanding item at level $level, id=${state.id}

                          // Create event with flat index and always send expand event
                          var e = VEvent();
                          if (_context?.treeImpl != null) {
                            // Use flat index only
                            e.index = _context!.treeImpl!.findItemIndex(state.id);
                            e.detail = 0; // Remove level detail
                            // Set bounds information to prevent NPE in JFace
                            e.x = 0;
                            e.y = 0;
                            e.width = 100;  // Default width
                            e.height = 20;  // Default height for tree item
                          }

                          _context?.parentTree.sendTreeExpand(
                              _context!.parentTreeValue,
                              e
                          );
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
                          value: grayed ? null : checked,
                          tristate: true,
                          activeColor: const Color(0xFF6366F1),
                          side: BorderSide(
                            color: useDarkTheme ? Colors.white70 : Colors.grey.shade600,
                            width: 1.5,
                          ),
                          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                          onChanged: (value) {
                            setState(() {
                              if (value == null) {
                                // Tri-state: grayed/indeterminate
                                state.checked = false;
                                state.grayed = true;
                              } else {
                                // Binary state: checked or unchecked
                                state.checked = value;
                                state.grayed = false;
                              }
                            });
                            // Use centralized selection management for checkbox too
                            _context?.treeImpl?.handleTreeItemSelection(state.id);

                            // Create a proper Event object for checkbox selection
                            var e = VEvent();
                            if (_context?.treeImpl != null) {
                              // Use flat index only
                              e.index = _context!.treeImpl!.findItemIndex(state.id);
                              e.detail = SWT.CHECK; // Indicate this is a checkbox event
                              // Set bounds information to prevent NPE in JFace
                              e.x = 0;
                              e.y = 0;
                              e.width = 100;  // Default width
                              e.height = 20;  // Default height for tree item
                            }

                            _context?.parentTree.sendSelectionSelection(
                                _context!.parentTreeValue,
                                e
                            );
                          },
                        ),
                      ),
                    ),

                  // Item icon - Modificado para usar la variable image
                  Container(
                    margin: const EdgeInsets.only(right: 4),
                    child: (image != null)
                        ? (getEclipseIcon(image) != null
                        ? Icon(
                      getEclipseIcon(image)!,
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

                  // Text content - support multi-column layout
                  Expanded(
                    child: _buildTextContent(texts, text, textColor),
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
    final List<VTreeItem> childItems = state.items
        ?.whereType<VTreeItem>()
        // Filter out completely empty child items
        .where((childItem) => 
            (childItem.text != null && childItem.text!.isNotEmpty) ||
            (childItem.texts != null && childItem.texts!.any((text) => text.isNotEmpty)) ||
            // Keep items that might have children even if text is empty
            (childItem.items != null && childItem.items!.isNotEmpty)
        )
        .toList() ?? [];

    return childItems.map((childItem) {
      return TreeItemContextProvider(
        level: (_context?.level ?? 0) + 1,
        isCheckMode: _context?.isCheckMode ?? false,
        parentTree: _context!.parentTree,
        parentTreeValue: _context!.parentTreeValue,
        treeImpl: _context?.treeImpl, // Pass tree implementation to children
        child: TreeItemSwt(
          value: childItem,
          key: ValueKey('tree_child_item_${childItem.id}'),
        ),
      );
    }).toList();
  }

  Widget _buildTextContent(List<String>? texts, String text, Color textColor) {
    // Get column information from parent tree context
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];

    // If no columns defined, show single text (backward compatibility)
    if (columns.isEmpty) {
      final displayText = text.isNotEmpty ? text : (texts?.isNotEmpty == true ? texts![0] : '');

      // Apply custom styling for single column
      final cellTextColor = _getCellTextColor(0, textColor);
      final cellBackgroundColor = _getCellBackgroundColor(0);

      return Container(
        padding: const EdgeInsets.symmetric(vertical: 4.0),
        decoration: cellBackgroundColor != null
            ? BoxDecoration(color: cellBackgroundColor)
            : null,
        child: Text(
          displayText,
          style: TextStyle(
            color: cellTextColor,
            fontSize: 14,
            fontWeight: _getCellFontWeight(0),
          ),
          overflow: TextOverflow.ellipsis,
        ),
      );
    }

    // Multi-column layout: align text with column widths
    return Row(
      children: columns.asMap().entries.map<Widget>((entry) {
        final int columnIndex = entry.key;
        final column = entry.value;
        final double columnWidth = (column.width ?? 100).toDouble();

        // Get text for this column
        String columnText = '';
        if (columnIndex == 0) {
          // First column: use primary text or first item in texts array
          columnText = text.isNotEmpty ? text : (texts?.isNotEmpty == true ? texts![0] : '');
        } else if (texts != null && columnIndex < texts.length) {
          // Other columns: use corresponding item from texts array
          columnText = texts[columnIndex];
        }

        // Apply custom styling for this cell
        final cellTextColor = _getCellTextColor(columnIndex, textColor);
        final cellBackgroundColor = _getCellBackgroundColor(columnIndex);

        return SizedBox(
          width: columnWidth,
          child: Container(
            padding: const EdgeInsets.symmetric(vertical: 4.0, horizontal: 4.0),
            decoration: cellBackgroundColor != null
                ? BoxDecoration(color: cellBackgroundColor)
                : null,
            child: Text(
              columnText,
              style: TextStyle(
                color: cellTextColor,
                fontSize: 14,
                fontWeight: _getCellFontWeight(columnIndex),
              ),
              textAlign: _getTextAlignForColumn(column.alignment),
              overflow: TextOverflow.ellipsis,
            ),
          ),
        );
      }).toList(),
    );
  }

  TextAlign _getTextAlignForColumn(int? alignment) {
    if (alignment == null) return TextAlign.left;

    switch (alignment) {
      case SWT.CENTER:
        return TextAlign.center;
      case SWT.RIGHT:
        return TextAlign.right;
      default:
        return TextAlign.left;
    }
  }

  // Custom styling helper methods
  // Note: Advanced styling properties (cellForeground, cellBackground, cellFont)
  // are defined in Java VTreeItem but not yet exposed in Flutter TreeItemValue.
  // This implementation provides the foundation for when those properties are added.

  Color _getCellTextColor(int columnIndex, Color defaultColor) {
    // TODO: When cellForeground property is added to TreeItemValue, implement:
    // final cellForegroundColors = state.cellForeground;
    // if (cellForegroundColors != null && columnIndex < cellForegroundColors.length) {
    //   return _convertSwtColorToFlutter(cellForegroundColors[columnIndex]);
    // }

    // TODO: When foreground property is added to TreeItemValue, implement:
    // final generalForeground = state.foreground;
    // if (generalForeground != null) {
    //   return _convertSwtColorToFlutter(generalForeground);
    // }

    // For now, use default color
    return defaultColor;
  }

  Color? _getCellBackgroundColor(int columnIndex) {
    // TODO: When cellBackground property is added to TreeItemValue, implement:
    // final cellBackgroundColors = state.cellBackground;
    // if (cellBackgroundColors != null && columnIndex < cellBackgroundColors.length) {
    //   return _convertSwtColorToFlutter(cellBackgroundColors[columnIndex]);
    // }

    // TODO: When background property is added to TreeItemValue, implement:
    // final generalBackground = state.background;
    // if (generalBackground != null) {
    //   return _convertSwtColorToFlutter(generalBackground);
    // }

    // For now, no custom background color
    return null;
  }

  FontWeight _getCellFontWeight(int columnIndex) {
    // TODO: When cellFont property is added to TreeItemValue, implement:
    // final cellFonts = state.cellFont;
    // if (cellFonts != null && columnIndex < cellFonts.length) {
    //   return _convertSwtFontToWeight(cellFonts[columnIndex]);
    // }

    // TODO: When font property is added to TreeItemValue, implement:
    // final generalFont = state.font;
    // if (generalFont != null) {
    //   return _convertSwtFontToWeight(generalFont);
    // }

    // For now, use default font weight
    return FontWeight.normal;
  }

  // Helper methods for future SWT property conversion
  Color _convertSwtColorToFlutter(dynamic swtColor) {
    // This will convert from SWT's Color object to Flutter Color
    if (swtColor is Map) {
      final red = swtColor['red'] ?? 0;
      final green = swtColor['green'] ?? 0;
      final blue = swtColor['blue'] ?? 0;
      return Color.fromARGB(255, red, green, blue);
    }
    return useDarkTheme ? Colors.white : Colors.black87;
  }

  FontWeight _convertSwtFontToWeight(dynamic swtFont) {
    // This will extract style information from SWT Font
    if (swtFont is Map) {
      final style = swtFont['style'] ?? 0;
      // SWT.BOLD = 1 << 0
      if ((style & 1) != 0) {
        return FontWeight.bold;
      }
    }
    return FontWeight.normal;
  }
}
