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
      case 655: // background
        background = r.color();
        return true;
      case 656: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 657: // checked
        checked = r.boolean();
        return true;
      case 658: // expanded
        expanded = r.boolean();
        return true;
      case 659: // font
        font = r.object(VFont.new);
        return true;
      case 660: // fonts
        fonts = r.objects(VFont.new);
        return true;
      case 661: // foreground
        foreground = r.color();
        return true;
      case 662: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 663: // grayed
        grayed = r.boolean();
        return true;
      case 664: // images
        images = r.list((r) => r.image());
        return true;
      case 666: // items
        items = r.items<VTreeItem>(WireType.treeItem, items);
        return true;
      case 667: // paintedImages
        paintedImages = r.ints();
        return true;
      case 668: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 669: // texts
        texts = r.strings();
        return true;
      case 665: // itemCount
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
        w.key(655);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(656);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(657, v_);
      }
    }
    {
      final v_ = expanded;
      if (v_ != null) {
        w.keyBoolean(658, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(659);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(660);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(661);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(662);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(663, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(664);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(666);
        w.items(v_);
      }
    }
    {
      final v_ = paintedImages;
      if (v_ != null) {
        w.key(667);
        w.ints(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(668);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(669);
        w.strings(v_);
      }
    }
    {
      final v_ = itemCount;
      if (v_ != null) {
        w.keyInt32(665, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(655, background);
    visit(656, backgrounds);
    visit(657, checked);
    visit(658, expanded);
    visit(659, font);
    visit(660, fonts);
    visit(661, foreground);
    visit(662, foregrounds);
    visit(663, grayed);
    visit(664, images);
    visit(666, items);
    visit(667, paintedImages);
    visit(668, paintedTexts);
    visit(669, texts);
    visit(665, itemCount);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
