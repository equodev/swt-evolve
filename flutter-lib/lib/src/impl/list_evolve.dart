import 'dart:async';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../gen/event.dart';
import '../gen/list.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart' show WireChannel, WireKey;
import '../impl/scrollable_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/list_theme_extension.dart';
import '../theme/theme_settings/list_theme_settings.dart';
import 'utils/double_tap_detector.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';

class ListImpl<T extends ListSwt, V extends VList>
    extends ScrollableImpl<T, V> {
  final ScrollController _scrollController = ScrollController();

  /// The height the items are laid out in.
  double? _itemExtent;

  int? _reportedScrollOffset;

  @override
  bool get keepsWheel => true;

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_reportScrollOffset);
    if (state.topIndex != null) _scrollToTopIndexAfterLayout();
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  void setValue(V value) {
    super.setValue(value);
    if (lastChange?.touches(WireKey.k_List_topIndex) ?? true) _scrollToTopIndexAfterLayout();
  }

  /// Java keeps how far the items are scrolled: getTopIndex and API scrolling are computed from it.
  void _reportScrollOffset() {
    final extent = _itemExtent;
    if (!_scrollController.hasClients || extent == null) return;
    final offset = _scrollController.offset.round();
    if (offset == _reportedScrollOffset) return;
    _reportedScrollOffset = offset;
    widget.sendEvent(state, WireChannel.scrollScroll, VEvent()
      ..y = offset
      ..height = extent.round());
  }

  /// Brings the item Java asked for to the top, once the items are laid out. Flutter clamps the
  /// distance at the end of the items; a list already showing that item at the top does not move.
  void _scrollToTopIndexAfterLayout() {
    final target = state.topIndex;
    if (target == null) return;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final extent = _itemExtent;
      if (!mounted || !_scrollController.hasClients || extent == null || extent <= 0) return;
      final position = _scrollController.position;
      if ((position.pixels / extent).floor() == target) return;
      _scrollController.jumpTo(
        (target * extent).clamp(position.minScrollExtent, position.maxScrollExtent),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ListThemeExtension>()!;
    // The item color does not change the line height, so any color measures the extent.
    final itemTextStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: widgetTheme.textColor,
      baseTextStyle: widgetTheme.textStyle,
    );
    final itemExtent = listItemExtent(widgetTheme, itemTextStyle);
    _itemExtent = itemExtent;

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
      itemExtent: itemExtent,
      state: state,
      items: state.items!,
      selection: state.selection!,
      scrollController: _scrollController,
      enabled: isEnabled,
      isMultiSelect: isMultiSelect,
      width: width,
      height: height,
      onSelectionChanged: (selection) {
        setState(() {
          state.selection = selection;
        });
        var e = VEvent()..segments = state.selection;
        widget.sendSelectionSelection(state, e);
      },
      pressIsDoubleClick: () => swtClickCount == 2,
      onItemDoubleClick: (buttonHeld) {
        // Sent on the second press, which still holds its button, as Win32 and GTK report it.
        var e = VEvent()
          ..segments = state.selection
          ..stateMask = swtStateMask(released: buttonHeld);
        widget.sendSelectionDefaultSelection(state, e);
      },
      onFocusIn: () => widget.sendFocusFocusIn(state, null),
      onFocusOut: () => widget.sendFocusFocusOut(state, null),
    ))));
  }

}

class _StyledList extends StatefulWidget {
  final ListThemeExtension widgetTheme;
  final double itemExtent;
  final VList state;
  final List<String> items;
  /// The selected indices: items with the same text are told apart by where they are.
  final List<int> selection;
  final ScrollController scrollController;
  final bool enabled;
  final bool isMultiSelect;
  final Function(List<int>) onSelectionChanged;
  /// Whether the press in flight is the second of a double click.
  final bool Function() pressIsDoubleClick;
  /// A double click on the selected item; [buttonHeld] when it came from a press.
  final void Function(bool buttonHeld) onItemDoubleClick;
  final VoidCallback onFocusIn;
  final VoidCallback onFocusOut;
  final double? width;
  final double? height;

  const _StyledList({
    required this.widgetTheme,
    required this.itemExtent,
    required this.state,
    required this.items,
    required this.selection,
    required this.scrollController,
    required this.enabled,
    required this.isMultiSelect,
    required this.onSelectionChanged,
    required this.pressIsDoubleClick,
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

  int? _lastPressedIndex;

  final DoubleTapDetector _tapsWithoutPress = DoubleTapDetector();

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
                  controller: widget.scrollController,
                  // Win32's and GTK's list take no wheel without a vertical scroll bar; Java's
                  // top index still scrolls it.
                  physics: StyleBits(widget.state.style).has(SWT.V_SCROLL)
                      ? null
                      : const NeverScrollableScrollPhysics(),
                  itemCount: widget.items.length,
                  itemExtent: widget.itemExtent,
                  itemBuilder: (context, index) {
                    return _ListItem(
                      widgetTheme: theme,
                      extent: widget.itemExtent,
                      state: widget.state,
                      item: widget.items[index],
                      index: index,
                      isSelected: widget.selection.contains(index),
                      enabled: widget.enabled,
                      onPress: () => _handleItemPress(index),
                      onTap: () => _handleItemTapWithoutPress(index),
                    );
                  },
                ),
        ),
      ),
    );
  }

  /// A double click is two presses on the same item; the control's click chain alone also joins
  /// presses on neighbouring items.
  void _handleItemPress(int pressedIndex) {
    final doubleClick = widget.pressIsDoubleClick() && _lastPressedIndex == pressedIndex;
    _lastPressedIndex = pressedIndex;
    if (doubleClick) {
      widget.onItemDoubleClick(true);
    } else {
      _handleItemTap(pressedIndex);
    }
  }

  /// A tap through the semantics tree, which the web engine also makes of a pointer click on the
  /// item: a second one on the same item inside the double-click window is a double click.
  void _handleItemTapWithoutPress(int tappedIndex) {
    if (_tapsWithoutPress.registerTap(key: tappedIndex) == 2) {
      widget.onItemDoubleClick(false);
    } else {
      _handleItemTap(tappedIndex);
    }
  }

  void _handleItemTap(int tappedIndex) {
    if (!widget.enabled) return;

    final items = widget.items;
    if (tappedIndex < 0 || tappedIndex >= items.length) return;

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
          .where((i) => i >= 0 && i < items.length)
          .toList();
      if (!newIndices.remove(tappedIndex)) {
        newIndices.add(tappedIndex);
      }
      _anchorIndex = tappedIndex;
    } else {
      newIndices = [tappedIndex];
      _anchorIndex = tappedIndex;
    }

    widget.onSelectionChanged(newIndices);
  }
}

class _ListItem extends StatefulWidget {
  final ListThemeExtension widgetTheme;
  final double extent;
  final VList state;
  final String item;
  final int index;
  final bool isSelected;
  final bool enabled;
  /// A primary press on the item.
  final VoidCallback onPress;
  /// A tap with no press before it.
  final VoidCallback onTap;

  const _ListItem({
    required this.widgetTheme,
    required this.extent,
    required this.state,
    required this.item,
    required this.index,
    required this.isSelected,
    required this.enabled,
    required this.onPress,
    required this.onTap,
  });

  @override
  State<_ListItem> createState() => _ListItemState();
}

class _ListItemState extends State<_ListItem> {
  bool _isHovered = false;
  // Set while a pointer press is in flight: the press already answered the click, so the tap the
  // same click completes does not answer it a second time.
  bool _handledByPress = false;

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
      // Answered on the press, as natively, so the selection is in place for the MouseUp; on a
      // microtask, as the control's own MouseDown listener runs after this one.
      child: Listener(
        onPointerDown: (e) {
          if (e.buttons != kPrimaryButton) return;
          _handledByPress = true;
          scheduleMicrotask(widget.onPress);
        },
        // A press that never became a tap (dragged off the item, cancelled) reaches no tap
        // callback; once the arena has settled on the release, the press is over either way.
        onPointerUp: (_) => scheduleMicrotask(() => _handledByPress = false),
        onPointerCancel: (_) => scheduleMicrotask(() => _handledByPress = false),
        // A tap with no press before it -- an accessibility or test tap through the semantics
        // tree -- still selects.
        child: GestureDetector(
          onTap: () {
            if (_handledByPress) {
              _handledByPress = false;
            } else {
              widget.onTap();
            }
          },
          child: Stack(
            children: [
              Container(
                height: widget.extent,
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
                    height: widget.extent,
                    color: selectedOverlayColor,
                  ),
                ),
              if (!widget.isSelected && widget.enabled)
                AnimatedOpacity(
                  duration: theme.animationDuration,
                  curve: Curves.easeInOut,
                  opacity: _isHovered ? 0.1 : 0.0,
                  child: Container(
                    height: widget.extent,
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
