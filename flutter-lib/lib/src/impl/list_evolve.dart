import 'dart:async';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../gen/event.dart';
import '../gen/list.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/list_theme_extension.dart';
import '../theme/theme_settings/list_theme_settings.dart';
import 'utils/double_tap_detector.dart';
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

    return SwtControlScope(swt: state.swt, child: tagSemantics(trackPointer(_StyledList(
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
      onFocusIn: () => widget.sendFocusFocusIn(state, null),
      onFocusOut: () => widget.sendFocusFocusOut(state, null),
    ))));
  }

  List<String> _convertIndicesToItems(List<String> items, List<int> indices) {
    return indices
        .where((index) => index >= 0 && index < items.length)
        .map((index) => items[index])
        .toList();
  }

  List<int> _convertItemsToIndices(
    List<String> items,
    List<String> selectedItems,
  ) {
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

  /// Anchor for Shift+click range extension — the item a plain or Ctrl click last landed on.
  int? _anchorIndex;

  @override
  Widget build(BuildContext context) {
    final theme = widget.widgetTheme;
    final themeBackground = widget.enabled
        ? theme.backgroundColor
        : theme.disabledBackgroundColor;
    final backgroundColor = getBackgroundColor(
          background: widget.state.background,
          defaultColor: themeBackground,
          context: context,
        ) ??
        themeBackground;

    final borderColor = _isFocused
        ? theme.focusedBorderColor
        : theme.borderColor;

    return Focus(
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
            border: Border.all(color: borderColor, width: theme.borderWidth),
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
    );
  }

  void _handleItemTap(String item) {
    if (!widget.enabled) return;

    final items = widget.items;
    final tappedIndex = items.indexOf(item);
    if (tappedIndex < 0) return;

    // Native SWT semantics: a plain click selects only the clicked item (deselecting the
    // rest) even in a MULTI list; Ctrl/Cmd toggles it; Shift extends a range from the anchor.
    final keyboard = HardwareKeyboard.instance;
    final toggle = widget.isMultiSelect &&
        (keyboard.isControlPressed || keyboard.isMetaPressed);
    final extendRange = widget.isMultiSelect && keyboard.isShiftPressed;

    List<int> newIndices;
    if (extendRange && _anchorIndex != null) {
      final lo = _anchorIndex! < tappedIndex ? _anchorIndex! : tappedIndex;
      final hi = _anchorIndex! < tappedIndex ? tappedIndex : _anchorIndex!;
      newIndices = [for (var i = lo; i <= hi; i++) i];
      // Shift+click keeps the existing anchor so a further shift re-extends from it.
    } else if (toggle) {
      newIndices = widget.selection
          .map((s) => items.indexOf(s))
          .where((i) => i >= 0)
          .toList();
      if (!newIndices.remove(tappedIndex)) {
        newIndices.add(tappedIndex);
      }
      _anchorIndex = tappedIndex;
    } else {
      newIndices = [tappedIndex];
      _anchorIndex = tappedIndex;
    }

    final newSelection = newIndices.map((i) => items[i]).toList();
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

    final themeBase = !widget.enabled
        ? theme.disabledBackgroundColor
        : theme.backgroundColor;
    final baseBackgroundColor = getBackgroundColor(
          background: widget.state.background,
          defaultColor: themeBase,
          context: context,
        ) ??
        themeBase;

    final selectedOverlayColor = theme.selectedItemBackgroundColor;
    final hoverOverlayColor = theme.hoverItemBackgroundColor;

    final textColor = getListItemTextColor(context, theme, widget.isSelected, widget.enabled,
        foreground: widget.state.foreground);

    final textStyle = getTextStyle(
      context: context,
      font: widget.state.font,
      textColor: textColor,
      baseTextStyle: theme.textStyle,
    );

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      // Selects on the press, as a native list does, so the Selection lands between MouseDown and
      // MouseUp: a tap is only recognised once the release has been reported. On a microtask, as
      // the press reaches the control's own MouseDown listener after this one.
      child: Listener(
        onPointerDown: (e) {
          if (e.buttons == kPrimaryButton) scheduleMicrotask(widget.onTap);
        },
        child: InstantDoubleTapDetector(
          onDoubleTap: widget.onDoubleTap,
          identity: widget.item,
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
      ),
    );
  }
}
