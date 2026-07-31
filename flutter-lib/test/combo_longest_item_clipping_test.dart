// Issue #905 — the combo's arrow cuts off the text of the longest item.
//
// A DROP_DOWN combo with no bounds sizes itself via _calculatePreferredSize.
// That width must leave the EditableText a viewport at least as wide as the
// longest item, after the text-field padding, the arrow cell, and the border
// are consumed by the Row. If the accounting under-counts the arrow footprint
// (or the border), the longest item is clipped on the right by the arrow.
//
// ComboSizes.java's HORIZONTAL_PADDING is measured from this same natural
// width, so a deficit here reproduces identically when Java fixes the bounds
// (this case).

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/swt.dart';

void main() {
  testWidgets('natural-size DROP_DOWN combo leaves room for its longest item',
      (WidgetTester tester) async {
    const longest = 'MOV (only for Mac)';
    final combo = VCombo()
      ..id = 7
      ..style = SWT.DROP_DOWN | SWT.READ_ONLY
      ..enabled = true
      ..text = longest
      ..items = ['AVI', 'MP4', longest];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: ComboSwt<VCombo>(value: combo),
      ),
    ));
    await tester.pumpAndSettle();

    final editable = find.byType(EditableText);
    expect(editable, findsOneWidget);

    final double viewportWidth = tester.getSize(editable).width;
    final TextStyle style = tester.widget<EditableText>(editable).style;
    final painter = TextPainter(
      text: TextSpan(text: longest, style: style),
      maxLines: 1,
      textDirection: TextDirection.ltr,
    )..layout();

    expect(viewportWidth, greaterThanOrEqualTo(painter.width),
        reason: 'The text viewport (${viewportWidth.toStringAsFixed(2)}px) is '
            'narrower than the longest item (${painter.width.toStringAsFixed(2)}px): '
            'the arrow cuts off the end of the text (issue #905).');
  });
}
