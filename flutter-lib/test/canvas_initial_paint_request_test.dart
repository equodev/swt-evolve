// Java owns every repaint but the first, which it cannot schedule for want of a mount event. So the
// first is the only one this side asks for, and no later rebuild asks again.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/cursor.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingCanvasSwt extends CanvasSwt<VCanvas> {
  const _CapturingCanvasSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VCanvas val, String ev, VEvent? payload) => onEvent(ev);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VCanvas _canvas({VCursor? cursor, VColor? background, VRectangle? bounds}) => VCanvas()
  ..swt = 'Canvas'
  ..id = 7
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..cursor = cursor
  ..background = background
  ..bounds = bounds ?? _rect(0, 0, 200, 100);

VCursor _cursor(int style) => VCursor()..cursorStyle = style;

VColor _color(int r, int g, int b) => VColor()
  ..alpha = 0xFF
  ..red = r
  ..green = g
  ..blue = b;

Future<void> _pumpCanvas(WidgetTester tester, VCanvas value, List<String> events) =>
    tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 100,
        child: _CapturingCanvasSwt(value: value, onEvent: events.add),
      ),
    ));

int _paints(List<String> events) => events.where((e) => e == 'Paint/Paint').length;

/// The GC overlay mounts a frame after the Canvas, and that transition legitimately asks for one
/// more paint. Settle past it so a count taken here reflects steady state.
Future<void> _settle(WidgetTester tester) async {
  await tester.pump();
  await tester.pump();
}

void main() {
  testWidgets('a Canvas asks Java to paint when it first mounts', (tester) async {
    final events = <String>[];
    await _pumpCanvas(tester, _canvas(), events);
    expect(_paints(events), 1);
  });

  testWidgets('a push that changes only the cursor does not ask Java to paint again',
      (tester) async {
    final events = <String>[];
    await _pumpCanvas(tester, _canvas(cursor: _cursor(SWT.CURSOR_ARROW)), events);
    await _settle(tester);
    final int afterMount = _paints(events);

    await _pumpCanvas(tester, _canvas(cursor: _cursor(SWT.CURSOR_SIZEE)), events);
    await tester.pump();

    expect(_paints(events), afterMount);
  });

  testWidgets('a rebuild carrying an unchanged state does not ask Java to paint again',
      (tester) async {
    final events = <String>[];
    await _pumpCanvas(tester, _canvas(), events);
    await _settle(tester);
    final int afterMount = _paints(events);

    await _pumpCanvas(tester, _canvas(), events);
    await tester.pump();

    expect(_paints(events), afterMount);
  });

  testWidgets('a push that changes the background does not ask Java to paint', (tester) async {
    final events = <String>[];
    await _pumpCanvas(tester, _canvas(background: _color(0x10, 0x20, 0x30)), events);
    await _settle(tester);
    final int afterMount = _paints(events);

    await _pumpCanvas(tester, _canvas(background: _color(0xA0, 0xB0, 0xC0)), events);
    await tester.pump();

    expect(_paints(events), afterMount);
  });

  testWidgets('a resize does not ask Java to paint', (tester) async {
    final events = <String>[];
    await _pumpCanvas(tester, _canvas(), events);
    await _settle(tester);
    final int afterMount = _paints(events);

    await _pumpCanvas(tester, _canvas(bounds: _rect(0, 0, 400, 300)), events);
    await tester.pump();

    expect(_paints(events), afterMount);
  });

  // Java drops a Paint request while the control still reports 0x0 bounds, so the request made at
  // mount would be lost for good. The retry is the one later request this side is allowed.
  testWidgets('a Canvas that mounted without bounds asks again once they arrive', (tester) async {
    final events = <String>[];
    await _pumpCanvas(tester, _canvas(bounds: _rect(0, 0, 0, 0)), events);
    await _settle(tester);

    await _pumpCanvas(tester, _canvas(bounds: _rect(0, 0, 200, 100)), events);
    await tester.pump();
    final int afterBounds = _paints(events);
    expect(afterBounds, greaterThan(1));

    await _pumpCanvas(tester, _canvas(bounds: _rect(0, 0, 400, 300)), events);
    await tester.pump();
    expect(_paints(events), afterBounds);
  });
}
