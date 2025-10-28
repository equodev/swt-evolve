import 'package:flutter/material.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/swt/tablecolumn.dart';
import '../impl/composite_evolve.dart';
import '../impl/color_utils.dart';
import '../styles.dart';
import '../gen/event.dart';
import '../gen/table.dart';
import '../gen/tableitem.dart';
import '../gen/tablecolumn.dart';
import 'utils/font_utils.dart';

class TableImpl<T extends TableSwt, V extends VTable>
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
    List<VTableColumn> columns = getTableColumns();
    List<VTableItem> items = getTableItems();
    bool headerVisible = state.headerVisible ?? false;
    bool linesVisible = state.linesVisible ?? false;

    Color defaultBackgroundColor =
        useDarkTheme ? const Color(0xFF1D1D1D) : Colors.white;
    Color defaultRowTextColor = useDarkTheme ? Colors.white : Colors.black;
    Color borderColor =
        useDarkTheme ? const Color(0xFF333333) : Colors.grey.shade300;
    Color headerBackgroundColor =
        useDarkTheme ? const Color(0xFF1E1E1E) : Colors.white;
    Color headerTextColor = useDarkTheme ? Colors.white : Colors.black;
    Color alternateRowColor =
        useDarkTheme ? const Color(0xFF121212) : const Color(0xFFF5F5F5);

    // Get text color from Table foreground or use default
    final finalHeaderTextColor =
        colorFromVColor(state.foreground, defaultColor: headerTextColor);

    // Create TextStyle from Table VFont
    final headerTextStyle = FontUtils.textStyleFromVFont(
      state.font,
      context,
      color: finalHeaderTextColor,
    );

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
                  verticalInside: linesVisible
                      ? BorderSide(color: borderColor, width: 1.0)
                      : BorderSide.none,
                  top: BorderSide.none,
                  left: BorderSide.none,
                  right: BorderSide.none,
                ),
                children: <TableRow>[
                  TableRow(
                    children: columns
                        .map((column) => Container(
                              height: 22,
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 8.0, vertical: 0.0),
                              alignment: Alignment.centerLeft,
                              child: Text(
                                column.text ?? "",
                                style: headerTextStyle,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ))
                        .toList(),
                  ),
                ],
              ),
            ),
          Container(
            color: defaultBackgroundColor,
            height: calculateTableHeight(items.length),
            child: items.isEmpty
                ? Container()
                : SingleChildScrollView(
                    child: Table(
                      border: TableBorder(
                        horizontalInside: BorderSide.none,
                        verticalInside: linesVisible
                            ? BorderSide(color: borderColor, width: 1.0)
                            : BorderSide.none,
                        top: BorderSide.none,
                        bottom: BorderSide.none,
                        left: BorderSide.none,
                        right: BorderSide.none,
                      ),
                      children: items
                          .map((item) => _buildTableRow(
                                items.indexOf(item),
                                item,
                                defaultBackgroundColor,
                                alternateRowColor,
                                defaultRowTextColor,
                                columns.length,
                              ))
                          .toList(),
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

  TableRow _buildTableRow(
    int index,
    VTableItem item,
    Color defaultBackgroundColor,
    Color alternateRowColor,
    Color defaultTextColor,
    int columnCount,
  ) {
    if (columnCount == 0) {
      columnCount = 1;
    }
    List<String?> cellTexts = item.texts ?? [];
    bool isSelected = index == _selectedRowIndex || isItemSelected(item);

    Color rowBackgroundColor = colorFromVColor(
      item.background,
      defaultColor: index % 2 == 1 ? alternateRowColor : defaultBackgroundColor,
    );

    Color rowTextColor =
        colorFromVColor(item.foreground, defaultColor: defaultTextColor);

    // Create TextStyle from item font or use table font as fallback
    final rowTextStyle = FontUtils.textStyleFromVFont(
      item.font ?? state.font,
      context,
      color: rowTextColor,
    );

    List<Widget> rowCells = [];

    for (int i = 0; i < columnCount; i++) {
      rowCells.add(GestureDetector(
        onTap: () => _handleRowTap(index, item),
        onDoubleTap: () => _handleRowDoubleTap(index, item),
        child: Container(
          height: 22,
          padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 0.0),
          alignment: Alignment.centerLeft,
          child: Text(
            i < cellTexts.length ? (cellTexts[i] ?? "") : "",
            style: rowTextStyle,
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ));
    }

    return TableRow(
      decoration: BoxDecoration(
        color: isSelected ? alternateRowColor : rowBackgroundColor,
      ),
      children: rowCells,
    );
  }

  void _handleRowTap(int index, VTableItem item) {
    setState(() {
      int previousSelectedIndex = _selectedRowIndex;
      Object? previousSelectedId = previousSelectedIndex >= 0 &&
              previousSelectedIndex < getTableItems().length
          ? getTableItems()[previousSelectedIndex].id
          : null;

      if (_selectedRowIndex == index) {
        _selectedRowIndex = -1;
        if (state.selection != null) {
          state.selection!.clear();
        }
      } else {
        _selectedRowIndex = index;

        if (state.selection == null) {
          state.selection = [];
        } else {
          state.selection!.clear();
        }
        state.selection!.add(_selectedRowIndex);
      }

      var e = VEvent()..segments = state.selection;
      widget.sendSelectionSelection(state, e);
    });
  }

  void _handleRowDoubleTap(int index, VTableItem item) {
    setState(() {
      int previousSelectedIndex = _selectedRowIndex;
      Object? previousSelectedId = previousSelectedIndex >= 0 &&
              previousSelectedIndex < getTableItems().length
          ? getTableItems()[previousSelectedIndex].id
          : null;

      _selectedRowIndex = index;

      if (state.selection == null) {
        state.selection = [];
      } else {
        state.selection!.clear();
      }
      state.selection!.add(_selectedRowIndex);

      var e = VEvent()..segments = state.selection;
      widget.sendSelectionDefaultSelection(state, e);
    });
  }

  List<VTableColumn> getTableColumns() {
    return state.columns ?? [];
  }

  List<VTableItem> getTableItems() {
    return state.items ?? [];
  }

  bool hasStyle(int style) {
    return StyleBits(state.style).has(style);
  }

  bool isItemSelected(VTableItem item) {
    return state.selection?.contains(getTableItems().indexOf(item)) ?? false;
  }
}
