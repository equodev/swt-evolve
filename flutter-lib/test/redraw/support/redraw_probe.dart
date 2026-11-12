// What one operation costs the frames it causes: widgets rebuilt, render objects painted, and how
// much of the window the re-recorded layers cover. Counts come from the framework's debug hooks, so
// they are exact in a test and say nothing about time.

import 'package:flutter/foundation.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

/// What an operation cost, summed over every frame it caused until nothing was left scheduled.
class RedrawCost {
  RedrawCost(this.name);

  final String name;
  int builds = 0;
  int paints = 0;
  int frames = 0;

  /// Frames that re-recorded at least one layer.
  int framesPainted = 0;

  /// Layers re-recorded, summed over the frames.
  int layers = 0;

  /// Area drawn again, summed over the frames, in windows: 2.0 is the whole window drawn twice. Per
  /// layer re-recorded it is the box around what drew in it.
  double windows = 0;

  /// The largest share of the window one frame repainted.
  double worstFrame = 0;
  final Map<String, int> buildsByType = {};
  final Map<String, int> paintsByType = {};

  static String _top(Map<String, int> counts, int n) {
    final sorted = counts.entries.toList()..sort((a, b) => b.value.compareTo(a.value));
    return sorted.take(n).map((e) => '${e.key} ${e.value}').join(', ');
  }

  @override
  String toString() =>
      '$name: builds=$builds paints=$paints frames=$frames painted=$framesPainted '
      'layers=$layers windows=${windows.toStringAsFixed(2)} worst=${(worstFrame * 100).toStringAsFixed(1)}%';

  String get detail => '  built: ${_top(buildsByType, 8)}\n  painted: ${_top(paintsByType, 8)}';
}

/// Counts what each [measure]d operation rebuilds and repaints in a window of [window] logical
/// pixels at the top left of the view.
class RedrawProbe {
  RedrawProbe(this.tester, this.window);

  final WidgetTester tester;
  final Size window;
  RedrawCost? _current;

  /// What was drawn in the frame under way, per layer re-recorded: the box around everything that
  /// draws (a leaf, a background or a custom painter), in window coordinates.
  final Map<RenderObject, Rect> _drawn = {};
  bool _installed = false;

  /// Hooks the framework; undone at the end of the test.
  void install() {
    if (_installed) return;
    _installed = true;
    debugOnRebuildDirtyWidget = (element, builtOnce) {
      final cost = _current;
      if (cost == null) return;
      cost.builds++;
      _bump(cost.buildsByType, element.widget.runtimeType);
    };
    debugOnProfilePaint = (renderObject) {
      final cost = _current;
      if (cost == null) return;
      // A clean repaint boundary is only composited again, not painted.
      if (renderObject.isRepaintBoundary && !renderObject.debugNeedsPaint) return;
      cost.paints++;
      _bump(cost.paintsByType, renderObject.runtimeType);
      if (_draws(renderObject) && renderObject is RenderBox && renderObject.hasSize) {
        final layer = _boundaryOf(renderObject.parent ?? renderObject);
        final r = _onScreen(renderObject).intersect(_onScreen(layer));
        if (!r.isEmpty) {
          final held = _drawn[layer];
          _drawn[layer] = held == null ? r : held.expandToInclude(r);
        }
      }
    };
    tester.binding.addPersistentFrameCallback(_afterFrame);
  }

  /// Unhooks the framework; the test binding checks the hooks are unset before tear-down runs.
  void uninstall() {
    debugOnRebuildDirtyWidget = null;
    debugOnProfilePaint = null;
    _current = null;
  }

  static void _bump(Map<String, int> counts, Type type) {
    final name = type.toString().split('<').first;
    counts[name] = (counts[name] ?? 0) + 1;
  }

  /// [o]'s paint bounds in the window, cut to the window and to every clip above it.
  Rect _onScreen(RenderObject o) {
    final view = Offset.zero & window;
    if (o is! RenderBox || !o.hasSize) return view;
    var r = MatrixUtils.transformRect(o.getTransformTo(null), o.paintBounds).intersect(view);
    for (var p = o.parent; p != null && !r.isEmpty; p = p.parent) {
      if (p is RenderClipRect || p is RenderViewportBase || p.runtimeType.toString() == '_RenderSingleChildViewport') {
        final box = p as RenderBox;
        r = r.intersect(MatrixUtils.transformRect(box.getTransformTo(null), Offset.zero & box.size));
      }
    }
    return r;
  }

  /// Whether [o] puts something on the canvas itself rather than only placing its children.
  static bool _draws(RenderObject o) {
    var leaf = true;
    o.visitChildren((_) => leaf = false);
    return leaf ||
        o is RenderDecoratedBox ||
        o is RenderCustomPaint ||
        o is RenderPhysicalModel ||
        o is RenderPhysicalShape ||
        o.runtimeType.toString() == '_RenderColoredBox';
  }

  static RenderObject _boundaryOf(RenderObject o) {
    var node = o;
    while (!node.isRepaintBoundary && node.parent != null) {
      node = node.parent!;
    }
    return node;
  }

  static String _who(RenderObject b) {
    final s = '${b.debugCreator}';
    return s.length > 400 ? s.substring(0, 400) : s;
  }

  void _afterFrame(Duration _) {
    final cost = _current;
    if (cost == null) return;
    cost.frames++;
    if (_drawn.isEmpty) return;
    var area = 0.0;
    for (final MapEntry(key: layer, value: r) in _drawn.entries) {
      area += r.width * r.height;
      cost.layers++;
      // --dart-define=REDRAW_WHO=true names every layer re-recorded and what it covered.
      if (const bool.fromEnvironment('REDRAW_WHO')) {
        // ignore: avoid_print
        print('[redraw-who] ${cost.name}: $r ${_who(layer)}');
      }
    }
    _drawn.clear();
    final share = area / (window.width * window.height);
    cost.windows += share;
    cost.framesPainted++;
    if (share > cost.worstFrame) cost.worstFrame = share;
  }

  /// Runs [operation], then pumps until nothing is scheduled (or [settle] frames passed) and returns
  /// what every frame in between rebuilt and repainted.
  Future<RedrawCost> measure(
    String name,
    Future<void> Function() operation, {
    int settle = 120,
    Duration step = const Duration(milliseconds: 8),
  }) async {
    install();
    final cost = RedrawCost(name);
    _drawn.clear();
    _current = cost;
    await operation();
    for (var i = 0; i < settle && tester.binding.hasScheduledFrame; i++) {
      await tester.pump(step);
    }
    _current = null;
    if (kDebugMode) {
      // ignore: avoid_print
      print('[redraw] $cost\n${cost.detail}');
    }
    return cost;
  }
}
