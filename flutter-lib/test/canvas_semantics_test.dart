// A custom-drawn control is a Canvas: the text its GC draws is its only name, and where a click lands
// is what it acts on. Both have to survive the semantics tree the web builds for accessibility (and E2E).

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/semantics.dart' show SemanticsAction;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

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
/// never fills a background. That overload reaches the client as the flags form SWT defines it by.
void _paintLinkLabel() {
  _deliver('GC/$_canvasId/drawTextStringintintint', {
    'string': 'Forgot My Password',
    'x': 0,
    'y': 0,
    'flags': SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT
  });
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

void main() {
  testWidgets('text drawn through the GC labels the Canvas node', (tester) async {
    final semantics = tester.ensureSemantics();
    await _pumpDrawingLink(tester);

    expect(find.text('Forgot My Password'), findsNothing,
        reason: 'GC text is painted, not a Text widget: the label is its only exposure');
    expect(tester.getSemantics(find.bySemanticsIdentifier('Canvas/$_canvasId')).label,
        contains('Forgot My Password'));
    semantics.dispose();
  });

  testWidgets('the Canvas node exposes no tap action', (tester) async {
    final semantics = tester.ensureSemantics();
    await _pumpDrawingLink(tester);

    // The web engine turns a click on a tappable node into a tap with no position; a Canvas acts on
    // where it was clicked, so its clicks must stay pointer events.
    final data = tester.getSemantics(find.bySemanticsIdentifier('Canvas/$_canvasId')).getSemanticsData();
    expect(data.hasAction(SemanticsAction.tap), isFalse);
    semantics.dispose();
  });
}
