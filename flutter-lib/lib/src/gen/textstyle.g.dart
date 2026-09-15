// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'textstyle.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTextStyle _$VTextStyleFromJson(Map<String, dynamic> json) => VTextStyle()
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..font = json['font'] == null
      ? null
      : VFont.fromJson(json['font'] as Map<String, dynamic>)
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..strikeout = json['strikeout'] as bool?
  ..strikeoutColor = json['strikeoutColor'] == null
      ? null
      : VColor.fromJson(json['strikeoutColor'] as Map<String, dynamic>)
  ..underline = json['underline'] as bool?
  ..underlineColor = json['underlineColor'] == null
      ? null
      : VColor.fromJson(json['underlineColor'] as Map<String, dynamic>)
  ..underlineStyle = (json['underlineStyle'] as num?)?.toInt();

Map<String, dynamic> _$VTextStyleToJson(VTextStyle instance) =>
    <String, dynamic>{
      'background': ?instance.background,
      'font': ?instance.font,
      'foreground': ?instance.foreground,
      'strikeout': ?instance.strikeout,
      'strikeoutColor': ?instance.strikeoutColor,
      'underline': ?instance.underline,
      'underlineColor': ?instance.underlineColor,
      'underlineStyle': ?instance.underlineStyle,
    };
