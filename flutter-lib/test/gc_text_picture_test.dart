// A GC's text is recorded once per layout, at the origin, and drawn as that picture moved to the
// text's place: it paints what painting the paragraph there does, and a layout drawn again records
// nothing more.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

class _Calls implements ui.Canvas {
  final List<String> log = [];

  @override
  void drawParagraph(ui.Paragraph paragraph, Offset offset) => log.add('paragraph');

  @override
  void drawPicture(ui.Picture picture) => log.add('picture');

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

Future<Uint8List> _render(double scale, void Function(ui.Canvas) paint) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)
    ..drawRect(Rect.fromLTWH(0, 0, 120 * scale, 60 * scale), Paint()..color = const Color(0xFFFFFFFF))
    ..scale(scale);
  paint(canvas);
  final image = await recorder.endRecording().toImage((120 * scale).round(), (60 * scale).round());
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

void main() {
  const style = TextStyle(fontSize: 13, color: Color(0xFF203040));

  for (final scale in [1.0, 2.0]) {
    for (final off in [const Offset(4, 6), const Offset(10.5, 20.5)]) {
      test('text drawn as its picture paints what painting its paragraph does, at $off, scale $scale', () async {
        final shape = TextShape('Node 12 (3)', off, style);
        final asPicture = await _render(scale, shape.paintBody);
        final asParagraph = await _render(scale, (c) => shape.painter.paint(c, off));

        expect(asPicture, asParagraph);
      });
    }
  }

  test('a layout drawn again draws the picture it recorded, and records the paragraph no more', () {
    final first = _Calls(), again = _Calls();
    TextShape('Monday', const Offset(1, 2), style).paintBody(first);
    TextShape('Monday', const Offset(30, 40), style).paintBody(again);

    expect(first.log, ['picture']);
    expect(again.log, ['picture']);
  });
}
