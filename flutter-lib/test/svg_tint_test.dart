import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/painting.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/svg_tint.dart';

/// [loader]'s picture drawn at 2x into a 32x32 image, under [filter] when one is given.
Future<Uint8List> _pixels(SvgStringLoader loader, {ColorFilter? filter}) async {
  final info = await vg.loadPicture(loader, null);
  final recorder = ui.PictureRecorder();
  final canvas = Canvas(recorder);
  canvas.scale(32 / info.size.width, 32 / info.size.height);
  if (filter != null) canvas.saveLayer(null, Paint()..colorFilter = filter);
  canvas.drawPicture(info.picture);
  if (filter != null) canvas.restore();
  final image = await recorder.endRecording().toImage(32, 32);
  final data = await image.toByteData(format: ui.ImageByteFormat.rawStraightRgba);
  info.picture.dispose();
  image.dispose();
  return data!.buffer.asUint8List();
}

/// The largest difference of any channel of any pixel.
int _maxDifference(Uint8List a, Uint8List b) {
  var most = 0;
  for (var i = 0; i < a.length; i++) {
    final d = (a[i] - b[i]).abs();
    if (d > most) most = d;
  }
  return most;
}

const _tint = Color(0xFF1E6FD9);

const _icons = <String, String>{
  'no fill set anywhere':
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><path d="M2 2h12v12H2z"/></svg>',
  'fill on the root':
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="#ff0000"><circle cx="8" cy="8" r="6"/></svg>',
  'colours of its own, overlapping':
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><rect x="1" y="1" width="10" height="10" fill="#00ff00"/>'
          '<rect x="5" y="5" width="10" height="10" fill="#0000ff"/></svg>',
  'translucent elements, overlapping':
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><rect x="1" y="1" width="10" height="10" fill="#000" fill-opacity="0.4"/>'
          '<rect x="5" y="5" width="10" height="10" fill="#333" opacity="0.5"/></svg>',
  'currentColor and a stroke':
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><path d="M2 8h12" stroke="currentColor" stroke-width="2"/>'
          '<circle cx="8" cy="8" r="3" fill="none" stroke="#123456"/></svg>',
  'a style attribute':
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><path style="fill:#abcdef" d="M2 2h12v12H2z"/></svg>',
  'a gradient':
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><defs><linearGradient id="g"><stop offset="0" stop-color="#f00"/>'
          '<stop offset="1" stop-color="#00f" stop-opacity="0.3"/></linearGradient></defs><rect width="16" height="16" fill="url(#g)"/></svg>',
};

void main() {
  group('an SVG tinted as it is parsed draws what the srcIn filter draws', () {
    for (final entry in _icons.entries) {
      testWidgets(entry.key, (tester) async {
        await tester.runAsync(() async {
          final baked = SvgTint.loader(entry.value, _tint);
          expect(baked, isNotNull, reason: 'one colour, opaque tint: baked');
          final filtered = await _pixels(SvgStringLoader(entry.value),
              filter: const ColorFilter.mode(_tint, BlendMode.srcIn));
          final tinted = await _pixels(baked!);
          // A few levels apart at most, at translucent pixels: the filter rounds its alpha at another
          // step of the composition, and a straight readback divides colour by that alpha.
          expect(_maxDifference(filtered, tinted), lessThanOrEqualTo(3));
        });
      });
    }
  });

  test('a translucent tint keeps the filter: it would compose once per overlapping element', () {
    expect(SvgTint.loader(_icons['no fill set anywhere']!, _tint.withValues(alpha: 0.5)), isNull);
  });

  test('an SVG that is not one colour keeps the filter', () {
    for (final svg in [
      '<svg viewBox="0 0 16 16"><image href="x.png" width="16" height="16"/></svg>',
      '<svg viewBox="0 0 16 16"><mask id="m"><rect width="8" height="8" fill="#fff"/></mask><rect width="16" height="16" mask="url(#m)"/></svg>',
      '<svg viewBox="0 0 16 16"><rect width="16" height="16" style="mix-blend-mode:multiply"/></svg>',
    ]) {
      expect(SvgTint.loader(svg, _tint), isNull, reason: svg);
    }
  });

  testWidgets('a GC decodes an SVG once per tint, and is told when the tint is still to be filtered on',
      (tester) async {
    await tester.runAsync(() async {
      final svg = _icons['no fill set anywhere']!;
      final (first, firstFiltered) = await SvgPictures.load(svg, _tint);
      final (again, _) = await SvgPictures.load(svg, _tint);
      final (other, _) = await SvgPictures.load(svg, const Color(0xFF000000));
      final (translucent, translucentFiltered) = await SvgPictures.load(svg, _tint.withValues(alpha: 0.5));
      final (untinted, untintedFiltered) = await SvgPictures.load(svg, null);

      expect(identical(first, again), isTrue, reason: 'decoded once');
      expect(identical(first, other), isFalse, reason: 'another tint is another picture');
      expect(firstFiltered, isFalse, reason: 'an opaque tint is in the picture');
      expect(translucentFiltered, isTrue, reason: 'a translucent tint is filtered on');
      expect(identical(translucent, untinted), isTrue, reason: 'both are the SVG as drawn');
      expect(untintedFiltered, isFalse);
    });
  });

  test('the same SVG and tint load as the same picture, so the SVG cache keeps one', () {
    final svg = _icons['no fill set anywhere']!;
    expect(SvgTint.loader(svg, _tint), SvgTint.loader(svg, _tint));
    expect(SvgTint.loader(svg, _tint), isNot(SvgTint.loader(svg, const Color(0xFF000000))));
  });
}
