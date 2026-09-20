// A shell Java put in a window of its own is drawn by a second client rooted at that shell, in
// that window. This client must leave it out: drawn here as well, it would be on screen twice --
// once full-size in its own window and once as a floating pane over the main one.
//
// Java names them in VDisplay.windowedShellIds. It is the only thing that can: the decision comes
// from WindowPolicy, from parentage and modality, and from whether a window actually opened -- none
// of which reach the client.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/shell_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';

const _viewport = Size(1280, 800);

/// Delivers an inbound frame exactly as the transport would (2-byte name length, name, JSON body)
/// so the channel subscription receives it -- the only way a Display update reaches a mounted
/// DisplaySwt, which keeps its own state rather than taking it from a rebuilding parent.
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

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _shell({
  required int id,
  required String text,
  required VRectangle bounds,
  int style = 33555696, // SHELL_TRIM
}) =>
    VShell()
      ..id = id
      ..style = style
      ..text = text
      ..bounds = bounds
      ..visible = true
      // One child so the shell doesn't take Shell's childless-Canvas painting shortcut
      // (see shell_background_paint_test.dart).
      ..children = [
        VComposite()
          ..id = id + 1
          ..style = 0
          ..bounds = _rect(0, 0, 100, 100)
      ];

VShell _mainShell() => _shell(
      id: 1001,
      text: 'Workbench',
      bounds: _rect(0, 0, _viewport.width.toInt(), _viewport.height.toInt()),
    );

VShell _detachedShell() => _shell(
      id: 2002,
      text: 'Detached View',
      bounds: _rect(120, 90, 500, 400),
    );

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  void sizeViewport(WidgetTester tester) {
    tester.view.physicalSize = _viewport;
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
  }

  Future<void> pumpDisplay(WidgetTester tester, VDisplay display) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: display),
    ));
    await tester.pumpAndSettle();
  }

  testWidgets('a shell named as windowed is not drawn here', (tester) async {
    sizeViewport(tester);
    final main = _mainShell();
    final detached = _detachedShell();

    await pumpDisplay(
        tester,
        VDisplay()
          ..id = 7
          ..shells = [main, detached]
          ..mainShellId = main.id
          ..windowedShellIds = [detached.id!]);

    expect(
      find.text('Detached View'),
      findsNothing,
      reason: 'its own window is drawing it; drawing it here too puts it on screen twice',
    );
    expect(
      find.byType(FloatingShellChromeScope),
      findsNothing,
      reason: 'a detached shell is a real window, not a floating pane with our own chrome',
    );
  });

  testWidgets('the main shell still fills the window when another is detached', (tester) async {
    sizeViewport(tester);
    final main = _mainShell();
    final detached = _detachedShell();

    await pumpDisplay(
        tester,
        VDisplay()
          ..id = 7
          ..shells = [main, detached]
          ..mainShellId = main.id
          ..windowedShellIds = [detached.id!]);

    expect(
      main.bounds,
      predicate<VRectangle?>((b) =>
          b?.x == 0 &&
          b?.y == 0 &&
          b?.width == _viewport.width.toInt() &&
          b?.height == _viewport.height.toInt()),
      reason: 'detaching a sibling changes nothing about the shell that fills this window',
    );
  });

  testWidgets('a shell not named as windowed is still drawn as a floating pane', (tester) async {
    sizeViewport(tester);
    final main = _mainShell();
    final inline = _detachedShell();

    await pumpDisplay(
        tester,
        VDisplay()
          ..id = 7
          ..shells = [main, inline]
          ..mainShellId = main.id);

    expect(
      find.text('Detached View'),
      findsOneWidget,
      reason: 'with no window of its own, the only place it can be drawn is here',
    );
    expect(find.byType(FloatingShellChromeScope), findsOneWidget);
  });

  testWidgets('an empty or absent list changes nothing', (tester) async {
    sizeViewport(tester);

    for (final ids in <List<int>?>[null, <int>[]]) {
      final main = _mainShell();
      final inline = _detachedShell();
      await pumpDisplay(
          tester,
          VDisplay()
            ..id = 7
            ..shells = [main, inline]
            ..mainShellId = main.id
            ..windowedShellIds = ids);

      expect(find.text('Detached View'), findsOneWidget,
          reason: 'windowedShellIds=$ids must read as "none of them", not as a filter');
    }
  });

  testWidgets('a shell that stops being windowed comes back into this window', (tester) async {
    sizeViewport(tester);
    final main = _mainShell();
    final detached = _detachedShell();

    await pumpDisplay(
        tester,
        VDisplay()
          ..swt = 'Display'
          ..id = 8
          ..shells = [main, detached]
          ..mainShellId = main.id
          ..windowedShellIds = [detached.id!]);
    expect(find.text('Detached View'), findsNothing);

    // The browser refused the window, or the policy changed: Java stops naming it, and the shell
    // has to be drawn here again or it exists nowhere at all. Delivered as a real frame, because
    // that is the only way a Display update reaches a mounted DisplaySwt.
    _receiveJson('Display/8', (VDisplay()
          ..swt = 'Display'
          ..id = 8
          ..shells = [_mainShell(), _detachedShell()]
          ..mainShellId = main.id)
        .toJson());
    await tester.pumpAndSettle();

    expect(find.text('Detached View'), findsOneWidget);
  });

  testWidgets('a shell that becomes windowed leaves this window', (tester) async {
    sizeViewport(tester);
    final main = _mainShell();
    final inline = _detachedShell();

    await pumpDisplay(
        tester,
        VDisplay()
          ..swt = 'Display'
          ..id = 9
          ..shells = [main, inline]
          ..mainShellId = main.id);
    expect(find.text('Detached View'), findsOneWidget);

    _receiveJson('Display/9', (VDisplay()
          ..swt = 'Display'
          ..id = 9
          ..shells = [_mainShell(), _detachedShell()]
          ..mainShellId = main.id
          ..windowedShellIds = [inline.id!])
        .toJson());
    await tester.pumpAndSettle();

    expect(
      find.text('Detached View'),
      findsNothing,
      reason: 'a window has opened for it; this client must let go of it in the same update',
    );
  });

  testWidgets('several detached shells are all left out', (tester) async {
    sizeViewport(tester);
    final main = _mainShell();
    final first = _detachedShell();
    final second = _shell(id: 3003, text: 'Second Detached', bounds: _rect(300, 200, 400, 300));

    await pumpDisplay(
        tester,
        VDisplay()
          ..id = 7
          ..shells = [main, first, second]
          ..mainShellId = main.id
          ..windowedShellIds = [first.id!, second.id!]);

    expect(find.text('Detached View'), findsNothing);
    expect(find.text('Second Detached'), findsNothing);
    expect(find.byType(FloatingShellChromeScope), findsNothing);
  });

  testWidgets('a windowed id naming no shell in this update is harmless', (tester) async {
    sizeViewport(tester);
    final main = _mainShell();
    final inline = _detachedShell();

    await pumpDisplay(
        tester,
        VDisplay()
          ..id = 7
          ..shells = [main, inline]
          ..mainShellId = main.id
          ..windowedShellIds = [999999]);

    expect(find.text('Detached View'), findsOneWidget,
        reason: 'an id for a shell this update does not carry must not hide a different one');
  });
}
