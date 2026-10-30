import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import '../gen/label.dart';
import '../gen/swt.dart';
import '../gen/widgets.dart' show WireKey;
import '../impl/control_evolve.dart';
import '../impl/color_utils.dart';
import '../impl/config_flags.dart';
import '../impl/widget_config.dart';
import './utils/cached_text.dart';
import './utils/image_utils.dart';
import './utils/font_utils.dart';
import './utils/mnemonic_text.dart';
import './utils/text_utils.dart';
import './utils/widget_utils.dart';
import '../theme/theme_extensions/label_theme_extension.dart';

class LabelImpl<T extends LabelSwt, V extends VLabel>
    extends ControlImpl<T, V> {
  /// What a single-line label shows of its text, font and foreground is its Text alone: a change of
  /// nothing else that keeps it on one line rebuilds only that Text, which follows [_textTicks].
  static const _textOnly = {WireKey.k_Label_text, WireKey.k_Control_font, WireKey.k_Control_foreground};
  final _textTicks = ValueNotifier<int>(0);
  bool _builtSingleLine = false;

  /// The single line's Text, the same widget while what it is built from outside the state stays
  /// the same, so that a rebuild of the label for anything else leaves it alone.
  Widget? _lineText;
  (TextAlign, LabelThemeExtension, bool, ConfigFlags)? _lineTextKey;

  /// The single line's style, and the font and foreground it was resolved from: a change of text
  /// keeps both, so the style is not resolved again.
  TextStyle? _lineStyle;
  Object? _lineStyleFont;
  Object? _lineStyleForeground;

  @override
  void setValue(V value) {
    final change = lastChange;
    if (mounted &&
        _builtSingleLine &&
        (change?.isOnly(_textOnly) ?? false) &&
        _fitsOneLine(stripAccelerators(state.text))) {
      _textTicks.value++;
      return;
    }
    // The label rebuilds; its Text only if what it shows changed.
    if (change != null && _textOnly.any(change.touches)) _textTicks.value++;
    super.setValue(value);
  }

  @override
  void dispose() {
    _textTicks.dispose();
    super.dispose();
  }

  /// The text conditions for a single line; the rest of them do not depend on the text.
  static bool _fitsOneLine(String text) => text.isNotEmpty && !hasLineDelimiter(text);

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<LabelThemeExtension>()!;

    final enabled = state.enabled ?? true;

    // SWT.VERTICAL is the same bit as SWT.V_SCROLL, and a Label reads it only for
    // separators -- a text label can carry it without ever having asked for it.
    if (hasStyle(state.style, SWT.SEPARATOR)) {
      return _buildSeparator(context, widgetTheme);
    }

    // Handle regular label
    return _buildLabel(context, widgetTheme, enabled);
  }

  Widget _buildSeparator(
    BuildContext context,
    LabelThemeExtension widgetTheme,
  ) {
    final isVertical = hasStyle(state.style, SWT.VERTICAL);
    final separatorColor = widgetTheme.borderColor;
    final thickness = widgetTheme.separatorThickness;

    final hasValidBounds = hasBounds(state.bounds);
    final constraints = hasValidBounds
        ? getConstraintsFromBounds(state.bounds)
        : isVertical
        ? BoxConstraints(maxHeight: 7, maxWidth: thickness)
        : BoxConstraints(maxWidth: 7, maxHeight: thickness);

    Widget separator;
    if (isVertical) {
      separator = VerticalDivider(
        color: separatorColor,
        indent: 0,
        endIndent: 0,
      );
    } else {
      separator = Divider(color: separatorColor, indent: 0, endIndent: 0);
    }

    return Focus(
      // Not a tab stop: Tab passes over it, as on every native; focus can still be set.
      skipTraversal: true,
      onFocusChange: (hasFocus) {
        if (hasFocus) {
          widget.sendFocusFocusIn(state, null);
        } else {
          widget.sendFocusFocusOut(state, null);
        }
      },
      child: wrap(
        constraints != null
            ? SizedBox(
                width: constraints.maxWidth,
                height: constraints.maxHeight,
                child: separator,
              )
            : separator,
      ),
    );
  }

  Widget _buildLabel(
    BuildContext context,
    LabelThemeExtension widgetTheme,
    bool enabled,
  ) {
    final text = stripAccelerators(state.text);
    final textAlign = getTextAlignFromStyle(state.style, widgetTheme.textAlign);
    // state.background already resolves inheritance (DartControl.getExplicitBackground(), Java
    // side); ParentBackgroundScope would unconditionally shadow it with a stale ancestor value.
    // An ancestor's background image is already painted behind this label; an inherited colour
    // would cover it, so only a colour the label set itself is painted over it.
    final stayTransparentForImage = state.backgroundImage == null &&
        !(state.hasOwnBackground ?? false) &&
        ParentBackgroundScope.backgroundImageOf(context) != null;
    final backgroundColor = stayTransparentForImage
        ? null
        : getBackgroundColor(
            background: state.background, defaultColor: null, context: context);
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    final padding = widgetTheme.padding.resolve(TextDirection.ltr);
    final shouldWrap = shouldWrapText(
      style: state.style,
      hasValidBounds: hasValidBounds,
      text: text,
    );
    // Squeezing a too-long line into its bounds is a single-line decision:
    // on multi-line text it would only ever see the first delimiter, and Java
    // has already sized the label for every line.
    final singleLine = hasValidBounds && !shouldWrap && _fitsOneLine(text) && state.image == null;
    _builtSingleLine = singleLine;

    final Widget child = singleLine
        ? _LabelLineFit(
            padding: padding,
            boundsWidth: state.bounds!.width.toDouble(),
            boundsHeight: state.bounds!.height.toDouble(),
            alignment: Alignment(
                _horizontalOf(getMainAxisAlignmentFromTextAlign(
                    textAlign, widgetTheme.mainAxisAlignment)),
                -1), // From the top, as in _contentAlignment.
            child: _lineTextFor(widgetTheme, enabled, textAlign),
          )
        : _buildLabelContent(
            context,
            widgetTheme,
            enabled,
            text,
            textAlign,
            hasValidBounds,
            _labelTextStyle(context, widgetTheme, enabled),
          );

    return Focus(
      // Not a tab stop: Tab passes over it, as on every native; focus can still be set.
      skipTraversal: true,
      onFocusChange: (hasFocus) {
        if (hasFocus) {
          widget.sendFocusFocusIn(state, null);
        } else {
          widget.sendFocusFocusOut(state, null);
        }
      },
      child: wrap(
          opacity: enabled ? 1.0 : widgetTheme.disabledOpacity,
          Container(
            constraints: constraints,
            padding: singleLine ? null : padding,
            margin: widgetTheme.margin,
            decoration: backgroundColor != null
                ? BoxDecoration(
                    color: backgroundColor,
                    borderRadius: BorderRadius.circular(
                      widgetTheme.borderRadius,
                    ),
                    border: widgetTheme.borderColor != null
                        ? Border.all(
                            color: widgetTheme.borderColor!,
                            width: widgetTheme.borderWidth,
                          )
                        : null,
                  )
                : null,
            child: hasValidBounds && !singleLine
                ? Align(
                    alignment: _contentAlignment(textAlign),
                    child: child,
                  )
                : child,
          ),
        ),
    );
  }

  Widget _lineTextFor(LabelThemeExtension widgetTheme, bool enabled, TextAlign textAlign) {
    // The configuration decides whether the application's font colours apply.
    final key = (textAlign, widgetTheme, enabled, getConfigFlags());
    final kept = _lineText;
    if (kept != null && key == _lineTextKey) return kept;
    _lineTextKey = key;
    _lineStyle = null;
    return _lineText = ValueListenableBuilder<int>(
      valueListenable: _textTicks,
      builder: (context, _, __) {
        final mnemonic = parseMnemonic(state.text);
        final style = _lineStyleIn(context, widgetTheme, enabled);
        final line = LabelLineText(mnemonic.text, textAlign: textAlign, style: style);
        if (mnemonic.index < 0) return line;
        // Only a label with a mnemonic follows Alt, as MnemonicText does.
        return ValueListenableBuilder<bool>(
          valueListenable: MnemonicCue.shown,
          builder: (context, shown, _) => !shown
              ? line
              : Text.rich(
                  TextSpan(children: mnemonicSpans(mnemonic.text, mnemonic.index, underline: true)),
                  style: style,
                  textAlign: textAlign,
                  softWrap: false,
                  overflow: TextOverflow.ellipsis,
                  maxLines: 1,
                ),
        );
      },
    );
  }

  TextStyle _lineStyleIn(BuildContext context, LabelThemeExtension widgetTheme, bool enabled) {
    final font = state.font;
    final foreground = state.foreground;
    final kept = _lineStyle;
    if (kept != null && identical(font, _lineStyleFont) && identical(foreground, _lineStyleForeground)) {
      return kept;
    }
    _lineStyleFont = font;
    _lineStyleForeground = foreground;
    return _lineStyle = _labelTextStyle(context, widgetTheme, enabled);
  }

  /// A Label's text starts at the top of its bounds on every native (unlike CLabel, which centres
  /// it); an image keeps its vertical centre.
  Alignment _contentAlignment(TextAlign textAlign) {
    final centred = getAlignmentFromTextAlign(textAlign);
    return state.image != null ? centred : Alignment(centred.x, -1);
  }

  /// Where a row aligned by [alignment] puts its one child across its width.
  static double _horizontalOf(MainAxisAlignment alignment) => switch (alignment) {
        MainAxisAlignment.center || MainAxisAlignment.spaceAround || MainAxisAlignment.spaceEvenly => 0,
        MainAxisAlignment.end => 1,
        _ => -1,
      };

  TextStyle _labelTextStyle(
    BuildContext context,
    LabelThemeExtension widgetTheme,
    bool enabled,
  ) {
    final textColor = enabled
        ? getForegroundColor(
            foreground: state.foreground,
            defaultColor: widgetTheme.primaryTextColor,
            context: context,
            minContrast: widgetTheme.minTextContrast,
          )
        : widgetTheme.disabledTextColor;

    final baseTextStyle = enabled
        ? widgetTheme.primaryTextStyle
        : widgetTheme.disabledTextStyle;

    return getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: baseTextStyle,
    );
  }

  Widget _buildLabelContent(
    BuildContext context,
    LabelThemeExtension widgetTheme,
    bool enabled,
    String text,
    TextAlign textAlign,
    bool hasValidBounds,
    TextStyle textStyle,
  ) {
    final shouldWrap = shouldWrapText(
      style: state.style,
      hasValidBounds: hasValidBounds,
      text: text,
    );

    // Without SWT.WRAP the label never wraps on its own, but it still breaks on
    // the delimiters the text already carries -- clamping to a single line
    // would drop every line after the first.
    final isMultiLine = hasLineDelimiter(text);

    Widget textWidget = MnemonicText(
      state.text,
      textAlign: textAlign,
      softWrap: shouldWrap,
      overflow: shouldWrap ? TextOverflow.visible : TextOverflow.ellipsis,
      maxLines: shouldWrap || isMultiLine ? null : 1,
      style: textStyle,
    );

    // Handle image
    Widget? imageWidget;
    if (state.image != null) {
      final imgW = state.image?.imageData?.width?.toDouble() ?? widgetTheme.iconSize;
      final imgH = state.image?.imageData?.height?.toDouble() ?? widgetTheme.iconSize;
      imageWidget = SizedBox(
        width: imgW,
        height: imgH,
        child: ImageUtils.buildVImage(
          state.image,
          width: imgW,
          height: imgH,
          enabled: enabled,
          constraints: null,
          useBinaryImage: true,
          renderAsIcon: true,
        ),
      );
    }

    // Build content row
    return Row(
      mainAxisSize: hasValidBounds ? MainAxisSize.max : MainAxisSize.min,
      mainAxisAlignment: getMainAxisAlignmentFromTextAlign(
        textAlign,
        widgetTheme.mainAxisAlignment,
      ),
      crossAxisAlignment: widgetTheme.crossAxisAlignment,
      children: [
        if (imageWidget != null) ...[
          imageWidget,
          if (text.isNotEmpty) SizedBox(width: widgetTheme.iconTextSpacing),
        ],
        if (text.isNotEmpty)
          Flexible(
            child: hasValidBounds
                ? ConstrainedBox(
                    constraints: BoxConstraints(
                      maxWidth: state.bounds!.width.toDouble(),
                      maxHeight: state.bounds!.height.toDouble(),
                    ),
                    child: textWidget,
                  )
                : textWidget,
          ),
      ],
    );
  }
}

/// A single line of label text inside bounds, with the label's padding, laid out with one shaping.
///
/// The text is laid out once at its natural width, and that width decides what the label does with
/// it: a line too wide for the padding drops the horizontal padding, and one too wide for the bounds
/// by at most 40% is scaled down to fit. Anything else is laid out again at the available width, where
/// it ellipsizes; a paragraph laid out again at a new width keeps its shaped text.
class _LabelLineFit extends SingleChildRenderObjectWidget {
  const _LabelLineFit({
    required this.padding,
    required this.boundsWidth,
    required this.boundsHeight,
    required this.alignment,
    required super.child,
  });

  final EdgeInsets padding;
  final double boundsWidth;
  final double boundsHeight;
  final Alignment alignment;

  @override
  RenderLabelLineFit createRenderObject(BuildContext context) =>
      RenderLabelLineFit(padding, boundsWidth, boundsHeight, alignment);

  @override
  void updateRenderObject(BuildContext context, RenderLabelLineFit renderObject) {
    renderObject
      ..padding = padding
      ..boundsWidth = boundsWidth
      ..boundsHeight = boundsHeight
      ..alignment = alignment;
  }
}

class RenderLabelLineFit extends RenderBox with RenderObjectWithChildMixin<RenderBox> {
  RenderLabelLineFit(this._padding, this._boundsWidth, this._boundsHeight, this._alignment);

  EdgeInsets _padding;
  set padding(EdgeInsets value) {
    if (value == _padding) return;
    _padding = value;
    markNeedsLayout();
  }

  double _boundsWidth;
  set boundsWidth(double value) {
    if (value == _boundsWidth) return;
    _boundsWidth = value;
    markNeedsLayout();
  }

  double _boundsHeight;
  set boundsHeight(double value) {
    if (value == _boundsHeight) return;
    _boundsHeight = value;
    markNeedsLayout();
  }

  Alignment _alignment;
  set alignment(Alignment value) {
    if (value == _alignment) return;
    _alignment = value;
    markNeedsLayout();
  }

  double _scale = 1;
  Offset _childOffset = Offset.zero;

  @override
  void performLayout() {
    final child = this.child;
    if (child == null) {
      size = constraints.smallest;
      return;
    }
    child.layout(const BoxConstraints(), parentUsesSize: true);
    final natural = child.size.width;
    var pad = natural > _boundsWidth - _padding.horizontal
        ? EdgeInsets.only(top: _padding.top, bottom: _padding.bottom)
        : _padding;
    // A label laid out at its font's line height (a common GridData.heightHint, taken from
    // GC.getFontMetrics()) has no room for the theme's vertical padding; native SWT draws the line in
    // exactly that height, so give up the padding rather than clip the glyphs.
    if (child.size.height > _boundsHeight - pad.vertical) {
      pad = EdgeInsets.only(left: pad.left, right: pad.right);
    }
    final inner = constraints.deflate(pad);
    final maxWidth = math.min(inner.maxWidth, _boundsWidth);
    final maxHeight = math.min(inner.maxHeight, _boundsHeight);
    _scale = 1;
    if (natural > _boundsWidth && natural <= _boundsWidth * 1.4) {
      final s = math.min(maxWidth / child.size.width, maxHeight / child.size.height);
      if (s < 1) _scale = math.max(s, 0);
    } else if (child.size.width > maxWidth || child.size.height > maxHeight) {
      child.layout(BoxConstraints(maxWidth: math.max(maxWidth, 0), maxHeight: math.max(maxHeight, 0)),
          parentUsesSize: true);
    }
    final drawn = child.size * _scale;
    size = constraints.hasBoundedWidth && constraints.hasBoundedHeight
        ? constraints.biggest
        : constraints.constrain(Size(drawn.width + pad.horizontal, drawn.height + pad.vertical));
    final free = Size(size.width - pad.horizontal - drawn.width, size.height - pad.vertical - drawn.height);
    _childOffset = Offset(
      pad.left + free.width * (_alignment.x + 1) / 2,
      pad.top + free.height * (_alignment.y + 1) / 2,
    );
  }

  @override
  Size computeDryLayout(BoxConstraints constraints) {
    if (constraints.hasBoundedWidth && constraints.hasBoundedHeight) return constraints.biggest;
    final childSize = child?.getDryLayout(const BoxConstraints()) ?? Size.zero;
    return constraints.constrain(Size(childSize.width + _padding.horizontal, childSize.height + _padding.vertical));
  }

  @override
  double computeMinIntrinsicHeight(double width) =>
      (child?.getMinIntrinsicHeight(double.infinity) ?? 0) + _padding.vertical;

  @override
  double computeMaxIntrinsicHeight(double width) =>
      (child?.getMaxIntrinsicHeight(double.infinity) ?? 0) + _padding.vertical;

  @override
  double computeMinIntrinsicWidth(double height) =>
      (child?.getMinIntrinsicWidth(double.infinity) ?? 0) + _padding.horizontal;

  @override
  double computeMaxIntrinsicWidth(double height) =>
      (child?.getMaxIntrinsicWidth(double.infinity) ?? 0) + _padding.horizontal;

  Matrix4 get _transform => Matrix4.translationValues(_childOffset.dx, _childOffset.dy, 0)
    ..scale(_scale, _scale, 1);

  @override
  void paint(PaintingContext context, Offset offset) {
    final child = this.child;
    if (child == null) return;
    if (_scale == 1) {
      layer = null;
      context.paintChild(child, offset + _childOffset);
      return;
    }
    layer = context.pushTransform(needsCompositing, offset, _transform,
        (context, offset) => context.paintChild(child, offset),
        oldLayer: layer is TransformLayer ? layer as TransformLayer : null);
  }

  @override
  bool hitTestChildren(BoxHitTestResult result, {required Offset position}) {
    final child = this.child;
    if (child == null) return false;
    return result.addWithPaintTransform(
      transform: _transform,
      position: position,
      hitTest: (result, position) => child.hitTest(result, position: position),
    );
  }

  @override
  void applyPaintTransform(RenderBox child, Matrix4 transform) {
    transform.multiply(_transform);
  }
}

/// The single line of a label: its text ellipsized on one line, drawn as [ColorCachedText] draws it.
@visibleForTesting
class LabelLineText extends ColorCachedText {
  const LabelLineText(super.data, {super.textAlign, required TextStyle super.style})
      : super(softWrap: false, overflow: TextOverflow.ellipsis, maxLines: 1);
}
