import 'package:flutter/foundation.dart' show listEquals;
import 'dart:async';

import 'package:flutter/foundation.dart' show TargetPlatform, defaultTargetPlatform;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../custom/html_tooltip.dart';
import '../gen/ccombo.dart';
import '../gen/event.dart';
import '../gen/point.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart' show WireKey;
import '../impl/composite_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/ccombo_theme_extension.dart';
import '../theme/theme_settings/ccombo_theme_settings.dart';
import 'utils/font_utils.dart';
import 'utils/cached_text.dart';
import 'utils/text_utils.dart';
import 'utils/at_rest.dart';
import 'utils/widget_utils.dart';
import 'utils/pending_text_echoes.dart';
import 'key_forwarding.dart';
import 'utils/quiet_animations.dart';

class CComboImpl<T extends CComboSwt, V extends VCCombo>
    extends CompositeImpl<T, V> with PendingTextEchoes, AtRestHandover {
  late TextEditingController _controller;
  FocusNode? _focusNode;
  /// The arrow's node, where Tab leaves the focus of a READ_ONLY CCombo. Enter and Space there are
  /// the field's keys, which Java answers (Enter fires DefaultSelection): they must not press the
  /// arrow and open the list, as a native CCombo's field does not.
  late final FocusNode _arrowFocusNode = FocusNode(
    onKeyEvent: (node, event) => _isFieldKey(event.logicalKey)
        ? KeyEventResult.handled
        : KeyEventResult.ignored,
  );

  static bool _isFieldKey(LogicalKeyboardKey key) =>
      key == LogicalKeyboardKey.enter ||
      key == LogicalKeyboardKey.numpadEnter ||
      key == LogicalKeyboardKey.space;

  final MenuController _menuController = MenuController();
  bool _isFocused = false;
  /// The last `listVisible` Java sent or was told. Only a change from it is a command to open or close.
  bool? _lastJavaListVisible;
  /// What Java last told the list to be, until the list gets there; its state before then is not news.
  bool? _javaListTarget;
  /// Whether the list was open when last reported, whatever Java was told.
  bool _listShown = false;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _focusNode = FocusNode();
    HardwareKeyboard.instance.addHandler(_handleListKey);
    _lastJavaListVisible = state.listVisible;
  }

  /// Its field keeps the wheel on every native, so the parent reports none.
  @override
  bool get keepsWheel => true;

  int get _selectedIndex => (state.items ?? const <String>[]).indexOf(state.text ?? '');

  // The list is an SWT List: it highlights the selection only, and the pointer over it moves nothing.
  bool _isHighlighted(int index) => index == _selectedIndex;

  /// The menu's entries, kept while what they are made of is unchanged; see [_DropdownEntries].
  final _DropdownEntries _entries = _DropdownEntries();

  /// Whether the [DropdownMenu] is mounted. At rest the field is drawn on its own, which rebuilds at a
  /// small fraction of the cost: DropdownMenu builds a button for every entry on every build, open or
  /// not. It is mounted while anything could interact with it -- the pointer over it, focus in it, its
  /// list open or about to open -- so input always reaches the real one.
  bool get _live =>
      _isFocused || hovered || _menuController.isOpen || _javaListTarget == true || handingOver || forcedLive;

  /// The field's place in focus traversal while at rest, and the parent of the menu's own nodes once
  /// it is mounted, so focus never leaves the control while one replaces the other.
  final FocusNode _restFocus = FocusNode(debugLabel: 'CCombo');

  bool get _isReadOnly => StyleBits(state.style).has(SWT.READ_ONLY);

  TextStyle _fieldTextStyle(BuildContext context, CComboThemeExtension widgetTheme) => getTextStyle(
        context: context,
        font: state.font,
        textColor: !(state.enabled ?? true)
            ? widgetTheme.disabledTextColor
            : getForegroundColor(
                foreground: state.foreground,
                defaultColor: widgetTheme.textColor,
                context: context,
                minContrast: widgetTheme.minTextContrast,
              ),
        baseTextStyle: widgetTheme.textStyle,
      );

  double _borderWidth(CComboThemeExtension widgetTheme) =>
      StyleBits(state.style).has(SWT.BORDER) ? 3.0 : widgetTheme.borderWidth;

  /// The text field, list and arrow are SWT widgets of their own, but what they show is in the
  /// CCombo's own state.
  @override
  bool get drawsHeldWidgets => false;

  /// What the field shows through its controller: a change naming only these is handed to the
  /// controller, which redraws the text and nothing else.
  static const _controllerProperties = {WireKey.k_CCombo_text, WireKey.k_CCombo_selection};

  /// [_controllerProperties] and what only the menu shows. At rest the menu is not mounted, and is
  /// built from the current state when it is.
  static const _atRestProperties = {
    WireKey.k_CCombo_text,
    WireKey.k_CCombo_selection,
    WireKey.k_CCombo_items,
    WireKey.k_CCombo_itemTooltips,
  };

  /// The theme and field text style the last build used. Only a build changes them, so a delivery
  /// that leaves the build alone can read them here.
  CComboThemeExtension? _builtTheme;
  TextStyle? _builtTextStyle;

  @override
  void setValue(V value) {
    final change = lastChange;
    // A part changed, and the CCombo mirrors its parts in its own state.
    if (change != null && change.ofHeld) return;
    final unbuilt = _live ? _controllerProperties : _atRestProperties;
    // An open list highlights the selection, which only a rebuild moves.
    if (mounted &&
        change != null &&
        change.isOnly(unbuilt) &&
        !_menuController.isOpen &&
        _keepsLayout(state.text ?? '')) {
      extraSetState();
      return;
    }
    super.setValue(value);
  }

  /// Whether showing [text] instead of what is shown leaves the field laid out as it is: a field
  /// without bounds is as wide as its value, and a pinned one gives up padding for a value too wide
  /// for it.
  bool _keepsLayout(String text) {
    final styleBits = StyleBits(state.style);
    final widgetTheme = _builtTheme;
    final style = _builtTextStyle;
    if (!hasBounds(state.bounds) || styleBits.has(SWT.SIMPLE) || widgetTheme == null || style == null) {
      return false;
    }
    final width = state.bounds!.width - _borderWidth(widgetTheme) * 2;
    final basePadding = widgetTheme.textFieldPadding.horizontal / 2;
    bool fullPadding(String t) =>
        width - widgetTheme.iconSize - (singleLineTextWidth(t, style) + 2) >= basePadding * 2;
    return fullPadding(_controller.text) && fullPadding(text);
  }

  /// A press on the field at rest, which only a pointer that did not hover first can make: mounts
  /// the menu and does what a tap does on it. On an editable field the text area takes focus and
  /// the arrow's area opens the list as well; a read-only field opens the list wherever it is tapped.
  void _tapAtRest(bool opensList) => handOver(() {
        _focusInside();
        if (opensList && !_menuController.isOpen) _menuController.open();
        handedOver();
        setState(() {});
      });

  /// Moves focus from the field at rest into the menu: its text field, or on a read-only field the
  /// arrow, which is where traversal stops on the menu.
  void _focusInside() {
    final FocusNode? node = _isReadOnly
        ? _restFocus.descendants.where((n) => n.canRequestFocus && !n.skipTraversal).firstOrNull
        : _focusNode;
    node?.requestFocus();
  }

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
    if (open != _listShown) {
      _listShown = open;
      // A list that closed may leave nothing keeping the menu mounted.
      if (!open) setState(() {});
    }
    if (_javaListTarget != null) {
      if (open != _javaListTarget) return;
      _javaListTarget = null;
    }
    // The anchor rebuilds for more than an open or a close: only a change reaches Java.
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
    final bool isEnabled = state.enabled ?? true;

    final bool isActive = _isFocused || hovered;
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

    final textStyle = _fieldTextStyle(context, widgetTheme);
    _builtTheme = widgetTheme;
    _builtTextStyle = textStyle;
    final borderWidth = _borderWidth(widgetTheme);

    final hasConstraints = hasBounds(state.bounds);
    final double? width = hasConstraints
        ? state.bounds!.width.toDouble()
        : null;
    final double? height = hasConstraints
        ? state.bounds!.height.toDouble()
        : null;

    Widget result = MouseRegion(
      onEnter: (_) => setHovered(true),
      onExit: (_) => setHovered(false),
      child: QuietAnimatedContainer(
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
          arrowFocusNode: _arrowFocusNode,
          menuController: _menuController,
          items: state.items ?? [],
          enabled: isEnabled,
          isReadOnly: isReadOnly,
          textStyle: textStyle,
          onSelected: isEnabled ? _onDropdownSelected : null,
          isHighlighted: _isHighlighted,
          reportListVisible: () => scheduleMicrotask(_reportListVisible),
          controlHeight: height,
          borderWidth: borderWidth,
          entries: _entries,
          live: _live,
          restFocus: _restFocus,
          onTapAtRest: isEnabled ? _tapAtRest : null,
        ),
      ),
    );

    // The field, the arrow button and the menu's own keyboard node all belong to this control: focus
    // on any of them is focus on the CCombo, so Java routes the arrow keys to it while the list is open.
    result = Focus(
      canRequestFocus: false,
      skipTraversal: true,
      onFocusChange: _handleFocusChange,
      child: result,
    );

    // Its own pointer events, through the hover arbiter like every other control: SWT's CCombo
    // forwards its field's and its arrow's mouse events as its own.
    result = trackPointer(result);

    final wrapped = DoubleClickWordSelector(
      controller: _controller,
      focusNode: _focusNode,
      onWordSelected: (start, end) {
        // The MouseDoubleClick itself is trackPointer's, sent on the second press.
        state.selection = VPoint()
          ..x = start
          ..y = end;
      },
      child: result,
    );

    if (hasConstraints) {
      return tagSemantics(SizedBox(width: width, height: height, child: wrapped));
    }

    return tagSemantics(wrapped);
  }

  /// A press in an editable field focuses the field itself, as SWT's CCombo gives its Text the
  /// focus: the composite's own surface would take it from the field on every press. A read-only
  /// field takes no focus, so there the surface does.
  @override
  void takeFocusOnPress() {
    if ((state.style & SWT.READ_ONLY) != 0) {
      super.takeFocusOnPress();
    } else {
      _focusNode?.requestFocus();
    }
  }

  /// Every native's field takes the focus on a right press too.
  @override
  bool get takesFocusOnSecondaryPress => (state.style & SWT.READ_ONLY) == 0;

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

  /// Set from an Enter in this CCombo until its release. Java answers that Enter, open list or not;
  /// the field submits the entry the menu last highlighted, which is no Selection.
  bool _enterHeld = false;

  // HardwareKeyboard handlers run before the key reaches the focused menu.
  bool _handleListKey(KeyEvent event) {
    final key = event.logicalKey;
    if (key == LogicalKeyboardKey.enter || key == LogicalKeyboardKey.numpadEnter) {
      _enterHeld = event is! KeyUpEvent && _isFocused;
    }
    if (event is KeyUpEvent || !_menuController.isOpen) return false;
    if (_isTypedLetter(event)) {
      _typeAhead(event.character!, event.timeStamp);
      // The letter is the list's, where native focus is: it never edits the field.
      return true;
    }
    if (_isListNavigationKey(key)) {
      _listKeyInFlight = true;
      scheduleMicrotask(() {
        _listKeyInFlight = false;
        _restoreSelectedText();
      });
    }
    return false;
  }

  static bool _isTypedLetter(KeyEvent event) {
    final keyboard = HardwareKeyboard.instance;
    final character = event.character;
    return character != null &&
        character.length == 1 &&
        character.codeUnitAt(0) > 0x20 &&
        character.codeUnitAt(0) != 0x7f &&
        !keyboard.isAltPressed &&
        !keyboard.isControlPressed &&
        !keyboard.isMetaPressed;
  }

  String _typed = '';
  Duration? _lastTyped;

  // NSTableView starts a new search once the user pauses.
  static const _typeSelectPause = Duration(seconds: 1);

  /// The popup List's own type-ahead. Win32: the next item starting with the letter, wrapping.
  /// Cocoa: the first item starting with the letters typed so far. GTK: none.
  void _typeAhead(String character, Duration timeStamp) {
    final items = state.items ?? const <String>[];
    final current = _selectedIndex;
    int? target;
    switch (defaultTargetPlatform) {
      case TargetPlatform.linux:
        return;
      case TargetPlatform.macOS || TargetPlatform.iOS:
        final last = _lastTyped;
        if (last == null || timeStamp - last > _typeSelectPause) _typed = '';
        _lastTyped = timeStamp;
        _typed += character.toLowerCase();
        final i = items.indexWhere((item) => item.toLowerCase().startsWith(_typed));
        if (i >= 0) target = i;
      default:
        final letter = character.toLowerCase();
        for (int step = 1; step <= items.length && target == null; step++) {
          final i = (current + step) % items.length;
          if (items[i].toLowerCase().startsWith(letter)) target = i;
        }
    }
    if (target == null || target == current) return;
    final value = items[target];
    setState(() {
      state.text = value;
      _controller.text = value;
    });
    widget.sendSelectionSelection(state, VEvent()..text = value);
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
    if (value == null || _listKeyInFlight || _enterHeld) {
      _restoreSelectedText();
    } else {
      onChanged(value);
    }
  }

  void _handleFocusChange(bool hasFocus) {
    if (!mounted) return;
    setState(() => _isFocused = hasFocus);
    // Traversal reached the field at rest: the menu mounts with this build, and takes the focus once
    // it has.
    if (hasFocus && _restFocus.hasPrimaryFocus) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && _restFocus.hasPrimaryFocus) _focusInside();
      });
    }
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
    _restFocus.dispose();
    _arrowFocusNode.dispose();
    super.dispose();
  }
}

/// What an editable line reserves after its text for the caret: the default cursor width (2) plus
/// RenderEditable's gap (1).
const double _caretRoom = 3;

/// The gap the input decorator leaves between the text field and a suffix icon.
const double _suffixGap = 4;

class _StyledDropdownCCombo extends StatelessWidget {
  final VCCombo state;
  final CComboThemeExtension widgetTheme;
  final TextEditingController controller;
  final FocusNode? focusNode;
  final FocusNode arrowFocusNode;
  final MenuController menuController;
  final List<String> items;
  final bool enabled;
  final bool isReadOnly;
  final TextStyle textStyle;
  final ValueChanged<String?>? onSelected;
  final bool Function(int index) isHighlighted;
  /// Tells Java whether the list is open. Called on every rebuild of the menu anchor, which every
  /// open and close causes; the report itself drops a state Java already has.
  final VoidCallback reportListVisible;
  final double? controlHeight;
  final double borderWidth;
  final _DropdownEntries entries;
  final bool live;
  final FocusNode restFocus;
  final void Function(bool opensList)? onTapAtRest;

  const _StyledDropdownCCombo({
    required this.state,
    required this.widgetTheme,
    required this.controller,
    this.focusNode,
    required this.arrowFocusNode,
    required this.menuController,
    required this.items,
    required this.enabled,
    required this.isReadOnly,
    required this.textStyle,
    this.onSelected,
    required this.isHighlighted,
    required this.reportListVisible,
    this.controlHeight,
    required this.borderWidth,
    required this.entries,
    required this.live,
    required this.restFocus,
    this.onTapAtRest,
  });

  /// The width of the value on display, with a pixel of slack either side.
  double _selectedTextWidth() => singleLineTextWidth(state.text ?? '', textStyle) + 2;

  double _calculateMinWidth() {
    double maxTextWidth = 0;
    final textToMeasure = [state.text ?? '', ...items];

    for (final text in textToMeasure) {
      final width = singleLineTextWidth(text, textStyle);
      if (width > maxTextWidth) maxTextWidth = width;
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

    // Live or at rest, under the same node: see CComboImpl._restFocus.
    Widget underRestFocus(Widget child) => Focus(
          focusNode: restFocus,
          canRequestFocus: enabled,
          skipTraversal: live,
          child: child,
        );

    // Taps this far from the end reach the menu itself, which opens the list; see the overlay below.
    final arrowAreaWidth = widgetTheme.iconSize + 24.0;

    if (!live) {
      return underRestFocus(_atRest(context, width, fieldHeight, effectivePadding, arrowAreaWidth));
    }

    final dropdown = DropdownMenu<String>(
      enabled: enabled,
      focusNode: focusNode,
      trailingIconFocusNode: arrowFocusNode,
      controller: controller,
      menuController: menuController,
      // The anchor's builder runs on every open and close, entries or not: an empty list opens too.
      decorationBuilder: (context, controller) {
        reportListVisible();
        return const InputDecoration();
      },
      expandedInsets: EdgeInsets.zero,
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
      dropdownMenuEntries: entries.of(
          items, state.itemTooltips, textStyle, state.alignment, widgetTheme, width, () =>
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
          labelWidget: HtmlTooltip(
            html: tooltip,
            child: labelWidget,
          ),
          style: getCComboEntryStyle(widgetTheme, textStyle, width, () => isHighlighted(index)),
        );
      }).toList()),
    );

    return underRestFocus(Stack(
      children: [
        SizedBox(width: width, child: dropdown),
        Positioned.fill(
          // Part of the field for its tap-outside check: a press here is on the field, and a mouse
          // press outside it would unfocus the field until the release focused it again.
          child: TextFieldTapRegion(
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
        ),
      ],
    ));
  }

  /// The closed field as the menu draws it -- its text and the arrow -- without the menu: what a
  /// CCombo nobody is interacting with is.
  Widget _atRest(
      BuildContext context, double width, double? fieldHeight, EdgeInsetsGeometry padding, double arrowAreaWidth) {
    // A disabled menu draws its text in its own disabled colour, whatever the style says.
    final style = enabled
        ? textStyle
        : textStyle.copyWith(
            color: DropdownMenuTheme.of(context).disabledColor ??
                Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.38));
    return Semantics(
      container: true,
      button: isReadOnly,
      textField: !isReadOnly,
      readOnly: isReadOnly,
      enabled: enabled,
      expanded: false,
      onTap: onTapAtRest == null ? null : () => onTapAtRest!(true),
      child: ExcludeSemantics(
        // On the press, not the tap: a press that arrives without a hover first brings one, which
        // mounts the menu before the release and would take the tap with it.
        child: Listener(
          behavior: HitTestBehavior.opaque,
          onPointerDown: onTapAtRest == null
              ? null
              : (event) => onTapAtRest!(isReadOnly || event.localPosition.dx >= width - arrowAreaWidth),
          child: SizedBox(
            width: width,
            height: fieldHeight,
            child: Row(children: [
              Expanded(
                // The text field ends where the decorator's gap before the arrow begins, and clips
                // there; within it, a short text is aligned clear of the room kept for the caret, and a
                // long one shows its start.
                child: Padding(
                  padding: padding.resolve(TextDirection.ltr).copyWith(right: _suffixGap),
                  child: ClipRect(
                    child: Padding(
                      padding: const EdgeInsets.only(right: _caretRoom),
                      child: Align(
                        alignment: Alignment(_getAlignment().x, 0),
                        // Without a pinned height the field is as tall as its line, as the menu's is.
                        heightFactor: fieldHeight == null ? 1 : null,
                        child: ValueListenableBuilder<TextEditingValue>(
                          valueListenable: controller,
                          builder: (context, value, _) => Semantics(
                            value: value.text,
                            // The same few texts shown by many combos are shaped once between them.
                            child: ExcludeSemantics(
                              child: ColorCachedText(
                                value.text,
                                style: style,
                                maxLines: 1,
                                softWrap: false,
                                overflow: TextOverflow.visible,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
              SizedBox(
                width: widgetTheme.iconSize,
                height: fieldHeight ?? widgetTheme.iconSize,
                child: Center(
                  child: Icon(
                    Icons.arrow_drop_down,
                    color: enabled ? widgetTheme.iconColor : widgetTheme.disabledIconColor,
                    size: widgetTheme.iconSize,
                  ),
                ),
              ),
            ]),
          ),
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

  AlignmentGeometry? _getMenuAlignment() {
    if (state.alignment == SWT.RIGHT || state.alignment == SWT.TRAIL) {
      return AlignmentDirectional.topEnd;
    }
    return null;
  }
}

/// The entries a CCombo hands its [DropdownMenu], the same list for as long as the items, their
/// tooltips, the style, the alignment, the theme and the width stay the same.
///
/// DropdownMenu compares its entries by identity: a new list, even an equal one, makes it rebuild
/// every entry's button under a new key, which a CCombo would otherwise pay on every update.
class _DropdownEntries {
  List<String>? _items;
  List<String?>? _tooltips;
  TextStyle? _style;
  int? _alignment;
  CComboThemeExtension? _theme;
  double? _width;
  List<DropdownMenuEntry<String>>? _entries;

  List<DropdownMenuEntry<String>> of(
    List<String> items,
    List<String?>? tooltips,
    TextStyle style,
    int? alignment,
    CComboThemeExtension theme,
    double width,
    List<DropdownMenuEntry<String>> Function() build,
  ) {
    final held = _entries;
    if (held != null &&
        listEquals(_items, items) &&
        listEquals(_tooltips, tooltips) &&
        _style == style &&
        _alignment == alignment &&
        identical(_theme, theme) &&
        _width == width) {
      return held;
    }
    _items = List.of(items);
    _tooltips = tooltips == null ? null : List.of(tooltips);
    _style = style;
    _alignment = alignment;
    _theme = theme;
    _width = width;
    return _entries = build();
  }
}
