import 'package:flutter/material.dart';
import '../gen/canvas.dart';
import '../gen/gc.dart';
import '../gen/widget.dart';
import 'utils/widget_utils.dart';
import '../theme/theme_extensions/canvas_theme_extension.dart';
import '../theme/theme_extensions/composite_theme_extension.dart';
import 'canvas_evolve.dart';
import 'control_evolve.dart';
import 'gcdrawer_evolve.dart';

class GCImpl<T extends GCSwt, V extends VGC> extends GCState<T, V> {
  late GCDrawer _drawer;
  List<Shape> _snapshot = [];

  // Guards against overlapping Paint request/response round-trips for the same
  // Canvas (e.g. a Shell fade animation and a hover-driven redraw both asking to
  // repaint close together): shapes.clear()+addAll() on gcDispose isn't safe if a
  // second request's ops are still arriving when the first one commits.
  bool _awaitingDispose = false;
  bool get hasPendingPaint => _awaitingDispose;
  void markPaintRequested() => _awaitingDispose = true;

  // Set when a VGC arrives on this GC's own channel. Until then the widget's value is the
  // placeholder VGC its parent synthesizes on every build, so holding a value proves nothing
  // about Java having drawn through this GC.
  bool _stateDelivered = false;

  @override
  void initState() {
    super.initState();
    _drawer = GCDrawer.embedded(
      state,
      onShapesUpdated: (_) {
        if (!mounted) return;
        setState(() {});
        // Promote the overlay onstage once it has painted content (a constructor-only paint
        // sends no VGC state push that would otherwise do it).
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) _notifyParentGCReady();
        });
      },
      onGCDispose: (finalShapes) {
        if (mounted) {
          setState(() {
            _snapshot = finalShapes;
          });
        }
      },
      onFullRepaintNeeded: _requestFullRepaintFromParent,
    );
    // The op channels are registered now; tell the owner it may ask Java to paint.
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) _notifyParentGCSubscribed();
    });
  }

  void _notifyParentGCSubscribed() {
    context.visitAncestorElements((element) {
      if (element is StatefulElement && element.state is WidgetSwtState) {
        final parentState = element.state as WidgetSwtState;
        if (parentState.gcOverlayKey == widget.key) {
          if (parentState is ControlImpl) parentState.onGCOverlaySubscribed();
          return false;
        }
      }
      return true;
    });
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
  void setValue(V value) {
    _stateDelivered = true;
    super.setValue(value);
  }

  @override
  void extraSetState() {
    super.extraSetState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) _notifyParentGCReady();
    });
  }

  void _requestFullRepaintFromParent() {
    if (!mounted) return;
    context.visitAncestorElements((element) {
      if (element is StatefulElement && element.state is ControlImpl) {
        (element.state as ControlImpl).requestFullRepaint();
        return false;
      }
      return true;
    });
  }

  /// Attaches this GC to the parent that owns it: hands the drawer the parent's paint boundary
  /// and, once the GC has something to show, promotes the overlay from Offstage.
  ///
  /// Readiness is a property of the GC, not of the parent's build count: extraSetState also runs
  /// from didUpdateWidget, so any rebuild of the parent reaches here carrying the parent's
  /// placeholder VGC. Promoting on that would make a control that was merely updated render a
  /// different structure than the same control freshly mounted with the same state.
  void _notifyParentGCReady() {
    final bool ready =
        _stateDelivered || _drawer.shapes.isNotEmpty || _snapshot.isNotEmpty;
    context.visitAncestorElements((element) {
      if (element is StatefulElement && element.state is WidgetSwtState) {
        final parentState = element.state as WidgetSwtState;
        if (parentState.gcOverlayKey == widget.key) {
          if (ready) parentState.notifyGCReady(state as VGC);
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
      // The backdrop stands in for SWT's erase-to-background. A Canvas with no background of
      // its own under an ancestor's backgroundImage erases to that image, which the ancestor
      // already painted underneath -- an opaque erase here would hide it.
      if (!(canvas.value.hasOwnBackground ?? false) &&
          canvas.value.backgroundImage == null &&
          ParentBackgroundScope.backgroundImageOf(context) != null) {
        return Colors.transparent;
      }
      final forced = ParentBackgroundScope.backgroundOf(context);
      if (forced != null) return forced;
      // The erase is not part of the application's drawing -- it is the surface that drawing
      // lands on -- so it follows the Canvas's own background, resolved by the same rule as
      // every other control. The GC's colors stay with the shapes: taking them here made an
      // owner-drawn control erase in the application's color while the trim around it used the
      // theme's, leaving the control as a visible block against its host.
      final canvasState = context.findAncestorStateOfType<CanvasImpl>();
      if (canvasState != null) return canvasState.bg;
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
    // No shapes to paint over: skip the backdrop, it was redundantly occluding the
    // Control/Canvas's own background (including any inherited backgroundImage).
    final Widget painted = CustomPaint(
      size: bounds,
      painter: ScenePainter(shapes.isEmpty ? Colors.transparent : canvasBg, shapes),
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
    void collect(List<Shape> from) {
      for (final shape in from) {
        if (shape is TextShape) {
          final text = shape.text.trim();
          if (text.isNotEmpty) parts.add(text);
        } else if (shape is RegionShape) {
          collect(shape.ops);
        } else if (shape is ClipPathShape) {
          collect(shape.children);
        }
      }
    }

    collect(shapes);
    return parts.isEmpty ? null : parts.join(' ');
  }

}