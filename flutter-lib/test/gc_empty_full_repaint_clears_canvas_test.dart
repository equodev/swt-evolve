// A full repaint that draws nothing must leave the canvas blank, on screen and not only in the
// drawer's list.
//
// An Eclipse status-line progress readout (ProgressCanvasViewer's Canvas) paints its percentage
// inside SWT.Paint and draws nothing at all on the cycles between jobs. Those empty cycles cleared
// the drawer's shapes but never replaced the widget's own fallback copy, so the control kept
// rendering the last string it had ever painted — the readout froze at whatever percentage it first
// showed.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

import 'delivery/support/deliver.dart';

const int _canvasId = 4711;

VCanvas _canvas() => VCanvas()
  ..swt = 'Canvas'
  ..id = _canvasId
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 324
    ..height = 22);

Future<void> _drawString(String text) => deliverFrame(
      'GC/$_canvasId/drawStringStringintintboolean',
      {'string': text, 'x': 2, 'y': 2, 'isTransparent': true},
    );

Future<void> _endPaint() =>
    deliverFrame('GC/$_canvasId/gcDispose', {'fullRepaint': true});

/// What the control actually renders, as opposed to what the drawer is holding.
List<Shape> _paintedShapes(WidgetTester tester) {
  final painters = tester
      .widgetList<CustomPaint>(find.byType(CustomPaint))
      .map((w) => w.painter)
      .whereType<ScenePainter>();
  return [for (final p in painters) ...p.shapes];
}

Iterable<String> _paintedText(WidgetTester tester) =>
    _paintedShapes(tester).whereType<TextShape>().map((s) => s.text);

/// A gcDispose commit awaits its pending decodes before it takes effect.
Future<void> _settle(WidgetTester tester) async {
  for (var i = 0; i < 20; i++) {
    await tester.pump(Duration.zero);
  }
}

void main() {
  testWidgets('a full repaint that draws nothing stops painting the previous string',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 324,
        height: 22,
        child: CanvasSwt(value: _canvas()),
      ),
    ));
    await _settle(tester);

    // The job reports progress: one paint cycle draws the readout.
    await _drawString('Setup check: (0%)');
    await _endPaint();
    await _settle(tester);
    expect(_paintedText(tester), contains('Setup check: (0%)'),
        reason: 'sanity: a cycle that draws a string paints it');

    // The job is gone, so the next Paint draws nothing at all.
    await _endPaint();
    await _settle(tester);

    expect(_paintedText(tester), isEmpty,
        reason: 'a full repaint replaces the whole client area, so a cycle that draws nothing '
            'must leave nothing painted — otherwise the readout keeps showing a percentage no '
            'job is reporting any more');
  });
}
