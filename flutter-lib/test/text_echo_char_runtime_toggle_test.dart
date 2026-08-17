import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/text_evolve.dart';

String _renderedText(WidgetTester tester) {
  final editable = find.byType(EditableText);
  expect(editable, findsOneWidget);
  final state = tester.state<EditableTextState>(editable);
  return state.renderEditable.text!.toPlainText();
}

Future<void> _pumpText(WidgetTester tester, VText value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: TextSwt<VText>(value: value),
    ),
  ));
  await tester.pumpAndSettle();
}

VText _value(int echoCharacter) => VText()
  ..id = 21
  ..style = SWT.SINGLE | SWT.BORDER
  ..enabled = true
  ..editable = true
  ..text = 'secret'
  ..echoCharacter = echoCharacter;

void main() {
  testWidgets('setting an echo char at runtime re-masks existing text',
      (WidgetTester tester) async {
    await _pumpText(tester, _value(0));
    expect(_renderedText(tester), 'secret',
        reason: 'no echo char yet: the text starts readable');

    final state = tester.state<TextImpl<TextSwt<VText>, VText>>(
        find.byType(TextSwt<VText>));
    state.setValue(_value('•'.codeUnitAt(0)));
    await tester.pumpAndSettle();

    expect(_renderedText(tester), '••••••',
        reason: 'a runtime setEchoChar must re-mask text already present');
  });

  testWidgets('clearing the echo char at runtime reveals existing text',
      (WidgetTester tester) async {
    await _pumpText(tester, _value('•'.codeUnitAt(0)));
    expect(_renderedText(tester), '••••••');

    final state = tester.state<TextImpl<TextSwt<VText>, VText>>(
        find.byType(TextSwt<VText>));
    state.setValue(_value(0));
    await tester.pumpAndSettle();

    expect(_renderedText(tester), 'secret',
        reason: 'clearing the echo char must reveal the real text');
  });
}
