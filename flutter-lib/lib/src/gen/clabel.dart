import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/control.dart';
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

  int? align;
  int? bottomMargin;
  List<VColor>? gradientColors;
  List<int>? gradientPercents;
  bool? gradientVertical;
  int? leftMargin;
  int? rightMargin;
  String? text;
  int? topMargin;

  factory VCLabel.fromJson(Map<String, dynamic> json) =>
      _$VCLabelFromJson(json);
  Map<String, dynamic> toJson() => _$VCLabelToJson(this);
}
