// A DROP_DOWN combo whose bounds are shorter than its preferred height clips
// the selected text at the bottom.
//
// An application is free to pin a control's height through GridData.heightHint,
// and real ones do: a preference page hints 18px for its READ_ONLY combos --
// enough for a native combo cell, well under the ~34px this one prefers. The
// text-field padding is a fixed inset, so the Row leaves EditableText a 2px
// viewport and the glyphs are cut off at the bottom. (Measured live on that
// page: combo render box 364x20, EditableText viewport 310x2, line height 16.)
//
// The chrome must yield to the text, not the other way round: the text keeps a
// full line height (centred in whatever height it was given) and the vertical
// padding absorbs the deficit.

import 'package:flutter/material.dart';
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

void main() {
  testWidgets('DROP_DOWN combo pinned to a short height still shows its text',
      (WidgetTester tester) async {
    const selected = 'No Proxy';
    final combo = VCombo()
      ..id = 11
      ..style = SWT.DROP_DOWN | SWT.READ_ONLY
      ..enabled = true
      ..text = selected
      ..items = ['No Proxy', 'Use System Proxy Settings', 'Manual Config']
      // What GridLayout hands the control when the page sets heightHint = 18:
      // the row grows to the label's 20px, and that is the control's height.
      ..bounds = _rect(0, 0, 364, 20);

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

    final double viewportHeight = tester.getSize(editable).height;
    final TextStyle style = tester.widget<EditableText>(editable).style;
    final painter = TextPainter(
      text: TextSpan(text: selected, style: style),
      maxLines: 1,
      textDirection: TextDirection.ltr,
    )..layout();

    expect(viewportHeight, greaterThanOrEqualTo(painter.height),
        reason: 'The text viewport (${viewportHeight.toStringAsFixed(2)}px) is '
            'shorter than one line of text (${painter.height.toStringAsFixed(2)}px): '
            'the selected text is clipped at the bottom.');
  });

  testWidgets('a combo with room to spare keeps its full text padding',
      (WidgetTester tester) async {
    // The natural height is what ComboSizes.java's VERTICAL_PADDING was measured
    // from, so the fitted padding must not shrink a combo that fits.
    const selected = 'No Proxy';
    final combo = VCombo()
      ..id = 12
      ..style = SWT.DROP_DOWN | SWT.READ_ONLY
      ..enabled = true
      ..text = selected
      ..items = ['No Proxy', 'Manual Config'];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: ComboSwt<VCombo>(value: combo),
      ),
    ));
    await tester.pumpAndSettle();

    final editable = find.byType(EditableText);
    final double boxHeight = tester.getSize(find.byType(ComboSwt<VCombo>)).height;
    final double viewportHeight = tester.getSize(editable).height;

    // 8px of padding above and below the text, and the 1px border on each side.
    expect(boxHeight - viewportHeight, 18.0);
  });
}
