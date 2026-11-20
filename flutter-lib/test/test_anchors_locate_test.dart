// TestAnchors.locate answers where a tagged widget is and whether a tap at its centre reaches it,
// from the render tree, with no semantics tree built.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/utils/test_anchors.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _label() => VLabel()
  ..swt = 'Label'
  ..id = 7
  ..style = SWT.NONE
  ..text = 'covered'
  ..visible = true
  ..enabled = true
  ..bounds = _rect(0, 0, 200, 40);

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..visible = true
  ..items = [VTreeItem()
    ..id = 10
    ..text = 'row']
  ..bounds = _rect(0, 0, 300, 200);

Future<void> _settle(WidgetTester tester) async {
  for (var i = 0; i < 5; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    await tester.pump();
  }
  await tester.pumpAndSettle();
}

void main() {
  setUp(() => TestAnchors.enabled = true);
  tearDown(() => TestAnchors.enabled = false);

  testWidgets('a tree row is located where it is painted, and takes a tap', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, height: 200, child: TreeSwt<VTree>(value: _tree())),
      ),
    ));
    await _settle(tester);

    final located = TestAnchors.locate('TreeItem/10');
    expect(located, isNotNull);
    expect(located!['hit'], isTrue);
    expect(located['paints'], isTrue, reason: 'a row on screen inside the scrolling area of the tree paints');
    final row = tester.getRect(find.text('row'));
    final box = Rect.fromLTWH(located['x'] as double, located['y'] as double, located['width'] as double,
        located['height'] as double);
    expect(box.contains(row.center), isTrue, reason: 'the row text is inside the located box');
  });

  testWidgets('a widget under an opaque overlay is located but takes no tap', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 200,
          height: 40,
          child: Stack(children: [
            LabelSwt(value: _label()),
            Positioned.fill(child: GestureDetector(behavior: HitTestBehavior.opaque, onTap: () {})),
          ]),
        ),
      ),
    ));
    await _settle(tester);

    final located = TestAnchors.locate('Label/7');
    expect(located, isNotNull);
    expect(located!['hit'], isFalse);
  });

  testWidgets('a widget that is gone is not located', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 200, height: 40, child: LabelSwt(value: _label())),
    ));
    await _settle(tester);
    expect(TestAnchors.locate('Label/7'), isNotNull);

    await tester.pumpWidget(EvolveApp(theme: ThemeMode.light, contentWidget: const SizedBox()));
    await _settle(tester);
    expect(TestAnchors.locate('Label/7'), isNull);
  });
}
