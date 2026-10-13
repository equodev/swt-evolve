// SWT draws a string in which '&' marks a mnemonic without that marker: every single '&' is dropped,
// the one at the end too, and "&&" draws one '&'. Java measures the text by the same rule
// (Mnemonics.display, whose test holds the same cases), so a widget is as wide as the text it draws.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/clabel.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/text_utils.dart';

const _drawn = {
  'Save': 'Save',
  '': '',
  '&Save': 'Save',
  'Sa&ve': 'Save',
  'Save&': 'Save',
  '&File &Edit': 'File Edit',
  'a && b': 'a & b',
  '&&Save': '&Save',
  '&&&': '&',
  '&&&&': '&&',
  '&&&Save': '&Save',
};

void main() {
  group('stripAccelerators', () {
    for (final MapEntry(key: raw, value: drawn) in _drawn.entries) {
      test('"$raw" is drawn as "$drawn"', () {
        expect(stripAccelerators(raw), drawn);
      });
    }

    test('no text draws nothing', () {
      expect(stripAccelerators(null), '');
    });
  });

  testWidgets('a CLabel draws its text without the mnemonic marker', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: CLabelSwt<VCLabel>(
          value: VCLabel()
            ..id = 1
            ..style = SWT.NONE
            ..enabled = true
            ..text = 'Sa&ve && Close',
        ),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Save & Close'), findsOneWidget);
    expect(find.text('Sa&ve && Close'), findsNothing);
  });
}
