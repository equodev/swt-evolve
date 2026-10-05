import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter/material.dart' show Icons, Icon, Divider, Theme;

import '../gen/combo.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/combo_theme_extension.dart';
import '../theme/theme_settings/combo_theme_settings.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'utils/pending_text_echoes.dart';
import 'key_forwarding.dart';
import 'utils/pointer.dart';

class ComboImpl<T extends ComboSwt, V extends VCombo>
    extends CompositeImpl<T, V> with PendingTextEchoes {
  late TextEditingController _controller;
  final FocusNode _focusNode = FocusNode();
  final OverlayPortalController _overlayController = OverlayPortalController();
  final LayerLink _layerLink = LayerLink();
  bool _isFocused = false;
  final bool _isHovered = false;
  /// The last `listVisible` Java sent. The dropdown is otherwise local UI state Java only drives
  /// through `Combo.setListVisible`, so only a change between two pushes is a command.
  bool? _lastJavaListVisible;
  /// The item the keyboard points at while the list is open; Enter commits it.
  int _highlighted = -1;
  final GlobalKey _highlightedKey = GlobalKey();

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _focusNode.addListener(_handleFocusChange);
    _focusNode.onKeyEvent = _handleKey;
    addOpenListKeyClaim(_listClaimsKey);
    _lastJavaListVisible = state.listVisible;
  }

  Size _maxTextSize(TextStyle style) {
    final List<String> allStrings = [state.text ?? "", ...(state.items ?? [])];

    double maxTextWidth = 0;
    double maxTextHeight = 0;

    for (var s in allStrings) {
      final painter = TextPainter(
        text: TextSpan(text: s, style: style),
        textDirection: TextDirection.ltr,
        maxLines: 1,
      )..layout();

      if (painter.width > maxTextWidth) maxTextWidth = painter.width + 2;
      if (painter.height > maxTextHeight) maxTextHeight = painter.height + 2;
    }

    return Size(maxTextWidth, maxTextHeight);
  }

  /// The arrow's own cell in the Row: the icon plus the gap that separates it
  /// from the text.
  double _arrowCell(ComboThemeExtension theme, bool isSimple) =>
      isSimple ? 0 : theme.iconSpacing + theme.iconSize;

  /// The arrow glyph alone -- the one inset, besides the border, that a pinned
  /// width cannot take back.
  double _arrowGlyph(ComboThemeExtension theme, bool isSimple) =>
      isSimple ? 0 : theme.iconSize;

  /// The width of the value on display, with the slack [_maxTextSize] leaves.
  double _selectedTextWidth(TextStyle style) {
    final painter = TextPainter(
      text: TextSpan(text: state.text ?? "", style: style),
      textDirection: TextDirection.ltr,
      maxLines: 1,
    )..layout();
    return painter.width + 2;
  }

  Size _calculatePreferredSize(
    Size textSize,
    ComboThemeExtension theme,
    bool isSimple,
  ) {
    // The border insets the Row (Container pads by decoration.padding), and the
    // arrow cell consumes iconSpacing + iconSize; both must be part of the
    // preferred width or the longest item is clipped by the arrow.
    final double width =
        textSize.width +
        theme.textFieldPadding.horizontal +
        theme.borderWidth * 2 +
        _arrowCell(theme, isSimple);
    final double height = textSize.height + theme.textFieldPadding.vertical;

    return Size(width, height);
  }

  /// The text-field padding is a fixed inset, so a size an application pins below
  /// the preferred one (GridData.heightHint / widthHint) leaves EditableText a
  /// viewport of a couple of pixels and the glyphs are cut off. The text keeps
  /// its full extent -- a whole line height, and the whole width of the value on
  /// display -- and the padding absorbs the deficit, down to none at all.
  ///
  /// [verticalRoom] and [horizontalRoom] are what the pinned size leaves the text
  /// on each axis; null where nothing is pinned.
  EdgeInsets _fitTextPadding(
    EdgeInsets padding, {
    double? verticalRoom,
    double? horizontalRoom,
  }) {
    EdgeInsets fitted = padding;
    if (verticalRoom != null && verticalRoom < padding.vertical) {
      final double half = verticalRoom > 0 ? verticalRoom / 2 : 0;
      fitted = fitted.copyWith(top: half, bottom: half);
    }
    if (horizontalRoom != null && horizontalRoom < padding.horizontal) {
      final double half = horizontalRoom > 0 ? horizontalRoom / 2 : 0;
      fitted = fitted.copyWith(left: half, right: half);
    }
    return fitted;
  }

  @override
  void extraSetState() {
    final String newText = state.text ?? "";
    // Ignore a stale echo of our own in-flight typing (see PendingTextEchoes); a value we
    // never sent is a genuine external change and still updates the controller below.
    if (!isStaleTextEcho(newText, _controller.text) &&
        _controller.text != newText) {
      _controller.text = newText;
      _controller.selection = TextSelection.collapsed(
        offset: _controller.text.length,
      );
    }
    final bool? newVisible = state.listVisible;
    if (newVisible != _lastJavaListVisible) {
      _lastJavaListVisible = newVisible;
      _showList(newVisible == true);
    }
  }

  void _showList(bool show) {
    if (show) {
      _highlighted = (state.items ?? const <String>[]).indexOf(state.text ?? "");
      _overlayController.show();
    } else {
      _overlayController.hide();
    }
  }

  bool _listClaimsKey(KeyEvent event) =>
      mounted && _overlayController.isShowing && _listKeys.contains(event.logicalKey);

  static final Set<LogicalKeyboardKey> _listKeys = {
    LogicalKeyboardKey.arrowUp,
    LogicalKeyboardKey.arrowDown,
    LogicalKeyboardKey.enter,
    LogicalKeyboardKey.numpadEnter,
    LogicalKeyboardKey.escape,
    LogicalKeyboardKey.f4,
  };

  /// Native SWT hands the open list the Combo's keys; without this the arrows and Enter only move
  /// the caret, or reach whatever control Java still thinks is focused.
  KeyEventResult _handleKey(FocusNode node, KeyEvent event) {
    if (event is! KeyDownEvent && event is! KeyRepeatEvent) return KeyEventResult.ignored;
    if (StyleBits(state.style).has(SWT.SIMPLE)) return KeyEventResult.ignored;
    final LogicalKeyboardKey key = event.logicalKey;
    final bool alt = HardwareKeyboard.instance.isAltPressed;
    final bool down = key == LogicalKeyboardKey.arrowDown;
    final bool up = key == LogicalKeyboardKey.arrowUp;
    if (!_overlayController.isShowing) {
      if (key == LogicalKeyboardKey.f4 || (alt && down)) {
        _toggleOverlay();
        return KeyEventResult.handled;
      }
      return KeyEventResult.ignored;
    }
    final List<String> items = state.items ?? const <String>[];
    if ((up || down) && !alt) {
      if (items.isNotEmpty) {
        setState(() => _highlighted = (_highlighted + (down ? 1 : -1)).clamp(0, items.length - 1));
        WidgetsBinding.instance.addPostFrameCallback((_) {
          final BuildContext? item = _highlightedKey.currentContext;
          if (item != null) Scrollable.ensureVisible(item);
        });
      }
      return KeyEventResult.handled;
    }
    if (key == LogicalKeyboardKey.enter || key == LogicalKeyboardKey.numpadEnter) {
      if (_highlighted >= 0 && _highlighted < items.length) {
        _onItemSelected(items[_highlighted]);
      } else {
        _toggleOverlay();
      }
      return KeyEventResult.handled;
    }
    if (key == LogicalKeyboardKey.escape || key == LogicalKeyboardKey.f4 || (alt && (up || down))) {
      _toggleOverlay();
      return KeyEventResult.handled;
    }
    return KeyEventResult.ignored;
  }

  void _handleFocusChange() {
    if (!mounted) return;
    setState(() => _isFocused = _focusNode.hasFocus);
    if (_focusNode.hasFocus) {
      // Record the pre-edit text so a later stale echo of it is ignored rather than applied over
      // in-progress typing (no keystroke records this value, so the echo guard can't recognise it).
      seedTextEchoBaseline(_controller.text);
      widget.sendFocusFocusIn(state, null);
    } else {
      clearSentTextEchoes();
      widget.sendFocusFocusOut(state, null);
    }
  }

  /// Forwards each edit of the text field as a Modify, the way native SWT does: anything that
  /// reads Combo.getText() from a Modify/Key listener (JFace field-editor validation) would
  /// otherwise keep seeing the pre-edit value for as long as the user only types.
  void _handleTextChanged(String value) {
    state.text = value;
    recordSentText(value);
    widget.sendModifyModify(state, VEvent()..text = value);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<ComboThemeExtension>()!;
    final bool isEnabled = state.enabled ?? false;
    final styleBits = StyleBits(state.style);
    final bool isSimple = styleBits.has(SWT.SIMPLE);
    final bool isReadOnly = styleBits.has(SWT.READ_ONLY);
    final bool hasFixedSize = hasBounds(state.bounds);

    final Color bgColor = getComboBackgroundColor(
      context,
      state,
      theme,
      enabled: isEnabled,
    );
    final Color textColor = getComboTextColor(context, state, theme, enabled: isEnabled);
    final Color borderColor = !isEnabled
        ? theme.disabledBorderColor
        : (_isFocused
            ? theme.focusedBorderColor
            : (_isHovered ? theme.borderColor : theme.dividerColor));
    final Color iconColor = getComboIconColor(theme, enabled: isEnabled);

    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: theme.textStyle,
    );

    final Size textSize = _maxTextSize(textStyle);
    final Size preferredSize = _calculatePreferredSize(
      textSize,
      theme,
      isSimple,
    );

    final double width = hasFixedSize
        ? state.bounds!.width.toDouble()
        : preferredSize.width;

    final double? height = hasFixedSize
        ? state.bounds!.height.toDouble()
        : (isSimple ? null : preferredSize.height);

    // What the pinned width leaves the insets that can yield, once the border
    // and the arrow glyph -- the ones that cannot -- are out. The gap ahead of
    // the arrow yields first, the text padding takes the rest of the deficit;
    // a width an application pins below the preferred one (GridData.widthHint)
    // then shows as much of the value as native SWT does in the same box.
    final double? insetRoom = hasFixedSize
        ? width -
              theme.borderWidth * 2 -
              _arrowGlyph(theme, isSimple) -
              _selectedTextWidth(textStyle)
        : null;
    final double arrowSpacing = insetRoom == null
        ? theme.iconSpacing
        : insetRoom.clamp(0.0, theme.iconSpacing);

    final EdgeInsets textPadding = _fitTextPadding(
      theme.textFieldPadding,
      verticalRoom: height == null
          ? null
          : height - theme.borderWidth * 2 - textSize.height,
      horizontalRoom: insetRoom == null ? null : insetRoom - arrowSpacing,
    );

    final Widget content = isSimple
        ? _SimpleComboLayout(
            state: state,
            theme: theme,
            controller: _controller,
            focusNode: _focusNode,
            textStyle: textStyle,
            isEnabled: isEnabled,
            isReadOnly: isReadOnly,
            bgColor: bgColor,
            borderColor: borderColor,
            onSelected: _onItemSelected,
            onTextChanged: _handleTextChanged,
            hasFixedSize: hasFixedSize,
          )
        : _DropdownComboLayout(
            state: state,
            theme: theme,
            controller: _controller,
            focusNode: _focusNode,
            textStyle: textStyle,
            isEnabled: isEnabled,
            isReadOnly: isReadOnly,
            bgColor: bgColor,
            borderColor: borderColor,
            iconColor: iconColor,
            overlayController: _overlayController,
            layerLink: _layerLink,
            onSelected: _onItemSelected,
            onTextChanged: _handleTextChanged,
            onToggleOverlay: _toggleOverlay,
            highlighted: _highlighted,
            highlightedKey: _highlightedKey,
            onHighlight: (i) => setState(() => _highlighted = i),
            width: width,
            textPadding: textPadding,
            arrowSpacing: arrowSpacing,
          );

    return tagSemantics(DoubleClickWordSelector(
      controller: _controller,
      focusNode: _focusNode,
      text: _controller.text,
      onWordSelected: (start, end) {
        widget.sendMouseMouseDoubleClick(
          state,
          VEvent()
            ..button = 1
            ..count = 2
            ..start = start
            ..end = end,
        );
        _focusNode.requestFocus();
      },
      child: SizedBox(width: width, height: height, child: content),
    ));
  }

  /// Toggles the dropdown keeping [VCombo.listVisible] truthful, so a value push that reuses the
  /// state object cannot resurrect a stale flag.
  ///
  /// Opening it focuses the Combo, as a click on a native one does: its keys belong to the list.
  void _toggleOverlay() {
    final bool showing = !_overlayController.isShowing;
    if (showing) _focusNode.requestFocus();
    setState(() {
      state.listVisible = showing;
      _showList(showing);
    });
  }

  void _onItemSelected(String? value) {
    setState(() {
      state.text = value;
      _controller.text = value ?? "";
      state.listVisible = false;
      _overlayController.hide();
    });
    widget.sendSelectionSelection(state, VEvent()..text = value);
  }

  @override
  void dispose() {
    removeOpenListKeyClaim(_listClaimsKey);
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }
}

class _DropdownComboLayout extends StatelessWidget {
  final VCombo state;
  final ComboThemeExtension theme;
  final TextEditingController controller;
  final FocusNode focusNode;
  final TextStyle textStyle;
  final bool isEnabled, isReadOnly;
  final Color bgColor, borderColor, iconColor;
  final OverlayPortalController overlayController;
  final LayerLink layerLink;
  final ValueChanged<String?> onSelected;
  final ValueChanged<String> onTextChanged;
  final VoidCallback onToggleOverlay;
  final int highlighted;
  final GlobalKey highlightedKey;
  final ValueChanged<int> onHighlight;
  final double width;
  final EdgeInsets textPadding;
  final double arrowSpacing;

  const _DropdownComboLayout({
    required this.state,
    required this.theme,
    required this.controller,
    required this.focusNode,
    required this.textStyle,
    required this.isEnabled,
    required this.isReadOnly,
    required this.bgColor,
    required this.borderColor,
    required this.iconColor,
    required this.overlayController,
    required this.layerLink,
    required this.onSelected,
    required this.onTextChanged,
    required this.onToggleOverlay,
    required this.highlighted,
    required this.highlightedKey,
    required this.onHighlight,
    required this.width,
    required this.textPadding,
    required this.arrowSpacing,
  });

  @override
  Widget build(BuildContext context) {
    return OverlayPortal(
      controller: overlayController,
      overlayChildBuilder: (_) => _buildOverlay(),
      child: CompositedTransformTarget(
        link: layerLink,
        child: Container(
          decoration: BoxDecoration(
            color: bgColor,
            borderRadius: BorderRadius.circular(theme.borderRadius),
            border: Border.all(color: borderColor, width: theme.borderWidth),
          ),
          child: Row(
            children: [
              Expanded(
                child: GestureDetector(
                  behavior: HitTestBehavior.opaque,
                  onTap: isEnabled && !isReadOnly
                      ? () => focusNode.requestFocus()
                      : (isEnabled ? onToggleOverlay : null),
                  child: Padding(
                    padding: textPadding,
                    // The DOM <input> Flutter Web builds for a text field's semantics node is
                    // disabled unless that node carries SemanticsFlag.isEnabled, and a disabled
                    // input can never take DOM focus, so no keystroke ever reaches the field. A
                    // bare EditableText does not set the flag (TextField is what normally does),
                    // hence this annotation.
                    child: IgnorePointer(
                      ignoring: isReadOnly,
                      child: Semantics(
                        enabled: isEnabled,
                        child: EditableText(
                          controller: controller,
                          focusNode: focusNode,
                          readOnly: isReadOnly,
                          onChanged: onTextChanged,
                          style: textStyle,
                          cursorColor: textStyle.color ?? theme.textColor,
                          backgroundCursorColor: bgColor,
                          // A READ_ONLY Combo has no editable text to select,
                          // so the word selection a double-click leaves behind
                          // must not be painted: native SWT shows none.
                          selectionColor: isReadOnly
                              ? const Color(0x00000000)
                              : (DefaultSelectionStyle.of(context).selectionColor ??
                                  Theme.of(context).colorScheme.primary.withOpacity(0.4)),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
              GestureDetector(
                behavior: HitTestBehavior.opaque,
                onTap: isEnabled ? onToggleOverlay : null,
                child: Padding(
                  // Right-only: the text field's own right padding already
                  // separates text from arrow; a left inset here eats viewport
                  // width the preferred size doesn't account for.
                  padding: EdgeInsets.only(right: arrowSpacing),
                  child: Icon(
                    Icons.arrow_drop_down,
                    color: iconColor,
                    size: theme.iconSize,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildOverlay() {
    return Align(
      alignment: Alignment.topLeft,
      child: CompositedTransformFollower(
        link: layerLink,
        targetAnchor: Alignment.bottomLeft,
        followerAnchor: Alignment.topLeft,
        child: pointerInterceptor(TapRegion(
          onTapOutside: (_) => overlayController.hide(),
          // SWT sizes the list to its widest item and never narrower than the
          // field. Pinning it to the field's width wraps every item of a Combo
          // an application hinted below its preferred width, and the list then
          // grows down over whatever follows it.
          child: ConstrainedBox(
            constraints: BoxConstraints(minWidth: width),
            child: Container(
              decoration: BoxDecoration(
                // The drop-down is part of the Combo, so it carries the Combo's own ground: the
                // closed field already did, and the list looked like a different widget.
                color: getBackgroundColor(
                      background: state.background,
                      defaultColor: theme.backgroundColor,
                    ) ??
                    theme.backgroundColor,
                borderRadius: BorderRadius.circular(theme.borderRadius),
                border: Border.all(
                  color: theme.dividerColor,
                  width: theme.borderWidth,
                ),
              ),
              child: SingleChildScrollView(
                child: IntrinsicWidth(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      for (final (i, item) in (state.items ?? const <String>[]).indexed)
                        _ComboItem(
                          key: i == highlighted ? highlightedKey : null,
                          text: item,
                          isSelected: i == highlighted,
                          theme: theme,
                          textStyle: textStyle,
                          onHover: () => onHighlight(i),
                          onTap: () => onSelected(item),
                        ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        )),
      ),
    );
  }
}

class _SimpleComboLayout extends StatelessWidget {
  final VCombo state;
  final ComboThemeExtension theme;
  final TextEditingController controller;
  final FocusNode focusNode;
  final TextStyle textStyle;
  final bool isEnabled, isReadOnly, hasFixedSize;
  final Color bgColor, borderColor;
  final ValueChanged<String?> onSelected;
  final ValueChanged<String> onTextChanged;

  const _SimpleComboLayout({
    required this.state,
    required this.theme,
    required this.controller,
    required this.focusNode,
    required this.textStyle,
    required this.isEnabled,
    required this.isReadOnly,
    required this.hasFixedSize,
    required this.bgColor,
    required this.borderColor,
    required this.onSelected,
    required this.onTextChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(theme.borderRadius),
        border: Border.all(color: borderColor, width: theme.borderWidth),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: theme.textFieldPadding,
            // See _DropdownComboLayout: without isEnabled on the semantics node the
            // DOM <input> Flutter Web builds for it is disabled and cannot be focused.
            child: Semantics(
              enabled: isEnabled,
              child: EditableText(
                controller: controller,
                focusNode: focusNode,
                readOnly: isReadOnly,
                onChanged: onTextChanged,
                style: textStyle,
                cursorColor: textStyle.color ?? theme.textColor,
                backgroundCursorColor: bgColor,
                selectionColor: DefaultSelectionStyle.of(context).selectionColor ??
                    Theme.of(context).colorScheme.primary.withOpacity(0.4),
              ),
            ),
          ),
          Divider(
            height: theme.dividerHeight,
            thickness: theme.dividerThickness,
            color: theme.dividerColor,
          ),
          _buildList(),
        ],
      ),
    );
  }

  Widget _buildList() {
    final list = SingleChildScrollView(
      child: Column(
        children: (state.items ?? [])
            .map(
              (item) => _ComboItem(
                text: item,
                isSelected: item == state.text,
                theme: theme,
                textStyle: textStyle,
                onTap: isEnabled ? () => onSelected(item) : () {},
              ),
            )
            .toList(),
      ),
    );
    return hasFixedSize ? Expanded(child: list) : list;
  }
}

class _ComboItem extends StatefulWidget {
  final String text;
  final bool isSelected;
  final ComboThemeExtension theme;
  final TextStyle textStyle;
  final VoidCallback onTap;
  // The drop-down list has one highlighted item: hover moves it, instead of painting a second one.
  final VoidCallback? onHover;

  const _ComboItem({
    super.key,
    required this.text,
    required this.isSelected,
    required this.theme,
    required this.textStyle,
    required this.onTap,
    this.onHover,
  });

  @override
  State<_ComboItem> createState() => _ComboItemState();
}

class _ComboItemState extends State<_ComboItem> {
  bool _itemHovered = false;

  @override
  Widget build(BuildContext context) {
    final Color bgColor = getComboItemBackgroundColor(
        widget.theme, widget.isSelected, widget.onHover == null && _itemHovered);

    return MouseRegion(
      onEnter: (_) => setState(() => _itemHovered = true),
      onExit: (_) => setState(() => _itemHovered = false),
      onHover: widget.onHover == null || widget.isSelected ? null : (_) => widget.onHover!(),
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: widget.onTap,
        child: AnimatedContainer(
          duration: widget.theme.animationDuration,
          padding: widget.theme.itemPadding,
          color: bgColor,
          // An SWT Combo item is a single line on every platform: it is the
          // list that widens, never the item that wraps.
          child: Text(
            widget.text,
            style: widget.textStyle,
            softWrap: false,
            maxLines: 1,
          ),
        ),
      ),
    );
  }
}
