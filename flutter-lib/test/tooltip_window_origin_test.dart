// A ToolTip's location is a screen point, and the window drawing it starts wherever the main shell
// sits on screen: the tooltip is placed at its location minus that origin, where a menu or a floating
// shell given the same point would be. A primary press on it selects it, with the button in the
// stateMask, as every native does on its mouse-down.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tooltip.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

import 'support/sent_events.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

Future<void> _pumpShownToolTip(WidgetTester tester, int style) async {
  tester.view.physicalSize = const Size(800, 600);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.resetPhysicalSize);
  addTearDown(tester.view.resetDevicePixelRatio);

  final shell = VShell()
    ..id = 1
    ..style = SWT.SHELL_TRIM
    ..text = 'main'
    ..bounds = _rect(200, 150, 800, 600)
    ..children = [
      VComposite()
        ..id = 2
        ..style = 0
        ..bounds = _rect(0, 0, 100, 100)
    ];
  final tip = VToolTip()
    ..id = 3
    ..style = style
    ..text = 'Title'
    ..message = 'Click me'
    ..location = (VPoint()
      ..x = 260
      ..y = 230)
    ..visible = true;

  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(value: VDisplay()
      ..shells = [shell]
      ..tooltips = [tip]),
  ));
  await tester.pumpAndSettle();
}

void main() {
  setUp(() {
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });
  tearDown(() {
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
  });

  for (final style in [SWT.NONE, SWT.BALLOON]) {
    testWidgets('a ToolTip (style $style) shown at a screen point is drawn there in a window off the screen origin',
        (tester) async {
      await _pumpShownToolTip(tester, style);

      final title = tester.getTopLeft(find.text('Title'));
      expect(title.dx, inInclusiveRange(60, 100), reason: 'screen x 260 is 60 into a window at x 200');
      expect(title.dy, inInclusiveRange(80, 120), reason: 'screen y 230 is 80 into a window at y 150');
    });

    testWidgets('a press on a ToolTip (style $style) selects it with the button held', (tester) async {
      await _pumpShownToolTip(tester, style);
      EquoCommService.commForTesting.clearSentFrames();

      final press = await tester.startGesture(tester.getCenter(find.text('Click me')));
      await tester.pump();
      final selections = sentEvents().where((e) => e.$1.endsWith('Selection/Selection')).toList();
      await press.up();
      await tester.pumpAndSettle();

      expect(selections, hasLength(1), reason: 'every native selects a tooltip on the press');
      expect(selections.single.$2['stateMask'], SWT.BUTTON1);
    });
  }
}
