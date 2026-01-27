import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../gen/event.dart';
import '../gen/spinner.dart';
import '../gen/swt.dart';
import '../styles.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import '../theme/theme_extensions/spinner_theme_extension.dart';
import '../theme/theme_settings/spinner_theme_settings.dart';
import 'utils/widget_utils.dart';

class SpinnerImpl<T extends SpinnerSwt, V extends VSpinner>
    extends CompositeImpl<T, V> {
  bool _isFocused = false;

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<SpinnerThemeExtension>()!;

    final minimum = state.minimum ?? widgetTheme.defaultMinimum;
    final maximum = state.maximum ?? widgetTheme.defaultMaximum;
    final selection = state.selection ?? widgetTheme.defaultSelection;
    final increment = state.increment ?? widgetTheme.defaultIncrement;
    final pageIncrement = state.pageIncrement ?? widgetTheme.defaultPageIncrement;
    final digits = state.digits ?? widgetTheme.defaultDigits;
    final textLimit = state.textLimit ?? widgetTheme.defaultTextLimit;

    final currentValue = selection!.clamp(minimum, maximum);
    final enabled = state.enabled ?? true;
    final readOnly = hasStyle(state.style, SWT.READ_ONLY);
    final size = getSpinnerSize(state, widgetTheme);

    final backgroundColor = getSpinnerBackgroundColor(
      state,
      widgetTheme,
      isEnabled: enabled,
    );

    final textColor = getSpinnerTextColor(
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
        pageIncrement: pageIncrement,
        textLimit: textLimit,
        enabled: enabled,
        readOnly: readOnly,
        width: size.width,
        height: size.height,
        widgetTheme: widgetTheme,
        backgroundColor: backgroundColor,
        textColor: textColor,
        borderColor: borderColor,
        textStyle: textStyle,
        onChanged: (value) {
          state.selection = value;
          widget.sendSelectionSelection(state, VEvent()..index = state.selection);
        },
        onSubmitted: (value) {
          state.selection = value;
          widget.sendSelectionDefaultSelection(state, VEvent()..index = state.selection);
        },
        onFocusChanged: (hasFocus) {
          setState(() => _isFocused = hasFocus);
          if (hasFocus) {
            widget.sendFocusFocusIn(state, null);
          } else {
            widget.sendFocusFocusOut(state, null);
          }
        },
        onHover: (isHovering) {
          if (isHovering) {
            widget.sendMouseTrackMouseEnter(state, null);
          } else {
            widget.sendMouseTrackMouseExit(state, null);
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
  final int pageIncrement;
  final int textLimit;
  final bool enabled;
  final bool readOnly;
  final double width;
  final double height;
  final SpinnerThemeExtension widgetTheme;
  final Color backgroundColor;
  final Color textColor;
  final Color borderColor;
  final TextStyle textStyle;
  final ValueChanged<int> onChanged;
  final ValueChanged<int> onSubmitted;
  final ValueChanged<bool> onFocusChanged;
  final ValueChanged<bool> onHover;

  const _ThemedSpinner({
    required this.value,
    required this.min,
    required this.max,
    required this.increment,
    required this.digits,
    required this.pageIncrement,
    required this.textLimit,
    required this.enabled,
    required this.readOnly,
    required this.width,
    required this.height,
    required this.widgetTheme,
    required this.backgroundColor,
    required this.textColor,
    required this.borderColor,
    required this.textStyle,
    required this.onChanged,
    required this.onSubmitted,
    required this.onFocusChanged,
    required this.onHover,
  });

  @override
  State<_ThemedSpinner> createState() => _ThemedSpinnerState();
}

class _ThemedSpinnerState extends State<_ThemedSpinner> {
  late TextEditingController _controller;
  late FocusNode _focusNode;
  int? _localValue;
  bool _isUpHovered = false;
  bool _isDownHovered = false;
  bool _isUpPressed = false;
  bool _isDownPressed = false;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: _formatValue(widget.value));
    _focusNode = FocusNode();
    _focusNode.addListener(_handleFocusChange);
  }

  @override
  void didUpdateWidget(_ThemedSpinner oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (_localValue == null && oldWidget.value != widget.value) {
      _controller.text = _formatValue(widget.value);
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    _focusNode.removeListener(_handleFocusChange);
    _focusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isInteractive = widget.enabled && !widget.readOnly;

    return MouseRegion(
      onEnter: (_) {
        widget.onHover(true);
      },
      onExit: (_) {
        widget.onHover(false);
      },
      child: AnimatedContainer(
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
            Expanded(child: _buildTextField(isInteractive)),
            _buildButtonColumn(isInteractive),
          ],
        ),
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
              ? RegExp(r'^-?\d*\.?\d*$')
              : RegExp(r'^-?\d*$'),
        ),
      ],
      onChanged: _handleTextChanged,
      onSubmitted: _handleSubmitted,
    );
  }

  Widget _buildButtonColumn(bool isInteractive) {
    return AnimatedContainer(
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
      onPressedChanged: (isPressed) => setState(() => _isDownPressed = isPressed),
    );
  }

  // Value formatting
  String _formatValue(int value) {
    if (widget.digits == 0) {
      return value.toString();
    }
    final displayValue = value / _getMultiplier();
    return displayValue.toStringAsFixed(widget.digits);
  }

  int _getMultiplier() {
    int multiplier = 1;
    for (int i = 0; i < widget.digits; i++) {
      multiplier *= 10;
    }
    return multiplier;
  }

  // Value manipulation
  void _incrementValue() {
    final newValue = (widget.value + widget.increment).clamp(widget.min, widget.max);
    _controller.text = _formatValue(newValue);
    widget.onChanged(newValue);
  }

  void _decrementValue() {
    final newValue = (widget.value - widget.increment).clamp(widget.min, widget.max);
    _controller.text = _formatValue(newValue);
    widget.onChanged(newValue);
  }

  // Event handlers
  void _handleTextChanged(String text) {
    if (text.isEmpty) return;

    final parsedValue = double.tryParse(text);
    if (parsedValue != null) {
      final scaledValue = (parsedValue * _getMultiplier()).round();
      final clampedValue = scaledValue.clamp(widget.min, widget.max);
      _localValue = clampedValue;
    }
  }

  void _handleSubmitted(String text) {
    try {
      final parsedValue = double.parse(text.isEmpty ? "0" : text);
      final scaledValue = (parsedValue * _getMultiplier()).round();
      final clampedValue = scaledValue.clamp(widget.min, widget.max);

      _localValue = null;
      _controller.text = _formatValue(clampedValue);
      widget.onSubmitted(clampedValue);
    } catch (e) {
      _localValue = null;
      _controller.text = _formatValue(widget.value);
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
        child: AnimatedContainer(
          duration: animationDuration,
          decoration: BoxDecoration(
            color: backgroundColor,
            border: showBottomBorder
                ? Border(bottom: BorderSide(color: borderColor, width: borderWidth * 0.5))
                : null,
          ),
          child: Center(
            child: Icon(
              icon,
              size: iconSize,
              color: iconColor,
            ),
          ),
        ),
      ),
    );
  }
}
