import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/swt/menu.dart';
import '../swt/item.dart';
import '../impl/menuitem_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'menuitem.g.dart';

class MenuItemSwt<V extends MenuItemValue> extends ItemSwt<V> {
  const MenuItemSwt({super.key, required super.value});

  @override
  State createState() =>
      MenuItemImpl<MenuItemSwt<MenuItemValue>, MenuItemValue>();

  void sendArmArm(V val, Object? payload) {
    sendEvent(val, "Arm/Arm", payload);
  }

  void sendHelpHelp(V val, Object? payload) {
    sendEvent(val, "Help/Help", payload);
  }

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class MenuItemValue extends ItemValue {
  MenuItemValue() : this.empty();
  MenuItemValue.empty() {
    swt = "MenuItem";
  }

  int? accelerator;
  bool? enabled;
  int? iD;
  MenuValue? menu;
  bool? selection;
  String? toolTipText;

  factory MenuItemValue.fromJson(Map<String, dynamic> json) =>
      _$MenuItemValueFromJson(json);
  Map<String, dynamic> toJson() => _$MenuItemValueToJson(this);
}
