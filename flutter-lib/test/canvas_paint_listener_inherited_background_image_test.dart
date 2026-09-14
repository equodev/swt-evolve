// A Canvas that paints via a PaintListener under a Shell with a backgroundImage (INHERIT_FORCE):
// SWT erases the Canvas to the ancestor's image before the listener runs, so the drawing lands on
// the image. The GC overlay reproduces that erase with ScenePainter's backdrop, but resolved it to
// a solid color even when the Canvas had no background of its own and the image was the thing it
// should erase to -- the backdrop was transparent only while there was nothing to draw, so the
// first drawText from the listener turned the Canvas into an opaque slab over the image.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

// A 1x1 transparent PNG -- just enough for MemoryImage to decode successfully.
final _tinyPngBytes = base64Decode(
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY'
    '42YAAAAASUVORK5CYII=');

const _canvasId = 835773569;

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

/// The login-screen shape: a Shell with a background image forced onto its children, and a
/// childless Canvas whose whole content comes from a PaintListener. `background` is the value
/// DartControl.getBackground() resolves for a Canvas that never called setBackground -- the
/// Shell's -- with hasOwnBackground left unset, exactly what reaches Flutter on the wire.
VShell _loginShell({required bool canvasHasOwnBackground, required bool shellHasImage}) =>
    VShell()
      ..id = 1
      ..style = SWT.SHELL_TRIM
      ..text = 'shell'
      ..bounds = _rect(0, 0, 800, 600)
      ..background = _vColor(240, 240, 240)
      ..backgroundImage =
          shellHasImage ? (VImage()..imageData = (VImageData()..data = _tinyPngBytes)) : null
      ..backgroundMode = SWT.INHERIT_FORCE
      ..children = [
        VCanvas()
          ..id = _canvasId
          ..style = SWT.NONE
          ..enabled = true
          ..visible = true
          ..background = _vColor(240, 240, 240)
          ..hasOwnBackground = canvasHasOwnBackground ? true : null
          ..bounds = _rect(40, 370, 720, 200)
      ];

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

/// What the login screen's PaintListener emits: one transparent drawText, then the Paint ends.
void _paintWaitingForConnection() {
  _deliver('GC/$_canvasId/drawTextStringintintboolean',
      {'string': 'Waiting for connection ....', 'x': 0, 'y': 184, 'isTransparent': true});
  _deliver('GC/$_canvasId/gcDispose', {'fullRepaint': true});
}

Future<void> _pump(WidgetTester tester,
    {required bool canvasHasOwnBackground, bool shellHasImage = true}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(
        value: VDisplay()
          ..shells = [
            _loginShell(
                canvasHasOwnBackground: canvasHasOwnBackground, shellHasImage: shellHasImage)
          ]),
  ));
  await tester.pumpAndSettle();
  _paintWaitingForConnection();
  await tester.pumpAndSettle();
}

ScenePainter _canvasScene(WidgetTester tester) {
  final scenes = tester
      .widgetList<CustomPaint>(find.byType(CustomPaint, skipOffstage: false))
      .map((p) => p.painter)
      .whereType<ScenePainter>()
      .where((s) => s.shapes.isNotEmpty)
      .toList();
  expect(scenes, hasLength(1),
      reason: 'the Canvas\'s GC overlay must hold the PaintListener\'s drawText');
  return scenes.single;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets(
      'a Canvas with no background of its own erases to the inherited backgroundImage, not to a solid color',
      (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = true);
    await _pump(tester, canvasHasOwnBackground: false);

    expect(_canvasScene(tester).bg.alpha, 0,
        reason: 'the ancestor already painted the image underneath; an opaque erase hides it '
            'and the Canvas reads as a solid slab over the Shell\'s background image');
  });

  testWidgets('a Canvas that set its own background keeps erasing to that color under an image',
      (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = true);
    await _pump(tester, canvasHasOwnBackground: true);

    expect(_canvasScene(tester).bg.alpha, 0xFF,
        reason: 'SWT\'s findBackgroundControl stops at a control with its own background, so '
            'the erase stays opaque -- the image must not leak through a Canvas that opted out');
  });

  testWidgets('without an inherited image the erase stays a solid color', (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_colors = true);
    await _pump(tester, canvasHasOwnBackground: false, shellHasImage: false);

    expect(_canvasScene(tester).bg.alpha, 0xFF,
        reason: 'a scoped repaint relies on the erase to clear the previous frame; only an '
            'ancestor image already painted underneath justifies making it transparent');
  });
}
