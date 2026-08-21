// CanvasImpl (canvas_evolve.dart) used to never paint a childless Canvas's own explicit
// background color -- state.background was computed (`bg`) but nothing painted it. Painting it
// isn't just a matter of *whether*: a Canvas can have BOTH its own background color AND its own
// GC-drawn content (a JFace ruler both sets its background and draws digits via PaintListener/GC
// on top of it), and wrapWithGCOverlay's Stack composites the GC overlay above the plain content
// layer -- so painting the color into that content layer would sit on top of, and hide, the GC
// drawing. The fix paints the color as the bottom layer of the GC Stack instead (gated on
// hasOwnBackground, set only when the app actually calls setBackground), so it sits under any
// hand-drawn GC content rather than fighting it for the same layer.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

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

const _navyBlue = Color(0xFF1A3458);

VCanvas _childlessCanvas({VColor? background, bool hasOwnBackground = true}) => VCanvas()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..hasOwnBackground = hasOwnBackground
  ..background = background
  ..bounds = _rect(0, 0, 1200, 38);

bool _hasColoredBackdrop(WidgetTester tester, Color color) =>
    tester.widgetList<ColoredBox>(find.byType(ColoredBox)).any((w) => w.color == color) ||
    tester
        .widgetList<DecoratedBox>(find.byType(DecoratedBox))
        .any((w) => (w.decoration as BoxDecoration?)?.color == color);

bool _hasAnyOpaqueBackdrop(WidgetTester tester) =>
    tester.widgetList<ColoredBox>(find.byType(ColoredBox)).isNotEmpty ||
    tester
        .widgetList<DecoratedBox>(find.byType(DecoratedBox))
        .any((w) => (w.decoration as BoxDecoration?)?.color != null);

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets(
      'a childless Canvas with an explicit background color and no backgroundImage paints that flat color',
      (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = true);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: CanvasSwt<VCanvas>(
          value: _childlessCanvas(background: _vColor(0x1A, 0x34, 0x58))),
    ));
    await tester.pumpAndSettle();

    expect(_hasColoredBackdrop(tester, _navyBlue), isTrue,
        reason: 'a childless Canvas must paint its own explicit background color even without a '
            'backgroundImage or a PaintListener, matching upstream SWT and Composite\'s childless path');
  });

  testWidgets(
      'a childless Canvas with no explicit background stays transparent (GC-only leaf, e.g. a JFace ruler)',
      (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = true);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: CanvasSwt<VCanvas>(
          value: _childlessCanvas(hasOwnBackground: false)),
    ));
    await tester.pumpAndSettle();

    expect(_hasAnyOpaqueBackdrop(tester), isFalse,
        reason: 'a Canvas that never called setBackground draws entirely via its own GC/'
            'PaintListener; an opaque fill here would sit on top of that drawing (wrap()\'s Stack '
            'order) and hide it -- this is the ruler regression from painting unconditionally');
  });

  testWidgets(
      'an explicit background sits under the GC overlay, not over it (a ruler draws its own '
      'digits via GC on top of its own background)', (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = true);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: CanvasSwt<VCanvas>(
          value: _childlessCanvas(background: _vColor(0xE0, 0xE0, 0xE0))),
    ));
    await tester.pumpAndSettle();

    bool backgroundUnderGc = false;
    for (final stack in tester.widgetList<Stack>(find.byType(Stack))) {
      int? bgIndex;
      int? gcIndex;
      for (var i = 0; i < stack.children.length; i++) {
        final c = stack.children[i];
        if (c is Positioned && c.child is ColoredBox) bgIndex = i;
        if (c is Positioned && c.child is IgnorePointer) gcIndex = i;
      }
      if (bgIndex != null && gcIndex != null) {
        backgroundUnderGc = bgIndex < gcIndex;
        break;
      }
    }

    expect(backgroundUnderGc, isTrue,
        reason: 'the background color must be the bottom layer of the GC Stack so hand-drawn GC '
            'content (e.g. a ruler\'s line-number digits) paints on top of it, not the other way '
            'around');
  });
}
