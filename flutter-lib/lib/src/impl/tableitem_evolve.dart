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
    if (_context == null) {
      return const SizedBox.shrink();
    }

    final theme = Theme.of(context).extension<TableThemeExtension>();
    if (theme == null) {
      return const SizedBox.shrink();
    }

    return const SizedBox.shrink();
  }

  List<Widget> buildCells(BuildContext context, TableThemeExtension theme) {
    final cellTexts = state.texts ?? [];
    final rowIndex = _context?.rowIndex ?? 0;
    final enabled = _context?.parentTableValue.enabled ?? false;
    final isSelected = _context?.tableImpl?.isItemSelected(state) ?? false;
    final hasCheckStyle = _context?.parentTableValue.style != null &&
        (_context!.parentTableValue.style! & SWT.CHECK) != 0;
    final columnCount = _context?.parentTableValue.columns?.length ?? cellTexts.length;
    final effectiveColumnCount = columnCount > 0 ? columnCount : 1;

    return List.generate(
      effectiveColumnCount,
      (columnIndex) {
        final cellText = columnIndex < cellTexts.length ? (cellTexts[columnIndex] ?? "") : "";
        final cellTextColor = getCellTextColor(columnIndex, theme, isSelected, enabled);
        final cellTextStyle = getTextStyle(
          context: context,
          font: state.font ?? _context?.tableFont,
          textColor: cellTextColor,
          baseTextStyle: theme.rowTextStyle,
        );
        return buildCell(
          context,
          columnIndex,
          cellText,
          cellTextStyle,
          theme,
          rowIndex,
          hasCheckStyle && columnIndex == 0,
        );
      },
    );
  }

  Widget buildCell(
    BuildContext context,
    int columnIndex,
    String cellText,
    TextStyle textStyle,
    TableThemeExtension theme,
    int rowIndex,
    bool showCheckbox,
  ) {
    final enabled = _context?.parentTableValue.enabled ?? true;
    final cellBackgroundColor = getCellBackgroundColor(columnIndex, theme);
    final rowHeight = calculateRowHeight(textStyle, theme);
    final isEditing = _context?.editingRowIndex == rowIndex && 
                     _context?.editingColumnIndex == columnIndex;

    return GestureDetector(
      onTap: () {
        if (!isEditing && enabled && _context?.tableImpl != null) {
          _context!.tableImpl!.handleRowTap(rowIndex, state);
        }
      },
      onDoubleTap: () {
        if (!isEditing && enabled && _context?.tableImpl != null) {
          _context!.tableImpl!.startEditing(rowIndex, columnIndex, cellText);
        }
      },
      child: SizedBox(
        height: rowHeight,
        child: Container(
          padding: theme.cellPadding,
          alignment: Alignment.centerLeft,
          color: cellBackgroundColor,
          child: Row(
            children: [
              if (showCheckbox) buildCheckbox(theme, enabled),
              if (columnIndex == 0 && state.image != null) 
                buildImageIcon(state.image!, enabled, textStyle, theme),
              Expanded(
                child: isEditing && _context?.editingController != null && _context?.editingFocusNode != null
                    ? TextField(
                        controller: _context!.editingController!,
                        focusNode: _context!.editingFocusNode!,
                        style: textStyle,
                        decoration: InputDecoration(
                          isDense: true,
                          contentPadding: EdgeInsets.zero,
                          border: InputBorder.none,
                          focusedBorder: InputBorder.none,
                          enabledBorder: InputBorder.none,
                        ),
                        onSubmitted: (value) {
                          if (_context?.tableImpl != null) {
                            _context!.tableImpl!.finishEditing();
                          }
                        },
                        onEditingComplete: () {
                          if (_context?.tableImpl != null) {
                            _context!.tableImpl!.finishEditing();
                          }
                        },
                      )
                    : Text(
                        cellText,
                        style: textStyle,
                        overflow: TextOverflow.ellipsis,
                      ),
              ),
            ],
          ),
        ),
      ),
    );
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
          final e = VEvent()..detail = SWT.CHECK;
          _context?.parentTable.sendSelectionSelection(_context!.parentTableValue, e);
        },
      ),
    );
  }

  Widget buildImageIcon(VImage image, bool enabled, TextStyle textStyle, TableThemeExtension theme) {
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
        if (snapshot.connectionState == ConnectionState.done && snapshot.data != null) {
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
    final defaultColor = enabled
        ? (isSelected
            ? theme.rowSelectedTextColor
            : theme.rowTextColor)
        : theme.rowDisabledTextColor;

    final cellForeground = getCellForeground(columnIndex);
    return getForegroundColor(
      foreground: cellForeground ?? state.foreground,
      defaultColor: defaultColor,
    );
  }

  Color getCellBackgroundColor(
    int columnIndex,
    TableThemeExtension theme,
  ) {
    final cellBackground = getCellBackground(columnIndex);
    return getBackgroundColor(
      background: cellBackground ?? state.background,
      defaultColor: Colors.transparent,
    ) ?? Colors.transparent;
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
  State<_TableCheckboxButtonWrapper> createState() => _TableCheckboxButtonWrapperState();
}

class _TableCheckboxButtonWrapperState extends State<_TableCheckboxButtonWrapper> {
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

  const _TableCheckboxButton({
    required super.value,
    required this.onChanged,
  });

  @override
  void sendSelectionSelection(VButton val, VEvent? payload) {
    onChanged();
  }
}
