import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/swt/composite.dart';
import 'package:swtflutter/src/widgets.dart';
import '../swt/gridlayout.dart';

import '../swt/widget.dart';
import '../swt/control.dart';
import '../swt/rectangle.dart';
import '../intrinsic_size_builder.dart';
import '../comm/comm.dart';

class GridLayoutImpl extends GridLayoutSwt {
  CustomMultiChildLayout? last;

  late final int leaves;

  GridLayoutImpl(
      {super.key, required super.value, required super.children, required super.composite}) {
    leaves = children.where((el) => el is! CompositeValue).length;
  }
  final List<ControlValue> sizes = [];

  @override
  Widget build(BuildContext context) {
    return Table(
      // defaultColumnWidth: const FlexColumnWidth(),
      defaultColumnWidth: const IntrinsicColumnWidth(),
      children: _buildTableRows(children, value.numColumns)
    );
  }

  List<TableRow> _buildTableRows(List<WidgetValue> childrenValues, int numColumns) {
    List<TableRow> rows = [];
    int totalCells = childrenValues.length;
    int currentCell = 0;

    while (currentCell < totalCells) {
      List<Widget> cells = [];

      for (int i = 0; i < numColumns; i++) {
        if (currentCell < totalCells) {
          final childValue = childrenValues[currentCell];
          cells.add(mapWidgetFromValue(childValue));
          currentCell++;
        } else {
          cells.add(Container()); // Empty container for remaining cells
        }
      }

      rows.add(TableRow(children: cells));
    }

    return rows;
  }

}
