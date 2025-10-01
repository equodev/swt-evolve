// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'config_flags.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ConfigFlags _$ConfigFlagsFromJson(Map<String, dynamic> json) => ConfigFlags()
  ..ctabfolder_visible_controls = json['ctabfolder_visible_controls'] as bool?
  ..image_disable_icons_replacement =
      json['image_disable_icons_replacement'] as bool?
  ..assets_path = json['assets_path'] as String?;

Map<String, dynamic> _$ConfigFlagsToJson(ConfigFlags instance) =>
    <String, dynamic>{
      'ctabfolder_visible_controls': instance.ctabfolder_visible_controls,
      'image_disable_icons_replacement':
          instance.image_disable_icons_replacement,
      'assets_path': instance.assets_path,
    };
