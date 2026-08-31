// SWT keeps "no clipping region" and "an empty clipping region" apart: the first draws everything,
// the second discards everything. Conflating them paints ops the application asked to throw away.
//
// A composition that confines each child by clipping to its intersection with the damaged area
// produces empty intersections routinely once a Paint is scoped, and its children still emit
// their grid lines before noticing they have nothing to draw.

import 'dart:ui' show Rect;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  GCDrawer drawerClippedTo(VRectangle? clipping) {
    final state = VGC.empty()
      ..id = 840001
      ..clipping = clipping;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);
    return drawer;
  }

  VRectangle rect(int x, int y, int width, int height) => VRectangle()
    ..x = x
    ..y = y
    ..width = width
    ..height = height;

  test('no clipping region leaves drawing unclipped', () {
    expect(drawerClippedTo(null).clipping, isNull);
  });

  test('a real clipping region is honoured as it stands', () {
    expect(drawerClippedTo(rect(10, 20, 30, 40)).clipping,
        const Rect.fromLTWH(10, 20, 30, 40));
  });

  test('an empty clipping region discards everything, rather than nothing', () {
    for (final empty in [
      rect(0, 0, 0, 0),
      rect(5, 5, 100, 0),
      rect(5, 5, 0, 100),
    ]) {
      final clip = drawerClippedTo(empty).clipping;
      expect(clip, isNotNull,
          reason: 'null means unclipped, which is the opposite of what an empty region asks for');
      expect(clip!.isEmpty, isTrue, reason: 'nothing may pass an empty clip');
    }
  });

  test('a degenerate clipping region discards everything too', () {
    final clip = drawerClippedTo(rect(5, 5, -10, 20)).clipping;
    expect(clip, isNotNull);
    expect(clip!.isEmpty, isTrue);
  });
}
