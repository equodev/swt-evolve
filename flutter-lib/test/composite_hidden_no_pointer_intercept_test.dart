// A hidden (visible=false) childless Composite must not intercept pointers. Its interaction chrome
// Listener sat outside wrap()'s Visibility gate, so a hidden CTabFolder page stacked over the shown
// one swallowed every click meant for the live page (dead terminal pane / no tab switch). A visible
// Composite still forwards MouseDown; a hidden one forwards nothing.

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

VComposite _childlessComposite({required bool visible}) => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = visible
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

Future<List<String>> _tapAndCapture(WidgetTester tester, {required bool visible}) async {
  final events = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    // Tight constraints reproduce the real layout: NoLayout force-sizes each child to its SWT
    // bounds, so a hidden composite's chrome fills that area and can intercept -- a standalone
    // Visibility(visible:false) would otherwise collapse to zero size and never be hit.
    contentWidget: SizedBox(
      width: 400,
      height: 200,
      child: _CapturingCompositeSwt(
        value: _childlessComposite(visible: visible),
        onEvent: events.add,
      ),
    ),
  ));
  await tester.pumpAndSettle();
  await tester.tapAt(const Offset(200, 100));
  await tester.pumpAndSettle();
  return events;
}

void main() {
  testWidgets('a visible childless Composite forwards MouseDown on tap', (tester) async {
    final events = await _tapAndCapture(tester, visible: true);
    expect(events, contains('Mouse/MouseDown'));
  });

  testWidgets('a hidden childless Composite does not intercept the pointer (no MouseDown)',
      (tester) async {
    final events = await _tapAndCapture(tester, visible: false);
    expect(events, isNot(contains('Mouse/MouseDown')));
  });
}
