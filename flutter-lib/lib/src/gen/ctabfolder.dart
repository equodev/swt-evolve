import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/ctabitem.dart';
import '../impl/ctabfolder_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'ctabfolder.g.dart';

class CTabFolderSwt<V extends VCTabFolder> extends CompositeSwt<V> {
  const CTabFolderSwt({super.key, required super.value});

  @override
  State createState() =>
      CTabFolderImpl<CTabFolderSwt<VCTabFolder>, VCTabFolder>();

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class VCTabFolder extends VComposite {
  VCTabFolder() : this.empty();
  VCTabFolder.empty() {
    swt = "CTabFolder";
  }

  bool? borderVisible;
  int? fixedTabHeight;
  List<VColor>? gradientColors;
  List<int>? gradientPercents;
  bool? gradientVertical;
  bool? highlightEnabled;
  List<VCTabItem>? items;
  bool? maximized;
  int? minChars;
  bool? minimized;
  bool? mru;
  bool? onBottom;
  int? selectedIndex;
  VColor? selectionBackground;
  VColor? selectionForeground;
  List<VColor>? selectionGradientColors;
  List<int>? selectionGradientPercents;
  bool? selectionGradientVertical;
  int? selectionHighlightBarThickness;
  bool? showMax;
  bool? showMin;
  bool? showSelectedImage;
  bool? showUnselectedClose;
  bool? showUnselectedImage;
  bool? simple;
  bool? single;
  VControl? topRight;
  int? topRightAlignment;

  factory VCTabFolder.fromJson(Map<String, dynamic> json) =>
      _$VCTabFolderFromJson(json);
  Map<String, dynamic> toJson() => _$VCTabFolderToJson(this);
}
