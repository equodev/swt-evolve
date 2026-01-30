import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/scheduler.dart';
import '../impl/composite_evolve.dart';
import '../styles.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/table.dart';
import '../gen/tableitem.dart';
import 'tableitem_evolve.dart';
import '../gen/tablecolumn.dart';
import '../gen/font.dart';
import 'utils/widget_utils.dart';
import 'utils/font_utils.dart';
import '../theme/theme_extensions/table_theme_extension.dart';
import '../theme/theme_settings/table_theme_settings.dart';

class TableImpl<T extends TableSwt, V extends VTable>
    extends CompositeImpl<T, V> {
  int _selectedRowIndex = -1;
  int? _editingRowIndex;
  int? _editingColumnIndex;
  TextEditingController? _editingController;
  FocusNode? _editingFocusNode;
  VoidCallback? _editingFocusListener;

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TableThemeExtension>()!;
    return super.wrap(buildTable(context, widgetTheme));
  }

  @override
  void dispose() {
    // Clean up editing resources
    if (_editingFocusNode != null) {
      _editingFocusNode!.removeListener(_editingFocusListener ?? () {});
      _editingFocusNode!.dispose();
      _editingFocusNode = null;
    }
    _editingController?.dispose();
    _editingController = null;
    _editingFocusListener = null;
    super.dispose();
  }

  Widget buildTable(BuildContext context, TableThemeExtension theme) {
    final columns = getColumns();
    final items = getItems();
    final showHeader = state.headerVisible ?? false;
    final showLines = state.linesVisible ?? false;
    final columnWidths = calculateColumnWidths(context, columns, theme);

    return Container(
      decoration: buildBorder(theme),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          if (showHeader && columns.isNotEmpty)
            buildHeader(context, columns, showLines, theme, columnWidths),
          Expanded(
            child: buildBody(context, items, columns, showLines, theme, columnWidths),
          ),
        ],
      ),
    );
  }

  Widget buildHeader(
    BuildContext context,
    List<VTableColumn> columns,
    bool showLines,
    TableThemeExtension theme,
    Map<int, TableColumnWidth>? columnWidths,
  ) {
    final backgroundColor = getTableHeaderBackgroundColor(state, theme);
    final textColor = getTableHeaderTextColor(state, theme);
    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: theme.headerTextStyle,
    );
    final totalWidth = calculateTotalWidth(columns, columnWidths);

    return Container(
      color: backgroundColor,
      child: Align(
        alignment: Alignment.centerLeft,
        child: totalWidth != null
            ? SizedBox(
                width: totalWidth,
                child: _buildHeaderTable(columns, columnWidths, showLines, textStyle, theme),
              )
            : IntrinsicWidth(
                child: _buildHeaderTable(columns, columnWidths, showLines, textStyle, theme),
              ),
      ),
    );
  }

  Widget _buildHeaderTable(
    List<VTableColumn> columns,
    Map<int, TableColumnWidth>? columnWidths,
    bool showLines,
    TextStyle textStyle,
    TableThemeExtension theme,
  ) {
    return Table(
      columnWidths: columnWidths,
      border: buildHeaderBorder(showLines, theme),
      children: <TableRow>[
        TableRow(
          children: columns.asMap().entries.map((entry) => 
            buildHeaderCell(entry.value, textStyle, theme, entry.key)
          ).toList(),
        ),
      ],
    );
  }

  Widget buildHeaderCell(
    VTableColumn column,
    TextStyle textStyle,
    TableThemeExtension theme,
    int columnIndex,
  ) {
    final hasCheckStyle = hasStyle(SWT.CHECK);
    final showCheckboxSpace = hasCheckStyle && columnIndex == 0;
    final headerHeight = calculateHeaderHeight(textStyle, theme);

    return SizedBox(
      height: headerHeight,
      child: Container(
        padding: theme.headerPadding,
        alignment: Alignment.centerLeft,
        child: Row(
          children: [
            if (showCheckboxSpace)
              SizedBox(width: 20.0 + theme.cellPadding.left),
            Expanded(
              child: Text(
                column.text ?? "",
                style: textStyle,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget buildBody(
    BuildContext context,
    List<VTableItem> items,
    List<VTableColumn> columns,
    bool showLines,
    TableThemeExtension theme,
    Map<int, TableColumnWidth>? columnWidths,
  ) {
    final backgroundColor = getTableBackgroundColor(state, theme);
    final rowTextStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: theme.rowTextColor,
      baseTextStyle: theme.rowTextStyle,
    );
    final height = calculateHeight(items.length, theme, context, rowTextStyle);

    return Container(
      color: backgroundColor,
      height: height,
      child: items.isEmpty
          ? Container()
          : SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: SingleChildScrollView(
                child: Table(
                  columnWidths: columnWidths,
                  border: buildBodyBorder(showLines, theme),
                  children: items
                      .asMap()
                      .entries
                      .map((entry) => buildRow(
                            context,
                            entry.key,
                            entry.value,
                            columns.length,
                            theme,
                          ))
                      .toList(),
                ),
              ),
            ),
    );
  }

  TableRow buildRow(
    BuildContext context,
    int rowIndex,
    VTableItem item,
    int columnCount,
    TableThemeExtension theme,
  ) {
    final isSelected = rowIndex == _selectedRowIndex || isItemSelected(item);
    final backgroundColor = getTableRowBackgroundColor(
      item,
      theme,
      isSelected,
      false,
      state.enabled ?? false,
      rowIndex % 2 == 1,
    );

    return TableRow(
      decoration: BoxDecoration(
        color: backgroundColor,
        border: buildRowBorder(isSelected, theme),
      ),
      children: TableItemSwtWrapper(
        item: item,
        rowIndex: rowIndex,
        tableImpl: this,
        parentTable: widget,
        parentTableValue: state,
        tableFont: state.font,
        editingRowIndex: _editingRowIndex,
        editingColumnIndex: _editingColumnIndex,
        editingController: _editingController,
        editingFocusNode: _editingFocusNode,
      ).buildCells(context, theme),
    );
  }

  void handleRowTap(int index, VTableItem item) {
    if (state.enabled != true) return;
    setState(() {
      if (_selectedRowIndex == index) {
        _selectedRowIndex = -1;
        state.selection?.clear();
      } else {
        _selectedRowIndex = index;
        state.selection ??= [];
        state.selection!.clear();
        state.selection!.add(_selectedRowIndex);
      }

      final event = VEvent()..segments = state.selection;
      widget.sendSelectionSelection(state, event);
    });
  }

  void handleRowDoubleTap(int index, VTableItem item) {
    if (state.enabled != true) return;
    setState(() {
      _selectedRowIndex = index;
      state.selection ??= [];
      state.selection!.clear();
      state.selection!.add(_selectedRowIndex);

      final event = VEvent()..segments = state.selection;
      widget.sendSelectionDefaultSelection(state, event);
    });
  }

  void startEditing(int rowIndex, int columnIndex, String initialText) {
    // Only allow editing if the table is editable
    if (state.editable != true) {
      return;
    }
    
    // Clean up any existing editing state first
    if (_editingFocusNode != null) {
      _editingFocusNode!.removeListener(_editingFocusListener!);
      _editingFocusNode!.dispose();
    }
    if (_editingController != null) {
      _editingController!.dispose();
    }
    
    // Create new editing state
    _editingController = TextEditingController(text: initialText);
    _editingFocusNode = FocusNode();
    
    // Create and add listener (outside setState to avoid concurrent modification)
    _editingFocusListener = () {
      if (_editingFocusNode != null && !_editingFocusNode!.hasFocus) {
        // Use post frame callback to avoid concurrent modification
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) {
            finishEditing();
          }
        });
      }
    };
    _editingFocusNode!.addListener(_editingFocusListener!);
    
    setState(() {
      _editingRowIndex = rowIndex;
      _editingColumnIndex = columnIndex;
    });
    
    // Request focus after the frame is built
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _editingFocusNode?.requestFocus();
    });
  }

  void finishEditing() {
    final rowIndex = _editingRowIndex;
    final columnIndex = _editingColumnIndex;
    final controller = _editingController;
    final focusNode = _editingFocusNode;
    final listener = _editingFocusListener;
    
    // Remove listener before disposing
    if (focusNode != null && listener != null) {
      focusNode.removeListener(listener);
    }
    
    if (rowIndex != null && columnIndex != null && controller != null) {
      final newText = controller.text;
      
      final event = VEvent()
        ..text = newText
        ..index = rowIndex
        ..start = columnIndex;
      
      widget.sendModifyModify(widget.value, event);
    }
    
    setState(() {
      controller?.dispose();
      focusNode?.dispose();
      _editingRowIndex = null;
      _editingColumnIndex = null;
      _editingController = null;
      _editingFocusNode = null;
      _editingFocusListener = null;
    });
  }

  double? calculateTotalWidth(List<VTableColumn> columns, Map<int, TableColumnWidth>? columnWidths) {
    if (columnWidths == null) return null;
    
    double totalWidth = 0.0;
    for (int i = 0; i < columns.length; i++) {
      final width = columnWidths[i];
      if (width is FixedColumnWidth) {
        totalWidth += width.value;
      } else {
        return null;
      }
    }
    return totalWidth;
  }

  double calculateHeaderHeight(TextStyle textStyle, TableThemeExtension theme) {
    final textPainter = TextPainter(
      text: TextSpan(text: 'Ag', style: textStyle),
      textDirection: TextDirection.ltr,
      maxLines: 1,
    );
    textPainter.layout();
    return textPainter.height + theme.headerPadding.vertical;
  }

  Border buildRowBorder(bool isSelected, TableThemeExtension theme) {
    final bottomBorder = isSelected
        ? BorderSide(
            color: theme.rowSelectedBorderColor,
            width: theme.rowSelectedBorderWidth,
          )
        : BorderSide(
            color: theme.rowSeparatorColor,
            width: theme.linesWidth,
          );

    final selectedBorder = isSelected
        ? BorderSide(
            color: theme.rowSelectedBorderColor,
            width: theme.rowSelectedBorderWidth,
          )
        : BorderSide.none;

    return Border(
      bottom: bottomBorder,
      top: selectedBorder,
      left: selectedBorder,
      right: selectedBorder,
    );
  }

  BoxDecoration buildBorder(TableThemeExtension theme) {
    return BoxDecoration(
      border: Border.all(
        color: theme.borderColor,
        width: theme.borderWidth,
      ),
    );
  }

  TableBorder buildHeaderBorder(bool showLines, TableThemeExtension theme) {
    return TableBorder(
      bottom: BorderSide(
        color: theme.headerBorderColor,
        width: theme.headerBorderWidth,
      ),
      verticalInside: showLines
          ? BorderSide(
              color: theme.linesColor,
              width: theme.linesWidth,
            )
          : BorderSide.none,
      top: BorderSide.none,
      left: BorderSide.none,
      right: BorderSide.none,
    );
  }

  TableBorder buildBodyBorder(bool showLines, TableThemeExtension theme) {
    return TableBorder(
      horizontalInside: BorderSide.none,
      verticalInside: showLines
          ? BorderSide(
              color: theme.linesColor,
              width: theme.linesWidth,
            )
          : BorderSide.none,
      top: BorderSide.none,
      bottom: BorderSide.none,
      left: BorderSide.none,
      right: BorderSide.none,
    );
  }

  double? calculateHeight(int rowCount, TableThemeExtension theme, BuildContext? context, TextStyle? rowTextStyle) {
    if (state.bounds?.height != null) {
      return state.bounds!.height.toDouble();
    }
    if (context == null || rowTextStyle == null) {
      return null;
    }
    final rowHeight = calculateRowHeight(rowTextStyle, theme);
    return rowCount * rowHeight;
  }

  double calculateRowHeight(TextStyle textStyle, TableThemeExtension theme) {
    final textPainter = TextPainter(
      text: TextSpan(text: 'Ag', style: textStyle),
      textDirection: TextDirection.ltr,
      maxLines: 1,
    );
    textPainter.layout();
    return textPainter.height + theme.cellPadding.vertical;
  }

  Map<int, TableColumnWidth>? calculateColumnWidths(
    BuildContext context,
    List<VTableColumn> columns,
    TableThemeExtension theme,
  ) {
    if (columns.isEmpty) {
      return null;
    }

    final hasExplicitWidths = columns.any((col) => col.width != null);
    final hasCheckStyle = hasStyle(SWT.CHECK);

    if (hasExplicitWidths) {
      return _calculateExplicitColumnWidths(columns, theme, hasCheckStyle);
    } else {
      return _calculateIntrinsicColumnWidths(context, columns, theme, hasCheckStyle);
    }
  }

  Map<int, TableColumnWidth> _calculateExplicitColumnWidths(
    List<VTableColumn> columns,
    TableThemeExtension theme,
    bool hasCheckStyle,
  ) {
    final Map<int, TableColumnWidth> widths = {};
    const double checkboxWidth = 20.0;

    for (int i = 0; i < columns.length; i++) {
      final column = columns[i];
      if (column.width != null) {
        double width = column.width!.toDouble();
        if (hasCheckStyle && i == 0) {
          width += checkboxWidth + theme.cellPadding.left;
        }
        widths[i] = FixedColumnWidth(width);
      } else {
        widths[i] = const IntrinsicColumnWidth();
      }
    }
    return widths;
  }

  Map<int, TableColumnWidth> _calculateIntrinsicColumnWidths(
    BuildContext context,
    List<VTableColumn> columns,
    TableThemeExtension theme,
    bool hasCheckStyle,
  ) {
    final Map<int, TableColumnWidth> widths = {};
    final headerTextStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: getTableHeaderTextColor(state, theme),
      baseTextStyle: theme.headerTextStyle,
    );
    final rowTextStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: theme.rowTextColor,
      baseTextStyle: theme.rowTextStyle,
    );
    final items = getItems();
    const double checkboxWidth = 20.0;

    for (int i = 0; i < columns.length; i++) {
      final column = columns[i];
      double maxWidth = _calculateHeaderWidth(column, headerTextStyle, theme);

      for (final item in items) {
        final cellWidth = _calculateCellWidth(
          context,
          item,
          i,
          rowTextStyle,
          theme,
          hasCheckStyle,
          checkboxWidth,
        );
        if (cellWidth > maxWidth) {
          maxWidth = cellWidth;
        }
      }

      if (hasCheckStyle && i == 0 && maxWidth < checkboxWidth + theme.cellPadding.left) {
        maxWidth = checkboxWidth + theme.cellPadding.left;
      }

      widths[i] = FixedColumnWidth(maxWidth > 0 ? maxWidth : 50.0);
    }
    return widths;
  }

  double _calculateHeaderWidth(VTableColumn column, TextStyle headerTextStyle, TableThemeExtension theme) {
    final headerText = column.text ?? "";
    if (headerText.isEmpty) return 0.0;

    final headerPainter = TextPainter(
      text: TextSpan(text: headerText, style: headerTextStyle),
      textDirection: TextDirection.ltr,
    );
    headerPainter.layout();
    return headerPainter.width + theme.headerPadding.horizontal;
  }

  double _calculateCellWidth(
    BuildContext context,
    VTableItem item,
    int columnIndex,
    TextStyle rowTextStyle,
    TableThemeExtension theme,
    bool hasCheckStyle,
    double checkboxWidth,
  ) {
    final cellTexts = item.texts ?? [];
    if (columnIndex >= cellTexts.length) return 0.0;

    final cellText = cellTexts[columnIndex] ?? "";
    if (cellText.isEmpty) return 0.0;

    final cellFont = item.font ?? state.font;
    final cellTextStyle = getTextStyle(
      context: context,
      font: cellFont,
      textColor: getTableRowTextColor(item, theme, false, true),
      baseTextStyle: rowTextStyle,
    );
    final cellPainter = TextPainter(
      text: TextSpan(text: cellText, style: cellTextStyle),
      textDirection: TextDirection.ltr,
    );
    cellPainter.layout();
    
    double cellWidth = cellPainter.width + theme.cellPadding.horizontal;
    if (hasCheckStyle && columnIndex == 0) {
      cellWidth += checkboxWidth + theme.cellPadding.left;
    }
    return cellWidth;
  }

  List<VTableColumn> getColumns() {
    return state.columns ?? [];
  }

  List<VTableItem> getItems() {
    return state.items ?? [];
  }

  bool hasStyle(int style) {
    return StyleBits(state.style).has(style);
  }

  bool isItemSelected(VTableItem item) {
    return state.selection?.contains(getItems().indexOf(item)) ?? false;
  }

  int findItemIndex(Object itemId) {
    final items = getItems();
    for (int i = 0; i < items.length; i++) {
      if (items[i].id == itemId) {
        return i;
      }
    }
    return -1;
  }
}

class TableItemSwtWrapper {
  final VTableItem item;
  final int rowIndex;
  final TableImpl tableImpl;
  final TableSwt parentTable;
  final VTable parentTableValue;
  final VFont? tableFont;
  final int? editingRowIndex;
  final int? editingColumnIndex;
  final TextEditingController? editingController;
  final FocusNode? editingFocusNode;

  TableItemSwtWrapper({
    required this.item,
    required this.rowIndex,
    required this.tableImpl,
    required this.parentTable,
    required this.parentTableValue,
    this.tableFont,
    this.editingRowIndex,
    this.editingColumnIndex,
    this.editingController,
    this.editingFocusNode,
  });

  List<Widget> buildCells(BuildContext context, TableThemeExtension theme) {
    final tableItemImpl = TableItemImpl<TableItemSwt, VTableItem>();
    tableItemImpl.state = item;
    tableItemImpl.setContext(TableItemContext(
      rowIndex: rowIndex,
      parentTable: parentTable,
      parentTableValue: parentTableValue,
      tableImpl: tableImpl,
      tableFont: tableFont,
      editingRowIndex: editingRowIndex,
      editingColumnIndex: editingColumnIndex,
      editingController: editingController,
      editingFocusNode: editingFocusNode,
    ));
    return tableItemImpl.buildCells(context, theme);
  }
}

class TableItemContext {
  final int rowIndex;
  final TableSwt parentTable;
  final VTable parentTableValue;
  final TableImpl? tableImpl;
  final VFont? tableFont;
  final int? editingRowIndex;
  final int? editingColumnIndex;
  final TextEditingController? editingController;
  final FocusNode? editingFocusNode;

  TableItemContext({
    required this.rowIndex,
    required this.parentTable,
    required this.parentTableValue,
    this.tableImpl,
    this.tableFont,
    this.editingRowIndex,
    this.editingColumnIndex,
    this.editingController,
    this.editingFocusNode,
  });

  static TableItemContext? of(BuildContext context) {
    final provider =
        context.dependOnInheritedWidgetOfExactType<TableItemContextProvider>();
    return provider?.context;
  }
}

class TableItemContextProvider extends InheritedWidget {
  final TableItemContext context;

  TableItemContextProvider({
    Key? key,
    required int rowIndex,
    required TableSwt parentTable,
    required VTable parentTableValue,
    TableImpl? tableImpl,
    VFont? tableFont,
    required Widget child,
  })  : context = TableItemContext(
          rowIndex: rowIndex,
          parentTable: parentTable,
          parentTableValue: parentTableValue,
          tableImpl: tableImpl,
          tableFont: tableFont,
        ),
        super(key: key, child: child);

  @override
  bool updateShouldNotify(TableItemContextProvider oldWidget) {
    return context.rowIndex != oldWidget.context.rowIndex ||
        context.parentTableValue.selection != oldWidget.context.parentTableValue.selection;
  }
}
