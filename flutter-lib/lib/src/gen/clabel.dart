import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/canvas.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../impl/clabel_evolve.dart';
import 'widgets.dart';

part 'clabel.g.dart';

class CLabelSwt<V extends VCLabel> extends CanvasSwt<V> {
  const CLabelSwt({super.key, required super.value});

  @override
  State createState() => CLabelImpl<CLabelSwt<VCLabel>, VCLabel>();
}

@JsonSerializable()
class VCLabel extends VCanvas {
  VCLabel() : this.empty();
  VCLabel.empty() {
    swt = "CLabel";
  }

  int? alignment;
  VImage? backgroundImage;
  int? bottomMargin;
  VImage? image;
  int? leftMargin;
  int? rightMargin;
  String? text;
  int? topMargin;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCLabel) {
      alignment = other.alignment;
      backgroundImage = other.backgroundImage;
      bottomMargin = other.bottomMargin;
      image = other.image;
      leftMargin = other.leftMargin;
      rightMargin = other.rightMargin;
      text = other.text;
      topMargin = other.topMargin;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'alignment':
        alignment = (json['alignment'] as num?)?.toInt();
      case 'backgroundImage':
        backgroundImage = json['backgroundImage'] == null
            ? null
            : VImage.fromJson(json['backgroundImage'] as Map<String, dynamic>);
      case 'bottomMargin':
        bottomMargin = (json['bottomMargin'] as num?)?.toInt();
      case 'image':
        image = json['image'] == null
            ? null
            : VImage.fromJson(json['image'] as Map<String, dynamic>);
      case 'leftMargin':
        leftMargin = (json['leftMargin'] as num?)?.toInt();
      case 'rightMargin':
        rightMargin = (json['rightMargin'] as num?)?.toInt();
      case 'text':
        text = json['text'] as String?;
      case 'topMargin':
        topMargin = (json['topMargin'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  factory VCLabel.fromJson(Map<String, dynamic> json) =>
      _$VCLabelFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VCLabelToJson(this);
}
