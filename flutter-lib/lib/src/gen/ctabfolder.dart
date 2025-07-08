import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/ctabitem.dart';
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
  List<VCTabItem>? items;
  bool? minimized;
  bool? showMin;
  int? minChars;
  bool? maximized;
  bool? showMax;
  bool? mru;
  int? selectedIndex;
  VColor? selectionBackground;
  VColor? selectionForeground;
  bool? simple;
  bool? single;
  VControl? topRight;
  int? topRightAlignment;
  bool? showUnselectedClose;
  bool? showUnselectedImage;
  bool? showSelectedImage;
  List<VColor>? gradientColors;
  List<int>? gradientPercents;
  bool? gradientVertical;
  List<VColor>? selectionGradientColors;
  List<int>? selectionGradientPercents;
  bool? selectionGradientVertical;
  int? selectionHighlightBarThickness;
  int? fixedTabHeight;
  bool? onBottom;
  bool? highlightEnabled;

  factory VCTabFolder.fromJson(Map<String, dynamic> json) =>
      _$VCTabFolderFromJson(json);
  Map<String, dynamic> toJson() => _$VCTabFolderToJson(this);
}
