import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/scale.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../styles.dart';
import 'color_utils.dart';

class ScaleImpl<T extends ScaleSwt, V extends VScale>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    state.minimum ??= 0;
    state.maximum ??= 100;
    state.selection ??= 0;
    state.increment ??= 1;
    state.pageIncrement ??= 10;

    final currentValue = state.selection!
        .clamp(state.minimum!, state.maximum!)
        .toDouble();

    final width = state.bounds?.width.toDouble() ?? 200.0;
    final height = state.bounds?.height.toDouble() ?? 40.0;
    final isVertical = state.style.has(SWT.VERTICAL);

    return wrap(
      _SimpleScale(
        value: currentValue,
        min: state.minimum!.toDouble(),
        max: state.maximum!.toDouble(),
        enabled: state.enabled ?? true,
        width: width,
        height: height,
        isVertical: isVertical,
        onChanged: (value) {
          state.selection = value.round();
          var e = VEvent()..index = state.selection;
          widget.sendSelectionSelection(state, e);
        },
        onChangeEnd: (value) {
          state.selection = value.round();
          var e = VEvent()..index = state.selection;
          widget.sendSelectionDefaultSelection(state, e);
        },
      ),
    );
  }
}

class _SimpleScale extends StatefulWidget {
  final double value;
  final double min;
  final double max;
  final bool enabled;
  final ValueChanged<double> onChanged;
  final ValueChanged<double> onChangeEnd;
  final double width;
  final double height;
  final bool isVertical;

  const _SimpleScale({
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
  _SimpleScaleState createState() => _SimpleScaleState();
}

class _SimpleScaleState extends State<_SimpleScale> {
  double? _localValue;

  @override
  Widget build(BuildContext context) {
    final accentColor = getAccentColor();
    final inactiveColor = getBorderColor();
    final foregroundColor = getForeground();

    final displayValue = _localValue ?? widget.value;

    final tickInterval = (widget.max - widget.min) / 10;
    final ticks = List<double>.generate(
      11,
      (index) => widget.min + (index * tickInterval)
    );

    final scale = Stack(
      children: [
        Positioned.fill(
          child: CustomPaint(
            painter: _TickMarkPainter(
              ticks: ticks,
              min: widget.min,
              max: widget.max,
              color: inactiveColor,
              isVertical: widget.isVertical,
            ),
          ),
        ),
        SliderTheme(
          data: SliderThemeData(
            activeTrackColor: accentColor,
            inactiveTrackColor: inactiveColor,
            thumbColor: accentColor,
            overlayColor: Colors.transparent,
            trackHeight: 2.0,
            thumbShape: RoundSliderThumbShape(enabledThumbRadius: 4.0),
            tickMarkShape: SliderTickMarkShape.noTickMark,
          ),
          child: Slider(
            value: displayValue.clamp(widget.min, widget.max),
            min: widget.min,
            max: widget.max,
            onChanged: widget.enabled ? (value) {
              setState(() {
                _localValue = value;
              });
              widget.onChanged(value);
            } : null,
            onChangeEnd: widget.enabled ? (value) {
              setState(() {
                _localValue = null;
              });
              widget.onChangeEnd(value);
            } : null,
          ),
        ),
      ],
    );

    if (widget.isVertical) {
      return Container(
        width: widget.width,
        height: widget.height,
        child: RotatedBox(
          quarterTurns: 3,
          child: SizedBox(
            width: widget.height,
            height: widget.width,
            child: scale,
          ),
        ),
      );
    } else {
      return Container(
        width: widget.width,
        height: widget.height,
        child: scale,
      );
    }
  }
}

class _TickMarkPainter extends CustomPainter {
  final List<double> ticks;
  final double min;
  final double max;
  final Color color;
  final bool isVertical;

  _TickMarkPainter({
    required this.ticks,
    required this.min,
    required this.max,
    required this.color,
    required this.isVertical,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 1.0
      ..style = PaintingStyle.stroke;

    final horizontalPadding = 24.0; // Flutter's slider has padding
    final verticalCenter = size.height / 2;

    for (var tickValue in ticks) {
      final position = (tickValue - min) / (max - min);

      final x = horizontalPadding + (position * (size.width - 2 * horizontalPadding));

      canvas.drawLine(
        Offset(x, verticalCenter - 8),
        Offset(x, verticalCenter + 8),
        paint,
      );
    }
  }

  @override
  bool shouldRepaint(_TickMarkPainter oldDelegate) {
    return oldDelegate.ticks != ticks ||
        oldDelegate.min != min ||
        oldDelegate.max != max ||
        oldDelegate.color != color;
  }
}
