// A bordered CCombo's value and arrow must sit centered in the control. The border is painted
// around the DropdownMenu, so the field's height has to leave room for it on both edges.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _QuietCComboSwt extends CComboSwt<VCCombo> {
  const _QuietCComboSwt({super.key, required super.value});

  @override
  void sendEvent(VCCombo val, String ev, VEvent? payload) {}
}

VCCombo _borderedReadOnlyCombo(int height) => VCCombo()
  ..swt = 'CCombo'
  ..id = 3
  ..style = SWT.BORDER | SWT.READ_ONLY
  ..enabled = true
  ..items = const ['TWT', 'TVD', 'TVDSS', 'FREQ']
  ..text = 'TVDSS'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 180
    ..height = height);

void main() {
  for (final height in [22, 30]) {
    testWidgets('value and arrow are vertically centered in a $height px bordered combo',
        (tester) async {
      final key = GlobalKey();
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.dark,
        contentWidget: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(
            width: 180,
            height: height.toDouble(),
            child: _QuietCComboSwt(key: key, value: _borderedReadOnlyCombo(height)),
          ),
        ),
      ));
      await tester.pumpAndSettle();

      final combo = tester.getRect(find.byKey(key));
      final text = tester.getRect(find.byType(EditableText));
      final arrow = tester.getRect(find.byIcon(Icons.arrow_drop_down).first);

      expect(text.center.dy, moreOrLessEquals(combo.center.dy, epsilon: 0.5),
          reason: 'the value sits ${text.center.dy - combo.center.dy}px below the center');
      expect(arrow.center.dy, moreOrLessEquals(combo.center.dy, epsilon: 0.5),
          reason: 'the arrow sits ${arrow.center.dy - combo.center.dy}px below the center');
      expect(arrow.bottom, lessThanOrEqualTo(combo.bottom),
          reason: 'the arrow extends past the control');
    });
  }
}
