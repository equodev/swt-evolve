import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/scrollable.dart';
import '../impl/list_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'list.g.dart';

class ListSwt<V extends ListValue> extends ScrollableSwt<V> {
  const ListSwt({super.key, required super.value});

  @override
  State createState() => ListImpl<ListSwt<ListValue>, ListValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class ListValue extends ScrollableValue {
  ListValue() : this.empty();
  ListValue.empty() {
    swt = "List";
  }

  List<String>? items;
  List<String>? selection;
  int? selectionIndex;
  int? topIndex;

  factory ListValue.fromJson(Map<String, dynamic> json) =>
      _$ListValueFromJson(json);
  Map<String, dynamic> toJson() => _$ListValueToJson(this);
}
