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

class ComboImpl<T extends ComboSwt, V extends VCombo> extends CompositeImpl<T, V> {
  late TextEditingController _controller;
  final FocusNode _focusNode = FocusNode();
  final OverlayPortalController _overlayController = OverlayPortalController();
  final LayerLink _layerLink = LayerLink();
  bool _isFocused = false;
  final bool _isHovered = false;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _focusNode.addListener(_handleFocusChange);
  }

  Size _calculatePreferredSize(TextStyle style, ComboThemeExtension theme, bool isSimple) {
    final List<String> allStrings = [
      state.text ?? "",
      ...(state.items ?? []),
    ];

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

    final double width = maxTextWidth + theme.textFieldPadding.horizontal + (isSimple ? 0 : theme.iconSpacing + theme.iconSize);
    final double height = maxTextHeight + theme.textFieldPadding.vertical;

    return Size(width, height);
  }

  @override
  void extraSetState() {
    if (_controller.text != state.text) {
      _controller.text = state.text ?? "";
      _controller.selection = TextSelection.collapsed(offset: _controller.text.length);
    }
    state.listVisible == true ? _overlayController.show() : _overlayController.hide();
  }

  void _handleFocusChange() {
    if (!mounted) return;
    setState(() => _isFocused = _focusNode.hasFocus);
    _focusNode.hasFocus
        ? widget.sendFocusFocusIn(state, null)
        : widget.sendFocusFocusOut(state, null);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<ComboThemeExtension>()!;
    final bool isEnabled = state.enabled ?? true;
    final styleBits = StyleBits(state.style);
    final bool isSimple = styleBits.has(SWT.SIMPLE);
    final bool isReadOnly = styleBits.has(SWT.READ_ONLY);
    final bool hasFixedSize = hasBounds(state.bounds);

    final Color bgColor = getComboBackgroundColor(state, theme, enabled: isEnabled);
    final Color textColor = getComboTextColor(state, theme, enabled: isEnabled);
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

    final Size preferredSize = _calculatePreferredSize(textStyle, theme, isSimple);

    final double width = hasFixedSize
        ? state.bounds!.width.toDouble()
        : preferredSize.width;

    final double? height = hasFixedSize
        ? state.bounds!.height.toDouble()
        : (isSimple ? null : preferredSize.height);

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
      width: width,
    );

    return SizedBox(
      width: width,
      height: height,
      child: content,
    );
  }

  void _onItemSelected(String? value) {
    setState(() {
      state.text = value;
      _controller.text = value ?? "";
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
  final double width;

  const _DropdownComboLayout({
    required this.state, required this.theme, required this.controller,
    required this.focusNode, required this.textStyle, required this.isEnabled,
    required this.isReadOnly, required this.bgColor, required this.borderColor,
    required this.iconColor, required this.overlayController, required this.layerLink,
    required this.onSelected, required this.width,
  });

  @override
  Widget build(BuildContext context) {
    return OverlayPortal(
      controller: overlayController,
      overlayChildBuilder: (_) => _buildOverlay(),
      child: CompositedTransformTarget(
        link: layerLink,
        child: GestureDetector(
          behavior: HitTestBehavior.opaque,
          onTap: isEnabled ? overlayController.toggle : null,
          child: Container(
            decoration: BoxDecoration(
              color: bgColor,
              borderRadius: BorderRadius.circular(theme.borderRadius),
              border: Border.all(color: borderColor, width: theme.borderWidth),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Padding(
                    padding: theme.textFieldPadding,
                    child: IgnorePointer(
                      ignoring: true,
                      child: EditableText(
                        controller: controller,
                        focusNode: focusNode,
                        readOnly: isReadOnly,
                        style: textStyle,
                        cursorColor: textStyle.color ?? theme.textColor,
                        backgroundCursorColor: bgColor,
                      ),
                    ),
                  ),
                ),
                SizedBox(width: theme.iconSpacing),
                Icon(Icons.arrow_drop_down, color: iconColor, size: theme.iconSize),
              ],
            ),
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
        child: TapRegion(
          onTapOutside: (_) => overlayController.hide(),
          child: Container(
            width: width,
            decoration: BoxDecoration(
              color: theme.backgroundColor,
              borderRadius: BorderRadius.circular(theme.borderRadius),
              border: Border.all(color: theme.dividerColor, width: theme.borderWidth),
            ),
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: (state.items ?? []).map((item) => _ComboItem(
                  text: item,
                  isSelected: item == state.text,
                  theme: theme,
                  textStyle: textStyle,
                  onTap: () => onSelected(item),
                )).toList(),
              ),
            ),
          ),
        ),
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

  const _SimpleComboLayout({
    required this.state, required this.theme, required this.controller,
    required this.focusNode, required this.textStyle, required this.isEnabled,
    required this.isReadOnly, required this.hasFixedSize, required this.bgColor,
    required this.borderColor, required this.onSelected,
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
              style: textStyle,
              cursorColor: textStyle.color ?? theme.textColor,
              backgroundCursorColor: bgColor,
            ),
          ),
          Divider(height: theme.dividerHeight, thickness: theme.dividerThickness, color: theme.dividerColor),
          _buildList(),
        ],
      ),
    );
  }

  Widget _buildList() {
    final list = SingleChildScrollView(
      child: Column(
        children: (state.items ?? []).map((item) => _ComboItem(
          text: item,
          isSelected: item == state.text,
          theme: theme,
          textStyle: textStyle,
          onTap: isEnabled ? () => onSelected(item) : () {},
        )).toList(),
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
    required this.text, required this.isSelected, required this.theme,
    required this.textStyle, required this.onTap,
  });

  @override
  State<_ComboItem> createState() => _ComboItemState();
}

class _ComboItemState extends State<_ComboItem> {
  bool _itemHovered = false;

  @override
  Widget build(BuildContext context) {
    final Color bgColor = widget.isSelected
        ? widget.theme.selectedItemBackgroundColor
        : (_itemHovered ? widget.theme.hoverBackgroundColor : const Color(0x00000000));

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