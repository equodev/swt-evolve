import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../custom/html_tooltip.dart';
import '../gen/ccombo.dart';
import '../gen/event.dart';
import '../gen/point.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/ccombo_theme_extension.dart';
import '../theme/theme_settings/ccombo_theme_settings.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'utils/pending_text_echoes.dart';
import 'key_forwarding.dart';

class CComboImpl<T extends CComboSwt, V extends VCCombo>
    extends CompositeImpl<T, V> with PendingTextEchoes {
  late TextEditingController _controller;
  FocusNode? _focusNode;
  final MenuController _menuController = MenuController();
  bool _isFocused = false;
  bool _isHovered = false;
  /// The last `listVisible` Java sent or was told. Only a change from it is a command to open or close.
  bool? _lastJavaListVisible;
  /// What Java last told the list to be, until the list gets there; its state before then is not news.
  bool? _javaListTarget;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _focusNode = FocusNode();
    HardwareKeyboard.instance.addHandler(_handleListKey);
    _lastJavaListVisible = state.listVisible;
  }

  int get _selectedIndex => (state.items ?? const <String>[]).indexOf(state.text ?? '');

  // The list is an SWT List: it highlights the selection only, and the pointer over it moves nothing.
  bool _isHighlighted(int index) => index == _selectedIndex;

  static bool _isListNavigationKey(LogicalKeyboardKey key) =>
      key == LogicalKeyboardKey.arrowDown ||
      key == LogicalKeyboardKey.arrowUp ||
      key == LogicalKeyboardKey.enter ||
      key == LogicalKeyboardKey.numpadEnter;

  @override
  void extraSetState() {
    _applyJavaListVisible();
    String newText = state.text ?? "";
    // Ignore a stale echo of our own in-flight typing (see PendingTextEchoes); a value we
    // never sent is a genuine external change and still updates the controller below.
    if (isStaleTextEcho(newText, _controller.text)) return;
    if (_controller.text != newText) {
      _controller.text = newText;
      _controller.selection = TextSelection.collapsed(offset: newText.length);
    }
  }

  void _applyJavaListVisible() {
    final bool? visible = state.listVisible;
    if (visible == _lastJavaListVisible) return;
    _lastJavaListVisible = visible;
    _javaListTarget = visible == true;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || _menuController.isOpen == (visible == true)) return;
      visible == true ? _menuController.open() : _menuController.close();
    });
  }

  /// Tells Java whether the list is open: Flutter draws it, so Java has no other way to know.
  void _reportListVisible() {
    if (!mounted) return;
    final bool open = _menuController.isOpen;
    if (_javaListTarget != null) {
      if (open != _javaListTarget) return;
      _javaListTarget = null;
    }
    if (open == (_lastJavaListVisible ?? false)) return;
    _lastJavaListVisible = open;
    state.listVisible = open;
    widget.sendEvent(state, "List/Visible", VEvent()..detail = open ? 1 : 0);
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<CComboThemeExtension>()!;
    final styleBits = StyleBits(state.style);
    final bool isReadOnly = styleBits.has(SWT.READ_ONLY);
    final bool isSimple = styleBits.has(SWT.SIMPLE);
    final bool isEnabled = state.enabled ?? true;

    final bool isActive = _isFocused || _isHovered;
    final Color currentBg = !isEnabled
        ? widgetTheme.disabledBackgroundColor
        : getBackgroundColor(
              background: state.background,
              defaultColor: widgetTheme.backgroundColor,
              context: context,
            ) ??
            widgetTheme.backgroundColor;

    final Color currentBorderColor = !isEnabled
        ? widgetTheme.disabledBorderColor
        : (_isFocused
            ? widgetTheme.focusedBorderColor
            : (isActive ? widgetTheme.borderColor : Colors.transparent));

    final textColor = !isEnabled
        ? widgetTheme.disabledTextColor
        : getForegroundColor(
            foreground: state.foreground,
            defaultColor: widgetTheme.textColor,
            context: context,
          );

    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: widgetTheme.textStyle,
    );

    final borderWidth = styleBits.has(SWT.BORDER)
        ? 3.0
        : widgetTheme.borderWidth;

    final hasConstraints = hasBounds(state.bounds);
    final double? width = hasConstraints
        ? state.bounds!.width.toDouble()
        : null;
    final double? height = hasConstraints
        ? state.bounds!.height.toDouble()
        : null;

    Widget result;

    if (isSimple) {
      result = _StyledSimpleCCombo(
        state: state,
        widgetTheme: widgetTheme,
        controller: _controller,
        focusNode: _focusNode,
        items: state.items ?? [],
        enabled: isEnabled,
        readOnly: isReadOnly,
        textStyle: textStyle,
        backgroundColor: currentBg,
        borderColor: currentBorderColor,
        borderWidth: borderWidth,
        onChanged: isEnabled ? onChanged : null,
        onTextChanged: onTextChanged,
        onTextSubmitted: onTextSubmitted,
        onMouseEnter: handleMouseEnter,
        onMouseExit: handleMouseExit,
        controlHeight: height,
      );
    } else {
      // Dropdown mode
      result = MouseRegion(
        onEnter: (_) => setState(() {
          _isHovered = true;
          handleMouseEnter();
        }),
        onExit: (_) => setState(() {
          _isHovered = false;
          handleMouseExit();
        }),
        child: AnimatedContainer(
          duration: widgetTheme.animationDuration,
          decoration: BoxDecoration(
            color: currentBg,
            borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
            border: Border.all(color: currentBorderColor, width: borderWidth),
          ),
          child: _StyledDropdownCCombo(
            state: state,
            widgetTheme: widgetTheme,
            controller: _controller,
            focusNode: _focusNode,
            menuController: _menuController,
            items: state.items ?? [],
            enabled: isEnabled,
            isReadOnly: isReadOnly,
            textStyle: textStyle,
            onSelected: isEnabled ? _onDropdownSelected : null,
            isHighlighted: _isHighlighted,
            onListShown: () => scheduleMicrotask(_reportListVisible),
            onListClosed: () => scheduleMicrotask(_reportListVisible),
            controlHeight: height,
            borderWidth: borderWidth,
          ),
        ),
      );
    }

    // The field, the arrow button and the menu's own keyboard node all belong to this control: focus
    // on any of them is focus on the CCombo, so Java routes the arrow keys to it while the list is open.
    result = Focus(
      canRequestFocus: false,
      skipTraversal: true,
      onFocusChange: _handleFocusChange,
      child: result,
    );

    final wrapped = DoubleClickWordSelector(
      controller: _controller,
      focusNode: _focusNode,
      text: _controller.text,
      onWordSelected: (start, end) {
        state.selection = VPoint()
          ..x = start
          ..y = end;
        widget.sendMouseMouseDoubleClick(
          state,
          VEvent()..button = 1..count = 2..start = start..end = end,
        );
        _focusNode?.requestFocus();
      },
      child: result,
    );

    if (hasConstraints) {
      return tagSemantics(SizedBox(width: width, height: height, child: wrapped));
    }

    return tagSemantics(wrapped);
  }

  void onChanged(String? value) {
    setState(() {
      state.text = value;
      _controller.text = value ?? "";
    });
    widget.sendSelectionSelection(state, VEvent()..text = value);
    _focusNode?.requestFocus();
  }

  /// Set while the open list is handling a navigation key. Java has the key too, so it owns the
  /// selection: the menu's own highlight must neither show in the field nor be reported on Enter.
  bool _listKeyInFlight = false;

  // HardwareKeyboard handlers run before the key reaches the focused menu.
  bool _handleListKey(KeyEvent event) {
    if (event is KeyUpEvent || !_menuController.isOpen) return false;
    final key = event.logicalKey;
    if (_isListNavigationKey(key)) {
      _listKeyInFlight = true;
      scheduleMicrotask(() {
        _listKeyInFlight = false;
        _restoreSelectedText();
      });
    }
    return false;
  }

  void _restoreSelectedText() {
    if (!mounted) return;
    final text = state.text ?? '';
    if (_controller.text == text) return;
    _controller.value = TextEditingValue(
      text: text,
      selection: TextSelection.collapsed(offset: text.length),
    );
  }

  void _onDropdownSelected(String? value) {
    if (value == null || _listKeyInFlight) {
      _restoreSelectedText();
    } else {
      onChanged(value);
    }
  }

  void onTextChanged(String value) {
    state.text = value;
    recordSentText(value);
    widget.sendModifyModify(state, VEvent()..text = value);
  }

  void onTextSubmitted(String text) {
    widget.sendSelectionDefaultSelection(state, VEvent()..text = text);
    widget.sendVerifyVerify(state, VEvent()..text = text);
  }

  void handleMouseEnter() => widget.sendMouseTrackMouseEnter(state, null);
  void handleMouseExit() => widget.sendMouseTrackMouseExit(state, null);

  void _handleFocusChange(bool hasFocus) {
    if (!mounted) return;
    setState(() => _isFocused = hasFocus);
    if (hasFocus) {
      // Record the pre-edit text so a later stale echo of it is ignored rather than applied over
      // in-progress typing (no keystroke records this value, so the echo guard can't recognise it).
      seedTextEchoBaseline(_controller.text);
      widget.sendFocusFocusIn(state, null);
    } else {
      clearSentTextEchoes();
      widget.sendFocusFocusOut(state, null);
    }
  }

  @override
  void dispose() {
    HardwareKeyboard.instance.removeHandler(_handleListKey);
    _controller.dispose();
    _focusNode?.dispose();
    super.dispose();
  }
}

class _StyledDropdownCCombo extends StatelessWidget {
  final VCCombo state;
  final CComboThemeExtension widgetTheme;
  final TextEditingController controller;
  final FocusNode? focusNode;
  final MenuController menuController;
  final List<String> items;
  final bool enabled;
  final bool isReadOnly;
  final TextStyle textStyle;
  final ValueChanged<String?>? onSelected;
  final bool Function(int index) isHighlighted;
  final VoidCallback onListShown;
  final VoidCallback onListClosed;
  final double? controlHeight;
  final double borderWidth;

  const _StyledDropdownCCombo({
    required this.state,
    required this.widgetTheme,
    required this.controller,
    this.focusNode,
    required this.menuController,
    required this.items,
    required this.enabled,
    required this.isReadOnly,
    required this.textStyle,
    this.onSelected,
    required this.isHighlighted,
    required this.onListShown,
    required this.onListClosed,
    this.controlHeight,
    required this.borderWidth,
  });

  /// The width of the value on display, with a pixel of slack either side.
  double _selectedTextWidth() {
    final painter = TextPainter(
      text: TextSpan(text: state.text ?? '', style: textStyle),
      textDirection: TextDirection.ltr,
      maxLines: 1,
    )..layout();
    return painter.width + 2;
  }

  double _calculateMinWidth() {
    double maxTextWidth = 0;
    final textToMeasure = [state.text ?? '', ...items];

    for (final text in textToMeasure) {
      final textPainter = TextPainter(
        text: TextSpan(text: text, style: textStyle),
        maxLines: 1,
        textDirection: TextDirection.ltr,
      )..layout();
      if (textPainter.width > maxTextWidth) {
        maxTextWidth = textPainter.width;
      }
    }

    final horizontalPadding = widgetTheme.textFieldPadding.horizontal;
    final iconWidth = widgetTheme.iconSize;
    const extraPadding = 16.0;

    return maxTextWidth + horizontalPadding + iconWidth + extraPadding;
  }

  @override
  Widget build(BuildContext context) {
    if (isReadOnly && focusNode != null) {
      focusNode!.canRequestFocus = false;
    }

    // The border is painted by the container wrapping this widget, so a pinned outer
    // size reaches the menu already reduced by it.
    final bool hasFixedWidth = hasBounds(state.bounds);
    final double width = hasFixedWidth
        ? state.bounds!.width.toDouble() - borderWidth * 2
        : _calculateMinWidth();
    final double? fieldHeight = controlHeight == null
        ? null
        : (controlHeight! - borderWidth * 2).clamp(0.0, double.infinity);

    // The value on display keeps its whole width and the padding absorbs whatever a
    // pinned width leaves short, down to none; a preferred-width combo keeps it all.
    final double basePadding = widgetTheme.textFieldPadding.horizontal / 2;
    final double textRoom = width - widgetTheme.iconSize - _selectedTextWidth();
    final double horizontalPadding = hasFixedWidth && textRoom < basePadding * 2
        ? (textRoom > 0 ? textRoom / 2 : 0)
        : basePadding;

    // Dense/collapsed styling must apply whether or not Java has already handed us a
    // controlHeight: without it, InputDecorator reserves Material's default text-field
    // baseline space (~48-56px) even for a natural, unconstrained measurement — which is
    // exactly the pass computeSize()/the measure tool captures, so it baked that inflated
    // height into CComboSizes' MIN_HEIGHT constants instead of the compact size the widget
    // actually renders at once Java gives it real bounds.
    final EdgeInsetsGeometry effectivePadding = EdgeInsets.symmetric(
      horizontal: horizontalPadding,
      vertical: controlHeight != null ? 0 : 4.0,
    );

    final BoxConstraints? inputConstraints = controlHeight != null
        ? BoxConstraints.tightFor(height: fieldHeight)
        : null;

    final dropdown = DropdownMenu<String>(
      enabled: enabled,
      focusNode: focusNode,
      controller: controller,
      menuController: menuController,
      width: width,
      initialSelection: state.text,
      requestFocusOnTap: !isReadOnly,
      enableSearch: !isReadOnly,
      textStyle: textStyle,
      textAlign: _getTextAlign(),
      inputDecorationTheme: InputDecorationTheme(
        border: InputBorder.none,
        isDense: true,
        contentPadding: effectivePadding,
        constraints: inputConstraints,
        isCollapsed: true,
        // IconButton's 48px minimum tap target is not the width _calculateMinWidth()
        // budgets; the arrow's hit area is the Positioned.fill overlay below.
        suffixIconConstraints: BoxConstraints.tightFor(
          width: widgetTheme.iconSize,
          height: fieldHeight ?? widgetTheme.iconSize,
        ),
      ),
      menuStyle: MenuStyle(
        backgroundColor: WidgetStateProperty.all(widgetTheme.backgroundColor),
        alignment: _getMenuAlignment(),
      ),
      // The menu anchors inside the border painted around this widget; clear it on either side.
      alignmentOffset: Offset(0, borderWidth),
      trailingIcon: Icon(
        Icons.arrow_drop_down,
        color: enabled ? widgetTheme.iconColor : widgetTheme.disabledIconColor,
        size: widgetTheme.iconSize,
      ),
      onSelected: onSelected,
      dropdownMenuEntries:
          items.asMap().entries.map<DropdownMenuEntry<String>>((entry) {
        final int index = entry.key;
        final String item = entry.value;
        // Per-item tooltip fed via setData; the string may carry HTML that HtmlTooltip renders.
        final String? tooltip =
            (state.itemTooltips != null && index < state.itemTooltips!.length)
                ? state.itemTooltips![index]
                : null;
        final Widget labelWidget = Align(
          alignment: _getAlignment(),
          child: Text(item, style: textStyle),
        );
        return DropdownMenuEntry<String>(
          value: item,
          label: item,
          labelWidget: _EntryLifecycle(
            onShown: onListShown,
            onGone: onListClosed,
            child: HtmlTooltip(
              html: tooltip,
              child: labelWidget,
            ),
          ),
          style: getCComboEntryStyle(widgetTheme, textStyle, width, () => isHighlighted(index)),
        );
      }).toList(),
    );

    final arrowAreaWidth = widgetTheme.iconSize + 24.0;
    return Stack(
      children: [
        dropdown,
        Positioned.fill(
          child: Row(
            children: [
              Expanded(
                child: GestureDetector(
                  behavior: isReadOnly
                      ? HitTestBehavior.translucent
                      : HitTestBehavior.opaque,
                  onTap: isReadOnly ? null : () => focusNode?.requestFocus(),
                ),
              ),
              SizedBox(width: arrowAreaWidth),
            ],
          ),
        ),
      ],
    );
  }

  TextAlign _getTextAlign() {
    if (state.alignment == SWT.CENTER) return TextAlign.center;
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return TextAlign.right;
    }
    return TextAlign.left;
  }

  Alignment _getAlignment() {
    if (state.alignment == SWT.CENTER) return Alignment.center;
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return Alignment.centerRight;
    }
    return Alignment.centerLeft;
  }

  AlignmentGeometry? _getMenuAlignment() {
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return AlignmentDirectional.topEnd;
    }
    return null;
  }
}

class _StyledSimpleCCombo extends StatelessWidget {
  final VCCombo state;
  final CComboThemeExtension widgetTheme;
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final bool enabled;
  final bool readOnly;
  final TextStyle textStyle;
  final Color backgroundColor;
  final Color borderColor;
  final double borderWidth;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final ValueChanged<String>? onTextSubmitted;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final double? controlHeight;

  const _StyledSimpleCCombo({
    required this.state,
    required this.widgetTheme,
    required this.controller,
    this.focusNode,
    required this.items,
    required this.enabled,
    required this.readOnly,
    required this.textStyle,
    required this.backgroundColor,
    required this.borderColor,
    required this.borderWidth,
    this.onChanged,
    this.onTextChanged,
    this.onTextSubmitted,
    this.onMouseEnter,
    this.onMouseExit,
    this.controlHeight,
  });

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => onMouseEnter?.call(),
      onExit: (_) => onMouseExit?.call(),
      child: Container(
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
          border: Border.all(color: borderColor, width: borderWidth),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Text field
            Padding(
              padding: widgetTheme.textFieldPadding,
              child: TextField(
                controller: controller,
                focusNode: focusNode,
                enabled: enabled,
                readOnly: readOnly,
                textAlign: _getTextAlign(),
                style: textStyle,
                decoration: const InputDecoration.collapsed(hintText: ''),
                maxLength: state.textLimit,
                minLines: controlHeight != null ? 1 : null,
                maxLines: 1,
                onChanged: readOnly ? null : onTextChanged,
                onSubmitted: readOnly ? null : onTextSubmitted,
              ),
            ),
            // Divider
            if (items.isNotEmpty)
              Divider(
                height: widgetTheme.dividerHeight,
                thickness: widgetTheme.dividerThickness,
                color: widgetTheme.dividerColor,
              ),
            // Items list
            ...items.map((item) {
              final bool isSelected = item == state.text;
              return InkWell(
                hoverColor: getCComboSimpleItemHoverColor(widgetTheme),
                onTap: enabled ? () => onChanged?.call(item) : null,
                child: Container(
                  height: widgetTheme.itemHeight,
                  padding: widgetTheme.textFieldPadding,
                  color: getCComboItemBackgroundColor(widgetTheme, isSelected),
                  alignment: _getAlignment(),
                  child: Text(item, style: textStyle),
                ),
              );
            }),
          ],
        ),
      ),
    );
  }

  TextAlign _getTextAlign() {
    if (state.alignment == SWT.CENTER) return TextAlign.center;
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return TextAlign.right;
    }
    return TextAlign.left;
  }

  Alignment _getAlignment() {
    if (state.alignment == SWT.CENTER) return Alignment.center;
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return Alignment.centerRight;
    }
    return Alignment.centerLeft;
  }
}

/// Reports a list entry entering and leaving the tree, which is the list opening and closing.
class _EntryLifecycle extends StatefulWidget {
  final VoidCallback onShown;
  final VoidCallback onGone;
  final Widget child;

  const _EntryLifecycle({required this.onShown, required this.onGone, required this.child});

  @override
  State<_EntryLifecycle> createState() => _EntryLifecycleState();
}

class _EntryLifecycleState extends State<_EntryLifecycle> {
  @override
  void initState() {
    super.initState();
    widget.onShown();
  }

  @override
  void dispose() {
    widget.onGone();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
