import 'package:json_annotation/json_annotation.dart';
import '../impl/utils/image_utils.dart';

part 'pathdata.g.dart';

@JsonSerializable()
class VPathData {
  VPathData() : this.empty();
  VPathData.empty();

  List<double>? points;
  @JsonKey(
    fromJson: ImageUtils.parseByteArray,
    toJson: ImageUtils.serializeByteArray,
  )
  List<int>? types;

  factory VPathData.fromJson(Map<String, dynamic> json) =>
      _$VPathDataFromJson(json);
  Map<String, dynamic> toJson() => _$VPathDataToJson(this);
}
