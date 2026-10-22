// A CCombo reports its own pointer events as SWT's does, forwarding its field's: one MouseDoubleClick
// per double click, between the second MouseDown and MouseUp, and one MouseEnter and MouseExit per
// crossing, with the pointer position. The field keeps the focus through every press, as SWT's
// CCombo keeps it in its Text.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

class _RecordingCComboSwt extends CComboSwt<VCCombo> {
  const _RecordingCComboSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendEvent(VCCombo val, String ev, VEvent? payload) {
    if (ev.startsWith('Mouse/') || ev.startsWith('MouseTrack/') || ev.startsWith('Focus/')) {
      calls.add('$ev:${payload?.count}@${payload?.x},${payload?.y}');
    }
  }
}

VCCombo _combo() => VCCombo()
  ..swt = 'CCombo'
  ..id = 1
  ..style = SWT.BORDER
  ..enabled = true
  ..visible = true
  ..text = 'alpha bravo'
  ..items = ['alpha bravo', 'charlie']
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 30);

Widget _app(List<String> calls) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 200, height: 30, child: _RecordingCComboSwt(value: _combo(), calls: calls)),
      ),
    );

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a double click on the field reports one MouseDoubleClick, after the second MouseDown',
      (tester) async {
    setConfigFlags(ConfigFlags()..double_click_timeout_ms = 60000);
    final calls = <String>[];
    await tester.pumpWidget(_app(calls));
    await tester.pump();

    const at = Offset(40, 15);
    await tester.tapAt(at);
    await tester.pump(const Duration(milliseconds: 50));
    await tester.tapAt(at);
    await tester.pump(const Duration(milliseconds: 500));

    expect(
      calls.where((c) => c.startsWith('Mouse/')),
      equals([
        'Mouse/MouseDown:1@40,15',
        'Mouse/MouseUp:1@40,15',
        'Mouse/MouseDown:2@40,15',
        'Mouse/MouseDoubleClick:2@40,15',
        'Mouse/MouseUp:2@40,15',
      ]),
    );
  });

  testWidgets('a crossing reports one MouseEnter and one MouseExit, with the pointer position',
      (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(_app(calls));
    await tester.pump();

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(300, 100));
    await tester.pump();
    await mouse.moveTo(const Offset(40, 15));
    await tester.pump();
    await mouse.moveTo(const Offset(300, 100));
    await tester.pump();
    await mouse.removePointer();

    expect(calls.where((c) => c.startsWith('MouseTrack/')),
        ['MouseTrack/MouseEnter:null@40,15', 'MouseTrack/MouseExit:null@300,100']);
  });

  testWidgets('presses on a focused field neither take its focus nor give it back', (tester) async {
    setConfigFlags(ConfigFlags()..double_click_timeout_ms = 60000);
    final calls = <String>[];
    await tester.pumpWidget(_app(calls));
    await tester.pump();

    const at = Offset(40, 15);
    await tester.tapAt(at);
    await tester.pump(const Duration(milliseconds: 500));
    expect(calls.where((c) => c.startsWith('Focus/')), ['Focus/FocusIn:null@null,null']);

    calls.clear();
    await tester.tapAt(at);
    await tester.pump(const Duration(milliseconds: 50));
    await tester.tapAt(at);
    await tester.pump(const Duration(milliseconds: 500));

    expect(calls.where((c) => c.startsWith('Focus/')), isEmpty);
  });
}
