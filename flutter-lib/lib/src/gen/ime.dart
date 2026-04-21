import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/textstyle.dart';
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

  int? compositionOffset;
  List<int>? ranges;
  List<VTextStyle>? styles;

  factory VIME.fromJson(Map<String, dynamic> json) => _$VIMEFromJson(json);
  Map<String, dynamic> toJson() => _$VIMEToJson(this);
}
