import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../impl/toolitem_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'toolitem.g.dart';

class ToolItemSwt<V extends VToolItem> extends ItemSwt<V> {
  const ToolItemSwt({super.key, required super.value});

  @override
  State createState() => ToolItemImpl<ToolItemSwt<VToolItem>, VToolItem>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionOpenMenu(V val, VEvent? payload) {
    sendEvent(val, "Selection/OpenMenu", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VToolItem extends VItem {
  VToolItem() : this.empty();
  VToolItem.empty() {
    swt = "ToolItem";
  }

  VColor? background;
  VControl? control;
  VImage? disabledImage;
  bool? enabled;
  VColor? foreground;
  VImage? hotImage;
  bool? selection;
  String? toolTipText;
  int? width;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VToolItem) {
      background = other.background;
      control = other.control;
      disabledImage = other.disabledImage;
      enabled = other.enabled;
      foreground = other.foreground;
      hotImage = other.hotImage;
      selection = other.selection;
      toolTipText = other.toolTipText;
      width = other.width;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'background':
        background = json['background'] == null
            ? null
            : VColor.fromJson(json['background'] as Map<String, dynamic>);
      case 'control':
        control = json['control'] == null
            ? null
            : VControl.fromJson(json['control'] as Map<String, dynamic>);
      case 'disabledImage':
        disabledImage = json['disabledImage'] == null
            ? null
            : VImage.fromJson(json['disabledImage'] as Map<String, dynamic>);
      case 'enabled':
        enabled = json['enabled'] as bool?;
      case 'foreground':
        foreground = json['foreground'] == null
            ? null
            : VColor.fromJson(json['foreground'] as Map<String, dynamic>);
      case 'hotImage':
        hotImage = json['hotImage'] == null
            ? null
            : VImage.fromJson(json['hotImage'] as Map<String, dynamic>);
      case 'selection':
        selection = json['selection'] as bool?;
      case 'toolTipText':
        toolTipText = json['toolTipText'] as String?;
      case 'width':
        width = (json['width'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }

  factory VToolItem.fromJson(Map<String, dynamic> json) =>
      _$VToolItemFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VToolItemToJson(this);
}
