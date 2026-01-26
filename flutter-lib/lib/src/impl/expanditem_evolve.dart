import 'package:flutter/material.dart';
import '../gen/expanditem.dart';
import '../gen/widgets.dart';
import '../impl/item_evolve.dart';
import '../theme/theme_extensions/expanditem_theme_extension.dart';
import 'utils/widget_utils.dart';
import 'utils/image_utils.dart';

class ExpandItemImpl<T extends ExpandItemSwt, V extends VExpandItem>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ExpandItemThemeExtension>()!;

    final textColor = widgetTheme.foregroundColor;
    final backgroundColor = widgetTheme.backgroundColor;
    final borderColor = widgetTheme.borderColor;

    final textStyle = getTextStyle(
      context: context,
      font: null,
      textColor: textColor,
      baseTextStyle: widgetTheme.headerTextStyle,
    );

    Widget? content;
    if (state.control != null) {
      content = mapWidgetFromValue(state.control!);
    }

    return Container(
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border.all(
          color: borderColor,
          width: widgetTheme.borderWidth,
        ),
        borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
      ),
      clipBehavior: Clip.antiAlias,
      child: IntrinsicHeight(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          mainAxisSize: MainAxisSize.min,
          children: [
            // Header
            if (state.text != null || (state.image != null && state.image?.imageData != null))
              Container(
                color: widgetTheme.headerBackgroundColor,
                padding: widgetTheme.headerPadding,
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    if (state.image != null && state.image?.imageData != null) ...[
                      _buildImage(widgetTheme),
                      SizedBox(width: widgetTheme.imageTextSpacing),
                    ],
                    if (state.text != null)
                      Expanded(
                        child: Text(
                          state.text!,
                          style: textStyle,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                  ],
                ),
              ),
            // Content
            if (content != null)
              Flexible(
                child: Container(
                  color: widgetTheme.contentBackgroundColor,
                  padding: widgetTheme.contentPadding,
                  constraints: (state.height != null && state.height! > 0)
                      ? BoxConstraints(
                          minHeight: state.height!.toDouble(),
                          maxHeight: state.height!.toDouble(),
                        )
                      : null,
                  alignment: Alignment.topLeft,
                  child: content,
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildImage(ExpandItemThemeExtension widgetTheme) {
    final imageData = state.image?.imageData;
    if (imageData == null) return const SizedBox.shrink();

    final double imageWidth = imageData.width?.toDouble() ?? widgetTheme.iconSize;
    final double imageHeight = imageData.height?.toDouble() ?? widgetTheme.iconSize;

    final builtImage = ImageUtils.buildVImage(
      state.image,
      width: imageWidth,
      height: imageHeight,
      enabled: true,
      constraints: null,
      useBinaryImage: true,
      renderAsIcon: false,
    );

    if (builtImage != null) {
      return SizedBox(
        width: imageWidth,
        height: imageHeight,
        child: builtImage,
      );
    }

    return const SizedBox.shrink();
  }
}
