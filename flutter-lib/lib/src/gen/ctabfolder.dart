import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/ctabitem.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
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

  void sendCTabFolderreorderItems(V val, VEvent? payload) {
    sendEvent(val, "CTabFolder/reorderItems", payload);
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

  bool? borderVisible;
  bool? chevronVisible;
  bool? highlight;
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
  bool? showChevron;
  int? showListPopupSeq;
  bool? single;
  int? tabPosition;
  VControl? topRight;
  int? topRightAlignment;
  bool? unselectedCloseVisible;
  bool? unselectedImageVisible;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCTabFolder) {
      borderVisible = other.borderVisible;
      chevronVisible = other.chevronVisible;
      highlight = other.highlight;
      highlightEnabled = other.highlightEnabled;
      items = other.items;
      maximizeVisible = other.maximizeVisible;
      maximized = other.maximized;
      minimizeVisible = other.minimizeVisible;
      minimized = other.minimized;
      minimumCharacters = other.minimumCharacters;
      selectedImageVisible = other.selectedImageVisible;
      selection = other.selection;
      selectionBackground = other.selectionBackground;
      selectionBarThickness = other.selectionBarThickness;
      selectionBgImage = other.selectionBgImage;
      selectionForeground = other.selectionForeground;
      showChevron = other.showChevron;
      showListPopupSeq = other.showListPopupSeq;
      single = other.single;
      tabPosition = other.tabPosition;
      topRight = other.topRight;
      topRightAlignment = other.topRightAlignment;
      unselectedCloseVisible = other.unselectedCloseVisible;
      unselectedImageVisible = other.unselectedImageVisible;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'borderVisible':
        borderVisible = json['borderVisible'] as bool?;
      case 'chevronVisible':
        chevronVisible = json['chevronVisible'] as bool?;
      case 'highlight':
        highlight = json['highlight'] as bool?;
      case 'highlightEnabled':
        highlightEnabled = json['highlightEnabled'] as bool?;
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => VCTabItem.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'maximizeVisible':
        maximizeVisible = json['maximizeVisible'] as bool?;
      case 'maximized':
        maximized = json['maximized'] as bool?;
      case 'minimizeVisible':
        minimizeVisible = json['minimizeVisible'] as bool?;
      case 'minimized':
        minimized = json['minimized'] as bool?;
      case 'minimumCharacters':
        minimumCharacters = (json['minimumCharacters'] as num?)?.toInt();
      case 'selectedImageVisible':
        selectedImageVisible = json['selectedImageVisible'] as bool?;
      case 'selection':
        selection = (json['selection'] as num?)?.toInt();
      case 'selectionBackground':
        selectionBackground = json['selectionBackground'] == null
            ? null
            : VColor.fromJson(
                json['selectionBackground'] as Map<String, dynamic>,
              );
      case 'selectionBarThickness':
        selectionBarThickness = (json['selectionBarThickness'] as num?)
            ?.toInt();
      case 'selectionBgImage':
        selectionBgImage = json['selectionBgImage'] == null
            ? null
            : VImage.fromJson(json['selectionBgImage'] as Map<String, dynamic>);
      case 'selectionForeground':
        selectionForeground = json['selectionForeground'] == null
            ? null
            : VColor.fromJson(
                json['selectionForeground'] as Map<String, dynamic>,
              );
      case 'showChevron':
        showChevron = json['showChevron'] as bool?;
      case 'showListPopupSeq':
        showListPopupSeq = (json['showListPopupSeq'] as num?)?.toInt();
      case 'single':
        single = json['single'] as bool?;
      case 'tabPosition':
        tabPosition = (json['tabPosition'] as num?)?.toInt();
      case 'topRight':
        topRight = json['topRight'] == null
            ? null
            : VControl.fromJson(json['topRight'] as Map<String, dynamic>);
      case 'topRightAlignment':
        topRightAlignment = (json['topRightAlignment'] as num?)?.toInt();
      case 'unselectedCloseVisible':
        unselectedCloseVisible = json['unselectedCloseVisible'] as bool?;
      case 'unselectedImageVisible':
        unselectedImageVisible = json['unselectedImageVisible'] as bool?;
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
    topRight = VWidget.adoptOne(topRight, adopt);
  }

  factory VCTabFolder.fromJson(Map<String, dynamic> json) =>
      _$VCTabFolderFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VCTabFolderToJson(this);
}
