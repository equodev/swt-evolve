import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:swtflutter/src/impl/tree_evolve.dart';
import '../gen/treeitem.dart';
import '../gen/treecolumn.dart';
import '../gen/image.dart';
import '../impl/item_evolve.dart';
import '../impl/widget_config.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import 'color_utils.dart';
import 'icons_map.dart';
import 'utils/font_utils.dart';
import 'utils/image_utils.dart';
import '../theme/tree_theme_extension.dart';
import '../theme/tree_theme_settings.dart';
import '../gen/button.dart';

class TreeItemImpl<T extends TreeItemSwt, V extends VTreeItem>
    extends ItemImpl<T, V> {

  TreeItemContext? _context;
  
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    _context = TreeItemContext.of(context);

    if (_context == null) {
      return Text(state.text ?? "");
    }

    return buildTreeItemContent(context);
  }

  Widget buildTreeItemContent(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    final widgetTheme = Theme.of(context).extension<TreeThemeExtension>();
    
    final String text = state.text ?? "";
    final List<String>? texts = state.texts;
    final bool expanded = state.expanded ?? false;
    final bool hasChildren = state.items != null && state.items!.isNotEmpty;
    final bool isCheckMode = _context?.isCheckMode ?? false;
    final bool checked = state.checked ?? false;
    final bool grayed = state.grayed ?? false;
    final int level = _context?.level ?? 0;
    final VImage? image = state.image;

    final bool selected = _context?.treeImpl?.isItemSelected(state.id) ?? false;
    final bool enabled = _context?.parentTreeValue.enabled ?? true;
    final bool nextItemSelected = _context?.treeImpl?.isNextItemSelected(state.id) ?? false;

    final textColor = getTreeItemTextColor(state, widgetTheme!, colorScheme, selected, enabled);
    final bgColor = getTreeItemBackgroundColor(state, widgetTheme!, colorScheme, selected, _isHovered && !selected, enabled);

    return MouseRegion(
      cursor: SystemMouseCursors.click,
      onEnter: (_) {
        setState(() {
          _isHovered = true;
        });
        _context?.parentTree
            .sendMouseTrackMouseEnter(_context!.parentTreeValue, null);
      },
      onExit: (_) {
        setState(() {
          _isHovered = false;
        });
        _context?.parentTree
            .sendMouseTrackMouseExit(_context!.parentTreeValue, null);
      },
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Listener(
            onPointerDown: (PointerDownEvent event) {
              if (event.buttons != 1) return;
              
              final pressedKeys = HardwareKeyboard.instance.logicalKeysPressed;
              final bool isCtrlPressed = pressedKeys.contains(LogicalKeyboardKey.controlLeft) ||
                  pressedKeys.contains(LogicalKeyboardKey.controlRight);
              final bool isShiftPressed = pressedKeys.contains(LogicalKeyboardKey.shiftLeft) ||
                  pressedKeys.contains(LogicalKeyboardKey.shiftRight);

              print('TreeItem pointerDown - Item ID=${state.id}');
              print('  Buttons: ${event.buttons}');
              print('  Pressed keys: ${pressedKeys.map((k) => k.keyLabel).join(", ")}');
              print('  isCtrlPressed: $isCtrlPressed');
              print('  isShiftPressed: $isShiftPressed');

              _context?.treeImpl?.handleTreeItemSelection(
                state.id,
                isCtrlPressed: isCtrlPressed,
                isShiftPressed: isShiftPressed,
              );

              var e = VEvent();
              if (_context?.treeImpl != null) {
                e.index = _context!.treeImpl!.findItemIndex(state.id);
                e.detail = widgetTheme!.eventDefaultDetail;
                e.x = widgetTheme.eventDefaultX;
                e.y = widgetTheme.eventDefaultY;
                e.width = widgetTheme.eventDefaultWidth.round();
                e.height = widgetTheme.eventDefaultHeight.round();
                int stateMask = 0;
                if (isCtrlPressed) {
                  stateMask |= SWT.CTRL;
                }
                if (isShiftPressed) {
                  stateMask |= SWT.SHIFT;
                }
                e.stateMask = stateMask;
              }

              _context?.parentTree
                  .sendSelectionSelection(_context!.parentTreeValue, e);
            },
            child: GestureDetector(
              onDoubleTap: () {
              var e = VEvent();
              if (_context?.treeImpl != null) {
                e.index = _context!.treeImpl!.findItemIndex(state.id);
                e.detail = widgetTheme!.eventDefaultDetail;
                e.x = widgetTheme.eventDefaultX;
                e.y = widgetTheme.eventDefaultY;
                e.width = widgetTheme.eventDefaultWidth.round();
                e.height = widgetTheme.eventDefaultHeight.round();
              }

              _context?.parentTree
                  .sendSelectionDefaultSelection(_context!.parentTreeValue, e);
            },
            child: Container(
              constraints: BoxConstraints(
                minHeight: widgetTheme!.itemHeight,
              ),
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
              decoration: BoxDecoration(
                color: bgColor,
                border: selected
                    ? Border(
                        left: BorderSide(
                          color: widgetTheme.itemSelectedBorderColor,
                          width: widgetTheme.itemSelectedBorderWidth,
                        ),
                        right: BorderSide(
                          color: widgetTheme.itemSelectedBorderColor,
                          width: widgetTheme.itemSelectedBorderWidth,
                        ),
                        top: BorderSide(
                          color: widgetTheme.itemSelectedBorderColor,
                          width: widgetTheme.itemSelectedBorderWidth,
                        ),
                        bottom: nextItemSelected
                            ? BorderSide.none
                            : BorderSide(
                                color: widgetTheme.itemSelectedBorderColor,
                                width: widgetTheme.itemSelectedBorderWidth,
                              ),
                      )
                    : null,
              ),
              padding: widgetTheme.itemPadding,
              child: _buildRowWithColumns(context, texts, text, textColor, widgetTheme, colorScheme, textTheme, level, hasChildren, expanded, isCheckMode, checked, grayed, enabled, selected, image),
            ),
            ),
          ),

          if (expanded && hasChildren) ...buildChildItems(),
        ],
      ),
    );
  }

  List<Widget> buildChildItems() {
    final List<VTreeItem> childItems = state.items
            ?.whereType<VTreeItem>()
            .where((childItem) =>
                (childItem.text != null && childItem.text!.isNotEmpty) ||
                (childItem.texts != null &&
                    childItem.texts!.any((text) => text.isNotEmpty)) ||
                (childItem.items != null && childItem.items!.isNotEmpty))
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
        child: TreeItemSwt(
          value: childItem,
          key: ValueKey('tree_child_item_${childItem.id}_${childItem.checked}_${childItem.grayed}'),
        ),
      );
    }).toList();
  }

  Widget _buildRowWithColumns(BuildContext context, List<String>? texts, String text, Color textColor, 
      TreeThemeExtension? widgetTheme, ColorScheme colorScheme, TextTheme textTheme, int level, 
      bool hasChildren, bool expanded, bool isCheckMode, bool checked, bool grayed, bool enabled, 
      bool selected, VImage? image) {
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];
    final bool hasMultipleColumns = columns.isNotEmpty;
    
    if (!hasMultipleColumns) {
      return Row(
        children: [
          SizedBox(width: widgetTheme!.itemIndent * level),

          hasChildren
              ? MouseRegion(
                  cursor: SystemMouseCursors.click,
                  child: GestureDetector(
                    onTap: () {
                      if (_context == null) {
                        print('TreeItem expand/collapse: _context is null');
                        return;
                      }

                      if (_context!.treeImpl == null) {
                        print('TreeItem expand/collapse: treeImpl is null for item ${state.id}');
                        return;
                      }

                      if (expanded) {
                        var e = VEvent();
                        e.index = _context!.treeImpl!.findItemIndex(state.id);
                        e.detail = widgetTheme!.eventDefaultDetail;
                        e.x = widgetTheme.eventDefaultX;
                        e.y = widgetTheme.eventDefaultY;
                        e.width = widgetTheme.eventDefaultWidth.round();
                        e.height = widgetTheme.eventDefaultHeight.round();

                        print('TreeItem collapse: sending Tree/Collapse event for item ${state.id}, index=${e.index}');
                        _context!.parentTree.sendTreeCollapse(_context!.parentTreeValue, e);
                      } else {
                        var e = VEvent();
                        e.index = _context!.treeImpl!.findItemIndex(state.id);
                        e.detail = widgetTheme!.eventDefaultDetail;
                        e.x = widgetTheme.eventDefaultX;
                        e.y = widgetTheme.eventDefaultY;
                        e.width = widgetTheme.eventDefaultWidth.round();
                        e.height = widgetTheme.eventDefaultHeight.round();

                        print('TreeItem expand: sending Tree/Expand event for item ${state.id}, index=${e.index}');
                        print('TreeItem expand: current state.expanded=${state.expanded}, state.items=${state.items?.length ?? 0}');
                        _context!.parentTree.sendTreeExpand(_context!.parentTreeValue, e);
                      }
                    },
                    child: Icon(
                      expanded ? Icons.keyboard_arrow_down : Icons.keyboard_arrow_right,
                      size: widgetTheme!.expandIconSize,
                      color: enabled ? widgetTheme.expandIconColor : widgetTheme.expandIconDisabledColor,
                    ),
                  ),
                )
              : SizedBox(width: widgetTheme!.expandIconSize),
          
          if (hasChildren) SizedBox(width: widgetTheme.expandIconSpacing),

          if (isCheckMode)
            Container(
              margin: EdgeInsets.only(right: widgetTheme!.checkboxSpacing),
              child: _CheckboxButtonWrapper(
                key: ValueKey('checkbox_${state.id}_$checked'),
                checked: checked,
                grayed: grayed,
                enabled: enabled,
                onChanged: () {
                  final bool newCheckedState = grayed ? false : (checked ? false : true);
                  _context?.treeImpl?.handleCheckboxCascade(state.id, newCheckedState);
                  _context?.treeImpl?.handleTreeItemSelection(state.id);

                  var e = VEvent();
                  if (_context?.treeImpl != null) {
                    e.index = _context!.treeImpl!.findItemIndex(state.id);
                    e.detail = SWT.CHECK;
                    e.x = widgetTheme.eventDefaultX;
                    e.y = widgetTheme.eventDefaultY;
                    e.width = widgetTheme.eventDefaultWidth.round();
                    e.height = widgetTheme.eventDefaultHeight.round();
                  }

                  _context?.parentTree.sendSelectionSelection(_context!.parentTreeValue, e);
                },
              ),
            ),

          Container(
            margin: EdgeInsets.only(right: widgetTheme!.itemIconSpacing),
            child: _buildItemIcon(widgetTheme, enabled, selected, hasChildren, expanded, image),
          ),

          Expanded(
            child: _buildTextContent(context, texts, text, textColor, widgetTheme, colorScheme, textTheme),
          ),
        ],
      );
    }
    
    final firstColumn = columns[0];
    final double firstColumnWidth = (firstColumn.width ?? widgetTheme!.columnDefaultWidth.round()).toDouble();
    
    final String firstColumnText = text.isNotEmpty ? text : (texts?.isNotEmpty == true ? texts![0] : '');
    
    return Row(
      children: [
        SizedBox(
          width: firstColumnWidth,
          child: Row(
            children: [
              SizedBox(width: widgetTheme!.itemIndent * level),

              hasChildren
                  ? MouseRegion(
                      cursor: SystemMouseCursors.click,
                      child: GestureDetector(
                        onTap: () {
                          if (_context == null) {
                            print('TreeItem expand/collapse: _context is null');
                            return;
                          }

                          if (_context!.treeImpl == null) {
                            print('TreeItem expand/collapse: treeImpl is null for item ${state.id}');
                            return;
                          }

                          if (expanded) {
                            var e = VEvent();
                            e.index = _context!.treeImpl!.findItemIndex(state.id);
                            e.detail = widgetTheme!.eventDefaultDetail;
                            e.x = widgetTheme.eventDefaultX;
                            e.y = widgetTheme.eventDefaultY;
                            e.width = widgetTheme.eventDefaultWidth.round();
                            e.height = widgetTheme.eventDefaultHeight.round();

                            print('TreeItem collapse: sending Tree/Collapse event for item ${state.id}, index=${e.index}');
                            _context!.parentTree.sendTreeCollapse(_context!.parentTreeValue, e);
                          } else {
                            var e = VEvent();
                            e.index = _context!.treeImpl!.findItemIndex(state.id);
                            e.detail = widgetTheme!.eventDefaultDetail;
                            e.x = widgetTheme.eventDefaultX;
                            e.y = widgetTheme.eventDefaultY;
                            e.width = widgetTheme.eventDefaultWidth.round();
                            e.height = widgetTheme.eventDefaultHeight.round();

                            print('TreeItem expand: sending Tree/Expand event for item ${state.id}, index=${e.index}');
                            _context!.parentTree.sendTreeExpand(_context!.parentTreeValue, e);
                          }
                        },
                        child: Icon(
                          expanded ? Icons.keyboard_arrow_down : Icons.keyboard_arrow_right,
                          size: widgetTheme!.expandIconSize,
                          color: enabled ? widgetTheme.expandIconColor : widgetTheme.expandIconDisabledColor,
                        ),
                      ),
                    )
                  : SizedBox(width: widgetTheme!.expandIconSize),
              
              if (hasChildren) SizedBox(width: widgetTheme.expandIconSpacing),

              if (isCheckMode)
                Container(
                  margin: EdgeInsets.only(right: widgetTheme!.checkboxSpacing),
                  child: _CheckboxButtonWrapper(
                    key: ValueKey('checkbox_${state.id}_$checked'),
                    checked: checked,
                    grayed: grayed,
                    enabled: enabled,
                    onChanged: () {
                      final bool newCheckedState = grayed ? false : (checked ? false : true);
                      _context?.treeImpl?.handleCheckboxCascade(state.id, newCheckedState);
                      _context?.treeImpl?.handleTreeItemSelection(state.id);

                      var e = VEvent();
                      if (_context?.treeImpl != null) {
                        e.index = _context!.treeImpl!.findItemIndex(state.id);
                        e.detail = SWT.CHECK;
                        e.x = widgetTheme.eventDefaultX;
                        e.y = widgetTheme.eventDefaultY;
                        e.width = widgetTheme.eventDefaultWidth.round();
                        e.height = widgetTheme.eventDefaultHeight.round();
                      }

                      _context?.parentTree.sendSelectionSelection(_context!.parentTreeValue, e);
                    },
                  ),
                ),

              Container(
                margin: EdgeInsets.only(right: widgetTheme!.itemIconSpacing),
                child: _buildItemIcon(widgetTheme, enabled, selected, hasChildren, expanded, image),
              ),

              Expanded(
                child: Container(
                  padding: _getCellPaddingForAlignment(firstColumn.alignment, widgetTheme!.cellMultiColumnPadding),
                  decoration: BoxDecoration(
                    color: _getCellBackgroundColor(0),
                  ),
                  child: Text(
                    firstColumnText,
                    style: getTreeItemTextStyle(
                      context,
                      state,
                      widgetTheme,
                      colorScheme,
                      textTheme,
                      _getCellTextColor(0, textColor),
                      _context?.treeFont,
                    ),
                    textAlign: _getTextAlignForColumn(firstColumn.alignment),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ),
            ],
          ),
        ),
        Expanded(
          child: _buildOtherColumns(context, texts, textColor, widgetTheme, colorScheme, textTheme, columns),
        ),
      ],
    );
  }

  Widget _buildItemIcon(TreeThemeExtension? widgetTheme, bool enabled, bool selected, 
      bool hasChildren, bool expanded, VImage? image) {
    return image != null
        ? ImageUtils.buildVImage(
            image,
            size: widgetTheme!.itemIconSize,
            width: widgetTheme.itemIconSize,
            height: widgetTheme.itemIconSize,
            color: enabled
                ? (selected ? widgetTheme.itemIconSelectedColor : widgetTheme.itemIconColor)
                : widgetTheme.itemIconDisabledColor,
            enabled: enabled,
            useBinaryImage: true,
            renderAsIcon: true,
          ) ?? Icon(
            hasChildren ? (expanded ? Icons.folder_open : Icons.folder) : Icons.cloud,
            size: widgetTheme.itemIconSize,
            color: enabled
                ? (selected ? widgetTheme.itemIconSelectedColor : widgetTheme.itemIconColor)
                : widgetTheme.itemIconDisabledColor,
          )
        : Icon(
            hasChildren ? (expanded ? Icons.folder_open : Icons.folder) : Icons.cloud,
            size: widgetTheme!.itemIconSize,
            color: enabled
                ? (selected ? widgetTheme.itemIconSelectedColor : widgetTheme.itemIconColor)
                : widgetTheme.itemIconDisabledColor,
          );
  }

  Widget _buildOtherColumns(BuildContext context, List<String>? texts, Color textColor, 
      TreeThemeExtension? widgetTheme, ColorScheme colorScheme, TextTheme textTheme, 
      List<VTreeColumn> columns) {
    return Row(
      children: columns.skip(1).toList().asMap().entries.map<Widget>((entry) {
        final int columnIndex = entry.key + 1;
        final column = entry.value;
        final double columnWidth = (column.width ?? widgetTheme!.columnDefaultWidth.round()).toDouble();

        String columnText = '';
        if (texts != null && columnIndex < texts.length) {
          columnText = texts[columnIndex];
        }

        final cellTextColor = _getCellTextColor(columnIndex, textColor);
        final cellBackgroundColor = _getCellBackgroundColor(columnIndex);

        final cellTextStyle = getTreeItemTextStyle(
          context,
          state,
          widgetTheme!,
          colorScheme,
          textTheme,
          cellTextColor,
          _context?.treeFont,
        );

        return SizedBox(
          width: columnWidth,
          child: Container(
            padding: _getCellPaddingForAlignment(column.alignment, widgetTheme!.cellMultiColumnPadding),
            decoration: BoxDecoration(
              color: cellBackgroundColor,
            ),
            child: Text(
              columnText,
              style: cellTextStyle,
              textAlign: _getTextAlignForColumn(column.alignment),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildTextContent(BuildContext context, List<String>? texts, String text, Color textColor, 
      TreeThemeExtension? widgetTheme, ColorScheme colorScheme, TextTheme textTheme) {
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];

    if (columns.isEmpty) {
      final displayText =
          text.isNotEmpty ? text : (texts?.isNotEmpty == true ? texts![0] : '');

      final cellTextColor = _getCellTextColor(0, textColor);
      final cellBackgroundColor = _getCellBackgroundColor(0);

      final cellTextStyle = getTreeItemTextStyle(
        context,
        state,
        widgetTheme!,
        colorScheme,
        textTheme,
        cellTextColor,
        _context?.treeFont,
      );

      return Container(
        padding: widgetTheme!.cellPadding,
        decoration: cellBackgroundColor != null
            ? BoxDecoration(color: cellBackgroundColor)
            : null,
        child: Text(
          displayText,
          style: cellTextStyle,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
      );
    }

    return _buildTextContentWithLines(context, texts, text, textColor, widgetTheme, colorScheme, textTheme);
  }

  Widget _buildTextContentWithLines(BuildContext context, List<String>? texts, String text, Color textColor, 
      TreeThemeExtension? widgetTheme, ColorScheme colorScheme, TextTheme textTheme) {
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];
    
    return Row(
      children: columns.asMap().entries.map<Widget>((entry) {
        final int columnIndex = entry.key;
        final column = entry.value;
        final double columnWidth = (column.width ?? widgetTheme!.columnDefaultWidth.round()).toDouble();

        String columnText = '';
        if (columnIndex == 0) {
          columnText = text.isNotEmpty
              ? text
              : (texts?.isNotEmpty == true ? texts![0] : '');
        } else if (texts != null && columnIndex < texts.length) {
          columnText = texts[columnIndex];
        }

        final cellTextColor = _getCellTextColor(columnIndex, textColor);
        final cellBackgroundColor = _getCellBackgroundColor(columnIndex);

        final cellTextStyle = getTreeItemTextStyle(
          context,
          state,
          widgetTheme!,
          colorScheme,
          textTheme,
          cellTextColor,
          _context?.treeFont,
        );

        return SizedBox(
          width: columnWidth,
          child: Container(
            padding: _getCellPaddingForAlignment(column.alignment, widgetTheme!.cellMultiColumnPadding),
            decoration: BoxDecoration(
              color: cellBackgroundColor,
            ),
            child: Text(
              columnText,
              style: cellTextStyle,
              textAlign: _getTextAlignForColumn(column.alignment),
              maxLines: 1,
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

  EdgeInsets _getCellPaddingForAlignment(int? alignment, EdgeInsets basePadding) {
    if (alignment == null || alignment == SWT.LEFT || alignment == SWT.RIGHT) {
      final extraPadding = 4.0;
      if (alignment == SWT.LEFT) {
        return EdgeInsets.only(
          left: basePadding.left + extraPadding,
          right: basePadding.right,
          top: basePadding.top,
          bottom: basePadding.bottom,
        );
      } else if (alignment == SWT.RIGHT) {
        return EdgeInsets.only(
          left: basePadding.left,
          right: basePadding.right + extraPadding,
          top: basePadding.top,
          bottom: basePadding.bottom,
        );
      } else {
        return EdgeInsets.only(
          left: basePadding.left + extraPadding,
          right: basePadding.right,
          top: basePadding.top,
          bottom: basePadding.bottom,
        );
      }
    }
    return basePadding;
  }

  Color _getCellTextColor(int columnIndex, Color defaultColor) {
    return colorFromVColor(state.foreground, defaultColor: defaultColor);
  }

  Color? _getCellBackgroundColor(int columnIndex) {
    return null;
  }

  FontWeight _getCellFontWeight(int columnIndex) {
    return FontWeight.normal;
  }

  Color _convertSwtColorToFlutter(dynamic swtColor, Color defaultColor) {
    if (swtColor is Map) {
      final red = swtColor['red'] ?? 0;
      final green = swtColor['green'] ?? 0;
      final blue = swtColor['blue'] ?? 0;
      return Color.fromARGB(255, red, green, blue);
    }
    return defaultColor;
  }

  FontWeight _convertSwtFontToWeight(dynamic swtFont) {
    if (swtFont is Map) {
      final style = swtFont['style'] ?? 0;
      if ((style & 1) != 0) {
        return FontWeight.bold;
      }
    }
    return FontWeight.normal;
  }
}

class _CheckboxButtonWrapper extends StatefulWidget {
  final bool checked;
  final bool grayed;
  final bool enabled;
  final VoidCallback onChanged;

  const _CheckboxButtonWrapper({
    Key? key,
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
    buttonValue = VButton.empty()
      ..id = -1
      ..style = SWT.CHECK
      ..enabled = widget.enabled
      ..selection = widget.checked
      ..grayed = widget.grayed;
  }

  @override
  void didUpdateWidget(_CheckboxButtonWrapper oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.checked != widget.checked ||
        oldWidget.grayed != widget.grayed ||
        oldWidget.enabled != widget.enabled) {
      setState(() {
        buttonValue.selection = widget.checked;
        buttonValue.grayed = widget.grayed;
        buttonValue.enabled = widget.enabled;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return _TreeCheckboxButton(
      value: buttonValue,
      onChanged: widget.onChanged,
    );
  }
}

class _TreeCheckboxButton extends ButtonSwt<VButton> {
  final VoidCallback onChanged;

  const _TreeCheckboxButton({
    required super.value,
    required this.onChanged,
  });

  @override
  void sendSelectionSelection(VButton val, VEvent? payload) {
    onChanged();
  }
}
