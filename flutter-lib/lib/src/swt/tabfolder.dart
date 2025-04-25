import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/tabfolder_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'tabfolder.g.dart';

class TabFolderSwt<V extends TabFolderValue> extends CompositeSwt<V> {
  const TabFolderSwt({super.key, required super.value});

  @override
  State createState() =>
      TabFolderImpl<TabFolderSwt<TabFolderValue>, TabFolderValue>(useDarkTheme: true);

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class TabFolderValue extends CompositeValue {
  TabFolderValue() : this.empty();
  TabFolderValue.empty() {
    swt = "TabFolder";
  }

  int? selectionIndex;

  factory TabFolderValue.fromJson(Map<String, dynamic> json) =>
      _$TabFolderValueFromJson(json);
  Map<String, dynamic> toJson() => _$TabFolderValueToJson(this);
}
