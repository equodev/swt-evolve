// Every native gives a CCombo's field the focus on the press itself: a right-click focuses it, and so
// does a press that drags off before its release. The press is reported first, as for any control.

import 'package:flutter/gestures.dart' show kSecondaryButton;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

class _CapturingCComboSwt extends CComboSwt<VCCombo> {
  const _CapturingCComboSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VCCombo val, String ev, VEvent? payload) => onEvent(ev);
}

Future<List<String>> _pump(WidgetTester tester) async {
  VRegistry.instance.clear();
  final focus = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: 200,
        height: 24,
        child: _CapturingCComboSwt(
          value: VCCombo()
            ..swt = 'CCombo'
            ..id = 21
            ..style = 0
            ..enabled = true
            ..items = const []
            ..text = ''
            ..bounds = (VRectangle()
              ..x = 0
              ..y = 0
              ..width = 200
              ..height = 24),
          onEvent: (ev) {
            if (ev.startsWith('Focus/') || ev == 'Mouse/MouseDown') focus.add(ev);
          },
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return focus;
}

Offset _field(WidgetTester tester) => tester.getTopLeft(find.byType(_CapturingCComboSwt)) + const Offset(60, 12);

void main() {
  testWidgets('a right-click on the field focuses it', (tester) async {
    final focus = await _pump(tester);

    final gesture = await tester.startGesture(_field(tester), buttons: kSecondaryButton);
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();

    expect(focus, ['Mouse/MouseDown', 'Focus/FocusIn']);
  });

  testWidgets('a press on the field that drags off before the release focuses it', (tester) async {
    final focus = await _pump(tester);

    final gesture = await tester.startGesture(_field(tester));
    await tester.pump();
    expect(focus, ['Mouse/MouseDown', 'Focus/FocusIn'], reason: 'on the press, before any move');
    await gesture.moveBy(const Offset(120, 0));
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();

    expect(focus, ['Mouse/MouseDown', 'Focus/FocusIn']);
  });

  testWidgets('a press on the focused field keeps the focus: no FocusOut and FocusIn around it',
      (tester) async {
    final focus = await _pump(tester);
    await tester.tapAt(_field(tester));
    await tester.pumpAndSettle();
    focus.clear();

    for (var i = 0; i < 2; i++) {
      await tester.tapAt(_field(tester));
      await tester.pump(const Duration(milliseconds: 50));
    }
    await tester.pumpAndSettle();

    expect(focus.where((ev) => ev.startsWith('Focus/')), isEmpty);
  });
}

