import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/theme/theme.dart';
import './measure.dart';
import './measure_button.dart' as button;
import './measure_label.dart' as label;

void main() {
  final measurer = WidgetMeasurer();

  button.setupCases(measurer);
  label.setupCases(measurer);

  print('Generated ${measurer.testCases.length} total test cases');
  // Run single app with all test cases
  runApp(MeasurementApp(measurer: measurer));
}

List<ThemeConfig> getThemes() => [
  ThemeConfig('NonDefault', () => createLightNonDefaultTheme(null)),
  ThemeConfig('Default', () => createLightDefaultTheme(null)),
];
