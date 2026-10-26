// A new text shorter than the selection Java last sent must not throw: the selection is clamped to
// the text, as SWT's Text.setSelection does.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';

const _id = 1;

VText _value(String text, int selectionStart, int selectionEnd) => VText()
  ..swt = 'Text'
  ..id = _id
  ..style = SWT.SINGLE
  ..enabled = true
  ..editable = true
  ..text = text
  ..selection = (VPoint()
    ..x = selectionStart
    ..y = selectionEnd)
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 30);

Future<void> _pump(WidgetTester tester, VText value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 30,
      child: TextSwt<VText>(value: value),
    ),
  ));
  await tester.pumpAndSettle();
}

/// A new text and selection arriving from Java by the route they really travel.
Future<void> _javaSets(
    WidgetTester tester, String text, int selectionStart, int selectionEnd) async {
  VRegistry.instance.apply('Text/$_id', <String, dynamic>{
    'id': _id,
    'swt': 'Text',
    '_s': 1,
    '_b': 0,
    '_d': <String>['text', 'selection'],
    'text': text,
    'selection': <String, dynamic>{'x': selectionStart, 'y': selectionEnd},
  });
  await tester.pumpAndSettle();
}

TextEditingController _controller(WidgetTester tester) =>
    tester.widget<EditableText>(find.byType(EditableText)).controller;

void main() {
  testWidgets('a selection past the end of the new text is clamped to it',
      (tester) async {
    await _pump(tester, _value('Hello World, a long value', 0, 25));

    await _javaSets(tester, 'Hi', 0, 25);

    expect(tester.takeException(), isNull);
    expect(_controller(tester).text, 'Hi');
    expect(_controller(tester).selection,
        const TextSelection(baseOffset: 0, extentOffset: 2));
  });

  testWidgets('a caret past the end of the new text is clamped to it',
      (tester) async {
    await _pump(tester, _value('Hello World', 9, 9));

    await _javaSets(tester, 'ab', 9, 9);

    expect(tester.takeException(), isNull);
    expect(_controller(tester).text, 'ab');
    expect(_controller(tester).selection, const TextSelection.collapsed(offset: 2));
  });
}
