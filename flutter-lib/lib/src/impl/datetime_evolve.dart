import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../gen/datetime.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../impl/composite_evolve.dart';
import '../theme/theme_extensions/spinner_theme_extension.dart';
import 'utils/widget_utils.dart';

const _kDtButtonWidth = 9.0;
const _kDtIconSize = 9.0;
const _kDtDropDownWidth = 9.0;

class DateTimeImpl<T extends DateTimeSwt, V extends VDateTime>
    extends CompositeImpl<T, V> {
  int _selectedField = 0;
  bool _isUpHovered = false;
  bool _isDownHovered = false;
  final _focusNode = FocusNode();
  String _digitBuffer = '';
  OverlayEntry? _calendarOverlay;
  bool _calendarDropped = false;

  int _maxDigits(_FieldDef f) => f.max >= 1000 ? 4 : 2;

  @override
  void dispose() {
    _closeCalendarOverlay();
    _focusNode.dispose();
    super.dispose();
  }

  void _openCalendarOverlay({
    required int year,
    required int month,
    required int day,
    required bool enabled,
    required ColorScheme colorScheme,
  }) {
    _closeCalendarOverlay();
    final renderBox = context.findRenderObject() as RenderBox?;
    if (renderBox == null) return;
    final offset = renderBox.localToGlobal(Offset.zero);
    final size = renderBox.size;

    _calendarOverlay = OverlayEntry(
      builder: (_) => Stack(
        children: [
          Positioned.fill(
            child: GestureDetector(
              onTap: () {
                setState(() => _calendarDropped = false);
                _closeCalendarOverlay();
              },
              behavior: HitTestBehavior.translucent,
            ),
          ),
          Positioned(
            left: offset.dx,
            top: offset.dy + size.height,
            child: Material(
              elevation: 4,
              borderRadius: BorderRadius.circular(4),
              child: _CalendarWidget(
                year: year,
                month: month,
                selectedDay: day,
                showWeekNumbers: false,
                enabled: enabled,
                colorScheme: colorScheme,
                onDaySelected: (y, m, d) {
                  setState(() {
                    state.year = y;
                    state.month = m;
                    state.day = d;
                    _calendarDropped = false;
                  });
                  _closeCalendarOverlay();
                  widget.sendSelectionSelection(state, _makeSelectionEvent());
                },
                onMonthChanged: (_, __) {},
              ),
            ),
          ),
        ],
      ),
    );
    Overlay.of(context).insert(_calendarOverlay!);
  }

  void _closeCalendarOverlay() {
    _calendarOverlay?.remove();
    _calendarOverlay = null;
  }

  @override
  Widget build(BuildContext context) {
    final style = state.style;
    if (hasStyle(style, SWT.CALENDAR)) {
      return _buildCalendar(context);
    } else if (hasStyle(style, SWT.TIME)) {
      return _buildTime(context);
    } else {
      return _buildDate(context);
    }
  }

  // ── CALENDAR ──────────────────────────────────────────────────────────────

  Widget _buildCalendar(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final enabled = state.enabled ?? true;
    final showWeekNumbers = hasStyle(state.style, SWT.CALENDAR_WEEKNUMBERS);

    final now = DateTime.now();
    final year = state.year ?? now.year;
    final month = state.month ?? now.month - 1;
    final day = state.day ?? 1;

    return wrap(
      _CalendarWidget(
        year: year,
        month: month,
        selectedDay: day,
        showWeekNumbers: showWeekNumbers,
        enabled: enabled,
        colorScheme: colorScheme,
        onDaySelected: (y, m, d) {
          setState(() {
            state.year = y;
            state.month = m;
            state.day = d;
          });
          widget.sendSelectionSelection(state, _makeSelectionEvent());
        },
        onMonthChanged: (y, m) {
          setState(() {
            state.year = y;
            state.month = m;
          });
          widget.sendSelectionSelection(state, _makeSelectionEvent());
        },
      ),
    );
  }

  // ── TIME ──────────────────────────────────────────────────────────────────

  Widget _buildTime(BuildContext context) {
    final theme = Theme.of(context).extension<SpinnerThemeExtension>()!;
    final style = state.style;
    final enabled = state.enabled ?? true;
    final isShort = hasStyle(style, SWT.SHORT);

    final now = DateTime.now();
    final hours = state.hours ?? now.hour;
    final minutes = state.minutes ?? now.minute;
    final seconds = state.seconds ?? now.second;

    final fields = <_FieldDef>[
      _FieldDef(
        value: hours,
        min: 0,
        max: 23,
        format: (v) => v.toString().padLeft(2, '0'),
        onChanged: (v) {
          setState(() => state.hours = v);
          widget.sendSelectionSelection(state, _makeSelectionEvent());
        },
      ),
      _FieldDef(
        value: minutes,
        min: 0,
        max: 59,
        format: (v) => v.toString().padLeft(2, '0'),
        onChanged: (v) {
          setState(() => state.minutes = v);
          widget.sendSelectionSelection(state, _makeSelectionEvent());
        },
      ),
      if (!isShort)
        _FieldDef(
          value: seconds,
          min: 0,
          max: 59,
          format: (v) => v.toString().padLeft(2, '0'),
          onChanged: (v) {
            setState(() => state.seconds = v);
            widget.sendSelectionSelection(state, _makeSelectionEvent());
          },
        ),
    ];

    return wrap(_buildSpinnerContent(theme, enabled, fields, ':'));
  }

  // ── DATE ──────────────────────────────────────────────────────────────────

  Widget _buildDate(BuildContext context) {
    final theme = Theme.of(context).extension<SpinnerThemeExtension>()!;
    final style = state.style;
    final enabled = state.enabled ?? true;
    final isShort = hasStyle(style, SWT.SHORT);
    final hasDropDown = hasStyle(style, SWT.DROP_DOWN);

    final now = DateTime.now();
    final year = state.year ?? now.year;
    final month = state.month ?? now.month - 1;
    final day = state.day ?? now.day;

    final fields = <_FieldDef>[
      _FieldDef(
        value: month + 1,
        min: 1,
        max: 12,
        format: (v) => v.toString().padLeft(2, '0'),
        onChanged: (v) {
          setState(() => state.month = v - 1);
          widget.sendSelectionSelection(state, _makeSelectionEvent());
        },
      ),
      if (!isShort)
        _FieldDef(
          value: day,
          min: 1,
          max: _daysInMonth(year, month),
          format: (v) => v.toString().padLeft(2, '0'),
          onChanged: (v) {
            setState(() => state.day = v);
            widget.sendSelectionSelection(state, _makeSelectionEvent());
          },
        ),
      _FieldDef(
        value: year,
        min: 1752,
        max: 9999,
        format: (v) => v.toString(),
        onChanged: (v) {
          setState(() => state.year = v);
          widget.sendSelectionSelection(state, _makeSelectionEvent());
        },
      ),
    ];

    final colorScheme = Theme.of(context).colorScheme;

    return wrap(_buildSpinnerContent(
      theme,
      enabled,
      fields,
      '/',
      extraTrailing: hasDropDown
          ? _DropDownArrow(
              enabled: enabled,
              isOpen: _calendarDropped,
              theme: theme,
              onTap: () {
                if (_calendarDropped) {
                  setState(() => _calendarDropped = false);
                  _closeCalendarOverlay();
                } else {
                  setState(() => _calendarDropped = true);
                  _openCalendarOverlay(
                    year: year,
                    month: month,
                    day: day,
                    enabled: enabled,
                    colorScheme: colorScheme,
                  );
                }
              },
            )
          : null,
    ));
  }

  // ── Shared spinner content ─────────────────────────────────────────────────

  Widget _buildSpinnerContent(
    SpinnerThemeExtension theme,
    bool enabled,
    List<_FieldDef> fields,
    String separator, {
    Widget? extraTrailing,
  }) {
    final selIdx = _selectedField.clamp(0, fields.length - 1);
    final textColor = enabled ? theme.textColor : theme.disabledTextColor;
    final iconColor = enabled ? theme.iconColor : theme.disabledIconColor;

    Widget fieldRow() => Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        for (int i = 0; i < fields.length; i++) ...[
          if (i > 0) _FieldSeparator(text: separator, theme: theme),
          GestureDetector(
            onTap: () {
              _focusNode.requestFocus();
              setState(() { _digitBuffer = ''; _selectedField = i; });
            },
            child: ColoredBox(
              color: i == selIdx && enabled
                  ? theme.focusedBorderColor.withOpacity(0.15)
                  : Colors.transparent,
              child: Padding(
                // Less padding on the separator side so numbers sit tight to the separator
                padding: EdgeInsets.only(
                  left: i == 0 ? 2 : 0,
                  right: i == fields.length - 1 ? 2 : 0,
                ),
                child: Text(
                  fields[i].format(fields[i].value),
                  style: (theme.textStyle ?? const TextStyle())
                      .copyWith(color: textColor),
                ),
              ),
            ),
          ),
        ],
      ],
    );

    Widget spinnerButtons() => SizedBox(
      width: _kDtButtonWidth,
      child: Column(
        children: [
          Expanded(
            child: _ArrowButton(
              icon: Icons.arrow_drop_up,
              iconSize: _kDtIconSize,
              iconColor: _isUpHovered && enabled ? theme.iconHoverColor : iconColor,
              backgroundColor: _isUpHovered && enabled ? theme.buttonHoverColor : theme.buttonBackgroundColor,
              borderColor: theme.borderColor,
              borderWidth: theme.borderWidth,
              showBottomBorder: true,
              enabled: enabled,
              onTap: enabled ? () => _adjust(fields, selIdx, 1) : null,
              onHoverChanged: (h) => setState(() => _isUpHovered = h),
            ),
          ),
          Expanded(
            child: _ArrowButton(
              icon: Icons.arrow_drop_down,
              iconSize: _kDtIconSize,
              iconColor: _isDownHovered && enabled ? theme.iconHoverColor : iconColor,
              backgroundColor: _isDownHovered && enabled ? theme.buttonHoverColor : theme.buttonBackgroundColor,
              borderColor: theme.borderColor,
              borderWidth: theme.borderWidth,
              showBottomBorder: false,
              enabled: enabled,
              onTap: enabled ? () => _adjust(fields, selIdx, -1) : null,
              onHoverChanged: (h) => setState(() => _isDownHovered = h),
            ),
          ),
        ],
      ),
    );

    return Focus(
      focusNode: _focusNode,
      onKeyEvent: (node, event) {
        if (event is! KeyDownEvent) return KeyEventResult.ignored;
        final idx = _selectedField.clamp(0, fields.length - 1);
        if (event.logicalKey == LogicalKeyboardKey.arrowUp) {
          _digitBuffer = '';
          _adjust(fields, idx, 1);
          return KeyEventResult.handled;
        } else if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
          _digitBuffer = '';
          _adjust(fields, idx, -1);
          return KeyEventResult.handled;
        } else if (event.logicalKey == LogicalKeyboardKey.arrowLeft) {
          if (_selectedField > 0) setState(() { _digitBuffer = ''; _selectedField--; });
          return KeyEventResult.handled;
        } else if (event.logicalKey == LogicalKeyboardKey.arrowRight) {
          if (_selectedField < fields.length - 1) setState(() { _digitBuffer = ''; _selectedField++; });
          return KeyEventResult.handled;
        }
        final char = event.character;
        if (char != null && char.length == 1) {
          final digit = int.tryParse(char);
          if (digit != null) {
            final f = fields[idx];
            final maxLen = _maxDigits(f);
            _digitBuffer += char;
            final parsed = int.parse(_digitBuffer);
            final clamped = parsed.clamp(f.min, f.max);
            f.onChanged(clamped);
            final autoAdvance = _digitBuffer.length >= maxLen || parsed * 10 > f.max;
            if (autoAdvance) {
              if (_selectedField < fields.length - 1) {
                setState(() { _digitBuffer = ''; _selectedField++; });
              } else {
                _digitBuffer = '';
              }
            }
            return KeyEventResult.handled;
          }
        }
        return KeyEventResult.ignored;
      },
      child: LayoutBuilder(
        builder: (ctx, constraints) {
          final isBounded = constraints.hasBoundedWidth;
          return Container(
            decoration: BoxDecoration(
              color: enabled ? theme.backgroundColor : theme.disabledBackgroundColor,
              border: Border.all(
                color: enabled ? theme.borderColor : theme.disabledBorderColor,
                width: theme.borderWidth,
              ),
              borderRadius: BorderRadius.circular(theme.borderRadius),
            ),
            child: IntrinsicHeight(
              child: Row(
                mainAxisSize: isBounded ? MainAxisSize.max : MainAxisSize.min,
                children: [
                  if (isBounded)
                    Expanded(
                      child: SingleChildScrollView(
                        scrollDirection: Axis.horizontal,
                        child: fieldRow(),
                      ),
                    )
                  else
                    fieldRow(),
                  spinnerButtons(),
                  if (extraTrailing != null) extraTrailing,
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  void _adjust(List<_FieldDef> fields, int idx, int delta) {
    if (idx < 0 || idx >= fields.length) return;
    final f = fields[idx];
    int next = f.value + delta;
    if (next > f.max) next = f.min;
    if (next < f.min) next = f.max;
    f.onChanged(next);
  }

  int _daysInMonth(int year, int month) => DateTime(year, month + 2, 0).day;

  VEvent _makeSelectionEvent() {
    final now = DateTime.now();
    return VEvent()
      ..x = state.year ?? now.year
      ..y = state.month ?? (now.month - 1)
      ..width = state.day ?? now.day
      ..height = state.hours ?? now.hour
      ..count = state.minutes ?? now.minute
      ..index = state.seconds ?? now.second;
  }
}

// ── Field definition ──────────────────────────────────────────────────────

class _FieldDef {
  final int value;
  final int min;
  final int max;
  final String Function(int) format;
  final ValueChanged<int> onChanged;

  const _FieldDef({
    required this.value,
    required this.min,
    required this.max,
    required this.format,
    required this.onChanged,
  });
}

// ── Arrow button ───────────────────────────────────────────────────────────

class _ArrowButton extends StatelessWidget {
  final IconData icon;
  final double iconSize;
  final Color iconColor;
  final Color backgroundColor;
  final Color borderColor;
  final double borderWidth;
  final bool showBottomBorder;
  final bool enabled;
  final VoidCallback? onTap;
  final ValueChanged<bool> onHoverChanged;

  const _ArrowButton({
    required this.icon,
    required this.iconSize,
    required this.iconColor,
    required this.backgroundColor,
    required this.borderColor,
    required this.borderWidth,
    required this.showBottomBorder,
    required this.enabled,
    this.onTap,
    required this.onHoverChanged,
  });

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => onHoverChanged(true),
      onExit: (_) => onHoverChanged(false),
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          decoration: BoxDecoration(
            color: backgroundColor,
            border: Border(
              left: BorderSide(color: borderColor, width: borderWidth),
              bottom: showBottomBorder
                  ? BorderSide(color: borderColor, width: borderWidth * 0.5)
                  : BorderSide.none,
            ),
          ),
          child: Center(
            child: Icon(icon, size: iconSize, color: iconColor),
          ),
        ),
      ),
    );
  }
}

// ── Field separator ────────────────────────────────────────────────────────

class _FieldSeparator extends StatelessWidget {
  final String text;
  final SpinnerThemeExtension theme;

  const _FieldSeparator({required this.text, required this.theme});

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: (theme.textStyle ?? const TextStyle()).copyWith(
        color: theme.textColor,
      ),
    );
  }
}

// ── Drop-down arrow button ─────────────────────────────────────────────────

class _DropDownArrow extends StatefulWidget {
  final bool enabled;
  final bool isOpen;
  final SpinnerThemeExtension theme;
  final VoidCallback onTap;

  const _DropDownArrow({
    required this.enabled,
    required this.isOpen,
    required this.theme,
    required this.onTap,
  });

  @override
  State<_DropDownArrow> createState() => _DropDownArrowState();
}

class _DropDownArrowState extends State<_DropDownArrow> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final t = widget.theme;
    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      child: GestureDetector(
        onTap: widget.enabled ? widget.onTap : null,
        child: Container(
          width: _kDtDropDownWidth,
          decoration: BoxDecoration(
            color: _isHovered && widget.enabled
                ? t.buttonHoverColor
                : t.buttonBackgroundColor,
            border: Border(
              left: BorderSide(color: t.borderColor, width: t.borderWidth),
            ),
          ),
          child: Center(
            child: Icon(
              widget.isOpen ? Icons.arrow_drop_up : Icons.arrow_drop_down,
              size: _kDtIconSize,
              color: widget.enabled ? t.iconColor : t.disabledIconColor,
            ),
          ),
        ),
      ),
    );
  }
}

// ── Calendar widget ────────────────────────────────────────────────────────

class _CalendarWidget extends StatefulWidget {
  final int year;
  final int month;
  final int selectedDay;
  final bool showWeekNumbers;
  final bool enabled;
  final ColorScheme colorScheme;
  final void Function(int year, int month, int day) onDaySelected;
  final void Function(int year, int month) onMonthChanged;

  const _CalendarWidget({
    required this.year,
    required this.month,
    required this.selectedDay,
    required this.showWeekNumbers,
    required this.enabled,
    required this.colorScheme,
    required this.onDaySelected,
    required this.onMonthChanged,
  });

  @override
  State<_CalendarWidget> createState() => _CalendarWidgetState();
}

class _CalendarWidgetState extends State<_CalendarWidget> {
  late int _displayYear;
  late int _displayMonth;

  static const _dayLabels = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
  static const _monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
  ];

  @override
  void initState() {
    super.initState();
    _displayYear = widget.year;
    _displayMonth = widget.month;
  }

  @override
  void didUpdateWidget(_CalendarWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.year != widget.year || oldWidget.month != widget.month) {
      _displayYear = widget.year;
      _displayMonth = widget.month;
    }
  }

  void _prevMonth() {
    setState(() {
      if (_displayMonth == 0) {
        _displayMonth = 11;
        _displayYear--;
      } else {
        _displayMonth--;
      }
    });
    widget.onMonthChanged(_displayYear, _displayMonth);
  }

  void _nextMonth() {
    setState(() {
      if (_displayMonth == 11) {
        _displayMonth = 0;
        _displayYear++;
      } else {
        _displayMonth++;
      }
    });
    widget.onMonthChanged(_displayYear, _displayMonth);
  }

  int _isoWeekNumber(DateTime date) {
    final thursday = date.add(Duration(days: 4 - (date.weekday % 7 == 0 ? 7 : date.weekday)));
    final jan1 = DateTime(thursday.year, 1, 1);
    return ((thursday.difference(jan1).inDays) / 7).floor() + 1;
  }

  @override
  Widget build(BuildContext context) {
    final cs = widget.colorScheme;
    final firstDay = DateTime(_displayYear, _displayMonth + 1, 1);
    final startOffset = firstDay.weekday % 7;
    final daysInMonth = DateTime(_displayYear, _displayMonth + 2, 0).day;
    final totalCells = startOffset + daysInMonth;
    final rows = (totalCells / 7).ceil();

    const cellSize = 28.0;
    const weekNumWidth = 28.0;

    return Container(
      padding: const EdgeInsets.all(8),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Header: prev / Month Year / next
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              IconButton(
                icon: const Icon(Icons.chevron_left, size: 18),
                onPressed: widget.enabled ? _prevMonth : null,
                splashRadius: 16,
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(minWidth: 28, minHeight: 28),
              ),
              Text(
                '${_monthNames[_displayMonth]} $_displayYear',
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  color: cs.onSurface,
                  fontSize: 13,
                ),
              ),
              IconButton(
                icon: const Icon(Icons.chevron_right, size: 18),
                onPressed: widget.enabled ? _nextMonth : null,
                splashRadius: 16,
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(minWidth: 28, minHeight: 28),
              ),
            ],
          ),
          const SizedBox(height: 4),
          // Day-of-week labels
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (widget.showWeekNumbers)
                SizedBox(
                  width: weekNumWidth,
                  child: Text(
                    'Wk',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontSize: 11,
                      color: cs.onSurfaceVariant,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              for (final label in _dayLabels)
                SizedBox(
                  width: cellSize,
                  child: Text(
                    label,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontSize: 11,
                      color: cs.onSurfaceVariant,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
            ],
          ),
          const SizedBox(height: 2),
          // Day grid
          for (int row = 0; row < rows; row++)
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (widget.showWeekNumbers) ...[
                  Builder(builder: (context) {
                    final firstCellIndex = row * 7;
                    final dayNum = firstCellIndex - startOffset + 1;
                    final clampedDay = dayNum.clamp(1, daysInMonth);
                    final date = DateTime(_displayYear, _displayMonth + 1, clampedDay);
                    return SizedBox(
                      width: weekNumWidth,
                      height: cellSize,
                      child: Center(
                        child: Text(
                          '${_isoWeekNumber(date)}',
                          style: TextStyle(fontSize: 10, color: cs.onSurfaceVariant),
                        ),
                      ),
                    );
                  }),
                ],
                for (int col = 0; col < 7; col++) ...[
                  Builder(builder: (context) {
                    final cellIndex = row * 7 + col;
                    final dayNum = cellIndex - startOffset + 1;
                    final isValid = dayNum >= 1 && dayNum <= daysInMonth;
                    final isSelected = isValid &&
                        dayNum == widget.selectedDay &&
                        _displayYear == widget.year &&
                        _displayMonth == widget.month;
                    final isToday = isValid &&
                        dayNum == DateTime.now().day &&
                        _displayMonth == DateTime.now().month - 1 &&
                        _displayYear == DateTime.now().year;

                    return _DayCell(
                      day: isValid ? dayNum : null,
                      isSelected: isSelected,
                      isToday: isToday,
                      enabled: widget.enabled && isValid,
                      cellSize: cellSize,
                      colorScheme: cs,
                      onTap: isValid && widget.enabled
                          ? () => widget.onDaySelected(_displayYear, _displayMonth, dayNum)
                          : null,
                    );
                  }),
                ],
              ],
            ),
        ],
      ),
    );
  }
}

// ── Day cell ───────────────────────────────────────────────────────────────

class _DayCell extends StatefulWidget {
  final int? day;
  final bool isSelected;
  final bool isToday;
  final bool enabled;
  final double cellSize;
  final ColorScheme colorScheme;
  final VoidCallback? onTap;

  const _DayCell({
    required this.day,
    required this.isSelected,
    required this.isToday,
    required this.enabled,
    required this.cellSize,
    required this.colorScheme,
    this.onTap,
  });

  @override
  State<_DayCell> createState() => _DayCellState();
}

class _DayCellState extends State<_DayCell> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    if (widget.day == null) {
      return SizedBox(width: widget.cellSize, height: widget.cellSize);
    }

    final cs = widget.colorScheme;
    Color bgColor;
    Color textColor;

    if (widget.isSelected) {
      bgColor = cs.primary;
      textColor = cs.onPrimary;
    } else if (_isHovered && widget.enabled) {
      bgColor = cs.primaryContainer;
      textColor = cs.onPrimaryContainer;
    } else {
      bgColor = Colors.transparent;
      textColor = widget.isToday
          ? cs.primary
          : widget.enabled
              ? cs.onSurface
              : cs.onSurface.withAlpha(97);
    }

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      child: GestureDetector(
        onTap: widget.onTap,
        child: Container(
          width: widget.cellSize,
          height: widget.cellSize,
          decoration: BoxDecoration(
            color: bgColor,
            shape: BoxShape.circle,
            border: widget.isToday && !widget.isSelected
                ? Border.all(color: cs.primary, width: 1)
                : null,
          ),
          child: Center(
            child: Text(
              '${widget.day}',
              style: TextStyle(
                fontSize: 12,
                color: textColor,
                fontWeight: widget.isToday ? FontWeight.bold : FontWeight.normal,
              ),
            ),
          ),
        ),
      ),
    );
  }
}