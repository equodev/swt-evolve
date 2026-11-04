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
      case 645: // background
        background = r.color();
        return true;
      case 646: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 647: // checked
        checked = r.boolean();
        return true;
      case 648: // expanded
        expanded = r.boolean();
        return true;
      case 649: // font
        font = r.object(VFont.new);
        return true;
      case 650: // fonts
        fonts = r.list((r) => r.object(VFont.new));
        return true;
      case 651: // foreground
        foreground = r.color();
        return true;
      case 652: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 653: // grayed
        grayed = r.boolean();
        return true;
      case 654: // images
        images = r.list((r) => r.image());
        return true;
      case 656: // items
        items = r.refs<VTreeItem>();
        return true;
      case 657: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 658: // texts
        texts = r.strings();
        return true;
      case 655: // itemCount
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
        w.key(645);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(646);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(647, v_);
      }
    }
    {
      final v_ = expanded;
      if (v_ != null) {
        w.keyBoolean(648, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(649);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(650);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(651);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(652);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(653, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(654);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(656);
        w.refs(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(657);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(658);
        w.strings(v_);
      }
    }
    {
      final v_ = itemCount;
      if (v_ != null) {
        w.keyInt32(655, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(645, background);
    visit(646, backgrounds);
    visit(647, checked);
    visit(648, expanded);
    visit(649, font);
    visit(650, fonts);
    visit(651, foreground);
    visit(652, foregrounds);
    visit(653, grayed);
    visit(654, images);
    visit(656, items);
    visit(657, paintedTexts);
    visit(658, texts);
    visit(655, itemCount);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
