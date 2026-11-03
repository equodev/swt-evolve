// Light or dark: the theme the application defines prevails; without one, the client paints in its
// system's appearance (the browser's on web, the OS's on desktop), which main() resolves at launch.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  Future<ThemeMode?> paintedMode(WidgetTester tester, {required ThemeMode system, String? appTheme}) async {
    setConfigFlags(ConfigFlags()..force_theme = appTheme);
    await tester.pumpWidget(EvolveApp(contentWidget: const SizedBox(), theme: system));
    return tester.widget<MaterialApp>(find.byType(MaterialApp)).themeMode;
  }

  testWidgets('with no theme of its own the app paints in its system\'s appearance', (tester) async {
    expect(await paintedMode(tester, system: ThemeMode.dark), ThemeMode.dark);
    expect(await paintedMode(tester, system: ThemeMode.light), ThemeMode.light);
  });

  testWidgets('the application\'s theme prevails over the system', (tester) async {
    expect(await paintedMode(tester, system: ThemeMode.dark, appTheme: 'light'), ThemeMode.light);
    expect(await paintedMode(tester, system: ThemeMode.light, appTheme: 'dark'), ThemeMode.dark);
  });

  testWidgets('a theme naming no variant leaves it to the system', (tester) async {
    expect(await paintedMode(tester, system: ThemeMode.dark, appTheme: 'solarized'), ThemeMode.dark);
  });

  test('a window Java opens with "system" resolves against the client\'s own appearance', () {
    expect(parseForcedThemeMode('system'), isNull);
    expect(parseForcedThemeMode(' Dark '), ThemeMode.dark);
    expect(parseForcedThemeMode(null), isNull);
  });
}
