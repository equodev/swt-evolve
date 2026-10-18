import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
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
    ('CHECK', SWT.CHECK),
    ('PUSH', SWT.PUSH),
    ('RADIO', SWT.RADIO),
    ('SEPARATOR', SWT.SEPARATOR),
    ('DROP_DOWN', SWT.DROP_DOWN),
  ];

  for (final style in styles) {
    for (final text in Texts) {
      for (final image in Images) {
        final caseName =
            '${text.$1}_${(image != null) ? '${image.$1}x${image.$2}' : 'noimg'}';
        measurer.addTestCase(
          createCase(
            caseName,
            style,
            text.$2,
            image: image,
            useFontTheme: false,
          ),
        );
      }
    }

    measurer.addThemeCase(
      createCase('theme', style, "Sample", useFontTheme: true),
    );
  }

  print('Generated ${measurer.testCases.length} ToolItem test cases');
}

MeasurementCase createCase(
  String caseName,
  (String, int) style,
  String text, {
  (int, int)? image,
  bool useFontTheme = false,
}) {
  final (value, expectedComponents) = createVToolItem(
    style,
    text,
    image: image,
  );
  return MeasurementCase(
    descr: caseName,
    style: style.$1,
    useFontTheme: useFontTheme,
    fqn: 'org.eclipse.swt.widgets.ToolItem',
    expectedComponents: expectedComponents,
    fontFromParent: true,
    widgetBuilder: (key) {
      getConfigFlags().use_swt_fonts = !useFontTheme;
      return ToolItemSwt(key: key, value: value);
    },
  );
}

(VToolItem, Map<String, dynamic>) createVToolItem(
  (String, int) style,
  String text, {
  (int, int)? image,
}) {
  final (w, h) = image ?? (0, 0);
  final value = VToolItem.empty()
    ..style = style.$2
    ..text = text
    ..image = image != null
        ? (VImage.empty()
            ..filename = '../swt_native/src/test/resources/images/${w}x${h}.png'
            ..imageData = (VImageData.empty()
              ..width = w
              ..height = h))
        : null;

  final expectedComponents = <String, dynamic>{'text': text, 'image': image};
  return (value, expectedComponents);
}
