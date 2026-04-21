// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'gldata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VGLData _$VGLDataFromJson(Map<String, dynamic> json) => VGLData()
  ..accumAlphaSize = (json['accumAlphaSize'] as num?)?.toInt()
  ..accumBlueSize = (json['accumBlueSize'] as num?)?.toInt()
  ..accumGreenSize = (json['accumGreenSize'] as num?)?.toInt()
  ..accumRedSize = (json['accumRedSize'] as num?)?.toInt()
  ..alphaSize = (json['alphaSize'] as num?)?.toInt()
  ..blueSize = (json['blueSize'] as num?)?.toInt()
  ..depthSize = (json['depthSize'] as num?)?.toInt()
  ..doubleBuffer = json['doubleBuffer'] as bool?
  ..greenSize = (json['greenSize'] as num?)?.toInt()
  ..redSize = (json['redSize'] as num?)?.toInt()
  ..sampleBuffers = (json['sampleBuffers'] as num?)?.toInt()
  ..samples = (json['samples'] as num?)?.toInt()
  ..shareContext = json['shareContext'] == null
      ? null
      : VGLCanvas.fromJson(json['shareContext'] as Map<String, dynamic>)
  ..stencilSize = (json['stencilSize'] as num?)?.toInt()
  ..stereo = json['stereo'] as bool?;

Map<String, dynamic> _$VGLDataToJson(VGLData instance) => <String, dynamic>{
  'accumAlphaSize': ?instance.accumAlphaSize,
  'accumBlueSize': ?instance.accumBlueSize,
  'accumGreenSize': ?instance.accumGreenSize,
  'accumRedSize': ?instance.accumRedSize,
  'alphaSize': ?instance.alphaSize,
  'blueSize': ?instance.blueSize,
  'depthSize': ?instance.depthSize,
  'doubleBuffer': ?instance.doubleBuffer,
  'greenSize': ?instance.greenSize,
  'redSize': ?instance.redSize,
  'sampleBuffers': ?instance.sampleBuffers,
  'samples': ?instance.samples,
  'shareContext': ?instance.shareContext,
  'stencilSize': ?instance.stencilSize,
  'stereo': ?instance.stereo,
};
