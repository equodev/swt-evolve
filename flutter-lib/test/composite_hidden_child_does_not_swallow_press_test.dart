// A Composite refuses to forward a press that landed on one of its children, so the child alone
// reports it. That guard reads the children's bounds, and a hidden control keeps the bounds it
// last had while rendering SizedBox.shrink() -- it paints nothing and absorbs no pointer. So the
// guard claimed a child was under a pointer Flutter had already routed to the parent, and the
// press was dropped between the two: an accordion whose collapsed sections stay laid out over the
// panel went dead everywhere those stale rectangles reached.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

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

VComposite _child({required int id, required bool visible, required VRectangle bounds}) =>
    VComposite()
      ..swt = 'Composite'
      ..id = id
      ..style = SWT.NONE
      ..enabled = true
      ..visible = visible
      ..bounds = bounds;

/// The shape an accordion panel takes: one section expanded over most of the panel, the collapsed
/// ones still carrying the bounds they had while open, and a strip of header at the bottom that the
/// parent paints itself and hit-tests from its own MouseDown.
VComposite _panel({required bool collapsedSectionsVisible}) => VComposite()
  ..swt = 'Composite'
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 246, 400)
  ..children = [
    _child(id: 2, visible: true, bounds: _rect(0, 27, 246, 300)),
    _child(id: 3, visible: collapsedSectionsVisible, bounds: _rect(0, 81, 246, 300)),
    _child(id: 4, visible: collapsedSectionsVisible, bounds: _rect(0, 108, 246, 292)),
  ];

Future<List<String>> _pressAt(WidgetTester tester, VComposite panel, Offset at) async {
  final events = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 246,
      height: 400,
      child: _CapturingCompositeSwt(value: panel, onEvent: events.add),
    ),
  ));
  await tester.pumpAndSettle();
  events.clear();
  await tester.tapAt(at);
  await tester.pumpAndSettle();
  return events;
}

void main() {
  // y=370 is the collapsed header strip: below the expanded section (27..327) but inside the
  // rectangles the two hidden sections still declare (81..381 and 108..400).
  const headerStrip = Offset(30, 370);

  testWidgets('a press over a hidden child\'s stale bounds still reaches the composite',
      (tester) async {
    final events =
        await _pressAt(tester, _panel(collapsedSectionsVisible: false), headerStrip);

    expect(events.where((e) => e.contains('MouseDown')), isNotEmpty,
        reason: 'nothing is rendered there, so this composite is the control under the pointer');
  });

  testWidgets('a press over a shown child is still left to that child', (tester) async {
    final events =
        await _pressAt(tester, _panel(collapsedSectionsVisible: true), headerStrip);

    expect(events.where((e) => e.contains('MouseDown')), isEmpty,
        reason: 'a visible child takes the press; the parent must not report it as well');
  });
}
