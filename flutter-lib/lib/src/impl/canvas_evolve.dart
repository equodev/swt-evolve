import 'package:flutter/material.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/gc.dart';
import '../gen/canvas.dart';
import '../gen/widgets.dart';
import 'composite_evolve.dart';
import '../custom/toolbar_composite.dart';
import 'color_utils.dart';
import 'utils/widget_utils.dart';
import '../theme/theme_extensions/canvas_theme_extension.dart';

class CanvasImpl<T extends CanvasSwt, V extends VCanvas>
    extends CompositeImpl<T, V> {

  CanvasThemeExtension get _theme =>
      Theme.of(context).extension<CanvasThemeExtension>()!;

  Color get bg =>
      colorFromVColor(state.background, defaultColor: _theme.backgroundColor);
  Color get fg => _theme.foregroundColor;
  Color gcBg = Colors.transparent;

  final int _alpha = 255;
  Rect? clipRect;
  Color applyAlpha(Color color) {
    if (_alpha == 255) return color;
    return color.withOpacity(_alpha / 255.0);
  }

  @override
  void initState() {
    super.initState();
    widget.sendPaintPaint(state, null);
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = _theme;
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);
    final children = state.children;
    if (children != null && children.isNotEmpty) {
      return buildComposite();
    }

    Widget content;
    if (hasValidBounds && constraints != null) {
      content = ConstrainedBox(constraints: constraints, child: const SizedBox.expand());
    } else {
      content = SizedBox(
        width: widgetTheme.defaultWidth,
        height: widgetTheme.defaultHeight,
      );
    }

    return wrap(content);
  }

  Size getBounds() {
    if (hasBounds(state.bounds)) {
      return Size(
        state.bounds!.width.toDouble(),
        state.bounds!.height.toDouble(),
      );
    }
    return Size(_theme.defaultWidth, _theme.defaultHeight);
  }

  // Canvas always shows GCSwt directly (never in Offstage) to preserve
  // coordinate system consistency. The old code rendered GCSwt as the main
  // content, so we replicate that: GCSwt is always visible via Positioned.fill.
  @override
  Widget wrapWithGCOverlay(Widget child) {
    final gc = gcOverlay ?? (VGC()..id = state.id);
    final gcWidget = GCSwt<VGC>(key: gcOverlayKey, value: gc);
    return Stack(
      children: [
        child,
        Positioned.fill(child: IgnorePointer(child: gcWidget)),
      ],
    );
  }

  Color rgbMapToColor(Map<String, dynamic> m) => Color.fromARGB(
    (m['alpha'] ?? 255) as int,
    (m['red'] ?? 0) as int,
    (m['green'] ?? 0) as int,
    (m['blue'] ?? 0) as int,
  );
}

