import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/sashform_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'sashform.g.dart';

class SashFormSwt<V extends SashFormValue> extends CompositeSwt<V> {
  const SashFormSwt({super.key, required super.value});

  @override
  State createState() =>
      SashFormImpl<SashFormSwt<SashFormValue>, SashFormValue>();
}

@JsonSerializable()
class SashFormValue extends CompositeValue {
  SashFormValue() : this.empty();
  SashFormValue.empty() {
    swt = "SashForm";
  }

  int? orientation;
  int? sashWidth;

  factory SashFormValue.fromJson(Map<String, dynamic> json) =>
      _$SashFormValueFromJson(json);
  Map<String, dynamic> toJson() => _$SashFormValueToJson(this);
}
