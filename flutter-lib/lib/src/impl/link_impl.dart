import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import '../swt/link.dart';
import '../impl/control_impl.dart';
import 'widget_config.dart';

class LinkImpl<T extends LinkSwt, V extends LinkValue>
    extends ControlImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

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
      useDarkTheme: useDarkTheme,
      onTap: (url) {
        if (enabled) {
          widget.sendSelectionSelection(state, url);
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
  final bool useDarkTheme;
  final Function(String) onTap;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledLink({
    Key? key,
    required this.text,
    required this.enabled,
    required this.useDarkTheme,
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
    final Color linkColor = widget.useDarkTheme ? Colors.lightBlue : Colors.blue;
    final Color disabledColor = widget.useDarkTheme ? Colors.grey[600]! : Colors.grey[400]!;

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
          padding: EdgeInsets.symmetric(horizontal: 12),
          alignment: Alignment.centerLeft,
          child: _buildRichText(linkColor, disabledColor),
        ),
      ),
    );
  }

  Widget _buildRichText(Color linkColor, Color disabledColor) {
    final List<TextSpan> spans = _parseText(widget.text, linkColor, disabledColor);

    return RichText(
      text: TextSpan(
        children: spans,
        style: TextStyle(
          color: widget.useDarkTheme ? Colors.white : Colors.black,
          fontSize: 14,
          height: 1.0,
        ),
      ),
      overflow: TextOverflow.ellipsis,
    );
  }

  List<TextSpan> _parseText(String text, Color linkColor, Color disabledColor) {
    final regex = RegExp(r'<a(?:\s+href="([^"]*)")?>(.+?)<\/a>|([^<]+)', caseSensitive: false);
    final matches = regex.allMatches(text);

    return matches.map((match) {
      if (match.group(2) != null) {
        final url = match.group(1) ?? match.group(2)!;
        final linkText = match.group(2)!;
        return TextSpan(
          text: linkText,
          style: TextStyle(
            color: widget.enabled ? linkColor : disabledColor,
            decoration: _isHovered || _isFocused ? TextDecoration.underline : null,
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