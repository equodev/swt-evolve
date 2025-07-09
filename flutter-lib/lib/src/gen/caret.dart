import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/widget.dart';
import '../impl/caret_evolve.dart';
import 'widgets.dart';

part 'caret.g.dart';

class CaretSwt<V extends VCaret> extends WidgetSwt<V> {
  const CaretSwt({super.key, required super.value});

  @override
  State createState() => CaretImpl<CaretSwt<VCaret>, VCaret>();
}

@JsonSerializable()
class VCaret extends VWidget {
  VCaret() : this.empty();
  VCaret.empty() {
    swt = "Caret";
  }

  int? height;
  bool? isVisible;

  factory VCaret.fromJson(Map<String, dynamic> json) => _$VCaretFromJson(json);
  Map<String, dynamic> toJson() => _$VCaretToJson(this);
}
