import 'package:json_annotation/json_annotation.dart';
import '../gen/glcanvas.dart';

part 'gldata.g.dart';

@JsonSerializable()
class VGLData {
  VGLData() : this.empty();
  VGLData.empty();

  int? accumAlphaSize;
  int? accumBlueSize;
  int? accumGreenSize;
  int? accumRedSize;
  int? alphaSize;
  int? blueSize;
  int? depthSize;
  bool? doubleBuffer;
  int? greenSize;
  int? redSize;
  int? sampleBuffers;
  int? samples;
  VGLCanvas? shareContext;
  int? stencilSize;
  bool? stereo;

  factory VGLData.fromJson(Map<String, dynamic> json) =>
      _$VGLDataFromJson(json);
  Map<String, dynamic> toJson() => _$VGLDataToJson(this);
}
