import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../impl/ctabitem_evolve.dart';
import 'widgets.dart';

part 'ctabitem.g.dart';

class CTabItemSwt<V extends VCTabItem> extends ItemSwt<V> {
  const CTabItemSwt({super.key, required super.value});

  @override
  State createState() => CTabItemImpl<CTabItemSwt<VCTabItem>, VCTabItem>();
}

@JsonSerializable()
class VCTabItem extends VItem {
  VCTabItem() : this.empty();
  VCTabItem.empty() {
    swt = "CTabItem";
  }

  VControl? control;
  VFont? font;
  VColor? foreground;
  VColor? selectionForeground;
  bool? showClose;
  bool? showing;
  String? toolTipText;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCTabItem) {
      control = other.control;
      font = other.font;
      foreground = other.foreground;
      selectionForeground = other.selectionForeground;
      showClose = other.showClose;
      showing = other.showing;
      toolTipText = other.toolTipText;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'control':
        control = json['control'] == null
            ? null
            : VControl.fromJson(json['control'] as Map<String, dynamic>);
      case 'font':
        font = json['font'] == null
            ? null
            : VFont.fromJson(json['font'] as Map<String, dynamic>);
      case 'foreground':
        foreground = json['foreground'] == null
            ? null
            : VColor.fromJson(json['foreground'] as Map<String, dynamic>);
      case 'selectionForeground':
        selectionForeground = json['selectionForeground'] == null
            ? null
            : VColor.fromJson(
                json['selectionForeground'] as Map<String, dynamic>,
              );
      case 'showClose':
        showClose = json['showClose'] as bool?;
      case 'showing':
        showing = json['showing'] as bool?;
      case 'toolTipText':
        toolTipText = json['toolTipText'] as String?;
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }

  factory VCTabItem.fromJson(Map<String, dynamic> json) =>
      _$VCTabItemFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VCTabItemToJson(this);
}
