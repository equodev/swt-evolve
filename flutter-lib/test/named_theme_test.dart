// The theme a name selects. Java sizes every name it does not know in the default scale, so Flutter
// has to draw those names in the default theme, never in one of its own choosing.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/named_theme.dart';
import 'package:swtflutter/src/theme/type_scales.dart';

const _registered = NamedTheme(
  lightColorScheme: ColorScheme.light(primary: Color(0xFF123456)),
  darkColorScheme: ColorScheme.dark(primary: Color(0xFF654321)),
  focusIndicators: true,
);

Future<ThemeData> _drawn(WidgetTester tester, ConfigFlags flags, ThemeMode mode) async {
  setConfigFlags(flags);
  late ThemeData theme;
  await tester.pumpWidget(EvolveApp(
    theme: mode,
    contentWidget: Builder(builder: (context) {
      theme = Theme.of(context);
      return const SizedBox();
    }),
  ));
  return theme;
}

void main() {
  tearDown(() {
    namedThemes.remove('registered');
    unnamedThemeBuilder = null;
    resetConfigFlags();
  });

  test('the default theme is the only one built in', () {
    expect(namedThemes.keys, [kDefaultThemeName]);
    expect(kTypeScales.keys, [kStandardTypeScale.name]);
  });

  test('a name nothing registered selects the default theme', () {
    expect(namedThemeFor('no-such-theme'), same(namedThemes[kDefaultThemeName]));
    expect(namedThemeFor(null), same(namedThemes[kDefaultThemeName]));
  });

  test('the default theme draws in the shared scale, without focus rings', () {
    final standard = namedThemeFor(kDefaultThemeName);
    expect(standard.typeScale, isNull);
    expect(standard.focusIndicators, isFalse);
    expect(standard.darkColorScheme, isNotNull);
  });

  test('a registered theme decides its focus indicators unless the flag does', () {
    namedThemes['registered'] = _registered;
    setConfigFlags(ConfigFlags()..theme_name = 'registered');
    expect(focusIndicators, isTrue);
    setConfigFlags(ConfigFlags()
      ..theme_name = 'registered'
      ..focus_indicators = false);
    expect(focusIndicators, isFalse);
  });

  for (final mode in [ThemeMode.light, ThemeMode.dark]) {
    final dark = mode == ThemeMode.dark;

    testWidgets('${mode.name}: an unknown name draws the default theme', (tester) async {
      final theme = await _drawn(tester, ConfigFlags()..theme_name = 'no-such-theme', mode);
      final standard = namedThemeFor(kDefaultThemeName);
      expect(theme.colorScheme.primary,
          (dark ? standard.darkColorScheme! : standard.lightColorScheme).primary);
    });

    testWidgets('${mode.name}: a registered name draws its theme', (tester) async {
      namedThemes['registered'] = _registered;
      final theme = await _drawn(tester, ConfigFlags()..theme_name = 'registered', mode);
      expect(theme.colorScheme.primary, dark ? const Color(0xFF654321) : const Color(0xFF123456));
    });

    testWidgets('${mode.name}: no name draws what the app built on this one supplies',
        (tester) async {
      unnamedThemeBuilder = (backgroundColor, {required bool dark}) => ThemeData(
          colorScheme: dark
              ? const ColorScheme.dark(primary: Color(0xFF00FF00))
              : const ColorScheme.light(primary: Color(0xFF0000FF)));
      final theme = await _drawn(tester, ConfigFlags()..theme_name = '', mode);
      expect(theme.colorScheme.primary, dark ? const Color(0xFF00FF00) : const Color(0xFF0000FF));
    });
  }
}
