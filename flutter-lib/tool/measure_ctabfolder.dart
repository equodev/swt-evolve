import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import './measure.dart';

const double _width = 600;
const double _height = 400;

void main() {
  final measurer = WidgetMeasurer();
  setupCases(measurer);
  runApp(MeasurementApp(measurer: measurer));
}

void setupCases(WidgetMeasurer measurer) {
  final styles = [
    ('TOP', SWT.TOP),
    ('TOP|CLOSE', SWT.TOP | SWT.CLOSE),
    ('TOP|FLAT', SWT.TOP | SWT.FLAT),
    ('TOP|BORDER', SWT.TOP | SWT.BORDER),
    ('TOP|SINGLE', SWT.TOP | SWT.SINGLE),
    ('TOP|MULTI', SWT.TOP | SWT.MULTI),
    ('BOTTOM', SWT.BOTTOM),
    ('BOTTOM|CLOSE', SWT.BOTTOM | SWT.CLOSE),
    ('BOTTOM|FLAT', SWT.BOTTOM | SWT.FLAT),
    ('BOTTOM|BORDER', SWT.BOTTOM | SWT.BORDER),
    ('BOTTOM|SINGLE', SWT.BOTTOM | SWT.SINGLE),
    ('BOTTOM|MULTI', SWT.BOTTOM | SWT.MULTI),
  ];

  for (final style in styles) {
    measurer.addTestCase(createCase(style));
  }

  print('Generated ${measurer.testCases.length} CTabFolder test cases');
}

MeasurementCase createCase((String, int) style) {
  return MeasurementCase(
    descr: '',
    style: style.$1,
    fqn: 'org.eclipse.swt.custom.CTabFolder',
    // The stack of pages is the area a page is laid out in: the render side of the client
    // area Java reports. Found through the widget that builds it, so the folder needs no
    // marker of its own.
    expectedComponents: const {'pageOf': 'IndexedStack'},
    widgetBuilder: (key) => SizedBox(
      key: key,
      width: _width,
      height: _height,
      child: CTabFolderSwt<VCTabFolder>(value: createVCTabFolder(style)),
    ),
  );
}

// Ids come from the style, which is distinct per case: the render side keys a widget's
// State by id, so sharing one would measure the first case's layout every time.
VCTabFolder createVCTabFolder((String, int) style) {
  final page = VComposite()
    ..id = 20000 + style.$2
    ..style = SWT.NONE
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = _width.toInt()
      ..height = _height.toInt())
    ..children = [];
  return VCTabFolder()
    ..id = 10000 + style.$2
    ..style = style.$2
    ..tabPosition = (style.$2 & SWT.BOTTOM) != 0 ? SWT.BOTTOM : SWT.TOP
    ..selection = 0
    ..items = [
      VCTabItem()
        ..id = 30000 + style.$2
        ..text = 'Section',
    ]
    ..children = [page];
}
