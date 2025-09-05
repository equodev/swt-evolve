import 'package:flutter/material.dart';
import '../swt/caret.dart';
import '../swt/rectangle.dart';
import '../swt/widget.dart';
import 'dart:async';
import 'package:swtflutter/src/impl/widget_config.dart';

class CaretImpl<T extends CaretSwt, V extends CaretValue>
    extends WidgetSwtState<T, V> {
  final bool useDarkTheme = getCurrentTheme();
  Timer? _blinkTimer;
  bool _isVisible = true;

  @override
  void initState() {
    super.initState();
    _startBlinking();
  }

  @override
  void dispose() {
    _blinkTimer?.cancel();
    super.dispose();
  }

  void _startBlinking() {
    _blinkTimer = Timer.periodic(const Duration(milliseconds: 500), (_) {
      if (mounted) {
        setState(() {
          _isVisible = !_isVisible;
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      painter: CaretPainter(
        bounds: state.bounds,
        visible: state.visible ?? true,
        isBlinkVisible: _isVisible,
        useDarkTheme: useDarkTheme,
      ),
    );
  }
}

class CaretPainter extends CustomPainter {
  final RectangleValue? bounds;
  final bool visible;
  final bool isBlinkVisible;
  final bool useDarkTheme;

  CaretPainter({
    required this.bounds,
    required this.visible,
    required this.isBlinkVisible,
    required this.useDarkTheme,
  });

  @override
  void paint(Canvas canvas, Size size) {
    if (!visible || !isBlinkVisible) return;

    final paint = Paint()
      ..color = useDarkTheme ? Colors.white : Colors.black
      ..style = PaintingStyle.fill;

    final rect = Rect.fromLTWH(
      bounds?.x?.toDouble() ?? 0,
      bounds?.y?.toDouble() ?? 0,
      bounds?.width?.toDouble() ?? 2,
      bounds?.height?.toDouble() ?? 20,
    );

    canvas.drawRect(rect, paint);
  }

  @override
  bool shouldRepaint(CaretPainter oldDelegate) {
    return oldDelegate.visible != visible ||
        oldDelegate.isBlinkVisible != isBlinkVisible ||
        oldDelegate.bounds != bounds ||
        oldDelegate.useDarkTheme != useDarkTheme;
  }
}
