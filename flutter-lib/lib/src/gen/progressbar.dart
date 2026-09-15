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

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VProgressBar) {
      maximum = other.maximum;
      minimum = other.minimum;
      selection = other.selection;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'maximum':
        maximum = (json['maximum'] as num?)?.toInt();
      case 'minimum':
        minimum = (json['minimum'] as num?)?.toInt();
      case 'selection':
        selection = (json['selection'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  factory VProgressBar.fromJson(Map<String, dynamic> json) =>
      _$VProgressBarFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VProgressBarToJson(this);
}
