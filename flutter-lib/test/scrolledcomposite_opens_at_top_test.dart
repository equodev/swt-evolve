// A ScrolledComposite must open at the top-left. A focus/reveal in the content
// subtree while the viewport is still 0-sized can scroll the view to the end on
// open; the impl re-asserts the origin until the user drags. This guards that:
// after the scroll offset is driven to the end (as the reveal does) and the
// widget rebuilds, the view settles back to (0, 0) — while a real user drag is
// preserved.

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

// Content (500x500) larger than the 150x150 viewport on both axes, so both
// scrollbars are active and can be driven off the origin. A child makes the
// content lay out at its bounds instead of trying to be infinite inside the
// unbounded scroll viewport.
VScrolledComposite _sc() => VScrolledComposite()
  ..id = 1
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..expandHorizontal = true
  ..expandVertical = true
  ..minWidth = 500
  ..minHeight = 500
  ..bounds = _rect(0, 0, 150, 150)
  ..content = (VComposite()
    ..id = 2
    ..style = SWT.NONE
    ..enabled = true
    ..bounds = _rect(0, 0, 500, 500)
    ..children = [
      VComposite()
        ..id = 3
        ..style = SWT.NONE
        ..enabled = true
        ..bounds = _rect(0, 0, 10, 10),
    ]);

List<ScrollPosition> _positions(WidgetTester tester) => tester
    .stateList<ScrollableState>(find.byType(Scrollable))
    .map((s) => s.position)
    .toList();

// Pumps one ScrolledComposite in a small viewport and hands back a callback that
// rebuilds it in place (as the app does when it keeps pushing state on open),
// keeping the same value so the State — and its scroll controllers — survive.
Future<VoidCallback> _pump(WidgetTester tester) async {
  final sc = _sc();
  late StateSetter setter;
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 150,
      height: 150,
      child: StatefulBuilder(builder: (context, setState) {
        setter = setState;
        return ScrolledCompositeSwt<VScrolledComposite>(value: sc);
      }),
    ),
  ));
  await tester.pump();
  return () => setter(() {});
}

void main() {
  testWidgets('ScrolledComposite settles back to the top-left after a reveal on open',
      (tester) async {
    final rebuild = await _pump(tester);

    final before = _positions(tester);
    expect(before.any((p) => p.maxScrollExtent > 0), isTrue,
        reason: 'content must be larger than the viewport so it can scroll');

    // Simulate the on-open focus/reveal driving the view to the end.
    for (final p in before) {
      p.jumpTo(p.maxScrollExtent);
    }
    await tester.pump();
    expect(_positions(tester).any((p) => p.pixels > 0), isTrue,
        reason: 'sanity: the view is scrolled off the origin');

    // A subsequent rebuild (the app keeps pushing state on open) must let the
    // view settle back to the origin.
    rebuild();
    await tester.pump();

    for (final p in _positions(tester)) {
      expect(p.pixels, 0,
          reason: 'ScrolledComposite must open at the top-left, not scrolled');
    }
  });

  testWidgets('a user drag is preserved (the view is not forced back to the top)',
      (tester) async {
    final rebuild = await _pump(tester);

    // A real drag carries dragDetails and marks the view as user-scrolled.
    await tester.drag(find.byType(Scrollable).first, const Offset(0, -120));
    await tester.pump();

    final dragged = _positions(tester).firstWhere((p) => p.axis == Axis.vertical);
    expect(dragged.pixels, greaterThan(0), reason: 'the drag scrolled the view down');

    // A later rebuild must NOT undo the user's scroll.
    rebuild();
    await tester.pump();

    final after = _positions(tester).firstWhere((p) => p.axis == Axis.vertical);
    expect(after.pixels, greaterThan(0),
        reason: 'a user drag must survive later rebuilds');
  });
}
