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
      case 95: // background
        background = r.color();
        return true;
      case 96: // backgrounds
        backgrounds = r.list(() => r.color());
        return true;
      case 97: // checked
        checked = r.boolean();
        return true;
      case 98: // expanded
        expanded = r.boolean();
        return true;
      case 99: // font
        font = r.object(VFont.new);
        return true;
      case 100: // fonts
        fonts = r.list(() => r.object(VFont.new));
        return true;
      case 101: // foreground
        foreground = r.color();
        return true;
      case 102: // foregrounds
        foregrounds = r.list(() => r.color());
        return true;
      case 103: // grayed
        grayed = r.boolean();
        return true;
      case 104: // images
        images = r.list(() => r.image());
        return true;
      case 106: // items
        items = r.refs<VTreeItem>();
        return true;
      case 107: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 108: // texts
        texts = r.strings();
        return true;
      case 105: // itemCount
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
      if (v_ != null && w.wants(95)) {
        w.key(95);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null && w.wants(96)) {
        w.key(96);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null && w.wants(97)) {
        w.key(97);
        w.boolean(v_);
      }
    }
    {
      final v_ = expanded;
      if (v_ != null && w.wants(98)) {
        w.key(98);
        w.boolean(v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null && w.wants(99)) {
        w.key(99);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null && w.wants(100)) {
        w.key(100);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null && w.wants(101)) {
        w.key(101);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null && w.wants(102)) {
        w.key(102);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null && w.wants(103)) {
        w.key(103);
        w.boolean(v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null && w.wants(104)) {
        w.key(104);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = items;
      if (v_ != null && w.wants(106)) {
        w.key(106);
        w.refs(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null && w.wants(107)) {
        w.key(107);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null && w.wants(108)) {
        w.key(108);
        w.strings(v_);
      }
    }
    {
      final v_ = itemCount;
      if (v_ != null && w.wants(105)) {
        w.key(105);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(95, background);
    visit(96, backgrounds);
    visit(97, checked);
    visit(98, expanded);
    visit(99, font);
    visit(100, fonts);
    visit(101, foreground);
    visit(102, foregrounds);
    visit(103, grayed);
    visit(104, images);
    visit(106, items);
    visit(107, paintedTexts);
    visit(108, texts);
    visit(105, itemCount);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
