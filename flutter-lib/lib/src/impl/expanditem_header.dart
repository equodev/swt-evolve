import 'package:flutter/material.dart';
import '../gen/expanditem.dart';
import '../theme/theme_extensions/expanditem_theme_extension.dart';
import 'utils/image_utils.dart';

/// The header of an ExpandItem: its image, its text and the chevron, inside the theme's header
/// padding. Java sizes the bar from this row's insets, so it is the one place a header is laid out.
class ExpandItemHeader extends StatelessWidget {
  const ExpandItemHeader({
    super.key,
    required this.item,
    required this.theme,
    required this.textStyle,
    this.chevron,
    this.height,
    this.color,
  });

  final VExpandItem item;
  final ExpandItemThemeExtension theme;
  final TextStyle textStyle;

  /// The expand/collapse indicator at the end of the row, or nothing.
  final Widget? chevron;

  /// The height Java gives the header; null lets the content and the vertical padding decide.
  final double? height;

  final Color? color;

  @override
  Widget build(BuildContext context) {
    final image = _buildImage();
    return AnimatedContainer(
      duration: theme.animationDuration,
      curve: theme.animationCurve,
      color: color,
      height: height,
      padding: height == null
          ? theme.headerPadding
          : theme.headerPadding.copyWith(top: 0, bottom: 0),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (image != null) ...[image, SizedBox(width: theme.imageTextSpacing)],
          Expanded(
            child: Text(
              item.text ?? '',
              style: textStyle,
              overflow: TextOverflow.ellipsis,
            ),
          ),
          ?chevron,
        ],
      ),
    );
  }

  Widget? _buildImage() {
    final imageData = item.image?.imageData;
    if (imageData == null) return null;
    final width = imageData.width?.toDouble() ?? theme.iconSize;
    final height = imageData.height?.toDouble() ?? theme.iconSize;
    final built = ImageUtils.buildVImage(
      item.image,
      width: width,
      height: height,
      enabled: true,
      constraints: null,
      useBinaryImage: true,
      renderAsIcon: true,
    );
    // The slot stays the image's size even when it cannot be drawn: Java reserves it from the bounds.
    return SizedBox(width: width, height: height, child: built);
  }
}
