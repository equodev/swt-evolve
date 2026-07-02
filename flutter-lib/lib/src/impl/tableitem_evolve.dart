import 'package:flutter/material.dart';
import '../gen/tableitem.dart';
import '../gen/widget.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/button.dart';
import '../gen/color.dart';
import '../gen/image.dart';
import '../impl/item_evolve.dart';
import '../impl/table_evolve.dart';
import '../theme/theme_extensions/table_theme_extension.dart';
import '../theme/theme_settings/table_theme_settings.dart';
import '../impl/color_utils.dart';
import 'utils/widget_utils.dart';
import 'utils/image_utils.dart';

class TableItemImpl<T extends TableItemSwt, V extends VTableItem>
    extends ItemImpl<T, V> {
  TableItemContext? _context;

  void setContext(TableItemContext context) {
    _context = context;
  }

  @override
  void extraSetState() {
    super.extraSetState();
  }

  @override
  Widget build(BuildContext context) {
    _context = TableItemContext.of(context);

    final theme = Theme.of(context).extension<TableThemeExtension>();
    if (theme == null) {
      return const SizedBox.shrink();
    }

    if (_context == null) {
      // Standalone mode used by the measure tool — render column 0 as a single row cell.
      final cellText =
          state.texts?.isNotEmpty == true ? (state.texts!.first ?? '') : '';
      final textStyle = getTextStyle(
        context: context,
        font: state.font,
        textColor: theme.rowTextStyle?.color ?? Colors.black,
        baseTextStyle: theme.rowTextStyle,
      );
      final VImage? cellImage =
          state.images?.isNotEmpty == true ? state.images!.first : state.image;
      return SizedBox(
        height: calculateRowHeight(textStyle, theme),
        child: Container(
          padding: theme.cellPadding,
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (cellImage != null)
                buildImageIcon(cellImage, true, textStyle, theme),
              Text(cellText, style: textStyle, maxLines: 1),
            ],
          ),
        ),
      );
    }

    return const SizedBox.shrink();
  }

  List<Widget> buildCells(BuildContext context, TableThemeExtension theme) {
    final cellTexts = state.texts ?? [];
    final rowIndex = _context?.rowIndex ?? 0;
    final enabled = _context?.parentTableValue.enabled ?? false;
    final isSelected = _context?.tableImpl?.isItemSelected(state) ?? false;
    final hasCheckStyle =
        _context?.parentTableValue.style != null &&
        (_context!.parentTableValue.style! & SWT.CHECK) != 0;
    final columnCount =
        _context?.parentTableValue.columns?.length ?? cellTexts.length;
    final effectiveColumnCount = columnCount > 0 ? columnCount : 1;

    final columns = _context?.parentTableValue.columns ?? [];

    return List.generate(effectiveColumnCount, (columnIndex) {
      final cellText = columnIndex < cellTexts.length
          ? (cellTexts[columnIndex] ?? "")
          : "";
      final cellTextColor = getCellTextColor(
        columnIndex,
        theme,
        isSelected,
        enabled,
      );
      final cellTextStyle = getTextStyle(
        context: context,
        font: state.font ?? _context?.tableFont,
        textColor: cellTextColor,
        baseTextStyle: theme.rowTextStyle,
      );
      final columnAlignment = columnIndex < columns.length
          ? (columns[columnIndex].alignment ?? SWT.LEFT)
          : SWT.LEFT;
      return buildCell(
        context,
        columnIndex,
        cellText,
        cellTextStyle,
        theme,
        rowIndex,
        hasCheckStyle && columnIndex == 0,
        columnAlignment,
      );
    });
  }

  VImage? _cellImageForColumn(int columnIndex) {
    final imgs = state.images;
    if (imgs != null && columnIndex < imgs.length) {
      return imgs[columnIndex];
    }
    if (imgs == null && columnIndex == 0) {
      return state.image;
    }
    return null;
  }

  Widget buildCell(
    BuildContext context,
    int columnIndex,
    String cellText,
    TextStyle textStyle,
    TableThemeExtension theme,
    int rowIndex,
    bool showCheckbox, [
    int columnAlignment = SWT.LEFT,
  ]) {
    final cellImage = _cellImageForColumn(columnIndex);
    final enabled = _context?.parentTableValue.enabled ?? true;
    final cellBackgroundColor = getCellBackgroundColor(columnIndex, theme);
    final rowHeight = calculateRowHeight(textStyle, theme);
    final checkboxOnly = showCheckbox && cellText.isEmpty && cellImage == null;
    final imageOnly = !showCheckbox && cellText.isEmpty && cellImage != null;
    final cellAlignment = _swtToAlignment(columnAlignment);

    void sendMouseDown(int button) {
      if (enabled && _context != null) {
        if ((button == 1 || button == 3) && _context!.tableImpl != null) {
          _context!.tableImpl!.handleRowTap(rowIndex, state);
        }
        final e = VEvent();
        e.x = _computeCellCenterX(columnIndex, theme);
        e.y = _computeCellCenterY(rowIndex, textStyle, theme);
        e.button = button;
        e.count = 1;
        _context!.parentTable.sendEvent(
          _context!.parentTableValue,
          "Mouse/MouseDown",
          e,
        );
      }
    }

    void sendMouseDoubleClick(int button) {
      if (enabled && _context?.tableImpl != null) {
        _context!.tableImpl!.handleRowTap(rowIndex, state);
        final e = VEvent();
        e.x = _computeCellCenterX(columnIndex, theme);
        e.y = _computeCellCenterY(rowIndex, textStyle, theme);
        e.count = 2;
        e.button = button;
        _context!.parentTable.sendEvent(
          _context!.parentTableValue,
          "Mouse/MouseDoubleClick",
          e,
        );
      }
    }

    return GestureDetector(
      onTapDown: (_) => sendMouseDown(1),
      onSecondaryTapDown: (details) {
        sendMouseDown(3);
        if (_context?.tableImpl != null) {
          _context!.tableImpl!.openContextMenu(details.globalPosition);
          final e = VEvent();
          e.x = _computeCellCenterX(columnIndex, theme);
          e.y = _computeCellCenterY(rowIndex, textStyle, theme);
          _context!.parentTable.sendMenuDetectMenuDetect(
            _context!.parentTableValue,
            e,
          );
        }
      },
      onTertiaryTapDown: (_) => sendMouseDown(2),
      onTap: () {
        if (enabled && _context?.tableImpl != null) {
          _context!.tableImpl!.handleRowTap(rowIndex, state);
        }
      },
      onDoubleTap: () => sendMouseDoubleClick(1),
      child: SizedBox(
        height: rowHeight,
        child: Container(
          padding: theme.cellPadding,
          alignment: checkboxOnly || imageOnly ? cellAlignment : Alignment.centerLeft,
          color: cellBackgroundColor,
          child: checkboxOnly
              ? buildCheckbox(theme, enabled)
              : imageOnly
                  ? buildImageIcon(cellImage!, enabled, textStyle, theme)
                  : Row(
                      children: [
                        if (showCheckbox) buildCheckbox(theme, enabled),
                        if (cellImage != null)
                          buildImageIcon(cellImage, enabled, textStyle, theme),
                        Expanded(
                          child: Text(
                            cellText,
                            style: textStyle,
                            overflow: TextOverflow.ellipsis,
                            textAlign: columnAlignment == SWT.CENTER
                                ? TextAlign.center
                                : columnAlignment == SWT.RIGHT
                                    ? TextAlign.right
                                    : TextAlign.left,
                          ),
                        ),
                      ],
                    ),
        ),
      ),
    );
  }

  static Alignment _swtToAlignment(int swtAlignment) {
    if (swtAlignment == SWT.CENTER) return Alignment.center;
    if (swtAlignment == SWT.RIGHT) return Alignment.centerRight;
    return Alignment.centerLeft;
  }

  int _computeCellCenterX(int columnIndex, TableThemeExtension theme) {
    final columnWidths = _context?.tableImpl?.cachedColumnWidths;
    double x = 0.0;
    if (columnWidths != null) {
      for (int i = 0; i < columnIndex; i++) {
        final w = columnWidths[i];
        if (w is FixedColumnWidth) x += w.value;
      }
      final colW = columnWidths[columnIndex];
      if (colW is FixedColumnWidth) x += colW.value / 2;
    } else {
      final columns = _context?.parentTableValue.columns ?? [];
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

  int _computeCellCenterY(
    int rowIndex,
    TextStyle textStyle,
    TableThemeExtension theme,
  ) {
    final rowHeight = _context?.tableImpl?.cachedRowHeight ?? 20.0;
    final headerOffset = 0; // _context?.tableImpl?.cachedHeaderOffset ?? 0.0;
    return (headerOffset + rowIndex * rowHeight + rowHeight / 2).round();
  }

  Widget buildCheckbox(TableThemeExtension theme, bool enabled) {
    final checked = state.checked ?? false;
    final grayed = state.grayed ?? false;

    return Container(
      margin: EdgeInsets.only(right: theme.cellPadding.left),
      child: _TableCheckboxButtonWrapper(
        checked: checked,
        grayed: grayed,
        enabled: enabled,
        onChanged: () {
          if (!enabled) return;
          final newCheckedState = grayed ? false : !checked;
          state.checked = newCheckedState;
          if (_context?.tableImpl != null) {
            _context!.tableImpl!.setState(() {});
          }
          final e = VEvent()
            ..detail = SWT.CHECK
            ..segments = [_context!.rowIndex];
          _context?.parentTable.sendSelectionSelection(
            _context!.parentTableValue,
            e,
          );
        },
      ),
    );
  }

  Widget buildImageIcon(
    VImage image,
    bool enabled,
    TextStyle textStyle,
    TableThemeExtension theme,
  ) {
    final iconSize = textStyle.fontSize ?? 16.0;
    return FutureBuilder<Widget?>(
      future: ImageUtils.buildVImageAsync(
        image,
        enabled: enabled,
        constraints: BoxConstraints(
          minWidth: iconSize,
          minHeight: iconSize,
          maxWidth: iconSize,
          maxHeight: iconSize,
        ),
        useBinaryImage: true,
        renderAsIcon: true,
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done &&
            snapshot.data != null) {
          return Padding(
            padding: EdgeInsets.only(right: theme.cellPadding.left),
            child: snapshot.data!,
          );
        }
        return SizedBox(width: iconSize, height: iconSize);
      },
    );
  }

  Color getCellTextColor(
    int columnIndex,
    TableThemeExtension theme,
    bool isSelected,
    bool enabled,
  ) {
    final defaultColor = getTableCellDefaultTextColor(theme, isSelected, enabled);

    final cellForeground = getCellForeground(columnIndex);
    return getForegroundColor(
      foreground: cellForeground ?? state.foreground,
      defaultColor: defaultColor,
    );
  }

  Color getCellBackgroundColor(int columnIndex, TableThemeExtension theme) {
    final cellBackground = getCellBackground(columnIndex);
    return getBackgroundColor(
          background: cellBackground ?? state.background,
          defaultColor: Colors.transparent,
        ) ??
        Colors.transparent;
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

  VColor? getCellForeground(int columnIndex) {
    return null;
  }

  VColor? getCellBackground(int columnIndex) {
    return null;
  }
}

class _TableCheckboxButtonWrapper extends StatefulWidget {
  final bool checked;
  final bool grayed;
  final bool enabled;
  final VoidCallback onChanged;

  const _TableCheckboxButtonWrapper({
    Key? key,
    required this.checked,
    required this.grayed,
    required this.enabled,
    required this.onChanged,
  }) : super(key: key);

  @override
  State<_TableCheckboxButtonWrapper> createState() =>
      _TableCheckboxButtonWrapperState();
}

class _TableCheckboxButtonWrapperState
    extends State<_TableCheckboxButtonWrapper> {
  late VButton buttonValue;

  @override
  void initState() {
    super.initState();
    buttonValue = VButton.empty()
      ..id = -1
      ..style = SWT.CHECK
      ..enabled = widget.enabled
      ..selection = widget.checked
      ..grayed = widget.grayed;
  }

  @override
  void didUpdateWidget(_TableCheckboxButtonWrapper oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.checked != widget.checked ||
        oldWidget.grayed != widget.grayed ||
        oldWidget.enabled != widget.enabled) {
      setState(() {
        buttonValue.selection = widget.checked;
        buttonValue.grayed = widget.grayed;
        buttonValue.enabled = widget.enabled;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return _TableCheckboxButton(
      value: buttonValue,
      onChanged: widget.onChanged,
    );
  }
}

class _TableCheckboxButton extends ButtonSwt<VButton> {
  final VoidCallback onChanged;

  const _TableCheckboxButton({required super.value, required this.onChanged});

  @override
  void sendSelectionSelection(VButton val, VEvent? payload) {
    onChanged();
  }
}
