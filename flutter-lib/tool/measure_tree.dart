import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import './measure.dart';
import './measure_data.dart';

void main() {
  final measurer = WidgetMeasurer();
  setupCases(measurer);
  runApp(MeasurementApp(measurer: measurer));
}

void setupCases(WidgetMeasurer measurer) {
  const columnCounts = [0, 1, 3];
  for (final headerVisible in [false, true]) {
    for (final columnCount in columnCounts) {
      measurer.addTestCase(createCase(headerVisible, columnCount));
    }
  }
  for (final columnCount in [0, 3]) {
    for (final width in geometryWidths) {
      measurer.addTestCase(createGeometryCase(width, columnCount));
    }
    measurer.addTestCase(
      createGeometryCase(
        geometryWidths.first,
        columnCount,
        fontSize: fontProbeSize,
      ),
    );
  }
  print('Generated ${measurer.testCases.length} Tree test cases');
}

MeasurementCase createCase(bool headerVisible, int columnCount) {
  final value = createVTree(headerVisible, columnCount);
  return MeasurementCase(
    descr: 'header_visible${headerVisible}_cols$columnCount',
    style: 'NONE',
    fqn: 'org.eclipse.swt.widgets.Tree',
    expectedComponents: const {
      'named': ['header'],
    },
    widgetBuilder: (key) => TreeSwt(key: key, value: value),
  );
}

MeasurementCase createGeometryCase(
  int width,
  int columnCount, {
  int? fontSize,
}) {
  final value = createVTree(true, columnCount)
    ..font = fontSize == null
        ? null
        : (VFont.empty()..fontData = [VFontData.empty()..height = fontSize])
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = width
      ..height = 400)
    ..items = createGeometryItems(columnCount);
  return MeasurementCase(
    descr: fontSize == null
        ? 'geometry_width${width}_cols$columnCount'
        : 'fontprobe_cols$columnCount',
    style: 'NONE',
    fqn: 'org.eclipse.swt.widgets.Tree',
    expectedComponents: const {
      'named': ['header'],
      'rowsOf': 'TreeItemSwt',
    },
    widgetBuilder: (key) {
      // The widget's own font is only honoured with use_swt_fonts on; without it the
      // theme's style wins and the font probe would measure the same height twice.
      getConfigFlags().use_swt_fonts = fontSize != null;
      return SizedBox(
        key: key,
        width: width.toDouble(),
        height: 400,
        child: TreeSwt(value: value),
      );
    },
  );
}

List<VTreeItem> createGeometryItems(int columnCount) {
  final cells = columnCount == 0 ? 1 : columnCount;
  return [
    VTreeItem.empty()
      ..id = 10
      ..text = 'R0'
      ..texts = List.generate(cells, (i) => 'R0$i')
      ..expanded = true
      ..items = [
        VTreeItem.empty()
          ..id = 20
          ..text = 'R1'
          ..texts = List.generate(cells, (i) => 'R1$i')
          ..expanded = false
          ..items = [
            VTreeItem.empty()
              ..id = 30
              ..text = 'R2'
              ..texts = List.generate(cells, (i) => 'R2$i'),
          ],
      ],
  ];
}

VTree createVTree(bool headerVisible, int columnCount) {
  return VTree.empty()
    ..style = 0
    ..headerVisible = headerVisible
    ..columns = List.generate(
      columnCount,
      (i) => VTreeColumn.empty()
        ..id = i + 1
        ..text = 'Col$i',
    );
}
