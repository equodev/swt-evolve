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
import '../theme/theme_extensions/tree_theme_extension.dart';
import '../theme/theme_settings/tree_theme_settings.dart';
import '../gen/button.dart';
import 'utils/widget_utils.dart';

class TreeItemImpl<T extends TreeItemSwt, V extends VTreeItem>
    extends ItemImpl<T, V> {

  TreeItemContext? _context;
  
  bool _isHovered = false;

  VEvent _createEvent({int? detail, int? stateMask}) {
    final widgetTheme = Theme.of(context).extension<TreeThemeExtension>();
    var e = VEvent();
    if (_context?.treeImpl != null && widgetTheme != null) {
      e.index = _context!.treeImpl!.findItemIndex(state.id);
      e.detail = detail ?? widgetTheme.eventDefaultDetail;
      e.x = widgetTheme.eventDefaultX;
      e.y = widgetTheme.eventDefaultY;
      e.width = widgetTheme.eventDefaultWidth.round();
      e.height = widgetTheme.eventDefaultHeight.round();
      if (stateMask != null) {
        e.stateMask = stateMask;
      }
    }
    return e;
  }

  @override
  Widget build(BuildContext context) {
    _context = TreeItemContext.of(context);

    if (_context == null) {
      return Text(state.text ?? "");
    }

    return buildTreeItemContent(context);
  }

  Widget buildTreeItemContent(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TreeThemeExtension>();
    if (widgetTheme == null) {
      return Text(state.text ?? "");
    }
    
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

    final textColor = getTreeItemTextColor(state, widgetTheme, selected, enabled);
    final bgColor = getTreeItemBackgroundColor(state, widgetTheme, selected, _isHovered && !selected, enabled);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        MouseRegion(
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
          child: Listener(
            onPointerDown: (PointerDownEvent event) {
              if (event.buttons != 1) return;
              
              final pressedKeys = HardwareKeyboard.instance.logicalKeysPressed;
              final bool isCtrlPressed = pressedKeys.contains(LogicalKeyboardKey.controlLeft) ||
                  pressedKeys.contains(LogicalKeyboardKey.controlRight);
              final bool isShiftPressed = pressedKeys.contains(LogicalKeyboardKey.shiftLeft) ||
                  pressedKeys.contains(LogicalKeyboardKey.shiftRight);

              _context?.treeImpl?.handleTreeItemSelection(
                state.id,
                isCtrlPressed: isCtrlPressed,
                isShiftPressed: isShiftPressed,
              );

              int stateMask = 0;
              if (isCtrlPressed) {
                stateMask |= SWT.CTRL;
              }
              if (isShiftPressed) {
                stateMask |= SWT.SHIFT;
              }
              final e = _createEvent(stateMask: stateMask);

              _context?.parentTree
                  .sendSelectionSelection(_context!.parentTreeValue, e);
            },
            child: GestureDetector(
              onDoubleTap: () {
                final e = _createEvent();
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
                child: _buildRowWithColumns(context, texts, text, textColor, widgetTheme!, level, hasChildren, expanded, isCheckMode, checked, grayed, enabled, selected, image),
              ),
            ),
          ),
        ),

        if (expanded && hasChildren) ...buildChildItems(),
      ],
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
      TreeThemeExtension widgetTheme, int level, 
      bool hasChildren, bool expanded, bool isCheckMode, bool checked, bool grayed, bool enabled, 
      bool selected, VImage? image) {
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];
    final bool hasMultipleColumns = columns.isNotEmpty;
    
    if (!hasMultipleColumns) {
      return Row(
        children: [
          _buildItemPrefix(
            theme: widgetTheme,
            level: level,
            hasChildren: hasChildren,
            expanded: expanded,
            isCheckMode: isCheckMode,
            checked: checked,
            grayed: grayed,
            enabled: enabled,
            selected: selected,
            image: image,
          ),
          Expanded(
            child: _buildTextContent(context, texts, text, textColor, widgetTheme),
          ),
        ],
      );
    }
    
    final firstColumn = columns[0];
    final double firstColumnWidth = (firstColumn.width ?? widgetTheme.columnDefaultWidth.round()).toDouble();
    
    final String firstColumnText = text.isNotEmpty ? text : (texts?.isNotEmpty == true ? texts![0] : '');
    
    return Row(
      children: [
        SizedBox(
          width: firstColumnWidth,
          child: Row(
            children: [
              _buildItemPrefix(
                theme: widgetTheme,
                level: level,
                hasChildren: hasChildren,
                expanded: expanded,
                isCheckMode: isCheckMode,
                checked: checked,
                grayed: grayed,
                enabled: enabled,
                selected: selected,
                image: image,
              ),
              Expanded(
                child: _buildCellText(
                  context: context,
                  text: firstColumnText,
                  textColor: textColor,
                  theme: widgetTheme,
                  columnAlignment: firstColumn.alignment,
                  cellPadding: widgetTheme.cellMultiColumnPadding,
                ),
              ),
            ],
          ),
        ),
        Expanded(
          child: _buildOtherColumns(context, texts, textColor, widgetTheme, columns),
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
      TreeThemeExtension widgetTheme, 
      List<VTreeColumn> columns) {
    return Row(
      children: columns.skip(1).toList().asMap().entries.map<Widget>((entry) {
        final int columnIndex = entry.key + 1;
        final column = entry.value;
        final double columnWidth = (column.width ?? widgetTheme.columnDefaultWidth.round()).toDouble();

        String columnText = '';
        if (texts != null && columnIndex < texts.length) {
          columnText = texts[columnIndex];
        }

        return SizedBox(
          width: columnWidth,
          child: _buildCellText(
            context: context,
            text: columnText,
            textColor: textColor,
            theme: widgetTheme,
            columnAlignment: column.alignment,
            cellPadding: widgetTheme.cellMultiColumnPadding,
          ),
        );
      }).toList(),
    );
  }

  Widget _buildTextContent(BuildContext context, List<String>? texts, String text, Color textColor, 
      TreeThemeExtension widgetTheme) {
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];

    if (columns.isEmpty) {
      final displayText =
          text.isNotEmpty ? text : (texts?.isNotEmpty == true ? texts![0] : '');
      return _buildCellText(
        context: context,
        text: displayText,
        textColor: textColor,
        theme: widgetTheme,
        columnAlignment: null,
        cellPadding: widgetTheme.cellPadding,
      );
    }

    return _buildTextContentWithLines(context, texts, text, textColor, widgetTheme);
  }

  Widget _buildTextContentWithLines(BuildContext context, List<String>? texts, String text, Color textColor, 
      TreeThemeExtension widgetTheme) {
    final columns = _context?.treeImpl?.getTreeColumns() ?? [];
    
    return Row(
      children: columns.asMap().entries.map<Widget>((entry) {
        final int columnIndex = entry.key;
        final column = entry.value;
        final double columnWidth = (column.width ?? widgetTheme.columnDefaultWidth.round()).toDouble();

        String columnText = '';
        if (columnIndex == 0) {
          columnText = text.isNotEmpty
              ? text
              : (texts?.isNotEmpty == true ? texts![0] : '');
        } else if (texts != null && columnIndex < texts.length) {
          columnText = texts[columnIndex];
        }

        return SizedBox(
          width: columnWidth,
          child: _buildCellText(
            context: context,
            text: columnText,
            textColor: textColor,
            theme: widgetTheme,
            columnAlignment: column.alignment,
            cellPadding: widgetTheme.cellMultiColumnPadding,
          ),
        );
      }).toList(),
    );
  }


  Widget _buildExpander({
    required TreeThemeExtension theme,
    required bool hasChildren,
    required bool expanded,
    required bool enabled,
  }) {
    if (!hasChildren) {
      return SizedBox(width: theme.expandIconSize);
    }

    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () {
        if (_context?.treeImpl == null) return;
        final e = _createEvent();
        if (expanded) {
          _context!.parentTree.sendTreeCollapse(_context!.parentTreeValue, e);
        } else {
          _context!.parentTree.sendTreeExpand(_context!.parentTreeValue, e);
        }
      },
      child: MouseRegion(
        cursor: SystemMouseCursors.click,
        child: Icon(
          expanded ? Icons.keyboard_arrow_down : Icons.keyboard_arrow_right,
          size: theme.expandIconSize,
          color: enabled ? theme.expandIconColor : theme.expandIconDisabledColor,
        ),
      ),
    );
  }

  Widget? _buildCheckbox({
    required TreeThemeExtension theme,
    required bool isCheckMode,
    required bool checked,
    required bool grayed,
    required bool enabled,
  }) {
    if (!isCheckMode) return null;

    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () {
        // Este GestureDetector captura el tap y evita que se propague al padre
        // El checkbox manejará su propio evento a través de _CheckboxButtonWrapper
      },
      child: Container(
        margin: EdgeInsets.only(right: theme.checkboxSpacing),
        child: _CheckboxButtonWrapper(
          key: ValueKey('checkbox_${state.id}_$checked'),
          checked: checked,
          grayed: grayed,
          enabled: enabled,
          onChanged: () {
            final bool newCheckedState = grayed ? false : (checked ? false : true);
            _context?.treeImpl?.handleCheckboxCascade(state.id, newCheckedState);
            _context?.treeImpl?.handleTreeItemSelection(state.id);
            final e = _createEvent(detail: SWT.CHECK);
            _context?.parentTree.sendSelectionSelection(_context!.parentTreeValue, e);
          },
        ),
      ),
    );
  }

  Widget _buildItemPrefix({
    required TreeThemeExtension theme,
    required int level,
    required bool hasChildren,
    required bool expanded,
    required bool isCheckMode,
    required bool checked,
    required bool grayed,
    required bool enabled,
    required bool selected,
    required VImage? image,
  }) {
    final checkbox = _buildCheckbox(
      theme: theme,
      isCheckMode: isCheckMode,
      checked: checked,
      grayed: grayed,
      enabled: enabled,
    );

    return Row(
      children: [
        SizedBox(width: theme.itemIndent * level),
        _buildExpander(
          theme: theme,
          hasChildren: hasChildren,
          expanded: expanded,
          enabled: enabled,
        ),
        if (hasChildren) SizedBox(width: theme.expandIconSpacing),
        if (checkbox != null) checkbox,
        Container(
          margin: EdgeInsets.only(right: theme.itemIconSpacing),
          child: _buildItemIcon(theme, enabled, selected, hasChildren, expanded, image),
        ),
      ],
    );
  }

  Widget _buildCellText({
    required BuildContext context,
    required String text,
    required Color textColor,
    required TreeThemeExtension theme,
    required int? columnAlignment,
    required EdgeInsets cellPadding,
  }) {
    final cellTextColor = getForegroundColor(foreground: state.foreground, defaultColor: textColor);
    final cellTextStyle = getTextStyle(
      context: context,
      font: state.font ?? _context?.treeFont,
      textColor: cellTextColor,
      baseTextStyle: theme.itemTextStyle,
    );

    return Container(
      padding: adjustPaddingForAlignment(
        basePadding: cellPadding,
        alignment: columnAlignment,
        extraPadding: 4.0,
      ),
      child: Text(
        text,
        style: cellTextStyle,
        textAlign: getTextAlignFromStyle(columnAlignment ?? 0, TextAlign.left),
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
    );
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
