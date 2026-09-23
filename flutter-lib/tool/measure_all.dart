import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/theme/theme.dart';
import 'package:swtflutter/src/theme/type_scales.dart';
import './measure.dart';
import './measure_button.dart' as button;
import './measure_canvas.dart' as canvas;
import './measure_ccombo.dart' as ccombo;
import './measure_clabel.dart' as clabel;
import './measure_combo.dart' as combo;
import './measure_label.dart' as label;
import './measure_link.dart' as link;
import './measure_progressbar.dart' as progressbar;
import './measure_sash.dart' as sash;
import './measure_scale.dart' as scale;
import './measure_slider.dart' as slider;
import './measure_table.dart' as table;
import './measure_tableitem.dart' as tableitem;
import './measure_text.dart' as text;
import './measure_theme_scales.dart' as theme_scales;
import './measure_tree.dart' as tree;
import './measure_treeitem.dart' as treeitem;

void main() {
  // Canvas has no TextStyle to measure -- it reads ColorScheme directly and writes its Java file
  // immediately, independent of the WidgetMeasurer/MeasurementApp cases below.
  canvas.writeCanvasThemeFile();

  // The theme -> type-scale mapping is declared, not measured: it reads kNamedThemes and writes
  // its Java file immediately, so the lookup stays in step with the themes even if a measurement
  // run is interrupted.
  theme_scales.writeThemeScalesFile();

  final measurer = WidgetMeasurer();

  button.setupCases(measurer);
  ccombo.setupCases(measurer);
  clabel.setupCases(measurer);
  combo.setupCases(measurer);
  label.setupCases(measurer);
  link.setupCases(measurer);
  progressbar.setupCases(measurer);
  sash.setupCases(measurer);
  scale.setupCases(measurer);
  slider.setupCases(measurer);
  table.setupCases(measurer);
  tableitem.setupCases(measurer);
  text.setupCases(measurer);
  tree.setupCases(measurer);
  treeitem.setupCases(measurer);

  print('Generated ${measurer.testCases.length} total test cases');
  // Run single app with all test cases
  runApp(MeasurementApp(measurer: measurer));
}

List<ThemeConfig> getThemes() => [
  ThemeConfig('NonDefault', () => createLightNonDefaultTheme(null)),
  ThemeConfig('Default', () => createLightDefaultTheme(null)),
  ThemeConfig(
    'Compact',
    () => createLightNonDefaultTheme(
      null,
      overrideTypeSizes: kTypeScales['Compact']!.apply,
    ),
  ),
];
