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
      case 656: // background
        background = r.color();
        return true;
      case 657: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 658: // checked
        checked = r.boolean();
        return true;
      case 659: // expanded
        expanded = r.boolean();
        return true;
      case 660: // font
        font = r.object(VFont.new);
        return true;
      case 661: // fonts
        fonts = r.objects(VFont.new);
        return true;
      case 662: // foreground
        foreground = r.color();
        return true;
      case 663: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 664: // grayed
        grayed = r.boolean();
        return true;
      case 665: // images
        images = r.list((r) => r.image());
        return true;
      case 667: // items
        items = r.items<VTreeItem>(WireType.treeItem, items);
        return true;
      case 668: // paintedImages
        paintedImages = r.ints();
        return true;
      case 669: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 670: // texts
        texts = r.strings();
        return true;
      case 666: // itemCount
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
        w.key(656);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(657);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(658, v_);
      }
    }
    {
      final v_ = expanded;
      if (v_ != null) {
        w.keyBoolean(659, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(660);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(661);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(662);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(663);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(664, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(665);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(667);
        w.items(v_);
      }
    }
    {
      final v_ = paintedImages;
      if (v_ != null) {
        w.key(668);
        w.ints(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(669);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(670);
        w.strings(v_);
      }
    }
    {
      final v_ = itemCount;
      if (v_ != null) {
        w.keyInt32(666, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(656, background);
    visit(657, backgrounds);
    visit(658, checked);
    visit(659, expanded);
    visit(660, font);
    visit(661, fonts);
    visit(662, foreground);
    visit(663, foregrounds);
    visit(664, grayed);
    visit(665, images);
    visit(667, items);
    visit(668, paintedImages);
    visit(669, paintedTexts);
    visit(670, texts);
    visit(666, itemCount);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
