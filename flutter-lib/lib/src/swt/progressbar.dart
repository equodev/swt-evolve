import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/control.dart';
import '../impl/progressbar_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'progressbar.g.dart';

class ProgressBarSwt<V extends ProgressBarValue> extends ControlSwt<V> {
  const ProgressBarSwt({super.key, required super.value});

  @override
  State createState() =>
      ProgressBarImpl<ProgressBarSwt<ProgressBarValue>, ProgressBarValue>();
}

@JsonSerializable()
class ProgressBarValue extends ControlValue {
  ProgressBarValue() : this.empty();
  ProgressBarValue.empty() {
    swt = "ProgressBar";
  }

  int? maximum;
  int? minimum;
  int? selection;
  int? state;

  factory ProgressBarValue.fromJson(Map<String, dynamic> json) =>
      _$ProgressBarValueFromJson(json);
  Map<String, dynamic> toJson() => _$ProgressBarValueToJson(this);
}
