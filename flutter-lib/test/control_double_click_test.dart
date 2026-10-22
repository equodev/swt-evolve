// The second press of a double click delivers MouseDoubleClick right after its MouseDown, at the
// same position, and before its MouseUp -- the order Win32, GTK and Cocoa all report. A pointer
// held without moving reports no MouseMove.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

class _RecordingButtonSwt extends ButtonSwt<VButton> {
  final List<String> calls;
  const _RecordingButtonSwt({required VButton value, required this.calls})
      : super(value: value);

  @override
  void sendEvent(VButton val, String ev, VEvent? payload) {
    if (ev.startsWith('Mouse/') || ev.startsWith('MouseMove/')) calls.add('$ev:${payload?.count}@${payload?.x},${payload?.y}');
  }
}

VButton _button() => VButton()
  ..id = 1
  ..style = SWT.PUSH
  ..enabled = true
  ..text = 'OK'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 40);

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a double click reports MouseDoubleClick between the second MouseDown and MouseUp',
      (tester) async {
    setConfigFlags(ConfigFlags()..double_click_timeout_ms = 60000);
    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 160,
          height: 40,
          child: _RecordingButtonSwt(value: _button(), calls: calls),
        ),
      ),
    ));
    await tester.pump();

    final at = tester.getTopLeft(find.byType(_RecordingButtonSwt)) + const Offset(80, 20);
    await tester.tapAt(at);
    await tester.pump(const Duration(milliseconds: 50));
    await tester.tapAt(at);
    await tester.pump();

    expect(
      calls.where((c) => c.startsWith('Mouse/')),
      equals([
        'Mouse/MouseDown:1@80,20',
        'Mouse/MouseUp:1@80,20',
        'Mouse/MouseDown:2@80,20',
        'Mouse/MouseDoubleClick:2@80,20',
        'Mouse/MouseUp:2@80,20',
      ]),
    );
  });

  testWidgets('a held pointer that does not move reports no MouseMove', (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 160,
          height: 40,
          child: _RecordingButtonSwt(value: _button(), calls: calls),
        ),
      ),
    ));
    await tester.pump();

    final at = tester.getTopLeft(find.byType(_RecordingButtonSwt)) + const Offset(80, 20);
    final gesture = await tester.startGesture(at);
    await gesture.moveBy(Offset.zero);
    await tester.pump(const Duration(milliseconds: 100));
    await gesture.moveBy(const Offset(5, 0));
    await tester.pump(const Duration(milliseconds: 100));
    await gesture.up();
    await tester.pump();

    expect(calls.where((c) => c.startsWith('MouseMove/')), ['MouseMove/MouseMove:null@85,20']);
  });
}
