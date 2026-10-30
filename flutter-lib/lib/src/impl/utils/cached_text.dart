import 'package:flutter/animation.dart';
import 'package:flutter/foundation.dart' show listEquals;
import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';

import 'font_utils.dart';

/// A [Text] of a single style that keeps the text it laid out for the styles it showed last.
///
/// A paragraph is shaped with its colour, so [Text] shapes its text again when only the colour
/// changes, and enabling or disabling a control does exactly that to its label. This keeps the
/// shaped text of the last two styles, so a label going back to a colour it had is only painted.
/// Labels showing the same text in the same style share one shaped text, so many controls changing
/// colour together, as an animation does, shape it once. What it draws is what [Text] draws with
/// the same arguments.
///
/// It is a [Text], so whatever looks a label up by its text finds it.
class ColorCachedText extends Text {
  const ColorCachedText(
    super.data, {
    super.key,
    super.style,
    super.textAlign,
    super.softWrap,
    super.overflow,
    super.maxLines,
  }) : animatedStyle = null;

  /// A label whose style follows [animatedStyle]: each step of it repaints the label, or lays it out
  /// again if it changes more than colours, without rebuilding it.
  const ColorCachedText.animated(
    super.data, {
    super.key,
    required Animation<TextStyle> this.animatedStyle,
    super.textAlign,
    super.softWrap,
    super.overflow,
    super.maxLines,
  });

  final Animation<TextStyle>? animatedStyle;

  @override
  Widget build(BuildContext context) {
    final leaf = _leaf(context);
    if (leaf.overflow != TextOverflow.fade) return leaf;
    // A cached text clips where it would fade.
    final source = animatedStyle;
    if (source == null) return super.build(context);
    return ListenableBuilder(
      listenable: source,
      builder: (context, _) => Text(data!,
          style: source.value, textAlign: textAlign, softWrap: softWrap, overflow: overflow, maxLines: maxLines),
    );
  }

  /// The width [singleLineTextWidth] measures for [data] in [measured], read off the text this label
  /// lays out in [context], or null when that text is not shaped as [measured] would shape it.
  ///
  /// The shaped text is kept for the label built next with the same text and style, so a build that
  /// checks whether its text fits does not shape it a second time.
  double? measuredWidth(BuildContext context, TextStyle measured) {
    if (animatedStyle != null) return null;
    final leaf = _leaf(context);
    if (leaf.textDirection != TextDirection.ltr || !_shapedAlike(leaf.style, measured)) return null;
    final fontSize = leaf.style.fontSize ?? kDefaultFontSize;
    if (leaf.textScaler.scale(fontSize) != fontSize) return null;
    return RenderCachedText._maxIntrinsicWidthOf(leaf._setup, leaf.style);
  }

  /// Whether text in [a] and in [b] comes out of shaping the same: what they differ in, if anything,
  /// is only painted, or only sets the line height. A weight, slant or spacing left unset is the
  /// engine's default for it.
  static bool _shapedAlike(TextStyle a, TextStyle b) =>
      a.fontFamily == b.fontFamily &&
      listEquals(a.fontFamilyFallback, b.fontFamilyFallback) &&
      a.fontSize == b.fontSize &&
      (a.fontWeight ?? FontWeight.normal) == (b.fontWeight ?? FontWeight.normal) &&
      (a.fontStyle ?? FontStyle.normal) == (b.fontStyle ?? FontStyle.normal) &&
      (a.letterSpacing ?? 0) == (b.letterSpacing ?? 0) &&
      (a.wordSpacing ?? 0) == (b.wordSpacing ?? 0) &&
      a.locale == b.locale &&
      listEquals(a.fontFeatures, b.fontFeatures) &&
      listEquals(a.fontVariations, b.fontVariations);

  _CachedTextLeaf _leaf(BuildContext context) {
    // Resolved the way Text resolves them.
    final defaultTextStyle = DefaultTextStyle.of(context);
    final bold = MediaQuery.boldTextOf(context);
    TextStyle resolve(TextStyle? style) {
      var effectiveStyle = style;
      if (style == null || style.inherit) effectiveStyle = defaultTextStyle.style.merge(style);
      if (bold) effectiveStyle = effectiveStyle!.merge(const TextStyle(fontWeight: FontWeight.bold));
      return effectiveStyle!;
    }

    final effectiveStyle = resolve(animatedStyle?.value ?? style);
    final effectiveOverflow = overflow ?? effectiveStyle.overflow ?? defaultTextStyle.overflow;
    return _CachedTextLeaf(
      text: data!,
      style: effectiveStyle,
      animatedStyle: animatedStyle,
      resolve: resolve,
      textAlign: textAlign ?? defaultTextStyle.textAlign ?? TextAlign.start,
      textDirection: Directionality.of(context),
      locale: locale ?? Localizations.maybeLocaleOf(context),
      softWrap: softWrap ?? defaultTextStyle.softWrap,
      overflow: effectiveOverflow,
      maxLines: maxLines ?? defaultTextStyle.maxLines,
      textScaler: MediaQuery.textScalerOf(context),
      textWidthBasis: defaultTextStyle.textWidthBasis,
      textHeightBehavior: defaultTextStyle.textHeightBehavior ?? DefaultTextHeightBehavior.maybeOf(context),
    );
  }
}

/// An [Icon] whose glyph is drawn as [ColorCachedText] draws a label: icons of the same glyph, size
/// and colour share one shaped glyph, and all of them follow system font changes through one
/// listener rather than one each, as a [RichText] would. What it draws is what [Icon] draws with the
/// same arguments.
///
/// It is an [Icon], so whatever looks an icon up finds it.
class CachedIcon extends Icon {
  const CachedIcon(super.icon, {super.key, super.size, super.color, super.semanticLabel, super.textDirection});

  /// What [Icon.build] builds, with the glyph drawn by a [RenderCachedText] set up as [RichText] sets
  /// up its paragraph.
  @override
  Widget build(BuildContext context) {
    final textDirection = this.textDirection ?? Directionality.of(context);
    final iconTheme = IconTheme.of(context);
    final applyTextScaling = this.applyTextScaling ?? iconTheme.applyTextScaling ?? false;
    final tentativeIconSize = size ?? iconTheme.size ?? kDefaultFontSize;
    final iconSize = applyTextScaling ? MediaQuery.textScalerOf(context).scale(tentativeIconSize) : tentativeIconSize;
    final iconFill = fill ?? iconTheme.fill;
    final iconWeight = weight ?? iconTheme.weight;
    final iconGrade = grade ?? iconTheme.grade;
    final iconOpticalSize = opticalSize ?? iconTheme.opticalSize;
    final iconShadows = shadows ?? iconTheme.shadows;

    final icon = this.icon;
    if (icon == null) {
      return Semantics(label: semanticLabel, child: SizedBox(width: iconSize, height: iconSize));
    }

    final iconOpacity = iconTheme.opacity ?? 1.0;
    Color? iconColor = color ?? iconTheme.color!;
    Paint? foreground;
    if (iconOpacity != 1.0) {
      iconColor = iconColor.withValues(alpha: iconColor.a * iconOpacity);
    }
    if (blendMode != null) {
      foreground = Paint()
        ..blendMode = blendMode!
        ..color = iconColor;
      iconColor = null;
    }

    final fontStyle = TextStyle(
      fontVariations: <FontVariation>[
        if (iconFill != null) FontVariation('FILL', iconFill),
        if (iconWeight != null) FontVariation('wght', iconWeight),
        if (iconGrade != null) FontVariation('GRAD', iconGrade),
        if (iconOpticalSize != null) FontVariation('opsz', iconOpticalSize),
      ],
      inherit: false,
      color: iconColor,
      fontSize: iconSize,
      fontFamily: icon.fontFamily,
      fontWeight: fontWeight,
      package: icon.fontPackage,
      fontFamilyFallback: icon.fontFamilyFallback,
      shadows: iconShadows,
      height: 1.0,
      leadingDistribution: TextLeadingDistribution.even,
      foreground: foreground,
    );

    Widget iconWidget = _CachedTextLeaf(
      text: String.fromCharCode(icon.codePoint),
      style: fontStyle,
      animatedStyle: null,
      resolve: RenderCachedText._unresolved,
      textAlign: TextAlign.start,
      textDirection: textDirection,
      locale: Localizations.maybeLocaleOf(context),
      softWrap: true,
      overflow: TextOverflow.visible,
      maxLines: null,
      textScaler: TextScaler.noScaling,
      textWidthBasis: TextWidthBasis.parent,
      textHeightBehavior: null,
    );

    if (icon.matchTextDirection && textDirection == TextDirection.rtl) {
      iconWidget = Transform(
        transform: Matrix4.identity()..scaleByDouble(-1.0, 1.0, 1.0, 1),
        alignment: Alignment.center,
        transformHitTests: false,
        child: iconWidget,
      );
    }

    return Semantics(
      label: semanticLabel,
      child: ExcludeSemantics(
        child: SizedBox(width: iconSize, height: iconSize, child: Center(child: iconWidget)),
      ),
    );
  }
}

class _CachedTextLeaf extends LeafRenderObjectWidget {
  const _CachedTextLeaf({
    required this.text,
    required this.style,
    required this.animatedStyle,
    required this.resolve,
    required this.textAlign,
    required this.textDirection,
    required this.locale,
    required this.softWrap,
    required this.overflow,
    required this.maxLines,
    required this.textScaler,
    required this.textWidthBasis,
    required this.textHeightBehavior,
  });

  final String text;
  final TextStyle style;
  final Animation<TextStyle>? animatedStyle;
  final TextStyle Function(TextStyle) resolve;
  final TextAlign textAlign;
  final TextDirection textDirection;
  final Locale? locale;
  final bool softWrap;
  final TextOverflow overflow;
  final int? maxLines;
  final TextScaler textScaler;
  final TextWidthBasis textWidthBasis;
  final TextHeightBehavior? textHeightBehavior;

  _TextSetup get _setup => (
        text: text,
        textAlign: textAlign,
        textDirection: textDirection,
        locale: locale,
        softWrap: softWrap,
        overflow: overflow,
        maxLines: maxLines,
        textScaler: textScaler,
        textWidthBasis: textWidthBasis,
        textHeightBehavior: textHeightBehavior,
      );

  @override
  RenderCachedText createRenderObject(BuildContext context) =>
      RenderCachedText(_setup, style)..follow(animatedStyle, resolve);

  @override
  void updateRenderObject(BuildContext context, RenderCachedText renderObject) {
    renderObject
      ..setup = _setup
      ..style = style
      ..follow(animatedStyle, resolve);
  }
}

typedef _TextSetup = ({
  String text,
  TextAlign textAlign,
  TextDirection textDirection,
  Locale? locale,
  bool softWrap,
  TextOverflow overflow,
  int? maxLines,
  TextScaler textScaler,
  TextWidthBasis textWidthBasis,
  TextHeightBehavior? textHeightBehavior,
});

/// A text and its style, hashed once: a style hashes every one of its properties each time it is
/// asked, and a label whose colour is animated asks for a new one on every step.
class _PainterKey {
  _PainterKey(this.setup, this.style) : hashCode = Object.hash(setup, style);

  final _TextSetup setup;
  final TextStyle style;

  @override
  final int hashCode;

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is _PainterKey && other.hashCode == hashCode && other.setup == setup && other.style == style;
}

/// A laid-out painter, shared by every [RenderCachedText] holding the same text in the same style.
class _SharedPainter {
  _SharedPainter(this.key, this.painter);

  final _PainterKey key;
  final TextPainter painter;

  /// The widths [painter] was last laid out at, by whichever of its users.
  (double, double)? widths;
  int users = 0;
}

/// Lays out and paints like a [RenderParagraph] of one [TextSpan], keeping a laid-out painter per
/// style for the last two styles.
@visibleForTesting
class RenderCachedText extends RenderBox {
  RenderCachedText(this._setup, this._style);

  static const _kept = 2;
  static const _ellipsis = '…';

  _TextSetup _setup;
  set setup(_TextSetup value) {
    if (value == _setup) return;
    _setup = value;
    _forget();
    markNeedsLayout();
    markNeedsSemanticsUpdate();
  }

  TextStyle _style;

  /// The style the text is painted in.
  TextStyle get style => _style;

  set style(TextStyle value) {
    if (value == _style) return;
    final comparison = value.compareTo(_style);
    _style = value;
    _current = null;
    if (comparison == RenderComparison.layout) {
      _forget();
      markNeedsLayout();
    } else if (comparison != RenderComparison.identical) {
      markNeedsPaint();
    }
  }

  static final _shared = <_PainterKey, _SharedPainter>{};

  /// Painters shaped for [ColorCachedText.measuredWidth] that no label has taken up yet.
  static final _unclaimed = <_PainterKey>{};
  static const _maxUnclaimed = 512;

  static _SharedPainter _newShared(_PainterKey key) {
    final _PainterKey(:setup, :style) = key;
    return _SharedPainter(
      key,
      TextPainter(
        text: TextSpan(text: setup.text, style: style),
        textAlign: setup.textAlign,
        textDirection: setup.textDirection,
        locale: setup.locale,
        textScaler: setup.textScaler,
        maxLines: setup.maxLines,
        ellipsis: setup.overflow == TextOverflow.ellipsis ? _ellipsis : null,
        textWidthBasis: setup.textWidthBasis,
        textHeightBehavior: setup.textHeightBehavior,
      ),
    );
  }

  /// The widest the text of [setup] in [style] lays out, from the painter labels of that text and
  /// style share, shaping it for them if none holds one yet.
  static double _maxIntrinsicWidthOf(_TextSetup setup, TextStyle style) {
    final key = _PainterKey(setup, style);
    var shared = _shared[key];
    if (shared == null) {
      if (_unclaimed.length >= _maxUnclaimed) _dropUnclaimed();
      shared = _shared[key] = _newShared(key);
      _unclaimed.add(key);
    }
    if (shared.widths == null) {
      shared.painter.layout();
      shared.widths = (0, double.infinity);
    }
    return shared.painter.maxIntrinsicWidth;
  }

  static void _dropUnclaimed() {
    for (final key in _unclaimed) {
      final shared = _shared[key];
      if (shared != null && shared.users == 0) {
        _shared.remove(key);
        shared.painter.dispose();
      }
    }
    _unclaimed.clear();
  }

  /// The attached labels, which a change of the system fonts lays out again.
  static final _attached = <RenderCachedText>{};
  static bool _watchingFonts = false;

  /// A font that finishes loading changes how text lays out: every shared painter is laid out again
  /// by the label that paints it next.
  static void _fontsChanged() {
    for (final shared in _shared.values) {
      shared.painter.markNeedsLayout();
      shared.widths = null;
    }
    for (final label in _attached) {
      label.markNeedsLayout();
    }
  }

  /// How many painters all labels hold between them.
  @visibleForTesting
  static int get sharedPainters => _shared.length;

  Animation<TextStyle>? _styleSource;
  TextStyle Function(TextStyle) _resolve = _unresolved;

  static TextStyle _unresolved(TextStyle style) => style;

  /// Takes the style from [source], resolved by [resolve], whenever it changes.
  void follow(Animation<TextStyle>? source, TextStyle Function(TextStyle) resolve) {
    _resolve = resolve;
    if (identical(source, _styleSource)) return;
    if (attached) {
      _styleSource?.removeListener(_onStyleStep);
      source?.addListener(_onStyleStep);
    }
    _styleSource = source;
  }

  void _styleChanged() => style = _resolve(_styleSource!.value);

  /// [_styleChanged] as one closure: an animation finds a listener to remove by hashing it, and a
  /// fresh tear-off is hashed anew each time.
  late final VoidCallback _onStyleStep = _styleChanged;

  @override
  void attach(PipelineOwner owner) {
    super.attach(owner);
    _styleSource?.addListener(_onStyleStep);
    _attached.add(this);
    if (!_watchingFonts) _watchingFonts = FontUtils.listenToSystemFonts(_fontsChanged);
  }

  @override
  void detach() {
    _styleSource?.removeListener(_onStyleStep);
    _attached.remove(this);
    super.detach();
  }

  /// Laid-out painters, the most recently used last: at most [_kept], so found by comparing styles,
  /// which tells two colours apart at the first property that differs, rather than by hashing.
  final _painters = <_SharedPainter>[];

  /// The painter of the current style: looked up by style only when the style changes, since hashing
  /// a style costs more than painting short text.
  _SharedPainter? _current;
  double _minWidth = 0;
  double _maxWidth = double.infinity;
  bool _needsClipping = false;

  @visibleForTesting
  int get keptPainters => _painters.length;

  /// Whether the text was cut short, as [RenderParagraph.didExceedMaxLines] tells it.
  @visibleForTesting
  bool get didExceedMaxLines => _laidOut(_minWidth, _maxWidth).didExceedMaxLines;

  _SharedPainter get _painter => _current ??= _painterFor();

  _SharedPainter _painterFor() {
    for (var i = 0; i < _painters.length; i++) {
      final held = _painters[i];
      if (held.key.style == _style) {
        _painters
          ..removeAt(i)
          ..add(held);
        return held;
      }
    }
    final key = _PainterKey(_setup, _style);
    final shared = _shared[key] ??= _newShared(key);
    if (shared.users++ == 0) _unclaimed.remove(key);
    while (_painters.length >= _kept) {
      _release(_painters.removeAt(0));
    }
    _painters.add(shared);
    return shared;
  }

  static void _release(_SharedPainter shared) {
    if (--shared.users > 0) return;
    _shared.remove(shared.key);
    shared.painter.dispose();
  }

  void _forget() {
    _painters.forEach(_release);
    _painters.clear();
    _current = null;
  }

  /// The painter of the current style laid out at these widths. Its other users may have laid it
  /// out at theirs since this one last did.
  TextPainter _laidOut(double minWidth, double maxWidth) {
    final wraps = _setup.softWrap || _setup.overflow == TextOverflow.ellipsis;
    final widths = (minWidth, wraps ? maxWidth : double.infinity);
    final shared = _painter;
    if (shared.widths != widths) {
      shared.painter.layout(minWidth: widths.$1, maxWidth: widths.$2);
      shared.widths = widths;
    }
    return shared.painter;
  }

  @override
  double computeMinIntrinsicWidth(double height) => _laidOut(0, double.infinity).minIntrinsicWidth;

  @override
  double computeMaxIntrinsicWidth(double height) => _laidOut(0, double.infinity).maxIntrinsicWidth;

  @override
  double computeMinIntrinsicHeight(double width) => _laidOut(width, width).height;

  @override
  double computeMaxIntrinsicHeight(double width) => _laidOut(width, width).height;

  @override
  Size computeDryLayout(BoxConstraints constraints) {
    final painter = _laidOut(constraints.minWidth, constraints.maxWidth);
    final size = constraints.constrain(painter.size);
    // The painter is laid out again for the real layout.
    _laidOut(_minWidth, _maxWidth);
    return size;
  }

  @override
  double? computeDistanceToActualBaseline(TextBaseline baseline) =>
      _laidOut(_minWidth, _maxWidth).computeDistanceToActualBaseline(baseline);

  @override
  void performLayout() {
    _minWidth = constraints.minWidth;
    _maxWidth = constraints.maxWidth;
    final painter = _laidOut(_minWidth, _maxWidth);
    final textSize = painter.size;
    size = constraints.constrain(textSize);
    final overflowed = size.width < textSize.width || size.height < textSize.height || painter.didExceedMaxLines;
    _needsClipping = overflowed && _setup.overflow != TextOverflow.visible;
  }

  @override
  void paint(PaintingContext context, Offset offset) {
    final painter = _laidOut(_minWidth, _maxWidth);
    if (_needsClipping) {
      final canvas = context.canvas
        ..save()
        ..clipRect(offset & size);
      painter.paint(canvas, offset);
      canvas.restore();
    } else {
      painter.paint(context.canvas, offset);
    }
  }

  @override
  bool hitTestSelf(Offset position) => true;

  /// As a [RenderParagraph] of one plain span describes itself: its label joins the node it sits in.
  @override
  void describeSemanticsConfiguration(SemanticsConfiguration config) {
    super.describeSemanticsConfiguration(config);
    config
      ..label = _setup.text
      ..textDirection = _setup.textDirection;
  }

  @override
  void dispose() {
    _forget();
    super.dispose();
  }
}
