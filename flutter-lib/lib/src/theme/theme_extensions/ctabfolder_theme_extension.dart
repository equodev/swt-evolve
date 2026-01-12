import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'ctabfolder_theme_extension.tailor.dart';
part 'ctabfolder_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
@OffsetConverter()
class CTabFolderThemeExtension extends ThemeExtension<CTabFolderThemeExtension> with _$CTabFolderThemeExtensionTailorMixin {
  // Tab bar colors
  final Color tabBarBackgroundColor;
  final Color tabBarBorderColor;
  
  // Tab colors
  final Color tabBackgroundColor;
  final Color tabSelectedBackgroundColor;
  final Color tabHoverBackgroundColor;
  final Color tabDisabledBackgroundColor;
  
  // Tab border colors
  final Color tabBorderColor;
  final Color tabSelectedBorderColor;
  final Color tabHoverBorderColor;
  final Color tabDisabledBorderColor;
  
  // Tab text colors
  final Color tabTextColor;
  final Color tabSelectedTextColor;
  final Color tabHoverTextColor;
  final Color tabDisabledTextColor;
  
  // Tab text opacity (for unselected tabs)
  final double tabUnselectedTextOpacity;
  
  // Tab close button opacity
  final double tabCloseButtonSelectedOpacity;
  final double tabCloseButtonUnselectedOpacity;
  
  // Highlight color (for selected tab top border)
  final Color tabHighlightColor;
  
  // Tab border properties
  final double tabBorderWidth;
  final double tabSelectedBorderWidth;
  final double tabHighlightBorderWidth;
  final double tabBorderRadius;
  
  // Tab padding
  final double tabHorizontalPadding;
  final double tabVerticalPadding;
  
  // Tab typography
  final TextStyle? tabTextStyle;
  final TextStyle? tabSelectedTextStyle;
  
  // Tab content area
  final Color tabContentBackgroundColor;
  final Color tabContentBorderColor;
  
  // Tab sizes
  final double tabIconSize;
  final double tabCloseIconSize;
  final double controlButtonSize;
  
  // Tab spacing
  final double tabIconTextSpacing;
  final double tabCloseButtonSpacing;
  
  // Tab icon/text padding
  final double tabIconBottomPadding;
  final double tabTextBottomPadding;
  final double tabCloseIconBottomPadding;
  
  // Control buttons (minimize/maximize)
  final Color controlButtonColor;
  final Color controlButtonHoverColor;
  final double controlButtonHorizontalPadding;
  final double controlButtonSpacing;
  final double controlButtonScale;
  
  // Hover reveal animation
  final Duration hoverRevealDuration;
  final Duration hoverHideDuration;
  
  // Scrollbar
  final double tabScrollbarThickness;
  
  // Top right controls overlay shadow
  final Color topRightControlsShadowColor;
  final double topRightControlsShadowOpacity;
  final double topRightControlsShadowBlurRadius;
  final Offset topRightControlsShadowOffset;
  
  // Top right controls padding
  final EdgeInsets topRightControlsPadding;
  
  // Scrollbar hide delay
  final Duration scrollbarHideDelay;
  
  const CTabFolderThemeExtension({
    required this.tabBarBackgroundColor,
    required this.tabBarBorderColor,
    required this.tabBackgroundColor,
    required this.tabSelectedBackgroundColor,
    required this.tabHoverBackgroundColor,
    required this.tabDisabledBackgroundColor,
    required this.tabBorderColor,
    required this.tabSelectedBorderColor,
    required this.tabHoverBorderColor,
    required this.tabDisabledBorderColor,
    required this.tabTextColor,
    required this.tabSelectedTextColor,
    required this.tabHoverTextColor,
    required this.tabDisabledTextColor,
    required this.tabUnselectedTextOpacity,
    required this.tabCloseButtonSelectedOpacity,
    required this.tabCloseButtonUnselectedOpacity,
    required this.tabHighlightColor,
    required this.tabBorderWidth,
    required this.tabSelectedBorderWidth,
    required this.tabHighlightBorderWidth,
    required this.tabBorderRadius,
    required this.tabHorizontalPadding,
    required this.tabVerticalPadding,
    this.tabTextStyle,
    this.tabSelectedTextStyle,
    required this.tabContentBackgroundColor,
    required this.tabContentBorderColor,
    required this.tabIconSize,
    required this.tabCloseIconSize,
    required this.controlButtonSize,
    required this.tabIconTextSpacing,
    required this.tabCloseButtonSpacing,
    required this.tabIconBottomPadding,
    required this.tabTextBottomPadding,
    required this.tabCloseIconBottomPadding,
    required this.controlButtonColor,
    required this.controlButtonHoverColor,
    required this.controlButtonHorizontalPadding,
    required this.controlButtonSpacing,
    required this.controlButtonScale,
    required this.hoverRevealDuration,
    required this.hoverHideDuration,
    required this.tabScrollbarThickness,
    required this.topRightControlsShadowColor,
    required this.topRightControlsShadowOpacity,
    required this.topRightControlsShadowBlurRadius,
    required this.topRightControlsShadowOffset,
    required this.topRightControlsPadding,
    required this.scrollbarHideDelay,
  });

  factory CTabFolderThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$CTabFolderThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$CTabFolderThemeExtensionToJson(this);
}

