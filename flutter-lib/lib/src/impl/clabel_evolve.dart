import 'package:flutter/material.dart';
import '../gen/clabel.dart';
import '../gen/widget.dart';
import '../gen/image.dart';
import '../gen/swt.dart';
import '../impl/canvas_evolve.dart';
import '../impl/color_utils.dart';
import './utils/image_utils.dart';
import './utils/widget_utils.dart';
import './utils/font_utils.dart';
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
    if (_isChipMode) return child;
    return super.wrapWithGCOverlay(child);
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<CLabelThemeExtension>()!;
    final enabled = state.enabled ?? true;

    final isInToolbar = ToolbarAreaMarker.of(context);
    if (isInToolbar && _isChipMode) {
      final rawText = state.text ?? '';
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
    final rawText = state.text ?? '';
    final text = rawText == '<none>' ? '' : rawText;
    final image = state.image;

    final textAlign = _getTextAlignFromAlignment(
      state.alignment,
      widgetTheme.textAlign,
    );
    final parentBg = ParentBackgroundScope.backgroundOf(context);
    final backgroundColor = getBackgroundColor(
      background: state.background,
      defaultColor: parentBg ?? widgetTheme.backgroundColor,
    );
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
      Opacity(
        opacity: enabled ? 1.0 : widgetTheme.disabledOpacity,
        child: Container(
          constraints: constraints,
          padding: padding,
          decoration: backgroundColor != null
              ? BoxDecoration(color: backgroundColor)
              : null,
          alignment: hasValidBounds
              ? getAlignmentFromTextAlign(textAlign)
              : null,
          child: child,
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
                      overflow: TextOverflow.ellipsis,
                      style: textStyle,
                    ),
                  )
                : Text(
                    text,
                    textAlign: textAlign,
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
                overflow: TextOverflow.ellipsis,
                style: textStyle,
              ),
            )
          : Text(
              text,
              textAlign: textAlign,
              overflow: TextOverflow.ellipsis,
              style: textStyle,
            );
    } else {
      // Empty CLabel
      return const SizedBox.shrink();
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
          renderAsIcon: false,
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
        renderAsIcon: false,
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
