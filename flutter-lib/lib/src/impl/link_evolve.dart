import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
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
  /// The node a click on a Link that holds an anchor focuses, as Java asks for it.
  final FocusNode _focusNode = FocusNode(debugLabel: 'Link');

  @override
  FocusNode? get swtFocusNode => _focusNode;

  @override
  void dispose() {
    _focusNode.dispose();
    super.dispose();
  }

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
      tabStop: state.tabStop,
      text: text,
      enabled: enabled,
      foreground: state.foreground,
      linkForeground: state.linkForeground,
      backgroundColor: null,
      vFont: state.font,
      widgetTheme: widgetTheme,
      focusNode: _focusNode,
      onTap: (url, anchor) {
        if (enabled) {
          var e = VEvent()
            ..text = url
            ..index = anchor;
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

  /// Called with an anchor's href and its index among the anchors.
  final void Function(String href, int anchor) onTap;
  final FocusNode? focusNode;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  /// Java's VControl.tabStop, absent when false.
  final bool? tabStop;

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
    this.focusNode,
    this.onFocusIn,
    this.onFocusOut,
    this.tabStop,
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
        focusNode: widget.focusNode,
        // A tab stop only while it holds a link, as natively.
        skipTraversal: !(widget.tabStop ?? false),
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
          key: _textKey,
          text: TextSpan(children: _spans(_parts, underline), style: baseTextStyle),
        );
    final text = !widget.text.contains('&')
        ? build(false)
        : ValueListenableBuilder<bool>(
            valueListenable: MnemonicCue.shown,
            builder: (context, shown, _) => build(shown),
          );
    // Inside the control's own pointer handling, so the anchor's Selection reaches Java ahead of the
    // release's MouseUp, as every native reports it.
    return Listener(
      onPointerDown: (e) => _pressedAnchor = e.buttons == kPrimaryButton ? _anchorAt(e.position) : null,
      onPointerCancel: (_) => _pressedAnchor = null,
      onPointerUp: (e) {
        final anchor = _pressedAnchor;
        _pressedAnchor = null;
        if (anchor == null || !widget.enabled || _anchorAt(e.position) != anchor) return;
        widget.onTap(_parts.where((part) => part.href != null).elementAt(anchor).href!, anchor);
      },
      child: text,
    );
  }

  final GlobalKey _textKey = GlobalKey();

  /// The anchor a press landed on, which the release has to land on too to select it.
  int? _pressedAnchor;

  /// The index among the anchors of the one drawn under [global], or null over plain text or none.
  int? _anchorAt(Offset global) {
    final paragraph = _textKey.currentContext?.findRenderObject();
    if (paragraph is! RenderParagraph) return null;
    final local = paragraph.globalToLocal(global);
    var start = 0, anchor = 0;
    for (final part in _parts) {
      final end = start + part.text.length;
      if (part.href != null) {
        final boxes = paragraph.getBoxesForSelection(TextSelection(baseOffset: start, extentOffset: end));
        if (boxes.any((box) => box.toRect().contains(local))) return anchor;
        anchor++;
      }
      start = end;
    }
    return null;
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
        mouseCursor: url != null && widget.enabled ? SystemMouseCursors.click : null,
      );
    }

    return [for (final part in parts) ...spans(part)];
  }
}
