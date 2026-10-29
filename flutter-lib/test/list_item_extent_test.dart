// A List item is laid out at a whole-pixel height, the one ListSizes.getItemHeight reports in Java,
// so the item height, the preferred size and the top index after a scroll agree with what the
// client draws.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/theme/theme_extensions/list_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_settings/list_theme_settings.dart';

Future<ListThemeExtension> _listTheme(WidgetTester tester) async {
  late ListThemeExtension theme;
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Builder(
        builder: (context) {
          theme = Theme.of(context).extension<ListThemeExtension>()!;
          return const SizedBox.shrink();
        },
      ),
    ),
  );
  return theme;
}

void main() {
  // The test fonts measure whole-pixel lines, so the line is given: a desktop system font at 18pt
  // measures about 20.57 pixels.
  testWidgets(
    'a line taller than the theme item rounds the item extent up to a whole pixel',
    (tester) async {
      final theme = await _listTheme(tester);
      const line = 20.57;
      expect(line + theme.itemPadding.vertical, greaterThan(theme.itemHeight));

      expect(
        listItemExtentForLine(theme, line),
        (line + theme.itemPadding.vertical).ceilToDouble(),
      );
    },
  );

  testWidgets('a line that fits keeps the theme item height', (tester) async {
    final theme = await _listTheme(tester);

    expect(listItemExtentForLine(theme, 15.3), theme.itemHeight);
  });
}
