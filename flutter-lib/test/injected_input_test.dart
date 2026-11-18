// The test harness's injected mouse and keyboard reach Java the way a user's input does.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/test_harness.dart' as harness;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

Future<void> _pumpButton(WidgetTester tester) async {
  final button = VButton()
    ..id = 2
    ..style = SWT.PUSH
    ..enabled = true
    ..text = 'OK'
    ..bounds = _rect(10, 10, 100, 40);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: CompositeSwt(
          value: VComposite()
            ..id = 1
            ..style = SWT.NONE
            ..enabled = true
            ..bounds = _rect(0, 0, 200, 120)
            ..children = [button]),
    ),
  ));
  await tester.pump();
}

void main() {
  testWidgets('an injected wheel is reported to the control under the injected mouse, once per notch', (tester) async {
    final sent = <String>[];
    EquoCommBase.sendObserverForTesting = (action, payload) {
      if (action.endsWith('/MouseWheel/MouseWheel')) sent.add('$action:${(payload as VEvent).count}');
    };
    addTearDown(() => EquoCommBase.sendObserverForTesting = null);
    final button = VButton()
      ..id = 2
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'OK'
      ..bounds = _rect(10, 10, 100, 40);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: CompositeSwt(
            value: VComposite()
              ..id = 1
              ..style = SWT.NONE
              ..enabled = true
              ..bounds = _rect(0, 0, 200, 120)
              ..children = [button]),
      ),
    ));
    await tester.pump();
    sent.clear();
    final moved = await harness.pointerOp({'op': 'move', 'targetId': 2, 'x': 50, 'y': 20});
    expect(moved['error'], isNull);
    final wheeled = await harness.pointerOp({'op': 'wheel', 'notches': 2});
    expect(wheeled['error'], isNull);
    await tester.pump(const Duration(seconds: 1));
    // One wheel event per notch, each three lines down, as GTK reports a wheel.
    expect(sent.where((s) => s.contains('/2/')), ['Button/2/MouseWheel/MouseWheel:-3', 'Button/2/MouseWheel/MouseWheel:-3']);
  });

  testWidgets('a click injected on the real mouse lands where the real mouse rests, once it has arrived', (tester) async {
    final sent = <String>[];
    EquoCommBase.sendObserverForTesting = (action, payload) {
      if (action.startsWith('Button/2/Mouse/')) sent.add(action);
    };
    addTearDown(() => EquoCommBase.sendObserverForTesting = null);
    await _pumpButton(tester);
    await tester.runAsync(() => harness.pointerOp({'op': 'watch'}));
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(60, 30));
    await mouse.moveTo(const Offset(60, 30));
    await tester.pump();
    final arrived = (await tester.runAsync(
        () => harness.pointerOp({'op': 'awaitReal', 'targetId': 2, 'x': 50, 'y': 20, 'timeoutMs': 200})))!;
    expect(arrived['arrived'], isTrue);
    await tester.runAsync(() => harness.pointerOp({'op': 'down', 'real': true}));
    await tester.runAsync(() => harness.pointerOp({'op': 'up', 'real': true}));
    await tester.pump(const Duration(seconds: 1));
    expect(sent, ['Button/2/Mouse/MouseDown', 'Button/2/Mouse/MouseUp']);
    await mouse.removePointer();
  });
}
