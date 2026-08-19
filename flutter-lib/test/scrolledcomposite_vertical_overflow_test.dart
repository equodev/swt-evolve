// A ScrolledComposite with expandVertical=true whose declared minHeight
// understates the content's real height (the content's own children are laid
// out past its own reported bounds — see nolayout.dart's getSize()) must still
// let the user reach the overflow by scrolling, not silently clip it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrolledcomposite.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

// Viewport is 100x100. minWidth (200) exceeds the viewport, so the horizontal
// axis scrolls normally. minHeight (90) does NOT exceed the viewport, so the
// vertical axis takes the "fill" branch -- exactly like the real report,
// where the last child sits past both the content's own declared bounds and
// minHeight/the viewport.
VScrolledComposite _sc() => VScrolledComposite()
  ..id = 1
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..expandHorizontal = true
  ..expandVertical = true
  ..minWidth = 200
  ..minHeight = 90
  ..bounds = _rect(0, 0, 100, 100)
  ..content = (VComposite()
    ..id = 2
    ..style = SWT.NONE
    ..enabled = true
    ..bounds = _rect(0, 0, 200, 90)
    ..children = [
      VComposite()
        ..id = 3
        ..style = SWT.NONE
        ..enabled = true
        ..bounds = _rect(0, 80, 50, 30),
    ]);

void main() {
  testWidgets(
      'vertical overflow past a stale minHeight/content bounds is reachable by scrolling',
      (tester) async {
    final sc = _sc();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 100,
        height: 100,
        child: ScrolledCompositeSwt<VScrolledComposite>(value: sc),
      ),
    ));
    await tester.pump();

    final vertical = tester
        .stateList<ScrollableState>(find.byType(Scrollable))
        .map((s) => s.position)
        .where((p) => p.axis == Axis.vertical)
        .toList();

    expect(vertical, isNotEmpty,
        reason: 'a vertical Scrollable must exist once content overflows, '
            'even when the declared minHeight/bounds says it fits');
    expect(vertical.first.maxScrollExtent, greaterThan(0),
        reason: 'the overflowing child (bottom edge at y=110) must be reachable '
            'by scrolling past the viewport (height 100)');
  });
}
