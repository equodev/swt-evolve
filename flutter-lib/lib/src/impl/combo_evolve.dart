import 'package:flutter/foundation.dart' show listEquals;
import 'package:flutter/rendering.dart';
import 'dart:math' as math;

import 'package:flutter/foundation.dart' show TargetPlatform, defaultTargetPlatform;
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter/material.dart' show Icons, Icon, Divider, Theme;

import '../gen/combo.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart' show WireChannel, WireKey;
import '../impl/composite_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/combo_theme_extension.dart';
import '../theme/theme_settings/combo_theme_settings.dart';
import 'utils/font_utils.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'utils/client_caret_report.dart';
import 'utils/pending_text_echoes.dart';
import 'key_forwarding.dart';
import 'utils/pointer.dart';
import 'utils/quiet_animations.dart';

class ComboImpl<T extends ComboSwt, V extends VCombo>
    extends CompositeImpl<T, V> with PendingTextEchoes, ClientCaretReport {
  @override
  bool get keepsWheel => true;

  late TextEditingController _controller;
  final FocusNode _focusNode = FocusNode();
  final OverlayPortalController _overlayController = OverlayPortalController();
  bool _isFocused = false;
  final bool _isHovered = false;
  /// The last `listVisible` Java sent. The dropdown is otherwise local UI state Java only drives
  /// through `Combo.setListVisible`, so only a change between two pushes is a command.
  bool? _lastJavaListVisible;
  /// The item the keyboard points at while the list is open; Enter commits it.
  int _highlighted = -1;
  /// Whether the highlighted item is already the selection, chosen by a typed letter.
  bool _highlightSelected = false;
  final GlobalKey _highlightedKey = GlobalKey();

  /// The field is the only node a Combo mounts, and the one its keys are handled on.
  @override
  FocusNode? get swtFocusNode => _focusNode;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _controller.addListener(_reportCaret);
    _focusNode.addListener(_handleFocusChange);
    _focusNode.onKeyEvent = _handleKey;
    addOpenListKeyClaim(_listClaimsKey);
    _lastJavaListVisible = state.listVisible;
  }

  /// The largest of the value's and the items' single-line sizes, kept until any of them or the
  /// style changes: a combo rebuilds for much more than its list.
  Size _maxTextSize(TextStyle style) {
    final text = state.text ?? "";
    final items = state.items ?? const <String>[];
    final held = _measured;
    if (held != null && held.$1 == text && held.$3 == style && listEquals(held.$2, items)) return held.$4;

    double maxTextWidth = 0;
    double maxTextHeight = 0;
    final measure = singleLineTextSizes(style);
    for (final s in [text, ...items]) {
      final size = measure(s);
      if (size.width > maxTextWidth) maxTextWidth = size.width + 2;
      if (size.height > maxTextHeight) maxTextHeight = size.height + 2;
    }
    final result = Size(maxTextWidth, maxTextHeight);
    _measured = (text, List<String>.of(items), style, result);
    return result;
  }

  (String, List<String>, TextStyle, Size)? _measured;

  /// The arrow's own cell in the Row: the icon plus the gap that separates it
  /// from the text.
  double _arrowCell(ComboThemeExtension theme, bool isSimple) =>
      isSimple ? 0 : theme.iconSpacing + theme.iconSize;

  /// The arrow glyph alone -- the one inset, besides the border, that a pinned
  /// width cannot take back.
  double _arrowGlyph(ComboThemeExtension theme, bool isSimple) =>
      isSimple ? 0 : theme.iconSize;

  /// The width of the value on display, with the slack [_maxTextSize] leaves.
  double _selectedTextWidth(TextStyle style) => singleLineTextWidth(state.text ?? "", style) + 2;

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
  void extraSetState() => fromJava(_applyJavaState);

  void _applyJavaState() {
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

  /// Java's echo of a list the user opened lands on an open list, which keeps its highlight.
  void _showList(bool show) {
    if (show == _overlayController.isShowing) return;
    if (show) {
      _highlighted = (state.items ?? const <String>[]).indexOf(state.text ?? "");
      _highlightSelected = false;
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
    final List<String> items = state.items ?? const <String>[];
    if (!_overlayController.isShowing) {
      if (key == LogicalKeyboardKey.f4 || (alt && down)) {
        _toggleOverlay();
        return KeyEventResult.handled;
      }
      return _handleClosedListKey(event, items);
    }
    if ((up || down) && !alt) {
      if (items.isNotEmpty) {
        setState(() {
          _highlighted = (_highlighted + (down ? 1 : -1)).clamp(0, items.length - 1);
          _highlightSelected = false;
        });
        WidgetsBinding.instance.addPostFrameCallback((_) {
          final BuildContext? item = _highlightedKey.currentContext;
          if (item != null) Scrollable.ensureVisible(item);
        });
      }
      return KeyEventResult.handled;
    }
    if (key == LogicalKeyboardKey.enter || key == LogicalKeyboardKey.numpadEnter) {
      if (_highlightSelected) {
        _setListOpen(false);
      } else if (_highlighted >= 0 && _highlighted < items.length) {
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
    if (_selectsByLetterWithListOpen && _isTypedLetter(event)) {
      final int? target = _nextStartingWith(items, items.indexOf(state.text ?? ""), event.character!);
      if (target != null) {
        setState(() {
          _highlighted = target;
          _highlightSelected = true;
        });
        _selectKeepingListOpen(items[target]);
      }
      return KeyEventResult.handled;
    }
    return KeyEventResult.ignored;
  }

  /// A READ_ONLY Combo's open list answers a typed letter as Win32 does: the letter selects, as it
  /// does with the list closed. GTK's list ignores it, and so does this one on macOS.
  bool get _selectsByLetterWithListOpen =>
      StyleBits(state.style).has(SWT.READ_ONLY) &&
      defaultTargetPlatform != TargetPlatform.linux &&
      defaultTargetPlatform != TargetPlatform.macOS &&
      defaultTargetPlatform != TargetPlatform.iOS;

  /// GTK's closed READ_ONLY Combo ignores a typed letter; Win32 selects with it.
  static bool get _selectsByLetterWithListClosed => defaultTargetPlatform != TargetPlatform.linux;

  bool _isTypedLetter(KeyEvent event) {
    final HardwareKeyboard keyboard = HardwareKeyboard.instance;
    return !keyboard.isAltPressed &&
        !keyboard.isControlPressed &&
        !keyboard.isMetaPressed &&
        _isTypedCharacter(event.character);
  }

  /// The rows a Page key moves, as a Windows list showing SWT's default five items does.
  static const int _pageStep = 4;

  /// A focused Combo with its list closed, as on Windows: the arrows step through the items, a
  /// READ_ONLY one also takes Home, End, the Page keys and a typed letter, and Enter is the default
  /// selection. An editable Combo leaves the rest to its text.
  KeyEventResult _handleClosedListKey(KeyEvent event, List<String> items) {
    final LogicalKeyboardKey key = event.logicalKey;
    if (key == LogicalKeyboardKey.enter || key == LogicalKeyboardKey.numpadEnter) {
      widget.sendSelectionDefaultSelection(state, VEvent()..text = state.text);
      return KeyEventResult.handled;
    }
    final HardwareKeyboard keyboard = HardwareKeyboard.instance;
    if (keyboard.isAltPressed || keyboard.isControlPressed || keyboard.isMetaPressed) {
      return KeyEventResult.ignored;
    }
    final bool readOnly = StyleBits(state.style).has(SWT.READ_ONLY);
    final int current = items.indexOf(state.text ?? "");
    final int last = items.length - 1;
    int? target;
    if (key == LogicalKeyboardKey.arrowDown) {
      target = current + 1;
    } else if (key == LogicalKeyboardKey.arrowUp) {
      target = current < 0 ? 0 : current - 1;
    } else if (readOnly && key == LogicalKeyboardKey.home) {
      target = 0;
    } else if (readOnly && key == LogicalKeyboardKey.end) {
      target = last;
    } else if (readOnly && key == LogicalKeyboardKey.pageDown) {
      target = current + _pageStep;
    } else if (readOnly && key == LogicalKeyboardKey.pageUp) {
      target = current < 0 ? 0 : current - _pageStep;
    } else if (readOnly && _isTypedCharacter(event.character)) {
      if (!_selectsByLetterWithListClosed) return KeyEventResult.ignored;
      target = _nextStartingWith(items, current, event.character!);
      if (target == null) return KeyEventResult.handled;
    } else {
      return KeyEventResult.ignored;
    }
    if (items.isNotEmpty) {
      final int index = target.clamp(0, last);
      if (index != current) _onItemSelected(items[index]);
    }
    return KeyEventResult.handled;
  }

  static bool _isTypedCharacter(String? character) =>
      character != null && character.length == 1 && character.codeUnitAt(0) >= 0x20;

  /// The first item after [current] that starts with [character], wrapping around the list.
  static int? _nextStartingWith(List<String> items, int current, String character) {
    final String prefix = character.toLowerCase();
    for (int step = 1; step <= items.length; step++) {
      final int i = (current + step) % items.length;
      if (items[i].toLowerCase().startsWith(prefix)) return i;
    }
    return null;
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
    _reportCaret();
  }

  /// A closed drop-down shows the value through its controller and the items not at all: a change
  /// naming only these needs no rebuild while the field keeps its layout. A SIMPLE combo shows its
  /// list.
  static const _fieldProperties = {WireKey.k_Combo_items, WireKey.k_Combo_text};

  @override
  int? get reportedCaret => state.caretPosition;

  @override
  set reportedCaret(int? caret) => state.caretPosition = caret;

  @override
  void sendCaretReport(VEvent event) => widget.sendVerifyVerify(state, event);

  /// A typed edit reaches Java as the Modify after this listener runs, and resets Java's selection
  /// there: its caret is reported once the Modify is sent.
  void _reportCaret() =>
      reportCaret(_controller, editPending: _controller.text != (state.text ?? ""));

  @override
  void setValue(V value) {
    final change = lastChange;
    if (mounted &&
        change != null &&
        change.isOnly(_fieldProperties) &&
        !_overlayController.isShowing &&
        !StyleBits(state.style).has(SWT.SIMPLE) &&
        _built != null &&
        _built == _layout(_builtTheme!, _builtTextStyle!)) {
      extraSetState();
      return;
    }
    super.setValue(value);
  }

  /// The layout of the last build, which a change of the value or the items may leave as it is.
  _ComboLayout? _built;

  /// The theme and text style the last build laid out with. Only a build changes them.
  ComboThemeExtension? _builtTheme;
  TextStyle? _builtTextStyle;

  TextStyle _textStyle(BuildContext context) {
    final theme = Theme.of(context).extension<ComboThemeExtension>()!;
    return getTextStyle(
      context: context,
      font: state.font,
      textColor: getComboTextColor(context, state, theme, enabled: state.enabled ?? false),
      baseTextStyle: theme.textStyle,
    );
  }

  _ComboLayout _layout(ComboThemeExtension theme, TextStyle textStyle) {
    final bool isSimple = StyleBits(state.style).has(SWT.SIMPLE);
    final bool hasFixedSize = hasBounds(state.bounds);
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
    return (width: width, height: height, textPadding: textPadding, arrowSpacing: arrowSpacing);
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
    final Color borderColor = !isEnabled
        ? theme.disabledBorderColor
        : (_isFocused
            ? theme.focusedBorderColor
            : (_isHovered ? theme.borderColor : theme.dividerColor));
    final Color iconColor = getComboIconColor(theme, enabled: isEnabled);

    final textStyle = _textStyle(context);
    final layout = _layout(theme, textStyle);
    _built = layout;
    _builtTheme = theme;
    _builtTextStyle = textStyle;
    final (:width, :height, :textPadding, :arrowSpacing) = layout;

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
            onSelected: _onItemSelected,
            onTextChanged: _handleTextChanged,
            onToggleOverlay: _toggleOverlay,
            onDismiss: () => _setListOpen(false),
            highlighted: _highlighted,
            highlightedKey: _highlightedKey,
            onHighlight: (i) => setState(() {
              _highlighted = i;
              _highlightSelected = false;
            }),
            width: width,
            textPadding: textPadding,
            arrowSpacing: arrowSpacing,
          );

    return tagSemantics(DoubleClickWordSelector(
      controller: _controller,
      focusNode: _focusNode,
      // The MouseDoubleClick itself is the press's, sent by trackPointer below.
      onWordSelected: (start, end) => _focusNode.requestFocus(),
      child: trackPointer(SizedBox(width: width, height: height, child: content)),
    ));
  }

  /// Opening the list focuses the Combo, as a click on a native one does: its keys belong to the list.
  void _toggleOverlay() {
    final bool showing = !_overlayController.isShowing;
    if (showing) _focusNode.requestFocus();
    _setListOpen(showing);
  }

  /// Opens or closes the list on the user's behalf. [VCombo.listVisible] stays truthful, so a value
  /// push that reuses the state object cannot resurrect a stale flag, and Java is told: Flutter draws
  /// the list, so Java has no other way to know.
  void _setListOpen(bool open) {
    if (open == _overlayController.isShowing) return;
    setState(() {
      state.listVisible = open;
      _showList(open);
    });
    widget.sendEvent(state, WireChannel.listVisible, VEvent()..detail = open ? 1 : 0);
  }

  void _onItemSelected(String? value) {
    _setListOpen(false);
    _selectKeepingListOpen(value);
  }

  void _selectKeepingListOpen(String? value) {
    setState(() {
      state.text = value;
      _controller.text = value ?? "";
    });
    widget.sendSelectionSelection(state, VEvent()..text = value);
  }

  @override
  void dispose() {
    removeOpenListKeyClaim(_listClaimsKey);
    _controller.removeListener(_reportCaret);
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }
}

typedef _ComboLayout = ({double width, double? height, EdgeInsets textPadding, double arrowSpacing});

class _DropdownComboLayout extends StatelessWidget {
  final VCombo state;
  final ComboThemeExtension theme;
  final TextEditingController controller;
  final FocusNode focusNode;
  final TextStyle textStyle;
  final bool isEnabled, isReadOnly;
  final Color bgColor, borderColor, iconColor;
  final OverlayPortalController overlayController;
  final ValueChanged<String?> onSelected;
  final ValueChanged<String> onTextChanged;
  final VoidCallback onToggleOverlay;
  final VoidCallback onDismiss;
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
    required this.onSelected,
    required this.onTextChanged,
    required this.onToggleOverlay,
    required this.onDismiss,
    required this.highlighted,
    required this.highlightedKey,
    required this.onHighlight,
    required this.width,
    required this.textPadding,
    required this.arrowSpacing,
  });

  @override
  Widget build(BuildContext context) {
    // The layout builder hands over the field's rect on every layout, so an open list keeps
    // following a field that scrolls or moves without the Combo itself rebuilding.
    return OverlayPortal.overlayChildLayoutBuilder(
      controller: overlayController,
      overlayChildBuilder: (_, info) => _buildOverlay(
          MatrixUtils.transformRect(info.childPaintTransform, Offset.zero & info.childSize)),
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
                  child: isReadOnly
                      ? Focus(
                          focusNode: focusNode,
                          child: ComboValueText(controller: controller, style: textStyle),
                        )
                      : Semantics(
                          enabled: isEnabled,
                          child: EditableText(
                            controller: controller,
                            focusNode: focusNode,
                            onChanged: onTextChanged,
                            style: textStyle,
                            cursorColor: textStyle.color ?? theme.textColor,
                            backgroundCursorColor: bgColor,
                            selectionColor: DefaultSelectionStyle.of(context).selectionColor ??
                                Theme.of(context).colorScheme.primary.withOpacity(0.4),
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
    );
  }

  /// [field] is the field's rect in the coordinates of the overlay the list is shown in.
  Widget _buildOverlay(Rect field) {
    return CustomSingleChildLayout(
      delegate: _ComboListLayout(field),
      child: pointerInterceptor(TapRegion(
        onTapOutside: (_) => onDismiss(),
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
    );
  }
}

/// Places the list under the field, and above it when it does not fit underneath and there is more
/// room above, as native SWT does: a Combo on the window's bottom edge would otherwise open
/// off-screen.
class _ComboListLayout extends SingleChildLayoutDelegate {
  _ComboListLayout(this.field);

  final Rect field;

  @override
  BoxConstraints getConstraintsForChild(BoxConstraints constraints) {
    final double below = constraints.maxHeight - field.bottom;
    return constraints.loosen().copyWith(maxHeight: math.max(0.0, math.max(below, field.top)));
  }

  @override
  Offset getPositionForChild(Size size, Size childSize) {
    final double below = size.height - field.bottom;
    final bool above = childSize.height > below && field.top > below;
    final double x = math.max(0.0, math.min(field.left, size.width - childSize.width));
    return Offset(x, above ? field.top - childSize.height : field.bottom);
  }

  @override
  bool shouldRelayout(_ComboListLayout oldDelegate) => oldDelegate.field != field;
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
        child: QuietAnimatedContainer(
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

/// The value of a READ_ONLY Combo, laid out and drawn as a read-only [EditableText] of one line
/// draws it: on a strut the style forces, unwrapped, in the style as given, and clipped as it clips. Nothing in
/// it can be edited or selected, so it carries none of an editable text's machinery.
class ComboValueText extends StatelessWidget {
  const ComboValueText({super.key, required this.controller, required this.style});

  final TextEditingController controller;
  final TextStyle style;

  @override
  Widget build(BuildContext context) {
    final effectiveStyle =
        MediaQuery.boldTextOf(context) ? style.merge(const TextStyle(fontWeight: FontWeight.bold)) : style;
    return ValueListenableBuilder<TextEditingValue>(
      valueListenable: controller,
      builder: (context, value, _) => _ClipOverflowingText(
        child: RichText(
          text: TextSpan(text: value.text, style: effectiveStyle),
          strutStyle: StrutStyle.fromTextStyle(effectiveStyle, forceStrutHeight: true),
          maxLines: 1,
          softWrap: false,
          overflow: TextOverflow.visible,
          textScaler: MediaQuery.textScalerOf(context),
          textHeightBehavior: DefaultTextHeightBehavior.maybeOf(context),
        ),
      ),
    );
  }
}

/// Clips its text as [RenderEditable] clips a line: with a hard edge, and only when the text and the
/// room it keeps after it for the caret are wider than the field.
class _ClipOverflowingText extends SingleChildRenderObjectWidget {
  const _ClipOverflowingText({required super.child});

  @override
  RenderObject createRenderObject(BuildContext context) => _RenderClipOverflowingText();
}

class _RenderClipOverflowingText extends RenderProxyBox {
  /// [RenderEditable]'s gap before the caret plus [EditableText]'s default cursor width.
  static const double _caretMargin = 1.0 + 2.0;

  final LayerHandle<ClipRectLayer> _clip = LayerHandle<ClipRectLayer>();

  @override
  void paint(PaintingContext context, Offset offset) {
    final child = this.child;
    if (child == null) return;
    if (child.getMaxIntrinsicWidth(double.infinity) + _caretMargin > size.width) {
      _clip.layer = context.pushClipRect(needsCompositing, offset, Offset.zero & size, super.paint,
          clipBehavior: Clip.hardEdge, oldLayer: _clip.layer);
    } else {
      _clip.layer = null;
      super.paint(context, offset);
    }
  }

  @override
  void dispose() {
    _clip.layer = null;
    super.dispose();
  }
}
