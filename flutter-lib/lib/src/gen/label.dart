import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/widget.dart';
import '../impl/label_evolve.dart';
import 'widgets.dart';

part 'label.g.dart';

class LabelSwt<V extends VLabel> extends ControlSwt<V> {
  const LabelSwt({super.key, required super.value});

  @override
  State createState() => LabelImpl<LabelSwt<VLabel>, VLabel>();
}

@JsonSerializable()
class VLabel extends VControl {
  VLabel() : this.empty();
  VLabel.empty() {
    swt = "Label";
  }

  VImage? image;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VLabel) {
      image = other.image;
      text = other.text;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'image':
        image = json['image'] == null
            ? null
            : VImage.fromJson(json['image'] as Map<String, dynamic>);
      case 'text':
        text = json['text'] as String?;
      default:
        super.readProperty(key, json);
    }
  }

  factory VLabel.fromJson(Map<String, dynamic> json) =>
      _$VLabelFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VLabelToJson(this);
}
