import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/label.dart';
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
    ('HORIZONTAL|SEPARATOR', SWT.HORIZONTAL | SWT.SEPARATOR),
    ('VERTICAL', SWT.VERTICAL),
    ('VERTICAL|SEPARATOR', SWT.VERTICAL | SWT.SEPARATOR),
  ];

  for (final style in styles) {
    for (final text in Texts) {
      for (final fontSize in Fonts) {
        for (final image in Images) {
          final caseName =
              '${style.$1.toLowerCase()}_${text.$1}_font${fontSize}_${(image != null) ? '${image.$1}x${image.$2}' : 'noimg'}';
          measurer.addTestCase(
            createCase(
              caseName,
              style,
              text.$2,
              fontSize: fontSize,
              image: image,
              useFontTheme: false,
            ),
          );
        }
      }
    }

    measurer.addThemeCase(
      createCase(
        '${style.$1.toLowerCase()}_theme',
        style,
        "Sample",
        useFontTheme: true,
      ),
    );
  }

  print('Generated ${measurer.testCases.length} Label test cases');
}

MeasurementCase createCase(
  String caseName,
  (String, int) style,
  String text, {
  int? fontSize,
  (int, int)? image,
  bool useFontTheme = false,
}) {
  final (value, expectedComponents) = createVLabel(
    style,
    text,
    fontSize: fontSize,
    image: image,
  );
  return MeasurementCase(
    descr: caseName,
    useFontTheme: useFontTheme,
    fqn: 'org.eclipse.swt.widgets.Label',
    expectedComponents: expectedComponents,
    widgetBuilder: (key) {
      getConfigFlags().use_swt_fonts = !useFontTheme;
      return LabelSwt(key: key, value: value);
    },
  );
}

(VLabel, Map<String, dynamic>) createVLabel(
  (String, int) style,
  String text, {
  int? fontSize,
  (int, int)? image,
}) {
  final (w, h) = image ?? (0, 0);
  final value = VLabel.empty()
    ..style = style.$2
    ..text = text
    ..font = fontSize != null
        ? (VFont.empty()..fontData = [VFontData.empty()..height = fontSize])
        : null
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
