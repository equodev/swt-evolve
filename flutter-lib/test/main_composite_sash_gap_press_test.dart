// The e4 workbench has no Sash widget: SashLayout draws nothing and handles the divider itself,
// from MouseDown/MouseMove on the sash container (a MainComposite here). It lays the part stacks
// out edge-to-edge around a sashWidth-wide band and, on MouseDown, engages the drag when the point
// is within 5px of that band (SashLayout.getSashRects inflates by 5 on each side).
//
// MainComposite paints each child inset by panelChildGap, so the band the user SEES is the real
// band plus one gap on each side -- while the guard that keeps a press on a child from reaching
// the parent reads the child's un-inset SWT bounds. The gap ring is therefore painted as parent
// background but hit-tested as child: a press there is dropped and SashLayout never hears it, so
// the resize simply does not start. Only the narrow real band works, which is why grabbing the
// divider engages only some of the time.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/main_composite.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingMainComposite extends MainComposite {
  const _CapturingMainComposite({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VComposite val, String ev, VEvent? payload) =>
      onEvent(ev, payload);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _part({required int id, required VRectangle bounds}) => VLabel()
  ..swt = 'Label'
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..text = 'part $id'
  ..bounds = bounds;

// The geometry SashLayout.tileSubNodes produces for two part stacks in a 400x300 container with
// its default sashWidth of 4: the stacks abut the band, they never overlap it.
const double _sashWidth = 4;
const double _panelChildGap = 5;
const double _bandLeft = 198;
const double _bandRight = _bandLeft + _sashWidth; // 202

VComposite _sashContainer() => VComposite()
  ..swt = 'MainComposite'
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    _part(id: 2, bounds: _rect(0, 0, _bandLeft.toInt(), 300)),
    _part(id: 3, bounds: _rect(_bandRight.toInt(), 0, 198, 300)),
  ];

Future<List<String>> _hoverAt(WidgetTester tester, Offset at) async {
  final events = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 300,
      child: _CapturingMainComposite(
        value: _sashContainer(),
        onEvent: (ev, payload) => events.add(ev),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  events.clear();
  final pointer = TestPointer(1, PointerDeviceKind.mouse);
  await tester.sendEventToBinding(pointer.hover(at));
  await tester.pumpAndSettle();
  return events;
}

Future<List<VEvent?>> _pressAt(WidgetTester tester, Offset at) async {
  final downs = <VEvent?>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 300,
      child: _CapturingMainComposite(
        value: _sashContainer(),
        onEvent: (ev, payload) {
          if (ev.contains('MouseDown')) downs.add(payload);
        },
      ),
    ),
  ));
  await tester.pumpAndSettle();
  downs.clear();
  await tester.tapAt(at);
  await tester.pumpAndSettle();
  return downs;
}

void main() {
  const y = 150.0;

  testWidgets('a press on the real sash band reaches the container', (tester) async {
    final downs = await _pressAt(tester, const Offset(_bandLeft + _sashWidth / 2, y));

    expect(downs, isNotEmpty,
        reason: 'the band is outside both part bounds, so the container is the control there');
  });

  testWidgets('a press in the painted gap beside the band reaches the container',
      (tester) async {
    // Inside the left part's SWT bounds, but in the ring MainComposite paints as its own
    // background -- and within the 5px SashLayout needs to engage the drag.
    const inGapRing = _bandLeft - _panelChildGap / 2; // 195.5

    final downs = await _pressAt(tester, const Offset(inGapRing, y));

    expect(downs, isNotEmpty,
        reason: 'nothing of the part is painted there, so the press is the container\'s');
    expect(downs.single?.x, inGapRing.round(),
        reason: 'SashLayout hit-tests this x against the band, so it must arrive unshifted');
  });

  testWidgets('a press on the painted part is still left to the part', (tester) async {
    final downs = await _pressAt(tester, const Offset(100, y));

    expect(downs, isEmpty,
        reason: 'the part is painted there; the container must not report the press as well');
  });

  // The painted rectangle is the press's rule alone. Hover reaching across the ring re-arms the
  // sash cursor after the MouseExit meant to clear it -- the last hover before the pointer crosses
  // into the part still sits within SashLayout's grab tolerance and races that exit -- and the
  // resize cursor then stays on over the part the pointer has moved onto.
  testWidgets('hover in the painted gap is still left to the part', (tester) async {
    const inGapRing = _bandLeft - _panelChildGap / 2;

    final events = await _hoverAt(tester, const Offset(inGapRing, y));

    expect(events.where((e) => e.contains('MouseMove')), isEmpty,
        reason: 'hover keeps the layout rectangle, so the exit that clears the cursor wins');
  });
}
