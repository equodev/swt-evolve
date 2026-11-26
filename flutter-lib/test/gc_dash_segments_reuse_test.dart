// A dotted or dashed line drawn again with the same ends, style, width and dash offset is the same
// dashes: the drawer keeps them and hands the next frame's line the same list.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'support/channels.dart' as channels;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 951301;
  late SegmentsShape drawn;

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
    }
  }

  /// The dashes of one line, drawn by a GC of its own, as one frame draws it.
  Future<List<double>> dashes(
      {int style = SWT.LINE_DOT,
      int width = 1,
      double? offset,
      List<int>? dash,
      int x1 = 0,
      int y1 = 5,
      int x2 = 20,
      int y2 = 5}) async {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..lineStyle = style
      ..lineWidth = width
      ..lineDashOffset = offset
      ..lineDash = dash;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);
    drawer.onDrawLineintintintint(VGCDrawLineintintintint()
      ..x1 = x1
      ..y1 = y1
      ..x2 = x2
      ..y2 = y2);
    EquoCommService.commForTesting
        .receiveBinary(channels.frame('GC/$id/gcDispose', TaggedValues.encode({'fullRepaint': true})));
    await settle();
    drawn = drawer.shapes.whereType<SegmentsShape>().single;
    return drawn.points;
  }

  test('the dashes a GC works out are drawn as a list that never changes', () async {
    await dashes();
    expect(drawn.fixed, isTrue);
    await dashes(style: SWT.LINE_CUSTOM, dash: [4, 2]);
    expect(drawn.fixed, isTrue);
  });

  test('the same dotted line in a later frame reuses its dashes', () async {
    final first = await dashes();
    final again = await dashes();
    expect(identical(again, first), isTrue);
    expect(first, [0, 5, 1, 5, 2, 5, 3, 5, 4, 5, 5, 5, 6, 5, 7, 5, 8, 5, 9, 5, 10, 5, 11, 5, 12, 5, 13, 5,
        14, 5, 15, 5, 16, 5, 17, 5, 18, 5, 19, 5]);
  });

  test('a moved line, another style, width or offset gets dashes of its own', () async {
    final base = await dashes();
    final hairline = await dashes(width: 0);
    expect(hairline, [0, 5, 3, 5, 6, 5, 9, 5, 12, 5, 15, 5, 18, 5, 20, 5]);
    final shifted = await dashes(offset: 1);
    expect(shifted, [1, 5, 2, 5, 3, 5, 4, 5, 5, 5, 6, 5, 7, 5, 8, 5, 9, 5, 10, 5, 11, 5, 12, 5, 13, 5,
        14, 5, 15, 5, 16, 5, 17, 5, 18, 5, 19, 5, 20, 5]);
    final dashed = await dashes(style: SWT.LINE_DASH, width: 2);
    expect(dashed, [0, 5, 6, 5, 8, 5, 14, 5, 16, 5, 20, 5]);
    final moved = await dashes(y1: 6, y2: 6);
    expect(moved, [for (var i = 0; i < base.length; i++) i.isOdd ? 6 : base[i]]);
    for (final other in [hairline, shifted, dashed, moved]) {
      expect(identical(other, base), isFalse);
    }
  });

  test('a custom dash is worked out for each line', () async {
    final first = await dashes(style: SWT.LINE_CUSTOM, dash: [4, 2]);
    final again = await dashes(style: SWT.LINE_CUSTOM, dash: [4, 2]);
    expect(first, [0, 5, 4, 5, 6, 5, 10, 5, 12, 5, 16, 5, 18, 5, 20, 5]);
    expect(again, first);
  });
}
