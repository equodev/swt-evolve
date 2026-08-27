import 'package:flutter/widgets.dart';
import 'package:flutter/material.dart' show Icons, Icon, Divider, Theme;

import '../gen/combo.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/combo_theme_extension.dart';
import '../theme/theme_settings/combo_theme_settings.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'utils/pending_text_echoes.dart';
import 'utils/pointer.dart';

class ComboImpl<T extends ComboSwt, V extends VCombo>
    extends CompositeImpl<T, V> with PendingTextEchoes {
  late TextEditingController _controller;
  final FocusNode _focusNode = FocusNode();
  final OverlayPortalController _overlayController = OverlayPortalController();
  final LayerLink _layerLink = LayerLink();
  bool _isFocused = false;
  final bool _isHovered = false;
  /// The last `listVisible` Java sent. The dropdown is otherwise local UI state Java only drives
  /// through `Combo.setListVisible`, so only a change between two pushes is a command.
  bool? _lastJavaListVisible;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _focusNode.addListener(_handleFocusChange);
    _lastJavaListVisible = state.listVisible;
  }

  Size _maxTextSize(TextStyle style) {
    final List<String> allStrings = [state.text ?? "", ...(state.items ?? [])];

    double maxTextWidth = 0;
    double maxTextHeight = 0;

    for (var s in allStrings) {
      final painter = TextPainter(
        text: TextSpan(text: s, style: style),
        textDirection: TextDirection.ltr,
        maxLines: 1,
      )..layout();

      if (painter.width > maxTextWidth) maxTextWidth = painter.width + 2;
      if (painter.height > maxTextHeight) maxTextHeight = painter.height + 2;
    }

    return Size(maxTextWidth, maxTextHeight);
  }

  /// The arrow's own cell in the Row: the icon plus the gap that separates it
  /// from the text.
  double _arrowCell(ComboThemeExtension theme, bool isSimple) =>
      isSimple ? 0 : theme.iconSpacing + theme.iconSize;

  /// The width of the value on display, with the slack [_maxTextSize] leaves.
  double _selectedTextWidth(TextStyle style) {
    final painter = TextPainter(
      text: TextSpan(text: state.text ?? "", style: style),
      textDirection: TextDirection.ltr,
      maxLines: 1,
    )..layout();
    return painter.width + 2;
  }

  Size _calculatePreferredSize(
    Size textSize,
    ComboThemeExtension theme,
    bool isSimple,
  ) {
    // The border insets the Row (Container pads by decoration.padding), and the
    // arrow cell consumes iconSpacing + iconSize; both must be part of the
    // preferred width or the longest item is clipped by the arrow.
    final double width =
        textSize.width +
        theme.textFieldPadding.horizontal +
        theme.borderWidth * 2 +
        _arrowCell(theme, isSimple);
    final double height = textSize.height + theme.textFieldPadding.vertical;

    return Size(width, height);
  }

  /// The text-field padding is a fixed inset, so a size an application pins below
  /// the preferred one (GridData.heightHint / widthHint) leaves EditableText a
  /// viewport of a couple of pixels and the glyphs are cut off. The text keeps
  /// its full extent -- a whole line height, and the whole width of the value on
  /// display -- and the padding absorbs the deficit, down to none at all.
  ///
  /// [verticalRoom] and [horizontalRoom] are what the pinned size leaves the text
  /// on each axis; null where nothing is pinned.
  EdgeInsets _fitTextPadding(
    EdgeInsets padding, {
    double? verticalRoom,
    double? horizontalRoom,
  }) {
    EdgeInsets fitted = padding;
    if (verticalRoom != null && verticalRoom < padding.vertical) {
      final double half = verticalRoom > 0 ? verticalRoom / 2 : 0;
      fitted = fitted.copyWith(top: half, bottom: half);
    }
    if (horizontalRoom != null && horizontalRoom < padding.horizontal) {
      final double half = horizontalRoom > 0 ? horizontalRoom / 2 : 0;
      fitted = fitted.copyWith(left: half, right: half);
    }
    return fitted;
  }

  @override
  void extraSetState() {
    final String newText = state.text ?? "";
    // Ignore a stale echo of our own in-flight typing (see PendingTextEchoes); a value we
    // never sent is a genuine external change and still updates the controller below.
    if (!isStaleTextEcho(newText, _controller.text) &&
        _controller.text != newText) {
      _controller.text = newText;
      _controller.selection = TextSelection.collapsed(
        offset: _controller.text.length,
      );
    }
    final bool? newVisible = state.listVisible;
    if (newVisible != _lastJavaListVisible) {
      _lastJavaListVisible = newVisible;
      newVisible == true ? _overlayController.show() : _overlayController.hide();
    }
  }

  void _handleFocusChange() {
    if (!mounted) return;
    setState(() => _isFocused = _focusNode.hasFocus);
    if (_focusNode.hasFocus) {
      // Record the pre-edit text so a later stale echo of it is ignored rather than applied over
      // in-progress typing (no keystroke records this value, so the echo guard can't recognise it).
      seedTextEchoBaseline(_controller.text);
      widget.sendFocusFocusIn(state, null);
    } else {
      clearSentTextEchoes();
      widget.sendFocusFocusOut(state, null);
    }
  }

  /// Forwards each edit of the text field as a Modify, the way native SWT does: anything that
  /// reads Combo.getText() from a Modify/Key listener (JFace field-editor validation) would
  /// otherwise keep seeing the pre-edit value for as long as the user only types.
  void _handleTextChanged(String value) {
    state.text = value;
    recordSentText(value);
    widget.sendModifyModify(state, VEvent()..text = value);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<ComboThemeExtension>()!;
    final bool isEnabled = state.enabled ?? false;
    final styleBits = StyleBits(state.style);
    final bool isSimple = styleBits.has(SWT.SIMPLE);
    final bool isReadOnly = styleBits.has(SWT.READ_ONLY);
    final bool hasFixedSize = hasBounds(state.bounds);

    final Color bgColor = getComboBackgroundColor(
      context,
      state,
      theme,
      enabled: isEnabled,
    );
    final Color textColor = getComboTextColor(context, state, theme, enabled: isEnabled);
    final Color borderColor = isEnabled && (_isFocused || _isHovered)
        ? theme.borderColor
        : (isEnabled ? theme.dividerColor : theme.disabledBorderColor);
    final Color iconColor = getComboIconColor(theme, enabled: isEnabled);

    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: theme.textStyle,
    );

    final Size textSize = _maxTextSize(textStyle);
    final Size preferredSize = _calculatePreferredSize(
      textSize,
      theme,
      isSimple,
    );

    final double width = hasFixedSize
        ? state.bounds!.width.toDouble()
        : preferredSize.width;

    final double? height = hasFixedSize
        ? state.bounds!.height.toDouble()
        : (isSimple ? null : preferredSize.height);

    final EdgeInsets textPadding = _fitTextPadding(
      theme.textFieldPadding,
      verticalRoom: height == null
          ? null
          : height - theme.borderWidth * 2 - textSize.height,
      // Only the arrow cell and the border stand between the pinned width and
      // the text; the preferred width is free to keep its full padding.
      horizontalRoom: hasFixedSize
          ? width -
                theme.borderWidth * 2 -
                _arrowCell(theme, isSimple) -
                _selectedTextWidth(textStyle)
          : null,
    );

    final Widget content = isSimple
        ? _SimpleComboLayout(
            state: state,
            theme: theme,
            controller: _controller,
            focusNode: _focusNode,
            textStyle: textStyle,
            isEnabled: isEnabled,
            isReadOnly: isReadOnly,
            bgColor: bgColor,
            borderColor: borderColor,
            onSelected: _onItemSelected,
            onTextChanged: _handleTextChanged,
            hasFixedSize: hasFixedSize,
          )
        : _DropdownComboLayout(
            state: state,
            theme: theme,
            controller: _controller,
            focusNode: _focusNode,
            textStyle: textStyle,
            isEnabled: isEnabled,
            isReadOnly: isReadOnly,
            bgColor: bgColor,
            borderColor: borderColor,
            iconColor: iconColor,
            overlayController: _overlayController,
            layerLink: _layerLink,
            onSelected: _onItemSelected,
            onTextChanged: _handleTextChanged,
            onToggleOverlay: _toggleOverlay,
            width: width,
            textPadding: textPadding,
          );

    return tagSemantics(DoubleClickWordSelector(
      controller: _controller,
      focusNode: _focusNode,
      text: _controller.text,
      onWordSelected: (start, end) {
        widget.sendMouseMouseDoubleClick(
          state,
          VEvent()
            ..button = 1
            ..count = 2
            ..start = start
            ..end = end,
        );
        _focusNode.requestFocus();
      },
      child: SizedBox(width: width, height: height, child: content),
    ));
  }

  /// Toggles the dropdown keeping [VCombo.listVisible] truthful, so a value push that reuses the
  /// state object cannot resurrect a stale flag.
  void _toggleOverlay() {
    final bool showing = !_overlayController.isShowing;
    setState(() {
      state.listVisible = showing;
    });
    showing ? _overlayController.show() : _overlayController.hide();
  }

  void _onItemSelected(String? value) {
    setState(() {
      state.text = value;
      _controller.text = value ?? "";
      state.listVisible = false;
      _overlayController.hide();
    });
    widget.sendSelectionSelection(state, VEvent()..text = value);
  }

  @override
  void dispose() {
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }
}

class _DropdownComboLayout extends StatelessWidget {
  final VCombo state;
  final ComboThemeExtension theme;
  final TextEditingController controller;
  final FocusNode focusNode;
  final TextStyle textStyle;
  final bool isEnabled, isReadOnly;
  final Color bgColor, borderColor, iconColor;
  final OverlayPortalController overlayController;
  final LayerLink layerLink;
  final ValueChanged<String?> onSelected;
  final ValueChanged<String> onTextChanged;
  final VoidCallback onToggleOverlay;
  final double width;
  final EdgeInsets textPadding;

  const _DropdownComboLayout({
    required this.state,
    required this.theme,
    required this.controller,
    required this.focusNode,
    required this.textStyle,
    required this.isEnabled,
    required this.isReadOnly,
    required this.bgColor,
    required this.borderColor,
    required this.iconColor,
    required this.overlayController,
    required this.layerLink,
    required this.onSelected,
    required this.onTextChanged,
    required this.onToggleOverlay,
    required this.width,
    required this.textPadding,
  });

  @override
  Widget build(BuildContext context) {
    return OverlayPortal(
      controller: overlayController,
      overlayChildBuilder: (_) => _buildOverlay(),
      child: CompositedTransformTarget(
        link: layerLink,
        child: Container(
          decoration: BoxDecoration(
            color: bgColor,
            borderRadius: BorderRadius.circular(theme.borderRadius),
            border: Border.all(color: borderColor, width: theme.borderWidth),
          ),
          child: Row(
            children: [
              Expanded(
                child: GestureDetector(
                  behavior: HitTestBehavior.opaque,
                  onTap: isEnabled && !isReadOnly
                      ? () => focusNode.requestFocus()
                      : (isEnabled ? onToggleOverlay : null),
                  child: Padding(
                    padding: textPadding,
                    child: IgnorePointer(
                      ignoring: isReadOnly,
                      child: EditableText(
                        controller: controller,
                        focusNode: focusNode,
                        readOnly: isReadOnly,
                        onChanged: onTextChanged,
                        style: textStyle,
                        cursorColor: textStyle.color ?? theme.textColor,
                        backgroundCursorColor: bgColor,
                        selectionColor: DefaultSelectionStyle.of(context).selectionColor ??
                            Theme.of(context).colorScheme.primary.withOpacity(0.4),
                      ),
                    ),
                  ),
                ),
              ),
              GestureDetector(
                behavior: HitTestBehavior.opaque,
                onTap: isEnabled ? onToggleOverlay : null,
                child: Padding(
                  // Right-only: the text field's own right padding already
                  // separates text from arrow; a left inset here eats viewport
                  // width the preferred size doesn't account for.
                  padding: EdgeInsets.only(right: theme.iconSpacing),
                  child: Icon(
                    Icons.arrow_drop_down,
                    color: iconColor,
                    size: theme.iconSize,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildOverlay() {
    return Align(
      alignment: Alignment.topLeft,
      child: CompositedTransformFollower(
        link: layerLink,
        targetAnchor: Alignment.bottomLeft,
        followerAnchor: Alignment.topLeft,
        child: pointerInterceptor(TapRegion(
          onTapOutside: (_) => overlayController.hide(),
          child: Container(
            width: width,
            decoration: BoxDecoration(
              color: theme.backgroundColor,
              borderRadius: BorderRadius.circular(theme.borderRadius),
              border: Border.all(
                color: theme.dividerColor,
                width: theme.borderWidth,
              ),
            ),
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: (state.items ?? [])
                    .map(
                      (item) => _ComboItem(
                        text: item,
                        isSelected: item == state.text,
                        theme: theme,
                        textStyle: textStyle,
                        onTap: () => onSelected(item),
                      ),
                    )
                    .toList(),
              ),
            ),
          ),
        )),
      ),
    );
  }
}

class _SimpleComboLayout extends StatelessWidget {
  final VCombo state;
  final ComboThemeExtension theme;
  final TextEditingController controller;
  final FocusNode focusNode;
  final TextStyle textStyle;
  final bool isEnabled, isReadOnly, hasFixedSize;
  final Color bgColor, borderColor;
  final ValueChanged<String?> onSelected;
  final ValueChanged<String> onTextChanged;

  const _SimpleComboLayout({
    required this.state,
    required this.theme,
    required this.controller,
    required this.focusNode,
    required this.textStyle,
    required this.isEnabled,
    required this.isReadOnly,
    required this.hasFixedSize,
    required this.bgColor,
    required this.borderColor,
    required this.onSelected,
    required this.onTextChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(theme.borderRadius),
        border: Border.all(color: borderColor, width: theme.borderWidth),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: theme.textFieldPadding,
            child: EditableText(
              controller: controller,
              focusNode: focusNode,
              readOnly: isReadOnly,
              onChanged: onTextChanged,
              style: textStyle,
              cursorColor: textStyle.color ?? theme.textColor,
              backgroundCursorColor: bgColor,
              selectionColor: DefaultSelectionStyle.of(context).selectionColor ??
                  Theme.of(context).colorScheme.primary.withOpacity(0.4),
            ),
          ),
          Divider(
            height: theme.dividerHeight,
            thickness: theme.dividerThickness,
            color: theme.dividerColor,
          ),
          _buildList(),
        ],
      ),
    );
  }

  Widget _buildList() {
    final list = SingleChildScrollView(
      child: Column(
        children: (state.items ?? [])
            .map(
              (item) => _ComboItem(
                text: item,
                isSelected: item == state.text,
                theme: theme,
                textStyle: textStyle,
                onTap: isEnabled ? () => onSelected(item) : () {},
              ),
            )
            .toList(),
      ),
    );
    return hasFixedSize ? Expanded(child: list) : list;
  }
}

class _ComboItem extends StatefulWidget {
  final String text;
  final bool isSelected;
  final ComboThemeExtension theme;
  final TextStyle textStyle;
  final VoidCallback onTap;

  const _ComboItem({
    required this.text,
    required this.isSelected,
    required this.theme,
    required this.textStyle,
    required this.onTap,
  });

  @override
  State<_ComboItem> createState() => _ComboItemState();
}

class _ComboItemState extends State<_ComboItem> {
  bool _itemHovered = false;

  @override
  Widget build(BuildContext context) {
    final Color bgColor = getComboItemBackgroundColor(widget.theme, widget.isSelected, _itemHovered);

    return MouseRegion(
      onEnter: (_) => setState(() => _itemHovered = true),
      onExit: (_) => setState(() => _itemHovered = false),
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: widget.onTap,
        child: AnimatedContainer(
          duration: widget.theme.animationDuration,
          padding: widget.theme.itemPadding,
          color: bgColor,
          child: Text(widget.text, style: widget.textStyle),
        ),
      ),
    );
  }
}
