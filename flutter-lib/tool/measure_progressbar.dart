import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/progressbar.dart';
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
  final styles = [
    ('HORIZONTAL', SWT.HORIZONTAL),
    ('HORIZONTAL|SMOOTH', SWT.HORIZONTAL | SWT.SMOOTH),
    ('HORIZONTAL|INDETERMINATE', SWT.HORIZONTAL | SWT.INDETERMINATE),
    ('VERTICAL', SWT.VERTICAL),
    ('VERTICAL|SMOOTH', SWT.VERTICAL | SWT.SMOOTH),
    ('VERTICAL|INDETERMINATE', SWT.VERTICAL | SWT.INDETERMINATE),
  ];

  for (final style in styles) {
    final caseName = '';
    measurer.addTestCase(createCase(caseName, style, useFontTheme: false));

    measurer.addThemeCase(createCase('theme', style, useFontTheme: true));
  }

  print('Generated ${measurer.testCases.length} ProgressBar test cases');
}

MeasurementCase createCase(
  String caseName,
  (String, int) style, {
  bool useFontTheme = false,
}) {
  final (value, expectedComponents) = createVProgressBar(style);
  return MeasurementCase(
    descr: caseName,
    style: style.$1,
    useFontTheme: useFontTheme,
    fqn: 'org.eclipse.swt.widgets.ProgressBar',
    expectedComponents: expectedComponents,
    widgetBuilder: (key) {
      getConfigFlags().use_swt_fonts = !useFontTheme;
      return ProgressBarSwt(key: key, value: value);
    },
  );
}

(VProgressBar, Map<String, dynamic>) createVProgressBar((String, int) style) {
  final value = VProgressBar.empty()..style = style.$2;

  final expectedComponents = <String, dynamic>{};
  return (value, expectedComponents);
}
