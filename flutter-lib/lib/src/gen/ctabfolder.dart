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
    sendEvent(val, 5, payload); // CTabFolder/itemClosed
  }

  void sendCTabFolderreorderItems(V val, VEvent? payload) {
    sendEvent(val, 6, payload); // CTabFolder/reorderItems
  }

  void sendCTabFolderstripLaidOut(V val, VEvent? payload) {
    sendEvent(val, 7, payload); // CTabFolder/stripLaidOut
  }

  void sendCTabFolder2close(V val, VEvent? payload) {
    sendEvent(val, 8, payload); // CTabFolder2/close
  }

  void sendCTabFolder2maximize(V val, VEvent? payload) {
    sendEvent(val, 9, payload); // CTabFolder2/maximize
  }

  void sendCTabFolder2minimize(V val, VEvent? payload) {
    sendEvent(val, 10, payload); // CTabFolder2/minimize
  }

  void sendCTabFolder2restore(V val, VEvent? payload) {
    sendEvent(val, 11, payload); // CTabFolder2/restore
  }

  void sendCTabFolder2showList(V val, VEvent? payload) {
    sendEvent(val, 12, payload); // CTabFolder2/showList
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
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
      case 36: // borderVisible
        borderVisible = r.boolean();
        return true;
      case 37: // chevronVisible
        chevronVisible = r.boolean();
        return true;
      case 38: // highlight
        highlight = r.boolean();
        return true;
      case 39: // highlightEnabled
        highlightEnabled = r.boolean();
        return true;
      case 40: // items
        items = r.refs<VCTabItem>();
        return true;
      case 41: // maximizeVisible
        maximizeVisible = r.boolean();
        return true;
      case 42: // maximized
        maximized = r.boolean();
        return true;
      case 43: // minimizeVisible
        minimizeVisible = r.boolean();
        return true;
      case 44: // minimized
        minimized = r.boolean();
        return true;
      case 45: // minimumCharacters
        minimumCharacters = r.int32();
        return true;
      case 46: // selectedImageVisible
        selectedImageVisible = r.boolean();
        return true;
      case 47: // selection
        selection = r.int32();
        return true;
      case 48: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 49: // selectionBarThickness
        selectionBarThickness = r.int32();
        return true;
      case 50: // selectionBgImage
        selectionBgImage = r.image();
        return true;
      case 51: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 52: // showChevron
        showChevron = r.boolean();
        return true;
      case 53: // showListPopupSeq
        showListPopupSeq = r.int32();
        return true;
      case 54: // single
        single = r.boolean();
        return true;
      case 55: // tabHeight
        tabHeight = r.intBoxed();
        return true;
      case 56: // tabPosition
        tabPosition = r.int32();
        return true;
      case 57: // topRight
        topRight = r.ref<VControl>();
        return true;
      case 58: // topRightAlignment
        topRightAlignment = r.int32();
        return true;
      case 59: // unselectedCloseVisible
        unselectedCloseVisible = r.boolean();
        return true;
      case 60: // unselectedImageVisible
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
      if (v_ != null) {
        w.keyBoolean(36, v_);
      }
    }
    {
      final v_ = chevronVisible;
      if (v_ != null) {
        w.keyBoolean(37, v_);
      }
    }
    {
      final v_ = highlight;
      if (v_ != null) {
        w.keyBoolean(38, v_);
      }
    }
    {
      final v_ = highlightEnabled;
      if (v_ != null) {
        w.keyBoolean(39, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(40);
        w.refs(v_);
      }
    }
    {
      final v_ = maximizeVisible;
      if (v_ != null) {
        w.keyBoolean(41, v_);
      }
    }
    {
      final v_ = maximized;
      if (v_ != null) {
        w.keyBoolean(42, v_);
      }
    }
    {
      final v_ = minimizeVisible;
      if (v_ != null) {
        w.keyBoolean(43, v_);
      }
    }
    {
      final v_ = minimized;
      if (v_ != null) {
        w.keyBoolean(44, v_);
      }
    }
    {
      final v_ = minimumCharacters;
      if (v_ != null) {
        w.keyInt32(45, v_);
      }
    }
    {
      final v_ = selectedImageVisible;
      if (v_ != null) {
        w.keyBoolean(46, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(47, v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null) {
        w.key(48);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionBarThickness;
      if (v_ != null) {
        w.keyInt32(49, v_);
      }
    }
    {
      final v_ = selectionBgImage;
      if (v_ != null) {
        w.keyRef(50, v_.id);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(51);
        w.colorValue(v_);
      }
    }
    {
      final v_ = showChevron;
      if (v_ != null) {
        w.keyBoolean(52, v_);
      }
    }
    {
      final v_ = showListPopupSeq;
      if (v_ != null) {
        w.keyInt32(53, v_);
      }
    }
    {
      final v_ = single;
      if (v_ != null) {
        w.keyBoolean(54, v_);
      }
    }
    {
      final v_ = tabHeight;
      if (v_ != null) {
        w.key(55);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = tabPosition;
      if (v_ != null) {
        w.keyInt32(56, v_);
      }
    }
    {
      final v_ = topRight;
      if (v_ != null) {
        w.keyRef(57, v_.id);
      }
    }
    {
      final v_ = topRightAlignment;
      if (v_ != null) {
        w.keyInt32(58, v_);
      }
    }
    {
      final v_ = unselectedCloseVisible;
      if (v_ != null) {
        w.keyBoolean(59, v_);
      }
    }
    {
      final v_ = unselectedImageVisible;
      if (v_ != null) {
        w.keyBoolean(60, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(36, borderVisible);
    visit(37, chevronVisible);
    visit(38, highlight);
    visit(39, highlightEnabled);
    visit(40, items);
    visit(41, maximizeVisible);
    visit(42, maximized);
    visit(43, minimizeVisible);
    visit(44, minimized);
    visit(45, minimumCharacters);
    visit(46, selectedImageVisible);
    visit(47, selection);
    visit(48, selectionBackground);
    visit(49, selectionBarThickness);
    visit(50, selectionBgImage);
    visit(51, selectionForeground);
    visit(52, showChevron);
    visit(53, showListPopupSeq);
    visit(54, single);
    visit(55, tabHeight);
    visit(56, tabPosition);
    visit(57, topRight);
    visit(58, topRightAlignment);
    visit(59, unselectedCloseVisible);
    visit(60, unselectedImageVisible);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
    topRight = VWidget.adoptOne(topRight, adopt);
  }
}
