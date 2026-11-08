// A Canvas's scroll bar names what moved it, as every native's does: a drag is a Selection(DRAG) per
// move and a Selection(NONE) as the button comes up; a press on the track warps the thumb to the
// pointer and holds it, as GTK's does, so it reports the same; Shift turns that press into a page
// towards the pointer, which is what GTK and Win32 both do with it, and its release is a NONE, as GTK's is.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/swt.dart';

const _barId = 11;

VCanvas _canvas() => VCanvas()
  ..swt = 'Canvas'
  ..id = 7
  ..style = SWT.V_SCROLL
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 200)
  ..verticalBar = (VScrollBar()
    ..swt = 'ScrollBar'
    ..id = _barId
    ..style = SWT.VERTICAL
    ..visible = true
    ..enabled = true
    ..minimum = 0
    ..maximum = 100
    ..thumb = 10
    ..increment = 5
    ..pageIncrement = 20
    ..selection = 40);

/// (selection, detail) of every Selection the bar sent, in order.
List<(int, int)> _selections() => EquoCommService.commForTesting.sentFrames
    .where((frame) => EquoCommBase.decodeFrame(frame).$1 == 'ScrollBar/$_barId/Selection/Selection')
    .map((frame) => EquoCommBase.decodeEvent(frame))
    .map((e) => (e.index ?? -1, e.detail ?? -1))
    .toList();

Finder get _bar => find.byWidgetPredicate(
    (w) => w is CustomPaint && w.painter.runtimeType.toString() == '_ScrollBarPainter');

Future<void> _pump(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 200, height: 200, child: CanvasSwt<VCanvas>(value: _canvas())),
    ),
  ));
  await tester.pump();
}

/// A point on the track [fraction] of the way down.
Offset _onTrack(WidgetTester tester, double fraction) {
  final rect = tester.getRect(_bar);
  return Offset(rect.center.dx, rect.top + rect.height * fraction);
}

void main() {
  setUp(() {
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });
  tearDown(() => EquoCommBase.recordSentFrames = false);

  testWidgets('a drag of the thumb is a DRAG per move, then NONE as the button comes up', (tester) async {
    await _pump(tester);
    final gesture = await tester.startGesture(_onTrack(tester, 0.45), kind: PointerDeviceKind.mouse);
    for (var i = 0; i < 4; i++) {
      await gesture.moveBy(const Offset(0, 15));
      await tester.pump(const Duration(milliseconds: 60));
    }
    await gesture.up();
    await tester.pump(const Duration(milliseconds: 100));

    final sent = _selections();
    expect(sent, isNotEmpty);
    expect(sent.sublist(0, sent.length - 1).map((s) => s.$2), everyElement(SWT.DRAG));
    expect(sent.last.$2, SWT.NONE);
    expect(sent.last.$1, greaterThan(40));
  });

  testWidgets('a press on the track warps the thumb to it and reports DRAG, then NONE on release',
      (tester) async {
    await _pump(tester);
    final gesture = await tester.startGesture(_onTrack(tester, 0.9), kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 100));
    expect(_selections().map((s) => s.$2), [SWT.DRAG]);
    final warped = _selections().single.$1;
    expect(warped, greaterThan(60));

    await gesture.up();
    await tester.pump(const Duration(milliseconds: 100));
    expect(_selections(), [(warped, SWT.DRAG), (warped, SWT.NONE)]);
  });

  testWidgets('a Shift press on the track pages towards the pointer', (tester) async {
    await _pump(tester);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.tapAt(_onTrack(tester, 0.9), kind: PointerDeviceKind.mouse);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pump(const Duration(milliseconds: 100));
    expect(_selections(), [(60, SWT.PAGE_DOWN), (60, SWT.NONE)]);

    EquoCommService.commForTesting.clearSentFrames();
    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.tapAt(_onTrack(tester, 0.05), kind: PointerDeviceKind.mouse);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pump(const Duration(milliseconds: 100));
    expect(_selections(), [(40, SWT.PAGE_UP), (40, SWT.NONE)]);
  });
}
