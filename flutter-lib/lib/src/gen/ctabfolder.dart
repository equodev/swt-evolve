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
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
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
      case 512: // borderVisible
        borderVisible = r.boolean();
        return true;
      case 513: // chevronVisible
        chevronVisible = r.boolean();
        return true;
      case 538: // dirtyIndicatorStyle
        dirtyIndicatorStyle = r.boolean();
        return true;
      case 514: // highlight
        highlight = r.boolean();
        return true;
      case 515: // highlightEnabled
        highlightEnabled = r.boolean();
        return true;
      case 516: // items
        items = r.refs<VCTabItem>(items);
        return true;
      case 517: // maximizeVisible
        maximizeVisible = r.boolean();
        return true;
      case 518: // maximized
        maximized = r.boolean();
        return true;
      case 519: // minimizeVisible
        minimizeVisible = r.boolean();
        return true;
      case 520: // minimized
        minimized = r.boolean();
        return true;
      case 521: // minimumCharacters
        minimumCharacters = r.int32();
        return true;
      case 522: // selectedImageVisible
        selectedImageVisible = r.boolean();
        return true;
      case 523: // selection
        selection = r.int32();
        return true;
      case 524: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 525: // selectionBarThickness
        selectionBarThickness = r.int32();
        return true;
      case 526: // selectionBgImage
        selectionBgImage = r.image();
        return true;
      case 527: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 528: // showChevron
        showChevron = r.boolean();
        return true;
      case 529: // showListPopupSeq
        showListPopupSeq = r.int32();
        return true;
      case 530: // single
        single = r.boolean();
        return true;
      case 537: // stripLayoutSeq
        stripLayoutSeq = r.int32();
        return true;
      case 531: // tabHeight
        tabHeight = r.intBoxed();
        return true;
      case 532: // tabPosition
        tabPosition = r.int32();
        return true;
      case 533: // topRight
        topRight = r.ref<VControl>();
        return true;
      case 534: // topRightAlignment
        topRightAlignment = r.int32();
        return true;
      case 535: // unselectedCloseVisible
        unselectedCloseVisible = r.boolean();
        return true;
      case 536: // unselectedImageVisible
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
        w.keyBoolean(512, v_);
      }
    }
    {
      final v_ = chevronVisible;
      if (v_ != null) {
        w.keyBoolean(513, v_);
      }
    }
    {
      final v_ = dirtyIndicatorStyle;
      if (v_ != null) {
        w.keyBoolean(538, v_);
      }
    }
    {
      final v_ = highlight;
      if (v_ != null) {
        w.keyBoolean(514, v_);
      }
    }
    {
      final v_ = highlightEnabled;
      if (v_ != null) {
        w.keyBoolean(515, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(516);
        w.refs(v_);
      }
    }
    {
      final v_ = maximizeVisible;
      if (v_ != null) {
        w.keyBoolean(517, v_);
      }
    }
    {
      final v_ = maximized;
      if (v_ != null) {
        w.keyBoolean(518, v_);
      }
    }
    {
      final v_ = minimizeVisible;
      if (v_ != null) {
        w.keyBoolean(519, v_);
      }
    }
    {
      final v_ = minimized;
      if (v_ != null) {
        w.keyBoolean(520, v_);
      }
    }
    {
      final v_ = minimumCharacters;
      if (v_ != null) {
        w.keyInt32(521, v_);
      }
    }
    {
      final v_ = selectedImageVisible;
      if (v_ != null) {
        w.keyBoolean(522, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(523, v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null) {
        w.key(524);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionBarThickness;
      if (v_ != null) {
        w.keyInt32(525, v_);
      }
    }
    {
      final v_ = selectionBgImage;
      if (v_ != null) {
        w.keyRef(526, v_.id);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(527);
        w.colorValue(v_);
      }
    }
    {
      final v_ = showChevron;
      if (v_ != null) {
        w.keyBoolean(528, v_);
      }
    }
    {
      final v_ = showListPopupSeq;
      if (v_ != null) {
        w.keyInt32(529, v_);
      }
    }
    {
      final v_ = single;
      if (v_ != null) {
        w.keyBoolean(530, v_);
      }
    }
    {
      final v_ = stripLayoutSeq;
      if (v_ != null) {
        w.keyInt32(537, v_);
      }
    }
    {
      final v_ = tabHeight;
      if (v_ != null) {
        w.key(531);
        w.intBoxed(v_);
      }
    }
    {
      final v_ = tabPosition;
      if (v_ != null) {
        w.keyInt32(532, v_);
      }
    }
    {
      final v_ = topRight;
      if (v_ != null) {
        w.keyRef(533, v_.id);
      }
    }
    {
      final v_ = topRightAlignment;
      if (v_ != null) {
        w.keyInt32(534, v_);
      }
    }
    {
      final v_ = unselectedCloseVisible;
      if (v_ != null) {
        w.keyBoolean(535, v_);
      }
    }
    {
      final v_ = unselectedImageVisible;
      if (v_ != null) {
        w.keyBoolean(536, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(512, borderVisible);
    visit(513, chevronVisible);
    visit(538, dirtyIndicatorStyle);
    visit(514, highlight);
    visit(515, highlightEnabled);
    visit(516, items);
    visit(517, maximizeVisible);
    visit(518, maximized);
    visit(519, minimizeVisible);
    visit(520, minimized);
    visit(521, minimumCharacters);
    visit(522, selectedImageVisible);
    visit(523, selection);
    visit(524, selectionBackground);
    visit(525, selectionBarThickness);
    visit(526, selectionBgImage);
    visit(527, selectionForeground);
    visit(528, showChevron);
    visit(529, showListPopupSeq);
    visit(530, single);
    visit(537, stripLayoutSeq);
    visit(531, tabHeight);
    visit(532, tabPosition);
    visit(533, topRight);
    visit(534, topRightAlignment);
    visit(535, unselectedCloseVisible);
    visit(536, unselectedImageVisible);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
    topRight = VWidget.adoptOne(topRight, adopt);
  }
}
