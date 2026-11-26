// Java sets a ScrolledComposite's scroll bars (range, thumb, increments) on every resize: a sash
// drag beside one does it on every step. Its views draw the bars and keep their own offset, so that
// is no change to what it draws. A selection the view still follows (the user has not scrolled)
// moves the view, as before.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/scrolledcomposite.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VScrollBar _bar({int seq = 10, int maximum = 500, int thumb = 150, int selection = 0}) => VScrollBar()
  ..swt = 'ScrollBar'
  ..id = 11
  ..seq = seq
  ..style = SWT.V_SCROLL
  ..enabled = true
  ..visible = true
  ..minimum = 0
  ..maximum = maximum
  ..thumb = thumb
  ..selection = selection
  ..increment = 1
  ..pageIncrement = thumb;

VScrolledComposite _sc() => VScrolledComposite()
  ..swt = 'ScrolledComposite'
  ..id = 1
  ..seq = 10
  ..style = SWT.V_SCROLL
  ..enabled = true
  ..visible = true
  ..expandHorizontal = true
  ..minHeight = 500
  ..bounds = _rect(0, 0, 150, 150)
  ..verticalBar = _bar()
  ..content = (VComposite()
    ..swt = 'Composite'
    ..id = 2
    ..seq = 10
    ..style = SWT.NONE
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, 150, 500)
    ..children = [
      VComposite()
        ..swt = 'Composite'
        ..id = 3
        ..seq = 10
        ..style = SWT.NONE
        ..enabled = true
        ..visible = true
        ..bounds = _rect(0, 0, 10, 10),
    ]);

Future<void> _mount(WidgetTester tester) async {
  await tester.pumpWidget(const SizedBox());
  freshClient();
  final window = VComposite()
    ..swt = 'Composite'
    ..id = 599
    ..style = SWT.NONE
    ..seq = 10
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, 300, 300)
    ..children = [_sc()];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 300, height: 300, child: CompositeSwt<VComposite>(value: window)),
    ),
  ));
  await tester.pumpAndSettle();
}

int Function() _countBuilds() {
  var builds = 0;
  debugOnRebuildDirtyWidget = (element, _) {
    if (element.widget is ScrolledCompositeSwt) builds++;
  };
  addTearDown(() => debugOnRebuildDirtyWidget = null);
  return () => builds;
}

double _offset(WidgetTester tester) =>
    tester.stateList<ScrollableState>(find.byType(Scrollable)).first.position.pixels;

void main() {
  testWidgets('a scroll bar given a new range does not build it again', (tester) async {
    await _mount(tester);
    final builds = _countBuilds();

    await deliverChange(_bar(seq: 11, maximum: 520, thumb: 140),
        keys: [WireKey.k_ScrollBar_maximum, WireKey.k_ScrollBar_thumb, WireKey.k_ScrollBar_pageIncrement], base: 10);
    await tester.pump();

    expect(builds(), 0);
  });

  testWidgets('a selection the view follows still moves it', (tester) async {
    await _mount(tester);
    expect(_offset(tester), 0);

    await deliverChange(_bar(seq: 11, selection: 120), keys: [WireKey.k_ScrollBar_selection], base: 10);
    await tester.pumpAndSettle();

    expect(_offset(tester), 120);
  });
}
