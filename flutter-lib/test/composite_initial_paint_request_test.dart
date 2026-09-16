// Java owns every repaint but the first, which it cannot schedule for want of a mount event. That
// first request used to be made only by a Canvas, but every Control carries a GC overlay, so a
// plain Composite that draws itself from a Paint listener asked for nothing and stayed blank until
// some unrelated damage (a mouse move, a resize) made Java paint it again.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'delivery/support/deliver.dart';

class _CapturingCompositeSwt extends CompositeSwt<VComposite> {
  const _CapturingCompositeSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VComposite val, String ev, VEvent? payload) => onEvent(ev);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VComposite _panel({VRectangle? bounds, List<VControl>? children}) => VComposite()
  ..swt = 'Composite'
  ..id = 4242
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = bounds ?? _rect(0, 0, 200, 100)
  ..children = children;

Future<void> _pump(WidgetTester tester, VComposite value, List<String> events) =>
    tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 100,
        child: _CapturingCompositeSwt(value: value, onEvent: events.add),
      ),
    ));

int _paints(List<String> events) => events.where((e) => e == 'Paint/Paint').length;

/// The GC overlay mounts a frame after its owner, and that transition is what releases the request.
Future<void> _settle(WidgetTester tester) async {
  await tester.pump();
  await tester.pump();
}

void main() {
  testWidgets('a Composite asks Java to paint when it first mounts', (tester) async {
    final events = <String>[];
    await _pump(tester, _panel(), events);
    await _settle(tester);
    expect(_paints(events), 1);
  });

  // Having children routes a Composite down a different build branch; the request must survive it.
  testWidgets('a Composite with children asks too', (tester) async {
    final events = <String>[];
    await _pump(
        tester,
        _panel(children: [
          VComposite()
            ..id = 4243
            ..style = SWT.NONE
            ..visible = false
            ..bounds = _rect(0, 0, 57, 22)
        ]),
        events);
    await _settle(tester);
    expect(_paints(events), 1);
  });

  testWidgets('and does not ask again on a later push', (tester) async {
    final events = <String>[];
    await _pump(tester, _panel(), events);
    await _settle(tester);
    final int afterMount = _paints(events);

    await deliverWhole(_panel()..seq = 10);
    await tester.pump();

    expect(_paints(events), afterMount);
  });

  // Java drops a Paint request while the control still reports 0x0 bounds, so the request made at
  // mount would be lost for good. The retry is the one later request this side is allowed.
  testWidgets('a Composite that mounted without bounds asks again once they arrive',
      (tester) async {
    final events = <String>[];
    await _pump(tester, _panel(bounds: _rect(0, 0, 0, 0)), events);
    await _settle(tester);

    await deliverWhole(_panel(bounds: _rect(0, 0, 200, 100))..seq = 10);
    await tester.pump();
    final int afterBounds = _paints(events);
    expect(afterBounds, greaterThan(1));

    await deliverWhole(_panel(bounds: _rect(0, 0, 400, 300))..seq = 20);
    await tester.pump();
    expect(_paints(events), afterBounds);
  });
}
