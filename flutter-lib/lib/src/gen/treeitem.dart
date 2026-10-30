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
      case 640: // background
        background = r.color();
        return true;
      case 641: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 642: // checked
        checked = r.boolean();
        return true;
      case 643: // expanded
        expanded = r.boolean();
        return true;
      case 644: // font
        font = r.object(VFont.new);
        return true;
      case 645: // fonts
        fonts = r.list((r) => r.object(VFont.new));
        return true;
      case 646: // foreground
        foreground = r.color();
        return true;
      case 647: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 648: // grayed
        grayed = r.boolean();
        return true;
      case 649: // images
        images = r.list((r) => r.image());
        return true;
      case 651: // items
        items = r.refs<VTreeItem>();
        return true;
      case 652: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 653: // texts
        texts = r.strings();
        return true;
      case 650: // itemCount
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
        w.key(640);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(641);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(642, v_);
      }
    }
    {
      final v_ = expanded;
      if (v_ != null) {
        w.keyBoolean(643, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(644);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(645);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(646);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(647);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(648, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(649);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(651);
        w.refs(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(652);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(653);
        w.strings(v_);
      }
    }
    {
      final v_ = itemCount;
      if (v_ != null) {
        w.keyInt32(650, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(640, background);
    visit(641, backgrounds);
    visit(642, checked);
    visit(643, expanded);
    visit(644, font);
    visit(645, fonts);
    visit(646, foreground);
    visit(647, foregrounds);
    visit(648, grayed);
    visit(649, images);
    visit(651, items);
    visit(652, paintedTexts);
    visit(653, texts);
    visit(650, itemCount);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
