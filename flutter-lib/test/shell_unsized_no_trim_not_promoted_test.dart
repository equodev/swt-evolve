// The lone-shell promotion used to accept any unsized Shell sitting at the origin. An Eclipse
// splash reaches Dart exactly like that -- WorkbenchPlugin.getSplashShell() wraps the launcher's
// splash handle through Shell.internal_new(), which builds it with SWT.NO_TRIM and no bounds -- so
// during startup, while it is the only shell, it was promoted to main, stretched to the whole
// viewport and had its background image tiled across the page.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/shell_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

// A 1x1 transparent PNG -- just enough for MemoryImage to decode successfully.
final _tinyPngBytes = base64Decode(
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY'
    '42YAAAAASUVORK5CYII=');

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

// The splash Shell as it actually arrives on web: style 33554440
// (LEFT_TO_RIGHT | NO_TRIM -- Decorations.checkStyle strips CLOSE/TITLE/MIN/MAX/RESIZE/BORDER off
// NO_TRIM), no bounds yet, and the splash bitmap as its background image.
VShell _splashShellValue() => VShell()
  ..id = 1
  ..style = SWT.LEFT_TO_RIGHT | SWT.NO_TRIM
  ..bounds = _rect(0, 0, 0, 0)
  ..backgroundImage = (VImage()..imageData = (VImageData()..data = _tinyPngBytes))
  ..children = [
    VComposite()
      ..id = 2
      ..style = 0
      ..bounds = _rect(0, 0, 100, 100)
  ];

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets(
      'a lone unsized Shell with no resize trim (a splash) is not promoted to main nor stretched to the viewport',
      (tester) async {
    // The viewport from the reported capture.
    tester.view.physicalSize = const Size(1600, 1000);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    final shell = _splashShellValue();

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: VDisplay()..shells = [shell]),
    ));
    await tester.pumpAndSettle();

    expect(
      shell.bounds,
      isNot(predicate<VRectangle?>(
          (b) => b?.width == 1600 && b?.height == 1000, 'stretched to the viewport')),
      reason: 'only the display-tracking main shell has its bounds overwritten to the viewport; a '
          'shell with no resize trim never tracks the viewport (DisplayBridge.shouldTrackDisplayBounds '
          'applies the same rule on the Java side)',
    );

    expect(
      find.byType(FloatingShellChromeScope),
      findsOneWidget,
      reason: 'a shell that does not qualify as main renders through the floating/windowed chrome, '
          'not Positioned.fill',
    );

    final imageBoxFinder = find.byWidgetPredicate(
        (w) => w is DecoratedBox && (w.decoration as BoxDecoration?)?.image != null);
    if (imageBoxFinder.evaluate().isNotEmpty) {
      expect(
        tester.getSize(imageBoxFinder.first).width,
        lessThan(1600.0 * 0.8),
        reason: 'the background image must not be tiled across the viewport',
      );
    }
  });

  // LoginShell carries the same style *value* as the splash -- SWT.BACKGROUND and SWT.NO_TRIM are
  // both 1 << 3 -- so the size is all that separates them, and the gate must not start reading the
  // shared bit as "splash".
  testWidgets('a sized Shell with the same style bits as the splash still renders windowed',
      (tester) async {
    tester.view.physicalSize = const Size(1600, 1000);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    final shell = _splashShellValue()..bounds = _rect(50, 50, 800, 600);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: VDisplay()..shells = [shell]),
    ));
    await tester.pumpAndSettle();

    expect(
      shell.bounds,
      allOf(
        predicate<VRectangle?>((b) => b?.x == 50 && b?.y == 50, 'x=50, y=50'),
        predicate<VRectangle?>((b) => b?.width == 800 && b?.height == 600, 'width=800, height=600'),
      ),
      reason: 'it keeps its own declared bounds instead of being stretched and tiled',
    );
    expect(find.byType(FloatingShellChromeScope), findsOneWidget);
  });

  // The two tests above reach the promotion through the geometry heuristic. A Display update that
  // carries VDisplay.mainShellId takes a different route -- _isMainShell answers from the id alone
  // and the heuristic never runs -- so the splash needs its own guard on that route too.
  group('with a main shell named in the Display update', () {
    // The workbench shell as it arrives once it exists: full trim, covering the viewport.
    VShell workbenchShellValue() => VShell()
      ..id = 10
      ..style = SWT.LEFT_TO_RIGHT | SWT.SHELL_TRIM
      ..text = 'workbench'
      ..bounds = _rect(0, 0, 1600, 1000)
      ..children = [
        VComposite()
          ..id = 11
          ..style = 0
          ..bounds = _rect(0, 0, 100, 100)
      ];

    testWidgets('a splash alongside the named main shell is neither promoted nor stretched',
        (tester) async {
      tester.view.physicalSize = const Size(1600, 1000);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);
      addTearDown(tester.view.resetDevicePixelRatio);

      final splash = _splashShellValue();
      final workbench = workbenchShellValue();

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: DisplaySwt(
          value: VDisplay()
            ..shells = [splash, workbench]
            ..mainShellId = workbench.id,
        ),
      ));
      await tester.pumpAndSettle();

      expect(
        splash.bounds,
        isNot(predicate<VRectangle?>(
            (b) => b?.width == 1600 && b?.height == 1000, 'stretched to the viewport')),
        reason: 'only the named main shell has its bounds overwritten to the viewport',
      );

      final imageBoxFinder = find.byWidgetPredicate(
          (w) => w is DecoratedBox && (w.decoration as BoxDecoration?)?.image != null);
      if (imageBoxFinder.evaluate().isNotEmpty) {
        expect(
          tester.getSize(imageBoxFinder.first).width,
          lessThan(1600.0 * 0.8),
          reason: 'the splash image must not be tiled across the viewport',
        );
      }
    });

    // Java sends 0 when no shell qualifies -- which is what a lone trimless splash produces, since
    // DisplayBridge.shouldTrackDisplayBounds rules it out on the same resize-trim cut. The client
    // must read that as "decide it yourself", not as "this shell is main".
    testWidgets('a lone splash with mainShellId 0 still falls through to the heuristic',
        (tester) async {
      tester.view.physicalSize = const Size(1600, 1000);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);
      addTearDown(tester.view.resetDevicePixelRatio);

      final splash = _splashShellValue();

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: DisplaySwt(
          value: VDisplay()
            ..shells = [splash]
            ..mainShellId = 0,
        ),
      ));
      await tester.pumpAndSettle();

      expect(
        splash.bounds,
        isNot(predicate<VRectangle?>(
            (b) => b?.width == 1600 && b?.height == 1000, 'stretched to the viewport')),
      );
      expect(find.byType(FloatingShellChromeScope), findsOneWidget);
    });
  });
}
