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
import '../theme/theme_extensions/ctabitem_theme_extension.dart';
import '../theme/theme_extensions/ctabfolder_theme_extension.dart';
import 'utils/widget_utils.dart';

class CTabItemImpl<T extends CTabItemSwt, V extends VCTabItem>
    extends ItemImpl<T, V> {
  final shouldShowImage = false;

  Widget? _buildImageWidget(
      BuildContext context,
      CTabItemThemeExtension itemTheme,
      CTabFolderThemeExtension folderTheme,
      VImage? image) {
    return ImageUtils.buildVImage(
      image,
      size: folderTheme.tabIconSize,
      enabled: true,
      useBinaryImage: false,
      renderAsIcon: true,
    );
  }

  @override
  Widget build(BuildContext context) {
    final itemTheme = Theme.of(context).extension<CTabItemThemeExtension>()!;
    final folderTheme = Theme.of(context).extension<CTabFolderThemeExtension>()!;
    return buildTabItemContent(context, itemTheme, folderTheme);
  }

  Widget buildTabItemContent(
      BuildContext context,
      CTabItemThemeExtension itemTheme,
      CTabFolderThemeExtension folderTheme) {
    final imageWidget = _buildImageWidget(context, itemTheme, folderTheme, state.image);

    final text = stripAccelerators(state.text) ?? "";
    final textStyle = itemTheme.tabItemTextStyle;
    final textColor = itemTheme.tabItemTextColor;

    return Padding(
      padding: EdgeInsets.only(
        right: itemTheme.tabItemHorizontalPadding,
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          if (imageWidget != null && shouldShowImage)
            Padding(
              padding: EdgeInsets.only(
                bottom: itemTheme.tabItemVerticalPadding,
                right: itemTheme.tabItemImageTextSpacing,
              ),
              child: imageWidget,
            ),
          Padding(
            padding: EdgeInsets.only(
              bottom: itemTheme.tabItemVerticalPadding,
            ),
            child: Text(
              text,
              style: textStyle?.copyWith(color: textColor) ??
                  TextStyle(color: textColor),
            ),
          ),
        ],
      ),
    );
  }

  void onCloseRequest() {}

  void onTabSelected() {}
}
