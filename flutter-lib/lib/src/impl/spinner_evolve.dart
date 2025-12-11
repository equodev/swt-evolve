import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../gen/event.dart';
import '../gen/spinner.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import '../styles.dart';
import 'color_utils.dart';

class SpinnerImpl<T extends SpinnerSwt, V extends VSpinner>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    state.minimum ??= 0;
    state.maximum ??= 100;
    state.selection ??= 0;
    state.increment ??= 1;
    state.pageIncrement ??= 10;
    state.digits ??= 0;
    state.textLimit ??= 10;

    final currentValue = state.selection!.clamp(state.minimum!, state.maximum!);

    final width = state.bounds?.width.toDouble() ?? 120.0;
    final height = state.bounds?.height.toDouble() ?? 32.0;

    return wrap(
      _SimpleSpinner(
        value: currentValue,
        min: state.minimum!,
        max: state.maximum!,
        increment: state.increment!,
        digits: state.digits!,
        enabled: state.enabled ?? true,
        readOnly: state.style.has(SWT.READ_ONLY),
        width: width,
        height: height,
        onChanged: (value) {
          state.selection = value;
          var e = VEvent()..index = state.selection;
          widget.sendSelectionSelection(state, e);
        },
        onSubmitted: (value) {
          state.selection = value;
          var e = VEvent()..index = state.selection;
          widget.sendSelectionDefaultSelection(state, e);
        },
      ),
    );
  }
}

class _SimpleSpinner extends StatefulWidget {
  final int value;
  final int min;
  final int max;
  final int increment;
  final int digits;
  final bool enabled;
  final bool readOnly;
  final ValueChanged<int> onChanged;
  final ValueChanged<int> onSubmitted;
  final double width;
  final double height;

  const _SimpleSpinner({
    Key? key,
    required this.value,
    required this.min,
    required this.max,
    required this.increment,
    required this.digits,
    required this.enabled,
    required this.readOnly,
    required this.onChanged,
    required this.onSubmitted,
    required this.width,
    required this.height,
  }) : super(key: key);

  @override
  _SimpleSpinnerState createState() => _SimpleSpinnerState();
}

class _SimpleSpinnerState extends State<_SimpleSpinner> {
  late TextEditingController _controller;
  int? _localValue;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: _formatValue(widget.value));
  }

  @override
  void didUpdateWidget(_SimpleSpinner oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (_localValue == null && oldWidget.value != widget.value) {
      _controller.text = _formatValue(widget.value);
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  String _formatValue(int value) {
    if (widget.digits == 0) {
      return value.toString();
    } else {
      double displayValue = value / _getMultiplier();
      return displayValue.toStringAsFixed(widget.digits);
    }
  }

  int _getMultiplier() {
    int multiplier = 1;
    for (int i = 0; i < widget.digits; i++) {
      multiplier *= 10;
    }
    return multiplier;
  }

  void _incrementValue() {
    if (!widget.enabled || widget.readOnly) return;
    int newValue =
        (widget.value + widget.increment).clamp(widget.min, widget.max);
    _controller.text = _formatValue(newValue);
    widget.onChanged(newValue);
  }

  void _decrementValue() {
    if (!widget.enabled || widget.readOnly) return;
    int newValue =
        (widget.value - widget.increment).clamp(widget.min, widget.max);
    _controller.text = _formatValue(newValue);
    widget.onChanged(newValue);
  }

  void _handleTextChanged(String text) {
    if (text.isEmpty) return;

    final parsedValue = double.tryParse(text);
    if (parsedValue != null) {
      int scaledValue = (parsedValue * _getMultiplier()).round();
      int clampedValue = scaledValue.clamp(widget.min, widget.max);
      _localValue = clampedValue;
    }
  }

  void _handleSubmitted(String text) {
    try {
      double parsedValue = double.parse(text.isEmpty ? "0" : text);
      int scaledValue = (parsedValue * _getMultiplier()).round();
      int clampedValue = scaledValue.clamp(widget.min, widget.max);

      _localValue = null;
      _controller.text = _formatValue(clampedValue);
      widget.onSubmitted(clampedValue);
    } catch (e) {
      _localValue = null;
      _controller.text = _formatValue(widget.value);
    }
  }

  @override
  Widget build(BuildContext context) {
    final accentColor = getAccentColor();
    final backgroundColor = getBackground();
    final foregroundColor = getForeground();
    final borderColor = getBorderColor();

    return Container(
      width: widget.width,
      height: widget.height,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border.all(color: borderColor, width: 1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _controller,
              enabled: widget.enabled && !widget.readOnly,
              readOnly: widget.readOnly,
              textAlign: TextAlign.right,
              style: TextStyle(
                color:
                    widget.enabled ? foregroundColor : getForegroundDisabled(),
                fontSize: 14,
              ),
              decoration: InputDecoration(
                border: InputBorder.none,
                contentPadding:
                    EdgeInsets.symmetric(horizontal: 8, vertical: 8),
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
            ),
          ),
          Container(
            width: 20,
            decoration: BoxDecoration(
              border: Border(left: BorderSide(color: borderColor, width: 1)),
            ),
            child: Column(
              children: [
                Expanded(
                  child: GestureDetector(
                    onTap: (widget.enabled && !widget.readOnly)
                        ? _incrementValue
                        : null,
                    child: Container(
                      decoration: BoxDecoration(
                        color: backgroundColor,
                        border: Border(
                            bottom: BorderSide(color: borderColor, width: 0.5)),
                      ),
                      child: Icon(
                        Icons.arrow_drop_up,
                        size: 16,
                        color: (widget.enabled && !widget.readOnly)
                            ? accentColor
                            : getForegroundDisabled(),
                      ),
                    ),
                  ),
                ),
                Expanded(
                  child: GestureDetector(
                    onTap: (widget.enabled && !widget.readOnly)
                        ? _decrementValue
                        : null,
                    child: Container(
                      color: backgroundColor,
                      child: Icon(
                        Icons.arrow_drop_down,
                        size: 16,
                        color: (widget.enabled && !widget.readOnly)
                            ? accentColor
                            : getForegroundDisabled(),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
