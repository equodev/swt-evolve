// The second press of a double click delivers MouseDoubleClick right after its MouseDown, at the
// same position, and before its MouseUp -- the order Win32, GTK and Cocoa all report. A press a
// few pixels away from the first still pairs with it; one further off starts a new click. A pointer
// held without moving reports no MouseMove.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _RecordingButtonSwt extends ButtonSwt<VButton> {
  final List<String> calls;
  const _RecordingButtonSwt({required VButton value, required this.calls})
      : super(value: value);

  @override
  void sendEvent(VButton val, int channel, VEvent? payload) {
    if (wireChannelNames[channel]!.startsWith('Mouse/') || wireChannelNames[channel]!.startsWith('MouseMove/')) calls.add('${wireChannelNames[channel]!}:${payload?.count}@${payload?.x},${payload?.y}');
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

  Future<Offset> pumpButton(WidgetTester tester, List<String> calls) async {
    setConfigFlags(ConfigFlags()..double_click_timeout_ms = 60000);
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
    return tester.getTopLeft(find.byType(_RecordingButtonSwt));
  }

  testWidgets('a second mouse press 30 pixels away starts a new click, as on Win32 and GTK',
      (tester) async {
    final calls = <String>[];
    final origin = await pumpButton(tester, calls);

    await tester.tapAt(origin + const Offset(20, 20), kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 50));
    await tester.tapAt(origin + const Offset(50, 20), kind: PointerDeviceKind.mouse);
    await tester.pump();

    expect(calls.where((c) => c.startsWith('Mouse/')), [
      'Mouse/MouseDown:1@20,20',
      'Mouse/MouseUp:1@20,20',
      'Mouse/MouseDown:1@50,20',
      'Mouse/MouseUp:1@50,20',
    ]);
  });

  testWidgets('a second mouse press a few pixels away is still a double click', (tester) async {
    final calls = <String>[];
    final origin = await pumpButton(tester, calls);

    await tester.tapAt(origin + const Offset(80, 20), kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 50));
    await tester.tapAt(origin + const Offset(83, 21), kind: PointerDeviceKind.mouse);
    await tester.pump();

    expect(calls, contains('Mouse/MouseDoubleClick:2@83,21'));
  });

  testWidgets('two taps of a finger 30 pixels apart are still a double tap', (tester) async {
    final calls = <String>[];
    final origin = await pumpButton(tester, calls);

    await tester.tapAt(origin + const Offset(20, 20));
    await tester.pump(const Duration(milliseconds: 50));
    await tester.tapAt(origin + const Offset(50, 20));
    await tester.pump();

    expect(calls, contains('Mouse/MouseDoubleClick:2@50,20'));
  });
}
