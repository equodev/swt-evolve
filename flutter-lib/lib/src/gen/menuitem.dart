import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/menu.dart';
import '../gen/widget.dart';
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

  int? accelerator;
  bool? enabled;
  VMenu? menu;
  bool? selection;
  String? toolTipText;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VMenuItem) {
      accelerator = other.accelerator;
      enabled = other.enabled;
      menu = other.menu;
      selection = other.selection;
      toolTipText = other.toolTipText;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'accelerator':
        accelerator = (json['accelerator'] as num?)?.toInt();
      case 'enabled':
        enabled = json['enabled'] as bool?;
      case 'menu':
        menu = json['menu'] == null
            ? null
            : VMenu.fromJson(json['menu'] as Map<String, dynamic>);
      case 'selection':
        selection = json['selection'] as bool?;
      case 'toolTipText':
        toolTipText = json['toolTipText'] as String?;
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    menu = VWidget.adoptOne(menu, adopt);
  }

  factory VMenuItem.fromJson(Map<String, dynamic> json) =>
      _$VMenuItemFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VMenuItemToJson(this);
}
