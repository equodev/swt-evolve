import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
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
  print('Generated ${measurer.testCases.length} Table header test cases');
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
