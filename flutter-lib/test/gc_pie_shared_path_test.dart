// A filled arc, a pie slice, is drawn from the path the drawer builds every polygon in: drawing records
// the geometry as it is then, so painting a slice makes no path of its own, and paints what a path
// made for it does.

import 'dart:math' as math;
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

class _Paths implements ui.Canvas {
  final List<Path> drawn = [];

  @override
  void drawPath(Path path, Paint paint) => drawn.add(path);

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

ArcShape _slice(double x, double start, double sweep, Color color) => ArcShape(
    Rect.fromLTWH(x, 4, 40, 40), start, sweep, color, 0, SWT.CAP_FLAT, SWT.JOIN_MITER,
    isFilled: true);

Future<Uint8List> _render(void Function(ui.Canvas) paint) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)..drawRect(const Rect.fromLTWH(0, 0, 100, 50), Paint()..color = Colors.white);
  paint(canvas);
  final image = await recorder.endRecording().toImage(100, 50);
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

void main() {
  test('two pie slices are drawn from one path', () {
    final c = _Paths();
    _slice(4, 0.3, 2.1, const Color(0xFFCC3322)).paintBody(c);
    _slice(52, -1, 4, const Color(0xFF2255CC)).paintBody(c);

    expect(c.drawn, hasLength(2));
    expect(identical(c.drawn[0], c.drawn[1]), isTrue);
  });

  test('pie slices drawn from the shared path paint what a path of their own does', () async {
    final slices = [
      _slice(4, 0.3, 2.1, const Color(0xFFCC3322)),
      _slice(52, -1, 4, const Color(0xFF2255CC)),
      _slice(30, 0, 2 * math.pi, const Color(0x8022AA44)),
    ];
    final shared = await _render((c) {
      for (final s in slices) {
        s.paintBody(c);
      }
    });
    final own = await _render((c) {
      for (final s in slices) {
        c.drawPath(piePath(s.rect, s.startAngle, s.sweepAngle),
            Paint()
              ..color = s.color
              ..isAntiAlias = true);
      }
    });

    expect(shared, own);
  });
}
