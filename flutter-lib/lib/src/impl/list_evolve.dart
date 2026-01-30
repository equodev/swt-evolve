import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/list.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/list_theme_extension.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';

class ListImpl<T extends ListSwt, V extends VList>
    extends ScrollableImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ListThemeExtension>()!;
    
    state.selection ??= <int>[];
    state.items ??= <String>[];

    final styleBits = StyleBits(state.style);
    final isMultiSelect = styleBits.has(SWT.MULTI);
    final isEnabled = state.enabled ?? false;
    
    final hasConstraints = hasBounds(state.bounds);
    final width = hasConstraints ? state.bounds!.width.toDouble() : null;
    final height = hasConstraints ? state.bounds!.height.toDouble() : null;

    return _StyledList(
      widgetTheme: widgetTheme,
      state: state,
      items: state.items!,
      selection: _convertIndicesToItems(state.items!, state.selection!),
      enabled: isEnabled,
      isMultiSelect: isMultiSelect,
      width: width,
      height: height,
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
  final ListThemeExtension widgetTheme;
  final VList state;
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

  const _StyledList({
    required this.widgetTheme,
    required this.state,
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
  });

  @override
  State<_StyledList> createState() => _StyledListState();
}

class _StyledListState extends State<_StyledList> {
  bool _isFocused = false;

  @override
  Widget build(BuildContext context) {
    final theme = widget.widgetTheme;
    final backgroundColor = widget.enabled
        ? theme.backgroundColor
        : theme.disabledBackgroundColor;
    
    final borderColor = _isFocused
        ? theme.focusedBorderColor
        : theme.borderColor;

    return MouseRegion(
      onEnter: (_) => widget.onMouseEnter(),
      onExit: (_) => widget.onMouseExit(),
      child: Focus(
        onFocusChange: (hasFocus) {
          setState(() => _isFocused = hasFocus);
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
              color: backgroundColor,
              border: Border.all(
                color: borderColor,
                width: theme.borderWidth,
              ),
              borderRadius: BorderRadius.circular(theme.borderRadius),
            ),
            child: widget.items.isEmpty
                ? const SizedBox.shrink()
                : ListView.builder(
                    itemCount: widget.items.length,
                    itemExtent: theme.itemHeight,
                    itemBuilder: (context, index) {
                      final item = widget.items[index];
                      final isSelected = widget.selection.contains(item);
                      return _ListItem(
                        widgetTheme: theme,
                        state: widget.state,
                        item: item,
                        isSelected: isSelected,
                        enabled: widget.enabled,
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

class _ListItem extends StatefulWidget {
  final ListThemeExtension widgetTheme;
  final VList state;
  final String item;
  final bool isSelected;
  final bool enabled;
  final VoidCallback onTap;
  final VoidCallback onDoubleTap;

  const _ListItem({
    required this.widgetTheme,
    required this.state,
    required this.item,
    required this.isSelected,
    required this.enabled,
    required this.onTap,
    required this.onDoubleTap,
  });

  @override
  State<_ListItem> createState() => _ListItemState();
}

class _ListItemState extends State<_ListItem> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final theme = widget.widgetTheme;
    
    final baseBackgroundColor = !widget.enabled
        ? theme.disabledBackgroundColor
        : theme.backgroundColor;
    
    final selectedOverlayColor = theme.selectedItemBackgroundColor;
    final hoverOverlayColor = theme.hoverItemBackgroundColor;
    
    final textColor = !widget.enabled
        ? theme.disabledTextColor
        : widget.isSelected
            ? theme.selectedItemTextColor
            : theme.textColor;

    final textStyle = getTextStyle(
      context: context,
      font: widget.state.font,
      textColor: textColor,
      baseTextStyle: theme.textStyle,
    );

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      child: GestureDetector(
        onTap: widget.onTap,
        onDoubleTap: widget.onDoubleTap,
        child: Stack(
          children: [
            Container(
              height: theme.itemHeight,
              color: baseBackgroundColor,
              padding: theme.itemPadding,
              alignment: Alignment.centerLeft,
              child: Text(
                widget.item,
                style: textStyle,
                overflow: TextOverflow.ellipsis,
              ),
            ),
            if (widget.isSelected && widget.enabled)
              AnimatedOpacity(
                duration: theme.animationDuration,
                curve: Curves.easeInOut,
                opacity: 0.3,
                child: Container(
                  height: theme.itemHeight,
                  color: selectedOverlayColor,
                ),
              ),
            if (!widget.isSelected && widget.enabled)
              AnimatedOpacity(
                duration: theme.animationDuration,
                curve: Curves.easeInOut,
                opacity: _isHovered ? 0.1 : 0.0,
                child: Container(
                  height: theme.itemHeight,
                  color: hoverOverlayColor,
                ),
              ),
          ],
        ),
      ),
    );
  }
}