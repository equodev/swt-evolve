import 'package:flutter/material.dart';
import '../gen/canvas.dart';
import '../gen/gc.dart';
import '../gen/widget.dart';
import '../theme/theme_extensions/canvas_theme_extension.dart';
import 'canvas_evolve.dart';
import 'gcdrawer_evolve.dart';

class GCImpl<T extends GCSwt, V extends VGC> extends GCState<T, V> {
  late GCDrawer _drawer;

  @override
  void initState() {
    super.initState();
    _drawer = GCDrawer.embedded(
      state,
      onShapesUpdated: (_) { if (mounted) setState(() {}); },
    );
  }

  @override
  void dispose() {
    _drawer.dispose();
    super.dispose();
  }

  void clearShapes() {
    if (mounted) setState(() => _drawer.clearShapes());
  }

  @override
  void extraSetState() {
    super.extraSetState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) _notifyParentGCReady();
    });
  }

  void _notifyParentGCReady() {
    context.visitAncestorElements((element) {
      if (element is StatefulElement && element.state is WidgetSwtState) {
        final parentState = element.state as WidgetSwtState;
        if (parentState.gcOverlayKey == widget.key) {
          parentState.notifyGCReady(state as VGC);
          return false;
        }
      }
      return true;
    });
  }

  CanvasThemeExtension get _canvasTheme =>
      Theme.of(context).extension<CanvasThemeExtension>()!;

  Size get bounds {
    final canvas = context.findAncestorWidgetOfExactType<CanvasSwt>();
    if (canvas != null && canvas.value.id == state.id) {
      final canvasState = context.findAncestorStateOfType<CanvasImpl>();
      return canvasState?.getBounds() ??
          Size(_canvasTheme.defaultWidth, _canvasTheme.defaultHeight);
    }
    return Size(_canvasTheme.defaultWidth, _canvasTheme.defaultHeight);
  }

  Color get canvasBg {
    final canvas = context.findAncestorWidgetOfExactType<CanvasSwt>();
    if (canvas != null && canvas.value.id == state.id) {
      if (state.background != null) return _drawer.bg;
      return _canvasTheme.backgroundColor;
    }
    return Colors.transparent;
  }

  @override
  Widget build(BuildContext context) {
    _drawer.syncContext(context);
    return CustomPaint(
      size: bounds,
      painter: ScenePainter(canvasBg, List.unmodifiable(_drawer.shapes)),
    );
  }

}
