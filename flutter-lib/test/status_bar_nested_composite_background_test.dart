import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/composite_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/toolbar_theme_extension.dart';

VRectangle _rect(int x, int width, int height) => VRectangle()
  ..x = x
  ..y = 0
  ..width = width
  ..height = height;

VComposite _composite(int id, VRectangle bounds, [List<VControl>? children]) => VComposite()
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = bounds
  ..children = children;

/// A status-line contribution is usually a Composite holding further Composites (a combo with its
/// label, a group of status fields), or an owner-drawn Canvas (a drag handle, a progress readout).
/// None of them sets a color, so on a native bar they all show the bar's one ground.
VComposite _statusBar() => _composite(1, _rect(0, 600, 36), [
      _composite(2, _rect(0, 300, 30), [
        _composite(3, _rect(0, 140, 30)),
        _composite(4, _rect(140, 160, 30)),
      ]),
      VCanvas()
        ..id = 5
        ..style = SWT.NONE
        ..enabled = true
        ..visible = true
        ..bounds = _rect(300, 200, 22)
        ..children = [
          // What the application reports for a control it never colored: its platform default.
          _composite(6, _rect(5, 195, 22))
            ..background = (VColor()
              ..alpha = 0xFF
              ..red = 0xF8
              ..green = 0xF8
              ..blue = 0xF8),
        ],
    ]);

void main() {
  testWidgets('controls nested in a status bar contribution paint the bar\'s ground',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 36,
        child: StatusBarComposite(value: _statusBar()),
      ),
    ));
    await tester.pumpAndSettle();

    final context = tester.element(find.byType(StatusBarComposite));
    final barGround =
        Theme.of(context).extension<ToolBarThemeExtension>()!.compositeBackgroundColor;

    final grounds = tester
        .widgetList<ColoredBox>(
            find.descendant(of: find.byType(StatusBarComposite), matching: find.byType(ColoredBox)))
        .map((box) => box.color)
        .toSet();

    expect(grounds, {barGround},
        reason: 'a Composite or Canvas with no color of its own inside the status bar must not fall '
            'back to a generic theme default and paint a block of a different color');
  });

  testWidgets('the same contributions in the main toolbar keep the toolbar\'s ground', (tester) async {
    const Color toolbarGround = Color(0xFF123456);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 36,
        child: ToolbarComposite(
            value: _statusBar(), backgroundColor: toolbarGround, useBoundsLayout: true),
      ),
    ));
    await tester.pumpAndSettle();

    final context = tester.element(find.byType(ToolbarComposite).first);
    final compositeDefault =
        Theme.of(context).extension<CompositeThemeExtension>()!.backgroundColor;
    const appDefault = Color(0xFFF8F8F8);

    final grounds = tester
        .widgetList<ColoredBox>(
            find.descendant(of: find.byType(ToolbarComposite).first, matching: find.byType(ColoredBox)))
        .map((box) => box.color)
        .toSet();

    expect(grounds, isNot(contains(compositeDefault)),
        reason: 'a nested contribution painted the generic Composite color inside the toolbar');
    expect(grounds, isNot(contains(appDefault)),
        reason: 'a Canvas handed the application\'s color to what it hosts inside the toolbar');
  });
}
