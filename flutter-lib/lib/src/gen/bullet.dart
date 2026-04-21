import 'package:json_annotation/json_annotation.dart';
import '../gen/stylerange.dart';

part 'bullet.g.dart';

@JsonSerializable()
class VBullet {
  VBullet() : this.empty();
  VBullet.empty();

  VStyleRange? style;
  String text = "";
  int type = 0;

  factory VBullet.fromJson(Map<String, dynamic> json) =>
      _$VBulletFromJson(json);
  Map<String, dynamic> toJson() => _$VBulletToJson(this);
}
