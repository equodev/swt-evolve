import 'package:flutter/material.dart';
import '../gen/canvas.dart';
import '../gen/gc.dart';
import '../gen/widget.dart';
import 'utils/widget_utils.dart';
import '../theme/theme_extensions/canvas_theme_extension.dart';
import '../theme/theme_extensions/composite_theme_extension.dart';
import 'canvas_evolve.dart';
import 'gcdrawer_evolve.dart';

class GCImpl<T extends GCSwt, V extends VGC> extends GCState<T, V> {
  late GCDrawer _drawer;
  List<Shape> _snapshot = [];

  @override
  void initState() {
    super.initState();
    _drawer = GCDrawer.embedded(
      state,
      onShapesUpdated: (_) {
        if (!mounted) return;
        setState(() {});
      },
      onGCDispose: (finalShapes) {
        if (mounted) {
          setState(() {
            _snapshot = finalShapes;
          });
        }
      },
    );
  }

  @override
  void dispose() {
    _drawer.dispose();
    super.dispose();
  }

  void clearShapes() {
    if (mounted) {
      setState(() {
        _snapshot = [];
        _drawer.clearShapes();
      });
    }
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
          _drawer.widgetBoundaryKey = parentState.widgetBoundaryKey;
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
      final forced = ParentBackgroundScope.backgroundOf(context);
      if (forced != null) return forced;
      if (state.background != null) return _drawer.bg;
      return Theme.of(context).extension<CompositeThemeExtension>()!.backgroundColor;
    }
    return Colors.transparent;
  }

  @override
  Widget build(BuildContext context) {
    _drawer.syncContext(context);
    final List<Shape> shapes = _drawer.shapes.isNotEmpty
        ? List.unmodifiable(_drawer.shapes)
        : List.unmodifiable(_snapshot);
    final Widget painted = CustomPaint(
      size: bounds,
      painter: ScenePainter(canvasBg, shapes),
    );
    // Expose any text painted via the GC (drawString/drawText) as an aria-label so
    // canvas-drawn controls (e.g. custom buttons) are identifiable in devtools / E2E,
    // instead of showing up as an opaque `Canvas/<id>` node with no text. Only built
    // when the semantics tree is active, so normal rendering is unaffected.
    if (WidgetsBinding.instance.semanticsEnabled) {
      final label = _paintedTextLabel(shapes);
      if (label != null) {
        return Semantics(label: label, child: painted);
      }
    }
    return painted;
  }

  String? _paintedTextLabel(List<Shape> shapes) {
    final parts = <String>[];
    for (final shape in shapes) {
      if (shape is TextShape) {
        final text = shape.text.trim();
        if (text.isNotEmpty) parts.add(text);
      }
    }
    return parts.isEmpty ? null : parts.join(' ');
  }

}