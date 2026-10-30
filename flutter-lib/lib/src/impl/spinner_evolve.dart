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
    final increment = state.increment ?? widgetTheme.defaultIncrement;
    final pageIncrement =
        state.pageIncrement ?? widgetTheme.defaultPageIncrement;
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
        increment: increment,
        digits: digits,
        decimalSeparator: decimalSeparator(),
        keptText: state.keptText,
        pageIncrement: pageIncrement,
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
        onChanged: (value) {
          state.selection = value;
          widget.sendSelectionSelection(
            state,
            VEvent()..index = state.selection,
          );
        },
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
  final int increment;
  final int digits;
  final String decimalSeparator;
  // The text a refused Verify kept over the selection; null while the text follows the value.
  final String? keptText;
  final int pageIncrement;
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
  final ValueChanged<int> onChanged;
  final ValueChanged<VEvent> onEdited;
  final ValueChanged<int> onSubmitted;
  final ValueChanged<bool> onFocusChanged;

  const _ThemedSpinner({
    required this.value,
    required this.min,
    required this.max,
    required this.increment,
    required this.digits,
    required this.decimalSeparator,
    this.keptText,
    required this.pageIncrement,
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
    required this.onChanged,
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
    widget.modifyGate.onUnmatched = _revertVetoedEdit;
  }

  @override
  void didUpdateWidget(_ThemedSpinner oldWidget) {
    super.didUpdateWidget(oldWidget);
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

  // Java vetoed the edit, or changed what was typed: the field shows the value Java holds.
  void _revertVetoedEdit(bool doit) {
    if (doit || !mounted) return;
    _localValue = null;
    final held = _heldText;
    _controller.value = TextEditingValue(
      text: held,
      selection: TextSelection.collapsed(offset: held.length),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isInteractive = widget.enabled && !widget.readOnly;

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
      controller: _controller,
      focusNode: _focusNode,
      enabled: widget.enabled && !widget.readOnly,
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
        FilteringTextInputFormatter.allow(
          widget.digits > 0
              ? RegExp('^-?\\d*${RegExp.escape(widget.decimalSeparator)}?\\d*\$')
              : RegExp(r'^-?\d*$'),
        ),
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
      onTap: isInteractive ? _incrementValue : null,
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
      onTap: isInteractive ? _decrementValue : null,
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

  // Value manipulation
  void _incrementValue() {
    final newValue = (widget.value + widget.increment).clamp(
      widget.min,
      widget.max,
    );
    _controller.text = _formatValue(newValue);
    widget.onChanged(newValue);
  }

  void _decrementValue() {
    final newValue = (widget.value - widget.increment).clamp(
      widget.min,
      widget.max,
    );
    _controller.text = _formatValue(newValue);
    widget.onChanged(newValue);
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

class _SpinnerButton extends StatelessWidget {
  final IconData icon;
  final double iconSize;
  final Color iconColor;
  final Color backgroundColor;
  final Color borderColor;
  final double borderWidth;
  final Duration animationDuration;
  final bool enabled;
  final bool showBottomBorder;
  final VoidCallback? onTap;
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
    this.onTap,
    required this.onHoverChanged,
    required this.onPressedChanged,
  });

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => onHoverChanged(true),
      onExit: (_) => onHoverChanged(false),
      child: GestureDetector(
        onTapDown: enabled ? (_) => onPressedChanged(true) : null,
        onTapUp: enabled ? (_) => onPressedChanged(false) : null,
        onTapCancel: enabled ? () => onPressedChanged(false) : null,
        onTap: onTap,
        child: QuietAnimatedContainer(
          duration: animationDuration,
          decoration: BoxDecoration(
            color: backgroundColor,
            border: showBottomBorder
                ? Border(
                    bottom: BorderSide(
                      color: borderColor,
                      width: borderWidth * 0.5,
                    ),
                  )
                : null,
          ),
          child: Center(
            child: Icon(icon, size: iconSize, color: iconColor),
          ),
        ),
      ),
    );
  }
}
