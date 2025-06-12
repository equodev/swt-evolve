import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/swt/control.dart';
import 'package:swtflutter/src/swt/ctabitem.dart';
import '../swt/composite.dart';
import '../impl/ctabfolder_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'ctabfolder.g.dart';

class CTabFolderSwt<V extends CTabFolderValue> extends CompositeSwt<V> {
  const CTabFolderSwt({super.key, required super.value});

  @override
  State createState() =>
      CTabFolderImpl<CTabFolderSwt<CTabFolderValue>, CTabFolderValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class CTabFolderValue extends CompositeValue {
  CTabFolderValue() : this.empty();
  CTabFolderValue.empty() {
    swt = "CTabFolder";
  }

  bool? borderVisible;
  bool? minimized;
  bool? minimizeVisible;
  int? minimumCharacters;
  bool? maximized;
  bool? maximizeVisible;
  bool? mRUVisible;
  int? selectionIndex;
  bool? simple;
  bool? single;
  int? tabHeight;
  int? tabPosition;
  bool? unselectedCloseVisible;
  bool? unselectedImageVisible;
  bool? highlightEnabled;
  List<CTabItemValue>? items;

  factory CTabFolderValue.fromJson(Map<String, dynamic> json) =>
      _$CTabFolderValueFromJson(json);
  Map<String, dynamic> toJson() => _$CTabFolderValueToJson(this);
}
