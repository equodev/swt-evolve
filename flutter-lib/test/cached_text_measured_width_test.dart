// A Table cell decides its padding from how wide its text is, and then draws that text as a
// ColorCachedText. Reading the width off the label's own shaped text spares shaping it twice, which
// is only right if the width is the one the separate measurement gives, to the last bit: the padding
// must come out the same.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/impl/table_evolve.dart' show measureTableText;
import 'package:swtflutter/src/impl/utils/cached_text.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart';
import 'package:swtflutter/src/theme/theme_extensions/table_theme_extension.dart';

const _texts = [
  'Row 1234 / 0',
  'A cell far too long for the column it was given to show',
  'trailing spaces   ',
  '  leading',
  'Ünïcödé — mixed «text» 123',
  'iiiiiiiiii',
  'WWWWWWWWWW',
  'tab\tinside',
  'x',
];

const _aligns = [TextAlign.left, TextAlign.center, TextAlign.right];

void main() {
  setUpAll(() async {
    final inter = FontLoader('Inter')..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
    await inter.load();
  });

  testWidgets('a cell label measures its text exactly as measureTableText does', (tester) async {
    late BuildContext context;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Builder(builder: (c) {
        context = c;
        return const SizedBox();
      }),
    ));
    final theme = Theme.of(context).extension<TableThemeExtension>()!;
    final styles = [
      getTextStyle(context: context, font: null, textColor: Colors.black, baseTextStyle: theme.rowTextStyle),
      getTextStyle(context: context, font: null, textColor: Colors.transparent, baseTextStyle: theme.rowTextStyle),
      const TextStyle(fontFamily: 'Inter', fontSize: 13, fontWeight: FontWeight.w400, letterSpacing: 0.4, color: Colors.blue),
    ];
    for (final style in styles) {
      for (final align in _aligns) {
        for (final text in _texts) {
          final label = ColorCachedText(text, style: style, overflow: TextOverflow.ellipsis, textAlign: align);
          final width = label.measuredWidth(context, style);
          expect(width, isNotNull, reason: 'the table row style must take the shared path');
          expect(width, measureTableText(text, style), reason: '"$text" $align $style');
        }
      }
    }
  });

  testWidgets('it measures the same once the label is laid out narrower than its text', (tester) async {
    final style = const TextStyle(fontFamily: 'Inter', fontSize: 14, letterSpacing: 0, color: Colors.black);
    // Set in full: the theme's body style a label inherits from sets a letter spacing of its own.
    final labels = [
      for (final align in _aligns)
        for (final text in _texts) ColorCachedText(text, style: style, overflow: TextOverflow.ellipsis, textAlign: align),
    ];
    late BuildContext context;
    await tester.pumpWidget(MaterialApp(
      home: Material(child: Builder(builder: (c) {
        context = c;
        return Column(children: [for (final label in labels) SizedBox(width: 40, child: label)]);
      })),
    ));
    for (final label in labels) {
      expect(label.measuredWidth(context, style), measureTableText(label.data!, style),
          reason: '"${label.data}" ${label.textAlign}');
    }
  });

  testWidgets('a label measured before it is built shapes its text once', (tester) async {
    late BuildContext context;
    await tester.pumpWidget(MaterialApp(home: Material(child: Builder(builder: (c) {
      context = c;
      return const SizedBox();
    }))));
    const style = TextStyle(fontFamily: 'Inter', fontSize: 14, letterSpacing: 0, color: Colors.black);
    const label = ColorCachedText('shaped once', style: style, overflow: TextOverflow.ellipsis);
    final painters = RenderCachedText.sharedPainters;
    label.measuredWidth(context, style);
    expect(RenderCachedText.sharedPainters, painters + 1);
    await tester.pumpWidget(MaterialApp(home: Material(child: Builder(builder: (c) {
      context = c;
      return const Center(child: SizedBox(width: 50, child: label));
    }))));
    expect(RenderCachedText.sharedPainters, painters + 1, reason: 'the label must take up the measured text');
    await tester.pumpWidget(const SizedBox());
    expect(RenderCachedText.sharedPainters, painters);
  });

  testWidgets('a style that shapes differently is left to the separate measure', (tester) async {
    late BuildContext context;
    await tester.pumpWidget(MaterialApp(home: Material(child: Builder(builder: (c) {
      context = c;
      return const SizedBox();
    }))));
    // Set in full: the theme's body style a label inherits from sets a letter spacing of its own.
    const drawn = TextStyle(fontFamily: 'Inter', fontSize: 14, letterSpacing: 0);
    const label = ColorCachedText('abc', style: drawn);
    expect(label.measuredWidth(context, drawn.copyWith(fontSize: 15)), isNull);
    expect(label.measuredWidth(context, drawn.copyWith(letterSpacing: 1)), isNull);
    expect(label.measuredWidth(context, drawn.copyWith(color: Colors.red)), isNotNull);
    // The label draws with the theme's letter spacing, which the bare style does not have.
    const inheriting = ColorCachedText('abc', style: TextStyle(fontFamily: 'Inter', fontSize: 14));
    expect(inheriting.measuredWidth(context, const TextStyle(fontFamily: 'Inter', fontSize: 14)), isNull);
  });
}
