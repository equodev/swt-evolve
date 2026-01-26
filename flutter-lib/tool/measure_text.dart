import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme.dart';
import './measure.dart';
import './measure_data.dart';

void main() {
  final measurer = WidgetMeasurer();
  setupCases(measurer);
  runApp(MeasurementApp(measurer: measurer));
}

void setupCases(WidgetMeasurer measurer) {
  final styles = [
    ('LEFT', SWT.LEFT),
    ('LEFT|PASSWORD', SWT.LEFT | SWT.PASSWORD),
    ('LEFT|SEARCH', SWT.LEFT | SWT.SEARCH),
    ('LEFT|READ_ONLY', SWT.LEFT | SWT.READ_ONLY),
    ('LEFT|WRAP', SWT.LEFT | SWT.WRAP),
    ('CENTER', SWT.CENTER),
    ('CENTER|PASSWORD', SWT.CENTER | SWT.PASSWORD),
    ('CENTER|SEARCH', SWT.CENTER | SWT.SEARCH),
    ('CENTER|READ_ONLY', SWT.CENTER | SWT.READ_ONLY),
    ('CENTER|WRAP', SWT.CENTER | SWT.WRAP),
    ('RIGHT', SWT.RIGHT),
    ('RIGHT|PASSWORD', SWT.RIGHT | SWT.PASSWORD),
    ('RIGHT|SEARCH', SWT.RIGHT | SWT.SEARCH),
    ('RIGHT|READ_ONLY', SWT.RIGHT | SWT.READ_ONLY),
    ('RIGHT|WRAP', SWT.RIGHT | SWT.WRAP),
    ('MULTI', SWT.MULTI),
    ('MULTI|PASSWORD', SWT.MULTI | SWT.PASSWORD),
    ('MULTI|SEARCH', SWT.MULTI | SWT.SEARCH),
    ('MULTI|READ_ONLY', SWT.MULTI | SWT.READ_ONLY),
    ('MULTI|WRAP', SWT.MULTI | SWT.WRAP),
    ('MULTI|LEFT', SWT.MULTI | SWT.LEFT),
    ('MULTI|LEFT|PASSWORD', SWT.MULTI | SWT.LEFT | SWT.PASSWORD),
    ('MULTI|LEFT|SEARCH', SWT.MULTI | SWT.LEFT | SWT.SEARCH),
    ('MULTI|LEFT|READ_ONLY', SWT.MULTI | SWT.LEFT | SWT.READ_ONLY),
    ('MULTI|LEFT|WRAP', SWT.MULTI | SWT.LEFT | SWT.WRAP),
    ('MULTI|CENTER', SWT.MULTI | SWT.CENTER),
    ('MULTI|CENTER|PASSWORD', SWT.MULTI | SWT.CENTER | SWT.PASSWORD),
    ('MULTI|CENTER|SEARCH', SWT.MULTI | SWT.CENTER | SWT.SEARCH),
    ('MULTI|CENTER|READ_ONLY', SWT.MULTI | SWT.CENTER | SWT.READ_ONLY),
    ('MULTI|CENTER|WRAP', SWT.MULTI | SWT.CENTER | SWT.WRAP),
    ('MULTI|RIGHT', SWT.MULTI | SWT.RIGHT),
    ('MULTI|RIGHT|PASSWORD', SWT.MULTI | SWT.RIGHT | SWT.PASSWORD),
    ('MULTI|RIGHT|SEARCH', SWT.MULTI | SWT.RIGHT | SWT.SEARCH),
    ('MULTI|RIGHT|READ_ONLY', SWT.MULTI | SWT.RIGHT | SWT.READ_ONLY),
    ('MULTI|RIGHT|WRAP', SWT.MULTI | SWT.RIGHT | SWT.WRAP),
    ('SINGLE', SWT.SINGLE),
    ('SINGLE|PASSWORD', SWT.SINGLE | SWT.PASSWORD),
    ('SINGLE|SEARCH', SWT.SINGLE | SWT.SEARCH),
    ('SINGLE|READ_ONLY', SWT.SINGLE | SWT.READ_ONLY),
    ('SINGLE|WRAP', SWT.SINGLE | SWT.WRAP),
    ('SINGLE|LEFT', SWT.SINGLE | SWT.LEFT),
    ('SINGLE|LEFT|PASSWORD', SWT.SINGLE | SWT.LEFT | SWT.PASSWORD),
    ('SINGLE|LEFT|SEARCH', SWT.SINGLE | SWT.LEFT | SWT.SEARCH),
    ('SINGLE|LEFT|READ_ONLY', SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY),
    ('SINGLE|LEFT|WRAP', SWT.SINGLE | SWT.LEFT | SWT.WRAP),
    ('SINGLE|CENTER', SWT.SINGLE | SWT.CENTER),
    ('SINGLE|CENTER|PASSWORD', SWT.SINGLE | SWT.CENTER | SWT.PASSWORD),
    ('SINGLE|CENTER|SEARCH', SWT.SINGLE | SWT.CENTER | SWT.SEARCH),
    ('SINGLE|CENTER|READ_ONLY', SWT.SINGLE | SWT.CENTER | SWT.READ_ONLY),
    ('SINGLE|CENTER|WRAP', SWT.SINGLE | SWT.CENTER | SWT.WRAP),
    ('SINGLE|RIGHT', SWT.SINGLE | SWT.RIGHT),
    ('SINGLE|RIGHT|PASSWORD', SWT.SINGLE | SWT.RIGHT | SWT.PASSWORD),
    ('SINGLE|RIGHT|SEARCH', SWT.SINGLE | SWT.RIGHT | SWT.SEARCH),
    ('SINGLE|RIGHT|READ_ONLY', SWT.SINGLE | SWT.RIGHT | SWT.READ_ONLY),
    ('SINGLE|RIGHT|WRAP', SWT.SINGLE | SWT.RIGHT | SWT.WRAP),
  ];

  for (final style in styles) {
    for (final text in Texts) {
      for (final fontSize in Fonts) {
        final caseName = '${text.$1}_font${fontSize}';
        measurer.addTestCase(
          createCase(
            caseName,
            style,
            text.$2,
            fontSize: fontSize,
            useFontTheme: false,
          ),
        );
      }
    }

    measurer.addThemeCase(
      createCase('theme', style, "Sample", useFontTheme: true),
    );
  }

  print('Generated ${measurer.testCases.length} Text test cases');
}

MeasurementCase createCase(
  String caseName,
  (String, int) style,
  String text, {
  int? fontSize,
  bool useFontTheme = false,
}) {
  final (value, expectedComponents) = createVText(
    style,
    text,
    fontSize: fontSize,
  );
  return MeasurementCase(
    descr: caseName,
    style: style.$1,
    useFontTheme: useFontTheme,
    fqn: 'org.eclipse.swt.widgets.Text',
    expectedComponents: expectedComponents,
    widgetBuilder: (key) {
      getConfigFlags().use_swt_fonts = !useFontTheme;
      return TextSwt(key: key, value: value);
    },
  );
}

(VText, Map<String, dynamic>) createVText(
  (String, int) style,
  String text, {
  int? fontSize,
}) {
  final value = VText.empty()
    ..style = style.$2
    ..text = text
    ..font = fontSize != null
        ? (VFont.empty()..fontData = [VFontData.empty()..height = fontSize])
        : null;

  final expectedComponents = <String, dynamic>{'text': text};
  return (value, expectedComponents);
}
