import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
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
  print('Generated ${measurer.testCases.length} Table test cases');
}

MeasurementCase createCase(bool headerVisible, int columnCount) {
  final value = createVTable(headerVisible, columnCount);
  return MeasurementCase(
    descr: 'header_visible${headerVisible}_cols$columnCount',
    style: 'NONE',
    fqn: 'org.eclipse.swt.widgets.Table',
    expectedComponents: const {
      'named': ['header'],
    },
    widgetBuilder: (key) => TableSwt(key: key, value: value),
  );
}

MeasurementCase createGeometryCase(
  int width,
  int columnCount, {
  int? fontSize,
}) {
  final value = createVTable(true, columnCount)
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
    fqn: 'org.eclipse.swt.widgets.Table',
    expectedComponents: const {
      'named': ['header'],
      'rowsOf': 'TableItemSwt',
    },
    widgetBuilder: (key) {
      // The widget's own font is only honoured with use_swt_fonts on; without it the
      // theme's style wins and the font probe would measure the same height twice.
      getConfigFlags().use_swt_fonts = fontSize != null;
      return SizedBox(
        key: key,
        width: width.toDouble(),
        height: 400,
        child: TableSwt(value: value),
      );
    },
  );
}

List<VTableItem> createGeometryItems(int columnCount) {
  final cells = columnCount == 0 ? 1 : columnCount;
  return List.generate(
    2,
    (row) => VTableItem.empty()
      ..id = 10 + row
      ..text = 'R$row'
      ..texts = List.generate(cells, (i) => 'R${row}$i'),
  );
}

VTable createVTable(bool headerVisible, int columnCount) {
  return VTable.empty()
    ..style = 0
    ..headerVisible = headerVisible
    ..columns = List.generate(
      columnCount,
      (i) => VTableColumn.empty()
        ..id = i + 1
        ..text = 'Col$i',
    );
}
