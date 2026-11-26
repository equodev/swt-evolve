// A gradient repainted frame after frame is drawn with the shader made the first time.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// Keeps the shader each paint was drawn with.
class _Calls implements ui.Canvas {
  final List<ui.Shader?> shaders = [];

  @override
  void drawPaint(Paint paint) => shaders.add(paint.shader);

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

Future<Uint8List> _render(void Function(ui.Canvas) paint) async {
  final recorder = ui.PictureRecorder();
  paint(ui.Canvas(recorder));
  final image = await recorder.endRecording().toImage(60, 40);
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

void main() {
  const from = Color(0xFF2060A0), to = Color(0xFFE0C040);
  const rect = Rect.fromLTWH(5, 4, 40, 30);

  ui.Shader? shaderOf(Shape shape) {
    final calls = _Calls();
    shape.draw(calls);
    return calls.shaders.single;
  }

  test('the same gradient drawn again uses the same shader', () {
    final first = shaderOf(GradientRectShape(rect, from, to, true));
    expect(first, isNotNull);
    expect(identical(shaderOf(GradientRectShape(rect, from, to, true)), first), isTrue);
  });

  test('another rectangle, colour or direction gets a shader of its own', () {
    final base = shaderOf(GradientRectShape(rect, from, to, true));
    for (final (name, shape) in [
      ('moved', GradientRectShape(rect.shift(const Offset(1, 0)), from, to, true)),
      ('from', GradientRectShape(rect, const Color(0xFF2060A1), to, true)),
      ('to', GradientRectShape(rect, from, const Color(0xFFE0C041), true)),
      ('direction', GradientRectShape(rect, from, to, false)),
    ]) {
      expect(identical(shaderOf(shape), base), isFalse, reason: name);
    }
  });

  test('a gradient drawn again paints what a shader made for it paints', () async {
    for (final vertical in [true, false]) {
      GradientRectShape(rect, from, to, vertical).draw(_Calls());
      final drawn = await _render((c) => GradientRectShape(rect, from, to, vertical).draw(c));
      final fresh = await _render((c) {
        c.save();
        c.clipRect(rect);
        c.drawPaint(Paint()
          ..shader = LinearGradient(
            begin: vertical ? Alignment.topCenter : Alignment.centerLeft,
            end: vertical ? Alignment.bottomCenter : Alignment.centerRight,
            colors: const [from, to],
          ).createShader(rect));
        c.restore();
      });
      expect(drawn, fresh, reason: 'vertical $vertical');
    }
  });
}
