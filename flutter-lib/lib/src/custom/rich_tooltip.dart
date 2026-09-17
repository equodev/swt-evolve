import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../gen/toolitem.dart';
import '../impl/widget_config.dart';
import '../theme/theme_extensions/tooltip_theme_extension.dart';
import 'main_toolbar_scope.dart';

/// The custom tooltip: a card with an icon, a title, a body and a keyboard-shortcut hint, in place
/// of the one-line strip `setToolTipText` gets everywhere else.
///
/// Everything the feature needs lives here. The widget that uses it only asks [maybeWrap] whether
/// this item gets one, and falls back to its own plain tooltip when the answer is no.
class EvolveToolTip {
  EvolveToolTip._();

  /// Wraps [child] in the card, or returns null when this item should keep the plain tooltip --
  /// the flag is off, or the item is not in the main toolbar.
  static Widget? maybeWrap(
    BuildContext context, {
    required VToolItem item,
    required String message,
    required Widget child,
    Widget? leading,
  }) {
    if (!customToolTip || !MainToolbarScope.of(context)) return null;
    final data = RichToolTipData.of(
      text: item.text,
      toolTipText: message.isEmpty ? item.toolTipText : message,
    );
    if (data == null) return null;
    return RichToolTip(data: data, leading: leading, child: child);
  }

  /// The card on its own, for a caller that opens and places its own overlay -- a Control's tooltip
  /// follows the pointer rather than hanging off the widget, so it cannot use [maybeWrap]. Null
  /// when the caller keeps the plain panel.
  ///
  /// Unlike [maybeWrap] this is not confined to the main toolbar. A control that paints its own
  /// content has no label, no image and no item behind it, so its tooltip is the only thing that can
  /// say what is under the pointer -- and that is as true of a canvas in a view as of one in the
  /// toolbar. Which controls opt in is the caller's decision, not this one's.
  ///
  /// One canvas carries one tooltip however many icons are drawn on it; SWT has no per-region
  /// tooltip. An application that wants a different card per icon has to keep setting `toolTipText`
  /// as the pointer moves, exactly as it would natively.
  static Widget? maybeCard(BuildContext context, {String? text, String? toolTipText}) {
    if (!customToolTip) return null;
    final data = RichToolTipData.of(text: text, toolTipText: toolTipText);
    return data == null ? null : RichToolTipCard(data: data);
  }
}

/// A keybinding e4 appends to a tooltip in parentheses -- "Save (Ctrl+S)", "Quick Access (Ctrl+3)".
/// Matched conservatively so a tooltip that merely ends in a parenthetical sentence keeps it as
/// body text.
final RegExp _trailingShortcut = RegExp(
  r'\s*\(((?:Ctrl|Control|Alt|Shift|Cmd|Command|Meta|Option|Fn|F\d)[^()]{0,40})\)\s*$',
  caseSensitive: false,
);

/// Stand-in body for an item that says nothing beyond its label. Applications differ on how much
/// they put in a tooltip: one following the e4 convention carries a sentence and a shortcut, while
/// another gives a bare word. This is what the second kind shows.
const String _sampleBody = 'Example description.';

/// What a card displays. Derived from what the application already says about the item -- an e4
/// tooltip of the form "Label (Ctrl+X)" already holds a title, a body and a shortcut.
@immutable
class RichToolTipData {
  final String? title;
  final String? body;
  final String? shortcut;

  const RichToolTipData({this.title, this.body, this.shortcut});

  /// Splits an item's label and tooltip into the card's parts, or null when there is nothing at all
  /// to show. An item with only a label gets [_sampleBody] rather than one word in a box.
  static RichToolTipData? of({String? text, String? toolTipText}) {
    var body = toolTipText?.trim() ?? '';
    String? shortcut;
    final match = _trailingShortcut.firstMatch(body);
    if (match != null) {
      shortcut = match.group(1)!.trim();
      body = body.substring(0, match.start).trim();
    }

    var title = _stripMnemonics(text);
    if (title.isEmpty) {
      // No label -- the tooltip itself is the only text, so its first line becomes the title.
      final split = body.indexOf('\n');
      if (split >= 0) {
        title = body.substring(0, split).trim();
        body = body.substring(split + 1).trim();
      } else {
        title = body;
        body = '';
      }
    } else if (title.toLowerCase() == body.toLowerCase()) {
      // e4 routinely sets the tooltip to the label plus a shortcut; repeating it under itself
      // would read as a stutter.
      body = '';
    }

    if (title.isEmpty && body.isEmpty && (shortcut == null || shortcut.isEmpty)) return null;
    return RichToolTipData(
      title: title.isEmpty ? null : title,
      body: body.isEmpty ? _sampleBody : body,
      shortcut: shortcut,
    );
  }

  /// SWT marks the mnemonic with `&`; `&&` is a literal ampersand.
  static String _stripMnemonics(String? raw) {
    if (raw == null || raw.isEmpty) return '';
    final out = StringBuffer();
    for (var i = 0; i < raw.length; i++) {
      if (raw[i] == '&') {
        if (i + 1 < raw.length && raw[i + 1] == '&') {
          out.write('&');
          i++;
        }
        continue;
      }
      out.write(raw[i]);
    }
    return out.toString().trim();
  }

  @override
  bool operator ==(Object other) =>
      other is RichToolTipData &&
      other.title == title &&
      other.body == body &&
      other.shortcut == shortcut;

  @override
  int get hashCode => Object.hash(title, body, shortcut);
}

/// Shows [data] as a card below [child] after the theme's wait duration, the way a platform tooltip
/// appears but with room for a title, a body and a shortcut.
class RichToolTip extends StatefulWidget {
  final RichToolTipData data;
  final Widget child;

  /// The item's own artwork, drawn at the head of the card. Null gives a text-only card.
  final Widget? leading;

  const RichToolTip({
    super.key,
    required this.data,
    required this.child,
    this.leading,
  });

  @override
  State<RichToolTip> createState() => _RichToolTipState();
}

class _RichToolTipState extends State<RichToolTip> {
  final LayerLink _link = LayerLink();
  OverlayEntry? _entry;
  bool _hovering = false;

  @override
  void dispose() {
    _remove();
    super.dispose();
  }

  @override
  void didUpdateWidget(RichToolTip oldWidget) {
    super.didUpdateWidget(oldWidget);
    // The item's text or tooltip can change under the pointer (a toggle flipping its label); the
    // card on screen would otherwise keep the old wording until the pointer leaves.
    if (oldWidget.data != widget.data) _entry?.markNeedsBuild();
  }

  void _scheduleShow(TooltipThemeExtension theme) {
    _hovering = true;
    Future.delayed(theme.waitDuration, () {
      if (!mounted || !_hovering || _entry != null) return;
      _show();
    });
  }

  void _show() {
    final overlay = Overlay.maybeOf(context, rootOverlay: true);
    if (overlay == null) return;
    _entry = OverlayEntry(
      builder: (ctx) {
        final theme = Theme.of(ctx).extension<TooltipThemeExtension>()!;
        return Positioned(
          // CompositedTransformFollower needs an unconstrained box to follow into; the width cap
          // is the card's own, applied inside RichToolTipCard.
          width: theme.richMaxWidth,
          child: CompositedTransformFollower(
            link: _link,
            showWhenUnlinked: false,
            targetAnchor: Alignment.bottomLeft,
            followerAnchor: Alignment.topLeft,
            offset: Offset(_horizontalShift(theme), theme.richGap),
            // The Positioned above is as wide as a card may get; keep a narrower card at its left
            // edge instead of letting it stretch across that width.
            child: Align(
              alignment: Alignment.topLeft,
              child: IgnorePointer(
                child: RichToolTipCard(data: widget.data, leading: widget.leading),
              ),
            ),
          ),
        );
      },
    );
    overlay.insert(_entry!);
  }

  /// How far left to pull the card so it stays inside the window. The card hangs off the item's
  /// left edge, and a main toolbar runs the full width of the window, so an item on the right would
  /// otherwise open a card that is partly off-screen. Zero for everything that already fits.
  double _horizontalShift(TooltipThemeExtension theme) {
    final box = context.findRenderObject();
    if (box is! RenderBox || !box.hasSize) return 0;
    final itemLeft = box.localToGlobal(Offset.zero).dx;
    final windowWidth = MediaQuery.maybeSizeOf(context)?.width;
    if (windowWidth == null) return 0;

    final rightLimit = windowWidth - theme.richMaxWidth - theme.screenMargin;
    // A window narrower than the card leaves nowhere to put it; sit at the left margin.
    if (rightLimit <= theme.screenMargin) return theme.screenMargin - itemLeft;
    return itemLeft > rightLimit ? rightLimit - itemLeft : 0;
  }

  void _remove() {
    _hovering = false;
    _entry?.remove();
    _entry = null;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<TooltipThemeExtension>()!;
    return CompositedTransformTarget(
      link: _link,
      child: MouseRegion(
        onEnter: (_) => _scheduleShow(theme),
        onExit: (_) => _remove(),
        child: widget.child,
      ),
    );
  }
}

/// The box the item's artwork occupies in the card. Named so a test can measure it: the card is an
/// overlay entry, so it is nowhere under the [RichToolTip] that opened it.
@visibleForTesting
const Key artworkSlotKey = Key('richToolTipArtwork');

/// The height one line of [style] occupies. TextStyle.height is a multiple of the font size, and is
/// often unset -- Flutter then uses the font's own, which 1.2 approximates closely enough to centre
/// an icon against.
double _lineHeight(TextStyle? style) {
  final size = style?.fontSize ?? 14.0;
  return size * (style?.height ?? 1.2);
}

class RichToolTipCard extends StatelessWidget {
  final RichToolTipData data;
  final Widget? leading;

  const RichToolTipCard({super.key, required this.data, this.leading});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<TooltipThemeExtension>()!;
    final title = data.title;
    final body = data.body;
    final shortcut = data.shortcut;

    final lines = <Widget>[];
    if (title != null && title.isNotEmpty) {
      lines.add(Text(
        title,
        style: theme.richTitleTextStyle ?? theme.titleTextStyle,
        maxLines: 2,
        overflow: TextOverflow.ellipsis,
      ));
    }
    if (body != null && body.isNotEmpty) {
      if (lines.isNotEmpty) {
        lines.add(SizedBox(height: theme.richTitleBodySpacing));
      }
      lines.add(Text(
        body,
        style: theme.richBodyTextStyle ?? theme.messageTextStyle,
        maxLines: theme.messageMaxLines,
        overflow: TextOverflow.ellipsis,
      ));
    }
    if (shortcut != null && shortcut.isNotEmpty) {
      if (lines.isNotEmpty) {
        lines.add(SizedBox(height: theme.richTitleBodySpacing));
      }
      lines.add(_ShortcutChip(shortcut: shortcut));
    }

    Widget content = Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: lines,
    );

    final Widget? art =
        leading == null ? null : FittedBox(fit: BoxFit.contain, child: leading);
    if (art != null) {
      // The icon is taller than the text line it sits beside, so aligning both to the top of the
      // column leaves it visibly high. It takes part in layout as a box exactly one line tall and
      // paints centred on that line, overflowing it evenly -- which the card's padding absorbs.
      final firstLine = lines.isEmpty
          ? theme.richIconSize
          : _lineHeight(theme.richTitleTextStyle ?? theme.titleTextStyle);
      content = Row(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            key: artworkSlotKey,
            width: theme.richIconSize,
            height: math.min(firstLine, theme.richIconSize),
            child: OverflowBox(
              maxWidth: theme.richIconSize,
              maxHeight: theme.richIconSize,
              child: art,
            ),
          ),
          SizedBox(width: theme.iconSpacing),
          Flexible(child: content),
        ],
      );
    }

    // Sized to its content, never to the space it is offered. A caller that places the card itself
    // measures it to decide where it goes -- a card that expanded to fill would measure as the whole
    // window and be pushed into the corner.
    return ConstrainedBox(
      constraints: BoxConstraints(maxWidth: theme.richMaxWidth),
      child: Material(
        color: Colors.transparent,
        child: Container(
          padding: theme.richPadding,
          decoration: BoxDecoration(
            color: theme.richBackgroundColor,
            borderRadius: BorderRadius.circular(theme.richBorderRadius),
            border: Border.all(
              color: theme.borderColor,
              width: theme.borderWidth,
            ),
            boxShadow: [
              BoxShadow(
                color: theme.shadowColor,
                blurRadius: theme.shadowBlurRadius,
                offset: Offset(0, theme.shadowOffsetY),
              ),
            ],
          ),
          child: content,
        ),
      ),
    );
  }
}

class _ShortcutChip extends StatelessWidget {
  final String shortcut;

  const _ShortcutChip({required this.shortcut});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<TooltipThemeExtension>()!;
    return Container(
      padding: theme.richShortcutPadding,
      decoration: BoxDecoration(
        color: theme.richShortcutBackgroundColor,
        borderRadius: BorderRadius.circular(theme.richShortcutBorderRadius),
        border: Border.all(
          color: theme.richShortcutBorderColor,
          width: theme.borderWidth,
        ),
      ),
      child: Text(
        shortcut,
        style: theme.richShortcutTextStyle ?? theme.messageTextStyle,
      ),
    );
  }
}
