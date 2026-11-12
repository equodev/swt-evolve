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
  bool? dirtyIndicatorStyle;
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
  int? stripLayoutSeq;
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
      dirtyIndicatorStyle = other.dirtyIndicatorStyle;
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
      stripLayoutSeq = other.stripLayoutSeq;
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
      case 37: // borderVisible
        borderVisible = r.boolean();
        return true;
      case 38: // chevronVisible
        chevronVisible = r.boolean();
        return true;
      case 39: // dirtyIndicatorStyle
        dirtyIndicatorStyle = r.boolean();
        return true;
      case 40: // highlight
        highlight = r.boolean();
        return true;
      case 41: // highlightEnabled
        highlightEnabled = r.boolean();
        return true;
      case 42: // items
        items = r.refs<VCTabItem>(items);
        return true;
      case 43: // maximizeVisible
        maximizeVisible = r.boolean();
        return true;
      case 44: // maximized
        maximized = r.boolean();
        return true;
      case 45: // minimizeVisible
        minimizeVisible = r.boolean();
        return true;
      case 46: // minimized
        minimized = r.boolean();
        return true;
      case 47: // minimumCharacters
        minimumCharacters = r.int32();
        return true;
      case 48: // selectedImageVisible
        selectedImageVisible = r.boolean();
        return true;
      case 49: // selection
        selection = r.int32();
        return true;
      case 50: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 51: // selectionBarThickness
        selectionBarThickness = r.int32();
        return true;
      case 52: // selectionBgImage
        selectionBgImage = r.image();
        return true;
      case 53: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 54: // showChevron
        showChevron = r.boolean();
        return true;
      case 55: // showListPopupSeq
        showListPopupSeq = r.int32();
        return true;
      case 56: // single
        single = r.boolean();
        return true;
      case 57: // stripLayoutSeq
        stripLayoutSeq = r.int32();
        return true;
      case 58: // tabHeight
        tabHeight = r.intBoxed();
        return true;
      case 59: // tabPosition
        tabPosition = r.int32();
        return true;
      case 60: // topRight
        topRight = r.ref<VControl>();
        return true;
      case 61: // topRightAlignment
        topRightAlignment = r.int32();
        return true;
      case 62: // unselectedCloseVisible
        unselectedCloseVisible = r.boolean();
        return true;
      case 63: // unselectedImageVisible
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
        w.keyBoolean(37, v_);
      }
    }
    {
      final v_ = chevronVisible;
      if (v_ != null) {
        w.keyBoolean(38, v_);
      }
    }
    {
      final v_ = dirtyIndicatorStyle;
      if (v_ != null) {
        w.keyBoolean(39, v_);
      }
    }
    {
      final v_ = highlight;
      if (v_ != null) {
        w.keyBoolean(40, v_);
      }
    }
    {
      final v_ = highlightEnabled;
      if (v_ != null) {
        w.keyBoolean(41, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(42);
        w.refs(v_);
      }
    }
    {
      final v_ = maximizeVisible;
      if (v_ != null) {
        w.keyBoolean(43, v_);
      }
    }
    {
      final v_ = maximized;
      if (v_ != null) {
        w.keyBoolean(44, v_);
      }
    }
    {
      final v_ = minimizeVisible;
      if (v_ != null) {
        w.keyBoolean(45, v_);
      }
    }
    {
      final v_ = minimized;
      if (v_ != null) {
        w.keyBoolean(46, v_);
      }
    }
    {
      final v_ = minimumCharacters;
      if (v_ != null) {
        w.keyInt32(47, v_);
      }
    }
    {
      final v_ = selectedImageVisible;
      if (v_ != null) {
        w.keyBoolean(48, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(49, v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null) {
        w.key(50);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionBarThickness;
      if (v_ != null) {
        w.keyInt32(51, v_);
      }
    }
    {
      final v_ = selectionBgImage;
      if (v_ != null) {
        w.keyRef(52, v_.id);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(53);
        w.colorValue(v_);
      }
    }
    {
      final v_ = showChevron;
      if (v_ != null) {
        w.keyBoolean(54, v_);
      }
    }
    {
      final v_ = showListPopupSeq;
      if (v_ != null) {
        w.keyInt32(55, v_);
      }
    }
    {
      final v_ = single;
      if (v_ != null) {
        w.keyBoolean(56, v_);
      }
    }
    {
      final v_ = stripLayoutSeq;
      if (v_ != null) {
        w.keyInt32(57, v_);
      }
    }
    {
      final v_ = tabHeight;
      if (v_ != null) {
        w.key(58);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = tabPosition;
      if (v_ != null) {
        w.keyInt32(59, v_);
      }
    }
    {
      final v_ = topRight;
      if (v_ != null) {
        w.keyRef(60, v_.id);
      }
    }
    {
      final v_ = topRightAlignment;
      if (v_ != null) {
        w.keyInt32(61, v_);
      }
    }
    {
      final v_ = unselectedCloseVisible;
      if (v_ != null) {
        w.keyBoolean(62, v_);
      }
    }
    {
      final v_ = unselectedImageVisible;
      if (v_ != null) {
        w.keyBoolean(63, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(37, borderVisible);
    visit(38, chevronVisible);
    visit(39, dirtyIndicatorStyle);
    visit(40, highlight);
    visit(41, highlightEnabled);
    visit(42, items);
    visit(43, maximizeVisible);
    visit(44, maximized);
    visit(45, minimizeVisible);
    visit(46, minimized);
    visit(47, minimumCharacters);
    visit(48, selectedImageVisible);
    visit(49, selection);
    visit(50, selectionBackground);
    visit(51, selectionBarThickness);
    visit(52, selectionBgImage);
    visit(53, selectionForeground);
    visit(54, showChevron);
    visit(55, showListPopupSeq);
    visit(56, single);
    visit(57, stripLayoutSeq);
    visit(58, tabHeight);
    visit(59, tabPosition);
    visit(60, topRight);
    visit(61, topRightAlignment);
    visit(62, unselectedCloseVisible);
    visit(63, unselectedImageVisible);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
    topRight = VWidget.adoptOne(topRight, adopt);
  }
}
