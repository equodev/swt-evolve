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
  List<int>? paintedImages;
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
      paintedImages = other.paintedImages;
      paintedTexts = other.paintedTexts;
      texts = other.texts;
      itemCount = other.itemCount;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 6144: // background
        background = r.color();
        return true;
      case 6145: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 6146: // checked
        checked = r.boolean();
        return true;
      case 6147: // expanded
        expanded = r.boolean();
        return true;
      case 6148: // font
        font = r.object(VFont.new);
        return true;
      case 6149: // fonts
        fonts = r.objects(VFont.new);
        return true;
      case 6150: // foreground
        foreground = r.color();
        return true;
      case 6151: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 6152: // grayed
        grayed = r.boolean();
        return true;
      case 6153: // images
        images = r.list((r) => r.image());
        return true;
      case 6155: // items
        items = r.items<VTreeItem>(WireType.treeItem, items);
        return true;
      case 6158: // paintedImages
        paintedImages = r.ints();
        return true;
      case 6156: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 6157: // texts
        texts = r.strings();
        return true;
      case 6154: // itemCount
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
        w.key(6144);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(6145);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(6146, v_);
      }
    }
    {
      final v_ = expanded;
      if (v_ != null) {
        w.keyBoolean(6147, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(6148);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(6149);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(6150);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(6151);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(6152, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(6153);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(6155);
        w.items(v_);
      }
    }
    {
      final v_ = paintedImages;
      if (v_ != null) {
        w.key(6158);
        w.ints(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(6156);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(6157);
        w.strings(v_);
      }
    }
    {
      final v_ = itemCount;
      if (v_ != null) {
        w.keyInt32(6154, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(6144, background);
    visit(6145, backgrounds);
    visit(6146, checked);
    visit(6147, expanded);
    visit(6148, font);
    visit(6149, fonts);
    visit(6150, foreground);
    visit(6151, foregrounds);
    visit(6152, grayed);
    visit(6153, images);
    visit(6155, items);
    visit(6158, paintedImages);
    visit(6156, paintedTexts);
    visit(6157, texts);
    visit(6154, itemCount);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
