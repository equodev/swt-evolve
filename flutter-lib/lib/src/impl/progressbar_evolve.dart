import 'package:flutter/material.dart';
import '../gen/progressbar.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../styles.dart';
import 'color_utils.dart';

class ProgressBarImpl<T extends ProgressBarSwt, V extends VProgressBar>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // Initialize values with defaults
    state.minimum ??= 0;
    state.maximum ??= 100;
    state.selection ??= 0;

    // Ensure selection is within bounds
    final currentValue =
        state.selection!.clamp(state.minimum!, state.maximum!).toDouble();

    // Calculate progress (0.0 to 1.0)
    final range = (state.maximum! - state.minimum!).toDouble();
    final progress = range > 0 ? (currentValue - state.minimum!) / range : 0.0;

    // Use bounds if available, otherwise use default size
    final width = state.bounds?.width.toDouble() ?? 200.0;
    final height = state.bounds?.height.toDouble() ?? 20.0;

    final isVertical = state.style.has(SWT.VERTICAL);
    final isIndeterminate = state.style.has(SWT.INDETERMINATE);

    return wrap(
      _SimpleProgressBar(
        progress: progress,
        width: width,
        height: height,
        isVertical: isVertical,
        isIndeterminate: isIndeterminate,
        enabled: state.enabled ?? true,
      ),
    );
  }
}

class _SimpleProgressBar extends StatefulWidget {
  final double progress;
  final double width;
  final double height;
  final bool isVertical;
  final bool isIndeterminate;
  final bool enabled;

  const _SimpleProgressBar({
    Key? key,
    required this.progress,
    required this.width,
    required this.height,
    required this.isVertical,
    required this.isIndeterminate,
    required this.enabled,
  }) : super(key: key);

  @override
  _SimpleProgressBarState createState() => _SimpleProgressBarState();
}

class _SimpleProgressBarState extends State<_SimpleProgressBar>
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    );

    if (widget.isIndeterminate) {
      _animationController.repeat();
    }
  }

  @override
  void didUpdateWidget(_SimpleProgressBar oldWidget) {
    super.didUpdateWidget(oldWidget);

    if (widget.isIndeterminate && !oldWidget.isIndeterminate) {
      _animationController.repeat();
    } else if (!widget.isIndeterminate && oldWidget.isIndeterminate) {
      _animationController.stop();
    }
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final accentColor = getAccentColor();
    final backgroundColor = getBackground();
    final borderColor = getBorderColor();
    final disabledColor = getForegroundDisabled();

    final progressBar = Container(
      width: widget.isVertical ? widget.width : widget.width,
      height: widget.isVertical ? widget.height : widget.height,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border.all(color: borderColor, width: 1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(3),
        child: widget.isIndeterminate
            ? _buildIndeterminateProgress(accentColor)
            : _buildDeterminateProgress(accentColor, disabledColor),
      ),
    );

    if (widget.isVertical) {
      return RotatedBox(
        quarterTurns: 3,
        child: SizedBox(
          width: widget.height,
          height: widget.width,
          child: progressBar,
        ),
      );
    } else {
      return progressBar;
    }
  }

  Widget _buildDeterminateProgress(Color accentColor, Color disabledColor) {
    final color = widget.enabled ? accentColor : disabledColor;

    return Align(
      alignment: Alignment.centerLeft,
      child: FractionallySizedBox(
        widthFactor: widget.progress.clamp(0.0, 1.0),
        heightFactor: 1.0,
        child: Container(
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.circular(3),
          ),
        ),
      ),
    );
  }

  Widget _buildIndeterminateProgress(Color accentColor) {
    return AnimatedBuilder(
      animation: _animationController,
      builder: (context, child) {
        final animationValue = _animationController.value;

        return CustomPaint(
          painter: _IndeterminateProgressPainter(
            animationValue: animationValue,
            color: accentColor,
          ),
          child: Container(),
        );
      },
    );
  }
}

class _IndeterminateProgressPainter extends CustomPainter {
  final double animationValue;
  final Color color;

  _IndeterminateProgressPainter({
    required this.animationValue,
    required this.color,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;

    // Create a moving bar effect (30% of width)
    final barWidth = size.width * 0.3;
    final maxOffset = size.width - barWidth;

    // Ease in-out animation
    final easedValue = Curves.easeInOut.transform(animationValue);
    final offset = easedValue * maxOffset;

    final rect = Rect.fromLTWH(
      offset,
      0,
      barWidth,
      size.height,
    );

    canvas.drawRRect(
      RRect.fromRectAndRadius(rect, const Radius.circular(3)),
      paint,
    );
  }

  @override
  bool shouldRepaint(_IndeterminateProgressPainter oldDelegate) {
    return oldDelegate.animationValue != animationValue ||
        oldDelegate.color != color;
  }
}
