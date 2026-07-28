import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
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
  print('Generated ${measurer.testCases.length} Tree header test cases');
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
