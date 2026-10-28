// A Canvas's scroll bar is always shown, as a native SWT one is, and its thumb follows the
// selection Java sends. The registry updates the bar in place, so the bar widget's old and new
// configuration hold the same object.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VScrollBar _bar(int selection, int seq) => VScrollBar()
  ..seq = seq
  ..swt = 'ScrollBar'
  ..id = 11
  ..style = SWT.NONE
  ..visible = true
  ..enabled = true
  ..minimum = 0
  ..maximum = 1000
  ..thumb = 50
  ..selection = selection;

/// The Canvas as Java writes it at [seq], with its bar at [selection].
VCanvas _canvas(int selection, {int seq = 0}) => VCanvas()
  ..seq = seq
  ..swt = 'Canvas'
  ..id = 7
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 400, 300)
  ..verticalBar = _bar(selection, seq);

Future<void> _pump(WidgetTester tester) => tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: CanvasSwt<VCanvas>(value: _canvas(0)),
      ),
    ));

Finder get _barPaint => find.byWidgetPredicate(
    (w) => w is CustomPaint && w.painter.runtimeType.toString() == '_ScrollBarPainter');

/// The opacity the bar is drawn at: its thumb colour's alpha times every opacity above it.
double _barOpacity(WidgetTester tester) {
  final painter = tester.widget<CustomPaint>(_barPaint).painter as dynamic;
  var opacity = (painter.thumbColor as Color).a;
  for (final w in tester.widgetList(find.ancestor(of: _barPaint, matching: find.byWidgetPredicate(
      (w) => w is Opacity || w is AnimatedOpacity || w is FadeTransition)))) {
    opacity *= switch (w) {
      Opacity o => o.opacity,
      AnimatedOpacity o => o.opacity,
      FadeTransition f => f.opacity.value,
      _ => 1.0,
    };
  }
  return opacity;
}

double _thumbOffset(WidgetTester tester) =>
    (tester.widget<CustomPaint>(_barPaint).painter as dynamic).thumbOffset as double;

/// Where the thumb is drawn for [selection] on the 0..1000 bar with a 50 thumb.
double _thumbOffsetAt(WidgetTester tester, int selection) {
  final track = tester.getSize(_barPaint).height;
  final thumb = (tester.widget<CustomPaint>(_barPaint).painter as dynamic).thumbSize as double;
  return selection / 950 * (track - thumb);
}

/// Long past the delay after which an overlay bar would fade.
Future<void> _idle(WidgetTester tester) async {
  await tester.pump(const Duration(milliseconds: 900));
  await tester.pump(const Duration(seconds: 2));
}

void main() {
  tearDown(freshClient);

  testWidgets('the scroll bar is shown with no scroll and no hover', (tester) async {
    await _pump(tester);
    await _idle(tester);

    expect(_barPaint, paints..rrect());
    expect(_barOpacity(tester), 1.0);
  });

  testWidgets('the scroll bar stays shown after a scroll, the thumb where Java put it',
      (tester) async {
    await _pump(tester);

    await deliverWhole(_canvas(450, seq: 10));
    await tester.pump();
    await _idle(tester);

    expect(_barOpacity(tester), 1.0);
    expect(_thumbOffset(tester), closeTo(_thumbOffsetAt(tester, 450), 0.01));
  });

  // Java takes the selection the bar sends without sending it back, so the next selection it
  // sends is a later scroll the thumb has to follow.
  testWidgets('after a drag the thumb follows the next scroll', (tester) async {
    await _pump(tester);
    final top = tester.getTopLeft(_barPaint) + const Offset(4, 4);
    await tester.dragFrom(top, const Offset(0, 120));
    await tester.pump(const Duration(milliseconds: 100));
    expect(_thumbOffset(tester), greaterThan(_thumbOffsetAt(tester, 100)));

    await deliverWhole(_canvas(100, seq: 10));
    await tester.pump();

    expect(_thumbOffset(tester), closeTo(_thumbOffsetAt(tester, 100), 0.01));
  });

  testWidgets('after a drag the thumb follows a scroll back to the pre-drag selection',
      (tester) async {
    await _pump(tester);
    final top = tester.getTopLeft(_barPaint) + const Offset(4, 4);
    await tester.dragFrom(top, const Offset(0, 120));
    await tester.pump(const Duration(milliseconds: 100));
    expect(_thumbOffset(tester), greaterThan(_thumbOffsetAt(tester, 100)));

    await deliverWhole(_canvas(0, seq: 10));
    await tester.pump();

    expect(_thumbOffset(tester), closeTo(0, 0.01));
  });

  testWidgets('after a track click the thumb follows the next scroll', (tester) async {
    await _pump(tester);
    await tester.tapAt(tester.getBottomLeft(_barPaint) + const Offset(4, -10));
    await tester.pump();
    expect(_thumbOffset(tester), greaterThan(_thumbOffsetAt(tester, 100)));

    await deliverWhole(_canvas(100, seq: 10));
    await tester.pump();

    expect(_thumbOffset(tester), closeTo(_thumbOffsetAt(tester, 100), 0.01));
  });
}
