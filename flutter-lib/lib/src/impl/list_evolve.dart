import 'dart:async';
import 'dart:math' show max, min;

import 'package:flutter/foundation.dart' show listEquals;
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
import 'utils/test_anchors.dart';
import 'menu_evolve.dart' show OwnPressListener;

class ListImpl<T extends ListSwt, V extends VList>
    extends ScrollableImpl<T, V> {
  final ScrollController _scrollController = ScrollController();
  final ScrollController _acrossScrollController = ScrollController();

  /// The height the items are laid out in.
  double? _itemExtent;

  (int, int, int, int?, int?, int?)? _reportedScroll;

  @override
  bool get keepsWheel => true;

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_reportScrollOffset);
    _acrossScrollController.addListener(_reportScrollOffset);
    if (state.topIndex != null) _scrollToTopIndexAfterLayout();
  }

  @override
  void dispose() {
    _scrollController.dispose();
    _acrossScrollController.dispose();
    super.dispose();
  }

  @override
  void setValue(V value) {
    super.setValue(value);
    final change = lastChange;
    // A hidden List drops its ListView, so the one built when it is shown again starts at item 0.
    if (change == null ||
        change.touches(WireKey.k_List_topIndex) ||
        (change.touches(WireKey.k_Control_visible) && !hidden)) {
      _scrollToTopIndexAfterLayout();
    }
  }

  /// Java keeps how far the items are scrolled: getTopIndex and API scrolling are computed from it.
  /// The event also carries the viewport ([VEvent.detail]) and rows' height ([VEvent.count]) and,
  /// with H_SCROLL, the sideways offset ([VEvent.x]), viewport ([VEvent.width]) and items' width
  /// ([VEvent.end]): Java's scroll bars take those ranges.
  void _reportScrollOffset() {
    final extent = _itemExtent;
    if (!_scrollController.hasClients || extent == null) return;
    final down = _scrollController.position;
    if (!down.hasContentDimensions || !down.hasViewportDimension) return;
    final across = _acrossScrollController.hasClients ? _acrossScrollController.position : null;
    final measured = across != null && across.hasContentDimensions && across.hasViewportDimension;
    final report = (
      down.pixels.round(),
      down.viewportDimension.round(),
      (down.maxScrollExtent + down.viewportDimension).round(),
      measured ? across.pixels.round() : null,
      measured ? across.viewportDimension.round() : null,
      measured ? (across.maxScrollExtent + across.viewportDimension).round() : null,
    );
    if (report == _reportedScroll) return;
    _reportedScroll = report;
    widget.sendEvent(state, WireChannel.scrollScroll, VEvent()
      ..y = report.$1
      ..height = extent.round()
      ..detail = report.$2
      ..count = report.$3
      ..x = report.$4
      ..width = report.$5
      ..end = report.$6);
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
    if (hidden) return const SizedBox.shrink();
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
    final measure = singleLineTextSizes(itemTextStyle);
    final across = styleBits.has(SWT.H_SCROLL)
        ? state.items!.fold(0.0, (double w, item) => max(w, measure(item).width)) +
            widgetTheme.itemPadding.horizontal
        : null;

    return SwtControlScope(swt: state.swt, child: tagSemantics(state, trackPointer(
        NotificationListener<ScrollMetricsNotification>(
            onNotification: _reportScrollMetrics, child: _StyledList(
      widgetTheme: widgetTheme,
      itemExtent: itemExtent,
      state: state,
      items: state.items!,
      selection: state.selection!,
      scrollController: _scrollController,
      acrossScrollController: _acrossScrollController,
      across: across,
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
    )))));
  }

  /// A new viewport or rows' size changes the bars' range without a scroll.
  bool _reportScrollMetrics(ScrollMetricsNotification notification) {
    _reportScrollOffset();
    return false;
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
  final ScrollController acrossScrollController;
  /// With H_SCROLL, the widest item and its padding: the items are at least this wide and scroll
  /// sideways in a narrower list. Null without H_SCROLL.
  final double? across;
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
    required this.acrossScrollController,
    required this.across,
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

  /// The item a primary press landed on while its button is held: a drag selects from it.
  int? _dragOrigin;


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
    final across = widget.across;
    final rows = ListView.builder(
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
          onRelease: () => _dragOrigin = null,
          onDragOver: () => _handleDragOver(index),
        );
      },
    );

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
              : across == null
                  ? rows
                  : _scrollableAcross(context, rows, across),
        ),
      ),
    );
  }

  /// The rows in a sideways viewport, at least [across] wide. The vertical bar stays on the list's
  /// right edge: it sits outside the horizontal viewport, so it listens one scrollable deep.
  Widget _scrollableAcross(BuildContext context, Widget rows, double across) => LayoutBuilder(
        builder: (context, constraints) => Scrollbar(
          controller: widget.scrollController,
          notificationPredicate: (notification) =>
              notification.metrics.axis == Axis.vertical && notification.depth == 1,
          child: Scrollbar(
            controller: widget.acrossScrollController,
            child: ScrollConfiguration(
              behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
              child: SingleChildScrollView(
                controller: widget.acrossScrollController,
                scrollDirection: Axis.horizontal,
                child: SizedBox(width: max(across, constraints.maxWidth), child: rows),
              ),
            ),
          ),
        ),
      );

  /// A double click is two presses on the same item; the control's click chain alone also joins
  /// presses on neighbouring items.
  void _handleItemPress(int pressedIndex) {
    final doubleClick = widget.pressIsDoubleClick() && _lastPressedIndex == pressedIndex;
    _lastPressedIndex = pressedIndex;
    _dragOrigin = pressedIndex;
    if (doubleClick) {
      widget.onItemDoubleClick(true);
    } else {
      _handleItemTap(pressedIndex);
    }
  }

  /// The pointer reached [index] with the press's button held: the selection follows it, as a
  /// Win32 list box and Cocoa's table view track it. A MULTI list selects the range from the pressed item.
  void _handleDragOver(int index) {
    final origin = _dragOrigin;
    if (origin == null || !widget.enabled) return;
    final next = widget.isMultiSelect ? _range(origin, index) : [index];
    if (listEquals(next, widget.selection)) return;
    widget.onSelectionChanged(next);
  }

  /// The indices from [a] to [b], both included, in ascending order.
  static List<int> _range(int a, int b) => [for (var i = min(a, b); i <= max(a, b); i++) i];

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
      newIndices = _range(_anchorIndex!, tappedIndex);
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
  /// The release or cancellation of a press on the item.
  final VoidCallback onRelease;
  /// The pointer came onto the item with the primary button held.
  final VoidCallback onDragOver;

  const _ListItem({
    required this.widgetTheme,
    required this.extent,
    required this.state,
    required this.item,
    required this.index,
    required this.isSelected,
    required this.enabled,
    required this.onPress,
    required this.onRelease,
    required this.onDragOver,
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
      onEnter: (e) {
        setState(() => _isHovered = true);
        if ((e.buttons & kPrimaryButton) != 0) widget.onDragOver();
      },
      onExit: (_) => setState(() => _isHovered = false),
      // Answered on the press, as natively, so the selection is in place for the MouseUp; on a
      // microtask, as the control's own MouseDown listener runs after this one.
      child: OwnPressListener(
        onPointerDown: (e) {
          if (e.buttons != kPrimaryButton) return;
          scheduleMicrotask(widget.onPress);
        },
        onPointerUp: (_) => widget.onRelease(),
        onPointerCancel: (_) => widget.onRelease(),
        child: Stack(
            children: [
              Container(
                height: widget.extent,
                color: baseBackgroundColor,
                padding: theme.itemPadding,
                alignment: Alignment.centerLeft,
                child: StyleBits(widget.state.style).has(SWT.H_SCROLL)
                    ? Text(widget.item, style: textStyle, softWrap: false)
                    : Text(widget.item, style: textStyle, overflow: TextOverflow.ellipsis),
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
    );
  }
}
