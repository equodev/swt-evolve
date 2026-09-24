// A Shell is a Decorations, and Decorations extends Canvas only by SWT's class hierarchy: it hosts
// controls, it is not a drawing surface. Its fallback background must therefore be the one every
// Composite inside it falls back to (CompositeThemeExtension.backgroundColor), not the Canvas
// drawing surface (CanvasThemeExtension.backgroundColor, the theme's `neutral`). The two differ in
// every theme, so painting the latter makes each child Composite read as an opaque block.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/canvas_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/composite_theme_extension.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

// An application that calls setBackground on its dialog Shells: hasOwnBackground is true, so the
// Shell paints a backdrop of its own. use_swt_colors is off, so the colour comes from the theme,
// not from the VColor here.
VShell _shellWithOwnBackground() => VShell()
  ..id = 1
  ..style = SWT.SHELL_TRIM
  ..text = 'shell'
  ..bounds = _rect(0, 0, 800, 600)
  ..hasOwnBackground = true
  ..background = (VColor()
    ..alpha = 0xFF
    ..red = 0x4D
    ..green = 0x50
    ..blue = 0x52)
  ..children = [
    VComposite()
      ..id = 2
      ..style = 0
      ..bounds = _rect(20, 20, 200, 60)
      ..children = [
        VComposite()
          ..id = 3
          ..style = 0
          ..bounds = _rect(0, 0, 100, 20)
      ]
  ];

Iterable<Color> _fills(WidgetTester tester, Finder root) => tester
    .widgetList<ColoredBox>(find.descendant(of: root, matching: find.byType(ColoredBox)))
    .map((w) => w.color)
    .where((c) => c.alpha != 0);

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a Shell with a background of its own paints the same surface its Composites do',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget:
          DisplaySwt(value: VDisplay()..shells = [_shellWithOwnBackground()]),
    ));
    await tester.pumpAndSettle();

    final context = tester.element(find.byType(ShellSwt<VShell>).first);
    final canvasSurface =
        Theme.of(context).extension<CanvasThemeExtension>()!.backgroundColor;
    final compositeSurface =
        Theme.of(context).extension<CompositeThemeExtension>()!.backgroundColor;

    expect(canvasSurface, isNot(compositeSurface),
        reason: 'the two theme surfaces must differ, or this test cannot see the defect');

    final fills = _fills(tester, find.byType(ShellSwt<VShell>).first).toSet();
    expect(fills, contains(compositeSurface));
    expect(fills, isNot(contains(canvasSurface)),
        reason: 'a Shell painting the Canvas drawing surface leaves every Composite inside it '
            'reading as an opaque block of a different colour');
  });
}
