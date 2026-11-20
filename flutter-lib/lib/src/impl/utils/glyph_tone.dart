import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter/painting.dart';
import 'package:image/image.dart' as img;

import 'widget_utils.dart' show contrastRatio;

/// What reads as a glyph — an image small enough, and with little enough color, that it has none of
/// the application's to keep when it is drawn in the theme's colors.
class GlyphTintLimits {
  final int maxSide;
  final int channelTolerance;

  const GlyphTintLimits({required this.maxSide, required this.channelTolerance});

  // Used wherever no Canvas theme is resolved: control icons and the standalone image drawer.
  static const GlyphTintLimits fallback =
      GlyphTintLimits(maxSide: 64, channelTolerance: 4);
}

/// The grey range a monochrome glyph spans, over its visible pixels.
class GlyphTone {
  final int darkest;
  final int lightest;

  const GlyphTone({required this.darkest, required this.lightest});

  // Below this spread the glyph is one tone of ink, and its alpha alone says where the ink is.
  static const int _minPaperContrast = 64;

  /// Ink alpha per grey level below [lightest], so that [darkest] is full ink and [lightest] is
  /// paper. Null for a single-tone glyph.
  double? get _inkScale {
    final range = lightest - darkest;
    return range < _minPaperContrast ? null : 255 / range;
  }

  /// Alpha from darkness, by [_inkScale]. Null for a single-tone glyph.
  ColorFilter? get inkMask {
    final k = _inkScale;
    if (k == null) return null;
    return ColorFilter.matrix(<double>[
      0, 0, 0, 0, 0,
      0, 0, 0, 0, 0,
      0, 0, 0, 0, 0,
      -k, 0, 0, 0, k * lightest,
    ]);
  }

  static final Map<String, Uint8List?> _stencils = {};

  /// [encoded] as a PNG whose alpha is [inkMask]'s within the image's own alpha, for a tint that
  /// paints through alpha: the ink takes it and the paper drops out. Null for a single-tone glyph,
  /// or for bytes that are not a PNG or a GIF.
  Uint8List? inkStencil(Uint8List encoded) {
    final k = _inkScale;
    if (k == null) return null;
    // The tone is derived from the content, so the content alone keys the stencil.
    final key = contentKey(encoded);
    if (_stencils.containsKey(key)) return _stencils[key];

    final decoder = _decoderFor(encoded);
    final rgba = decoder == null ? null : _decodeRgba(decoder, encoded);
    Uint8List? stencil;
    if (rgba != null) {
      for (final p in rgba) {
        final ink = (k * (lightest - p.r)).round().clamp(0, 255);
        p
          ..r = 0
          ..g = 0
          ..b = 0
          ..a = ink * p.a.toInt() ~/ 255;
      }
      stencil = Uint8List.fromList(img.encodePng(rgba));
    }
    if (_stencils.length > 512) _stencils.clear();
    _stencils[key] = stencil;
    return stencil;
  }

  /// Whether the glyph's ink reads on [ground] as drawn, by WCAG 2's non-text contrast.
  bool readsOn(Color ground) =>
      contrastRatio(Color.fromARGB(255, darkest, darkest, darkest), ground) >= minGlyphContrast;

  static const double minGlyphContrast = 3.0;

  /// Whether a glyph painted in [color] reads on [ground]; see [readsOn].
  static bool colorReadsOn(Color color, Color ground) =>
      contrastRatio(color.withValues(alpha: 1), ground) >= minGlyphContrast;

  static final RegExp _svgPaint = RegExp(
      r'''(?:fill|stroke|stop-color)\s*(?:=\s*["']|:\s*)([^"';>]+)''',
      caseSensitive: false);
  static final Map<String, GlyphTone?> _svgVerdicts = {};

  /// The tone of an SVG whose every paint is one grey, or null when any paint is a color, a
  /// gradient or a value this does not read. An SVG that names no paint draws in black.
  static GlyphTone? ofSvg(String svg, int channelTolerance) {
    final key = '${svg.hashCode}-${svg.length}-$channelTolerance';
    if (_svgVerdicts.containsKey(key)) return _svgVerdicts[key];

    final greys = <int>[];
    var monochrome = true;
    for (final match in _svgPaint.allMatches(svg)) {
      final value = match.group(1)!.trim().toLowerCase();
      if (value == 'none' || value == 'transparent') continue;
      final grey = _svgGrey(value, channelTolerance);
      if (grey == null) {
        monochrome = false;
        break;
      }
      greys.add(grey);
    }
    final tone = !monochrome
        ? null
        : greys.isEmpty
        ? const GlyphTone(darkest: 0, lightest: 0)
        : GlyphTone(darkest: greys.reduce(math.min), lightest: greys.reduce(math.max));
    if (_svgVerdicts.length > 512) _svgVerdicts.clear();
    _svgVerdicts[key] = tone;
    return tone;
  }

  // flutter_svg resolves currentColor to black unless a theme says otherwise.
  static int? _svgGrey(String value, int channelTolerance) {
    if (value == 'black' || value == 'currentcolor') return 0;
    if (value == 'white') return 255;
    if (!value.startsWith('#')) return null;
    var hex = value.substring(1);
    if (hex.length == 3 || hex.length == 4) hex = hex.split('').map((c) => '$c$c').join();
    if (hex.length != 6 && hex.length != 8) return null;
    final rgb = int.tryParse(hex.substring(0, 6), radix: 16);
    if (rgb == null) return null;
    final r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
    if (math.max(r, math.max(g, b)) - math.min(r, math.min(g, b)) > channelTolerance) return null;
    return r;
  }

  /// The tone of RGBA pixels, or null when a visible pixel carries color.
  static GlyphTone? scan(Uint8List rgba, int channelTolerance, {required bool premultiplied}) {
    var darkest = 255;
    var lightest = 0;
    for (var i = 0; i + 3 < rgba.length; i += 4) {
      final a = rgba[i + 3];
      if (a == 0) continue;
      var r = rgba[i], g = rgba[i + 1], b = rgba[i + 2];
      // Compared premultiplied: alpha scales all three channels alike, so grey stays grey.
      if (!premultiplied) {
        r = r * a ~/ 255;
        g = g * a ~/ 255;
        b = b * a ~/ 255;
      }
      if (math.max(r, math.max(g, b)) - math.min(r, math.min(g, b)) > channelTolerance) {
        return null;
      }
      final grey = math.min(255, r * 255 ~/ a);
      darkest = math.min(darkest, grey);
      lightest = math.max(lightest, grey);
    }
    return GlyphTone(darkest: darkest, lightest: lightest);
  }

  static final Map<String, GlyphTone?> _encodedVerdicts = {};

  /// The tone of an encoded PNG or GIF, or null when it is not a glyph. Only an image within
  /// [GlyphTintLimits.maxSide] is decoded. Other formats never read as one: JPEG is lossy, and
  /// naming the decoders keeps the unused ones out of the bundle.
  static GlyphTone? ofEncoded(Uint8List bytes, GlyphTintLimits limits) {
    final key = '${contentKey(bytes)}-${limits.maxSide}x${limits.channelTolerance}';
    if (_encodedVerdicts.containsKey(key)) return _encodedVerdicts[key];

    GlyphTone? tone;
    try {
      final decoder = _decoderFor(bytes);
      final info = decoder?.startDecode(bytes);
      if (info != null &&
          info.width <= limits.maxSide &&
          info.height <= limits.maxSide) {
        final decoded = _decodeRgba(decoder!, bytes);
        if (decoded != null) {
          final rgba = decoded.getBytes(order: img.ChannelOrder.rgba);
          tone = scan(rgba, limits.channelTolerance, premultiplied: false);
        }
      }
    } catch (_) {
      tone = null;
    }
    // The key is content-derived, so the map would otherwise grow without bound.
    if (_encodedVerdicts.length > 512) _encodedVerdicts.clear();
    _encodedVerdicts[key] = tone;
    return tone;
  }

  /// [bytes] decoded to 8-bit RGBA, or null when [decoder] cannot read them.
  static img.Image? _decodeRgba(img.Decoder decoder, Uint8List bytes) =>
      decoder.decode(bytes)?.convert(format: img.Format.uint8, numChannels: 4);

  static img.Decoder? _decoderFor(Uint8List bytes) {
    if (bytes.length < 8) return null;
    if (bytes[0] == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
      return img.PngDecoder();
    }
    if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46) return img.GifDecoder();
    return null;
  }

  /// A key that tells two encoded images apart by their whole content, not only their length.
  static String contentKey(Uint8List bytes) {
    var hash = 0;
    final wordCount = bytes.length ~/ 4;
    // A Uint32List view needs a 4-byte-aligned offset; ByteData reads the same words from any other.
    if (bytes.offsetInBytes % 4 == 0) {
      for (final word in bytes.buffer.asUint32List(bytes.offsetInBytes, wordCount)) {
        hash = (hash * 31 + word) & 0x3FFFFFFF;
      }
    } else {
      final view = ByteData.sublistView(bytes);
      for (var w = 0; w < wordCount; w++) {
        hash = (hash * 31 + view.getUint32(w * 4, Endian.host)) & 0x3FFFFFFF;
      }
    }
    for (var i = wordCount * 4; i < bytes.length; i++) {
      hash = (hash * 31 + bytes[i]) & 0x3FFFFFFF;
    }
    return 'bin-${bytes.length}-$hash';
  }
}
