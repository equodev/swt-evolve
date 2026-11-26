// A plain Canvas resized by its parent's layout, as the widgets of a Forms page are on every step of
// a sash drag beside it, is laid out again, not built again, and ends where a fresh mount at the new
// bounds puts it and its children. A Canvas with scroll bars is built again: they are laid out from
// its size.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VCanvas _canvas(VRectangle bounds, {int seq = 10, int style = SWT.NONE}) => VCanvas()
  ..swt = 'Canvas'
  ..id = 610
  ..seq = seq
  ..style = style
  ..enabled = true
  ..visible = true
  ..bounds = bounds
  ..children = [
    VComposite()
      ..swt = 'Composite'
      ..id = 611
      ..seq = 10
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(4, 4, 40, 20),
  ];

Future<void> _mount(WidgetTester tester, VCanvas canvas) async {
  await tester.pumpWidget(const SizedBox());
  freshClient();
  final window = VComposite()
    ..swt = 'Composite'
    ..id = 599
    ..style = SWT.NONE
    ..seq = 10
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, 500, 300)
    ..children = [canvas];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 500, height: 300, child: CompositeSwt<VComposite>(value: window)),
    ),
  ));
  await tester.pumpAndSettle();
}

int Function() _countCanvasBuilds() {
  var builds = 0;
  debugOnRebuildDirtyWidget = (element, _) {
    if (element.widget is CanvasSwt) builds++;
  };
  addTearDown(() => debugOnRebuildDirtyWidget = null);
  return () => builds;
}

List<Rect> _geometry(WidgetTester tester) => [
      tester.getRect(find.byWidgetPredicate((w) => w is CanvasSwt)),
      tester.getRect(find.byWidgetPredicate((w) => w is CompositeSwt && w.value.id == 611)),
    ];

void main() {
  testWidgets('a plain Canvas resized is laid out again, not built again', (tester) async {
    await _mount(tester, _canvas(_rect(10, 10, 200, 100)));
    final builds = _countCanvasBuilds();

    await deliverChange(_canvas(_rect(10, 10, 260, 140), seq: 11), keys: [WireKey.k_Control_bounds], base: 10);
    await tester.pumpAndSettle();
    debugOnRebuildDirtyWidget = null;

    expect(builds(), 0);
    final resized = _geometry(tester);

    await _mount(tester, _canvas(_rect(10, 10, 260, 140)));
    expect(resized, _geometry(tester), reason: 'where a Canvas mounted at those bounds is drawn');
  });

  testWidgets('a Canvas with scroll bars is built again for a new size', (tester) async {
    await _mount(tester, _canvas(_rect(10, 10, 200, 100), style: SWT.V_SCROLL));
    final builds = _countCanvasBuilds();

    await deliverChange(_canvas(_rect(10, 10, 260, 140), seq: 11, style: SWT.V_SCROLL),
        keys: [WireKey.k_Control_bounds], base: 10);
    await tester.pumpAndSettle();

    expect(builds(), greaterThan(0));
  });
}
