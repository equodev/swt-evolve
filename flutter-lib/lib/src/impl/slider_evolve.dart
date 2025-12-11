import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/slider.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../styles.dart';
import 'color_utils.dart';

class SliderImpl<T extends SliderSwt, V extends VSlider>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // Initialize values with defaults
    state.minimum ??= 0;
    state.maximum ??= 100;
    state.selection ??= 0;
    state.increment ??= 1;
    state.pageIncrement ??= 10;
    state.thumb ??= 10;

    // Ensure selection is within bounds
    final currentValue =
        state.selection!.clamp(state.minimum!, state.maximum!).toDouble();

    // Use bounds if available, otherwise use default size from Sizes.java
    final width = state.bounds?.width.toDouble() ?? 200.0;
    final height = state.bounds?.height.toDouble() ?? 40.0;
    final isVertical = state.style.has(SWT.VERTICAL);

    return wrap(
      _SimpleSlider(
        value: currentValue,
        min: state.minimum!.toDouble(),
        max: state.maximum!.toDouble(),
        enabled: state.enabled ?? true,
        width: width,
        height: height,
        isVertical: isVertical,
        onChanged: (value) {
          // Don't call setState here - wait for Java to respond
          state.selection = value.round();
          var e = VEvent()..index = state.selection;
          widget.sendSelectionSelection(state, e);
        },
        onChangeEnd: (value) {
          // Don't call setState here - wait for Java to respond
          state.selection = value.round();
          var e = VEvent()..index = state.selection;
          widget.sendSelectionDefaultSelection(state, e);
        },
      ),
    );
  }
}

class _SimpleSlider extends StatefulWidget {
  final double value;
  final double min;
  final double max;
  final bool enabled;
  final ValueChanged<double> onChanged;
  final ValueChanged<double> onChangeEnd;
  final double width;
  final double height;
  final bool isVertical;

  const _SimpleSlider({
    Key? key,
    required this.value,
    required this.min,
    required this.max,
    required this.enabled,
    required this.onChanged,
    required this.onChangeEnd,
    required this.width,
    required this.height,
    required this.isVertical,
  }) : super(key: key);

  @override
  _SimpleSliderState createState() => _SimpleSliderState();
}

class _SimpleSliderState extends State<_SimpleSlider> {
  double? _localValue; // Temporary value during drag for smooth UI

  @override
  Widget build(BuildContext context) {
    final accentColor = getAccentColor();
    final inactiveColor = getBorderColor();

    // Use local value during drag, otherwise use prop value
    final displayValue = _localValue ?? widget.value;

    final slider = SliderTheme(
      data: SliderThemeData(
        activeTrackColor: accentColor,
        inactiveTrackColor: inactiveColor,
        thumbColor: accentColor,
        overlayColor: Colors.transparent,
      ),
      child: Slider(
        value: displayValue.clamp(widget.min, widget.max),
        min: widget.min,
        max: widget.max,
        onChanged: widget.enabled
            ? (value) {
                // Update local value for smooth UI during drag
                setState(() {
                  _localValue = value;
                });
                widget.onChanged(value);
              }
            : null,
        onChangeEnd: widget.enabled
            ? (value) {
                // Clear local value and let Java's response be the source of truth
                setState(() {
                  _localValue = null;
                });
                widget.onChangeEnd(value);
              }
            : null,
      ),
    );

    if (widget.isVertical) {
      // For vertical sliders, rotate the horizontal slider 90 degrees counter-clockwise
      return Container(
        width: widget.width,
        height: widget.height,
        child: RotatedBox(
          quarterTurns:
              3, // 270 degrees counter-clockwise (same as 90 degrees clockwise)
          child: SizedBox(
            width: widget.height, // Swap width and height because of rotation
            height: widget.width,
            child: slider,
          ),
        ),
      );
    } else {
      // Horizontal slider
      return Container(
        width: widget.width,
        height: widget.height,
        child: slider,
      );
    }
  }
}
