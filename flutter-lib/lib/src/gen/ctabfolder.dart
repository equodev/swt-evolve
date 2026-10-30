import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
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
import '../gen/widgets.dart';
import '../impl/ctabfolder_evolve.dart';
import 'event.dart';
import 'widgets.dart';

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

  void sendCTabFolderstripLaidOut(V val, VEvent? payload) {
    sendEvent(val, "CTabFolder/stripLaidOut", payload);
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
  int? tabHeight;
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
      tabHeight = other.tabHeight;
      tabPosition = other.tabPosition;
      topRight = other.topRight;
      topRightAlignment = other.topRightAlignment;
      unselectedCloseVisible = other.unselectedCloseVisible;
      unselectedImageVisible = other.unselectedImageVisible;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 248: // borderVisible
        borderVisible = r.boolean();
        return true;
      case 249: // chevronVisible
        chevronVisible = r.boolean();
        return true;
      case 250: // highlight
        highlight = r.boolean();
        return true;
      case 251: // highlightEnabled
        highlightEnabled = r.boolean();
        return true;
      case 252: // items
        items = r.refs<VCTabItem>();
        return true;
      case 253: // maximizeVisible
        maximizeVisible = r.boolean();
        return true;
      case 254: // maximized
        maximized = r.boolean();
        return true;
      case 255: // minimizeVisible
        minimizeVisible = r.boolean();
        return true;
      case 256: // minimized
        minimized = r.boolean();
        return true;
      case 257: // minimumCharacters
        minimumCharacters = r.int32();
        return true;
      case 258: // selectedImageVisible
        selectedImageVisible = r.boolean();
        return true;
      case 259: // selection
        selection = r.int32();
        return true;
      case 260: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 261: // selectionBarThickness
        selectionBarThickness = r.int32();
        return true;
      case 262: // selectionBgImage
        selectionBgImage = r.image();
        return true;
      case 263: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 264: // showChevron
        showChevron = r.boolean();
        return true;
      case 265: // showListPopupSeq
        showListPopupSeq = r.int32();
        return true;
      case 266: // single
        single = r.boolean();
        return true;
      case 1374: // tabHeight
        tabHeight = r.intBoxed();
        return true;
      case 267: // tabPosition
        tabPosition = r.int32();
        return true;
      case 268: // topRight
        topRight = r.ref<VControl>();
        return true;
      case 269: // topRightAlignment
        topRightAlignment = r.int32();
        return true;
      case 270: // unselectedCloseVisible
        unselectedCloseVisible = r.boolean();
        return true;
      case 271: // unselectedImageVisible
        unselectedImageVisible = r.boolean();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = borderVisible;
      if (v_ != null && w.wants(248)) {
        w.key(248);
        w.boolean(v_);
      }
    }
    {
      final v_ = chevronVisible;
      if (v_ != null && w.wants(249)) {
        w.key(249);
        w.boolean(v_);
      }
    }
    {
      final v_ = highlight;
      if (v_ != null && w.wants(250)) {
        w.key(250);
        w.boolean(v_);
      }
    }
    {
      final v_ = highlightEnabled;
      if (v_ != null && w.wants(251)) {
        w.key(251);
        w.boolean(v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null && w.wants(252)) {
        w.key(252);
        w.refs(v_);
      }
    }
    {
      final v_ = maximizeVisible;
      if (v_ != null && w.wants(253)) {
        w.key(253);
        w.boolean(v_);
      }
    }
    {
      final v_ = maximized;
      if (v_ != null && w.wants(254)) {
        w.key(254);
        w.boolean(v_);
      }
    }
    {
      final v_ = minimizeVisible;
      if (v_ != null && w.wants(255)) {
        w.key(255);
        w.boolean(v_);
      }
    }
    {
      final v_ = minimized;
      if (v_ != null && w.wants(256)) {
        w.key(256);
        w.boolean(v_);
      }
    }
    {
      final v_ = minimumCharacters;
      if (v_ != null && w.wants(257)) {
        w.key(257);
        w.int32(v_);
      }
    }
    {
      final v_ = selectedImageVisible;
      if (v_ != null && w.wants(258)) {
        w.key(258);
        w.boolean(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(259)) {
        w.key(259);
        w.int32(v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null && w.wants(260)) {
        w.key(260);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionBarThickness;
      if (v_ != null && w.wants(261)) {
        w.key(261);
        w.int32(v_);
      }
    }
    {
      final v_ = selectionBgImage;
      if (v_ != null && w.wants(262)) {
        w.key(262);
        w.ref(v_.id);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null && w.wants(263)) {
        w.key(263);
        w.colorValue(v_);
      }
    }
    {
      final v_ = showChevron;
      if (v_ != null && w.wants(264)) {
        w.key(264);
        w.boolean(v_);
      }
    }
    {
      final v_ = showListPopupSeq;
      if (v_ != null && w.wants(265)) {
        w.key(265);
        w.int32(v_);
      }
    }
    {
      final v_ = single;
      if (v_ != null && w.wants(266)) {
        w.key(266);
        w.boolean(v_);
      }
    }
    {
      final v_ = tabHeight;
      if (v_ != null && w.wants(1374)) {
        w.key(1374);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = tabPosition;
      if (v_ != null && w.wants(267)) {
        w.key(267);
        w.int32(v_);
      }
    }
    {
      final v_ = topRight;
      if (v_ != null && w.wants(268)) {
        w.key(268);
        w.ref(v_.id);
      }
    }
    {
      final v_ = topRightAlignment;
      if (v_ != null && w.wants(269)) {
        w.key(269);
        w.int32(v_);
      }
    }
    {
      final v_ = unselectedCloseVisible;
      if (v_ != null && w.wants(270)) {
        w.key(270);
        w.boolean(v_);
      }
    }
    {
      final v_ = unselectedImageVisible;
      if (v_ != null && w.wants(271)) {
        w.key(271);
        w.boolean(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(248, borderVisible);
    visit(249, chevronVisible);
    visit(250, highlight);
    visit(251, highlightEnabled);
    visit(252, items);
    visit(253, maximizeVisible);
    visit(254, maximized);
    visit(255, minimizeVisible);
    visit(256, minimized);
    visit(257, minimumCharacters);
    visit(258, selectedImageVisible);
    visit(259, selection);
    visit(260, selectionBackground);
    visit(261, selectionBarThickness);
    visit(262, selectionBgImage);
    visit(263, selectionForeground);
    visit(264, showChevron);
    visit(265, showListPopupSeq);
    visit(266, single);
    visit(1374, tabHeight);
    visit(267, tabPosition);
    visit(268, topRight);
    visit(269, topRightAlignment);
    visit(270, unselectedCloseVisible);
    visit(271, unselectedImageVisible);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
    topRight = VWidget.adoptOne(topRight, adopt);
  }
}
