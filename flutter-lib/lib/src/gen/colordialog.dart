import 'package:json_annotation/json_annotation.dart';
import '../gen/dialog.dart';
import '../gen/rgb.dart';

part 'colordialog.g.dart';

@JsonSerializable()
class VColorDialog extends VDialog {
  VColorDialog() : this.empty();
  VColorDialog.empty() : super.empty();

  // dsl-json derives the wire names from the Java getters (getRGB/getRGBs), so
  // they keep SWT's capitalisation rather than the lowerCamelCase default.
  @JsonKey(name: 'RGB')
  VRGB? rgb;
  @JsonKey(name: 'RGBs')
  List<VRGB>? rgbs;

  factory VColorDialog.fromJson(Map<String, dynamic> json) =>
      _$VColorDialogFromJson(json);
  Map<String, dynamic> toJson() => _$VColorDialogToJson(this);
}
