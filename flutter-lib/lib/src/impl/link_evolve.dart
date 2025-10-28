import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/font.dart';
import '../gen/color.dart';
import '../gen/event.dart';
import '../gen/link.dart';
import '../impl/control_evolve.dart';
import 'color_utils.dart';
import 'utils/font_utils.dart';

class LinkImpl<T extends LinkSwt, V extends VLink> extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    return super.wrap(_buildLink());
  }

  Widget _buildLink() {
    final text = state.text ?? '';
    final enabled = state.enabled ?? true;

    return StyledLink(
      text: text,
      enabled: enabled,
      linkForeground: state.linkForeground,
      backgroundColor: state.background,
      vFont: state.font,
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
  }
}

class StyledLink extends StatefulWidget {
  final String text;
  final bool enabled;
  final VColor? linkForeground;
  final VColor? backgroundColor;
  final VFont? vFont;
  final Function(String) onTap;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledLink({
    Key? key,
    required this.text,
    required this.enabled,
    this.linkForeground,
    this.backgroundColor,
    this.vFont,
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
        child: Container(
          height: 32,
          padding: const EdgeInsets.symmetric(horizontal: 12),
          alignment: Alignment.centerLeft,
          color: colorFromVColor(widget.backgroundColor),
          child: _buildRichText(),
        ),
      ),
    );
  }

  Widget _buildRichText() {
    final List<TextSpan> spans = _parseText(widget.text);

    // Get text color from VColor or use default
    final finalTextColor =
        colorFromVColor(widget.linkForeground, defaultColor: getForeground());

    // Create TextStyle from VFont
    final baseTextStyle = FontUtils.textStyleFromVFont(
      widget.vFont,
      context,
      color: finalTextColor,
    );

    return RichText(
      text: TextSpan(
        children: spans,
        style: baseTextStyle,
      ),
      overflow: TextOverflow.ellipsis,
    );
  }

  List<TextSpan> _parseText(String text) {
    final regex = RegExp(r'<a(?:\s+href="([^"]*)")?>(.+?)<\/a>|([^<]+)',
        caseSensitive: false);
    final matches = regex.allMatches(text);

    // Use custom linkForeground if available, otherwise use default
    final Color? linkColor =
        colorFromVColor(widget.linkForeground, defaultColor: getLinkColor());
    final Color disabledColor = getForegroundDisabled();

    return matches.map((match) {
      if (match.group(2) != null) {
        final url = match.group(1) ?? match.group(2)!;
        final linkText = match.group(2)!;
        return TextSpan(
          text: linkText,
          style: TextStyle(
            color: widget.enabled ? linkColor : disabledColor,
            decoration:
                _isHovered || _isFocused ? TextDecoration.underline : null,
            height: 1.0,
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
