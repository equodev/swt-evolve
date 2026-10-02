import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/coolbar.dart';
import 'package:swtflutter/src/gen/coolitem.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/swt.dart';
import './measure.dart';

/// The one item every case carries. Its width is what the row it sits in comes out as.
const (int, int) _itemSize = (100, 24);

void main() {
  final measurer = WidgetMeasurer();
  setupCases(measurer);
  runApp(MeasurementApp(measurer: measurer));
}

void setupCases(WidgetMeasurer measurer) {
  final styles = [
    ('HORIZONTAL', SWT.HORIZONTAL),
    ('HORIZONTAL|FLAT', SWT.HORIZONTAL | SWT.FLAT),
    ('VERTICAL', SWT.VERTICAL),
    ('VERTICAL|FLAT', SWT.VERTICAL | SWT.FLAT),
  ];

  for (final style in styles) {
    measurer.addTestCase(createCase(style));
    measurer.addThemeCase(createCase(style));
  }

  print('Generated ${measurer.testCases.length} CoolBar test cases');
}

MeasurementCase createCase((String, int) style) {
  return MeasurementCase(
    descr: '',
    style: style.$1,
    fqn: 'org.eclipse.swt.widgets.CoolBar',
    // The row the items are laid out in: everything between it and the bar's own edge is
    // the frame Java has to reserve.
    expectedComponents: const {'pageOf': '_SimpleCoolBarContent'},
    widgetBuilder: (key) =>
        CoolBarSwt<VCoolBar>(key: key, value: createVCoolBar(style)),
  );
}

// No bounds, so the bar wraps its row instead of filling the window, and the row is the
// item alone. Ids come from the style: the render side keys a State by id.
VCoolBar createVCoolBar((String, int) style) {
  final (width, height) = _itemSize;
  return VCoolBar()
    ..id = 10000 + style.$2
    ..style = style.$2
    ..items = [
      VCoolItem()
        ..id = 20000 + style.$2
        ..preferredSize = (VPoint()
          ..x = width
          ..y = height),
    ]
    ..children = [];
}
