// A GC stays under the parent it was mounted in, so whether it paints an owner-drawn Table or Tree
// row is looked up once: a Canvas painting frame after frame walks its ancestors for a row no more.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gc_evolve.dart';

import 'delivery/support/deliver.dart';
import 'support/gc_frames.dart';

const int _canvasId = 4721;

VCanvas _canvas() => VCanvas()
  ..swt = 'Canvas'
  ..id = _canvasId
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 120
    ..height = 40);

Future<void> _settle(WidgetTester tester) async {
  for (var i = 0; i < 20; i++) {
    await tester.pump(Duration.zero);
  }
}

void main() {
  testWidgets('a Canvas painting frame after frame looks for an owner-drawn row once', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 120, height: 40, child: CanvasSwt(value: _canvas())),
    ));
    await _settle(tester);

    Future<void> paint(int i) async {
      receive('GC/$_canvasId/b', gcOp('fillRectangleintintintint', [i, 2, 10, 10]));
      await deliverFrame('GC/$_canvasId/gcDispose', {'fullRepaint': true});
      await _settle(tester);
    }

    await paint(0);
    final after = gcRowLookups;
    for (var i = 1; i <= 10; i++) {
      await paint(i);
    }

    expect(gcRowLookups - after, 0);
  });
}
