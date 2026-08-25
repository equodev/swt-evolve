// A lone Shell can reach its first Dart build before Java has sized it (width/height still unset).
// It used to fail the "fills the viewport" gate that guards the lone-shell promotion, fall through
// to the windowed chrome, and get clamped to that chrome's 200x150 minimum -- which the chrome then
// sent back to Java as the shell's real bounds. From then on the shell genuinely was a small
// off-origin window, so it could never qualify as main again and the whole app stayed dialog-sized.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/shell_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

// Mirrors GallerySnippet: `new Shell(display)` with no setSize(), serialized before Java has
// resolved its bounds against the Display.
VShell _unsizedShellValue() => VShell()
  ..id = 1
  ..style = SWT.SHELL_TRIM
  ..text = 'Equo SWT Gallery'
  ..bounds = _rect(0, 0, -1, -1)
  ..children = [
    VComposite()
      ..id = 2
      ..style = 0
      ..bounds = _rect(0, 0, 100, 100)
  ];

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a lone Shell Java has not sized yet is promoted to main and fills the viewport',
      (tester) async {
    tester.view.physicalSize = const Size(1400, 1800);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    final shell = _unsizedShellValue();

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: VDisplay()..shells = [shell]),
    ));
    await tester.pumpAndSettle();

    expect(
      shell.bounds,
      allOf(
        predicate<VRectangle?>((b) => b?.x == 0 && b?.y == 0, 'x=0, y=0'),
        predicate<VRectangle?>((b) => b?.width == 1400 && b?.height == 1800, 'width=1400, height=1800'),
      ),
      reason: 'an unsized lone shell tracks the Display bounds, so it must be stretched to the '
          'viewport instead of being clamped to the windowed chrome minimum',
    );

    expect(
      find.byType(FloatingShellChromeScope),
      findsNothing,
      reason: 'the main shell renders through Positioned.fill, not the floating/windowed chrome '
          '-- the chrome would push its invented geometry back to Java as the real bounds',
    );
  });
}
