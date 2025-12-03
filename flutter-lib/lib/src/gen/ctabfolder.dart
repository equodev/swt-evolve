import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/ctabitem.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../impl/ctabfolder_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'ctabfolder.g.dart';

class CTabFolderSwt<V extends VCTabFolder> extends CompositeSwt<V> {
  const CTabFolderSwt({super.key, required super.value});

  @override
  State createState() =>
      CTabFolderImpl<CTabFolderSwt<VCTabFolder>, VCTabFolder>();

  void sendCTabFolderitemClosed(V val, VEvent? payload) {
    sendEvent(val, "CTabFolder/itemClosed", payload);
  }

  void sendCTabFolder2close(V val, VEvent? payload) {
    sendEvent(val, "CTabFolder2/close", payload);
  }

  void sendCTabFolder2maximize(V val, VEvent? payload) {
    sendEvent(val, "CTabFolder2/maximize", payload);
  }

  void sendCTabFolder2minimize(V val, VEvent? payload) {
    sendEvent(val, "CTabFolder2/minimize", payload);
  }

  void sendCTabFolder2restore(V val, VEvent? payload) {
    sendEvent(val, "CTabFolder2/restore", payload);
  }

  void sendCTabFolder2showList(V val, VEvent? payload) {
    sendEvent(val, "CTabFolder2/showList", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VCTabFolder extends VComposite {
  VCTabFolder() : this.empty();
  VCTabFolder.empty() {
    swt = "CTabFolder";
  }

  bool? MRUVisible;
  bool? borderVisible;
  List<VColor>? gradientColors;
  List<int>? gradientPercents;
  bool? gradientVertical;
  bool? highlightEnabled;
  List<VCTabItem>? items;
  bool? maximizeVisible;
  bool? maximized;
  bool? minimizeVisible;
  bool? minimized;
  int? minimumCharacters;
  bool? selectedImageVisible;
  int? selection;
  VColor? selectionBackground;
  int? selectionBarThickness;
  VImage? selectionBgImage;
  VColor? selectionForeground;
  List<VColor>? selectionGradientColors;
  List<int>? selectionGradientPercents;
  bool? selectionGradientVertical;
  bool? simple;
  bool? single;
  int? tabHeight;
  int? tabPosition;
  VControl? topRight;
  int? topRightAlignment;
  bool? unselectedCloseVisible;
  bool? unselectedImageVisible;

  factory VCTabFolder.fromJson(Map<String, dynamic> json) =>
      _$VCTabFolderFromJson(json);
  Map<String, dynamic> toJson() => _$VCTabFolderToJson(this);
}
