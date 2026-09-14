// A Canvas laid out shorter than the scrollbar thumb's 16px floor used to make _thumbSize()
// call clamp(16, trackSize) with lowerLimit > upperLimit, which throws ArgumentError. The call
// sits inside LayoutBuilder's builder, so the throw escaped the layout/paint pass and took the
// whole frame down with it -- every widget's update, not just this scrollbar's. On a GEF
// FigureCanvas that reads as a canvas which stops refreshing mid-interaction and only catches
// up once something remounts it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// A bar wide enough to scroll, so the Canvas actually renders it.
VScrollBar _bar(int id) => VScrollBar()
  ..swt = 'ScrollBar'
  ..id = id
  ..style = SWT.NONE
  ..visible = true
  ..enabled = true
  ..minimum = 0
  ..maximum = 1000
  ..thumb = 50
  ..selection = 0;

VCanvas _canvas(int w, int h) => VCanvas()
  ..swt = 'Canvas'
  ..id = 7
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, w, h)
  ..verticalBar = _bar(11)
  ..horizontalBar = _bar(12);

Future<void> _pump(WidgetTester tester, double w, double h) => tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: w,
        height: h,
        child: CanvasSwt<VCanvas>(value: _canvas(w.round(), h.round())),
      ),
    ));

void main() {
  testWidgets('a scrollbar track shorter than the minimum thumb does not throw',
      (tester) async {
    await _pump(tester, 10, 10);
    await tester.pump();

    expect(tester.takeException(), isNull);
  });

  testWidgets('a zero-sized Canvas does not throw', (tester) async {
    await _pump(tester, 0, 0);
    await tester.pump();

    expect(tester.takeException(), isNull);
  });

  testWidgets('a normally sized Canvas still renders its scrollbars', (tester) async {
    await _pump(tester, 400, 300);
    await tester.pump();

    expect(tester.takeException(), isNull);
    expect(find.byType(CanvasSwt<VCanvas>), findsOneWidget);
  });
}
