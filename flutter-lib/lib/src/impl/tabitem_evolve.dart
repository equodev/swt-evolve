import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/tabitem.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/item_evolve.dart';
import '../impl/ctabfolder_evolve.dart';
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

    final tabItemContext = TabItemContext.of(context);
    final isEnabled = tabItemContext?.isEnabled ?? true;
    final isSelected = tabItemContext?.isSelected ?? false;
    // VTabItem carries no colour or font of its own, so a tab takes the TabFolder's -- except
    // the selected one, whose surface stays the theme's: the folder's foreground was chosen
    // against the folder's background, and applying it there is what leaves text unreadable.
    final textColor = getForegroundColor(
      foreground: isSelected ? null : ParentForegroundScope.of(context),
      defaultColor: !isEnabled
          ? widgetTheme.disabledTextColor
          : (isSelected ? widgetTheme.selectedTextColor : widgetTheme.textColor),
      context: context,
    );
    final textStyle = getTextStyle(
      context: context,
      font: ParentForegroundScope.fontOf(context),
      textColor: textColor,
      baseTextStyle: widgetTheme.textStyle,
    );

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
            Padding(padding: widgetTheme.imagePadding, child: imageWidget),
          Padding(
            padding: widgetTheme.textPadding,
            child: Text(stripAccelerators(state.text), style: textStyle),
          ),
        ],
      ),
    );
  }
}
