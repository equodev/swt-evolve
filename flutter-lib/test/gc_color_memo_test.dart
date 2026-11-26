// A paint draws with a handful of colours, alternating between them op by op (a chart's series, its
// grid and its labels): each is made once and handed out again, in its own colour, whatever the order.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

VColor _colour(int r, int g, int b) => VColor.empty()
  ..alpha = 255
  ..red = r
  ..green = g
  ..blue = b;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('a paint cycling through six colours gets each in its own colour, made once', () {
    final d = GCDrawer.embedded(VGC.empty()..id = 851101, onShapesUpdated: (_) {});
    addTearDown(d.dispose);
    final rgbs = [(200, 30, 30), (30, 160, 60), (40, 40, 40), (220, 220, 220), (0, 90, 200), (250, 180, 0)];

    final first = <Color>[];
    for (final (r, g, b) in rgbs) {
      d.state.foreground = _colour(r, g, b);
      first.add(d.lineColor);
    }
    for (var i = 0; i < rgbs.length; i++) {
      final (r, g, b) = rgbs[i];
      d.state.foreground = _colour(r, g, b);
      final again = d.lineColor;
      expect(again.toARGB32(), Color.fromARGB(255, r, g, b).toARGB32());
      expect(identical(again, first[i]), isTrue, reason: 'colour $i is made again');
    }
  });
}
