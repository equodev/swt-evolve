// A CCombo drops the colors the application painted it with unless use_swt_colors asks for them --
// the rule every other widget follows through getBackgroundColor/getForegroundColor. Reading
// state.background directly leaves a themed application's combo on the light system colors Java
// hands out, over a dark toolbar.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/ccombo_theme_extension.dart';

/// The light system-color pair a themed application hands its toolbar controls.
const _appBackground = Color(0xFFF0F0F0);
const _appForeground = Color(0xFF000000);

class _SilentCComboSwt extends CComboSwt<VCCombo> {
  const _SilentCComboSwt({required super.value});

  @override
  void sendEvent(VCCombo val, String ev, VEvent? payload) {}
}

VColor _vColor(int r, int g, int b) => VColor()
  ..alpha = 0xFF
  ..red = r
  ..green = g
  ..blue = b;

VCCombo _appColoredCombo() => VCCombo()
  ..swt = 'CCombo'
  ..id = 11
  ..style = 0
  ..enabled = true
  ..items = const ['TWT', 'TVD', 'TVDSS', 'FREQ']
  ..text = 'TVDSS'
  ..background = _vColor(0xF0, 0xF0, 0xF0)
  ..foreground = _vColor(0x00, 0x00, 0x00)
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 180
    ..height = 24);

Future<void> _pump(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.dark,
    contentWidget: Center(
      child: SizedBox(
        width: 180,
        height: 24,
        child: _SilentCComboSwt(value: _appColoredCombo()),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

/// The ground the closed field is painted on.
Color? _fieldBackground(WidgetTester tester) {
  final container = tester.widget<AnimatedContainer>(find.byType(AnimatedContainer));
  return (container.decoration as BoxDecoration?)?.color;
}

Color? _fieldTextColor(WidgetTester tester) =>
    tester.widget<DropdownMenu<String>>(find.byType(DropdownMenu<String>)).textStyle?.color;

CComboThemeExtension _theme(WidgetTester tester) =>
    Theme.of(tester.element(find.byType(DropdownMenu<String>)))
        .extension<CComboThemeExtension>()!;

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('the application colors are dropped for the theme by default', (tester) async {
    await _pump(tester);

    final widgetTheme = _theme(tester);
    expect(_fieldBackground(tester), widgetTheme.backgroundColor);
    expect(_fieldBackground(tester), isNot(_appBackground));
    expect(_fieldTextColor(tester), widgetTheme.textColor);
    expect(_fieldTextColor(tester), isNot(_appForeground));
  });

  testWidgets('use_swt_colors / use_swt_fonts still hand the control the application colors',
      (tester) async {
    setConfigFlags(ConfigFlags()
      ..use_swt_colors = true
      ..use_swt_fonts = true);

    await _pump(tester);

    expect(_fieldBackground(tester), _appBackground);
    expect(_fieldTextColor(tester), _appForeground);
  });
}
