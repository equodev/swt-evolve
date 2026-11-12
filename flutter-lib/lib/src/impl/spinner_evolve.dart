import 'dart:async';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../comm/comm.dart';
import '../gen/event.dart';
import '../gen/spinner.dart';
import '../gen/swt.dart';
import '../styles.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import '../theme/theme_extensions/spinner_theme_extension.dart';
import '../theme/theme_settings/spinner_theme_settings.dart';
import 'utils/client_locale.dart';
import 'utils/key_edit_order.dart';
import 'utils/text_utils.dart';
import 'utils/veto_gate.dart';
import 'utils/widget_utils.dart';
import 'utils/quiet_animations.dart';

class SpinnerImpl<T extends SpinnerSwt, V extends VSpinner>
    extends CompositeImpl<T, V> {
  bool _isFocused = false;

  // A KeyDown listener may veto the keystroke, so an edit is held un-rendered until Java answers.
  final VetoGate _keyGate = VetoGate.key();

  // Java answers an edit it was asked to Verify; a veto sends the field back to the value Java holds.
  final VetoGate _modifyGate = VetoGate.modify();

  @override
  void initState() {
    super.initState();
    _keyGate.attach(state.wireType, state.id);
    _modifyGate.attach(state.wireType, state.id);
  }

  @override
  void dispose() {
    _keyGate.detach();
    _modifyGate.detach();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<SpinnerThemeExtension>()!;

    final minimum = state.minimum ?? widgetTheme.defaultMinimum;
    final maximum = state.maximum ?? widgetTheme.defaultMaximum;
    final selection = state.selection ?? widgetTheme.defaultSelection;
    final digits = state.digits ?? widgetTheme.defaultDigits;
    final textLimit = state.textLimit ?? widgetTheme.defaultTextLimit;

    final currentValue = selection!.clamp(minimum, maximum);
    final enabled = state.enabled ?? false;
    final readOnly = hasStyle(state.style, SWT.READ_ONLY);
    final size = getSpinnerSize(state, widgetTheme);

    final backgroundColor = getSpinnerBackgroundColor(
      context,
      state,
      widgetTheme,
      isEnabled: enabled,
    );

    final textColor = getSpinnerTextColor(
      context,
      state,
      widgetTheme,
      isEnabled: enabled,
    );

    final borderColor = getSpinnerBorderColor(
      state,
      widgetTheme,
      isEnabled: enabled,
      isFocused: _isFocused,
    );

    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: widgetTheme.textStyle,
    );

    return wrap(
      _ThemedSpinner(
        value: currentValue,
        min: minimum,
        max: maximum,
        digits: digits,
        decimalSeparator: decimalSeparator(),
        keptText: state.keptText,
        textLimit: textLimit,
        enabled: enabled,
        readOnly: readOnly,
        keyGate: _keyGate,
        modifyGate: _modifyGate,
        width: size.width,
        height: size.height,
        widgetTheme: widgetTheme,
        backgroundColor: backgroundColor,
        textColor: textColor,
        borderColor: borderColor,
        textStyle: textStyle,
        onArrow: (direction, press) => widget.sendSelectionSelection(
          state,
          VEvent()
            ..detail = direction
            ..button = press ? 1 : 0,
        ),
        onEdited: (edit) => widget.sendModifyModify(state, edit),
        onSubmitted: (value) {
          state.selection = value;
          widget.sendSelectionDefaultSelection(
            state,
            VEvent()..index = state.selection,
          );
        },
        onFocusChanged: (hasFocus) {
          setState(() => _isFocused = hasFocus);
          if (hasFocus) {
            widget.sendFocusFocusIn(state, null);
          } else {
            _keyGate.armed = false;
            _modifyGate.armed = false;
            widget.sendFocusFocusOut(state, null);
          }
        },
      ),
    );
  }
}

class _ThemedSpinner extends StatefulWidget {
  final int value;
  final int min;
  final int max;
  final int digits;
  final String decimalSeparator;
  // The text a refused Verify kept over the selection; null while the text follows the value.
  final String? keptText;
  final int textLimit;
  final bool enabled;
  final bool readOnly;

  final VetoGate keyGate;
  final VetoGate modifyGate;
  final double width;
  final double height;
  final SpinnerThemeExtension widgetTheme;
  final Color backgroundColor;
  final Color textColor;
  final Color borderColor;
  final TextStyle textStyle;
  // A button's step, as SWT.ARROW_UP or SWT.ARROW_DOWN, and whether its press made it rather than a repeat while
  // it is held: Java steps the value and sends it back.
  final void Function(int direction, bool press) onArrow;
  final ValueChanged<VEvent> onEdited;
  final ValueChanged<int> onSubmitted;
  final ValueChanged<bool> onFocusChanged;

  const _ThemedSpinner({
    required this.value,
    required this.min,
    required this.max,
    required this.digits,
    required this.decimalSeparator,
    this.keptText,
    required this.textLimit,
    required this.enabled,
    required this.readOnly,
    required this.keyGate,
    required this.modifyGate,
    required this.width,
    required this.height,
    required this.widgetTheme,
    required this.backgroundColor,
    required this.textColor,
    required this.borderColor,
    required this.textStyle,
    required this.onArrow,
    required this.onEdited,
    required this.onSubmitted,
    required this.onFocusChanged,
  });

  @override
  State<_ThemedSpinner> createState() => _ThemedSpinnerState();
}

class _ThemedSpinnerState extends State<_ThemedSpinner> {
  late TextEditingController _controller;
  late FocusNode _focusNode;
  int? _localValue;
  // The text the field showed before the edit that is being reported: what the range of that edit is relative to.
  String _editedFrom = '';
  // An edit that leaves no number, offered to Java and held off the field until Java lets it through.
  TextEditingValue? _offered;
  bool _isUpHovered = false;
  bool _isDownHovered = false;
  bool _isUpPressed = false;
  bool _isDownPressed = false;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: _heldText);
    _focusNode = FocusNode();
    _focusNode.addListener(_handleFocusChange);
    KeyEditOrder.instance.registerEditor(_focusNode, () => widget.enabled && !widget.readOnly);
    widget.modifyGate.onUnmatched = _revertVetoedEdit;
  }

  @override
  void didUpdateWidget(_ThemedSpinner oldWidget) {
    super.didUpdateWidget(oldWidget);
    // A disabled button lets go of its press without telling us (see _SpinnerButtonState).
    if (!widget.enabled) {
      _isUpPressed = false;
      _isDownPressed = false;
    }
    final shown = oldWidget.value != widget.value ||
        oldWidget.digits != widget.digits ||
        oldWidget.decimalSeparator != widget.decimalSeparator ||
        oldWidget.keptText != widget.keptText;
    if (_localValue == null && shown) {
      _controller.text = _heldText;
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    _focusNode.removeListener(_handleFocusChange);
    KeyEditOrder.instance.unregisterEditor(_focusNode);
    _focusNode.dispose();
    super.dispose();
  }

  TextEditingValue _gateEdit(TextEditingValue oldValue, TextEditingValue newValue) {
    if (newValue.text != oldValue.text) _editedFrom = oldValue.text;
    if (!widget.keyGate.armed) return newValue;
    if (newValue.text == oldValue.text) return newValue;
    widget.keyGate.propose((doit) {
      if (!doit || !mounted) return;
      _controller.value = newValue;
      _handleTextChanged(newValue.text, oldValue.text);
    });
    return oldValue;
  }

  // A native spinner refuses an edit that leaves no number, keeping its text, unless a Verify listener lets it
  // through. Java makes that call: such an edit goes up as typed and is held off the field until Java answers yes.
  // The pattern only decides which edits are worth showing before Java answers.
  TextEditingValue _holdNonNumeric(TextEditingValue oldValue, TextEditingValue newValue) {
    if (newValue.text == oldValue.text || _numeric.hasMatch(newValue.text)) {
      _offered = null;
      return newValue;
    }
    _offered = newValue;
    _reportEdit(oldValue.text, newValue.text);
    return oldValue;
  }

  RegExp? _numericPattern;
  String? _numericFor;

  RegExp get _numeric {
    final key = widget.digits > 0 ? widget.decimalSeparator : '';
    if (_numericPattern == null || _numericFor != key) {
      _numericFor = key;
      _numericPattern = key.isEmpty
          ? RegExp(r'^-?\d*$')
          : RegExp('^-?\\d*${RegExp.escape(key)}?\\d*\$');
    }
    return _numericPattern!;
  }

  // Java vetoed the edit, or changed what was typed: the field shows the value Java holds.
  void _revertVetoedEdit(bool doit) {
    final offered = _offered;
    _offered = null;
    if (!mounted) return;
    if (doit) {
      if (offered != null) _controller.value = offered;
      return;
    }
    _localValue = null;
    final held = _heldText;
    _controller.value = TextEditingValue(
      text: held,
      selection: TextSelection.collapsed(offset: held.length),
    );
  }

  @override
  Widget build(BuildContext context) {
    // A READ_ONLY spinner takes no typing, but its buttons still step it, as Win32's and Cocoa's do.
    final isInteractive = widget.enabled;

    return QuietAnimatedContainer(
      duration: widget.widgetTheme.animationDuration,
      width: widget.width,
      height: widget.height,
      decoration: BoxDecoration(
        color: widget.backgroundColor,
        border: Border.all(
          color: widget.borderColor,
          width: widget.widgetTheme.borderWidth,
        ),
        borderRadius: BorderRadius.circular(widget.widgetTheme.borderRadius),
      ),
      child: Row(
        children: [
          Expanded(
            child: DoubleClickWordSelector(
              controller: _controller,
              focusNode: _focusNode,
              onWordSelected: (_, __) {},
              child: _buildTextField(isInteractive),
            ),
          ),
          _buildButtonColumn(isInteractive),
        ],
      ),
    );
  }

  Widget _buildTextField(bool isInteractive) {
    return TextField(
      autofillHints: null,
      controller: _controller,
      focusNode: _focusNode,
      enabled: widget.enabled,
      readOnly: widget.readOnly,
      textAlign: TextAlign.right,
      style: widget.textStyle,
      decoration: InputDecoration(
        border: InputBorder.none,
        contentPadding: widget.widgetTheme.textFieldPadding,
        isDense: true,
      ),
      keyboardType: TextInputType.numberWithOptions(
        decimal: widget.digits > 0,
        signed: widget.min < 0,
      ),
      inputFormatters: [
        TextInputFormatter.withFunction(_holdNonNumeric),
        TextInputFormatter.withFunction(_gateEdit),
      ],
      onChanged: (text) => _handleTextChanged(text, _editedFrom),
      onSubmitted: _handleSubmitted,
      // No Flutter toolbar on a right-click: SWT's edit field opens the platform's own menu, which an
      // application Menu replaces and MenuDetect can cancel, and Flutter's toolbar follows neither.
      contextMenuBuilder: null,
    );
  }

  Widget _buildButtonColumn(bool isInteractive) {
    return QuietAnimatedContainer(
      duration: widget.widgetTheme.animationDuration,
      width: widget.widgetTheme.buttonWidth,
      decoration: BoxDecoration(
        border: Border(
          left: BorderSide(
            color: widget.borderColor,
            width: widget.widgetTheme.borderWidth,
          ),
        ),
      ),
      child: Column(
        children: [
          Expanded(child: _buildUpButton(isInteractive)),
          Expanded(child: _buildDownButton(isInteractive)),
        ],
      ),
    );
  }

  Widget _buildUpButton(bool isInteractive) {
    final iconColor = getSpinnerIconColor(
      widget.widgetTheme,
      isEnabled: isInteractive,
      isHovered: _isUpHovered,
    );

    return _SpinnerButton(
      icon: Icons.arrow_drop_up,
      iconSize: widget.widgetTheme.iconSize,
      iconColor: iconColor,
      backgroundColor: _isUpPressed
          ? widget.widgetTheme.buttonPressedColor
          : (_isUpHovered
                ? widget.widgetTheme.buttonHoverColor
                : widget.widgetTheme.buttonBackgroundColor),
      borderColor: widget.borderColor,
      borderWidth: widget.widgetTheme.borderWidth,
      animationDuration: widget.widgetTheme.animationDuration,
      enabled: isInteractive,
      showBottomBorder: true,
      onStep: isInteractive ? (press) => widget.onArrow(SWT.ARROW_UP, press) : null,
      onHoverChanged: (isHovered) => setState(() => _isUpHovered = isHovered),
      onPressedChanged: (isPressed) => setState(() => _isUpPressed = isPressed),
    );
  }

  Widget _buildDownButton(bool isInteractive) {
    final iconColor = getSpinnerIconColor(
      widget.widgetTheme,
      isEnabled: isInteractive,
      isHovered: _isDownHovered,
    );

    return _SpinnerButton(
      icon: Icons.arrow_drop_down,
      iconSize: widget.widgetTheme.iconSize,
      iconColor: iconColor,
      backgroundColor: _isDownPressed
          ? widget.widgetTheme.buttonPressedColor
          : (_isDownHovered
                ? widget.widgetTheme.buttonHoverColor
                : widget.widgetTheme.buttonBackgroundColor),
      borderColor: widget.borderColor,
      borderWidth: widget.widgetTheme.borderWidth,
      animationDuration: widget.widgetTheme.animationDuration,
      enabled: isInteractive,
      showBottomBorder: false,
      onStep: isInteractive ? (press) => widget.onArrow(SWT.ARROW_DOWN, press) : null,
      onHoverChanged: (isHovered) => setState(() => _isDownHovered = isHovered),
      onPressedChanged: (isPressed) =>
          setState(() => _isDownPressed = isPressed),
    );
  }

  // Value formatting
  String get _heldText => widget.keptText ?? _formatValue(widget.value);

  String _formatValue(int value) {
    if (widget.digits == 0) {
      return value.toString();
    }
    final displayValue = value / _getMultiplier();
    return displayValue
        .toStringAsFixed(widget.digits)
        .replaceFirst('.', widget.decimalSeparator);
  }

  // The number a typed text stands for, or null when it is not one.
  double? _parseNumber(String text) =>
      double.tryParse(text.replaceFirst(widget.decimalSeparator, '.'));

  int _getMultiplier() {
    int multiplier = 1;
    for (int i = 0; i < widget.digits; i++) {
      multiplier *= 10;
    }
    return multiplier;
  }

  // Event handlers

  // What a user edit did to the text: the range of the previous text it replaced and how many characters it put
  // there. Java asks its Verify listeners about that range and reads the value out of the new text.
  void _reportEdit(String from, String to) {
    final shorter = from.length < to.length ? from.length : to.length;
    var start = 0;
    while (start < shorter && from.codeUnitAt(start) == to.codeUnitAt(start)) {
      start++;
    }
    var tail = 0;
    while (tail < shorter - start &&
        from.codeUnitAt(from.length - 1 - tail) ==
            to.codeUnitAt(to.length - 1 - tail)) {
      tail++;
    }
    widget.onEdited(
      VEvent()
        ..text = to
        ..start = start
        ..end = from.length - tail
        ..count = to.length - tail - start,
    );
    KeyEditOrder.instance.editApplied();
  }

  void _handleTextChanged(String text, String from) {
    _reportEdit(from, text);
    if (text.isEmpty) return;

    final parsedValue = _parseNumber(text);
    if (parsedValue != null) {
      final scaledValue = (parsedValue * _getMultiplier()).round();
      final clampedValue = scaledValue.clamp(widget.min, widget.max);
      _localValue = clampedValue;
    }
  }

  void _handleSubmitted(String text) {
    try {
      final parsedValue = _parseNumber(text.isEmpty ? '0' : text);
      if (parsedValue == null) throw const FormatException();
      final scaledValue = (parsedValue * _getMultiplier()).round();
      final clampedValue = scaledValue.clamp(widget.min, widget.max);

      _localValue = null;
      _controller.text = _formatValue(clampedValue);
      widget.onSubmitted(clampedValue);
    } catch (e) {
      _localValue = null;
      _controller.text = _heldText;
    }
  }

  void _handleFocusChange() {
    widget.onFocusChanged(_focusNode.hasFocus);
  }
}

// Steps once as the primary button goes down, as a native spinner's button does, then again after a delay and
// at a steady rate while it is held, until it comes up.
class _SpinnerButton extends StatefulWidget {
  static const repeatDelay = Duration(milliseconds: 500);
  static const repeatInterval = Duration(milliseconds: 50);

  final IconData icon;
  final double iconSize;
  final Color iconColor;
  final Color backgroundColor;
  final Color borderColor;
  final double borderWidth;
  final Duration animationDuration;
  final bool enabled;
  final bool showBottomBorder;
  // A step, with whether the press made it rather than a repeat while the button is held.
  final ValueChanged<bool>? onStep;
  final ValueChanged<bool> onHoverChanged;
  final ValueChanged<bool> onPressedChanged;

  const _SpinnerButton({
    required this.icon,
    required this.iconSize,
    required this.iconColor,
    required this.backgroundColor,
    required this.borderColor,
    required this.borderWidth,
    required this.animationDuration,
    required this.enabled,
    required this.showBottomBorder,
    this.onStep,
    required this.onHoverChanged,
    required this.onPressedChanged,
  });

  @override
  State<_SpinnerButton> createState() => _SpinnerButtonState();
}

class _SpinnerButtonState extends State<_SpinnerButton> {
  Timer? _repeat;
  int? _pointer;
  // Whether the held pointer is over the button: the repeat steps only then, as Win32's and GTK's do.
  bool _inside = false;
  // Whether a pointer press stepped since the last press by role, which the same click may also send.
  bool _steppedByPointer = false;

  @override
  void didUpdateWidget(_SpinnerButton oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Disabled while held: the repeat stops. The parent is mid-update, so it shows the button released
    // itself rather than hearing it from here.
    if (!widget.enabled || widget.onStep == null) {
      _repeat?.cancel();
      _repeat = null;
      _pointer = null;
    }
  }

  @override
  void dispose() {
    _repeat?.cancel();
    super.dispose();
  }

  void _press(PointerDownEvent event) {
    if (!widget.enabled || widget.onStep == null || _pointer != null) return;
    if (event.buttons != kPrimaryButton) return;
    _pointer = event.pointer;
    _inside = true;
    _steppedByPointer = true;
    widget.onPressedChanged(true);
    widget.onStep!(true);
    _repeat = Timer(_SpinnerButton.repeatDelay, () {
      _repeat = Timer.periodic(_SpinnerButton.repeatInterval, (_) {
        if (_inside) widget.onStep?.call(false);
      });
    });
  }

  void _move(PointerMoveEvent event) {
    if (event.pointer != _pointer) return;
    final size = context.size;
    _inside = size != null && (Offset.zero & size).contains(event.localPosition);
  }

  void _up(PointerEvent event) {
    if (event.pointer == _pointer) _release();
  }

  void _release() {
    _repeat?.cancel();
    _repeat = null;
    if (_pointer == null) return;
    _pointer = null;
    widget.onPressedChanged(false);
  }

  // A press by role (an accessibility tool, the E2E suite) steps once. No MouseDown follows it, so it is no
  // press of the pointer's. A click the pointer already stepped for is not stepped again.
  void _semanticTap() {
    if (_steppedByPointer) {
      _steppedByPointer = false;
      return;
    }
    widget.onStep?.call(false);
  }

  @override
  Widget build(BuildContext context) {
    final steps = widget.enabled && widget.onStep != null;
    return MouseRegion(
      onEnter: (_) => widget.onHoverChanged(true),
      onExit: (_) => widget.onHoverChanged(false),
      child: Semantics(
        button: true,
        enabled: steps,
        onTap: steps ? _semanticTap : null,
        child: Listener(
          onPointerDown: _press,
          onPointerMove: _move,
          onPointerUp: _up,
          onPointerCancel: _up,
          child: QuietAnimatedContainer(
            duration: widget.animationDuration,
            decoration: BoxDecoration(
              color: widget.backgroundColor,
              border: widget.showBottomBorder
                  ? Border(
                      bottom: BorderSide(
                        color: widget.borderColor,
                        width: widget.borderWidth * 0.5,
                      ),
                    )
                  : null,
            ),
            child: Center(
              child: Icon(widget.icon, size: widget.iconSize, color: widget.iconColor),
            ),
          ),
        ),
      ),
    );
  }
}
