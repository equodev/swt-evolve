// A control that builds its own tree reports pointer events through trackPointer, as wrap() does
// for the rest: only while it is enabled, and only for the pointer over itself, not over a child.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/group.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

class _RecordingGroupSwt extends GroupSwt<VGroup> {
  const _RecordingGroupSwt({required super.value, required this.sent});

  final List<String> sent;

  @override
  void sendEvent(VGroup val, String ev, VEvent? payload) => sent.add(ev);
}

class _RecordingListSwt extends ListSwt<VList> {
  const _RecordingListSwt({required super.value, required this.sent});

  final List<String> sent;

  @override
  void sendEvent(VList val, String ev, VEvent? payload) => sent.add(ev);
}

Future<List<String>> _groupEvents(WidgetTester tester, Future<void> Function() gesture) async {
  final sent = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 300,
      height: 200,
      child: _RecordingGroupSwt(
        sent: sent,
        value: VGroup()
          ..id = 1
          ..style = SWT.NONE
          ..text = ''
          ..visible = true
          ..enabled = true
          ..bounds = _rect(0, 0, 300, 200)
          ..children = [
            VLabel()
              ..id = 2
              ..style = SWT.NONE
              ..text = 'inside'
              ..visible = true
              ..enabled = true
              ..bounds = _rect(10, 10, 160, 40),
          ],
      ),
    ),
  ));
  await tester.pumpAndSettle();
  sent.clear();
  await gesture();
  await tester.pumpAndSettle();
  return sent;
}

void main() {
  testWidgets('a press on a control inside a Group is not the Group\'s', (tester) async {
    final sent = await _groupEvents(tester, () async {
      final label = tester.getCenter(find.text('inside'));
      final g = await tester.startGesture(label, kind: PointerDeviceKind.mouse);
      await g.moveBy(const Offset(4, 0));
      await g.up();
    });

    expect(sent.where((e) => e.contains('MouseDown') || e.contains('MouseUp') || e.contains('MouseMove')),
        isEmpty,
        reason: 'SWT delivers the press, the drag and the release to the Label under the pointer');
  });

  testWidgets('a hover over a control inside a Group is not the Group\'s', (tester) async {
    final sent = await _groupEvents(tester, () async {
      final label = tester.getCenter(find.text('inside'));
      final pointer = TestPointer(1, PointerDeviceKind.mouse);
      await tester.sendEventToBinding(pointer.hover(label));
      await tester.sendEventToBinding(pointer.hover(label + const Offset(6, 0)));
    });

    expect(sent.where((e) => e.contains('MouseMove') || e.contains('MouseHover')), isEmpty);
  });

  testWidgets('a disabled List reports no press', (tester) async {
    final sent = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 120,
        child: _RecordingListSwt(
          sent: sent,
          value: VList()
            ..id = 3
            ..style = SWT.SINGLE | SWT.BORDER
            ..items = ['one', 'two']
            ..visible = true
            ..enabled = false
            ..bounds = _rect(0, 0, 200, 120),
        ),
      ),
    ));
    await tester.pumpAndSettle();
    sent.clear();

    final g = await tester.startGesture(const Offset(100, 60), kind: PointerDeviceKind.mouse);
    await g.up();
    await tester.pumpAndSettle();

    expect(sent.where((e) => e.contains('MouseDown') || e.contains('MouseUp')), isEmpty);
  });
}
