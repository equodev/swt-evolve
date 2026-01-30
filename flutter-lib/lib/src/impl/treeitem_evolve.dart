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
  bool _hasCheckedForChildren = false;

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
    final bool enabled = _context?.parentTreeValue.enabled ?? false;
    final bool nextItemSelected = _context?.treeImpl?.isNextItemSelected(state.id) ?? false;

    if (expanded || hasChildren || _hasCheckedForChildren) {
      _hasCheckedForChildren = true;
    }
    
    final bool mightHaveChildren = !hasChildren && !expanded && !_hasCheckedForChildren;
    
    if (mightHaveChildren && _context?.treeImpl != null && _context?.parentTree != null) {
      _hasCheckedForChildren = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && _context?.treeImpl != null && _context?.parentTree != null) {
          final e = _createEvent();
          _context!.parentTree.sendTreeExpand(_context!.parentTreeValue, e);
        }
      });
    }

    final textColor = getTreeItemTextColor(state, widgetTheme, selected, enabled);
    final bgColor = getTreeItemBackgroundColor(state, widgetTheme, selected, _isHovered && !selected, enabled);

    double? totalTreeWidth = _context?.treeWidth;
    if (totalTreeWidth == null) {
      final columns = _context?.treeImpl?.getTreeColumns() ?? [];
      if (columns.isNotEmpty) {
        double calculatedWidth = 0.0;
        for (final column in columns) {
          calculatedWidth += (column.width ?? widgetTheme.columnDefaultWidth.round()).toDouble();
        }
        totalTreeWidth = calculatedWidth;
      }
    }

    return SizedBox(
      width: totalTreeWidth ?? double.infinity,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildItemRow(
            context: context,
            widgetTheme: widgetTheme!,
            texts: texts,
            text: text,
            textColor: textColor,
            level: level,
            hasChildren: hasChildren,
            expanded: expanded,
            isCheckMode: isCheckMode,
            checked: checked,
            grayed: grayed,
            enabled: enabled,
            selected: selected,
            image: image,
            bgColor: bgColor,
            nextItemSelected: nextItemSelected,
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
        treeWidth: _context?.treeWidth,
        editingItemId: _context?.editingItemId,
        editingController: _context?.editingController,
        editingFocusNode: _context?.editingFocusNode,
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
                  columnIndex: 0,
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

  Widget? _buildItemIcon(TreeThemeExtension? widgetTheme, bool enabled, bool selected,
      bool hasChildren, bool expanded, VImage? image) {
    // Only show an icon if explicitly provided
    if (image == null) {
      return null;
    }

    final Color iconColor = _getItemIconColor(widgetTheme!, enabled, selected);

    return FutureBuilder<Widget?>(
      future: ImageUtils.buildVImageAsync(
        image,
        enabled: enabled,
        constraints: BoxConstraints(
          minWidth: widgetTheme.itemIconSize,
          minHeight: widgetTheme.itemIconSize,
          maxWidth: widgetTheme.itemIconSize,
          maxHeight: widgetTheme.itemIconSize,
        ),
        useBinaryImage: false,
        renderAsIcon: true,
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done && snapshot.data != null) {
          return snapshot.data!;
        }
        // Return null if image fails to load
        return SizedBox(
          width: widgetTheme.itemIconSize,
          height: widgetTheme.itemIconSize,
        );
      },
    );
  }


  Color _getItemIconColor(TreeThemeExtension theme, bool enabled, bool selected) {
    if (!enabled) return theme.itemIconDisabledColor;
    return selected ? theme.itemIconSelectedColor : theme.itemIconColor;
  }

  Widget _buildOtherColumns(BuildContext context, List<String>? texts, Color textColor, 
      TreeThemeExtension widgetTheme, 
      List<VTreeColumn> columns) {
    return Row(
      children: columns.skip(1).toList().asMap().entries.map<Widget>((entry) {
        final int columnIndex = entry.key + 1;
        final column = entry.value;
        final String columnText = _getColumnText(texts, columnIndex);
        
        return _buildColumnCell(
          context: context,
          text: columnText,
          textColor: textColor,
          theme: widgetTheme,
          column: column,
          columnIndex: columnIndex,
        );
      }).toList(),
    );
  }

  String _getColumnText(List<String>? texts, int columnIndex) {
    if (texts != null && columnIndex < texts.length) {
      return texts[columnIndex];
    }
    return '';
  }

  Widget _buildColumnCell({
    required BuildContext context,
    required String text,
    required Color textColor,
    required TreeThemeExtension theme,
    required VTreeColumn column,
    required int columnIndex,
  }) {
    final double columnWidth = (column.width ?? theme.columnDefaultWidth.round()).toDouble();
    return SizedBox(
      width: columnWidth,
      child: _buildCellText(
        context: context,
        text: text,
        textColor: textColor,
        theme: theme,
        columnAlignment: column.alignment,
        cellPadding: theme.cellMultiColumnPadding,
        columnIndex: columnIndex,
      ),
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
        columnIndex: 0,
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
        final String columnText = columnIndex == 0 
            ? (text.isNotEmpty ? text : (texts?.isNotEmpty == true ? texts![0] : ''))
            : _getColumnText(texts, columnIndex);
        
        return _buildColumnCell(
          context: context,
          text: columnText,
          textColor: textColor,
          theme: widgetTheme,
          column: column,
          columnIndex: columnIndex,
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
      return SizedBox(
        width: theme.expandIconSize,
        height: theme.expandIconSize,
      );
    }
    
    final Color arrowColor = enabled ? theme.expandIconColor : theme.expandIconDisabledColor;
    
    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () {
        if (!enabled) return;
        if (_context?.treeImpl == null) return;
        final e = _createEvent();
        if (expanded) {
          _context!.parentTree.sendTreeCollapse(_context!.parentTreeValue, e);
        } else {
          _context!.parentTree.sendTreeExpand(_context!.parentTreeValue, e);
        }
      },
      child: MouseRegion(
        cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
        child: Icon(
          expanded ? Icons.keyboard_arrow_down : Icons.keyboard_arrow_right,
          size: theme.expandIconSize,
          color: arrowColor,
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
      },
      child: Container(
        margin: EdgeInsets.only(right: theme.checkboxSpacing),
        child: _CheckboxButtonWrapper(
          key: ValueKey('checkbox_${state.id}_$checked'),
          checked: checked,
          grayed: grayed,
          enabled: enabled,
          onChanged: () {
            if (!enabled) return;
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

    final icon = _buildItemIcon(theme, enabled, selected, hasChildren, expanded, image);

    return Row(
      children: [
        SizedBox(width: theme.itemIndent * level),
        _buildExpander(
          theme: theme,
          hasChildren: hasChildren,
          expanded: expanded,
          enabled: enabled,
        ),
        SizedBox(width: theme.expandIconSpacing),
        if (checkbox != null) checkbox,
         if (icon != null)
          Container(
            margin: EdgeInsets.only(right: theme.itemIconSpacing),
            child: icon,
          ),
      ],
    );
  }

  Widget _buildItemRow({
    required BuildContext context,
    required TreeThemeExtension widgetTheme,
    required List<String>? texts,
    required String text,
    required Color textColor,
    required int level,
    required bool hasChildren,
    required bool expanded,
    required bool isCheckMode,
    required bool checked,
    required bool grayed,
    required bool enabled,
    required bool selected,
    required VImage? image,
    required Color bgColor,
    required bool nextItemSelected,
  }) {
    final double expanderAreaWidth = widgetTheme.itemIndent * level + 
        widgetTheme.expandIconSize + 
        widgetTheme.expandIconSpacing;
    
    double? totalTreeWidth = _context?.treeWidth;
    if (totalTreeWidth == null) {
      final columns = _context?.treeImpl?.getTreeColumns() ?? [];
      if (columns.isNotEmpty) {
        double calculatedWidth = 0.0;
        for (final column in columns) {
          calculatedWidth += (column.width ?? widgetTheme.columnDefaultWidth.round()).toDouble();
        }
        totalTreeWidth = calculatedWidth;
      }
    }

    return SizedBox(
      width: totalTreeWidth ?? double.infinity,
      child: MouseRegion(
        cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
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
          behavior: HitTestBehavior.opaque,
          onPointerDown: (PointerDownEvent event) {
            if (!enabled) return;
            if (event.buttons != 1) return;

            final localPosition = event.localPosition;
            if (localPosition.dx < expanderAreaWidth && hasChildren) {
              return;
            }

            final pressedKeys = RawKeyboard.instance.keysPressed;
            final isCtrlPressed = pressedKeys.contains(LogicalKeyboardKey.controlLeft) ||
                pressedKeys.contains(LogicalKeyboardKey.controlRight) ||
                pressedKeys.contains(LogicalKeyboardKey.metaLeft) ||
                pressedKeys.contains(LogicalKeyboardKey.metaRight);
            final isShiftPressed = pressedKeys.contains(LogicalKeyboardKey.shiftLeft) ||
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
            behavior: HitTestBehavior.opaque,
            onDoubleTap: () {
              if (!enabled) return;
              // Send default selection event for double click on non-text areas
              final e = _createEvent();
              _context?.parentTree
                  .sendSelectionDefaultSelection(_context!.parentTreeValue, e);
            },
            child: Container(
              width: double.infinity,
              constraints: BoxConstraints(
                minHeight: widgetTheme.itemHeight,
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
              child: _buildRowWithColumns(
                context,
                texts,
                text,
                textColor,
                widgetTheme,
                level,
                hasChildren,
                expanded,
                isCheckMode,
                checked,
                grayed,
                enabled,
                selected,
                image,
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildCellText({
    required BuildContext context,
    required String text,
    required Color textColor,
    required TreeThemeExtension theme,
    required int? columnAlignment,
    required EdgeInsets cellPadding,
    required int columnIndex,
  }) {
    final cellTextColor = getForegroundColor(foreground: state.foreground, defaultColor: textColor);
    final cellTextStyle = getTextStyle(
      context: context,
      font: state.font ?? _context?.treeFont,
      textColor: cellTextColor,
      baseTextStyle: theme.itemTextStyle,
    );

    // Only allow editing on the first column (columnIndex 0)
    final isEditing = columnIndex == 0 &&
                     _context?.editingItemId == state.id &&
                     _context?.editingController != null &&
                     _context?.editingFocusNode != null;

    Widget textWidget = isEditing
        ? TextField(
              controller: _context!.editingController!,
              focusNode: _context!.editingFocusNode!,
              style: cellTextStyle,
              textAlign: getTextAlignFromStyle(columnAlignment ?? 0, TextAlign.left),
              decoration: InputDecoration(
                isDense: true,
                contentPadding: EdgeInsets.zero,
                border: InputBorder.none,
                focusedBorder: InputBorder.none,
                enabledBorder: InputBorder.none,
              ),
              onSubmitted: (value) {
                if (_context?.treeImpl != null) {
                  _context!.treeImpl!.finishEditing();
                }
              },
              onEditingComplete: () {
                if (_context?.treeImpl != null) {
                  _context!.treeImpl!.finishEditing();
                }
              },
            )
          : Text(
              text,
              style: cellTextStyle,
              textAlign: getTextAlignFromStyle(columnAlignment ?? 0, TextAlign.left),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            );

    // Wrap text in GestureDetector for double-click editing (only first column)
    // Use deferToChild so only the text area triggers editing, not the padding
    final bool parentEnabled = _context?.parentTreeValue.enabled ?? false;
    if (columnIndex == 0 &&
        parentEnabled &&
        _context?.parentTreeValue.editable == true &&
        !isEditing &&
        _context?.treeImpl != null) {
      textWidget = GestureDetector(
        behavior: HitTestBehavior.deferToChild,
        onDoubleTap: () {
          // Start editing on double tap over the text
          if (_context?.editingItemId != state.id && _context?.treeImpl != null) {
            _context!.treeImpl!.startEditing(state.id);
          }
        },
        child: textWidget,
      );
    }

    final adjustedPadding = adjustPaddingForAlignment(
      basePadding: cellPadding,
      alignment: columnAlignment,
      extraPadding: 4.0,
    );

    // Wrap textWidget to prevent it from expanding beyond its content
    // This ensures the GestureDetector only captures clicks on the actual text
    Widget contentWidget = textWidget;
    if (columnIndex == 0 &&
        _context?.parentTreeValue.editable == true &&
        !isEditing) {
      // Use IntrinsicWidth to limit the clickable area to the actual text width
      // Then align it according to the column alignment
      final alignment = _getAlignmentFromStyle(columnAlignment ?? 0);
      contentWidget = Align(
        alignment: alignment,
        child: IntrinsicWidth(
          child: textWidget,
        ),
      );
    }

    return Container(
      padding: adjustedPadding,
      child: contentWidget,
    );
  }

  Alignment _getAlignmentFromStyle(int alignment) {
    if (alignment == SWT.CENTER) return Alignment.center;
    if (alignment == SWT.RIGHT || alignment == SWT.TRAIL) {
      return Alignment.centerRight;
    }
    return Alignment.centerLeft;
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
