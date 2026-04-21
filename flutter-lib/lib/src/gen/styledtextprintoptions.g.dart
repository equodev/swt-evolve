// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'styledtextprintoptions.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VStyledTextPrintOptions _$VStyledTextPrintOptionsFromJson(
  Map<String, dynamic> json,
) => VStyledTextPrintOptions()
  ..PAGE_TAG = json['PAGE_TAG'] as String?
  ..SEPARATOR = json['SEPARATOR'] as String?
  ..footer = json['footer'] as String?
  ..header = json['header'] as String?
  ..jobName = json['jobName'] as String?
  ..lineLabels = (json['lineLabels'] as List<dynamic>?)
      ?.map((e) => e as String)
      .toList()
  ..printLineBackground = json['printLineBackground'] as bool?
  ..printLineNumbers = json['printLineNumbers'] as bool?
  ..printTextBackground = json['printTextBackground'] as bool?
  ..printTextFontStyle = json['printTextFontStyle'] as bool?
  ..printTextForeground = json['printTextForeground'] as bool?;

Map<String, dynamic> _$VStyledTextPrintOptionsToJson(
  VStyledTextPrintOptions instance,
) => <String, dynamic>{
  'PAGE_TAG': ?instance.PAGE_TAG,
  'SEPARATOR': ?instance.SEPARATOR,
  'footer': ?instance.footer,
  'header': ?instance.header,
  'jobName': ?instance.jobName,
  'lineLabels': ?instance.lineLabels,
  'printLineBackground': ?instance.printLineBackground,
  'printLineNumbers': ?instance.printLineNumbers,
  'printTextBackground': ?instance.printTextBackground,
  'printTextFontStyle': ?instance.printTextFontStyle,
  'printTextForeground': ?instance.printTextForeground,
};
