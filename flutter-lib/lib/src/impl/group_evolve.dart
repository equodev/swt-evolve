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
    final text = state.text ?? '';

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

  const _StyledGroup({
    Key? key,
    required this.composite,
    required this.widgetTheme,
    required this.text,
    this.children,
    this.vFont,
    this.textColor,
    required this.hasBounds,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final hasValidBounds = hasBounds;
    final constraints = hasValidBounds ? getConstraintsFromBounds(composite.bounds) : null;

    final backgroundColor = getBackgroundColor(
      background: composite.background,
      defaultColor: widgetTheme.backgroundColor,
    );
    final borderColor = widgetTheme.borderColor;
    final foregroundColor = getForegroundColor(
      foreground: textColor,
      defaultColor: widgetTheme.foregroundColor,
    );
    final textStyle = getTextStyle(
    context: context,
    font: vFont,
    textColor: foregroundColor,
    baseTextStyle: widgetTheme.textStyle,
  );

    return Container(
      constraints: constraints,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border.all(color: borderColor, width: widgetTheme.borderWidth),
        borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
      ),
      padding: widgetTheme.padding,
      margin: widgetTheme.margin,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          // Title
          if (text.isNotEmpty)
            Padding(
              padding: widgetTheme.padding,
              child: Text(
                text,
                style: textStyle,
              ),  
            ),
          if (children != null)
            NoLayout(children: children!, composite: composite)
        ],
      ),
    );
  }
}
