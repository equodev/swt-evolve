import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/treeitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/treeitem_evolve.dart';
import 'widgets.dart';

class TreeItemSwt<V extends VTreeItem> extends ItemSwt<V> {
  const TreeItemSwt({super.key, required super.value});

  @override
  State createState() => TreeItemImpl<TreeItemSwt<VTreeItem>, VTreeItem>();
}

class VTreeItem extends VItem {
  VTreeItem() : this.empty();
  VTreeItem.empty() {
    swt = "TreeItem";
  }

  int? itemCount;
  VColor? background;
  List<VColor?>? backgrounds;
  bool? checked;
  bool? expanded;
  VFont? font;
  List<VFont?>? fonts;
  VColor? foreground;
  List<VColor?>? foregrounds;
  bool? grayed;
  List<VImage?>? images;
  List<VTreeItem>? items;
  List<int>? paintedTexts;
  List<String?>? texts;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTreeItem) {
      background = other.background;
      backgrounds = other.backgrounds;
      checked = other.checked;
      expanded = other.expanded;
      font = other.font;
      fonts = other.fonts;
      foreground = other.foreground;
      foregrounds = other.foregrounds;
      grayed = other.grayed;
      images = other.images;
      items = other.items;
      paintedTexts = other.paintedTexts;
      texts = other.texts;
      itemCount = other.itemCount;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 646: // background
        background = r.color();
        return true;
      case 647: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 648: // checked
        checked = r.boolean();
        return true;
      case 649: // expanded
        expanded = r.boolean();
        return true;
      case 650: // font
        font = r.object(VFont.new);
        return true;
      case 651: // fonts
        fonts = r.list((r) => r.object(VFont.new));
        return true;
      case 652: // foreground
        foreground = r.color();
        return true;
      case 653: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 654: // grayed
        grayed = r.boolean();
        return true;
      case 655: // images
        images = r.list((r) => r.image());
        return true;
      case 657: // items
        items = r.refs<VTreeItem>();
        return true;
      case 658: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 659: // texts
        texts = r.strings();
        return true;
      case 656: // itemCount
        itemCount = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = background;
      if (v_ != null) {
        w.key(646);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(647);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(648, v_);
      }
    }
    {
      final v_ = expanded;
      if (v_ != null) {
        w.keyBoolean(649, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(650);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(651);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(652);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(653);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(654, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(655);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(657);
        w.refs(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(658);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(659);
        w.strings(v_);
      }
    }
    {
      final v_ = itemCount;
      if (v_ != null) {
        w.keyInt32(656, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(646, background);
    visit(647, backgrounds);
    visit(648, checked);
    visit(649, expanded);
    visit(650, font);
    visit(651, fonts);
    visit(652, foreground);
    visit(653, foregrounds);
    visit(654, grayed);
    visit(655, images);
    visit(657, items);
    visit(658, paintedTexts);
    visit(659, texts);
    visit(656, itemCount);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
