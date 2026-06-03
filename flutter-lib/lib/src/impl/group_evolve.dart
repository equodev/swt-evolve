import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/nolayout.dart';
import '../theme/theme_extensions/group_theme_extension.dart';
import '../gen/control.dart';
import '../gen/group.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import 'utils/widget_utils.dart';
import 'utils/text_utils.dart';

const int _shadowIn = 1 << 2;
const int _shadowOut = 1 << 3;
const int _shadowEtchedIn = 1 << 4;
const int _shadowEtchedOut = 1 << 5;
const int _shadowNone = 1 << 7;

class GroupImpl<T extends GroupSwt, V extends VGroup>
    extends CompositeImpl<T, V> {
  FocusNode? _focusNode;

  @override
  void initState() {
    super.initState();
    _focusNode = FocusNode();
    _focusNode!.addListener(_handleFocusChange);
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<GroupThemeExtension>()!;
    final children = state.children;
    final text = stripAccelerators(state.text);

    return MouseRegion(
      onEnter: (_) => widget.sendMouseTrackMouseEnter(state, null),
      onExit: (_) => widget.sendMouseTrackMouseExit(state, null),
      child: Focus(
        focusNode: _focusNode,
        child: _StyledGroup(
          widgetTheme: widgetTheme,
          text: text,
          composite: state,
          children: children,
          vFont: state.font,
          textColor: state.foreground,
          hasBounds: hasBounds(state.bounds),
          style: state.style,
        ),
      ),
    );
  }

  void _handleFocusChange() {
    if (_focusNode!.hasFocus) {
      widget.sendFocusFocusIn(state, null);
    } else {
      widget.sendFocusFocusOut(state, null);
    }
  }

  @override
  void dispose() {
    _focusNode?.removeListener(_handleFocusChange);
    _focusNode?.dispose();
    super.dispose();
  }
}

class _StyledGroup extends StatelessWidget {
  final GroupThemeExtension widgetTheme;
  final String text;
  final List<VControl>? children;
  final VFont? vFont;
  final VColor? textColor;
  final VComposite composite;
  final bool hasBounds;
  final int style;

  const _StyledGroup({
    Key? key,
    required this.composite,
    required this.widgetTheme,
    required this.text,
    this.children,
    this.vFont,
    this.textColor,
    required this.hasBounds,
    required this.style,
  }) : super(key: key);

  bool get _noShadow => style & _shadowNone != 0;
  bool get _shadowInStyle => style & _shadowIn != 0;
  bool get _shadowOutStyle => style & _shadowOut != 0;
  bool get _etchedOut => style & _shadowEtchedOut != 0;

  List<BoxShadow> _buildBoxShadows() {
    if (_noShadow || _shadowInStyle) return [];
    if (_shadowOutStyle) {
      return [
        BoxShadow(
          color: widgetTheme.shadowDarkColor.withOpacity(widgetTheme.shadowOutOpacity),
          blurRadius: widgetTheme.shadowOutBlurRadius,
          offset: Offset(0, widgetTheme.shadowOutElevation),
        ),
        BoxShadow(
          color: widgetTheme.shadowDarkColor.withOpacity(widgetTheme.shadowOutOpacityAlt),
          blurRadius: widgetTheme.shadowOutBlurRadiusAlt,
          offset: Offset(0, widgetTheme.shadowSecondaryElevation),
        ),
      ];
    }
    if (_etchedOut) {
      return [
        BoxShadow(
          color: widgetTheme.shadowDarkColor.withOpacity(widgetTheme.shadowEtchedOpacity),
          blurRadius: widgetTheme.shadowEtchedBlurRadius,
          offset: Offset(0, widgetTheme.shadowSecondaryElevation),
        ),
      ];
    }
    return [];
  }

  @override
  Widget build(BuildContext context) {
    final backgroundColor = getBackgroundColor(
          background: composite.background,
          defaultColor: widgetTheme.backgroundColor,
        ) ??
        widgetTheme.backgroundColor;

    final borderColor = _shadowInStyle
        ? (Color.lerp(widgetTheme.borderColor, widgetTheme.shadowDarkColor, widgetTheme.shadowInBorderFactor) ??
            widgetTheme.borderColor)
        : widgetTheme.borderColor;

    final resolvedBg = _shadowInStyle
        ? (Color.lerp(backgroundColor, widgetTheme.shadowDarkColor, widgetTheme.shadowInBgFactor) ?? backgroundColor)
        : backgroundColor;

    final foregroundColor = getForegroundColor(
      foreground: textColor,
      defaultColor: widgetTheme.foregroundColor,
    );
    final titleStyle = getTextStyle(
      context: context,
      font: vFont,
      textColor: foregroundColor,
      baseTextStyle: widgetTheme.textStyle,
    );

    final titleFontSize = titleStyle?.fontSize ?? widgetTheme.textStyle?.fontSize ?? 12.0;
    final titleTopOffset = text.isNotEmpty ? titleFontSize / 2 : 0.0;

    final borderWidth = _noShadow ? 0.0 : widgetTheme.borderWidth;
    final borderDecoration = BoxDecoration(
      border: borderWidth > 0
          ? Border.all(color: borderColor, width: borderWidth)
          : null,
      borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
      boxShadow: _buildBoxShadows(),
    );

    final chromeHeight = hasBounds
        ? composite.bounds!.height.toDouble() - titleTopOffset
        : null;

    final stack = Stack(
      clipBehavior: Clip.hardEdge,
      children: [
        if (!_noShadow)
          Positioned(
            top: titleTopOffset,
            left: 0,
            right: 0,
            bottom: chromeHeight != null ? null : 0,
            child: IgnorePointer(
              child: SizedBox(
                height: chromeHeight,
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    color: resolvedBg,
                    borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
                  ),
                ),
              ),
            ),
          ),
        Positioned(
          top: titleTopOffset,
          left: 0,
          right: 0,
          bottom: chromeHeight != null ? null : 0,
          child: IgnorePointer(
            child: SizedBox(
              height: chromeHeight,
              child: DecoratedBox(decoration: borderDecoration),
            ),
          ),
        ),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            if (text.isNotEmpty)
              Opacity(
                opacity: 0,
                child: Text(text, style: titleStyle),
              ),
            if (children != null)
              ParentBackgroundScope(
                background: resolvedBg,
                child: NoLayout(children: children!, composite: composite),
              ),
          ],
        ),
        if (text.isNotEmpty)
          Positioned(
            top: 0,
            left: widgetTheme.titleHorizontalOffset,
            child: IgnorePointer(
              child: Container(
                padding: EdgeInsets.symmetric(horizontal: widgetTheme.titleLabelPadding),
                decoration: BoxDecoration(
                  color: resolvedBg,
                  border: borderWidth > 0
                      ? Border.all(color: borderColor, width: borderWidth)
                      : null,
                  borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
                ),
                child: Text(text, style: titleStyle),
              ),
            ),
          ),
      ],
    );

    if (hasBounds) {
      return SizedBox(
        width: composite.bounds!.width.toDouble(),
        height: composite.bounds!.height.toDouble(),
        child: ClipRect(child: stack),
      );
    }

    return stack;
  }
}
