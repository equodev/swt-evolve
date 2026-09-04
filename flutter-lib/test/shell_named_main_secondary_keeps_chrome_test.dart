// "At the origin and covering most of the viewport" describes the workbench shell and a
// near-maximized non-modal secondary window equally well, and a shell the geometry heuristic
// promotes to main renders full-bleed -- no title bar, no close button, whatever its
// SWT.TITLE/SWT.CLOSE bits say. Java names the shell that tracks the viewport in
// VDisplay.mainShellId; the heuristic only decides when it names none.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/shell_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

// The reported viewport.
const _viewport = Size(1712, 782);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _shell({required int id, required String text, required int style, required VRectangle bounds}) =>
    VShell()
      ..id = id
      ..style = style
      ..text = text
      ..bounds = bounds
      // One child so the shell doesn't take Shell's childless-Canvas painting shortcut
      // (see shell_background_paint_test.dart).
      ..children = [
        VComposite()
          ..id = id + 1
          ..style = 0
          ..bounds = _rect(0, 0, 100, 100)
      ];

// The workbench shell: SHELL_TRIM, maximized over the whole viewport.
VShell _workbenchShell() => _shell(
      id: 692167805,
      text: 'workbench',
      style: 33555696,
      bounds: _rect(0, 0, _viewport.width.toInt(), _viewport.height.toInt()),
    );

// A secondary window with full window trim (SHELL_TRIM | BORDER), non-modal, opened at viewport
// size -- indistinguishable from the workbench shell on geometry alone.
VShell _secondaryShell() => _shell(
      id: 1303218675,
      text: 'Secondary Window',
      style: 33557744,
      bounds: _rect(0, 0, _viewport.width.toInt(), _viewport.height.toInt()),
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

  testWidgets(
      'a full-viewport non-modal secondary shell renders windowed with its chrome when Java names '
      'another shell as main', (tester) async {
    sizeViewport(tester);

    final workbench = _workbenchShell();
    final secondary = _secondaryShell();

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
        value: VDisplay()
          ..shells = [workbench, secondary]
          ..mainShellId = workbench.id,
      ),
    ));
    await tester.pumpAndSettle();

    expect(
      find.byType(FloatingShellChromeScope),
      findsOneWidget,
      reason: 'only the secondary shell is windowed; the named main shell renders full-bleed',
    );
    expect(
      find.descendant(
        of: find.byType(FloatingShellChromeScope),
        matching: find.text('Secondary Window'),
      ),
      findsOneWidget,
      reason: 'the title bar draws the shell text, so the window has a title bar and a close button',
    );
    expect(
      tester.getSize(find.byType(FloatingShellChromeScope)).width,
      lessThan(_viewport.width),
      reason: 'the floating chrome clamps a window to 90% of the viewport, so a windowed shell is '
          'narrower than the page it sits on',
    );
  });

  testWidgets('the shell Java names as main still renders full-bleed', (tester) async {
    sizeViewport(tester);

    final workbench = _workbenchShell()..bounds = _rect(0, 0, 1024, 768);
    final secondary = _secondaryShell();

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
        value: VDisplay()
          ..shells = [workbench, secondary]
          ..mainShellId = workbench.id,
      ),
    ));
    await tester.pumpAndSettle();

    expect(
      workbench.bounds,
      predicate<VRectangle?>((b) =>
          b?.x == 0 &&
          b?.y == 0 &&
          b?.width == _viewport.width.toInt() &&
          b?.height == _viewport.height.toInt()),
      reason: 'the main shell is slaved to the viewport',
    );
  });

  // An update that names no main shell keeps the geometry heuristic, so a Java side that does not
  // send the id cannot strand the client with every shell windowed and nothing filling the page.
  testWidgets('with no main shell named, the geometry heuristic still elects one', (tester) async {
    sizeViewport(tester);

    final workbench = _workbenchShell();

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: VDisplay()..shells = [workbench]),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(FloatingShellChromeScope), findsNothing);
  });

  // The named shell can be missing from the update that carries the name (it stops being visible in
  // the same round-trip). That is a gap in what arrived, not "none of these is the main shell", so
  // the heuristic decides rather than leaving the page with no full-bleed shell at all.
  testWidgets('a named main shell absent from the update falls back to the geometry heuristic',
      (tester) async {
    sizeViewport(tester);

    final workbench = _workbenchShell();

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
        value: VDisplay()
          ..shells = [workbench]
          ..mainShellId = 999999999,
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(FloatingShellChromeScope), findsNothing);
  });
}
