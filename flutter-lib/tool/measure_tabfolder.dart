import 'dart:io';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
import 'package:swtflutter/src/impl/tabfolder_evolve.dart';
import './measure.dart';

const double _width = 600;
const double _height = 400;

/// The label every case carries, so the style it is drawn in is measured from a real one.
const String _label = 'Section';

/// What a tab can hold besides its label. What each one adds to a tab's width is the
/// difference between its case and the plain one, which is what Java adds to a label it has
/// measured itself.
const List<String> _tabShapes = ['plain', 'image'];

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
  final styles = [('TOP', SWT.TOP), ('BOTTOM', SWT.BOTTOM)];

  for (final style in styles) {
    measurer.addTestCase(createCase(style));
    measurer.addThemeCase(createCase(style));
  }
  // Held on one style: what a tab holds changes the tab, not the chrome around the page, and
  // the chrome is what every style has to agree on.
  for (final shape in _tabShapes) {
    measurer.addTestCase(createTabCase(shape));
    measurer.addThemeCase(createTabCase(shape));
  }

  print('Generated ${measurer.testCases.length} TabFolder test cases');
}

MeasurementCase createCase((String, int) style) {
  return MeasurementCase(
    descr: '',
    style: style.$1,
    fqn: 'org.eclipse.swt.widgets.TabFolder',
    // The page stack is Java's client area; the tab and label give Java the surround and font.
    expectedComponents: const {
      'pageOf': 'IndexedStack',
      'tabsOf': TabFolderImpl.tabRow,
      'text': _label,
    },
    widgetBuilder: (key) => SizedBox(
      key: key,
      width: _width,
      height: _height,
      child: TabFolderSwt<VTabFolder>(value: createVTabFolder(style)),
    ),
  );
}

/// One tab shape, on a fixed style. The `tab:` prefix marks these as being about the tab
/// rather than the chrome, which is what the page cases measure.
MeasurementCase createTabCase(String shape) {
  return MeasurementCase(
    descr: '',
    style: 'tab:$shape',
    fqn: 'org.eclipse.swt.widgets.TabFolder',
    expectedComponents: {
      'tabsOf': TabFolderImpl.tabRow,
      'text': _label,
      if (shape == 'image') 'image': _tabImage,
    },
    widgetBuilder: (key) => SizedBox(
      key: key,
      width: _width,
      height: _height,
      child: TabFolderSwt<VTabFolder>(value: createTabVTabFolder(shape)),
    ),
  );
}

VTabFolder createTabVTabFolder(String shape) {
  final id = 40000 + _tabShapes.indexOf(shape);
  final (imageWidth, imageHeight) = _tabImage;
  final folder = VTabFolder()
    ..id = id
    ..style = SWT.TOP
    ..bounds = _folderBounds()
    ..items = [
      VTabItem()
        ..id = id + 1000
        ..text = _label
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
  folder.selection = [folder.items!.first];
  return folder;
}

/// The folder fills the case, as it does at runtime: without bounds it wraps its tabs and
/// page, and the page it is measured by would be the size of what it holds.
VRectangle _folderBounds() => VRectangle()
  ..x = 0
  ..y = 0
  ..width = _width.toInt()
  ..height = _height.toInt();

// Ids come from the style, which is distinct per case: the render side keys a widget's
// State by id, so sharing one would measure the first case's layout every time.
VTabFolder createVTabFolder((String, int) style) {
  final page = VComposite()
    ..id = 20000 + style.$2
    ..style = SWT.NONE
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = _width.toInt()
      ..height = _height.toInt())
    ..children = [];
  final folder = VTabFolder()
    ..id = 10000 + style.$2
    ..style = style.$2
    ..bounds = _folderBounds()
    ..items = [
      VTabItem()
        ..id = 30000 + style.$2
        ..text = _label,
    ]
    ..children = [page];
  folder.selection = [folder.items!.first];
  return folder;
}
