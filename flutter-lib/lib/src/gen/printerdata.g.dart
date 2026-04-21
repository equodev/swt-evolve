// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'printerdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VPrinterData _$VPrinterDataFromJson(Map<String, dynamic> json) => VPrinterData()
  ..debug = json['debug'] as bool
  ..tracking = json['tracking'] as bool
  ..ALL_PAGES = (json['ALL_PAGES'] as num?)?.toInt()
  ..DUPLEX_LONG_EDGE = (json['DUPLEX_LONG_EDGE'] as num?)?.toInt()
  ..DUPLEX_NONE = (json['DUPLEX_NONE'] as num?)?.toInt()
  ..DUPLEX_SHORT_EDGE = (json['DUPLEX_SHORT_EDGE'] as num?)?.toInt()
  ..LANDSCAPE = (json['LANDSCAPE'] as num?)?.toInt()
  ..PAGE_RANGE = (json['PAGE_RANGE'] as num?)?.toInt()
  ..PORTRAIT = (json['PORTRAIT'] as num?)?.toInt()
  ..SELECTION = (json['SELECTION'] as num?)?.toInt()
  ..collate = json['collate'] as bool?
  ..copyCount = (json['copyCount'] as num?)?.toInt()
  ..driver = json['driver'] as String?
  ..duplex = (json['duplex'] as num?)?.toInt()
  ..endPage = (json['endPage'] as num?)?.toInt()
  ..fileName = json['fileName'] as String?
  ..name = json['name'] as String?
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..printToFile = json['printToFile'] as bool?
  ..scope = (json['scope'] as num?)?.toInt()
  ..startPage = (json['startPage'] as num?)?.toInt();

Map<String, dynamic> _$VPrinterDataToJson(VPrinterData instance) =>
    <String, dynamic>{
      'debug': instance.debug,
      'tracking': instance.tracking,
      'ALL_PAGES': ?instance.ALL_PAGES,
      'DUPLEX_LONG_EDGE': ?instance.DUPLEX_LONG_EDGE,
      'DUPLEX_NONE': ?instance.DUPLEX_NONE,
      'DUPLEX_SHORT_EDGE': ?instance.DUPLEX_SHORT_EDGE,
      'LANDSCAPE': ?instance.LANDSCAPE,
      'PAGE_RANGE': ?instance.PAGE_RANGE,
      'PORTRAIT': ?instance.PORTRAIT,
      'SELECTION': ?instance.SELECTION,
      'collate': ?instance.collate,
      'copyCount': ?instance.copyCount,
      'driver': ?instance.driver,
      'duplex': ?instance.duplex,
      'endPage': ?instance.endPage,
      'fileName': ?instance.fileName,
      'name': ?instance.name,
      'orientation': ?instance.orientation,
      'printToFile': ?instance.printToFile,
      'scope': ?instance.scope,
      'startPage': ?instance.startPage,
    };
