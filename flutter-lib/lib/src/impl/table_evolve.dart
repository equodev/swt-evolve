import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/scheduler.dart';
import '../impl/composite_evolve.dart';
import '../comm/comm.dart';
import '../styles.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/table.dart';
import '../gen/tableitem.dart';
import 'tableitem_evolve.dart';
import '../gen/tablecolumn.dart';
import '../gen/tableeditor.dart';
import '../gen/droptarget.dart';
import '../gen/font.dart';
import 'utils/widget_utils.dart';
import 'utils/font_utils.dart';
import 'utils/image_utils.dart';
import 'utils/dnd_utils.dart';
import '../gen/image.dart';
import '../theme/theme_extensions/table_theme_extension.dart';
import '../theme/theme_settings/table_theme_settings.dart';
import '../gen/widgets.dart';
import '../gen/rectangle.dart';
import 'utils/double_tap_detector.dart';

/// Below this many rows a table is built whole, so short tables keep every row's semantics node.
const int _rowWindowFloor = 200;


class _RowWindow {
  const _RowWindow(this.start, this.end);

  final int start;
  final int end;
}

class TableImpl<T extends TableSwt, V extends VTable>
    extends CompositeImpl<T, V> {
  int _selectedRowIndex = -1;

  // Cached layout values updated each build, used by Flutter→Java listeners
  double? _cachedRowHeight;
  double? _cachedHeaderOffset;
  Map<int, TableColumnWidth>? _cachedColumnWidths;
  final List<String> _eventNames = [];
  final ScrollController _verticalScrollController = ScrollController();

  final DoubleTapDetector _rowTapDetector = DoubleTapDetector();

  int _requestedRowEnd = 0;

  /// Width the dragged column had when its resize started; the drag applies a
  /// delta to this instead of to the live width, so rounding can't accumulate.
  double? _resizeStartWidth;

  final Set<int> _userResizedColumns = {};

  int registerRowTap(int rowIndex) => _rowTapDetector.registerTap(key: rowIndex);

  // Each cell has its own GestureDetector that sends MouseDown; suppress both
  // the ControlImpl.wrap() Listener and wrapCompositeInteractionChrome to
  // avoid a second event with different coordinates.
  @override
  bool get forwardsControlMouseDown => false;

  @override
  bool get wrapsWholeWidgetForDnd => false;

  @override
  void initState() {
    super.initState();
    _verticalScrollController.addListener(() => setState(() {}));
    _registerGetIdFromPointListener();
    _registerGetItemBoundsListener();
    _registerGetItemHeightListener();
  }

  void _registerGetIdFromPointListener() {
    final eventName = "${state.swt}/${state.id}/GetIdFromPoint";
    _eventNames.add(eventName);
    EquoCommService.onRaw(eventName, (payload) {
      if (payload != null) {
        try {
          final Map<String, dynamic> point = Map<String, dynamic>.from(payload as Map);
          final x = (point['x'] as num?)?.toDouble() ?? 0.0;
          final y = (point['y'] as num?)?.toDouble() ?? 0.0;
          final itemId = findItemIdAtPosition(x, y);
          final responseEvent =
              "${state.swt}/${state.id}/GetIdFromPointResponse";
          EquoCommService.sendPayload(
            responseEvent,
            itemId?.toString() ?? "-1",
          );
        } catch (e) {
          print('Error processing GetIdFromPoint: $e');
          final responseEvent =
              "${state.swt}/${state.id}/GetIdFromPointResponse";
          EquoCommService.sendPayload(responseEvent, "-1");
        }
      }
    });
  }

  void _registerGetItemBoundsListener() {
    final eventName = "${state.swt}/${state.id}/GetItemBounds";
    _eventNames.add(eventName);
    EquoCommService.onRaw(eventName, (payload) {
      final responseEvent = "${state.swt}/${state.id}/GetItemBoundsResponse";
      if (payload == null) {
        EquoCommService.sendPayload(responseEvent, VRectangle().toJson());
        return;
      }
      try {
        String raw = payload.toString();
        if (raw.startsWith('"') && raw.endsWith('"')) {
          raw = raw.substring(1, raw.length - 1);
        }
        final parts = raw.split(",");
        final itemId = int.parse(parts[0]);
        final columnIndex = int.parse(parts[1]);
        final bounds = _calculateItemBounds(itemId, columnIndex);
        final rect = VRectangle()
          ..x = bounds.left.round()
          ..y = bounds.top.round()
          ..width = bounds.width.round()
          ..height = bounds.height.round();
        EquoCommService.sendPayload(responseEvent, rect.toJson());
      } catch (e) {
        print('Error processing GetItemBounds: $e');
        EquoCommService.sendPayload(responseEvent, VRectangle().toJson());
      }
    });
  }

  void _registerGetItemHeightListener() {
    final eventName = "${state.swt}/${state.id}/GetItemHeight";
    _eventNames.add(eventName);
    EquoCommService.onRaw(eventName, (payload) {
      final responseEvent = "${state.swt}/${state.id}/GetItemHeightResponse";
      EquoCommService.sendPayload(responseEvent, cachedRowHeight.round().toString());
    });
  }

  /// Returns the VTableItem id at the given Flutter coordinates, or null.
  Object? findItemIdAtPosition(double x, double y) {
    final rowHeight = _cachedRowHeight;
    if (rowHeight == null || rowHeight <= 0) return null;
    final headerOffset = _cachedHeaderOffset ?? 0.0;
    final adjustedY = y - headerOffset;
    if (adjustedY < 0) return null;
    final itemIndex = (adjustedY / rowHeight).floor();
    final items = getItems();
    if (itemIndex < 0 || itemIndex >= items.length) return null;
    return items[itemIndex].id;
  }

  /// Calculates the Flutter-coordinate bounds of a cell identified by its
  /// Java hashCode [itemId] and [columnIndex].
  Rect _calculateItemBounds(int itemId, int columnIndex) {
    final rowHeight = _cachedRowHeight ?? 20.0;
    final headerOffset = _cachedHeaderOffset ?? 0.0;
    final columnWidths = _cachedColumnWidths;

    final items = getItems();
    int itemIndex = -1;
    for (int i = 0; i < items.length; i++) {
      if (items[i].id == itemId) {
        itemIndex = i;
        break;
      }
    }
    if (itemIndex < 0) return Rect.zero;

    final y = headerOffset + (itemIndex * rowHeight);

    final columns = getColumns();
    double x = 0.0;
    double width = state.bounds?.width?.toDouble() ?? 200.0;

    if (columns.isNotEmpty && columnWidths != null) {
      for (int i = 0; i < columnIndex && i < columns.length; i++) {
        final w = columnWidths[i];
        if (w is FixedColumnWidth) x += w.value;
      }
      final colW = columnWidths[columnIndex];
      if (colW is FixedColumnWidth) width = colW.value;
    }

    return Rect.fromLTWH(x, y, width, rowHeight);
  }

  @override
  void didUpdateWidget(covariant T oldWidget) {
    final items = state.items;
    final columns = state.columns;
    final editors = state.editors;
    super.didUpdateWidget(oldWidget);
    // Use new data from Java if present; fall back to preserved ones only when
    // the serializer omitted the field (null) due to an unrelated dirty() flush.
    state.items = state.items ?? items;
    state.columns = state.columns ?? columns;
    state.editors = state.editors ?? editors;
  }

  @override
  void extraSetState() {
    super.extraSetState();
    final selection = state.selection;
    if (selection != null && selection.isNotEmpty) {
      _selectedRowIndex = selection.first;
    } else {
      _selectedRowIndex = -1;
    }
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TableThemeExtension>()!;
    final columns = getColumns();
    final items = getItems();
    final showHeader = state.headerVisible ?? false;
    final showLines = state.linesVisible ?? false;
    final columnWidths = calculateColumnWidths(context, columns, widgetTheme);

    _cachedColumnWidths = columnWidths;
    final rowTextStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: widgetTheme.rowTextColor,
      baseTextStyle: widgetTheme.rowTextStyle,
    );
    _cachedRowHeight = calculateRowHeight(rowTextStyle, widgetTheme);
    double headerOff = frameBorderWidth(widgetTheme);
    if (showHeader) {
      final headerTextStyle = getTextStyle(
        context: context,
        font: state.font,
        textColor: getTableHeaderTextColor(state, widgetTheme),
        baseTextStyle: widgetTheme.headerTextStyle,
      );
      headerOff += calculateHeaderHeight(headerTextStyle, widgetTheme) + widgetTheme.headerBorderWidth;
    }
    _cachedHeaderOffset = headerOff;

    final editorOverlays = _buildEditorOverlays(context, columns, widgetTheme, columnWidths);

    final totalRows = rowCount(items);
    final body = buildBody(context, items, columns, showLines, widgetTheme, columnWidths);
    final naturalBodyHeight = totalRows * _cachedRowHeight!;

    final tableContent = wrapTableForDrop(
      Stack(
        children: [
          Container(
            decoration: buildBorder(widgetTheme),
            child: LayoutBuilder(
              builder: (context, constraints) {
                final bounded = constraints.maxHeight.isFinite;
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  mainAxisSize: bounded ? MainAxisSize.max : MainAxisSize.min,
                  children: <Widget>[
                    if (showHeader)
                      buildHeader(context, columns, showLines, widgetTheme, columnWidths),
                    bounded
                        ? Expanded(child: body)
                        : SizedBox(height: naturalBodyHeight, child: body),
                  ],
                );
              },
            ),
          ),
        ],
      ),
      totalRows,
    );

    final wrappedTable = super.wrap(tableContent);

    return Stack(
      children: [
        wrappedTable,
        ...editorOverlays,
      ],
    );
  }

  @override
  void dispose() {
    _verticalScrollController.dispose();
    // Remove Flutter event listeners
    for (final eventName in _eventNames) {
      EquoCommService.remove(eventName);
    }
    super.dispose();
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
    final headerHeight = calculateHeaderHeight(textStyle, theme);
    return Semantics(
      identifier: 'sizeprobe:header',
      child: Container(
        color: backgroundColor,
        height: headerHeight,
        child: Stack(
          children: [
            _buildHeaderTable(columns, columnWidths, showLines, textStyle, theme),
            ..._buildColumnResizeHandles(
                columns, columnWidths, headerHeight, theme),
          ],
        ),
      ),
    );
  }

  /// Native SWT has no sash widget on a column boundary: the header itself
  /// switches to the resize cursor a few pixels either side of the boundary and
  /// drags it. These transparent strips reproduce that hit region, so they must
  /// straddle the boundary rather than sit inside one header cell -- a cell is
  /// clipped, and a grip inside it would only respond from its own side.
  List<Widget> _buildColumnResizeHandles(
    List<VTableColumn> columns,
    Map<int, TableColumnWidth>? columnWidths,
    double headerHeight,
    TableThemeExtension theme,
  ) {
    if (columns.isEmpty || columnWidths == null) return const [];
    final grip = theme.columnResizeHandleWidth * 2;
    final handles = <Widget>[];
    double boundary = 0.0;
    for (int i = 0; i < columns.length; i++) {
      final width = columnWidths[i];
      if (width is! FixedColumnWidth) return const [];
      boundary += width.value;
      // SWT's TableColumn.resizable defaults to true.
      if (columns[i].resizable == false) continue;
      handles.add(Positioned(
        left: boundary - grip / 2,
        top: 0,
        width: grip,
        height: headerHeight,
        child: MouseRegion(
          cursor: SystemMouseCursors.resizeColumn,
          child: GestureDetector(
            behavior: HitTestBehavior.opaque,
            onHorizontalDragStart: (_) =>
                _startColumnResize(columns[i], width.value),
            onHorizontalDragUpdate: (details) =>
                _updateColumnResize(columns[i], details.delta.dx),
            onHorizontalDragEnd: (_) => _resizeStartWidth = null,
            onHorizontalDragCancel: () => _resizeStartWidth = null,
          ),
        ),
      ));
    }
    return handles;
  }

  void _startColumnResize(VTableColumn column, double renderedWidth) {
    // A column with no width of its own is sized to content; the width the user
    // grabbed is that rendered width, and dragging it makes it explicit.
    _resizeStartWidth = (column.width ?? renderedWidth.round()).toDouble();
  }

  void _updateColumnResize(VTableColumn column, double deltaX) {
    final start = _resizeStartWidth;
    if (start == null) return;
    final next = (start + deltaX).clamp(0.0, double.infinity);
    _resizeStartWidth = next;
    if (next.round() == column.width) return;
    setState(() {
      _userResizedColumns.add(column.id);
      column.width = next.round();
    });
    // Native SWT resizes the column live and fires Resize on every change, and
    // JFace layouts relayout from it; a single event on drag end would leave
    // them stale for the whole drag.
    final e = VEvent()..width = column.width;
    TableColumnSwt<VTableColumn>(value: column).sendControlResize(column, e);
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
          children: [
            ...columns
                .asMap()
                .entries
                .map(
                  (entry) =>
                      buildHeaderCell(entry.value, textStyle, theme, entry.key),
                ),
            const SizedBox.shrink(),
          ],
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

    final double? columnWidth = _columnWidthAt(columnIndex);
    final EdgeInsets headerPadding = columnWidth == null
        ? theme.headerPadding
        : fitTableCellPadding(
            theme.headerPadding,
            available: columnWidth -
                (showCheckboxSpace ? 20.0 + theme.cellPadding.left : 0.0),
            contentWidth: measureTableText(column.text ?? "", textStyle),
          );

    Widget cell = SizedBox(
      height: headerHeight,
      child: Container(
        padding: headerPadding,
        alignment: Alignment.centerLeft,
        child: Row(
          children: [
            if (showCheckboxSpace)
              SizedBox(width: 20.0 + theme.cellPadding.left),
            if (column.image != null)
              FutureBuilder<Widget?>(
                future: ImageUtils.buildVImageAsync(
                  column.image!,
                  enabled: true,
                  useBinaryImage: true,
                  renderAsIcon: true,
                ),
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.done &&
                      snapshot.data != null) {
                    return snapshot.data!;
                  }
                  return const SizedBox.shrink();
                },
              ),
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
    return tagItemSemantics(column, GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () {
        TableColumnSwt<VTableColumn>(value: column).sendSelectionSelection(
          column,
          null,
        );
      },
      onSecondaryTapDown: (details) {
        openContextMenu(details.globalPosition);
        final e = VEvent();
        e.x = _computeHeaderCellCenterX(columnIndex);
        e.y = ((_cachedHeaderOffset ?? 20.0) / 2).round();
        widget.sendMenuDetectMenuDetect(state, e);
      },
      child: ClipRect(child: cell),
    ));
  }

  double? _columnWidthAt(int index) {
    final width = _cachedColumnWidths?[index];
    return width is FixedColumnWidth ? width.value : null;
  }

  int _computeHeaderCellCenterX(int columnIndex) {
    double x = 0.0;
    if (_cachedColumnWidths != null) {
      for (int i = 0; i < columnIndex; i++) {
        final w = _cachedColumnWidths![i];
        if (w is FixedColumnWidth) x += w.value;
      }
      final colW = _cachedColumnWidths![columnIndex];
      if (colW is FixedColumnWidth) x += colW.value / 2;
    } else {
      final columns = state.columns ?? [];
      for (int i = 0; i < columnIndex && i < columns.length; i++) {
        x += columns[i].width?.toDouble() ?? 100.0;
      }
      final colWidth = columnIndex < columns.length
          ? (columns[columnIndex].width?.toDouble() ?? 100.0)
          : 100.0;
      x += colWidth / 2;
    }
    return x.round();
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
    final totalRows = rowCount(items);
    final height = calculateHeight(totalRows, theme, context, rowTextStyle);
    final rowHeight = _cachedRowHeight ?? calculateRowHeight(rowTextStyle, theme);

    // A column-less table (SWT List-style single column) still gets a trailing
    // SizedBox.shrink() cell from buildRow, so its row has two Table columns.
    // Pin column 0 to flex and the trailing filler to zero width — otherwise the
    // unlisted filler falls back to Flutter's default FlexColumnWidth(1) and the
    // two equal-weight flex columns split the width 50/50, ellipsizing the text
    // at half the list width with an empty gap on the right.
    final trailingColumnWidths = columns.isEmpty
        ? const {0: FlexColumnWidth(), 1: FixedColumnWidth(0)}
        : columnWidths;

    return Container(
      color: backgroundColor,
      height: height,
      child: totalRows == 0
          ? Container()
          : LayoutBuilder(
              builder: (context, constraints) {
                final window = _rowWindow(constraints, rowHeight, totalRows);
                _requestRows(window.end);
                return SingleChildScrollView(
                  controller: _verticalScrollController,
                  child: Padding(
                    // The rows outside the window are height alone, so the scrollbar spans the table.
                    padding: EdgeInsets.only(
                      top: window.start * rowHeight,
                      bottom: (totalRows - window.end) * rowHeight,
                    ),
                    child: Table(
                      columnWidths: trailingColumnWidths,
                      border: buildBodyBorder(showLines, theme),
                      children: [
                        for (int i = window.start; i < window.end; i++)
                          i < items.length
                              ? buildRow(context, i, items[i], columns.length, theme,
                                  showLines: showLines)
                              : buildPendingRow(columns, rowHeight, showLines, theme),
                      ],
                    ),
                  ),
                );
              },
            ),
    );
  }

  /// Rows the table has: for a VIRTUAL table, more than the rows whose data has arrived. Falls back
  /// to the items when no count is sent.
  int rowCount(List<VTableItem> items) {
    final declared = state.itemCount ?? 0;
    return declared > items.length ? declared : items.length;
  }

  /// The row range to build; everything outside it costs height only.
  _RowWindow _rowWindow(BoxConstraints constraints, double rowHeight, int totalRows) {
    if (totalRows <= _rowWindowFloor || rowHeight <= 0) {
      return _RowWindow(0, totalRows);
    }
    final viewport =
        constraints.maxHeight.isFinite ? constraints.maxHeight : totalRows * rowHeight;
    final offset =
        _verticalScrollController.hasClients ? _verticalScrollController.offset : 0.0;
    final visibleRows = (viewport / rowHeight).ceil() + 1;
    // Half a screenful each side: enough that a small scroll shows no blank frame, and proportional
    // so the window stays inside SWT's budget of three times the visible rows of SetData.
    final overscan = visibleRows ~/ 2;
    final span = visibleRows + 2 * overscan;
    var start = (offset / rowHeight).floor() - overscan;
    if (start < 0) start = 0;
    var end = start + span;
    if (end > totalRows) end = totalRows;
    return _RowWindow(start, end);
  }

  /// A row whose SWT.SetData has not run yet. Its cell count must match the populated rows, or
  /// Flutter's Table asserts.
  TableRow buildPendingRow(
    List<VTableColumn> columns,
    double rowHeight,
    bool showLines,
    TableThemeExtension theme,
  ) {
    final cellCount = columns.isNotEmpty ? columns.length : 1;
    return TableRow(
      decoration: BoxDecoration(
        border: getTableRowBorder(false, theme, showLines: showLines),
      ),
      children: [
        for (int i = 0; i < cellCount; i++) SizedBox(height: rowHeight),
        const SizedBox.shrink(),
      ],
    );
  }

  /// Asks Java to run SWT.SetData up to [end]. Deferred past the frame: the answer is a state push,
  /// and sending mid-build would rebuild the tree being built.
  void _requestRows(int end) {
    if (end <= _requestedRowEnd) return;
    if (!StyleBits(state.style).has(SWT.VIRTUAL)) return;
    _requestedRowEnd = end;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      widget.sendEvent(state, "SetData/SetData", VEvent()..end = end);
    });
  }

  TableRow buildRow(
    BuildContext context,
    int rowIndex,
    VTableItem item,
    int columnCount,
    TableThemeExtension theme, {
    bool showLines = true,
  }) {
    final isSelected = rowIndex == _selectedRowIndex || isItemSelected(item);
    final backgroundColor = getTableRowBackgroundColor(
      item,
      theme,
      isSelected,
      false,
      state.enabled ?? false,
      rowIndex % 2 == 1,
    );

    final cells = TableItemSwtWrapper(
      item: item,
      rowIndex: rowIndex,
      tableImpl: this,
      parentTable: widget,
      parentTableValue: state,
      tableFont: state.font,
    ).buildCells(context, theme);

    return TableRow(
      decoration: BoxDecoration(
        color: backgroundColor,
        border: getTableRowBorder(isSelected, theme, showLines: showLines),
      ),
      children: [
        ...cells.map((cell) => wrapCellForRowDrag(cell, rowIndex, item)),
        const SizedBox.shrink(),
      ],
    );
  }

  final HoverTracker<int> _dragHover = HoverTracker<int>();

  int _rowIndexForLocalY(double y, int itemCount) {
    final rowHeight = _cachedRowHeight;
    if (rowHeight == null || rowHeight <= 0) return itemCount;
    final headerOffset = _cachedHeaderOffset ?? 0.0;
    final adjustedY = y - headerOffset;
    if (adjustedY < 0) return 0;
    return (adjustedY / rowHeight).floor().clamp(0, itemCount);
  }

  Widget wrapTableForDrop(Widget content, int itemCount) {
    int resolveIndex(DragTargetDetails<DndDragPayload> details) {
      final box = context.findRenderObject();
      final index = box is RenderBox
          ? _rowIndexForLocalY(box.globalToLocal(details.offset).dy, itemCount)
          : itemCount;
      _dragHover.update(index);
      return index;
    }

    return wrapDropTarget<DndDragPayload>(
      child: content,
      state: state,
      resolveIndex: resolveIndex,
      onDrop: (_, targetIndex, __, position) => sendRowDrop(targetIndex ?? itemCount, position),
      builder: (context, child, negotiation, isHovering) {
        if (!isHovering) _dragHover.update(null);
        return child;
      },
    );
  }

  Widget wrapCellForRowDrag(Widget cell, int rowIndex, VTableItem item) {
    final row = ValueListenableBuilder<int?>(
      valueListenable: _dragHover.notifier,
      builder: (context, hoveredIndex, child) => hoveredIndex == rowIndex
          ? DecoratedBox(
              decoration: const BoxDecoration(
                border: Border(top: BorderSide(color: Colors.blue, width: 2)),
              ),
              child: child,
            )
          : child!,
      child: cell,
    );
    return wrapDraggable<DndDragPayload>(
      child: row,
      data: DndDragPayload(sourceControlId: state.id, index: rowIndex),
      widget: widget,
      state: state,
      onDragStarted: () => handleRowTap(rowIndex, item),
      // A cell's content can include a flex Row (Expanded text/padding) — fine within the
      // table's own bounded layout, but the feedback overlay renders inside Flutter's Overlay,
      // which gives unbounded width. Without an explicit width here, that flex Row throws
      // "incoming width constraints are unbounded" as soon as a drag starts.
      feedbackBuilder: (c) => SizedBox(
        width: state.bounds?.width?.toDouble() ?? 200,
        child: Material(
          elevation: 2,
          child: Opacity(opacity: 0.85, child: cell),
        ),
      ),
      childWhenDraggingBuilder: (c) => Opacity(opacity: 0.3, child: cell),
    );
  }

  void sendRowDrop(int targetIndex, Offset position) {
    final dropTargetId = state.dropTargetId;
    if (dropTargetId == null) return;
    final dropTargetValue = VDropTarget()..id = dropTargetId;
    DropTargetSwt<VDropTarget>(value: dropTargetValue).sendDropdrop(
      dropTargetValue,
      VEvent()
        ..index = targetIndex
        ..x = position.dx.round()
        ..y = position.dy.round(),
    );
  }

  void handleRowTap(int index, VTableItem item) {
    if (state.enabled != true) return;
    setState(() {
      _selectedRowIndex = index;
      state.selection ??= [];
      state.selection!.clear();
      state.selection!.add(index);

      final event = VEvent()..segments = state.selection;
      widget.sendSelectionSelection(state, event);
    });
  }

  void commitEditorIfLeaving(int rowIndex) {
    final editors = state.editors;
    if (editors == null || editors.isEmpty) return;
    // A JFace inline cell editor (TableViewerEditor) lives on the selected row
    // and must commit when selection moves off it -- send Focus/FocusOut so the
    // edit is persisted. Only the editor on the row we are LEAVING is committed;
    // permanent editors on other rows (per-row checkboxes etc.) are left alone,
    // since in SWT they are not tied to selection. Java's editor list stays the
    // source of truth for which editors exist, so this does not remove any.
    final leavingRow = _selectedRowIndex;
    if (leavingRow < 0 || leavingRow == rowIndex) return;
    for (final e in editors) {
      final ed = e.editor;
      if (ed != null &&
          e.item != null &&
          findItemIndex(e.item!.id) == leavingRow) {
        EquoCommService.send("${ed.swt}/${ed.id}/Focus/FocusOut");
      }
    }
  }

  void selectRowLocally(int index) {
    if (state.enabled != true) return;
    setState(() {
      _selectedRowIndex = index;
      state.selection ??= [];
      state.selection!.clear();
      state.selection!.add(index);
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

  /// Builds editor overlay widgets for all active editors.
  List<Widget> _buildEditorOverlays(
    BuildContext context,
    List<VTableColumn> columns,
    TableThemeExtension theme,
    Map<int, TableColumnWidth>? columnWidths,
  ) {
    final editors = state.editors;
    if (editors == null || editors.isEmpty) return [];

    final rowTextStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: theme.rowTextColor,
      baseTextStyle: theme.rowTextStyle,
    );
    final rowHeight = calculateRowHeight(rowTextStyle, theme);
    final showLines = state.linesVisible ?? false;
    final lineWidth = showLines ? theme.linesWidth : 0.0;
    final borderWidth = frameBorderWidth(theme);

    double headerOffset = borderWidth;
    if (state.headerVisible == true) {
      final headerTextStyle = getTextStyle(
        context: context,
        font: state.font,
        textColor: getTableHeaderTextColor(state, theme),
        baseTextStyle: theme.headerTextStyle,
      );
      headerOffset += calculateHeaderHeight(headerTextStyle, theme) +
          theme.headerBorderWidth;
    }

    final List<Widget> overlays = [];
    for (final editable in editors) {
      if (editable.editor == null || editable.item == null) continue;

      final editingItemId = editable.item!.id;
      final columnIndex = editable.column ?? 0;

      final itemIndex = findItemIndex(editingItemId);
      if (itemIndex < 0) continue;
      // An SWT TableEditor control is visible as soon as setEditor(...) is
      // called and stays visible until the editor is disposed -- it does NOT
      // follow selection. A table can place a permanent control on every row
      // (e.g. a Button(SWT.CHECK) per row via TableEditor.setEditor(button,
      // item, col)), so every editor in the list must render regardless of
      // which row is selected. Java's editor list is the source of truth for
      // which editors exist.

      final scrollOffset = _verticalScrollController.hasClients
          ? _verticalScrollController.offset
          : 0.0;
      final editorY = headerOffset + (itemIndex * rowHeight) - scrollOffset;

      double editorX = borderWidth;
      double editorWidth = state.bounds?.width?.toDouble() ?? 200.0;

      if (columns.isNotEmpty && columnWidths != null) {
        for (int i = 0; i < columnIndex && i < columns.length; i++) {
          final w = columnWidths[i];
          if (w is FixedColumnWidth) {
            editorX += w.value;
          }
          editorX += lineWidth;
        }

        final colW = columnWidths[columnIndex];
        if (colW is FixedColumnWidth) {
          editorWidth = colW.value;
        }
      }

      final editorWidget = mapWidgetFromValue(editable.editor!);

      overlays.add(
        Positioned(
          left: editorX,
          top: editorY,
          width: editorWidth,
          height: rowHeight,
          child: Container(
            color: Colors.white,
            foregroundDecoration: BoxDecoration(
              border: Border.all(color: Colors.blue, width: 1),
            ),
            child: editorWidget,
          ),
        ),
      );
    }
    return overlays;
  }

  double? calculateTotalWidth(
    List<VTableColumn> columns,
    Map<int, TableColumnWidth>? columnWidths,
  ) {
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



  /// A borderless SWT Table reserves no trim, so rows * getItemHeight() must fit exactly.
  double frameBorderWidth(TableThemeExtension theme) =>
      hasStyle(SWT.BORDER) ? theme.borderWidth : 0.0;

  BoxDecoration buildBorder(TableThemeExtension theme) {
    if (!hasStyle(SWT.BORDER)) return const BoxDecoration();
    return BoxDecoration(
      border: Border.all(color: theme.borderColor, width: theme.borderWidth),
    );
  }

  TableBorder buildHeaderBorder(bool showLines, TableThemeExtension theme) {
    return TableBorder(
      bottom: BorderSide(
        color: theme.headerBorderColor,
        width: theme.headerBorderWidth,
      ),
      verticalInside: showLines
          ? BorderSide(color: theme.linesColor, width: theme.linesWidth)
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
          ? BorderSide(color: theme.linesColor, width: theme.linesWidth)
          : BorderSide.none,
      top: BorderSide.none,
      bottom: BorderSide.none,
      left: BorderSide.none,
      right: BorderSide.none,
    );
  }

  double? calculateHeight(
    int rowCount,
    TableThemeExtension theme,
    BuildContext? context,
    TextStyle? rowTextStyle,
  ) {
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

    final Map<int, TableColumnWidth> widths;
    if (hasExplicitWidths) {
      widths =
          _calculateExplicitColumnWidths(context, columns, theme, hasCheckStyle);
    } else {
      widths = _calculateIntrinsicColumnWidths(
        context,
        columns,
        theme,
        hasCheckStyle,
      );
    }
    widths[columns.length] = const FlexColumnWidth(1);
    return widths;
  }

  Map<int, TableColumnWidth> _calculateExplicitColumnWidths(
    BuildContext context,
    List<VTableColumn> columns,
    TableThemeExtension theme,
    bool hasCheckStyle,
  ) {
    final Map<int, TableColumnWidth> widths = {};
    const double checkboxWidth = 20.0;

    final headerTextStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: getTableHeaderTextColor(state, theme),
      baseTextStyle: theme.headerTextStyle,
    );

    for (int i = 0; i < columns.length; i++) {
      final column = columns[i];
      double width;
      if (column.width != null) {
        width = column.width!.toDouble();
        if (hasCheckStyle && i == 0) {
          width += checkboxWidth + theme.cellPadding.left;
        }
        if (!_userResizedColumns.contains(column.id)) {
          width = math.max(
            width,
            _headerTextFloor(column, headerTextStyle, theme, hasCheckStyle, i,
                checkboxWidth),
          );
        }
      } else {
        // No explicit width (e.g. Find Actions' category column): size to
        // content instead of collapsing to 0, which would hide the column.
        width = _intrinsicColumnWidth(
            context, columns, i, theme, hasCheckStyle, checkboxWidth);
      }
      widths[i] = FixedColumnWidth(width);
    }
    return widths;
  }

  double _headerTextFloor(
    VTableColumn column,
    TextStyle headerTextStyle,
    TableThemeExtension theme,
    bool hasCheckStyle,
    int columnIndex,
    double checkboxWidth,
  ) {
    if ((column.width ?? 0) <= 0) return 0.0;
    final headerText = column.text ?? "";
    if (headerText.isEmpty) return 0.0;

    double floor = measureTableText(headerText, headerTextStyle);
    if (hasCheckStyle && columnIndex == 0) {
      floor += checkboxWidth + theme.cellPadding.left;
    }
    if (column.image != null) {
      floor += (headerTextStyle.fontSize ?? 16.0) + theme.headerPadding.left;
    }
    return floor;
  }

  /// Content-based width for a single column (widest of its header and cells),
  /// mirroring [_calculateIntrinsicColumnWidths] for one column.
  double _intrinsicColumnWidth(
    BuildContext context,
    List<VTableColumn> columns,
    int i,
    TableThemeExtension theme,
    bool hasCheckStyle,
    double checkboxWidth,
  ) {
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

    double maxWidth = _calculateHeaderWidth(columns[i], headerTextStyle, theme);
    for (final item in getItems()) {
      final cellWidth = _calculateCellWidth(
          context, item, i, rowTextStyle, theme, hasCheckStyle, checkboxWidth);
      if (cellWidth > maxWidth) maxWidth = cellWidth;
    }
    if (hasCheckStyle &&
        i == 0 &&
        maxWidth < checkboxWidth + theme.cellPadding.left) {
      maxWidth = checkboxWidth + theme.cellPadding.left;
    }
    return maxWidth > 0 ? maxWidth : 50.0;
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

      if (hasCheckStyle &&
          i == 0 &&
          maxWidth < checkboxWidth + theme.cellPadding.left) {
        maxWidth = checkboxWidth + theme.cellPadding.left;
      }

      widths[i] = FixedColumnWidth(maxWidth > 0 ? maxWidth : 50.0);
    }
    return widths;
  }

  double _calculateHeaderWidth(
    VTableColumn column,
    TextStyle headerTextStyle,
    TableThemeExtension theme,
  ) {
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
    final cellText = columnIndex < cellTexts.length
        ? (cellTexts[columnIndex] ?? "")
        : "";

    final cellFont = item.font ?? state.font;
    final cellTextStyle = getTextStyle(
      context: context,
      font: cellFont,
      textColor: getTableRowTextColor(item, theme, false, true),
      baseTextStyle: rowTextStyle,
    );

    double cellWidth = theme.cellPadding.horizontal;

    if (cellText.isNotEmpty) {
      final cellPainter = TextPainter(
        text: TextSpan(text: cellText, style: cellTextStyle),
        textDirection: TextDirection.ltr,
      );
      cellPainter.layout();
      cellWidth += cellPainter.width;
    }

    // Account for the image icon width + gap if this column has an image
    final cellImage = _cellImageForItem(item, columnIndex);
    if (cellImage != null) {
      final iconSize = cellTextStyle.fontSize ?? 16.0;
      cellWidth += iconSize + theme.cellPadding.left;
    }

    if (hasCheckStyle && columnIndex == 0) {
      cellWidth += checkboxWidth + theme.cellPadding.left;
    }
    return cellWidth;
  }

  VImage? _cellImageForItem(VTableItem item, int columnIndex) {
    final imgs = item.images;
    if (imgs != null && columnIndex < imgs.length) return imgs[columnIndex];
    if (imgs == null && columnIndex == 0) return item.image;
    return null;
  }

  List<VTableColumn> getColumns() {
    return state.columns ?? [];
  }

  List<VTableItem> getItems() {
    return state.items ?? [];
  }

  double get cachedRowHeight => _cachedRowHeight ?? 20.0;
  double get cachedHeaderOffset => _cachedHeaderOffset ?? 0.0;
  Map<int, TableColumnWidth>? get cachedColumnWidths => _cachedColumnWidths;

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

  TableItemSwtWrapper({
    required this.item,
    required this.rowIndex,
    required this.tableImpl,
    required this.parentTable,
    required this.parentTableValue,
    this.tableFont,
  });

  List<Widget> buildCells(BuildContext context, TableThemeExtension theme) {
    final tableItemImpl = TableItemImpl<TableItemSwt, VTableItem>();
    tableItemImpl.state = item;
    tableItemImpl.setContext(
      TableItemContext(
        rowIndex: rowIndex,
        parentTable: parentTable,
        parentTableValue: parentTableValue,
        tableImpl: tableImpl,
        tableFont: tableFont,
      ),
    );
    return tableItemImpl.buildCells(context, theme);
  }
}

double measureTableText(String text, TextStyle style) {
  if (text.isEmpty) return 0.0;
  final painter = TextPainter(
    text: TextSpan(text: text, style: style),
    textDirection: TextDirection.ltr,
    maxLines: 1,
  )..layout();
  return painter.width;
}

EdgeInsets fitTableCellPadding(
  EdgeInsets padding, {
  required double available,
  required double contentWidth,
}) {
  final double room = available - contentWidth;
  if (room >= padding.horizontal) return padding;
  final double half = room > 0 ? room / 2 : 0;
  return padding.copyWith(left: half, right: half);
}

class TableItemContext {
  final int rowIndex;
  final TableSwt parentTable;
  final VTable parentTableValue;
  final TableImpl? tableImpl;
  final VFont? tableFont;

  TableItemContext({
    required this.rowIndex,
    required this.parentTable,
    required this.parentTableValue,
    this.tableImpl,
    this.tableFont,
  });

  static TableItemContext? of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<TableItemContextProvider>();
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
  }) : context = TableItemContext(
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
        context.parentTableValue.selection !=
            oldWidget.context.parentTableValue.selection;
  }
}
