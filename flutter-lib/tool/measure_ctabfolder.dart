import 'dart:io';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import './measure.dart';

const double _width = 600;
const double _height = 400;

/// The label every case carries, so the style it is drawn in is measured from a real one.
const String _label = 'Section';

/// What a tab can hold besides its label. What each one adds to a tab's width is the
/// difference between its case and the plain one, which is what Java adds to a label it has
/// measured itself.
const List<String> _tabShapes = ['plain', 'image', 'close'];

/// The image the image shape carries, with its bytes: the render side builds one from
/// `imageData.data` and draws nothing at all without it, which would make the difference a
/// measurement of this harness rather than of the tab.
const (int, int) _tabImage = (16, 16);

const String _tabImagePath =
    '../swt_native/src/test/resources/images/16x16.png';

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
  // Held on one style: what a tab holds changes the tab, not the chrome around the page, and
  // the chrome is what every style has to agree on.
  for (final shape in _tabShapes) {
    measurer.addTestCase(createTabCase(shape));
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
    //
    // The tabs and their label come along because Java sizes a tab itself, to answer which
    // one a point is over: the label's style is the font to measure it in, and the tab
    // around it is what to add.
    expectedComponents: const {
      'pageOf': 'IndexedStack',
      'tabsOf': 'TabDragRow',
      'text': _label,
    },
    widgetBuilder: (key) => SizedBox(
      key: key,
      width: _width,
      height: _height,
      child: CTabFolderSwt<VCTabFolder>(value: createVCTabFolder(style)),
    ),
  );
}

/// One tab shape, on a fixed style. The `tab:` prefix marks these as being about the tab
/// rather than the chrome, which is what the page cases measure.
MeasurementCase createTabCase(String shape) {
  return MeasurementCase(
    descr: '',
    style: 'tab:$shape',
    fqn: 'org.eclipse.swt.custom.CTabFolder',
    expectedComponents: {
      'tabsOf': 'TabDragRow',
      'text': _label,
      if (shape == 'image') 'image': _tabImage,
    },
    widgetBuilder: (key) => SizedBox(
      key: key,
      width: _width,
      height: _height,
      child: CTabFolderSwt<VCTabFolder>(value: createTabVCTabFolder(shape)),
    ),
  );
}

VCTabFolder createTabVCTabFolder(String shape) {
  final id = 40000 + _tabShapes.indexOf(shape);
  final (imageWidth, imageHeight) = _tabImage;
  return VCTabFolder()
    ..id = id
    ..style = SWT.TOP
    ..tabPosition = SWT.TOP
    ..selection = 0
    ..items = [
      VCTabItem()
        ..id = id + 1000
        ..text = _label
        ..showClose = shape == 'close'
        ..image = shape == 'image'
            ? (VImage.empty()
                ..filename = _tabImagePath
                ..imageData = (VImageData.empty()
                  ..width = imageWidth
                  ..height = imageHeight
                  ..data = File(_tabImagePath).readAsBytesSync()))
            : null,
    ]
    ..children = [];
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
        ..text = _label,
    ]
    ..children = [page];
}
