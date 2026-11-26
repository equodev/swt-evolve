import 'package:flutter/painting.dart';
import 'package:flutter_svg/flutter_svg.dart';

import 'font_utils.dart';

/// A single-colour SVG drawn in a tint, the colour put into the picture as it is parsed where
/// `ColorFilter.mode(tint, BlendMode.srcIn)` puts it on as it is drawn.
///
/// A colour filter on a picture costs a layer of its own each time the picture is drawn, on every
/// frame; a colour baked into the picture costs none.
///
/// srcIn keeps the alpha of what the SVG draws and takes the colour from the tint. Every colour in
/// the SVG taking the tint's, each keeping its own alpha, draws the same pixels when the tint is
/// opaque: overlapping elements of one colour compose to the same alpha either way. A translucent
/// tint is applied once over the whole icon by the filter but once per element here, darker where
/// elements overlap; and an SVG that draws what has no colour to replace (an image) or composes
/// its elements otherwise (a mask, a filter, a pattern, a blend mode) is not one colour. Those keep
/// the filter.
class SvgTint {
  SvgTint._();

  static final Map<String, String?> _sources = {};

  /// A loader for [svg] drawn in [tint], or null when it has to be drawn under the filter.
  static SvgStringLoader? loader(String svg, Color tint) {
    if (tint.a != 1.0) return null;
    final source = _source(svg);
    if (source == null) return null;
    return SvgStringLoader(source, colorMapper: _TintMapper(tint));
  }

  /// The tint [loader] puts into the picture it loads ([SvgTint.loader]); null for any other loader.
  static Color? tintOf(BytesLoader loader) {
    if (loader is! SvgLoader) return null;
    final mapper = loader.colorMapper;
    return mapper is _TintMapper ? mapper.tint : null;
  }

  /// [svg] with a black fill on its root when it sets none, or null when it is not one colour.
  ///
  /// An element that sets no fill of its own and inherits none is filled black without its colour
  /// being parsed, so the mapper never sees it: the fill given to the root is inherited instead,
  /// and parsed.
  static String? _source(String svg) {
    if (_sources.containsKey(svg)) return _sources[svg];
    if (_sources.length >= 512) _sources.clear();
    return _sources[svg] = _withRootFill(svg);
  }

  static const List<String> _notOneColour = [
    '<image', '<mask', 'mask=', 'mask:', '<filter', 'filter=', 'filter:', '<pattern', 'blend-mode',
  ];

  static String? _withRootFill(String svg) {
    for (final marker in _notOneColour) {
      if (svg.contains(marker)) return null;
    }
    final open = RegExp(r'<svg[\s>]').firstMatch(svg);
    if (open == null) return null;
    final end = svg.indexOf('>', open.start);
    if (end < 0) return null;
    final tag = svg.substring(open.start, end);
    if (RegExp(r'[\s;"]fill\s*[=:]').hasMatch(tag)) return svg;
    final at = open.start + 4;
    return '${svg.substring(0, at)} fill="#000000"${svg.substring(at)}';
  }
}

/// SVG pictures for a GC to draw, decoded once per SVG and tint.
///
/// A GC draws an image again each time the application paints, so the SVG is not decoded again for
/// each paint. The pictures are shared, so nothing that draws one disposes it.
/// They are dropped when the system fonts change, as an SVG's text is laid out when it is decoded.
class SvgPictures {
  SvgPictures._();

  /// The pictures decoded, kept as pictures rather than as the futures that made them: a completed
  /// future answers in the zone it was made in, which a later caller's may not be (a test's).
  static final Map<(String, Color?), PictureInfo> _decoded = {};

  /// The decodes still running, so that draws of one SVG at once decode it once.
  static final Map<(String, Color?), Future<PictureInfo>> _decoding = {};

  static void _clear() {
    _decoded.clear();
    _decoding.clear();
  }

  /// The tint put into each picture loaded here, for the pictures that have one.
  static final Expando<Color> _tints = Expando();

  /// The tint put into [picture] when it was loaded here; null when it is drawn as authored or the
  /// tint is still to be filtered on.
  static Color? tintOf(PictureInfo picture) => _tints[picture];
  static bool _watchingFonts = false;

  /// [svg]'s picture in [tint] (as it is drawn, when null), and whether the tint is still to be
  /// put on with a srcIn filter: when [SvgTint] cannot put it into the picture.
  static Future<(PictureInfo, bool)> load(String svg, Color? tint) async {
    final baked = tint == null ? null : SvgTint.loader(svg, tint);
    final key = (svg, baked == null ? null : tint);
    if (!_watchingFonts) _watchingFonts = FontUtils.listenToSystemFonts(_clear);
    final filtered = tint != null && baked == null;
    final kept = _decoded[key];
    if (kept != null) return (kept, filtered);
    if (_decoded.length >= 256) _decoded.clear();
    final future = _decoding[key] ??= vg.loadPicture(baked ?? SvgStringLoader(svg), null);
    try {
      final picture = await future;
      if (baked != null) _tints[picture] = tint;
      if (identical(_decoding[key], future)) _decoded[key] = picture;
      return (picture, filtered);
    } finally {
      if (identical(_decoding[key], future)) _decoding.remove(key);
    }
  }
}

class _TintMapper extends ColorMapper {
  const _TintMapper(this.tint);

  final Color tint;

  @override
  Color substitute(String? id, String elementName, String attributeName, Color color) =>
      tint.withValues(alpha: color.a);

  @override
  bool operator ==(Object other) => other is _TintMapper && other.tint == tint;

  @override
  int get hashCode => tint.hashCode;
}
