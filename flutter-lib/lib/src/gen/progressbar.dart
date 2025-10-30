import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/rectangle.dart';
import '../impl/progressbar_evolve.dart';
import 'widgets.dart';

part 'progressbar.g.dart';

class ProgressBarSwt<V extends VProgressBar> extends ControlSwt<V> {
  const ProgressBarSwt({super.key, required super.value});

  @override
  State createState() =>
      ProgressBarImpl<ProgressBarSwt<VProgressBar>, VProgressBar>();
}

@JsonSerializable()
class VProgressBar extends VControl {
  VProgressBar() : this.empty();
  VProgressBar.empty() {
    swt = "ProgressBar";
  }

  int? maximum;
  int? minimum;
  int? selection;

  factory VProgressBar.fromJson(Map<String, dynamic> json) =>
      _$VProgressBarFromJson(json);
  Map<String, dynamic> toJson() => _$VProgressBarToJson(this);
}
