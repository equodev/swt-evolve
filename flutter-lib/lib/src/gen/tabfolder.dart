import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/tabitem.dart';
import '../impl/tabfolder_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'tabfolder.g.dart';

class TabFolderSwt<V extends VTabFolder> extends CompositeSwt<V> {
  const TabFolderSwt({super.key, required super.value});

  @override
  State createState() => TabFolderImpl<TabFolderSwt<VTabFolder>, VTabFolder>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VTabFolder extends VComposite {
  VTabFolder() : this.empty();
  VTabFolder.empty() {
    swt = "TabFolder";
  }

  List<VTabItem>? items;
  List<VTabItem>? selection;

  factory VTabFolder.fromJson(Map<String, dynamic> json) =>
      _$VTabFolderFromJson(json);
  Map<String, dynamic> toJson() => _$VTabFolderToJson(this);
}
