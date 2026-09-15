import 'package:json_annotation/json_annotation.dart';

part 'bullet.g.dart';

@JsonSerializable()
class VBullet {
  VBullet() : this.empty();
  VBullet.empty();

  factory VBullet.fromJson(Map<String, dynamic> json) =>
      _$VBulletFromJson(json);
  Map<String, dynamic> toJson() => _$VBulletToJson(this);
}
