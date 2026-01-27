import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/scale.dart';
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
  final styles = [('HORIZONTAL', SWT.HORIZONTAL), ('VERTICAL', SWT.VERTICAL)];

  for (final style in styles) {
    final caseName = '';
    measurer.addTestCase(createCase(caseName, style, useFontTheme: false));

    measurer.addThemeCase(createCase('theme', style, useFontTheme: true));
  }

  print('Generated ${measurer.testCases.length} Scale test cases');
}

MeasurementCase createCase(
  String caseName,
  (String, int) style, {
  bool useFontTheme = false,
}) {
  final (value, expectedComponents) = createVScale(style);
  return MeasurementCase(
    descr: caseName,
    style: style.$1,
    useFontTheme: useFontTheme,
    fqn: 'org.eclipse.swt.widgets.Scale',
    expectedComponents: expectedComponents,
    widgetBuilder: (key) {
      getConfigFlags().use_swt_fonts = !useFontTheme;
      return ScaleSwt(key: key, value: value);
    },
  );
}

(VScale, Map<String, dynamic>) createVScale((String, int) style) {
  final value = VScale.empty()..style = style.$2;

  final expectedComponents = <String, dynamic>{};
  return (value, expectedComponents);
}
