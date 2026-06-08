import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
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
        for (final image in Images) {
          final caseName =
              '${text.$1}_font${fontSize}_${(image != null) ? '${image.$1}x${image.$2}' : 'noimg'}';
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
      createCase('theme', style, "Sample", useFontTheme: true),
    );
  }

  print('Generated ${measurer.testCases.length} TableItem test cases');
}

MeasurementCase createCase(
  String caseName,
  (String, int) style,
  String text, {
  int? fontSize,
  (int, int)? image,
  bool useFontTheme = false,
}) {
  final (value, expectedComponents) = createVTableItem(
    style,
    text,
    fontSize: fontSize,
    image: image,
  );
  return MeasurementCase(
    descr: caseName,
    style: style.$1,
    useFontTheme: useFontTheme,
    fqn: 'org.eclipse.swt.widgets.TableItem',
    expectedComponents: expectedComponents,
    widgetBuilder: (key) {
      getConfigFlags().use_swt_fonts = !useFontTheme;
      return TableItemSwt(key: key, value: value);
    },
  );
}

(VTableItem, Map<String, dynamic>) createVTableItem(
  (String, int) style,
  String text, {
  int? fontSize,
  (int, int)? image,
}) {
  final (w, h) = image ?? (0, 0);
  final value = VTableItem.empty()
    ..style = style.$2
    ..texts = [text]
    ..font = fontSize != null
        ? (VFont.empty()..fontData = [VFontData.empty()..height = fontSize])
        : null
    ..images = image != null
        ? [
            (VImage.empty()
              ..filename =
                  '../swt_native/src/test/resources/images/${w}x${h}.png'
              ..imageData = (VImageData.empty()
                ..width = w
                ..height = h)),
          ]
        : null;

  final expectedComponents = <String, dynamic>{'text': text, 'image': image};
  return (value, expectedComponents);
}
