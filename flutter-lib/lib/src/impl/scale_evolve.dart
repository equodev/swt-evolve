import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/scale.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../theme/theme_extensions/scale_theme_extension.dart';
import '../theme/theme_settings/scale_theme_settings.dart';
import 'utils/widget_utils.dart';

class ScaleImpl<T extends ScaleSwt, V extends VScale>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ScaleThemeExtension>()!;

    final minimum = state.minimum ?? widgetTheme.defaultMinimum;
    final maximum = state.maximum ?? widgetTheme.defaultMaximum;
    final selection = state.selection ?? widgetTheme.defaultSelection;

    final currentValue = selection.clamp(minimum, maximum).toDouble();
    final enabled = state.enabled ?? true;
    final isVertical = hasStyle(state.style, SWT.VERTICAL);
    final size = getScaleSize(state, widgetTheme, isVertical: isVertical);

    return wrap(
      _ThemedScale(
        value: currentValue,
        min: minimum.toDouble(),
        max: maximum.toDouble(),
        enabled: enabled,
        width: size.width,
        height: size.height,
        isVertical: isVertical,
        widgetTheme: widgetTheme,
        onChanged: (value) {
          state.selection = value.round();
          widget.sendSelectionSelection(state, VEvent()..index = state.selection);
        },
        onChangeEnd: (value) {
          state.selection = value.round();
          widget.sendSelectionDefaultSelection(state, VEvent()..index = state.selection);
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

class _ThemedScale extends StatefulWidget {
  final double value;
  final double min;
  final double max;
  final bool enabled;
  final double width;
  final double height;
  final bool isVertical;
  final ScaleThemeExtension widgetTheme;
  final ValueChanged<double> onChanged;
  final ValueChanged<double> onChangeEnd;
  final ValueChanged<bool> onHover;

  const _ThemedScale({
    required this.value,
    required this.min,
    required this.max,
    required this.enabled,
    required this.width,
    required this.height,
    required this.isVertical,
    required this.widgetTheme,
    required this.onChanged,
    required this.onChangeEnd,
    required this.onHover,
  });

  @override
  State<_ThemedScale> createState() => _ThemedScaleState();
}

class _ThemedScaleState extends State<_ThemedScale> {
  double? _localValue;
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final displayValue = _localValue ?? widget.value;
    final ticks = generateTickPositions(
      widget.min,
      widget.max,
      widget.widgetTheme.tickMarkCount,
    );

    final tickMarkColor = getScaleTickMarkColor(
      widget.widgetTheme,
      isEnabled: widget.enabled,
    );

    final scale = MouseRegion(
      onEnter: (_) {
        setState(() => _isHovered = true);
        widget.onHover(true);
      },
      onExit: (_) {
        setState(() => _isHovered = false);
        widget.onHover(false);
      },
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: _TickMarkPainter(
                ticks: ticks,
                min: widget.min,
                max: widget.max,
                color: tickMarkColor,
                tickHeight: widget.widgetTheme.tickMarkHeight,
                tickWidth: widget.widgetTheme.tickMarkWidth,
                horizontalPadding: widget.widgetTheme.horizontalPadding,
              ),
            ),
          ),
          _buildSlider(displayValue),
        ],
      ),
    );

    if (widget.isVertical) {
      return _buildVerticalScale(scale);
    }
    return _buildHorizontalScale(scale);
  }

  Widget _buildSlider(double displayValue) {
    final activeTrackColor = getScaleActiveTrackColor(
      widget.widgetTheme,
      isEnabled: widget.enabled,
    );
    final inactiveTrackColor = getScaleInactiveTrackColor(
      widget.widgetTheme,
      isEnabled: widget.enabled,
    );
    final thumbColor = getScaleThumbColor(
      widget.widgetTheme,
      isEnabled: widget.enabled,
      isHovered: _isHovered,
    );

    return SliderTheme(
      data: SliderThemeData(
        activeTrackColor: activeTrackColor,
        inactiveTrackColor: inactiveTrackColor,
        thumbColor: thumbColor,
        overlayColor: widget.widgetTheme.overlayColor,
        trackHeight: widget.widgetTheme.trackHeight,
        thumbShape: RoundSliderThumbShape(
          enabledThumbRadius: _isHovered
              ? widget.widgetTheme.thumbHoverRadius
              : widget.widgetTheme.thumbRadius,
        ),
        tickMarkShape: SliderTickMarkShape.noTickMark,
      ),
      child: Slider(
        value: displayValue.clamp(widget.min, widget.max),
        min: widget.min,
        max: widget.max,
        onChanged: widget.enabled ? _handleChanged : null,
        onChangeEnd: widget.enabled ? _handleChangeEnd : null,
      ),
    );
  }

  Widget _buildHorizontalScale(Widget scale) {
    return AnimatedContainer(
      duration: widget.widgetTheme.animationDuration,
      width: widget.width,
      height: widget.height,
      child: scale,
    );
  }

  Widget _buildVerticalScale(Widget scale) {
    return AnimatedContainer(
      duration: widget.widgetTheme.animationDuration,
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
  }

  void _handleChanged(double value) {
    setState(() => _localValue = value);
    widget.onChanged(value);
  }

  void _handleChangeEnd(double value) {
    setState(() => _localValue = null);
    widget.onChangeEnd(value);
  }
}

class _TickMarkPainter extends CustomPainter {
  final List<double> ticks;
  final double min;
  final double max;
  final Color color;
  final double tickHeight;
  final double tickWidth;
  final double horizontalPadding;

  const _TickMarkPainter({
    required this.ticks,
    required this.min,
    required this.max,
    required this.color,
    required this.tickHeight,
    required this.tickWidth,
    required this.horizontalPadding,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = tickWidth
      ..style = PaintingStyle.stroke;

    final verticalCenter = size.height / 2;
    final halfTickHeight = tickHeight / 2;

    for (final tickValue in ticks) {
      final position = (tickValue - min) / (max - min);
      final x = horizontalPadding + (position * (size.width - 2 * horizontalPadding));

      canvas.drawLine(
        Offset(x, verticalCenter - halfTickHeight),
        Offset(x, verticalCenter + halfTickHeight),
        paint,
      );
    }
  }

  @override
  bool shouldRepaint(_TickMarkPainter oldDelegate) {
    return oldDelegate.ticks != ticks ||
        oldDelegate.min != min ||
        oldDelegate.max != max ||
        oldDelegate.color != color ||
        oldDelegate.tickHeight != tickHeight ||
        oldDelegate.tickWidth != tickWidth ||
        oldDelegate.horizontalPadding != horizontalPadding;
  }
}
