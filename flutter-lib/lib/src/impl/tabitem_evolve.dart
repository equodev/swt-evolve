import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/tabitem.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/item_evolve.dart';
import './utils/image_utils.dart';
import '../theme/theme_extensions/tabitem_theme_extension.dart';
import 'utils/widget_utils.dart';
import 'utils/text_utils.dart';

class TabItemImpl<T extends TabItemSwt, V extends VTabItem>
    extends ItemImpl<T, V> {
  /// Helper method to build an image widget from VImage using ImageUtils
  Widget? _buildImageWidget(TabItemThemeExtension widgetTheme) {
    if (state.image == null) return null;

    return ImageUtils.buildVImage(
      state.image,
      size: widgetTheme.iconSize,
      enabled: true,
      useBinaryImage: false,
      renderAsIcon: true,
    );
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TabItemThemeExtension>()!;
    final imageWidget = _buildImageWidget(widgetTheme);

    final textColor = widgetTheme.textColor;
    final textStyle = (widgetTheme.textStyle ?? const TextStyle()).copyWith(color: textColor);

    final alignment = getMainAxisAlignmentFromTextAlign(
      getTextAlignFromStyle(state.style, TextAlign.start),
      MainAxisAlignment.start,
    );

    return Padding(
      padding: widgetTheme.containerPadding,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: alignment,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          if (imageWidget != null)
            Padding(
              padding: widgetTheme.imagePadding,
              child: imageWidget,
            ),
          Padding(
            padding: widgetTheme.textPadding,
            child: Text(
              stripAccelerators(state.text) ?? "",
              style: textStyle,
            ),
          ),
        ],
      ),
    );
  }
}
