import 'package:flutter/material.dart';
import 'package:swtflutter/src/swt/tablecolumn.dart';
import '../impl/composite_impl.dart';
import '../styles.dart';
import '../swt/swt.dart';
import '../swt/table.dart';
import '../swt/tableitem.dart';

class TableImpl<T extends TableSwt, V extends TableValue>
    extends CompositeImpl<T, V> {
  int _selectedRowIndex = -1;
  final double cellHeight = 25;

  @override
  Widget build(BuildContext context) {
    return super.wrap(
      createTableView(),
    );
  }

  Widget createTableView() {
    List<TableColumnValue> columns = getTableColumns();
    List<TableItemValue> items = getTableItems();
    bool headerVisible = state.headerVisible ?? true;
    bool linesVisible = state.linesVisible ?? true;


    if (columns.isEmpty) {
      columns = [
        TableColumnValue()..text = " ",
        TableColumnValue()..text = "C",
        TableColumnValue()..text = "!",
        TableColumnValue()..text = "Description",
        TableColumnValue()..text = "Resource",
        TableColumnValue()..text = "In Folder",
        TableColumnValue()..text = "Location",
      ];
    }

    return Container(
      decoration: BoxDecoration(
        border: hasStyle(SWT.BORDER) ? Border.all(color: Colors.grey.shade300) : null,
        color: Colors.white,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [

          if (headerVisible)
            Container(
              padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 8),
              decoration: BoxDecoration(
                color: Colors.grey.shade200,
                border: Border(
                  bottom: BorderSide(
                    color: Colors.grey.shade300,
                    width: linesVisible ? 1.0 : 0.0,
                  ),
                ),
              ),
              child: Row(
                children: [
                  for (int i = 0; i < columns.length; i++)
                    Expanded(
                      flex: getFlexForColumn(i, columns.length),
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 4.0),
                        child: Text(
                          columns[i].text ?? "",
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ),
                ],
              ),
            ),

          Container(
            height: calculateTableHeight(items.length),
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min, // No intenta expandirse
                children: [
                  for (int i = 0; i < items.length; i++)
                    _buildTableRow(i, items[i], columns, linesVisible),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }


  double calculateTableHeight(int rowCount) {

    if (state.bounds?.height != null) {
      return state.bounds!.height.toDouble();
    }


    int visibleRows = rowCount > 10 ? 10 : rowCount;
    return visibleRows * cellHeight + (visibleRows > 0 ? 2 : 0); // +2 para los bordes
  }


  Widget _buildTableRow(int index, TableItemValue item, List<TableColumnValue> columns, bool linesVisible) {
    List<String?> cellTexts = item.texts ?? [item.text ?? ""];
    bool isSelected = index == _selectedRowIndex || isItemSelected(item);

    return InkWell(
      onTap: () {
        setState(() {
          _selectedRowIndex = index;

          if (isItemSelected(item)) {
            widget.sendDeselectionDeselection(state, item.id);
          } else {
            widget.sendSelectionSelection(state, item.id);
          }
        });
      },
      child: Container(
        height: cellHeight,
        decoration: BoxDecoration(
          color: isSelected ? Colors.blue.withOpacity(0.1) : Colors.transparent,
          border: linesVisible ? Border(
            bottom: BorderSide(color: Colors.grey.shade300, width: 0.5),
          ) : null,
        ),
        child: Row(
          children: [
            for (int i = 0; i < columns.length; i++)
              Expanded(
                flex: getFlexForColumn(i, columns.length),
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
                  child: Text(
                    i == 0 ?
                    (item.text ?? "") :
                    (i - 1 < cellTexts.length ? (cellTexts[i - 1] ?? "") : ""),
                    style: TextStyle(
                      fontSize: 12,
                      color: isSelected ? Colors.blue : Colors.black,
                    ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }


  int getFlexForColumn(int columnIndex, int totalColumns) {
    if (totalColumns <= 1) return 1;


    if (totalColumns == 7) {
      switch (columnIndex) {
        case 0: return 1;  // " " columna pequeña
        case 1: return 1;  // "C" columna pequeña
        case 2: return 1;  // "!" columna pequeña
        case 3: return 4;  // "Description" columna grande
        case 4: return 3;  // "Resource" columna mediana
        case 5: return 2;  // "In Folder" columna mediana
        case 6: return 4;  // "Location" columna grande
        default: return 2;
      }
    } else {
      return columnIndex == 0 ? 1 : 2;
    }
  }

  List<TableColumnValue> getTableColumns() {
    if (state.children == null) return [];

    return state.children!
        .whereType<TableColumnValue>()
        .toList();
  }

  List<TableItemValue> getTableItems() {
    if (state.children == null) return [];

    return state.children!
        .whereType<TableItemValue>()
        .toList();
  }

  bool hasStyle(int style) {
    return StyleBits(state.style).has(style);
  }

  bool isItemSelected(TableItemValue item) {
    return state.selection?.contains(item.id) ?? false;
  }

}