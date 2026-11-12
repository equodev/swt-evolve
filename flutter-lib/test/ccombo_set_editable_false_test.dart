// A CCombo made uneditable with setEditable(false) takes no typing, as one created READ_ONLY: its
// field opens no text input on a press.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

class _QuietCComboSwt extends CComboSwt<VCCombo> {
  const _QuietCComboSwt({required super.value});

  @override
  void sendEvent(VCCombo val, int channel, VEvent? payload) {}
}

Future<void> _pressField(WidgetTester tester, {required bool? editable}) async {
  VRegistry.instance.clear();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: 200,
        height: 24,
        child: _QuietCComboSwt(
          value: VCCombo()
            ..swt = 'CCombo'
            ..id = 21
            ..style = 0
            ..enabled = true
            ..editable = editable
            ..items = const ['one', 'two']
            ..text = 'one'
            ..bounds = (VRectangle()
              ..x = 0
              ..y = 0
              ..width = 200
              ..height = 24),
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  await tester.tapAt(tester.getTopLeft(find.byType(_QuietCComboSwt)) + const Offset(60, 12));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('an editable field opens a text input on a press', (tester) async {
    await _pressField(tester, editable: true);

    expect(tester.testTextInput.hasAnyClients, isTrue);
  });

  testWidgets('a field set uneditable opens none', (tester) async {
    await _pressField(tester, editable: false);

    expect(tester.testTextInput.hasAnyClients, isFalse);
  });
}
