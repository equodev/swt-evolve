// A run of ovals is one atlas draw and paints what each oval drawn alone paints, to within the
// coverage kept in eight bits.

import 'dart:math' as math;
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart' show getConfigFlags;

class _Calls implements ui.Canvas {
  _Calls([this.matrix]);
  final Float64List? matrix;
  final List<String> log = [];
  /// For each atlas draw, the image's height and the lowest edge of the sprites it is drawn from.
  final List<(int, double)> atlases = [];
  /// The sprites drawn from, by their left and top in the image.
  final Set<(double, double)> sprites = {};

  @override
  void drawOval(Rect rect, Paint paint) => log.add('oval');

  @override
  void drawRawAtlas(ui.Image atlas, Float32List rstTransforms, Float32List rects, Int32List? colors,
      BlendMode? blendMode, Rect? cullRect, Paint paint) {
    log.add('atlas ${rects.length ~/ 4}');
    var bottom = 0.0;
    for (var i = 3; i < rects.length; i += 4) {
      if (rects[i] > bottom) bottom = rects[i];
    }
    atlases.add((atlas.height, bottom));
    for (var i = 0; i + 1 < rects.length; i += 4) {
      sprites.add((rects[i], rects[i + 1]));
    }
  }

  @override
  Float64List getTransform() => matrix ?? Float64List.fromList([1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]);

  @override
  dynamic noSuchMethod(Invocation invocation) => null;
}

const _size = 100;

Future<Uint8List> _render(double scale, void Function(ui.Canvas) paint) async {
  final recorder = ui.PictureRecorder();
  final canvas = ui.Canvas(recorder)
    ..drawRect(Rect.fromLTWH(0, 0, _size * scale, _size * scale), Paint()..color = const Color(0xFFFFFFFF))
    ..scale(scale);
  paint(canvas);
  final image = await recorder.endRecording().toImage((_size * scale).round(), (_size * scale).round());
  return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
}

Future<int> _worst(List<Shape> shapes, double scale) async {
  final run = await _render(scale, (c) => drawShapeRun(c, shapes));
  final apart = await _render(scale, (c) {
    for (final s in shapes) {
      s.draw(c);
    }
  });
  var worst = 0;
  for (var i = 0; i < run.length; i++) {
    final d = (run[i] - apart[i]).abs();
    if (d > worst) worst = d;
  }
  return worst;
}

void main() {
  const red = Color(0xFFD02020), blue = Color(0x800030C0), green = Color(0xB010A040);

  // gc.scatter's dots, filled then outlined, and gc.alpha's overlapping translucent ellipses, in
  // strokes two to five pixels wide: odd widths sit on pixel centres.
  List<Shape> ovals() => [
        for (var i = 0; i < 8; i++) ...[
          OvalShape(Rect.fromLTWH(4.0 + i * 11, 4, 7, 7), red, 0, isFilled: true),
          OvalShape(Rect.fromLTWH(4.0 + i * 11, 4, 7, 7), blue, 2.0 + i % 4),
        ],
        OvalShape(const Rect.fromLTWH(10, 20, 50, 30), blue, 0, isFilled: true),
        OvalShape(const Rect.fromLTWH(30, 30, 40, 45), green, 0, isFilled: true),
        OvalShape(const Rect.fromLTWH(20, 50, 33, 21), red.withAlpha(140), 3),
        OvalShape(const Rect.fromLTWH(60, 60, 30, 30), blue, 2),
      ];

  test('consecutive ovals of any paint, size and stroke are one atlas draw', () {
    final calls = _Calls();
    drawShapeRun(calls, ovals());

    expect(calls.log, ['atlas 20']);
  });

  for (final scale in [1.0, 2.0]) {
    test('as one atlas draw they paint what drawing each does, but for eight-bit coverage, at scale $scale',
        () async {
      expect(await _worst(ovals(), scale), lessThanOrEqualTo(8));
    });
  }

  test('ovals drawn again, as a GC repaints them, are not drawn into the atlas again', () async {
    final first = await _render(2, (c) => drawShapeRun(c, ovals()));
    final builds = ovalAtlasBuilds;
    final again = await _render(2, (c) => drawShapeRun(c, ovals()));

    expect(ovalAtlasBuilds, builds);
    expect(again, first);
  });

  test('a thousand ovals of mixed sizes in any order fit the atlas once, and drawn again build nothing', () {
    // gc.alpha's ellipses, 10 to 89 pixels each way in random order: in drawing order, rows as tall as
    // their tallest sprite fill the image before every sprite is in, and each repaint built it again.
    final r = math.Random(42);
    final shapes = [
      for (var i = 0; i < 1000; i++)
        OvalShape(Rect.fromLTWH(r.nextInt(1024).toDouble(), r.nextInt(768).toDouble(), 10.0 + r.nextInt(80),
            10.0 + r.nextInt(80)), blue, 0, isFilled: true),
    ];
    final first = _Calls();
    drawShapeRun(first, shapes);
    final builds = ovalAtlasBuilds;
    final again = _Calls();
    drawShapeRun(again, shapes);

    expect(first.log, ['atlas 1000']);
    expect(again.log, ['atlas 1000']);
    expect(ovalAtlasBuilds, builds);
  });

  group('where Impeller draws', () {
    // Impeller's oval shader bends edge coverage towards the colour's luma: a sprite is drawn in a
    // grey of its oval's luma and kept as coverage alone.
    setUp(() => debugMultisampled = true);
    tearDown(() => debugMultisampled = null);
    List<Shape> sameSize(List<Color> colors) => [
          for (var i = 0; i < colors.length; i++)
            OvalShape(Rect.fromLTWH(4.0 + i * 11, 4, 9, 9), colors[i], 0, isFilled: true),
        ];

    test('ovals of one size share a sprite per luma step', () {
      const black = Color(0xFF000000), white = Color(0xFFFFFFFF), almostBlack = Color(0xFF010101);
      final calls = _Calls();
      drawShapeRun(calls, sameSize([black, white, almostBlack, white, black, almostBlack]));
      expect(calls.log, ['atlas 6']);
      expect(calls.sprites, hasLength(2));
    });

    for (final scale in [1.0, 2.0]) {
      test('grey sprites tinted by their colours paint what each oval drawn alone does, at scale $scale',
          () async {
        expect(await _worst(ovals(), scale), lessThanOrEqualTo(8));
      });
    }
  });

  test('a hairline oval ends the run and is drawn on its own', () {
    final calls = _Calls();
    drawShapeRun(calls, [...ovals().sublist(0, 4), OvalShape(const Rect.fromLTWH(0, 0, 10, 10), blue, 0)]);

    expect(calls.log, ['atlas 4', 'oval']);
  });

  test('an oval larger than a sprite ends the run and is drawn on its own', () {
    final calls = _Calls();
    drawShapeRun(calls, [
      ...ovals().sublist(0, 4),
      OvalShape(const Rect.fromLTWH(0, 0, 300, 200), red, 0, isFilled: true),
    ]);

    expect(calls.log, ['atlas 4', 'oval']);
  });

  test('ovals too many for the atlas image go as several atlas draws, each from inside its image', () {
    // 2,300 distinct sprites up to 124 device pixels across: rows of them reach far past 2,048 pixels.
    final calls = _Calls();
    drawShapeRun(calls, [
      for (var i = 0; i < 2300; i++)
        OvalShape(Rect.fromLTWH((i ~/ 784) * 0.25, 0, 97.0 + i % 28, 97.0 + i ~/ 28 % 28), red, 0, isFilled: true),
    ]);

    for (final (height, bottom) in calls.atlases) {
      expect(height, lessThanOrEqualTo(2048));
      expect(bottom, lessThanOrEqualTo(height));
    }
    expect(calls.atlases.length, greaterThan(1));
    expect(calls.log.where((call) => call == 'oval'), isEmpty);
  });

  test('a few ovals are drawn one by one, which costs less than an atlas draw', () {
    final calls = _Calls();
    drawShapeRun(calls, ovals().sublist(0, 3));

    expect(calls.log, ['oval', 'oval', 'oval']);
  });

  test('under a rotation ovals are drawn on their own', () {
    final calls = _Calls(Float64List.fromList([0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]));
    drawShapeRun(calls, ovals().sublist(0, 3));

    expect(calls.log, ['oval', 'oval', 'oval']);
  });

  test('without approximate batching, ovals are each drawn on their own', () {
    getConfigFlags().gc_approximate_batching = false;
    try {
      final calls = _Calls();
      drawShapeRun(calls, ovals().sublist(0, 3));

      expect(calls.log, ['oval', 'oval', 'oval']);
    } finally {
      getConfigFlags().gc_approximate_batching = null;
    }
  });
}
