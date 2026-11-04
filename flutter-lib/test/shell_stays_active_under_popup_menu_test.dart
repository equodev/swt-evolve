// A popup menu the Display shows takes the keyboard without deactivating the shell it opened over:
// the shell reports no Deactivate while the menu is open, nor an Activate when it closes.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

import 'support/menu_shown_ack.dart';

const _shellId = 4700;
const _menuId = 4710;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _shell() => VShell()
  ..id = _shellId
  ..style = SWT.SHELL_TRIM
  ..text = 'shell'
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    VComposite()
      ..id = 4701
      ..style = SWT.NONE
      ..enabled = true
      ..bounds = _rect(20, 20, 200, 60),
  ];

VMenu _popup() => VMenu()
  ..id = _menuId
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..location = (VPoint()
    ..x = 50
    ..y = 50)
  ..items = [
    VMenuItem()
      ..id = 4711
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'One',
  ];

List<String> _shellActivation() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((c) => c.startsWith('Shell/$_shellId/') && c.endsWith('ctivate'))
    .toList();

void main() {
  tearDown(() {
    VRegistry.instance.clear();
    EquoCommService.commForTesting.clearSentFrames();
  });

  testWidgets('a shown popup menu does not deactivate the shell under it', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    EquoCommService.commForTesting.clearSentFrames();
    // Shown by Java: the popup waits for its Show to be answered before it opens.
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
        value: VDisplay()
          ..id = 7
          ..shells = [_shell()]
          ..popups = [_popup()],
      ),
    ));
    await tester.pumpAndSettle();
    final shellScope = FocusManager.instance.rootScope.descendants
        .firstWhere((node) => node.debugLabel == 'ShellFocusScope');
    shellScope.requestFocus();
    await tester.pumpAndSettle();
    expect(_shellActivation(), contains('Shell/$_shellId/Shell/Activate'),
        reason: 'the click activates the shell');
    EquoCommService.commForTesting.clearSentFrames();

    await ackMenuShown(tester, _menuId);
    expect(find.text('One'), findsOneWidget, reason: 'the popup is open');

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();
    expect(find.text('One'), findsNothing, reason: 'Escape closes the popup');

    expect(_shellActivation(), isEmpty);
  });
}
