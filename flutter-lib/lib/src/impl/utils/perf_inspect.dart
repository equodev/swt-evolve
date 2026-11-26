import 'dart:convert';
import 'dart:developer' as developer;

import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';

import '../gcdrawer_evolve.dart';
import 'perf_marks.dart';

/// With perf marks on, the service extension `ext.evolve.paintStats`: what the frame on screen is
/// made of that can cost the raster thread a pass of its own (layers by kind, opacity, colour
/// filters, shader masks, clips that save a layer, blurred shadows), counted over the render and
/// layer trees, with up to `samples` (default 3) owner chains each. A profile build has no
/// `debugDumpLayerTree`; this is the part of it the raster cost depends on.
void registerPerfInspector() {
  if (!perfMarksEnabled) return;
  developer.registerExtension('ext.evolve.paintStats', (method, params) async {
    final samples = int.tryParse(params['samples'] ?? '') ?? 3;
    final stats = _Stats(samples);
    for (final view in RendererBinding.instance.renderViews) {
      stats.render(view, const []);
      // ignore: invalid_use_of_protected_member
      final root = view.layer;
      if (root != null) stats.layer(root);
    }
    return developer.ServiceExtensionResponse.result(jsonEncode(stats.toJson()));
  });
}

class _Stats {
  _Stats(this.samples);

  final int samples;
  final Map<String, int> counts = {};
  final Map<String, List<String>> where = {};
  final Map<String, int> layers = {};
  final Map<String, int> types = {};

  void _count(String key, List<String> chain) {
    counts[key] = (counts[key] ?? 0) + 1;
    final list = where.putIfAbsent(key, () => []);
    if (list.length < samples) list.add(chain.reversed.take(8).join(' < '));
  }

  void render(RenderObject o, List<String> chain) {
    final type = o.runtimeType.toString();
    types[type] = (types[type] ?? 0) + 1;
    final here = _interesting(o, type);
    final nextChain = _owner(o, type) ? [...chain, type] : chain;
    if (here != null) _count(here, nextChain);
    // Offstage and the like do not paint what they hold.
    if (o is RenderOffstage && o.offstage) return;
    o.visitChildren((c) => render(c, nextChain));
  }

  /// Types worth naming in a chain: the ones that say whose subtree this is.
  bool _owner(RenderObject o, String type) =>
      type.contains('LayoutBuilder') || type.contains('CustomPaint') || type.contains('Image') ||
      type.contains('Picture') || type.contains('Svg');

  String? _interesting(RenderObject o, String type) {
    if (o is RenderOpacity) {
      final a = o.opacity;
      return a > 0 && a < 1 ? 'opacity<1' : null;
    }
    if (o is RenderAnimatedOpacity) return 'animatedOpacity';
    if (o is RenderShaderMask) return 'shaderMask';
    if (o is RenderBackdropFilter) return 'backdropFilter';
    if (type.contains('ColorFilter')) return 'colorFiltered';
    if (type.contains('ImageFilter')) return 'imageFiltered';
    if (o is RenderImage && o.image != null) {
      if (o.color != null) return 'image+color(${o.colorBlendMode})';
      return null;
    }
    if (o is RenderClipRRect && o.clipBehavior == Clip.antiAliasWithSaveLayer) return 'clipRRectSaveLayer';
    if (o is RenderClipRect && o.clipBehavior == Clip.antiAliasWithSaveLayer) return 'clipRectSaveLayer';
    if (o is RenderClipPath && o.clipBehavior == Clip.antiAliasWithSaveLayer) return 'clipPathSaveLayer';
    if (o is RenderPhysicalModel && o.elevation > 0) return 'physicalModel';
    if (o is RenderPhysicalShape && o.elevation > 0) return 'physicalShape';
    if (o is RenderDecoratedBox) {
      final d = o.decoration;
      if (d is BoxDecoration && (d.boxShadow?.any((s) => s.blurRadius > 0) ?? false)) {
        final transparent = d.boxShadow!.every((s) => s.color.a == 0);
        return transparent ? 'boxShadow(transparent)' : 'boxShadow';
      }
    }
    if (o is RenderCustomPaint && o.painter != null) {
      final painter = o.painter;
      if (painter is ScenePainter) _scene(painter);
      return 'painter:${painter.runtimeType}';
    }
    return null;
  }

  /// A GC scene's shapes by kind, and what in them saves a layer.
  void _scene(ScenePainter p) {
    if (p.bg.a == 0 && p.shapes.any((s) => s is RegionShape || s is EraseShape)) _count('scene:ownLayer', const []);
    _shapes(p.shapes);
  }

  void _shapes(List<Shape> shapes) {
    for (final s in shapes) {
      final type = s.runtimeType.toString();
      counts['shape:$type'] = (counts['shape:$type'] ?? 0) + 1;
      if (s is ImageShape) {
        final key = 'img:${s.type.name}${s.colorFilter != null ? ' colorFilter' : ''}'
            '${s.alpha != 255 ? ' alpha' : ''}${s.inkMask != null ? ' ink' : ''}';
        counts[key] = (counts[key] ?? 0) + 1;
      }
      if (s is RegionShape) _shapes(s.ops);
      if (s is XorShape) _shapes(s.children);
      if (s is RenderHintsShape) _shapes(s.children);
      if (s is PatternStrokeShape) _shapes(s.children);
      if (s is MaskShape) _shapes([...s.mask, ...s.children]);
      if (s is ClipPathShape) _shapes(s.children);
      if (s is TransformShape) _shapes(s.children);
    }
  }

  void layer(Layer l) {
    final type = l.runtimeType.toString();
    var key = type;
    if (l is OpacityLayer) key = 'OpacityLayer(${(l.alpha ?? 255) == 255 ? 'opaque' : 'alpha'})';
    if (l is ClipRRectLayer) key = 'ClipRRectLayer(${l.clipBehavior.name})';
    if (l is ClipRectLayer) key = 'ClipRectLayer(${l.clipBehavior.name})';
    if (l is ClipPathLayer) key = 'ClipPathLayer(${l.clipBehavior.name})';
    layers[key] = (layers[key] ?? 0) + 1;
    if (l is ContainerLayer) {
      for (Layer? c = l.firstChild; c != null; c = c.nextSibling) {
        layer(c);
      }
    }
  }

  Map<String, Object> toJson() => {
        'render': counts,
        'layers': layers,
        'types': types,
        'where': where,
      };
}
