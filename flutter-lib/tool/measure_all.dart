import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/theme/theme.dart';
import './measure.dart';
import './measure_button.dart' as button;
import './measure_ccombo.dart' as ccombo;
import './measure_clabel.dart' as clabel;
import './measure_combo.dart' as combo;
import './measure_label.dart' as label;
import './measure_link.dart' as link;
import './measure_progressbar.dart' as progressbar;
import './measure_scale.dart' as scale;
import './measure_slider.dart' as slider;
import './measure_text.dart' as text;

void main() {
  final measurer = WidgetMeasurer();

  button.setupCases(measurer);
  ccombo.setupCases(measurer);
  clabel.setupCases(measurer);
  combo.setupCases(measurer);
  label.setupCases(measurer);
  link.setupCases(measurer);
  progressbar.setupCases(measurer);
  scale.setupCases(measurer);
  slider.setupCases(measurer);
  text.setupCases(measurer);

  print('Generated ${measurer.testCases.length} total test cases');
  // Run single app with all test cases
  runApp(MeasurementApp(measurer: measurer));
}

List<ThemeConfig> getThemes() => [
  ThemeConfig('NonDefault', () => createLightNonDefaultTheme(null)),
  ThemeConfig('Default', () => createLightDefaultTheme(null)),
];
