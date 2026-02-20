import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import '../gen/color.dart';
import '../gen/event.dart';
import '../gen/font.dart';
import '../gen/link.dart';
import '../impl/control_evolve.dart';
import './utils/widget_utils.dart';
import './utils/font_utils.dart';
import '../theme/theme_extensions/link_theme_extension.dart';

class LinkImpl<T extends LinkSwt, V extends VLink> extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<LinkThemeExtension>()!;

    final enabled = state.enabled ?? false;

    return _buildLink(context, widgetTheme, enabled);
  }

  Widget _buildLink(BuildContext context, LinkThemeExtension widgetTheme, bool enabled) {
    final text = state.text ?? '';
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    final backgroundColor = getSwtBackgroundColor(context, defaultColor: widgetTheme.backgroundColor);

    final child = StyledLink(
      text: text,
      enabled: enabled,
      foreground: state.foreground,
      linkForeground: state.linkForeground,
      backgroundColor: null,
      vFont: state.font,
      widgetTheme: widgetTheme,
      onTap: (url) {
        if (enabled) {
          var e = VEvent()..text = url;
          widget.sendSelectionSelection(state, e);
        }
      },
      onMouseEnter: () => widget.sendMouseTrackMouseEnter(state, null),
      onMouseExit: () => widget.sendMouseTrackMouseExit(state, null),
      onFocusIn: () => widget.sendFocusFocusIn(state, null),
      onFocusOut: () => widget.sendFocusFocusOut(state, null),
    );

    return Opacity(
      opacity: enabled ? 1.0 : widgetTheme.disabledOpacity,
      child: wrap(
        Container(
          constraints: constraints,
          padding: widgetTheme.padding,
          alignment: hasValidBounds ? getAlignmentFromTextAlign(widgetTheme.textAlign) : null,
          decoration: backgroundColor != null
              ? BoxDecoration(color: backgroundColor)
              : null,
          child: child,
        ),
      ),
    );
  }
}

class StyledLink extends StatefulWidget {
  final String text;
  final bool enabled;
  final VColor? foreground;
  final VColor? linkForeground;
  final VColor? backgroundColor;
  final VFont? vFont;
  final LinkThemeExtension widgetTheme;
  final Function(String) onTap;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledLink({
    Key? key,
    required this.text,
    required this.enabled,
    this.foreground,
    this.linkForeground,
    this.backgroundColor,
    this.vFont,
    required this.widgetTheme,
    required this.onTap,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  _StyledLinkState createState() => _StyledLinkState();
}

class _StyledLinkState extends State<StyledLink> {
  bool _isHovered = false;
  bool _isFocused = false;

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) {
        setState(() => _isHovered = true);
        widget.onMouseEnter?.call();
      },
      onExit: (_) {
        setState(() => _isHovered = false);
        widget.onMouseExit?.call();
      },
      child: Focus(
        onFocusChange: (hasFocus) {
          setState(() => _isFocused = hasFocus);
          if (hasFocus) {
            widget.onFocusIn?.call();
          } else {
            widget.onFocusOut?.call();
          }
        },
        child: _buildRichText(),
      ),
    );
  }

  Widget _buildRichText() {
    final textColor = widget.enabled
        ? getForegroundColor(
            foreground: widget.foreground,
            defaultColor: widget.widgetTheme.textColor,
          )
        : widget.widgetTheme.disabledTextColor;

    final baseTextStyle = getTextStyle(
      context: context,
      font: widget.vFont,
      textColor: textColor,
      baseTextStyle: widget.enabled
          ? widget.widgetTheme.textStyle
          : widget.widgetTheme.disabledTextStyle,
    );

    final hasLinks = widget.text.toLowerCase().contains('<a');
    if (!hasLinks) {
      return Text(
        widget.text,
        style: baseTextStyle,
      );
    }

    final List<TextSpan> spans = _parseText(widget.text);
    return RichText(
      text: TextSpan(
        children: spans,
        style: baseTextStyle,
      ),
    );
  }

  List<TextSpan> _parseText(String text) {
    final regex = RegExp(r'<a(?:\s+href="([^"]*)")?>(.+?)<\/a>|([^<]+)',
        caseSensitive: false);
    final matches = regex.allMatches(text);

    final linkColor = widget.enabled
        ? getForegroundColor(
            foreground: widget.linkForeground,
            defaultColor: (_isHovered || _isFocused)
                ? widget.widgetTheme.linkHoverTextColor
                : widget.widgetTheme.linkTextColor,
          )
        : widget.widgetTheme.disabledTextColor;

    final linkDecoration = (_isHovered || _isFocused)
        ? widget.widgetTheme.linkHoverDecoration
        : widget.widgetTheme.linkDecoration;

    return matches.map((match) {
      if (match.group(2) != null) {
        final url = match.group(1) ?? match.group(2)!;
        final linkText = match.group(2)!;
        return TextSpan(
          text: linkText,
          style: TextStyle(
            color: linkColor,
            decoration: linkDecoration
          ),
          recognizer: widget.enabled
              ? (TapGestureRecognizer()..onTap = () => widget.onTap(url))
              : null,
        );
      } else {
        return TextSpan(text: match.group(3));
      }
    }).toList();
  }
}
