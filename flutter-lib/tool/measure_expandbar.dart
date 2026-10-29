import 'dart:io';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/expandbar.dart';
import 'package:swtflutter/src/gen/expanditem.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/swt.dart';
import './measure.dart';

const double _width = 600;
const double _height = 400;

/// The label every case carries, so the style it is drawn in is measured from a real one.
const String _label = 'Section';

/// The image the image case carries, with its bytes, so the header draws a real one.
const (int, int) _headerImage = (16, 16);

const String _headerImagePath =
    '../swt_native/src/test/resources/images/16x16.png';

void main() {
  final measurer = WidgetMeasurer();
  setupCases(measurer);
  runApp(MeasurementApp(measurer: measurer));
}

void setupCases(WidgetMeasurer measurer) {
  final styles = [('V_SCROLL', SWT.V_SCROLL)];

  for (final style in styles) {
    measurer.addTestCase(createCase(style.$1, style.$2, image: false));
  }
  // Held on one style: an image changes the header, not what each style draws around it.
  measurer.addTestCase(
    createCase('header:image', styles.first.$2, image: true),
  );

  print('Generated ${measurer.testCases.length} ExpandBar test cases');
}

/// A `header:` style marks a case that is about what the header holds rather than the bar's
/// style, which is what the other cases measure.
MeasurementCase createCase(String name, int style, {required bool image}) {
  return MeasurementCase(
    descr: '',
    style: name,
    fqn: 'org.eclipse.swt.widgets.ExpandBar',
    expectedComponents: {
      'headersOf': 'ExpandItemHeader',
      'text': _label,
      if (image) 'image': _headerImage,
    },
    widgetBuilder: (key) => SizedBox(
      key: key,
      width: _width,
      height: _height,
      child: ExpandBarSwt<VExpandBar>(value: createVExpandBar(style, image)),
    ),
  );
}

// Ids are distinct per case: the render side keys a widget's State by id, so sharing one
// would measure the first case's layout every time.
VExpandBar createVExpandBar(int style, bool image) {
  final id = (image ? 50000 : 10000) + style;
  final (imageWidth, imageHeight) = _headerImage;
  return VExpandBar()
    ..id = id
    ..style = style
    ..items = [
      VExpandItem()
        ..id = id + 10000
        ..text = _label
        ..image = image
            ? (VImage.empty()
                ..filename = _headerImagePath
                ..imageData = (VImageData.empty()
                  ..width = imageWidth
                  ..height = imageHeight
                  ..data = File(_headerImagePath).readAsBytesSync()))
            : null,
    ]
    ..children = [];
}
