import 'package:flutter/foundation.dart' show visibleForTesting;
import 'package:flutter/material.dart';
import '../gen/clabel.dart';
import '../gen/color.dart';
import '../gen/widget.dart';
import '../gen/image.dart';
import '../gen/swt.dart';
import '../impl/canvas_evolve.dart';
import '../impl/color_utils.dart';
import './utils/image_utils.dart';
import './utils/widget_utils.dart';
import './utils/font_utils.dart';
import './utils/text_utils.dart' show stripAccelerators;
import '../theme/theme_extensions/clabel_theme_extension.dart';
import '../theme/theme_extensions/toolitem_theme_extension.dart';
import '../theme/theme_extensions/toolbar_theme_extension.dart';
import '../custom/toolbar_composite.dart';

class CLabelImpl<T extends CLabelSwt, V extends VCLabel>
    extends CanvasImpl<T, V> {
  bool _isHovered = false;

  bool get _isChipMode =>
      state.cursor?.cursorStyle == SWT.CURSOR_HAND &&
      state.image != null &&
      ((state.text?.isNotEmpty == true && state.text != '<none>') ||
          state.toolTipText?.isNotEmpty == true);

  @override
  Widget wrapWithGCOverlay(Widget child) {
    return child;
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<CLabelThemeExtension>()!;
    final enabled = state.enabled ?? true;

    final isInToolbar = ToolbarAreaMarker.of(context);
    if (isInToolbar && _isChipMode) {
      final rawText = stripAccelerators(state.text);
      final effectiveText = rawText == '<none>' ? '' : rawText;
      return wrap(_buildToolbarChip(context, widgetTheme, enabled, effectiveText, state.image!));
    }

    return _buildCLabel(context, widgetTheme, enabled);
  }

  Widget _buildToolbarChip(
    BuildContext context,
    CLabelThemeExtension clabelTheme,
    bool enabled,
    String text,
    VImage image,
  ) {
    final itemTheme = Theme.of(context).extension<ToolItemThemeExtension>()!;

    final textColor = getForegroundColor(
      context: context,
      foreground: state.foreground,
      defaultColor: clabelTheme.primaryTextColor,
    );
    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: clabelTheme.primaryTextStyle,
    );

    return MouseRegion(
      onEnter: enabled ? (_) => setState(() => _isHovered = true) : null,
      onExit: enabled ? (_) => setState(() => _isHovered = false) : null,
      child: Align(
        alignment: Alignment.center,
        child: Container(
          padding: EdgeInsets.fromLTRB(
            (state.leftMargin ?? 0).toDouble(),
            (state.topMargin ?? 0).toDouble(),
            (state.rightMargin ?? 0).toDouble(),
            (state.bottomMargin ?? 0).toDouble(),
          ),
          decoration: BoxDecoration(
            color: _isHovered
                ? itemTheme.hoverColor
                : itemTheme.segmentUnselectedBackgroundColor,
            borderRadius: BorderRadius.circular(itemTheme.segmentBorderRadius),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              _buildImageWidget(image, enabled, size: clabelTheme.iconSize) ?? const SizedBox.shrink(),
              if (text.isNotEmpty) ...[
                SizedBox(width: clabelTheme.iconTextSpacing),
                Text(
                  text,
                  style: textStyle,
                  softWrap: false,
                  overflow: TextOverflow.clip,
                ),
              ],
              for (final addonImage in ChipAddonImages.of(context) ?? [])
                _buildImageWidget(addonImage, enabled, size: clabelTheme.iconSize) ?? const SizedBox.shrink(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildCLabel(
    BuildContext context,
    CLabelThemeExtension widgetTheme,
    bool enabled,
  ) {
    // Treat "<none>" as empty text
    final rawText = stripAccelerators(state.text);
    final text = rawText == '<none>' ? '' : rawText;
    final image = state.image;

    final textAlign = _getTextAlignFromAlignment(
      state.alignment,
      widgetTheme.textAlign,
    );
    final parentBg = ParentBackgroundScope.backgroundOf(context);
    final resolvedBackground = getBackgroundColor(
      background: state.background,
      defaultColor: parentBg ?? widgetTheme.backgroundColor,
    );
    // Gate on backgroundImage (not background, which Java always resolves non-null):
    // stay transparent so an inherited image, already painted by the ancestor, shows through.
    final stayTransparentForImage = state.backgroundImage == null &&
        ParentBackgroundScope.backgroundImageOf(context) != null;
    var backgroundColor = stayTransparentForImage ? null : resolvedBackground;
    // CLabel.onPaint draws the image when there is one, the gradient otherwise, and the plain
    // background only when it has neither; Java keeps at most one of the two set. The application
    // chose these colours and this image for the label, so use_swt_colors does not gate them the
    // way it does the plain background.
    final tiledImage = ImageUtils.buildTiledBackgroundImage(state.backgroundImage);
    Gradient? gradient;
    if (state.backgroundImage == null) {
      final gradientColors = state.gradientColors;
      if (gradientColors != null && gradientColors.length == 1) {
        backgroundColor = colorFromVColor(gradientColors.first);
      } else if (gradientColors != null && gradientColors.length > 1) {
        gradient = clabelGradient(
          colors: gradientColors,
          percents: state.gradientPercents ?? const [],
          vertical: state.gradientVertical ?? false,
          background: resolvedBackground ?? Colors.transparent,
        );
      }
    }
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    final child = _buildCLabelContent(
      context,
      widgetTheme,
      enabled,
      text,
      image,
      textAlign,
      hasValidBounds,
    );

    final dm = widgetTheme.defaultMargin;
    final padding = EdgeInsets.fromLTRB(
        state.leftMargin?.toDouble() ?? dm,
        state.topMargin?.toDouble() ?? dm,
        state.rightMargin?.toDouble() ?? dm,
        state.bottomMargin?.toDouble() ?? dm,
      );

    return wrap(
      exposeSemanticsTapAction(
        Opacity(
          opacity: enabled ? 1.0 : widgetTheme.disabledOpacity,
          child: Container(
            constraints: constraints,
            padding: padding,
            decoration: backgroundColor != null ||
                    gradient != null ||
                    tiledImage != null
                ? BoxDecoration(
                    color: backgroundColor,
                    gradient: gradient,
                    image: tiledImage,
                  )
                : null,
            alignment: hasValidBounds
                ? getAlignmentFromTextAlign(textAlign)
                : null,
            child: child,
          ),
        ),
      ),
    );
  }

  Widget _buildCLabelContent(
    BuildContext context,
    CLabelThemeExtension widgetTheme,
    bool enabled,
    String text,
    VImage? image,
    TextAlign textAlign,
    bool hasValidBounds,
  ) {
    final textColor = enabled
        ? getForegroundColor(
            context: context,
            foreground: state.foreground,
            defaultColor: widgetTheme.primaryTextColor,
          )
        : widgetTheme.disabledTextColor;

    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: enabled
          ? widgetTheme.primaryTextStyle
          : widgetTheme.disabledTextStyle,
    );

    Widget? imageWidget;
    if (image != null) {
      imageWidget = _buildImageWidget(image, enabled);
    }

    if (image != null && text.isNotEmpty) {
      // Image + Text
      return Row(
        mainAxisSize: hasValidBounds ? MainAxisSize.max : MainAxisSize.min,
        mainAxisAlignment: getMainAxisAlignmentFromTextAlign(
          textAlign,
          widgetTheme.mainAxisAlignment,
        ),
        crossAxisAlignment: widgetTheme.crossAxisAlignment,
        children: [
          imageWidget!,
          SizedBox(width: widgetTheme.iconTextSpacing),
          Flexible(
            child: hasValidBounds
                ? ConstrainedBox(
                    constraints: BoxConstraints(
                      maxWidth: state.bounds!.width.toDouble(),
                      maxHeight: state.bounds!.height.toDouble(),
                    ),
                    child: Text(
                      text,
                      textAlign: textAlign,
                      softWrap: false,
                      overflow: TextOverflow.ellipsis,
                      style: textStyle,
                    ),
                  )
                : Text(
                    text,
                    textAlign: textAlign,
                    softWrap: false,
                    overflow: TextOverflow.ellipsis,
                    style: textStyle,
                  ),
          ),
        ],
      );
    } else if (image != null) {
      // Only image
      return imageWidget!;
    } else if (text.isNotEmpty) {
      // Only text
      return hasValidBounds
          ? ConstrainedBox(
              constraints: BoxConstraints(
                maxWidth: state.bounds!.width.toDouble(),
                maxHeight: state.bounds!.height.toDouble(),
              ),
              child: Text(
                text,
                textAlign: textAlign,
                softWrap: false,
                overflow: TextOverflow.ellipsis,
                style: textStyle,
              ),
            )
          : Text(
              text,
              textAlign: textAlign,
              softWrap: false,
              overflow: TextOverflow.ellipsis,
              style: textStyle,
            );
    } else {
      // Empty CLabel: still reserves one line of text height, matching native SWT (a CLabel is
      // a text-bearing widget and stays laid out at font-line height even with no text set yet,
      // e.g. a label column reused as a spacer/divider next to a field). SizedBox.shrink() here
      // collapsed to just the margins, in turn undersizing CLabelSizes.computeSize() (which is
      // measured off this same render) and losing the whole row's visual height.
      return Text('', style: textStyle);
    }
  }

  Widget? _buildImageWidget(VImage? image, bool enabled, {double? size}) {
    if (image == null) return null;
    final w = size ?? image.imageData?.width?.toDouble();
    final h = size ?? image.imageData?.height?.toDouble();
    final validW = (w != null && w > 0) ? w : null;
    final validH = (h != null && h > 0) ? h : null;
    final imageKey = ImageUtils.stableImageKey(image);
    final futureKey = '${imageKey}_${validW}_${validH}_$enabled';
    // sync fallback shown immediately while async loads
    final fallback = ImageUtils.buildVImage(
          image,
          width: validW,
          height: validH,
          enabled: enabled,
          useBinaryImage: true,
          renderAsIcon: true,
        ) ??
        SizedBox(width: validW ?? 0, height: validH ?? 0);
    return FutureBuilder<Widget?>(
      key: ValueKey(futureKey),
      future: ImageUtils.buildVImageAsync(
        image,
        width: validW,
        height: validH,
        enabled: enabled,
        useBinaryImage: true,
        renderAsIcon: true,
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          return snapshot.data ?? fallback;
        }
        return fallback;
      },
    );
  }

  TextAlign _getTextAlignFromAlignment(int? alignment, TextAlign defaultAlign) {
    if (alignment != null) {
      if ((alignment & SWT.CENTER) != 0) return TextAlign.center;
      if ((alignment & SWT.RIGHT) != 0) return TextAlign.right;
      if ((alignment & SWT.LEFT) != 0) return TextAlign.left;
    }
    return defaultAlign;
  }
}

/// What `CLabel.onPaint` draws for `setBackground(Color[], int[], boolean)` with two or more
/// colours: colour `i` runs into colour `i + 1` over the span that ends at `percents[i]` of the
/// width (of the height when [vertical]) and starts where the previous one ended, and whatever
/// lies past the last percent is the widget's [background], not the last colour.
///
/// A colour Java was given as null is already the background by the time it is sent: SWT stores
/// the background in its place when the gradient is set.
@visibleForTesting
Gradient clabelGradient({
  required List<VColor> colors,
  required List<int> percents,
  required bool vertical,
  required Color background,
}) {
  // Java sends one percent fewer than colours; tolerate a list that is out of step rather than
  // paint nothing.
  final spans = percents.length < colors.length - 1
      ? percents.length
      : colors.length - 1;
  final stops = <double>[0.0];
  for (var i = 0; i < spans; i++) {
    final previous = stops.last;
    stops.add((percents[i] / 100.0).clamp(previous, 1.0));
  }
  final painted = <Color>[
    for (var i = 0; i <= spans; i++) colorFromVColor(colors[i]),
  ];
  if (stops.last < 1.0) {
    // A hard edge: two stops at the same place.
    painted.add(background);
    stops.add(stops.last);
  }
  return LinearGradient(
    begin: vertical ? Alignment.topCenter : Alignment.centerLeft,
    end: vertical ? Alignment.bottomCenter : Alignment.centerRight,
    colors: painted,
    stops: stops,
  );
}
