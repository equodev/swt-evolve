import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
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

  bool? enabled;
  List<VMenuItem>? items;
  VPoint? location;
  int? orientation;
  bool? visible;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VMenu) {
      enabled = other.enabled;
      items = other.items;
      location = other.location;
      orientation = other.orientation;
      visible = other.visible;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'enabled':
        enabled = json['enabled'] as bool?;
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => VMenuItem.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'location':
        location = json['location'] == null
            ? null
            : VPoint.fromJson(json['location'] as Map<String, dynamic>);
      case 'orientation':
        orientation = (json['orientation'] as num?)?.toInt();
      case 'visible':
        visible = json['visible'] as bool?;
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }

  factory VMenu.fromJson(Map<String, dynamic> json) =>
      _$VMenuFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VMenuToJson(this);
}
