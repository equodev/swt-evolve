import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollable.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../impl/list_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'list.g.dart';

class ListSwt<V extends VList> extends ScrollableSwt<V> {
  const ListSwt({super.key, required super.value});

  @override
  State createState() => ListImpl<ListSwt<VList>, VList>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VList extends VScrollable {
  VList() : this.empty();
  VList.empty() {
    swt = "List";
  }

  List<String>? items;
  List<int>? selection;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VList) {
      items = other.items;
      selection = other.selection;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => e as String)
            .toList();
      case 'selection':
        selection = (json['selection'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
      default:
        super.readProperty(key, json);
    }
  }

  factory VList.fromJson(Map<String, dynamic> json) =>
      _$VListFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VListToJson(this);
}
