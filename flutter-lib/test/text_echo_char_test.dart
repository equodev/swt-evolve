import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';

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

void main() {
  testWidgets('setEchoChar masks the text with the echo character',
      (WidgetTester tester) async {
    await _pumpText(
      tester,
      VText()
        ..id = 11
        ..style = SWT.SINGLE | SWT.BORDER
        ..enabled = true
        ..editable = true
        ..text = 'secret'
        ..echoCharacter = '*'.codeUnitAt(0),
    );

    expect(_renderedText(tester), '******',
        reason: 'an echo-char field must not show what was typed');
  });

  testWidgets('no echo character leaves the text readable',
      (WidgetTester tester) async {
    await _pumpText(
      tester,
      VText()
        ..id = 12
        ..style = SWT.SINGLE | SWT.BORDER
        ..enabled = true
        ..editable = true
        ..text = 'secret'
        ..echoCharacter = 0,
    );

    expect(_renderedText(tester), 'secret');
  });

  testWidgets('MULTI ignores the echo character', (WidgetTester tester) async {
    await _pumpText(
      tester,
      VText()
        ..id = 13
        ..style = SWT.MULTI | SWT.BORDER
        ..enabled = true
        ..editable = true
        ..text = 'secret'
        ..echoCharacter = '*'.codeUnitAt(0),
    );

    expect(_renderedText(tester), 'secret');
  });

  testWidgets('PASSWORD keeps masking without an echo character',
      (WidgetTester tester) async {
    await _pumpText(
      tester,
      VText()
        ..id = 14
        ..style = SWT.SINGLE | SWT.PASSWORD | SWT.BORDER
        ..enabled = true
        ..editable = true
        ..text = 'secret',
    );

    expect(_renderedText(tester), '••••••');
  });
}
