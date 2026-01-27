import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/link.dart';
import 'package:swtflutter/src/gen/swt.dart';
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
  final styles = [('NONE', SWT.NONE)];

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

  print('Generated ${measurer.testCases.length} Link test cases');
}

MeasurementCase createCase(
  String caseName,
  (String, int) style,
  String text, {
  int? fontSize,
  bool useFontTheme = false,
}) {
  final (value, expectedComponents) = createVLink(
    style,
    text,
    fontSize: fontSize,
  );
  return MeasurementCase(
    descr: caseName,
    style: style.$1,
    useFontTheme: useFontTheme,
    fqn: 'org.eclipse.swt.widgets.Link',
    expectedComponents: expectedComponents,
    widgetBuilder: (key) {
      getConfigFlags().use_swt_fonts = !useFontTheme;
      return LinkSwt(key: key, value: value);
    },
  );
}

(VLink, Map<String, dynamic>) createVLink(
  (String, int) style,
  String text, {
  int? fontSize,
}) {
  final value = VLink.empty()
    ..style = style.$2
    ..text = text
    ..font = fontSize != null
        ? (VFont.empty()..fontData = [VFontData.empty()..height = fontSize])
        : null;

  final expectedComponents = <String, dynamic>{'text': text};
  return (value, expectedComponents);
}
