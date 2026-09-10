// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'path.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VPath _$VPathFromJson(Map<String, dynamic> json) =>
    VPath()
      ..pathData = json['pathData'] == null
          ? null
          : VPathData.fromJson(json['pathData'] as Map<String, dynamic>);

Map<String, dynamic> _$VPathToJson(VPath instance) => <String, dynamic>{
  'pathData': ?instance.pathData,
};
