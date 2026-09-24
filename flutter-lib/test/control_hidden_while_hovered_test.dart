// A hidden control is dropped from the tree while its State lives on. Hiding it must end its hover,
// since its pointer region goes without an exit, and showing it again must repaint it, since its
// GC drawing went with the dropped subtree.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'delivery/support/deliver.dart';

const String _message = 'Show the filter field';
const Offset _hover = Offset(10, 10);

class _CapturingCompositeSwt extends CompositeSwt<VComposite> {
  const _CapturingCompositeSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VComposite val, String ev, VEvent? payload) => onEvent(ev);
}

VComposite _toolbar({required bool visible, int seq = 0}) => VComposite()
  ..swt = 'Composite'
  ..id = 4242
  ..seq = seq
  ..style = SWT.NONE
  ..enabled = true
  ..visible = visible
  ..toolTipText = _message
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 30
    ..height = 24);

Future<void> _pump(WidgetTester tester, List<String> events) => tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 30,
          height: 24,
          child: _CapturingCompositeSwt(value: _toolbar(visible: true), onEvent: events.add),
        ),
      ),
    ));

Future<TestGesture> _hoverToolbar(WidgetTester tester) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await gesture.addPointer(location: const Offset(200, 200));
  addTearDown(gesture.removePointer);
  await gesture.moveTo(_hover);
  await tester.pump();
  return gesture;
}

int _paints(List<String> events) => events.where((e) => e == 'Paint/Paint').length;

void main() {
  testWidgets('a control hidden before its hover delay elapses opens no tooltip', (tester) async {
    await _pump(tester, <String>[]);
    await _hoverToolbar(tester);

    await deliverWhole(_toolbar(visible: false, seq: 10));
    await tester.pump(const Duration(milliseconds: 700));
    await tester.pump();

    expect(find.text(_message), findsNothing);
  });

  testWidgets('a control hidden under its open tooltip takes the tooltip down', (tester) async {
    await _pump(tester, <String>[]);
    await _hoverToolbar(tester);
    await tester.pump(const Duration(milliseconds: 700));
    await tester.pump();
    expect(find.text(_message), findsOneWidget);

    await deliverWhole(_toolbar(visible: false, seq: 10));
    await tester.pump();

    expect(find.text(_message), findsNothing);
  });

  testWidgets('a control shown again asks Java to paint it again', (tester) async {
    final events = <String>[];
    await _pump(tester, events);
    await tester.pump();
    await tester.pump();
    final int afterMount = _paints(events);
    expect(afterMount, 1);

    await deliverWhole(_toolbar(visible: false, seq: 10));
    await tester.pump();
    await deliverWhole(_toolbar(visible: true, seq: 20));
    await tester.pump();
    await tester.pump();

    expect(_paints(events), afterMount + 1);
  });
}
