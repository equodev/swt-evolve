// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'filedialog_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$FileDialogThemeExtensionTailorMixin
    on ThemeExtension<FileDialogThemeExtension> {
  TextStyle? get titleStyle;
  TextStyle? get messageStyle;
  TextStyle? get labelStyle;
  Color get backgroundColor;
  double get borderRadius;
  double get width;
  double get height;
  double get promptWidth;
  EdgeInsets get padding;
  double get titleContentSpacing;
  double get sectionSpacing;
  double get contentButtonsSpacing;
  double get buttonSpacing;
  TextStyle? get pathStyle;
  Color get iconColor;
  Color get disabledIconColor;
  double get iconSize;
  double get iconButtonSize;
  Color get folderIconColor;
  Color get fileIconColor;
  double get rowIconSize;
  double get rowIconSpacing;
  double get selectedOverlayOpacity;
  double get hoverOverlayOpacity;
  Color get progressColor;

  @override
  FileDialogThemeExtension copyWith({
    TextStyle? titleStyle,
    TextStyle? messageStyle,
    TextStyle? labelStyle,
    Color? backgroundColor,
    double? borderRadius,
    double? width,
    double? height,
    double? promptWidth,
    EdgeInsets? padding,
    double? titleContentSpacing,
    double? sectionSpacing,
    double? contentButtonsSpacing,
    double? buttonSpacing,
    TextStyle? pathStyle,
    Color? iconColor,
    Color? disabledIconColor,
    double? iconSize,
    double? iconButtonSize,
    Color? folderIconColor,
    Color? fileIconColor,
    double? rowIconSize,
    double? rowIconSpacing,
    double? selectedOverlayOpacity,
    double? hoverOverlayOpacity,
    Color? progressColor,
  }) {
    return FileDialogThemeExtension(
      titleStyle: titleStyle ?? this.titleStyle,
      messageStyle: messageStyle ?? this.messageStyle,
      labelStyle: labelStyle ?? this.labelStyle,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      borderRadius: borderRadius ?? this.borderRadius,
      width: width ?? this.width,
      height: height ?? this.height,
      promptWidth: promptWidth ?? this.promptWidth,
      padding: padding ?? this.padding,
      titleContentSpacing: titleContentSpacing ?? this.titleContentSpacing,
      sectionSpacing: sectionSpacing ?? this.sectionSpacing,
      contentButtonsSpacing:
          contentButtonsSpacing ?? this.contentButtonsSpacing,
      buttonSpacing: buttonSpacing ?? this.buttonSpacing,
      pathStyle: pathStyle ?? this.pathStyle,
      iconColor: iconColor ?? this.iconColor,
      disabledIconColor: disabledIconColor ?? this.disabledIconColor,
      iconSize: iconSize ?? this.iconSize,
      iconButtonSize: iconButtonSize ?? this.iconButtonSize,
      folderIconColor: folderIconColor ?? this.folderIconColor,
      fileIconColor: fileIconColor ?? this.fileIconColor,
      rowIconSize: rowIconSize ?? this.rowIconSize,
      rowIconSpacing: rowIconSpacing ?? this.rowIconSpacing,
      selectedOverlayOpacity:
          selectedOverlayOpacity ?? this.selectedOverlayOpacity,
      hoverOverlayOpacity: hoverOverlayOpacity ?? this.hoverOverlayOpacity,
      progressColor: progressColor ?? this.progressColor,
    );
  }

  @override
  FileDialogThemeExtension lerp(
    covariant ThemeExtension<FileDialogThemeExtension>? other,
    double t,
  ) {
    if (other is! FileDialogThemeExtension)
      return this as FileDialogThemeExtension;
    return FileDialogThemeExtension(
      titleStyle: TextStyle.lerp(titleStyle, other.titleStyle, t),
      messageStyle: TextStyle.lerp(messageStyle, other.messageStyle, t),
      labelStyle: TextStyle.lerp(labelStyle, other.labelStyle, t),
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      width: t < 0.5 ? width : other.width,
      height: t < 0.5 ? height : other.height,
      promptWidth: t < 0.5 ? promptWidth : other.promptWidth,
      padding: t < 0.5 ? padding : other.padding,
      titleContentSpacing: t < 0.5
          ? titleContentSpacing
          : other.titleContentSpacing,
      sectionSpacing: t < 0.5 ? sectionSpacing : other.sectionSpacing,
      contentButtonsSpacing: t < 0.5
          ? contentButtonsSpacing
          : other.contentButtonsSpacing,
      buttonSpacing: t < 0.5 ? buttonSpacing : other.buttonSpacing,
      pathStyle: TextStyle.lerp(pathStyle, other.pathStyle, t),
      iconColor: Color.lerp(iconColor, other.iconColor, t)!,
      disabledIconColor: Color.lerp(
        disabledIconColor,
        other.disabledIconColor,
        t,
      )!,
      iconSize: t < 0.5 ? iconSize : other.iconSize,
      iconButtonSize: t < 0.5 ? iconButtonSize : other.iconButtonSize,
      folderIconColor: Color.lerp(folderIconColor, other.folderIconColor, t)!,
      fileIconColor: Color.lerp(fileIconColor, other.fileIconColor, t)!,
      rowIconSize: t < 0.5 ? rowIconSize : other.rowIconSize,
      rowIconSpacing: t < 0.5 ? rowIconSpacing : other.rowIconSpacing,
      selectedOverlayOpacity: t < 0.5
          ? selectedOverlayOpacity
          : other.selectedOverlayOpacity,
      hoverOverlayOpacity: t < 0.5
          ? hoverOverlayOpacity
          : other.hoverOverlayOpacity,
      progressColor: Color.lerp(progressColor, other.progressColor, t)!,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is FileDialogThemeExtension &&
            const DeepCollectionEquality().equals(
              titleStyle,
              other.titleStyle,
            ) &&
            const DeepCollectionEquality().equals(
              messageStyle,
              other.messageStyle,
            ) &&
            const DeepCollectionEquality().equals(
              labelStyle,
              other.labelStyle,
            ) &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(width, other.width) &&
            const DeepCollectionEquality().equals(height, other.height) &&
            const DeepCollectionEquality().equals(
              promptWidth,
              other.promptWidth,
            ) &&
            const DeepCollectionEquality().equals(padding, other.padding) &&
            const DeepCollectionEquality().equals(
              titleContentSpacing,
              other.titleContentSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              sectionSpacing,
              other.sectionSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              contentButtonsSpacing,
              other.contentButtonsSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              buttonSpacing,
              other.buttonSpacing,
            ) &&
            const DeepCollectionEquality().equals(pathStyle, other.pathStyle) &&
            const DeepCollectionEquality().equals(iconColor, other.iconColor) &&
            const DeepCollectionEquality().equals(
              disabledIconColor,
              other.disabledIconColor,
            ) &&
            const DeepCollectionEquality().equals(iconSize, other.iconSize) &&
            const DeepCollectionEquality().equals(
              iconButtonSize,
              other.iconButtonSize,
            ) &&
            const DeepCollectionEquality().equals(
              folderIconColor,
              other.folderIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              fileIconColor,
              other.fileIconColor,
            ) &&
            const DeepCollectionEquality().equals(
              rowIconSize,
              other.rowIconSize,
            ) &&
            const DeepCollectionEquality().equals(
              rowIconSpacing,
              other.rowIconSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              selectedOverlayOpacity,
              other.selectedOverlayOpacity,
            ) &&
            const DeepCollectionEquality().equals(
              hoverOverlayOpacity,
              other.hoverOverlayOpacity,
            ) &&
            const DeepCollectionEquality().equals(
              progressColor,
              other.progressColor,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(titleStyle),
      const DeepCollectionEquality().hash(messageStyle),
      const DeepCollectionEquality().hash(labelStyle),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(width),
      const DeepCollectionEquality().hash(height),
      const DeepCollectionEquality().hash(promptWidth),
      const DeepCollectionEquality().hash(padding),
      const DeepCollectionEquality().hash(titleContentSpacing),
      const DeepCollectionEquality().hash(sectionSpacing),
      const DeepCollectionEquality().hash(contentButtonsSpacing),
      const DeepCollectionEquality().hash(buttonSpacing),
      const DeepCollectionEquality().hash(pathStyle),
      const DeepCollectionEquality().hash(iconColor),
      const DeepCollectionEquality().hash(disabledIconColor),
      const DeepCollectionEquality().hash(iconSize),
      const DeepCollectionEquality().hash(iconButtonSize),
      const DeepCollectionEquality().hash(folderIconColor),
      const DeepCollectionEquality().hash(fileIconColor),
      const DeepCollectionEquality().hash(rowIconSize),
      const DeepCollectionEquality().hash(rowIconSpacing),
      const DeepCollectionEquality().hash(selectedOverlayOpacity),
      const DeepCollectionEquality().hash(hoverOverlayOpacity),
      const DeepCollectionEquality().hash(progressColor),
    ]);
  }
}

extension FileDialogThemeExtensionBuildContextProps on BuildContext {
  FileDialogThemeExtension get fileDialogThemeExtension =>
      Theme.of(this).extension<FileDialogThemeExtension>()!;
  TextStyle? get titleStyle => fileDialogThemeExtension.titleStyle;
  TextStyle? get messageStyle => fileDialogThemeExtension.messageStyle;
  TextStyle? get labelStyle => fileDialogThemeExtension.labelStyle;
  Color get backgroundColor => fileDialogThemeExtension.backgroundColor;
  double get borderRadius => fileDialogThemeExtension.borderRadius;
  double get width => fileDialogThemeExtension.width;
  double get height => fileDialogThemeExtension.height;
  double get promptWidth => fileDialogThemeExtension.promptWidth;
  EdgeInsets get padding => fileDialogThemeExtension.padding;
  double get titleContentSpacing =>
      fileDialogThemeExtension.titleContentSpacing;
  double get sectionSpacing => fileDialogThemeExtension.sectionSpacing;
  double get contentButtonsSpacing =>
      fileDialogThemeExtension.contentButtonsSpacing;
  double get buttonSpacing => fileDialogThemeExtension.buttonSpacing;
  TextStyle? get pathStyle => fileDialogThemeExtension.pathStyle;
  Color get iconColor => fileDialogThemeExtension.iconColor;
  Color get disabledIconColor => fileDialogThemeExtension.disabledIconColor;
  double get iconSize => fileDialogThemeExtension.iconSize;
  double get iconButtonSize => fileDialogThemeExtension.iconButtonSize;
  Color get folderIconColor => fileDialogThemeExtension.folderIconColor;
  Color get fileIconColor => fileDialogThemeExtension.fileIconColor;
  double get rowIconSize => fileDialogThemeExtension.rowIconSize;
  double get rowIconSpacing => fileDialogThemeExtension.rowIconSpacing;
  double get selectedOverlayOpacity =>
      fileDialogThemeExtension.selectedOverlayOpacity;
  double get hoverOverlayOpacity =>
      fileDialogThemeExtension.hoverOverlayOpacity;
  Color get progressColor => fileDialogThemeExtension.progressColor;
}
