// A small, off-origin single Shell used to be force-stretched to fill the whole viewport: the
// "no shell independently qualifies as main" fallback in DisplaySwt.build() promoted the lone
// shell unconditionally, regardless of size, tiling its backgroundImage across the oversized area.
// The fallback now reuses _computeIsMainShell's 0.8-of-viewport ratio, so a small lone shell falls
// through to the existing floating/windowed chrome (FloatingShellChromeScope) instead.

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

// Mirrors LoginShell.java: an 800x600 Shell positioned off-origin via setLocation(50, 50), with
// setBackgroundImage(...). Non-modal, one child so the shell doesn't take Shell's childless-Canvas
// painting shortcut (see shell_background_paint_test.dart).
VShell _loginShellValue() => VShell()
  ..id = 1
  ..style = SWT.SHELL_TRIM
  ..text = 'login'
  ..bounds = _rect(50, 50, 800, 600)
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
      'a small off-origin lone Shell renders windowed at its own bounds, not stretched to fill the viewport',
      (tester) async {
    // A browser viewport much larger than the shell's declared 800x600 -- the scenario from the
    // report (an 800x600 dialog-sized Shell hosted alone in a full-size browser window).
    tester.view.physicalSize = const Size(1600, 1000);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    final shell = _loginShellValue();

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
      reason: 'a lone shell that is far from filling the viewport must not be promoted to "main" '
          'and force-stretched -- it should keep its own declared bounds',
    );

    expect(
      find.byType(FloatingShellChromeScope),
      findsOneWidget,
      reason: 'a shell that does not qualify as main must render through the existing '
          'floating/windowed chrome instead of Positioned.fill',
    );

    final decoratedBoxWithImage = tester
        .widgetList<DecoratedBox>(find.byType(DecoratedBox))
        .where((w) => (w.decoration as BoxDecoration?)?.image != null);
    expect(decoratedBoxWithImage, isNotEmpty, reason: 'the backgroundImage must still paint');

    final imageBoxFinder = find.byWidgetPredicate(
        (w) => w is DecoratedBox && (w.decoration as BoxDecoration?)?.image != null);
    final renderedSize = tester.getSize(imageBoxFinder.first);
    expect(
      renderedSize.width,
      lessThan(900),
      reason: 'the backdrop must be painted within the shell\'s own ~800-wide body, '
          'not stretched across the 1600-wide viewport (which is what made the tiled '
          'background image look repeated far beyond its native size)',
    );
  });
}
