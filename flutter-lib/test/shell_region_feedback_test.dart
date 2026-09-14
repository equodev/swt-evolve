// The Eclipse workbench shows where a dragged view will dock by opening a shell the size of the
// whole window, giving it no children, and clipping it to a region that is a few thin bars. Two
// things have to hold for that to be visible: the shell is clipped to the region (or it covers the
// application), and it is filled with the colour the application set (or the bars are painted in
// the theme's own background and vanish into what is behind them).
//
// This is the childless path -- CanvasImpl.buildComposite() takes an early branch when a
// Shell/Composite/Canvas has no children, so ShellImpl.buildComposite() is never reached (see
// shell_background_paint_test.dart). The gate that ignores an incidental SWT background still
// applies everywhere else; a region is what marks the colour as deliberate.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/region.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/region_clip.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/display_theme_extension.dart';

const _feedbackBlue = Color(0xFF2A6FD0);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VColor _vColor(int r, int g, int b) => VColor()
  ..alpha = 0xFF
  ..red = r
  ..green = g
  ..blue = b;

/// A window-sized, childless NO_TRIM|ON_TOP shell — the shape of E4's SplitFeedbackOverlay. It
/// carries no title, which is how the workbench's own feedback arrives.
VShell _overlay({List<int>? rects}) => VShell()
  ..id = 2
  ..style = SWT.NO_TRIM | SWT.ON_TOP
  ..bounds = _rect(0, 0, 800, 600)
  ..background = _vColor(0x2A, 0x6F, 0xD0)
  ..hasOwnBackground = true
  ..region = (rects == null ? null : (VRegion()..rects = rects))
  ..children = [];

/// The workbench window the feedback is shown over. The overlay is never alone on the Display —
/// it is a second shell, which is what puts it down the dialog branch of DisplaySwt's build, and
/// that branch is the one the application actually exercises.
VShell _workbench() => VShell()
  ..id = 1
  ..style = SWT.SHELL_TRIM
  ..text = 'workbench'
  ..bounds = _rect(0, 0, 800, 600)
  ..children = [];

Future<void> _pump(WidgetTester tester, VShell overlay) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(
        value: VDisplay()
          ..shells = [_workbench(), overlay]
          // Java names the main shell from parentage and the workbench layout, and that verdict
          // replaces the client's geometry heuristic. Leaving it unset lets the overlay be mistaken
          // for a main shell here, which is not the path the application takes.
          ..mainShellId = 1),
  ));
  await tester.pumpAndSettle();
}

bool _paintsColor(WidgetTester tester, Color color) => tester
    .widgetList<ColoredBox>(find.byType(ColoredBox))
    .any((w) => w.color == color);

/// The theme's own fill for a shaped shell, read from the tree under test so the expectation
/// follows the theme rather than restating a colour it would then stop tracking.
Color _themeDragFeedback(WidgetTester tester) =>
    Theme.of(tester.element(find.byType(DisplaySwt)))
        .extension<DisplayThemeExtension>()!
        .dragFeedbackColor;

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a shell shaped by a region is clipped to it', (tester) async {
    await _pump(tester, _overlay(rects: const [0, 0, 800, 2, 0, 598, 800, 2]));

    expect(find.byType(RegionClip), findsAtLeastNWidgets(1),
        reason: 'unclipped, a window-sized feedback shell covers the application');
  });

  testWidgets('a shell shaped by a region is filled by the theme, not by the window background',
      (tester) async {
    // The bars are the whole drawing, so the window background an ordinary control falls back to
    // would paint them in the colour of the application behind them.
    await _pump(tester, _overlay(rects: const [0, 0, 800, 2, 0, 598, 800, 2]));

    expect(_paintsColor(tester, _themeDragFeedback(tester)), isTrue);
  });

  testWidgets('use_swt_colors hands a shaped shell back to the application', (tester) async {
    // The theme's colour is a default, not an override: the flag that puts the application in
    // charge of every other colour puts it in charge of this one too.
    setConfigFlags(ConfigFlags()..use_swt_colors = true);

    await _pump(tester, _overlay(rects: const [0, 0, 800, 2, 0, 598, 800, 2]));

    expect(_paintsColor(tester, _feedbackBlue), isTrue);
  });

  testWidgets('a feedback shell sits where the application put it, not centred',
      (tester) async {
    // The workbench sizes its feedback to its own window, which is not the viewport Flutter is
    // laying out into. The shell asks for the origin, and honouring that is what puts the frames
    // over the area they describe -- centring a shell larger than the viewport shifts them by
    // hundreds of pixels, off the zone the drop is about and often off screen entirely.
    final oversized = _overlay(rects: const [0, 0, 1875, 2])
      ..bounds = _rect(0, 0, 1875, 1671);

    await _pump(tester, oversized);

    final clip = find.byType(RegionClip);
    expect(clip, findsAtLeastNWidgets(1));
    expect(tester.getTopLeft(clip.first), Offset.zero,
        reason: 'a shell the application placed at the origin must render at the origin');
  });

  testWidgets('a region clips the shell itself, not only what it contains', (tester) async {
    // A shell paints its own background across its whole area, and the feedback shell's area is the
    // whole window. Clipping only what the shell contains leaves that background covering the
    // application: the frame then appears over a filled window rather than over the workbench.
    await _pump(tester, _overlay(rects: const [0, 0, 800, 2]));

    final filled = find.byWidgetPredicate((w) =>
        w is Container &&
        w.decoration is BoxDecoration &&
        (w.decoration as BoxDecoration).color != null);
    expect(filled, findsAtLeastNWidgets(1));
    expect(
        find.ancestor(of: filled.first, matching: find.byType(RegionClip)),
        findsAtLeastNWidgets(1),
        reason: 'the shell background must be clipped by the region, not painted around it');
  });


  testWidgets('a shell with no region still leaves an incidental background to the theme',
      (tester) async {
    // The gate is not lifted for everything: a plain shell's background is usually the SWT default
    // widget grey, set without any visual intent, and the theme keeps winning there.
    await _pump(tester, _overlay(rects: null));

    expect(find.byType(RegionClip), findsNothing);
    expect(_paintsColor(tester, _feedbackBlue), isFalse);
  });
}
