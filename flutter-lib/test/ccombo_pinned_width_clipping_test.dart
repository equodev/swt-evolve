// DropdownMenu renders the arrow in an IconButton at its 48px minimum tap target,
// not at the theme's iconSize that _calculateMinWidth() budgets, so the chrome
// outgrows the width -- pinned or preferred -- and cuts the last glyph off.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

const _selected = 'X-Large';
const _items = ['Small', 'Medium', 'Large', _selected];

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

// The test fallback font gives every glyph a full em box, which would make the
// deficit an artefact of the harness rather than a real one. Load the face the
// theme asks for so the widths are the application's own.
Future<void> _loadInter() async {
  final loader = FontLoader('Inter')
    ..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
  await loader.load();
}

VCCombo _ccombo(int id) => VCCombo()
  ..id = id
  ..style = SWT.BORDER | SWT.READ_ONLY
  ..enabled = true
  ..text = _selected
  ..items = _items;

Future<double> _pumpAndMeasure(WidgetTester tester, VCCombo value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: CComboSwt<VCCombo>(value: value),
    ),
  ));
  await tester.pumpAndSettle();
  return tester.getSize(find.byType(EditableText)).width;
}

double _textWidth(WidgetTester tester) {
  final editable = tester.widget<EditableText>(find.byType(EditableText));
  final painter = TextPainter(
    text: TextSpan(text: _selected, style: editable.style),
    maxLines: 1,
    textDirection: TextDirection.ltr,
  )..layout();
  return painter.width;
}

void main() {
  setUpAll(_loadInter);

  testWidgets('a CCombo pinned to a narrow width still shows its text',
      (WidgetTester tester) async {
    // What RowLayout hands the control when the contribution pins 80px.
    final viewport =
        await _pumpAndMeasure(tester, _ccombo(31)..bounds = _rect(0, 0, 80, 22));
    final needed = _textWidth(tester);

    expect(viewport, greaterThanOrEqualTo(needed),
        reason: 'The text viewport (${viewport.toStringAsFixed(2)}px) is '
            'narrower than the selected text (${needed.toStringAsFixed(2)}px): '
            'the last character is cut off.');
  });

  testWidgets('a CCombo at its own preferred width shows its text in full',
      (WidgetTester tester) async {
    // _calculateMinWidth() budgets iconSize for the arrow, so the rendered arrow
    // has to fit in that budget or even an unpinned combo comes up short.
    final viewport = await _pumpAndMeasure(tester, _ccombo(32));
    final needed = _textWidth(tester);

    expect(viewport, greaterThanOrEqualTo(needed),
        reason: 'An unpinned CCombo left its text '
            '${viewport.toStringAsFixed(2)}px for ${needed.toStringAsFixed(2)}px.');
  });

  testWidgets('a CCombo with room to spare keeps its full text padding',
      (WidgetTester tester) async {
    // The natural width is what CComboSizes.java's padding constants were
    // measured from, so the fitted padding must not shrink a combo that fits.
    await _pumpAndMeasure(tester, _ccombo(33));

    final menu =
        tester.widget<DropdownMenu<String>>(find.byType(DropdownMenu<String>));
    expect(menu.inputDecorationTheme!.contentPadding,
        const EdgeInsets.symmetric(horizontal: 12.0, vertical: 4.0));
  });
}
