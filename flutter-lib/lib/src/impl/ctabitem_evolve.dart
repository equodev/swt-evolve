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
import 'ctabfolder_evolve.dart';

class CTabItemImpl<T extends CTabItemSwt, V extends VCTabItem>
    extends ItemImpl<T, V> {
  Widget? _buildImageWidget(
    BuildContext context,
    CTabItemThemeExtension itemTheme,
    CTabFolderThemeExtension folderTheme,
    VImage? image,
    Color? iconColor,
    bool enabled,
  ) {
    if (image == null) return null;
    final iconSize = folderTheme.tabIconSize;
    final imageKey = ImageUtils.stableImageKey(image);
    final futureKey =
        '${imageKey}_${iconSize}_${iconColor?.value ?? 'null'}_$enabled';
    return FutureBuilder<Widget?>(
      key: ValueKey(futureKey),
      future: ImageUtils.buildVImageAsync(
        image,
        size: iconSize,
        color: iconColor,
        enabled: enabled,
        useBinaryImage: true,
        renderAsIcon: true,
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          return snapshot.data ?? const SizedBox.shrink();
        }
        return SizedBox(width: iconSize, height: iconSize);
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final itemTheme = Theme.of(context).extension<CTabItemThemeExtension>()!;
    final folderTheme = Theme.of(
      context,
    ).extension<CTabFolderThemeExtension>()!;
    return buildTabItemContent(context, itemTheme, folderTheme);
  }

  Widget buildTabItemContent(
    BuildContext context,
    CTabItemThemeExtension itemTheme,
    CTabFolderThemeExtension folderTheme,
  ) {
    final text = stripAccelerators(state.text);
    final tabItemContext = TabItemContext.of(context);
    final isSelected = tabItemContext?.isSelected ?? false;
    final isEnabled = tabItemContext?.isEnabled ?? true;
    final textColor = !isEnabled
        ? itemTheme.tabItemDisabledTextColor
        : (isSelected
              ? itemTheme.tabItemSelectedTextColor
              : itemTheme.tabItemTextColor);
    final baseStyle = isSelected
        ? folderTheme.tabSelectedTextStyle
        : folderTheme.tabTextStyle;
    final textStyle = baseStyle?.copyWith(color: textColor) ??
        itemTheme.tabItemTextStyle?.copyWith(color: textColor) ??
        TextStyle(color: textColor);
    final preserveIconColors = getConfigFlags().preserve_icon_colors ?? true;
    final imageWidget = _buildImageWidget(
      context,
      itemTheme,
      folderTheme,
      state.image,
      preserveIconColors ? null : textColor,
      isEnabled,
    );

    return Padding(
      padding: EdgeInsets.only(right: itemTheme.tabItemHorizontalPadding),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          if (imageWidget != null)
            Padding(
              padding: EdgeInsets.only(
                bottom: itemTheme.tabItemVerticalPadding,
                right: itemTheme.tabItemImageTextSpacing,
              ),
              child: imageWidget,
            ),
          Padding(
            padding: EdgeInsets.only(bottom: itemTheme.tabItemVerticalPadding),
            child: Text(text, style: textStyle),
          ),
        ],
      ),
    );
  }

  void onCloseRequest() {}

  void onTabSelected() {}
}
