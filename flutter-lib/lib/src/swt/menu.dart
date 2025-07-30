import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/widget.dart';
import '../impl/menu_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'menu.g.dart';

class MenuSwt<V extends MenuValue> extends WidgetSwt<V> {
  const MenuSwt({super.key, required super.value});

  @override
  State createState() => MenuImpl<MenuSwt<MenuValue>, MenuValue>();

  void sendMenuHide(V val, Object? payload) {
    sendEvent(val, "Menu/Hide", payload);
  }

  void sendMenuShow(V val, Object? payload) {
    sendEvent(val, "Menu/Show", payload);
  }

  void sendHelpHelp(V val, Object? payload) {
    sendEvent(val, "Help/Help", payload);
  }
}

@JsonSerializable()
class MenuValue extends WidgetValue {
  MenuValue() : this.empty();
  MenuValue.empty() {
    swt = "Menu";
  }

  bool? enabled;
  int? orientation;
  bool? visible;

  factory MenuValue.fromJson(Map<String, dynamic> json) =>
      _$MenuValueFromJson(json);
  Map<String, dynamic> toJson() => _$MenuValueToJson(this);
}
