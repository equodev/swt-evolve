import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/font.dart';
import '../gen/event.dart';
import '../gen/list.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';
import '../styles.dart';
import 'color_utils.dart';
import 'utils/font_utils.dart';

class ListImpl<T extends ListSwt, V extends VList>
    extends ScrollableImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    state.selection ??= <int>[];
    state.items ??= <String>[];

    return _StyledList(
      items: state.items!,
      selection: _convertIndicesToItems(state.items!, state.selection!),
      enabled: state.enabled ?? true,
      isMultiSelect: state.style.has(SWT.MULTI),
      width: state.bounds?.width.toDouble(),
      height: state.bounds?.height.toDouble(),
      vFont: state.font,
      textColor: state.foreground,
      onSelectionChanged: (selectedItems) {
        setState(() {
          state.selection = _convertItemsToIndices(state.items!, selectedItems);
        });
        var e = VEvent()..segments = state.selection;
        widget.sendSelectionSelection(state, e);
      },
      onItemDoubleClick: (item) {
        var e = VEvent()..segments = state.selection;
        widget.sendSelectionDefaultSelection(state, e);
      },
      onMouseEnter: () => widget.sendMouseTrackMouseEnter(state, null),
      onMouseExit: () => widget.sendMouseTrackMouseExit(state, null),
      onFocusIn: () => widget.sendFocusFocusIn(state, null),
      onFocusOut: () => widget.sendFocusFocusOut(state, null),
    );
  }

  List<String> _convertIndicesToItems(List<String> items, List<int> indices) {
    return indices
        .where((index) => index >= 0 && index < items.length)
        .map((index) => items[index])
        .toList();
  }

  List<int> _convertItemsToIndices(
      List<String> items, List<String> selectedItems) {
    return selectedItems
        .map((item) => items.indexOf(item))
        .where((index) => index != -1)
        .toList();
  }
}

class _StyledList extends StatefulWidget {
  final List<String> items;
  final List<String> selection;
  final bool enabled;
  final bool isMultiSelect;
  final Function(List<String>) onSelectionChanged;
  final Function(String) onItemDoubleClick;
  final VoidCallback onMouseEnter;
  final VoidCallback onMouseExit;
  final VoidCallback onFocusIn;
  final VoidCallback onFocusOut;
  final double? width;
  final double? height;
  final VFont? vFont;
  final VColor? textColor;

  const _StyledList({
    Key? key,
    required this.items,
    required this.selection,
    required this.enabled,
    required this.isMultiSelect,
    required this.onSelectionChanged,
    required this.onItemDoubleClick,
    required this.onMouseEnter,
    required this.onMouseExit,
    required this.onFocusIn,
    required this.onFocusOut,
    this.width,
    this.height,
    this.vFont,
    this.textColor,
  }) : super(key: key);

  @override
  _StyledListState createState() => _StyledListState();
}

class _StyledListState extends State<_StyledList> {
  @override
  Widget build(BuildContext context) {
    final containerBgColor = getBackground();
    final borderColor = getBorderColor();

    return MouseRegion(
      onEnter: (_) => widget.onMouseEnter(),
      onExit: (_) => widget.onMouseExit(),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            widget.onFocusIn();
          } else {
            widget.onFocusOut();
          }
        },
        child: SizedBox(
          width: widget.width,
          height: widget.height,
          child: Container(
            decoration: BoxDecoration(
              color: containerBgColor,
              border: Border.all(
                color: borderColor,
                width: 1,
              ),
            ),
            child: ListView.builder(
              itemCount: widget.items.length,
              itemExtent: 24,
              itemBuilder: (context, index) {
                final item = widget.items[index];
                final isSelected = widget.selection.contains(item);
                return _ListItem(
                  item: item,
                  isSelected: isSelected,
                  enabled: widget.enabled,
                  vFont: widget.vFont,
                  textColor: widget.textColor,
                  onTap: () => _handleItemTap(item),
                  onDoubleTap: () => widget.onItemDoubleClick(item),
                );
              },
            ),
          ),
        ),
      ),
    );
  }

  void _handleItemTap(String item) {
    if (!widget.enabled) return;

    List<String> newSelection = List.from(widget.selection);
    if (widget.isMultiSelect) {
      if (newSelection.contains(item)) {
        newSelection.remove(item);
      } else {
        newSelection.add(item);
      }
    } else {
      newSelection.clear();
      newSelection.add(item);
    }
    widget.onSelectionChanged(newSelection);
  }
}

class _ListItem extends StatelessWidget {
  final String item;
  final bool isSelected;
  final bool enabled;
  final VFont? vFont;
  final VColor? textColor;
  final VoidCallback onTap;
  final VoidCallback onDoubleTap;

  const _ListItem({
    Key? key,
    required this.item,
    required this.isSelected,
    required this.enabled,
    this.vFont,
    this.textColor,
    required this.onTap,
    required this.onDoubleTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final backgroundColor =
        isSelected ? getBackgroundSelected() : getBackground();

    // Get text color - if textColor (VColor) is provided, use it, otherwise use default
    final finalTextColor = colorFromVColor(textColor,
        defaultColor: (enabled ? getForeground() : getForegroundDisabled()));

    // Create TextStyle from VFont
    final textStyle = FontUtils.textStyleFromVFont(
      vFont,
      context,
      color: finalTextColor,
    );

    return GestureDetector(
      onTap: onTap,
      onDoubleTap: onDoubleTap,
      child: Container(
        height: 24,
        color: backgroundColor,
        padding: const EdgeInsets.symmetric(horizontal: 8),
        alignment: Alignment.centerLeft,
        child: Text(
          item,
          style: textStyle,
          overflow: TextOverflow.ellipsis,
        ),
      ),
    );
  }
}
