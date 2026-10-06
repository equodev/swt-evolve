import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../impl/tableitem_evolve.dart';
import 'widgets.dart';

part 'tableitem.g.dart';

class TableItemSwt<V extends VTableItem> extends ItemSwt<V> {
  const TableItemSwt({super.key, required super.value});

  @override
  State createState() => TableItemImpl<TableItemSwt<VTableItem>, VTableItem>();
}

@JsonSerializable()
class VTableItem extends VItem {
  VTableItem() : this.empty();
  VTableItem.empty() {
    swt = "TableItem";
  }

  VColor? background;
  List<VColor?>? backgrounds;
  bool? checked;
  VFont? font;
  List<VFont?>? fonts;
  VColor? foreground;
  List<VColor?>? foregrounds;
  bool? grayed;
  List<VImage?>? images;
  List<int>? paintedTexts;
  List<String?>? texts;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTableItem) {
      background = other.background;
      backgrounds = other.backgrounds;
      checked = other.checked;
      font = other.font;
      fonts = other.fonts;
      foreground = other.foreground;
      foregrounds = other.foregrounds;
      grayed = other.grayed;
      images = other.images;
      paintedTexts = other.paintedTexts;
      texts = other.texts;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'background':
        background = json['background'] == null
            ? null
            : VColor.fromJson(json['background'] as Map<String, dynamic>);
      case 'backgrounds':
        backgrounds = (json['backgrounds'] as List<dynamic>?)
            ?.map(
              (e) =>
                  e == null ? null : VColor.fromJson(e as Map<String, dynamic>),
            )
            .toList();
      case 'checked':
        checked = json['checked'] as bool?;
      case 'font':
        font = json['font'] == null
            ? null
            : VFont.fromJson(json['font'] as Map<String, dynamic>);
      case 'fonts':
        fonts = (json['fonts'] as List<dynamic>?)
            ?.map(
              (e) =>
                  e == null ? null : VFont.fromJson(e as Map<String, dynamic>),
            )
            .toList();
      case 'foreground':
        foreground = json['foreground'] == null
            ? null
            : VColor.fromJson(json['foreground'] as Map<String, dynamic>);
      case 'foregrounds':
        foregrounds = (json['foregrounds'] as List<dynamic>?)
            ?.map(
              (e) =>
                  e == null ? null : VColor.fromJson(e as Map<String, dynamic>),
            )
            .toList();
      case 'grayed':
        grayed = json['grayed'] as bool?;
      case 'images':
        images = (json['images'] as List<dynamic>?)
            ?.map(
              (e) =>
                  e == null ? null : VImage.fromJson(e as Map<String, dynamic>),
            )
            .toList();
      case 'paintedTexts':
        paintedTexts = (json['paintedTexts'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
      case 'texts':
        texts = (json['texts'] as List<dynamic>?)
            ?.map((e) => e as String?)
            .toList();
      default:
        super.readProperty(key, json);
    }
  }

  factory VTableItem.fromJson(Map<String, dynamic> json) =>
      _$VTableItemFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VTableItemToJson(this);
}
