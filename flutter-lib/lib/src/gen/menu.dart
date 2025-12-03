import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/menu.dart';
import '../gen/menuitem.dart';
import '../gen/point.dart';
import '../gen/widget.dart';
import '../impl/menu_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'menu.g.dart';

class MenuSwt<V extends VMenu> extends WidgetSwt<V> {
  const MenuSwt({super.key, required super.value});

  @override
  State createState() => MenuImpl<MenuSwt<VMenu>, VMenu>();

  void sendHelpHelp(V val, VEvent? payload) {
    sendEvent(val, "Help/Help", payload);
  }

  void sendMenuHide(V val, VEvent? payload) {
    sendEvent(val, "Menu/Hide", payload);
  }

  void sendMenuShow(V val, VEvent? payload) {
    sendEvent(val, "Menu/Show", payload);
  }
}

@JsonSerializable()
class VMenu extends VWidget {
  VMenu() : this.empty();
  VMenu.empty() {
    swt = "Menu";
  }

  VMenuItem? defaultItem;
  bool? enabled;
  List<VMenuItem>? items;
  VPoint? location;
  int? orientation;
  VMenu? parentMenu;
  bool? visible;

  factory VMenu.fromJson(Map<String, dynamic> json) => _$VMenuFromJson(json);
  Map<String, dynamic> toJson() => _$VMenuToJson(this);
}
