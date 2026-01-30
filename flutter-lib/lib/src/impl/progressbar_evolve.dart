import 'dart:math' as math;
import 'package:flutter/material.dart';
import '../gen/progressbar.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/progressbar_theme_extension.dart';
import 'utils/widget_utils.dart';

class ProgressBarImpl<T extends ProgressBarSwt, V extends VProgressBar>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ProgressBarThemeExtension>()!;
    
    state.minimum ??= 0;
    state.maximum ??= 100;
    state.selection ??= 0;

    final currentValue = state.selection!.clamp(state.minimum!, state.maximum!).toDouble();

    final range = (state.maximum! - state.minimum!).toDouble();
    final progress = range > 0 ? (currentValue - state.minimum!) / range : 0.0;

    final hasConstraints = hasBounds(state.bounds);
    final width = hasConstraints ? state.bounds!.width.toDouble() : widgetTheme.defaultWidth;
    final height = hasConstraints ? state.bounds!.height.toDouble() : widgetTheme.defaultHeight;

    final styleBits = StyleBits(state.style);
    final isVertical = styleBits.has(SWT.VERTICAL);
    final isIndeterminate = styleBits.has(SWT.INDETERMINATE);
    final isEnabled = state.enabled ?? true;

    return wrap(
      _StyledProgressBar(
        widgetTheme: widgetTheme,
        progress: progress,
        width: width,
        height: height,
        isVertical: isVertical,
        isIndeterminate: isIndeterminate,
        enabled: isEnabled,
      ),
    );
  }
}

class _StyledProgressBar extends StatefulWidget {
  final ProgressBarThemeExtension widgetTheme;
  final double progress;
  final double width;
  final double height;
  final bool isVertical;
  final bool isIndeterminate;
  final bool enabled;

  const _StyledProgressBar({
    required this.widgetTheme,
    required this.progress,
    required this.width,
    required this.height,
    required this.isVertical,
    required this.isIndeterminate,
    required this.enabled,
  });

  @override
  State<_StyledProgressBar> createState() => _StyledProgressBarState();
}

class _StyledProgressBarState extends State<_StyledProgressBar>
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  int _currentCycle = 0; 

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      vsync: this,
      duration: widget.widgetTheme.indeterminateDuration,
    );

    if (widget.isIndeterminate) {
      _animationController.addStatusListener(_onAnimationStatus);
      _animationController.forward();
    }
  }

  void _onAnimationStatus(AnimationStatus status) {
    if (status == AnimationStatus.completed) {
      _currentCycle = (_currentCycle + 1) % 2; 
      _animationController.forward(from: 0.0);
    }
  }

  @override
  void didUpdateWidget(_StyledProgressBar oldWidget) {
    super.didUpdateWidget(oldWidget);

    if (oldWidget.widgetTheme.indeterminateDuration != widget.widgetTheme.indeterminateDuration) {
      _animationController.duration = widget.widgetTheme.indeterminateDuration;
    }

    if (widget.isIndeterminate && !oldWidget.isIndeterminate) {
      _animationController.addStatusListener(_onAnimationStatus);
      _animationController.forward();
    } else if (!widget.isIndeterminate && oldWidget.isIndeterminate) {
      _animationController.removeStatusListener(_onAnimationStatus);
      _animationController.stop();
      _animationController.reset();
    }
  }

  @override
  void dispose() {
    _animationController.removeStatusListener(_onAnimationStatus);
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = widget.widgetTheme;
    
    final progressBar = Container(
      width: widget.isVertical ? widget.width : widget.width,
      height: widget.isVertical ? widget.height : widget.height,
      decoration: BoxDecoration(
        color: theme.backgroundColor,
        border: Border.all(color: theme.borderColor, width: theme.borderWidth),
        borderRadius: BorderRadius.circular(theme.borderRadius),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(theme.borderRadius - theme.borderWidth),
        child: widget.isIndeterminate
            ? _buildIndeterminateProgress()
            : _buildDeterminateProgress(),
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
    }

    return progressBar;
  }

  Widget _buildDeterminateProgress() {
    final theme = widget.widgetTheme;
    final progressColor = widget.enabled 
        ? theme.progressColor 
        : theme.disabledProgressColor;

    return Align(
      alignment: Alignment.centerLeft,
      child: FractionallySizedBox(
        widthFactor: widget.progress.clamp(0.0, 1.0),
        heightFactor: 1.0,
        child: Container(
          decoration: BoxDecoration(
            color: progressColor,
            borderRadius: BorderRadius.circular(theme.borderRadius - theme.borderWidth),
          ),
        ),
      ),
    );
  }

  Widget _buildIndeterminateProgress() {
    final theme = widget.widgetTheme;
    
    return AnimatedBuilder(
      animation: _animationController,
      builder: (context, child) {
        return CustomPaint(
          painter: _IndeterminateProgressPainter(
            animationValue: _animationController.value,
            color: theme.progressColor,
            borderRadius: theme.borderRadius - theme.borderWidth,
            enableSizeAnimation: theme.indeterminateEnableSizeAnimation,
            cycleType: _currentCycle,
            sizeStartA: theme.indeterminateSizeStartA,
            sizeMidA: theme.indeterminateSizeMidA,
            sizeEndA: theme.indeterminateSizeEndA,
            speedFirstA: theme.indeterminateSpeedFirstA,
            speedSecondA: theme.indeterminateSpeedSecondA,
            sizeStartB: theme.indeterminateSizeStartB,
            sizeMidB: theme.indeterminateSizeMidB,
            sizeEndB: theme.indeterminateSizeEndB,
            speedFirstB: theme.indeterminateSpeedFirstB,
            speedSecondB: theme.indeterminateSpeedSecondB,
          ),
        );
      },
    );
  }
}

class _IndeterminateProgressPainter extends CustomPainter {
  final double animationValue;
  final Color color;
  final double borderRadius;
  final bool enableSizeAnimation;
  final int cycleType; 
  
  final double sizeStartA;
  final double sizeMidA;
  final double sizeEndA;
  final double speedFirstA;
  final double speedSecondA;
  
  final double sizeStartB;
  final double sizeMidB;
  final double sizeEndB;
  final double speedFirstB;
  final double speedSecondB;

  _IndeterminateProgressPainter({
    required this.animationValue,
    required this.color,
    required this.borderRadius,
    required this.enableSizeAnimation,
    required this.cycleType,
    required this.sizeStartA,
    required this.sizeMidA,
    required this.sizeEndA,
    required this.speedFirstA,
    required this.speedSecondA,
    required this.sizeStartB,
    required this.sizeMidB,
    required this.sizeEndB,
    required this.speedFirstB,
    required this.speedSecondB,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final position = _calculatePosition(animationValue);
    
    final sizeMultiplier = enableSizeAnimation
        ? _calculateSize(animationValue)
        : 1.0;

    final opacity = _calculateOpacity(animationValue);
    
    if (opacity <= 0.0) return;

    final paint = Paint()
      ..color = color.withOpacity(opacity)
      ..style = PaintingStyle.fill;

    final barWidth = size.width * sizeMultiplier;
    final offset = position * size.width;

    final rect = Rect.fromLTWH(
      offset.clamp(0.0, size.width),
      0,
      barWidth.clamp(0.0, (size.width - offset.clamp(0.0, size.width)).clamp(0.0, size.width)),
      size.height,
    );

    canvas.drawRRect(
      RRect.fromRectAndRadius(rect, Radius.circular(borderRadius)),
      paint,
    );
  }

  double _calculatePosition(double progress) {
    final params = cycleType == 0
        ? (speedFirst: speedFirstA, speedSecond: speedSecondA)
        : (speedFirst: speedFirstB, speedSecond: speedSecondB);
    
    if (progress < 0.5) {
      return (progress * 2.0) * 0.5 * params.speedFirst;
    } else {
      final secondProgress = (progress - 0.5) * 2.0;
      return (0.5 * params.speedFirst) + (secondProgress * 0.5 * params.speedSecond);
    }
  }

  double _calculateSize(double progress) {
    final params = cycleType == 0
        ? (start: sizeStartA, mid: sizeMidA, end: sizeEndA)
        : (start: sizeStartB, mid: sizeMidB, end: sizeEndB);
    
    if (progress < 0.5) {
      final t = progress * 2.0;
      return params.start + (params.mid - params.start) * t;
    } else {
      final t = (progress - 0.5) * 2.0;
      return params.mid + (params.end - params.mid) * t;
    }
  }

  double _calculateOpacity(double progress) {
    if (progress < 0.85) {
      return 1.0;
    } else {
      final fadeProgress = (progress - 0.85) / 0.15;
      return 1.0 - fadeProgress;
    }
  }

  @override
  bool shouldRepaint(_IndeterminateProgressPainter oldDelegate) {
    return oldDelegate.animationValue != animationValue ||
        oldDelegate.color != color ||
        oldDelegate.cycleType != cycleType ||
        oldDelegate.borderRadius != borderRadius ||
        oldDelegate.enableSizeAnimation != enableSizeAnimation;
  }
}