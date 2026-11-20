// A custom-drawn control is a Canvas: the text its GC draws is its only name, so a test finds it by
// what it paints.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/utils/test_anchors.dart';
import 'support/gc_frames.dart';

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

/// `Hyperlink.paintText` ends in `GC.drawText(text, x, y, true)`, which reaches the client as the
/// flags form SWT defines it by.
void _paintLinkLabel() {
  receive('GC/$_canvasId/b', gcOp('drawTextStringintintint', ['Forgot My Password', 0, 0, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT]));
  receive('GC/$_canvasId/gcDispose', tagged({'fullRepaint': true}));
}

void main() {
  setUpAll(() => TestAnchors.enabled = true);
  tearDownAll(() => TestAnchors.enabled = false);

  testWidgets('a Canvas is found by the text its GC draws', (tester) async {
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

    expect(find.text('Forgot My Password'), findsNothing,
        reason: 'GC text is painted, not a Text widget: what it paints is its only exposure');
    final canvases = TestAnchors.nodes(['Canvas/'], paintedText: true);
    final link = canvases.singleWhere((n) => n['id'] == 'Canvas/$_canvasId');
    expect(link['text'], contains('Forgot My Password'));
  });
}
