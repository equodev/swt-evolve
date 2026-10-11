// A CLabel paints its text without the '&' that marks a mnemonic, and "&&" as one '&': the width
// Java measures is that of the text drawn.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/clabel.dart';
import 'package:swtflutter/src/gen/swt.dart';

VCLabel _label(String text) => VCLabel()
  ..id = 12
  ..style = SWT.NONE
  ..enabled = true
  ..text = text;

Future<String> _paintedText(WidgetTester tester, String text) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: CLabelSwt<VCLabel>(value: _label(text)),
    ),
  ));
  await tester.pumpAndSettle();
  return tester.widget<Text>(find.byType(Text)).data!;
}

void main() {
  testWidgets('a single & marks the mnemonic and is not painted', (tester) async {
    expect(await _paintedText(tester, '&Save'), 'Save');
  });

  testWidgets('a doubled && paints one &', (tester) async {
    expect(await _paintedText(tester, 'Fish && &Chips'), 'Fish & Chips');
  });
}
