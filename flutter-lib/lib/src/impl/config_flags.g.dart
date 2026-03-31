// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'config_flags.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ConfigFlags _$ConfigFlagsFromJson(Map<String, dynamic> json) => ConfigFlags()
  ..ctabfolder_visible_controls = json['ctabfolder_visible_controls'] as bool?
  ..image_disable_icons_replacement =
      json['image_disable_icons_replacement'] as bool?
  ..assets_path = json['assets_path'] as String?
  ..use_swt_colors = json['use_swt_colors'] as bool?
  ..use_swt_fonts = json['use_swt_fonts'] as bool?
  ..theme_name = json['theme_name'] as String?
  ..force_theme = json['force_theme'] as String?
  ..theme_color_widget = json['theme_color_widget'] as String?
  ..use_special_dropdown_button = json['use_special_dropdown_button'] as bool?
  ..preserve_icon_colors = json['preserve_icon_colors'] as bool?;

Map<String, dynamic> _$ConfigFlagsToJson(
  ConfigFlags instance,
) => <String, dynamic>{
  'ctabfolder_visible_controls': instance.ctabfolder_visible_controls,
  'image_disable_icons_replacement': instance.image_disable_icons_replacement,
  'assets_path': instance.assets_path,
  'use_swt_colors': instance.use_swt_colors,
  'use_swt_fonts': instance.use_swt_fonts,
  'theme_name': instance.theme_name,
  'force_theme': instance.force_theme,
  'theme_color_widget': instance.theme_color_widget,
  'use_special_dropdown_button': instance.use_special_dropdown_button,
  'preserve_icon_colors': instance.preserve_icon_colors,
};
