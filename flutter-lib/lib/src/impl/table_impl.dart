import 'package:fluent_ui/fluent_ui.dart' as fluent;
import 'package:flutter/material.dart';
import 'package:swtflutter/src/swt/tablecolumn.dart';
import 'package:swtflutter/src/swt/widget.dart';

import '../impl/composite_impl.dart';
import '../styles.dart';
import '../swt/swt.dart';
import '../swt/table.dart';
import '../swt/tableitem.dart';

class TableImpl<T extends TableSwt, V extends TableValue>
    extends CompositeImpl<T, V> {
  late List<DataRow> items;
  late List<DataColumn> columns;
  final double cellHeight = 25;

  @override
  Widget build(BuildContext context) {
    columns = getColumns(state.children);
    items = getRows(state.children);
    final basicTable = createTable();
    SingleChildScrollView tableScrollable =
    SingleChildScrollView(scrollDirection: Axis.vertical, child: basicTable);
    final table = wrappedTable(hasStyle(SWT.NO_SCROLL) ? basicTable : tableScrollable);
    return super.wrap(table);
  }

  Widget wrappedTable(Widget table) {
    return SizedBox(
      height: state.bounds?.height.toDouble() ?? items.length * cellHeight,
      width: state.bounds?.width.toDouble() ?? columns.length * 50,
      child: table,
    );
  }

  List<DataColumn> getColumns(List<WidgetValue>? children) {
    List<DataColumn> fallback = [const DataColumn(label: Text(""))];
    if (children == null) return fallback;

    List<DataColumn> columns = children
        .whereType<TableColumnValue>()
        .map((column) => DataColumn(label: Text(column.text ?? "")))
        .toList();

    return columns.isEmpty ? fallback : columns;
  }

  List<String?> getTexts(TableItemValue item) {
    return item.texts?.isNotEmpty == true ? item.texts! : [item.text ?? ""];
  }

  List<DataRow> getRows(List<WidgetValue>? children) {
    if (children == null) return [];

    return children
        .whereType<TableItemValue>()
        .map((item) => addRow(item, getTexts(item)))
        .toList();
  }

  bool hasStyle(int style) {
    return StyleBits(state.style).has(style);
  }

  bool isSelected(TableItemValue item) {
    return state.selection?.contains(item.id) ?? false;
  }

  DataRow addRow(TableItemValue row, List<String?> elements) {
    return hasStyle(SWT.CHECK)
        ? DataRow(
            selected: isSelected(row),
            onSelectChanged: (selected) {
              if (selected == true) {
                widget.sendSelectionSelection(state, row.id);
              } else {
                widget.sendDeselectionDeselection(state, row.id);
              }
            },
            cells: getCells(elements, row))
        : DataRow(
            selected: isSelected(row),
            cells: getCells(elements, row)
          );
  }

  getCells(List<String?> textsByColumns, TableItemValue row) {
    return textsByColumns
        .map((text) => DataCell(
                Text(
                  text ?? "",
                  style: fluent.FluentTheme.of(context).typography.body,
                ),
                onTap: () {
                  if (isSelected(row)) {
                    widget.sendDeselectionDeselection(state, row.id);
                  } else {
                    widget.sendSelectionSelection(state, row.id);
                  }
                })
        )
        .toList();
  }

  Widget createTable() {
    return DataTable(
        headingRowHeight: mustHide(),
        dataRowMinHeight: cellHeight,
        dataRowMaxHeight: cellHeight,
        columns: columns,
        rows: items,
        showCheckboxColumn: hasStyle(SWT.CHECK),
    );
  }

  double mustHide() {
    bool headerVisible = state.headerVisible ?? false;
    return headerVisible ? cellHeight : 0;
  }
}
