import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/image.dart';
import '../gen/rectangle.dart';
import '../gen/scrollable.dart';
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
  List<String>? selection;
  List<int>? selectionIndices;
  int? topIndex;

  factory VList.fromJson(Map<String, dynamic> json) => _$VListFromJson(json);
  Map<String, dynamic> toJson() => _$VListToJson(this);
}
