import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/coolbar_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'coolbar.g.dart';

class CoolBarSwt<V extends CoolBarValue> extends CompositeSwt<V> {
  const CoolBarSwt({super.key, required super.value});

  @override
  State createState() => CoolBarImpl<CoolBarSwt<CoolBarValue>, CoolBarValue>(useDarkTheme: true);
}

@JsonSerializable()
class CoolBarValue extends CompositeValue {
  CoolBarValue() : this.empty();
  CoolBarValue.empty() {
    swt = "CoolBar";
  }

  bool? locked;

  factory CoolBarValue.fromJson(Map<String, dynamic> json) =>
      _$CoolBarValueFromJson(json);
  Map<String, dynamic> toJson() => _$CoolBarValueToJson(this);
}
