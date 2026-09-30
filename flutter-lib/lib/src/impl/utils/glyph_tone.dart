import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter/painting.dart';
import 'package:image/image.dart' as img;

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

  /// Alpha from darkness: [darkest] is full ink, [lightest] is paper. Null for a single-tone glyph.
  ColorFilter? get inkMask {
    final range = lightest - darkest;
    if (range < _minPaperContrast) return null;
    final k = 255 / range;
    return ColorFilter.matrix(<double>[
      0, 0, 0, 0, 0,
      0, 0, 0, 0, 0,
      0, 0, 0, 0, 0,
      -k, 0, 0, 0, k * lightest,
    ]);
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
        final decoded = decoder!.decode(bytes);
        if (decoded != null) {
          final rgba = decoded
              .convert(format: img.Format.uint8, numChannels: 4)
              .getBytes(order: img.ChannelOrder.rgba);
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
    for (final byte in bytes) {
      hash = (hash * 31 + byte) & 0x3FFFFFFF;
    }
    return 'bin-${bytes.length}-$hash';
  }
}
