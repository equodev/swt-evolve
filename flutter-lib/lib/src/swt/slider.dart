import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/control.dart';
import '../impl/slider_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'slider.g.dart';

class SliderSwt<V extends SliderValue> extends ControlSwt<V> {
  const SliderSwt({super.key, required super.value});

  @override
  State createState() => SliderImpl<SliderSwt<SliderValue>, SliderValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class SliderValue extends ControlValue {
  SliderValue() : this.empty();
  SliderValue.empty() {
    swt = "Slider";
  }

  int? increment;
  int? maximum;
  int? minimum;
  int? pageIncrement;
  int? selection;
  int? thumb;

  factory SliderValue.fromJson(Map<String, dynamic> json) =>
      _$SliderValueFromJson(json);
  Map<String, dynamic> toJson() => _$SliderValueToJson(this);
}
