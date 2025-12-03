import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/menu.dart';
import '../impl/menuitem_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'menuitem.g.dart';

class MenuItemSwt<V extends VMenuItem> extends ItemSwt<V> {
  const MenuItemSwt({super.key, required super.value});

  @override
  State createState() => MenuItemImpl<MenuItemSwt<VMenuItem>, VMenuItem>();

  void sendArmArm(V val, VEvent? payload) {
    sendEvent(val, "Arm/Arm", payload);
  }

  void sendHelpHelp(V val, VEvent? payload) {
    sendEvent(val, "Help/Help", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VMenuItem extends VItem {
  VMenuItem() : this.empty();
  VMenuItem.empty() {
    swt = "MenuItem";
  }

  int? ID;
  int? accelerator;
  bool? enabled;
  VMenu? menu;
  bool? selection;
  String? toolTipText;

  factory VMenuItem.fromJson(Map<String, dynamic> json) =>
      _$VMenuItemFromJson(json);
  Map<String, dynamic> toJson() => _$VMenuItemToJson(this);
}
