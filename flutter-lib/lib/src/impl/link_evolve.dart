import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import '../gen/color.dart';
import '../gen/event.dart';
import '../gen/font.dart';
import '../gen/link.dart';
import '../impl/control_evolve.dart';
import './utils/widget_utils.dart';
import './utils/font_utils.dart';
import './utils/link_markup.dart';
import './utils/mnemonic_text.dart';
import '../theme/theme_extensions/link_theme_extension.dart';

class LinkImpl<T extends LinkSwt, V extends VLink> extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<LinkThemeExtension>()!;

    final enabled = state.enabled ?? false;

    return _buildLink(context, widgetTheme, enabled);
  }

  Widget _buildLink(
    BuildContext context,
    LinkThemeExtension widgetTheme,
    bool enabled,
  ) {
    final text = state.text ?? '';
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    final backgroundColor = getBackgroundColor(
      background: state.background,
      defaultColor: widgetTheme.backgroundColor,
    );

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
      onFocusIn: () => widget.sendFocusFocusIn(state, null),
      onFocusOut: () => widget.sendFocusFocusOut(state, null),
    );

    return Opacity(
      opacity: enabled ? 1.0 : widgetTheme.disabledOpacity,
      child: wrap(
        Container(
          constraints: constraints,
          padding: widgetTheme.padding,
          alignment: hasValidBounds
              ? getAlignmentFromTextAlign(widgetTheme.textAlign)
              : null,
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
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  _StyledLinkState createState() => _StyledLinkState();
}

class _StyledLinkState extends State<StyledLink> {
  bool _isHovered = false;
  bool _isFocused = false;
  late List<LinkPart> _parts;

  /// Whether the text holds a link, read with the same parse that renders it, so that the tab stop
  /// and the rendering never disagree on a malformed tag.
  bool get _hasLinks => _parts.any((part) => part.href != null);

  @override
  void initState() {
    super.initState();
    _parts = parseLinkMarkup(widget.text);
  }

  @override
  void didUpdateWidget(StyledLink oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.text != widget.text) _parts = parseLinkMarkup(widget.text);
  }

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) {
        setState(() => _isHovered = true);
      },
      onExit: (_) {
        setState(() => _isHovered = false);
      },
      child: Focus(
        // A tab stop only while it holds a link, as natively.
        skipTraversal: !_hasLinks,
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
            context: context,
            foreground: widget.foreground,
            defaultColor: widget.widgetTheme.textColor,
            minContrast: widget.widgetTheme.minTextContrast,
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

    if (!_hasLinks) {
      return MnemonicText(widget.text, style: baseTextStyle);
    }

    Widget build(bool underline) => RichText(
          text: TextSpan(children: _spans(_parts, underline), style: baseTextStyle),
        );
    if (!widget.text.contains('&')) return build(false);
    return ValueListenableBuilder<bool>(
      valueListenable: MnemonicCue.shown,
      builder: (context, shown, _) => build(shown),
    );
  }

  /// The parts as spans; the first one carrying a mnemonic underlines it when [underline] is set,
  /// as `Link.parse` takes the one mnemonic from the whole text without its markup.
  List<TextSpan> _spans(List<LinkPart> parts, bool underline) {
    final linkColor = widget.enabled
        ? getForegroundColor(
            context: context,
            foreground: widget.linkForeground,
            defaultColor: (_isHovered || _isFocused)
                ? widget.widgetTheme.linkHoverTextColor
                : widget.widgetTheme.linkTextColor,
            minContrast: widget.widgetTheme.minTextContrast,
          )
        : widget.widgetTheme.disabledTextColor;

    final linkDecoration = (_isHovered || _isFocused)
        ? widget.widgetTheme.linkHoverDecoration
        : widget.widgetTheme.linkDecoration;

    var marked = false;
    List<TextSpan> spans(LinkPart part) {
      final index = marked ? -1 : part.mnemonicIndex;
      if (index >= 0) marked = true;
      final url = part.href;
      return mnemonicSpans(
        part.text,
        index,
        underline: underline,
        style:
            url == null ? null : TextStyle(color: linkColor, decoration: linkDecoration),
        recognizer: url != null && widget.enabled
            ? (TapGestureRecognizer()..onTap = () => widget.onTap(url))
            : null,
      );
    }

    return [for (final part in parts) ...spans(part)];
  }
}
