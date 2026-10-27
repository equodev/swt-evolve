import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const style = TextStyle(fontSize: 12);

  void drawOnce(TextShape shape) {
    final recorder = ui.PictureRecorder();
    shape.draw(ui.Canvas(recorder));
    recorder.endRecording().dispose();
  }

  test('a text shape keeps its layout across repaints', () {
    final shape = TextShape('cell 42', const Offset(3, 4), style);
    final painter = shape.painter;

    drawOnce(shape);
    drawOnce(shape);

    expect(identical(shape.painter, painter), isTrue);
  });

  test('a translated text shape reuses the original layout', () {
    final shape = TextShape('cell 42', const Offset(3, 4), style);
    final moved = shape.translated(const Offset(10, 20), const Rect.fromLTWH(0, 0, 100, 100));

    expect(identical(moved.painter, shape.painter), isTrue);
    expect(moved.off, const Offset(13, 24));
    expect(moved.clipRect, const Rect.fromLTWH(0, 0, 100, 100));
  });
}
