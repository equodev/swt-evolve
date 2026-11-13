// A disabled control takes no pointer: on every native the press, release and move over it go to
// its parent, as if the parent's own surface were there.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _CapturingCompositeSwt extends CompositeSwt<VComposite> {
  const _CapturingCompositeSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VComposite val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!.split('/').last);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VComposite _parentWithChild({required bool childEnabled}) => VComposite()
  ..swt = 'Composite'
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 400, 200)
  ..children = [
    VCanvas()
      ..swt = 'Canvas'
      ..id = 2
      ..style = SWT.NONE
      ..enabled = childEnabled
      ..visible = true
      ..bounds = _rect(200, 0, 200, 200),
  ];

Future<List<String>> _parentEventsOverChild(WidgetTester tester, {required bool childEnabled}) async {
  final events = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 200,
      child: _CapturingCompositeSwt(value: _parentWithChild(childEnabled: childEnabled), onEvent: events.add),
    ),
  ));
  await tester.pumpAndSettle();

  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.addPointer(location: const Offset(100, 100));
  await tester.pump();
  events.clear();
  await mouse.moveTo(const Offset(300, 100));
  await tester.pump(const Duration(milliseconds: 100));
  await mouse.down(const Offset(300, 100));
  await tester.pump();
  await mouse.up();
  await tester.pumpAndSettle();
  await mouse.removePointer();
  return events;
}

void main() {
  testWidgets('the pointer over a disabled child is the parent\'s', (tester) async {
    final events = await _parentEventsOverChild(tester, childEnabled: false);

    expect(events, containsAll(['MouseMove', 'MouseDown', 'MouseUp']));
  });

  testWidgets('the pointer over an enabled child is the child\'s', (tester) async {
    final events = await _parentEventsOverChild(tester, childEnabled: true);

    expect(events.where(['MouseMove', 'MouseDown', 'MouseUp'].contains), isEmpty);
  });
}
