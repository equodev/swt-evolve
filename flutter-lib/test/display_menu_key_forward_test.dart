// The Menu key has no SWT key code, so the Display posts it to Java as a context-menu request
// instead of a KeyDown.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

const _displayId = 5;

Widget _wrap() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: DisplaySwt(value: VDisplay()..id = _displayId),
      ),
    );

List<String> _sent() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((channel) => channel.startsWith('Display/$_displayId/'))
    .toList();

void main() {
  testWidgets('the Menu key posts a context-menu request, not a KeyDown', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    await tester.pumpWidget(_wrap());
    EquoCommService.commForTesting.clearSentFrames();

    await tester.sendKeyEvent(LogicalKeyboardKey.contextMenu);
    await tester.pump();

    expect(_sent(), ['Display/$_displayId/MenuKey']);
  });

  testWidgets('Shift+F10 still reaches Java as a KeyDown', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    await tester.pumpWidget(_wrap());
    EquoCommService.commForTesting.clearSentFrames();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.sendKeyEvent(LogicalKeyboardKey.f10);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pump();

    expect(_sent(), contains('Display/$_displayId/Key/KeyDown'));
    expect(_sent(), isNot(contains('Display/$_displayId/MenuKey')));
  });
}
