import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/widget.dart';
import '../impl/ime_evolve.dart';
import 'widgets.dart';

part 'ime.g.dart';

class IMESwt<V extends VIME> extends WidgetSwt<V> {
  const IMESwt({super.key, required super.value});

  @override
  State createState() => IMEImpl<IMESwt<VIME>, VIME>();
}

@JsonSerializable()
class VIME extends VWidget {
  VIME() : this.empty();
  VIME.empty() {
    swt = "IME";
  }

  factory VIME.fromJson(Map<String, dynamic> json) =>
      _$VIMEFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VIMEToJson(this);
}
