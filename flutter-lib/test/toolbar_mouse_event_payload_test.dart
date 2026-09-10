// A real SWT ToolBar takes a third path through the render layer: ToolBarImpl overrides build()
// and always goes through ControlImpl.wrap(), never the with-children interaction chrome that
// serves a plain Composite. It therefore has to report the same mouse payload as the other two
// shapes -- an application that hangs selection off raw SWT.MouseDown/SWT.MouseUp listeners gets
// the same button, count and stateMask wherever it attaches them.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

class _RecordingToolBarSwt extends ToolBarSwt<VToolBar> {
  const _RecordingToolBarSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendMouseMouseDown(VToolBar val, VEvent? payload) => calls.add(
      'down:${payload?.button}:${payload?.count}:${payload?.stateMask}');

  @override
  void sendMouseMouseUp(VToolBar val, VEvent? payload) => calls.add(
      'up:${payload?.button}:${payload?.count}:${payload?.stateMask}');
}

VToolBar _toolBar() => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 2
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Item',
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 40);

Future<List<String>> _setUp(WidgetTester tester, List<String> calls) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 300,
      height: 40,
      child: _RecordingToolBarSwt(value: _toolBar(), calls: calls),
    ),
  ));
  await tester.pumpAndSettle();
  return calls;
}

Future<void> _click(
  WidgetTester tester,
  Offset at,
  Duration when, {
  int buttons = kPrimaryButton,
}) async {
  final gesture = await tester.createGesture(buttons: buttons);
  await gesture.down(at, timeStamp: when);
  await gesture.up(timeStamp: when + const Duration(milliseconds: 20));
}

void main() {
  // Past the items, on the bar's own strip, so the bar is the deepest control under the pointer.
  const onBar = Offset(280, 20);

  testWidgets('a ToolBar reports button=1, count=1 on both edges of a left click',
      (tester) async {
    final calls = await _setUp(tester, <String>[]);

    await _click(tester, onBar, const Duration(seconds: 1));
    await tester.pump(Duration.zero);

    expect(calls, equals(['down:1:1:0', 'up:1:1:${SWT.BUTTON1}']));
  });

  testWidgets('a ToolBar reports the right button, not the left one', (tester) async {
    final calls = await _setUp(tester, <String>[]);

    await _click(tester, onBar, const Duration(seconds: 1), buttons: kSecondaryMouseButton);
    await tester.pump(Duration.zero);

    expect(calls, equals(['down:3:1:0', 'up:3:1:${SWT.BUTTON3}']));
  });

  testWidgets('a ToolBar carries the modifiers on a ctrl+click', (tester) async {
    final calls = await _setUp(tester, <String>[]);

    await simulateKeyDownEvent(LogicalKeyboardKey.controlLeft);
    await _click(tester, onBar, const Duration(seconds: 1));
    await tester.pump(Duration.zero);
    await simulateKeyUpEvent(LogicalKeyboardKey.controlLeft);

    expect(calls, equals(['down:1:1:${SWT.CTRL}', 'up:1:1:${SWT.CTRL | SWT.BUTTON1}']));
  });

  testWidgets('the second click of a double-click carries count=2', (tester) async {
    final calls = await _setUp(tester, <String>[]);

    await _click(tester, onBar, const Duration(seconds: 1));
    await _click(tester, onBar, const Duration(seconds: 1, milliseconds: 150));
    await tester.pump(Duration.zero);

    expect(calls, equals([
      'down:1:1:0', 'up:1:1:${SWT.BUTTON1}',
      'down:1:2:0', 'up:1:2:${SWT.BUTTON1}',
    ]));
  });
}
