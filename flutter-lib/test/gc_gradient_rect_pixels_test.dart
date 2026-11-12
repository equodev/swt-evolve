// A gradient rect is painted as a clipped paint rather than as a rect filled by the shader; the pixels
// are those of the shader-filled rect.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const from = Color(0xFF1E64C8);
  const to = Color(0xFFF0C814);

  Future<Uint8List> pixels(void Function(ui.Canvas) paint, double scale) async {
    final recorder = ui.PictureRecorder();
    final canvas = ui.Canvas(recorder);
    canvas.scale(scale);
    paint(canvas);
    final image = await recorder.endRecording().toImage((120 * scale).ceil(), (90 * scale).ceil());
    final bytes = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
    image.dispose();
    return bytes!.buffer.asUint8List();
  }

  void shaderFilledRect(ui.Canvas c, Rect rect, bool vertical, Rect? clip) {
    if (clip != null) {
      c.save();
      c.clipRect(clip);
    }
    final gradient = LinearGradient(
      begin: vertical ? Alignment.topCenter : Alignment.centerLeft,
      end: vertical ? Alignment.bottomCenter : Alignment.centerRight,
      colors: const [from, to],
    );
    c.drawRect(rect, Paint()..shader = gradient.createShader(rect));
    if (clip != null) c.restore();
  }

  for (final scale in [1.0, 2.0]) {
    for (final vertical in [true, false]) {
      for (final clip in [null, const Rect.fromLTWH(20, 15, 50, 40)]) {
        testWidgets('scale $scale, ${vertical ? "vertical" : "horizontal"}, ${clip == null ? "unclipped" : "clipped"}',
            (tester) async {
          const rect = Rect.fromLTWH(10, 8, 90, 64);
          await tester.runAsync(() async {
            final shape = GradientRectShape(rect, from, to, vertical, clip);
            final drawn = await pixels(shape.draw, scale);
            final expected = await pixels((c) => shaderFilledRect(c, rect, vertical, clip), scale);
            expect(drawn, expected);
          });
        });
      }
    }
  }
}
