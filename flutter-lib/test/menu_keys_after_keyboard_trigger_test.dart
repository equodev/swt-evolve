// The keys after Shift+F10 or the menu key wait for the menu it may open: on a control with no
// menu, they reach Java once the trigger limit passes, with the modifiers held when they were typed.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/menu_evolve.dart';

const _displayId = 5;

Widget _wrap() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: DisplaySwt(value: VDisplay()..id = _displayId),
      ),
    );

List<(String, VEvent)> _keys() => [
      for (final frame in EquoCommService.commForTesting.sentFrames)
        if (EquoCommBase.decodeFrame(frame).$1.startsWith('Display/$_displayId/Key/'))
          (EquoCommBase.decodeFrame(frame).$1.replaceFirst('Display/$_displayId/', ''),
              EquoCommBase.decodeEvent(frame)),
    ];

void main() {
  testWidgets('the release of Shift+F10 on a control with no menu reaches Java after the trigger limit, with Shift',
      (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    await tester.pumpWidget(_wrap());
    EquoCommService.commForTesting.clearSentFrames();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.f10);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.f10);
    await tester.pump(OpenMenus.triggerLimit ~/ 2);
    expect(_keys().map((k) => k.$1), ['Key/KeyDown', 'Key/KeyDown'],
        reason: 'the release waits for a menu that may open');

    await tester.pump(OpenMenus.triggerLimit);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pumpAndSettle();

    expect(_keys().map((k) => k.$1), ['Key/KeyDown', 'Key/KeyDown', 'Key/KeyUp', 'Key/KeyUp']);
    final f10Release = _keys()[2].$2;
    expect(f10Release.keyCode, SWT.F10);
    expect(f10Release.stateMask, SWT.SHIFT, reason: 'typed with Shift down, it is released with Shift');
  });
}
