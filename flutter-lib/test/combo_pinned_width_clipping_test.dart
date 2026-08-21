// A DROP_DOWN combo whose bounds are narrower than its preferred width clips
// the selected text on the right.
//
// An application is free to pin a control's width through GridData.widthHint,
// and real ones do: a preference page hints 150px for its READ_ONLY combos --
// enough for a native combo, well under the ~157px this one prefers. The
// text-field padding and the arrow cell are fixed insets, so the Row leaves
// EditableText a 96px viewport for ~103px of text and the last glyph is cut
// off. What makes it read as a rendering fault rather than a tight fit is the
// slack it leaves: the right-hand padding and the arrow glyph's own inset sit
// between the truncated text and the visible arrow.
//
// This is the horizontal twin of combo_short_height_clipping_test.dart, and the
// rule is the same: the chrome must yield to the text, not the other way round.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

// The test fallback font gives every glyph a full em box, which would make the
// deficit an artefact of the harness rather than the one the reporter saw. Load
// the real face the theme asks for so the widths are the app's own.
Future<void> _loadInter() async {
  final loader = FontLoader('Inter')
    ..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
  await loader.load();
}

void main() {
  setUpAll(_loadInter);

  testWidgets('DROP_DOWN combo pinned to a narrow width still shows its text',
      (WidgetTester tester) async {
    const selected = 'Default Locator';
    final combo = VCombo()
      ..id = 21
      ..style = SWT.DROP_DOWN | SWT.READ_ONLY
      ..enabled = true
      ..text = selected
      ..items = ['None', selected, 'All Locators']
      // What GridLayout hands the control when the page sets widthHint = 150.
      ..bounds = _rect(0, 0, 150, 30);

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
      text: TextSpan(text: selected, style: style),
      maxLines: 1,
      textDirection: TextDirection.ltr,
    )..layout();

    expect(viewportWidth, greaterThanOrEqualTo(painter.width),
        reason: 'The text viewport (${viewportWidth.toStringAsFixed(2)}px) is '
            'narrower than the selected text (${painter.width.toStringAsFixed(2)}px): '
            'the last character is cut off.');
  });

  testWidgets('a combo with room to spare keeps its full text padding',
      (WidgetTester tester) async {
    // The natural width is what ComboSizes.java's HORIZONTAL_PADDING was
    // measured from, so the fitted padding must not shrink a combo that fits.
    const selected = 'Default Locator';
    final combo = VCombo()
      ..id = 22
      ..style = SWT.DROP_DOWN | SWT.READ_ONLY
      ..enabled = true
      ..text = selected
      ..items = ['None', selected, 'All Locators'];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: ComboSwt<VCombo>(value: combo),
      ),
    ));
    await tester.pumpAndSettle();

    final editable = find.byType(EditableText);
    final double boxWidth = tester.getSize(find.byType(ComboSwt<VCombo>)).width;
    final double viewportWidth = tester.getSize(editable).width;

    // 12px of padding either side of the text, the 1px border on each side, and
    // the arrow cell (20px icon + 8px spacing).
    expect(boxWidth - viewportWidth, 54.0);
  });
}
