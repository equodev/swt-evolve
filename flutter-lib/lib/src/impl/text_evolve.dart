import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../comm/comm.dart';
import '../gen/event.dart';
import '../gen/point.dart';
import '../gen/swt.dart';
import '../gen/text.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';
import '../theme/theme_extensions/text_theme_extension.dart';
import '../theme/theme_settings/text_theme_settings.dart';
import 'key_forwarding.dart';
import 'key_mapping.dart';
import 'utils/hover_arbiter.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'utils/pending_text_echoes.dart';

class TextImpl<T extends TextSwt, V extends VText>
    extends ScrollableImpl<T, V> with PendingTextEchoes {
  late TextEditingController _controller;

  final FocusNode _focusNode = FocusNode();

  @override
  FocusNode get swtFocusNode => _focusNode;

  // SWT's VerifyListener runs BEFORE the character is displayed. Java flags a Verify-hooked
  // field via modify/vetoable; each edit is then held un-rendered, sent to Java as the Modify
  // proposal, and applied or dropped on the modify/verdict answer.
  bool _vetoable = false;
  TextEditingValue? _pendingEdit;
  Timer? _pendingTimer;
  Object? _vetoableToken;
  Object? _verdictToken;

  String get _vetoableChannel => '${state.swt}/${state.id}/modify/vetoable';
  String get _verdictChannel => '${state.swt}/${state.id}/modify/verdict';

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _controller.addListener(_updateCaretPosition);
    _focusNode.addListener(_handleFocusChange);
    _vetoableToken = EquoCommService.onRaw(
      _vetoableChannel,
      (args) => _vetoable = _boolArg(args, 'value') ?? false,
    );
    _verdictToken = EquoCommService.onRaw(
      _verdictChannel,
      (args) => _handleModifyVerdict(_boolArg(args, 'doit') ?? true),
    );
    // Initialize caret position
    WidgetsBinding.instance.addPostFrameCallback((_) => _updateCaretPosition());
  }

  static bool? _boolArg(dynamic args, String key) {
    final decoded = args is String ? jsonDecode(args) : args;
    return decoded is Map ? decoded[key] as bool? : null;
  }

  /// Holds an edit while the field is vetoable: the proposal goes to Java as the Modify, the
  /// controller keeps the old value until the verdict. Selection-only changes and the
  /// single-line newline path (submit semantics, see [_handleTextChanged]) stay optimistic.
  TextEditingValue _gateEdit(TextEditingValue oldValue, TextEditingValue newValue) {
    if (!_vetoable) return newValue;
    if (newValue.text == oldValue.text) return newValue;
    final isSingleLineExpand =
        hasBounds(state.bounds) && !hasStyle(state.style, SWT.MULTI);
    if (isSingleLineExpand && newValue.text.contains('\n')) return newValue;
    _pendingEdit = newValue;
    _pendingTimer?.cancel();
    // If Java never answers (e.g. it went away mid-edit), fall back to the optimistic apply
    // rather than swallowing the user's typing.
    _pendingTimer = Timer(
      const Duration(milliseconds: 400),
      () => _resolvePendingEdit(true),
    );
    recordSentText(newValue.text);
    widget.sendModifyModify(
      state,
      VEvent()
        ..text = newValue.text
        ..start = newValue.selection.baseOffset,
    );
    return oldValue;
  }

  void _handleModifyVerdict(bool doit) {
    _pendingTimer?.cancel();
    _pendingTimer = null;
    _resolvePendingEdit(doit);
  }

  void _resolvePendingEdit(bool doit) {
    final pending = _pendingEdit;
    _pendingEdit = null;
    if (!doit) {
      // Java rejected the edit: it is authoritative now. A held edit is simply dropped (it was
      // never rendered). An edit that had already been applied optimistically (no vetoable flag
      // yet) must be reverted from Java's push — which the stale-echo guard would otherwise
      // swallow, since the pre-edit text is its focus baseline — so stop suppressing echoes and
      // resync from the pushed state.
      clearSentTextEchoes();
      if (pending == null && mounted) {
        final authoritative = state.text ?? '';
        if (_controller.text != authoritative) {
          _controller.value = _controller.value.copyWith(
            text: authoritative,
            selection: TextSelection.collapsed(offset: authoritative.length),
            composing: TextRange.empty,
          );
        }
      }
      return;
    }
    if (pending == null || !mounted) return;
    _controller.value = pending;
    state.text = pending.text;
  }

  @override
  void extraSetState() {
    String newText = state.text ?? "";
    // A Java value push may be the echo of a Modify this field sent moments ago. If the user
    // has typed further in the meantime, resetting the controller to the echoed (older) text
    // silently drops those keystrokes (the mid-word lost character on slow machines). See
    // PendingTextEchoes: our own in-flight echoes are ignored, external changes fall through.
    if (isStaleTextEcho(newText, _controller.text)) return;
    bool textChanged = _controller.text != newText;

    if (textChanged) {
      final isReadOnly =
          !(state.editable ?? true) || hasStyle(state.style, SWT.READ_ONLY);
      final cursorOffset = isReadOnly ? 0 : newText.length;

      _controller.value = _controller.value.copyWith(
        text: newText,
        selection: TextSelection.collapsed(offset: cursorOffset),
        composing: TextRange.empty,
      );

      if (state.selection != null) {
        final newSelection = TextSelection(
          baseOffset: state.selection!.x,
          extentOffset: state.selection!.y,
        );
        _controller.selection = newSelection;
      }
    }

    final sel = state.selection;
    if (sel != null && sel.x != sel.y && sel.y <= _controller.text.length) {
      final desired = TextSelection(baseOffset: sel.x, extentOffset: sel.y);
      if (_controller.selection != desired) {
        _controller.selection = desired;
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TextThemeExtension>();
    if (widgetTheme == null) {
      return const SizedBox.shrink();
    }
    // Text builds its own tree and never routes through ControlImpl.wrap(), which is
    // otherwise the only place that honors state.visible == false.
    if (state.visible != null && !state.visible!) {
      return const SizedBox.shrink();
    }

    final textAlign = getTextAlignFromStyle(state.style, TextAlign.left);
    final isMultiLine = hasStyle(state.style, SWT.MULTI);
    final enabled = state.enabled ?? false;
    final editable = state.editable ?? true;
    final hasValidBounds = hasBounds(state.bounds);

    final textField = _buildTextField(
      context,
      widgetTheme,
      enabled,
      textAlign,
      isMultiLine,
      hasValidBounds,
    );

    final fieldWrapper = _buildTextFieldWrapper(
      textField,
      widgetTheme,
      isMultiLine,
      hasValidBounds,
    );
    final hoverDepth = ControlNestingScope.depthOf(context);
    return tagSemantics(
      wrapDnd(
        // When a whole-tree Display forwards keys from its single top-level handler, that handler
        // already delivers shortcuts to Java while a Text has focus, so skip the per-field wrapper
        // to avoid double-dispatch. In the embedded backend (no whole-tree Display) it is the only
        // path — Text builds its own field via [wrapDnd] and never routes through [ControlImpl.wrap].
        // MouseRegion here mirrors ControlImpl.wrap(), which Text also never routes through --
        // otherwise MouseEnter/MouseExit (e.g. a hover highlighter) never fire on a Text.
        MouseRegion(
          onEnter: (_) => HoverExclusivityArbiter.instance
              .setActive(this, hoverDepth, true),
          onExit: (_) => HoverExclusivityArbiter.instance
              .setActive(this, hoverDepth, false),
          child: displayLevelKeyForwardingActive
              ? fieldWrapper
              : _forwardShortcutKeys(fieldWrapper),
        ),
      ),
    );
  }

  /// Forwards shortcut keystrokes the text field itself does not consume (e.g. Ctrl/Cmd+S) to Java
  /// as SWT.KeyDown, so an app's Display key filter or KeyListener — the mechanism Eclipse RCP
  /// command bindings use — sees them even while a Text has focus. Ordinary typing and the field's own
  /// editing shortcuts are consumed by the TextField first and never bubble up here, so this does
  /// not double-process input. The wrapper never takes focus itself; it only observes keys bubbling
  /// from the field. Only used in the embedded backend (see the [build] call site).
  Widget _forwardShortcutKeys(Widget child) {
    return Focus(
      canRequestFocus: false,
      onKeyEvent: (node, event) {
        if (event is KeyDownEvent) {
          final vEvent = mapNewKeyEventToSwt(event);
          if (vEvent.keyCode != 0 || vEvent.character != 0) {
            widget.sendKeyKeyDown(state, vEvent);
          }
        }
        return KeyEventResult.ignored;
      },
      child: child,
    );
  }

  Widget _buildTextField(
    BuildContext context,
    TextThemeExtension widgetTheme,
    bool enabled,
    TextAlign textAlign,
    bool isMultiLine,
    bool hasValidBounds,
  ) {
    final textStyle = getTextFieldTextStyle(context, state, widgetTheme);
    var decoration = getInputDecoration(
      context,
      state,
      widgetTheme,
      _controller,
      () {
        _controller.clear();
        var e = VEvent()..detail = SWT.ICON_CANCEL;
        widget.sendSelectionDefaultSelection(state, e);
      },
    );

    final isReadOnly =
        !(state.editable ?? true) || hasStyle(state.style, SWT.READ_ONLY);
    final hasBorderStyle = hasStyle(state.style, SWT.BORDER);
    final parentBg = ParentBackgroundScope.backgroundOf(context);
    if (parentBg != null && isReadOnly && !hasBorderStyle) {
      decoration = decoration.copyWith(filled: true, fillColor: parentBg);
    }

    final isPassword = hasStyle(state.style, SWT.PASSWORD);
    final echoChar = (isPassword || isMultiLine)
        ? null
        : _obscuringCharacter(state.echoCharacter);
    final isObscured = isPassword || echoChar != null;
    final singleLine = !isMultiLine || isPassword;
    final shouldExpand = hasValidBounds && !singleLine;

    if (hasValidBounds) {
      if (singleLine) {
        final isSearch = hasStyle(state.style, SWT.SEARCH);
        InputBorder noGap(InputBorder? b) =>
            (!isSearch && b is OutlineInputBorder) ? b.copyWith(gapPadding: 0.0) : b!;
        decoration = decoration.copyWith(
          isDense: false,
          contentPadding: const EdgeInsets.symmetric(horizontal: 3.0),
          border: noGap(decoration.border),
          enabledBorder: noGap(decoration.enabledBorder),
          focusedBorder: noGap(decoration.focusedBorder),
          disabledBorder: noGap(decoration.disabledBorder),
          constraints: BoxConstraints.tightFor(
            height: state.bounds!.height.toDouble(),
          ),
        );
      } else {
        decoration = decoration.copyWith(
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 3.0,
            vertical: 4.0,
          ),
        );
      }
    }

    final cursorColor = getForegroundColor(
      foreground: state.foreground,
      defaultColor: widgetTheme.textColor,
      context: context,
    );

    final textField = TextField(
      controller: _controller,
      focusNode: _focusNode,
      enabled: enabled,
      obscureText: isObscured,
      obscuringCharacter: echoChar ?? _defaultObscuringCharacter,
      enableSuggestions: !isObscured,
      autocorrect: !isObscured,
      readOnly: isReadOnly,
      maxLines: singleLine ? 1 : null,
      expands: shouldExpand,
      textAlignVertical:
          singleLine ? TextAlignVertical.center : TextAlignVertical.top,
      textAlign: textAlign,
      style: textStyle,
      decoration: decoration,
      keyboardType: isMultiLine ? TextInputType.multiline : TextInputType.text,
      maxLength: state.textLimit,
      // SWT rejects the keystroke at the limit; Flutter's default on web lets over-limit
      // text stand until composition ends, which briefly renders it.
      maxLengthEnforcement: MaxLengthEnforcement.enforced,
      inputFormatters: [TextInputFormatter.withFunction(_gateEdit)],
      onChanged: _handleTextChanged,
      onSubmitted: _handleSubmitted,
      onTapOutside: (_) {},
      cursorColor: cursorColor,
    );
    return DoubleClickWordSelector(
      controller: _controller,
      focusNode: _focusNode,
      text: state.text ?? '',
      onWordSelected: (start, end) {
        state.selection = VPoint()
          ..x = start
          ..y = end;
        widget.sendMouseMouseDoubleClick(
          state,
          VEvent()
            ..button = 1
            ..count = 2
            ..start = start
            ..end = end,
        );
      },
      child: textField,
    );
  }

  Widget _buildTextFieldWrapper(
    Widget textField,
    TextThemeExtension widgetTheme,
    bool isMultiLine,
    bool hasValidBounds,
  ) {
    final constraints = getConstraintsFromBounds(state.bounds);

    if (hasValidBounds && constraints != null) {
      return ConstrainedBox(constraints: constraints, child: textField);
    }

    return IntrinsicWidth(child: textField);
  }

  static String? _obscuringCharacter(int? echoCharacter) {
    if (echoCharacter == null || echoCharacter == 0) return null;
    final mask = String.fromCharCode(echoCharacter);
    return mask.length == 1 ? mask : _defaultObscuringCharacter;
  }

  static const String _defaultObscuringCharacter = '•';

  void _handleTextChanged(String value) {
    final isSingleLineExpand =
        hasBounds(state.bounds) && !hasStyle(state.style, SWT.MULTI);
    if (isSingleLineExpand && value.contains('\n')) {
      final clean = value.replaceAll('\n', '');
      _controller.value = _controller.value.copyWith(
        text: clean,
        selection: TextSelection.collapsed(offset: clean.length),
        composing: TextRange.empty,
      );
      _handleSubmitted(clean);
      return;
    }
    state.text = value;
    recordSentText(value);
    var e = VEvent()
      ..text = value
      ..start = _controller.selection.baseOffset;
    widget.sendModifyModify(state, e);
  }

  void _handleSubmitted(String value) {
    widget.sendSelectionDefaultSelection(state, null);
  }

  void _handleFocusChange() {
    if (_focusNode.hasFocus) {
      // Record the pre-edit text so a later stale echo of it is ignored rather than applied over
      // in-progress typing (no keystroke records this value, so the echo guard can't recognise it).
      seedTextEchoBaseline(_controller.text);
      widget.sendFocusFocusIn(state, null);
    } else {
      // Java re-flags vetoable per focus (DisplayBridge.setFocus).
      _vetoable = false;
      clearSentTextEchoes();
      widget.sendFocusFocusOut(state, null);
    }
  }

  void _updateCaretPosition() {
    if (!mounted) return;

    // Get the character index position of the cursor
    final cursorOffset = _controller.selection.baseOffset;
    if (cursorOffset < 0) return;

    // Only update if position actually changed
    if (state.caretPosition == null || state.caretPosition != cursorOffset) {
      state.caretPosition = cursorOffset;

      // Send the caret position to Java via Verify event
      var e = VEvent()
        ..start = cursorOffset
        ..end = cursorOffset
        ..text = "";
      widget.sendVerifyVerify(state, e);
    }
  }

  @override
  void dispose() {
    _pendingTimer?.cancel();
    EquoCommService.remove(_vetoableChannel, _vetoableToken);
    EquoCommService.remove(_verdictChannel, _verdictToken);
    _controller.removeListener(_updateCaretPosition);
    _controller.dispose();
    _focusNode.removeListener(_handleFocusChange);
    _focusNode.dispose();
    super.dispose();
  }
}