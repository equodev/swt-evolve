import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../impl/slider_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'slider.g.dart';

class SliderSwt<V extends VSlider> extends ControlSwt<V> {
  
  const SliderSwt({super.key, required super.value});

  
  @override
  State createState() => SliderImpl<SliderSwt<VSlider>, VSlider>();

  

  
  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}


@JsonSerializable() class VSlider extends VControl {
  VSlider() : this.empty();
  VSlider.empty()  { swt = "Slider"; }
  
  int? increment;
  int? maximum;
  int? minimum;
  int? pageIncrement;
  int? selection;
  int? thumb;
  
  factory VSlider.fromJson(Map<String, dynamic> json) => _$VSliderFromJson(json);
  Map<String, dynamic> toJson() => _$VSliderToJson(this);
  
}