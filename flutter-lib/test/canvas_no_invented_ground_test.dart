// A Canvas whose background the theme discards has no ground of its own, and the Canvas theme's
// drawing surface is not a stand-in for it: the surrounding Composite paints a different token, so
// behind a Canvas the size of its own label that surface reads as a block hugging the label. Only
// a light theme shows it -- under dark both tokens are colorScheme.surface.
//
// Two layers paint the ground: the GC's erase-to-background, which covers everything once there is
// a shape, and the ColoredBox under the GC Stack, which shows when there is none. Each is asserted
// on the mount where its probe discriminates.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _canvasId = 835773570;

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

/// What the reporting application sets on every control of the dialog, links included.
const _appTurquoise = Color(0xFF2E97C4);

/// A Hyperlink: a childless Canvas the size of its own text, carrying the background the
/// application set on it and drawing its label through a PaintListener.
VCanvas _link() => VCanvas()
  ..id = _canvasId
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..background = _vColor(46, 151, 196)
  ..hasOwnBackground = true
  ..bounds = _rect(20, 260, 120, 18);

void _deliver(String actionId, Map<String, dynamic> json) {
  final action = utf8.encode(actionId);
  final body = utf8.encode(jsonEncode(json));
  final out = Uint8List(2 + action.length + body.length);
  out[0] = (action.length >> 8) & 0xFF;
  out[1] = action.length & 0xFF;
  out.setRange(2, 2 + action.length, action);
  out.setRange(2 + action.length, out.length, body);
  EquoCommService.commForTesting.receiveBinary(out);
}

/// `Hyperlink.paintText` ends in `GC.drawText(text, x, y, true)` -- transparent, so the GC itself
/// never fills a background.
void _paintLinkLabel() {
  _deliver('GC/$_canvasId/drawTextStringintintboolean',
      {'string': 'Forgot My Password', 'x': 0, 'y': 0, 'isTransparent': true});
  _deliver('GC/$_canvasId/gcDispose', {'fullRepaint': true});
}

Future<void> _pumpDrawingLink(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(
        value: VDisplay()
          ..shells = [
            VShell()
              ..id = 1
              ..style = SWT.SHELL_TRIM
              ..text = 'shell'
              ..bounds = _rect(0, 0, 500, 300)
              ..children = [_link()]
          ]),
  ));
  await tester.pumpAndSettle();
  _paintLinkLabel();
  await tester.pumpAndSettle();
}

Future<void> _pumpBareCanvas(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: CanvasSwt<VCanvas>(value: _link()),
  ));
  await tester.pumpAndSettle();
}

/// The erase the GC paints under the label.
ScenePainter _linkScene(WidgetTester tester) {
  final scenes = tester
      .widgetList<CustomPaint>(find.byType(CustomPaint, skipOffstage: false))
      .map((p) => p.painter)
      .whereType<ScenePainter>()
      .where((s) => s.shapes.isNotEmpty)
      .toList();
  expect(scenes, hasLength(1), reason: "the Canvas's GC overlay must hold the drawText");
  return scenes.single;
}

/// The fill at the bottom of the Canvas's own GC Stack, or null when it paints none.
Color? _groundUnderGc(WidgetTester tester) {
  for (final stack in tester.widgetList<Stack>(find.byType(Stack))) {
    Color? ground;
    var sawGc = false;
    for (final child in stack.children) {
      if (child is! Positioned) continue;
      if (child.child is ColoredBox) ground ??= (child.child as ColoredBox).color;
      if (child.child is IgnorePointer) sawGc = true;
    }
    if (sawGc) return ground;
  }
  return null;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  group('the erase under a drawing', () {
    testWidgets('erases to nothing when the theme discarded the application colour',
        (tester) async {
      setConfigFlags(ConfigFlags()..use_swt_colors = false);
      await _pumpDrawingLink(tester);

      expect(_linkScene(tester).bg.a, 0,
          reason: 'this is the layer that shows: with the application colour discarded the Canvas '
              'has no ground of its own, and erasing to the theme drawing surface paints a block '
              'the surrounding Composite is not painted in');
    });

    testWidgets('still erases to the application colour when the application owns the colours',
        (tester) async {
      setConfigFlags(ConfigFlags()..use_swt_colors = true);
      await _pumpDrawingLink(tester);

      expect(_linkScene(tester).bg, _appTurquoise,
          reason: 'standing down is about the substitute, not about the colour: where the '
              'application colour survives it is still what the Canvas erases to');
    });
  });

  group('the fill under the GC stack, with nothing drawn', () {
    testWidgets('paints none when the theme discarded the application colour', (tester) async {
      setConfigFlags(ConfigFlags()..use_swt_colors = false);
      await _pumpBareCanvas(tester);

      expect(_groundUnderGc(tester), isNull,
          reason: 'the same invented ground, one layer down: with nothing drawn the erase above '
              'it is transparent, so this is what would be on screen');
    });

    testWidgets('paints the application colour when the application owns the colours',
        (tester) async {
      setConfigFlags(ConfigFlags()..use_swt_colors = true);
      await _pumpBareCanvas(tester);

      // Also validates the probe above: it does find a fill when there is one to find.
      expect(_groundUnderGc(tester), _appTurquoise);
    });
  });
}
