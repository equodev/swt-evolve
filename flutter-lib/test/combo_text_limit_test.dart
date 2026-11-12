// A Combo with a text limit takes no more than that many characters, typed or pasted, as the native
// field does. Java keeps only that many, so a field that showed more would no longer match getText().

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _CapturingComboSwt extends ComboSwt<VCombo> {
  const _CapturingComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCombo val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

Future<List<String?>> _typeInto(WidgetTester tester, {required int style, int? textLimit}) async {
  final modifies = <String?>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: style & SWT.SIMPLE != 0 ? 120 : 30,
      child: _CapturingComboSwt(
        value: VCombo()
          ..swt = 'Combo'
          ..id = 3
          ..style = style
          ..enabled = true
          ..items = const ['one']
          ..text = 'ab'
          ..textLimit = textLimit
          ..bounds = (VRectangle()
            ..x = 0
            ..y = 0
            ..width = 200
            ..height = style & SWT.SIMPLE != 0 ? 120 : 30),
        onEvent: (ev, payload) {
          if (ev == 'Modify/Modify') modifies.add(payload?.text);
        },
      ),
    ),
  ));
  await tester.enterText(find.byType(EditableText), 'abcdef');
  await tester.pump();
  return modifies;
}

void main() {
  for (final (name, style) in [('drop-down', SWT.DROP_DOWN), ('simple', SWT.SIMPLE)]) {
    testWidgets('a $name Combo keeps its text within the limit', (tester) async {
      final modifies = await _typeInto(tester, style: style, textLimit: 4);

      expect(find.text('abcd'), findsOneWidget);
      expect(modifies, ['abcd']);
    });
  }

  testWidgets('Combo.LIMIT is no limit', (tester) async {
    final modifies = await _typeInto(tester, style: SWT.DROP_DOWN, textLimit: 0x7FFFFFFF);

    expect(modifies, ['abcdef']);
  });
}
