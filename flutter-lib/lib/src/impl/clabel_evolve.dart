import 'package:flutter/material.dart';
import '../gen/clabel.dart';
import '../gen/widget.dart';
import '../gen/image.dart';
import '../gen/swt.dart';
import '../impl/canvas_evolve.dart';
import '../impl/color_utils.dart';
import './utils/image_utils.dart';
import './utils/widget_utils.dart';
import '../theme/theme_extensions/clabel_theme_extension.dart';

class CLabelImpl<T extends CLabelSwt, V extends VCLabel>
    extends CanvasImpl<T, V> {

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<CLabelThemeExtension>()!;

    final enabled = state.enabled ?? true;

    return _buildCLabel(context, widgetTheme, enabled);
  }

  Widget _buildCLabel(BuildContext context, CLabelThemeExtension widgetTheme, bool enabled) {
    // Treat "<none>" as empty text
    final rawText = state.text ?? '';
    final text = rawText == '<none>' ? '' : rawText;
    final image = state.image;

    final textAlign = _getTextAlignFromAlignment(state.alignment, widgetTheme.textAlign);
    final backgroundColor = getBackgroundColor(
      background: state.background,
      defaultColor: widgetTheme.backgroundColor,
    );
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    final child = _buildCLabelContent(context, widgetTheme, enabled, text, image, textAlign, hasValidBounds);

    // Margins from state
    final padding = EdgeInsets.fromLTRB(
      (state.leftMargin ?? 0).toDouble(),
      (state.topMargin ?? 0).toDouble(),
      (state.rightMargin ?? 0).toDouble(),
      (state.bottomMargin ?? 0).toDouble(),
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
          alignment: hasValidBounds ? getAlignmentFromTextAlign(textAlign) : null,
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

    final textStyle = enabled
        ? widgetTheme.primaryTextStyle?.copyWith(color: textColor)!
        : widgetTheme.disabledTextStyle?.copyWith(color: textColor)!;

    Widget? imageWidget;
    if (image != null) {
      imageWidget = _buildImageWidget(image, enabled, widgetTheme.iconSize);
    }

    if (image != null && text.isNotEmpty) {
      // Image + Text
      return Row(
        mainAxisSize: hasValidBounds ? MainAxisSize.max : MainAxisSize.min,
        mainAxisAlignment: getMainAxisAlignmentFromTextAlign(textAlign, widgetTheme.mainAxisAlignment),
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

  Widget _buildImageWidget(VImage? image, bool enabled, double iconSize) {
    return FutureBuilder<Widget?>(
      future: ImageUtils.buildVImageAsync(
        image,
        width: image?.imageData?.width?.toDouble() ?? iconSize,
        height: image?.imageData?.height?.toDouble() ?? iconSize,
        enabled: enabled,
        constraints: null,
        useBinaryImage: true,
        renderAsIcon: false,
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          return snapshot.data ?? const SizedBox.shrink();
        }
        // Show placeholder while loading
        return SizedBox(
          width: iconSize,
          height: iconSize,
          child: Center(
            child: SizedBox(
              width: iconSize * 0.75,
              height: iconSize * 0.75,
              child: const CircularProgressIndicator(strokeWidth: 2),
            ),
          ),
        );
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
