// A Combo pinned well below its preferred width gives the text less room than
// native SWT does, and hands the same too-narrow width to its dropdown.
//
// An application is free to pin a Combo through GridData.widthHint, and a real
// search form does: READ_ONLY combos hinted to 100px, holding "any prep time"
// over seven "Less than NN mins" items. Native SWT clips that value too -- 100px
// is under what it needs on any platform -- but it clips it later, because its
// trim is only the bezel and the arrow glyph. Here two further insets stand in
// the way:
//
//   * the gap between the text and the arrow is a fixed 8px that the text
//     padding's fitting pass never reaches, so the value loses another
//     character on top of what native loses;
//   * the dropdown is pinned to the field's width, so every item wraps onto two
//     lines and the list grows down over whatever sits below it -- something no
//     SWT platform does, since a Combo item is always a single line.
//
// The rule is the one combo_pinned_width_clipping_test.dart already states --
// the chrome must yield to the text, not the other way round -- extended past
// the padding to the arrow's gap, and down into the list.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

const String _selected = 'any prep time';
const List<String> _items = [
  _selected,
  'Less than 15 mins',
  'Less than 30 mins',
  'Less than 45 mins',
  'Less than 1 hour',
  'Less than 2 hours',
  'Less than 3 hours',
];

// The test fallback font gives every glyph a full em box, which would make the
// deficit an artefact of the harness rather than the one the reporter saw. Load
// the real face the theme asks for so the widths are the app's own.
Future<void> _loadInter() async {
  final loader = FontLoader('Inter')
    ..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
  await loader.load();
}

VCombo _hintedCombo() => VCombo()
  ..swt = 'Combo'
  ..id = 1445
  ..style = SWT.DROP_DOWN | SWT.READ_ONLY
  ..enabled = true
  ..text = _selected
  ..items = _items
  // What GridLayout hands the control when the form sets widthHint = 100.
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 100
    ..height = 30);

Future<void> _pumpCombo(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: ComboSwt<VCombo>(value: _hintedCombo()),
    ),
  ));
  await tester.pumpAndSettle();
}

double _textWidth(String text, TextStyle style) => (TextPainter(
      text: TextSpan(text: text, style: style),
      maxLines: 1,
      textDirection: TextDirection.ltr,
    )..layout())
    .width;

void main() {
  setUpAll(_loadInter);

  testWidgets('a Combo pinned to 100px keeps everything but the arrow glyph',
      (WidgetTester tester) async {
    await _pumpCombo(tester);

    final editable = find.byType(EditableText);
    expect(editable, findsOneWidget);

    final double viewportWidth = tester.getSize(editable).width;
    // The border on each side and the arrow glyph -- the trim native SWT keeps
    // too. Everything else belongs to the text once the width is this tight.
    const double irreducibleChrome = 1.0 * 2 + 20.0;
    expect(viewportWidth, 100.0 - irreducibleChrome,
        reason: 'The text viewport is ${viewportWidth.toStringAsFixed(2)}px of '
            'the 100px the form allowed: an inset other than the border and the '
            'arrow glyph is still taking room from the value.');
  });

  testWidgets('the dropdown is as wide as its widest item, not as the field',
      (WidgetTester tester) async {
    await _pumpCombo(tester);

    await tester.tap(find.byType(ComboSwt<VCombo>));
    await tester.pumpAndSettle();

    // The list renders one Text per item; the field itself uses EditableText.
    final items = find.byType(Text);
    expect(items, findsNWidgets(_items.length));

    final TextStyle style = tester.widget<Text>(items.first).style!;
    for (final String item in _items) {
      final Finder cell = find.byWidgetPredicate(
          (Widget w) => w is Text && w.data == item);
      expect(cell, findsOneWidget);
      final Size cellSize = tester.getSize(cell);
      final double itemWidth = _textWidth(item, style);
      expect(cellSize.width, greaterThanOrEqualTo(itemWidth),
          reason: 'Item "$item" is laid out ${cellSize.width.toStringAsFixed(2)}px '
              'wide for ${itemWidth.toStringAsFixed(2)}px of text: it wraps '
              'onto a second line instead of widening the list.');
    }
  });

  testWidgets('a READ_ONLY Combo paints no text selection',
      (WidgetTester tester) async {
    await _pumpCombo(tester);

    final EditableText field = tester.widget<EditableText>(
      find.byType(EditableText),
    );
    expect(field.selectionColor?.alpha ?? 0, 0,
        reason: 'A double-click leaves a word selection behind in the '
            'controller; painting it shows a highlight a native READ_ONLY '
            'Combo never has.');
  });
}
