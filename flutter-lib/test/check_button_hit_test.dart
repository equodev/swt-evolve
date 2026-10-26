// A check box or radio is selected by a click anywhere over its box, the gap after it and its
// label, as on GTK, whose check button is exactly that wide.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _RecordingButtonSwt extends ButtonSwt<VButton> {
  final List<String> calls;
  const _RecordingButtonSwt({required VButton value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VButton val, String ev, VEvent? payload) => calls.add(ev);
}

Future<List<String>> _drag(WidgetTester tester, double width, double fromX, double toX) async {
  final calls = <String>[];
  final value = VButton()
    ..id = 1
    ..style = SWT.CHECK
    ..enabled = true
    ..text = 'OK'
    ..selection = false
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = width.toInt()
      ..height = 18);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
          width: width,
          height: 18,
          child: KeyedSubtree(key: UniqueKey(), child: _RecordingButtonSwt(value: value, calls: calls))),
    ),
  ));
  await tester.pump();
  final gesture = await tester.startGesture(Offset(fromX, 9));
  for (var x = fromX; x <= toX; x += 4) {
    await gesture.moveTo(Offset(x, 9));
    await tester.pump(const Duration(milliseconds: 16));
  }
  await gesture.moveTo(Offset(toX, 9));
  await gesture.up();
  await tester.pump(const Duration(seconds: 1));
  return calls.where((c) => c.startsWith('Selection/')).toList();
}

void main() {
  testWidgets('a drag across a check box released over it selects it', (tester) async {
    expect(await _drag(tester, 36, 4, 32), isNotEmpty);
  });

  testWidgets('a drag released past a check box label, inside its bounds, does not select it', (tester) async {
    expect(await _drag(tester, 160, 4, 150), isEmpty);
  });

  testWidgets('a click above a check box label, inside its row, selects it', (tester) async {
    final calls = <String>[];
    final value = VButton()
      ..id = 1
      ..style = SWT.CHECK
      ..enabled = true
      ..text = 'OK'
      ..selection = false
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 36
        ..height = 18);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
            width: 36,
            height: 18,
            child: KeyedSubtree(key: UniqueKey(), child: _RecordingButtonSwt(value: value, calls: calls))),
      ),
    ));
    await tester.pump();
    final label = tester.getRect(find.text('OK'));
    final row = tester.getRect(find.byType(IntrinsicHeight));
    expect(label.top - row.top, greaterThan(0.5), reason: 'the label must be shorter than the row for this to test anything');
    await tester.tapAt(Offset(label.center.dx, (row.top + label.top) / 2));
    await tester.pump(const Duration(seconds: 1));
    expect(calls.where((c) => c.startsWith('Selection/')), isNotEmpty);
  });

  for (final style in [SWT.CHECK, SWT.RADIO]) {
    testWidgets('a click across a ${style == SWT.CHECK ? 'check box' : 'radio'} selects it',
        (tester) async {
      const width = 36.0, height = 18.0;
      for (var x = 2.0; x < width; x += 4) {
        final calls = <String>[];
        final value = VButton()
          ..id = 1
          ..style = style
          ..enabled = true
          ..text = 'OK'
          ..selection = false
          ..bounds = (VRectangle()
            ..x = 0
            ..y = 0
            ..width = width.toInt()
            ..height = height.toInt());
        await tester.pumpWidget(EvolveApp(
          theme: ThemeMode.light,
          contentWidget: Align(
            alignment: Alignment.topLeft,
            child: SizedBox(
                width: width,
                height: height,
                child: KeyedSubtree(key: UniqueKey(), child: _RecordingButtonSwt(value: value, calls: calls))),
          ),
        ));
        await tester.pump();

        await tester.tapAt(Offset(x, height / 2));
        await tester.pump(const Duration(seconds: 1));

        expect(calls.where((c) => c.startsWith('Selection/')), isNotEmpty, reason: 'click at x=$x');
      }
    });
  }
}
