// A floating pane is drawn where the application put it, and an application built for real windows
// places one against the monitor rather than against the window everything here is drawn inside.
// The e4 workbench detaching a view is that case: the new shell is placed at Control.toDisplay of
// the part stack it came from -- which on the desktop surface is a screen coordinate, several
// hundred pixels past the right edge of the window -- and kept on the monitor, not in the viewport.
// Laid out there, the pane falls outside the stack that holds it and is clipped away entirely:
// "Detach" appeared to do nothing at all.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/shell_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _viewport = Size(2211, 1199);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _shell({
  required int id,
  required String text,
  required VRectangle bounds,
  int style = SWT.SHELL_TRIM,
}) =>
    VShell()
      ..id = id
      ..style = style
      ..text = text
      ..bounds = bounds
      ..visible = true
      ..children = [
        VComposite()
          ..id = id + 1
          ..style = 0
          ..bounds = _rect(0, 0, 100, 100)
      ];

VShell _workbench({int x = 0, int y = 0}) => _shell(
      id: 1001,
      text: 'Workbench',
      bounds: _rect(x, y, _viewport.width.toInt(), _viewport.height.toInt()),
    );

/// What WBWRenderer builds for a detached view: a child shell with TITLE|RESIZE|MAX|CLOSE, at the
/// display coordinates StackRenderer computed and Geometry.moveInside kept inside the *monitor*.
VShell _detached({required int x, required int y}) => _shell(
      id: 2002,
      text: 'Detached view',
      bounds: _rect(x, y, 419, 837),
      style: SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.CLOSE,
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

  Future<void> pumpDisplay(WidgetTester tester, List<VShell> shells) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
          value: VDisplay()
            ..id = 7
            ..shells = shells
            ..mainShellId = shells.first.id),
    ));
    await tester.pumpAndSettle();
  }

  testWidgets('a pane placed past the viewport is moved inside it', (tester) async {
    sizeViewport(tester);
    // 2573 is what the monitor-wide clamp leaves for a 419-wide shell on a 2992-wide screen.
    await pumpDisplay(tester, [_workbench(), _detached(x: 2573, y: 152)]);

    final rect = tester.getRect(find.text('Detached view'));
    expect(rect.right, lessThanOrEqualTo(_viewport.width),
        reason: 'drawn past the edge it is clipped away, and the detach looks like a no-op');
    expect(rect.left, greaterThanOrEqualTo(0.0));
  });

  testWidgets('a pane that already fits is not moved', (tester) async {
    sizeViewport(tester);
    await pumpDisplay(tester, [_workbench(), _detached(x: 300, y: 200)]);

    final pane = tester.getRect(find.byType(FloatingShellChromeScope));
    expect(pane.left, 300);
    expect(pane.top, 200);
  });

  // A floating shell's bounds say where it sits on SCREEN. This offset places it inside the window
  // drawing it, so the window's own position has to come off. The two agreed only while the main
  // shell was pinned to (0,0); once a window may sit anywhere, forgetting the subtraction draws the
  // workbench's drag feedback one whole window-origin away from the cursor it is tracking -- and by
  // more the further from the screen corner the window is.
  testWidgets('a pane is placed in window space, not screen space', (tester) async {
    sizeViewport(tester);
    // The window sits at (856,347) on screen; the pane is at (1000,600) on screen.
    await pumpDisplay(
        tester, [_workbench(x: 856, y: 347), _detached(x: 1000, y: 600)]);

    final pane = tester.getRect(find.byType(FloatingShellChromeScope));
    expect(pane.left, 1000 - 856,
        reason: 'a screen x drawn verbatim lands a window-width off to the right');
    expect(pane.top, 600 - 347,
        reason: 'and a screen y lands below by the title bar and whatever else is above the window');
  });

  testWidgets('a pane in a window at the screen corner is unaffected', (tester) async {
    sizeViewport(tester);
    await pumpDisplay(tester, [_workbench(), _detached(x: 300, y: 200)]);

    final pane = tester.getRect(find.byType(FloatingShellChromeScope));
    expect(pane.left, 300, reason: 'nothing to subtract, so nothing changes');
    expect(pane.top, 200);
  });

  // The other direction, and the one that is easy to get wrong twice: what the client reports back
  // has to be in the space Java keeps a shell's bounds in -- on screen -- not the space it is drawn
  // in. Reporting the drawn offset makes Java believe the shell moved to wherever that lands on
  // screen, so the next update arrives with the window's origin taken off a second time: the shell
  // is drawn correctly for one frame and then walks a whole window-origin away. Asserting only what
  // is drawn cannot see that; the two directions have to be inverses, so this asserts the report.
  testWidgets('the position reported back to Java is in screen space', (tester) async {
    sizeViewport(tester);
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    EquoCommService.commForTesting.clearSentFrames();

    await pumpDisplay(
        tester, [_workbench(x: 856, y: 347), _detached(x: 1000, y: 600)]);
    // The shell reports its geometry once the first frame is up.
    await tester.pump();

    final setBounds = EquoCommService.commForTesting.sentFrames
        .map(EquoCommBase.decodeFrame)
        .where((f) => f.$1.startsWith('Shell/') && f.$1.endsWith('/SetBounds'))
        .toList();
    expect(setBounds, isNotEmpty,
        reason: 'the shell reports its geometry after the first frame');

    final body = jsonDecode(setBounds.last.$2) as Map<String, dynamic>;
    expect(body['x'], 1000,
        reason: 'reporting the drawn offset tells Java the shell moved left by the window x');
    expect(body['y'], 600,
        reason: 'and up by the title bar and whatever else is above the window');
  });
}
