import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/rectangle.dart';
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

  int? alignment;
  VImage? image;
  String? text;

  factory VLabel.fromJson(Map<String, dynamic> json) => _$VLabelFromJson(json);
  Map<String, dynamic> toJson() => _$VLabelToJson(this);
}
