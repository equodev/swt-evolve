// The client that draws a detached shell is rooted at that shell rather than at the Display --
// main.dart boots it with widgetName=Shell (a `?widgetName=Shell&widgetId=N` URL on the web, the
// same pair passed to the native bridge on the desktop).
//
// Two things have to hold for that client. It has to fill its window rather than draw the shell as
// a floating pane with our own chrome: the pane is what a shell looks like when it shares a window
// with others, and this one has a window to itself. And it has to render from a frame on the
// shell's own channel, because there is no Display state here to carry one down.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/impl/shell_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _window = Size(600, 420);
const _shellId = 4242;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _detachedShell() => VShell()
  ..swt = 'Shell'
  ..id = _shellId
  ..style = 33555696 // SHELL_TRIM
  ..text = 'Detached View'
  ..visible = true
  ..bounds = _rect(0, 0, _window.width.toInt(), _window.height.toInt())
  ..children = [
    VComposite()
      ..swt = 'Composite'
      ..id = _shellId + 1
      ..style = 0
      ..bounds = _rect(0, 0, _window.width.toInt(), _window.height.toInt())
      ..children = [
        VLabel()
          ..swt = 'Label'
          ..id = _shellId + 2
          ..style = 0
          ..text = 'Inside the detached window'
          ..bounds = _rect(0, 0, 200, 20)
      ]
  ];

void _receiveJson(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  Future<void> pumpShellRoot(WidgetTester tester) async {
    tester.view.physicalSize = _window;
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    // Exactly what main.dart builds for widgetName == "Shell".
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      isMainWindow: true,
      contentWidget: createContentWidget('Shell', _shellId),
    ));
    await tester.pumpAndSettle();
  }

  testWidgets('a shell rooted as a window renders its content from its own channel',
      (tester) async {
    await pumpShellRoot(tester);

    // Nothing has described the shell yet: the root carries identity only.
    expect(find.text('Inside the detached window'), findsNothing);

    _receiveJson('Shell/$_shellId', _detachedShell().toJson());
    await tester.pumpAndSettle();

    expect(
      find.text('Inside the detached window'),
      findsOneWidget,
      reason: 'there is no Display state in this client, so the shell frame is the only source',
    );
  });

  testWidgets('a shell rooted as a window fills it instead of floating in it', (tester) async {
    await pumpShellRoot(tester);
    _receiveJson('Shell/$_shellId', _detachedShell().toJson());
    await tester.pumpAndSettle();

    expect(
      find.byType(FloatingShellChromeScope),
      findsNothing,
      reason: 'the floating pane is how a shell looks when it shares a window; this one owns its',
    );
    expect(
      tester.getSize(find.byType(ShellSwt)).width,
      _window.width,
      reason: 'the window is the shell, so the shell takes all of it',
    );
  });
}
