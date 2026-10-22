import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/shell_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _viewport = Size(1000, 600);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _shell({
  required int id,
  required String text,
  required VRectangle bounds,
  VPoint? minimumSize,
}) =>
    VShell()
      ..id = id
      ..style = SWT.SHELL_TRIM
      ..text = text
      ..bounds = bounds
      ..minimumSize = minimumSize
      ..visible = true
      ..children = [
        VComposite()
          ..id = id + 1
          ..style = 0
          ..bounds = _rect(0, 0, 100, 100)
      ];

VPoint _point(int x, int y) => VPoint()
  ..x = x
  ..y = y;

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  Future<Rect> pumpOversized(WidgetTester tester, VPoint minimumSize, VRectangle bounds) async {
    tester.view.physicalSize = _viewport;
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    final main = _shell(
        id: 1001,
        text: 'Main',
        bounds: _rect(0, 0, _viewport.width.toInt(), _viewport.height.toInt()));
    final oversized =
        _shell(id: 2002, text: 'Oversized', bounds: bounds, minimumSize: minimumSize);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
          value: VDisplay()
            ..id = 7
            ..shells = [main, oversized]
            ..mainShellId = main.id),
    ));
    await tester.pumpAndSettle();
    return tester.getRect(find.byType(FloatingShellChromeScope));
  }

  Future<void> drag(WidgetTester tester, Offset from, Offset by) async {
    final gesture = await tester.startGesture(from);
    await gesture.moveBy(by / 2);
    await gesture.moveBy(by / 2);
    await gesture.up();
    await tester.pumpAndSettle();
  }

  testWidgets('dragging the top edge of a shell taller than the viewport does not throw',
      (tester) async {
    final pane = await pumpOversized(tester, _point(300, 700), _rect(100, 20, 400, 700));

    await drag(tester, Offset(pane.center.dx, pane.top + 3), const Offset(0, 40));

    expect(tester.takeException(), isNull);
    expect(tester.getRect(find.byType(FloatingShellChromeScope)).height, pane.height,
        reason: 'drawn capped to the viewport, a shrinking drag must not jump it to its minimum');
    expect(tester.getRect(find.byType(FloatingShellChromeScope)).top, pane.top,
        reason: 'growing from the bottom edge would push the title bar out of reach');
  });

  testWidgets('dragging the right edge of a shell wider than the viewport does not throw',
      (tester) async {
    final pane = await pumpOversized(tester, _point(1100, 200), _rect(0, 20, 1100, 300));

    await drag(tester, Offset(pane.right - 3, pane.center.dy), const Offset(-40, 0));

    expect(tester.takeException(), isNull);
    expect(tester.getRect(find.byType(FloatingShellChromeScope)).width, pane.width,
        reason: 'drawn capped to the viewport, a shrinking drag must not jump it to its minimum');
  });
}
