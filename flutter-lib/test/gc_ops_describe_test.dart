// A paint op's description reads the same whichever compiler built the app: the screenshot gate
// compares these strings across builds.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  test('a whole stroke width has no fraction', () {
    final rect = RectShape(const Rect.fromLTWH(1, 2, 3, 4), const Color(0xff000000), 0.0, 1, 1, isFilled: true);
    expect(rect.describe(), contains('w=0 '));
  });

  test('a gradient names its alignments', () {
    final gradient = GradientRectShape.aligned(const Rect.fromLTWH(0, 0, 10, 10), const Color(0xffff0000),
        const Color(0xff00ff00), Alignment.topCenter, Alignment.bottomCenter);
    expect(gradient.describe(), contains('Alignment.topCenter->Alignment.bottomCenter'));
  });
}
