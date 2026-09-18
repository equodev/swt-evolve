// The GC backdrop stands in for SWT's erase-to-background. It is the surface the application's
// drawing lands on, not part of that drawing, so it follows the Canvas's own background resolved
// the way every other control resolves one -- the theme's colour unless use_swt_colors asks for
// the application's. Taking the GC's colour here made an owner-drawn control erase in the
// application's colour while the trim around it used the theme's, so the control stood out as a
// block against its host (an IDE status-line progress readout is the case that showed it).

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _canvasId = 771120044;

/// The colour the application set on the control. Distinct from anything the theme resolves to, so
/// a match can only come from the application's value.
const _appBackground = Color(0xFF48484C);

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

/// An owner-drawn control: its own background, and all of its content drawn through its GC.
VCanvas _ownerDrawnCanvas() => VCanvas()
  ..id = _canvasId
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..background = _vColor(0x48, 0x48, 0x4C)
  ..hasOwnBackground = true
  ..bounds = _rect(0, 0, 320, 22);

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

/// One drawn op, so the overlay has shapes and therefore paints a backdrop at all.
void _paintReadout() {
  _deliver('GC/$_canvasId/drawTextStringintintboolean',
      {'string': 'Detect installed JVMs: (0%)', 'x': 0, 'y': 4, 'isTransparent': true});
  _deliver('GC/$_canvasId/gcDispose', {'fullRepaint': true});
}

Future<ScenePainter> _pumpAndReadBackdrop(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.dark,
    contentWidget: CanvasSwt<VCanvas>(value: _ownerDrawnCanvas()),
  ));
  await tester.pumpAndSettle();
  _paintReadout();
  await tester.pumpAndSettle();

  final scenes = tester
      .widgetList<CustomPaint>(find.byType(CustomPaint, skipOffstage: false))
      .map((p) => p.painter)
      .whereType<ScenePainter>()
      .where((s) => s.shapes.isNotEmpty)
      .toList();
  expect(scenes, hasLength(1), reason: 'the Canvas\'s GC overlay must hold the drawn op');
  return scenes.single;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('the backdrop takes the theme when the application\'s colours were not asked for',
      (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = false);

    final scene = await _pumpAndReadBackdrop(tester);

    expect(scene.bg, isNot(_appBackground),
        reason: 'with use_swt_colors off the trim around this control is themed, so erasing in '
            'the application\'s colour leaves the control as a block against its host');
  });

  testWidgets('the backdrop keeps the application\'s colour when use_swt_colors asks for it',
      (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = true);

    final scene = await _pumpAndReadBackdrop(tester);

    expect(scene.bg, _appBackground,
        reason: 'use_swt_colors means the application owns the palette everywhere, so the erase '
            'must keep the colour it set');
  });
}
