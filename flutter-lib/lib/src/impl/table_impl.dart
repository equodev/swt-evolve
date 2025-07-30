import 'package:flutter/material.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/swt/tablecolumn.dart';
import '../impl/composite_impl.dart';
import '../styles.dart';
import '../swt/table.dart';
import '../swt/tableitem.dart';

class TableImpl<T extends TableSwt, V extends TableValue>
    extends CompositeImpl<T, V> {
  int _selectedRowIndex = -1;
  final bool useDarkTheme = getCurrentTheme();

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

    Color backgroundColor = useDarkTheme ? const Color(0xFF1D1D1D) : Colors.white;
    Color borderColor = useDarkTheme ? const Color(0xFF333333) : Colors.grey.shade300;
    Color headerBackgroundColor = useDarkTheme ? const Color(0xFF1E1E1E) : Colors.white;
    Color headerTextColor = useDarkTheme ? Colors.white : Colors.black;
    Color rowTextColor = useDarkTheme ? Colors.white : Colors.black;
    Color alternateRowColor = useDarkTheme ? const Color(0xFF121212) : const Color(0xFFF5F5F5);

    return Container(
      decoration: BoxDecoration(
        border: Border.all(color: borderColor, width: 2.0),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          if (headerVisible && columns.isNotEmpty)
            Container(
              color: headerBackgroundColor,
              child: Table(
                border: TableBorder(
                  bottom: BorderSide(color: borderColor, width: 2.0),
                  verticalInside: BorderSide.none,
                  top: BorderSide.none,
                  left: BorderSide.none,
                  right: BorderSide.none,
                ),
                columnWidths: generateColumnWidths(columns.length),
                children: <TableRow>[
                  TableRow(
                    children: columns.map((column) =>
                        Container(
                          height: 40,
                          padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 0.0),
                          alignment: Alignment.centerLeft,
                          child: Text(
                            column.text ?? "",
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 13,
                              color: headerTextColor,
                            ),
                            overflow: TextOverflow.ellipsis,
                          ),
                        )
                    ).toList(),
                  ),
                ],
              ),
            ),

          Container(
            color: backgroundColor,
            height: calculateTableHeight(items.length),
            child: items.isEmpty
                ? Container()
                : SingleChildScrollView(
              child: Table(
                border: TableBorder(
                  horizontalInside: BorderSide.none,
                  verticalInside: BorderSide(color: borderColor, width: 2.0),
                  top: BorderSide.none,
                  bottom: BorderSide.none,
                  left: BorderSide.none,
                  right: BorderSide.none,
                ),
                columnWidths: generateColumnWidths(columns.length),
                children: items.map((item) =>
                    _buildTableRow(
                      items.indexOf(item),
                      item,
                      backgroundColor,
                      alternateRowColor,
                      rowTextColor,
                      columns.length,
                    )
                ).toList(),
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

    return rowCount * 22.0;
  }

  Map<int, TableColumnWidth> generateColumnWidths(int columnCount) {
    Map<int, TableColumnWidth> widths = {};

    if (columnCount == 0) return widths;

    for (int i = 0; i < columnCount; i++) {
      if (columnCount == 4) {
        switch (i) {
          case 0: widths[i] = const FlexColumnWidth(3); break;
          case 1: widths[i] = const FlexColumnWidth(2); break;
          case 2: widths[i] = const FlexColumnWidth(2); break;
          case 3: widths[i] = const FlexColumnWidth(3); break;
        }
      }
      else {
        widths[i] = const FlexColumnWidth(1);
      }
    }

    return widths;
  }

  TableRow _buildTableRow(
      int index,
      TableItemValue item,
      Color backgroundColor,
      Color alternateRowColor,
      Color textColor,
      int columnCount,
      ) {
    List<String?> cellTexts = item.texts ?? [];
    bool isSelected = index == _selectedRowIndex || isItemSelected(item);

    Color rowColor = index % 2 == 1 ? alternateRowColor : backgroundColor;

    List<Widget> rowCells = [];

    rowCells.add(
        GestureDetector(
          onTap: () => _handleRowTap(index, item),
          onDoubleTap: () => _handleRowDoubleTap(index, item),
          child: Container(
            height: 22,
            padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 0.0),
            alignment: Alignment.centerLeft,
            child: Text(
              item.text ?? "",
              style: TextStyle(
                fontSize: 13,
                color: textColor,
              ),
              overflow: TextOverflow.ellipsis,
            ),
          ),
        )
    );

    for (int i = 0; i < columnCount - 1; i++) {
      rowCells.add(
          GestureDetector(
            onTap: () => _handleRowTap(index, item),
            onDoubleTap: () => _handleRowDoubleTap(index, item),
            child: Container(
              height: 22,
              padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 0.0),
              alignment: Alignment.centerLeft,
              child: Text(
                i < cellTexts.length ? (cellTexts[i] ?? "") : "",
                style: TextStyle(
                  fontSize: 13,
                  color: textColor,
                ),
                overflow: TextOverflow.ellipsis,
              ),
            ),
          )
      );
    }

    return TableRow(
      decoration: BoxDecoration(
        color: rowColor,
      ),
      children: rowCells,
    );
  }

  void _handleRowTap(int index, TableItemValue item) {
    setState(() {
      int previousSelectedIndex = _selectedRowIndex;
      Object? previousSelectedId = previousSelectedIndex >= 0 && previousSelectedIndex < getTableItems().length
          ? getTableItems()[previousSelectedIndex].id
          : null;

      if (_selectedRowIndex == index) {
        _selectedRowIndex = -1;
        if (state.selection != null) {
          state.selection!.clear();
        }
        widget.sendDeselectionDeselection(state, item.id);
      } else {
        if (previousSelectedId != null) {
          widget.sendDeselectionDeselection(state, previousSelectedId);
        }

        _selectedRowIndex = index;

        if (state.selection == null) {
          state.selection = [];
        } else {
          state.selection!.clear();
        }
        state.selection!.add(item.id);

        widget.sendSelectionSelection(state, item.id);
      }
    });
  }

  void _handleRowDoubleTap(int index, TableItemValue item) {
    setState(() {
      int previousSelectedIndex = _selectedRowIndex;
      Object? previousSelectedId = previousSelectedIndex >= 0 && previousSelectedIndex < getTableItems().length
          ? getTableItems()[previousSelectedIndex].id
          : null;

      if (previousSelectedId != null && previousSelectedId != item.id) {
        widget.sendDeselectionDeselection(state, previousSelectedId);
      }

      _selectedRowIndex = index;

      if (state.selection == null) {
        state.selection = [];
      } else {
        state.selection!.clear();
      }
      state.selection!.add(item.id);

      widget.sendSelectionDefaultSelection(state, item.id);
    });
  }

  List<TableColumnValue> getTableColumns() {
    if (state.children == null) return [];
    return state.children!.whereType<TableColumnValue>().toList();
  }

  List<TableItemValue> getTableItems() {
    if (state.children == null) return [];
    return state.children!.whereType<TableItemValue>().toList();
  }

  bool hasStyle(int style) {
    return StyleBits(state.style).has(style);
  }

  bool isItemSelected(TableItemValue item) {
    return state.selection?.contains(item.id) ?? false;
  }
}