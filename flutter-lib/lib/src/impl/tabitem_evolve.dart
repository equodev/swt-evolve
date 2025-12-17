import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/tabitem.dart';
import '../gen/widget.dart';
import '../impl/item_evolve.dart';
import './utils/image_utils.dart';
import 'widget_config.dart';
import 'utils/text_utils.dart';

class TabItemImpl<T extends TabItemSwt, V extends VTabItem>
    extends ItemImpl<T, V> {
  /// Helper method to build an image widget from VImage using ImageUtils
  Widget? _buildImageWidget() {
    if (state.image == null) return null;

    return ImageUtils.buildVImage(
      state.image,
      size: AppSizes.tabIconSize,
      enabled: true,
      useBinaryImage: false,
      renderAsIcon: true,
    );
  }

  @override
  Widget build(BuildContext context) {
    return buildTabItemContent(context);
  }

  Widget buildTabItemContent(BuildContext context) {
    final imageWidget = _buildImageWidget();

    return Padding(
      padding: const EdgeInsets.only(right: 2.0),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          if (imageWidget != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 1.0, right: 3.0),
              child: imageWidget,
            ),
          Padding(
            padding: const EdgeInsets.only(bottom: 2.0),
            child: Text(
              stripAccelerators(state.text) ?? "",
              style: TextStyle(
                fontSize: 12,
                color: AppColors.getTextColor(),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
