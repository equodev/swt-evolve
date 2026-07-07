// tool/font_metrics_core.dart
//
// Platform-agnostic font-measurement + Java-source-generation core shared by tool/gen_fonts.dart
// (desktop, measures with the desktop Skia renderer, writes files via dart:io) and
// tool/gen_fonts_web.dart (web, measures with CanvasKit in a real browser, prints to stdout — no
// dart:io, which isn't available for a web compile target).
import 'dart:math';
import 'package:flutter/widgets.dart';
import 'package:flutter/rendering.dart';

const BASE_SIZE = 10;
const DECIMALS = 4; // Precision for all double comparisons and output

/// Measures every (font, style, weight) combination in [fonts] and returns the per-font
/// ascent/descent/height regression factors, average glyph width, and (unless monospace) the
/// per-glyph width table — the same shape `generateJava` expects.
Map<String, dynamic> measureFonts(List<(String, FontStyle, FontWeight)> fonts) {
  final sizes = [4, 6, 8, BASE_SIZE, 17, 20, 25, 40, 80, 160];

  // glyph set: printable ASCII 32..126 (space .. ~). Add more codepoints if you want.
  final glyphs = List<int>.generate(126 - 32 + 1, (i) => i + 32);
  final sampleText = glyphs.map(String.fromCharCode).join();
  print("Using sample: $sampleText");

  final Map<String, dynamic> out = {};
  final Map<String, List<String>> fontKeysByFont = {};
  for (final (font, style, weight) in fonts) {
    final k = "$font-${styleKey(style)}-${weightKey(weight)}";
    fontKeysByFont.putIfAbsent(font, () => []).add(k);
  }
  for (final (font, style, weight) in fonts) {
    final xs = <double>[];
    final asc = <double>[];
    final dsc = <double>[];
    final hgt = <double>[];

    Map<String, double>? baseGlyphs;

    for (final size in sizes) {
      final double fontSize = size.toDouble();

      // Compute global metrics (ascent/descent/height) using a sample text.
      var textStyle = TextStyle(
        fontFamily: font,
        fontSize: fontSize,
        fontWeight: weight,
        fontStyle: style,
      );
      final samplePainter = TextPainter(
        text: TextSpan(text: sampleText, style: textStyle),
        textDirection: TextDirection.ltr,
      )..layout();

      final lm = samplePainter.computeLineMetrics().first;
      xs.add(fontSize);
      asc.add(lm.ascent);
      dsc.add(lm.descent);
      hgt.add(lm.height);

      if (size == BASE_SIZE) {
        final m = <String, double>{};
        for (final cp in glyphs) {
          final p = TextPainter(
            text: TextSpan(text: String.fromCharCode(cp), style: textStyle),
            textDirection: TextDirection.ltr,
          )..layout();
          m['$cp'] = p.width;
        }
        baseGlyphs = m;
      }

      print(
        'Collected $font $weight $style @ $size -> ascent:${lm.ascent.toStringAsFixed(2)} descent:${lm.descent.toStringAsFixed(2)} height:${lm.height.toStringAsFixed(2)}',
      );
    }
    final avgWidth =
        baseGlyphs!.values.reduce((a, b) => a + b) / baseGlyphs.length;
    final isMonospace = baseGlyphs!.values.every(
      (e) => e == baseGlyphs!.values.first,
    );

    var ascentFactor = lr(xs, asc);
    var descentFactor = lr(xs, dsc);
    var heightFactor = lr(xs, hgt);
    var fk = "$font-${styleKey(style)}-${weightKey(weight)}";
    out[fk] = {
      if (!isMonospace) "glyphs": baseGlyphs,
      "ascent": ascentFactor,
      "descent": descentFactor,
      "height": heightFactor,
      "avgWidth": avgWidth,
    };

    var fontVariations = fontKeysByFont[font]!
        .where((k) => k != fk)
        .toList();

    for (var variation in fontVariations) {
      if (out.containsKey(variation) &&
          _doubleEquals(out[variation]["avgWidth"], avgWidth, DECIMALS) &&
          _mapEquals(out[variation]["height"], heightFactor, DECIMALS)) {
        print(
          "WARNING DUPLICATE FONT METRICS $variation == $fk (optimize!)",
        );
      }
    }
    print(
      'Collected $font $weight $style -> ascentFactor:${ascentFactor} descent:${descentFactor} height:${heightFactor} avgWidth: $avgWidth',
    );
  }

  return out;
}

int weightKey(FontWeight weight) => weight.index;

int styleKey(FontStyle style) => style.index;

Map<String, double> lr(List<double> x, List<double> y) {
  final n = x.length;
  double sx = 0, sy = 0, sxy = 0, sxx = 0;
  for (var i = 0; i < n; i++) {
    sx += x[i];
    sy += y[i];
    sxy += x[i] * y[i];
    sxx += x[i] * x[i];
  }
  final m = (n * sxy - sx * sy) / (n * sxx - sx * sx);
  final b = (sy - m * sx) / n;
  return {"m": m, "b": b};
}

String _double(Object? v, {int decimals = DECIMALS}) {
  if (v is num) {
    // Truncate to specified decimal places for reduced precision
    final truncated = (v * pow(10, decimals)).truncate() / pow(10, decimals);
    return truncated.toStringAsFixed(decimals);
  }
  return '0.0';
}

double _truncate(double v, int decimals) {
  return (v * pow(10, decimals)).truncate() / pow(10, decimals);
}

bool _doubleEquals(double a, double b, int decimals) {
  return _truncate(a, decimals) == _truncate(b, decimals);
}

bool _mapEquals(
  Map<String, dynamic>? a,
  Map<String, dynamic>? b,
  int decimals,
) {
  if (a == null && b == null) return true;
  if (a == null || b == null) return false;
  if (a.length != b.length) return false;

  for (var key in a.keys) {
    if (!b.containsKey(key)) return false;
    final aVal = a[key];
    final bVal = b[key];
    if (aVal is num && bVal is num) {
      if (!_doubleEquals(aVal.toDouble(), bVal.toDouble(), decimals)) {
        return false;
      }
    } else if (aVal != bVal) {
      return false;
    }
  }
  return true;
}

/// Renders [data] (as produced by [measureFonts]) as a standalone `GenFontMetrics.java` source
/// (desktop) — or, for the web entrypoint, the caller extracts just the entries it needs from this
/// output rather than using the whole class body.
String generateJava(Map<String, dynamic> data) {
  final sb = StringBuffer();

  sb.writeln('package dev.equo.swt;');
  sb.writeln();
  sb.writeln('import java.util.*;');
  sb.writeln();
  sb.writeln('public final class GenFontMetrics {');
  sb.writeln('    static final int BASE = $BASE_SIZE;');
  sb.writeln('    static final int GLYPH_START = 32; // First printable ASCII');
  sb.writeln('    private GenFontMetrics() {}');
  sb.writeln();
  sb.writeln(
    '    public static final Map<String, Metrics> DATA = new HashMap<>();',
  );
  sb.writeln();
  sb.writeln('    static {');

  data.forEach((font, metrics) {
    final fm = metrics as Map<String, dynamic>;
    final ascent = _double(fm['ascent']['m']);
    final descent = _double(fm['descent']['m']);
    final height = _double(fm['height']['m']);
    final avg = _double(fm['avgWidth']);
    final glyphs = fm['glyphs'] as Map<String, dynamic>?;
    fm['processed'] = true;
    var dupGlyphsFont = data.entries
        .where(
          (e) =>
              e.key != font &&
              (e.value as Map<String, dynamic>)["processed"] == true,
        )
        .where(
          (e) => _doubleEquals(
            (e.value as Map<String, dynamic>)["avgWidth"],
            fm['avgWidth'],
            DECIMALS,
          ),
        )
        .where(
          (e) => _mapEquals(
            glyphs,
            ((e.value as Map<String, dynamic>)["glyphs"]
                as Map<String, dynamic>?),
            DECIMALS,
          ),
        )
        .map((e) => e.key)
        .firstOrNull;
    final glyphsArg = switch (glyphs) {
      null => "null",
      _ =>
        (dupGlyphsFont != null)
            ? 'DATA.get("$dupGlyphsFont").glyphWidths()'
            : '${jMethod(font)}()',
    };
    if (dupGlyphsFont != null) {
      fm.remove('glyphs');
    }
    sb.writeln(
      '        DATA.put("${fontKey(font)}", new Metrics($ascent, $descent, $height, $avg, $glyphsArg));',
    );
  });
  sb.writeln('    }');

  data.forEach((font, metrics) {
    final fm = metrics as Map<String, dynamic>;
    final glyphs = fm['glyphs'] as Map<String, dynamic>?;

    if (glyphs != null) {
      sb.writeln('    private static double[] ${jMethod(font)}() {');
      sb.writeln('        return new double[] {');

      // Build array from codepoints 32-126
      for (int cp = 32; cp <= 126; cp++) {
        final width = glyphs['$cp'];
        final w = _double(width);
        sb.write('            $w');
        if (cp != 126) {
          sb.write(',');
        }
        sb.writeln('');
      }
      sb.writeln('        };');
      sb.writeln('    }');
    }
  });

  sb.writeln('}');
  return sb.toString();
}

String jMethod(String font) => font.replaceAll(RegExp('[\\s-.]'), '_');

String fontKey(String font) => font.replaceAll('"', '\\"');
