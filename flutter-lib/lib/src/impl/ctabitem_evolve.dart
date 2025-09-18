import 'dart:io';
import 'dart:typed_data' show Uint8List;

import 'package:flutter/material.dart';
import 'package:flutter/material.dart' as material;
import 'package:flutter/widgets.dart';
import '../gen/ctabitem.dart';
import '../gen/widget.dart';
import '../gen/image.dart';
import '../impl/item_evolve.dart';
import './utils/image_utils.dart';
import 'widget_config.dart';
import 'utils/text_utils.dart';

class CTabItemImpl<T extends CTabItemSwt, V extends VCTabItem>
    extends ItemImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  /// Helper method to build an image widget from VImage using ImageUtils
  Widget? _buildImageWidget(VImage? image) {
    return ImageUtils.buildVImageWidget(
      image,
      size: AppSizes.tabIconSize,
      enabled: true,
      useBinaryImage: false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return buildTabItemContent(context);
  }

  Widget buildTabItemContent(BuildContext context) {
    final imageWidget = _buildImageWidget(state.image);

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

  void onCloseRequest() {}

  void onTabSelected() {}
}
