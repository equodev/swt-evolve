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
      case 658: // background
        background = r.color();
        return true;
      case 659: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 660: // checked
        checked = r.boolean();
        return true;
      case 661: // expanded
        expanded = r.boolean();
        return true;
      case 662: // font
        font = r.object(VFont.new);
        return true;
      case 663: // fonts
        fonts = r.objects(VFont.new);
        return true;
      case 664: // foreground
        foreground = r.color();
        return true;
      case 665: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 666: // grayed
        grayed = r.boolean();
        return true;
      case 667: // images
        images = r.list((r) => r.image());
        return true;
      case 669: // items
        items = r.items<VTreeItem>(WireType.treeItem, items);
        return true;
      case 670: // paintedImages
        paintedImages = r.ints();
        return true;
      case 671: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 672: // texts
        texts = r.strings();
        return true;
      case 668: // itemCount
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
        w.key(658);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(659);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(660, v_);
      }
    }
    {
      final v_ = expanded;
      if (v_ != null) {
        w.keyBoolean(661, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(662);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(663);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(664);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(665);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(666, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(667);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(669);
        w.items(v_);
      }
    }
    {
      final v_ = paintedImages;
      if (v_ != null) {
        w.key(670);
        w.ints(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(671);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(672);
        w.strings(v_);
      }
    }
    {
      final v_ = itemCount;
      if (v_ != null) {
        w.keyInt32(668, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(658, background);
    visit(659, backgrounds);
    visit(660, checked);
    visit(661, expanded);
    visit(662, font);
    visit(663, fonts);
    visit(664, foreground);
    visit(665, foregrounds);
    visit(666, grayed);
    visit(667, images);
    visit(669, items);
    visit(670, paintedImages);
    visit(671, paintedTexts);
    visit(672, texts);
    visit(668, itemCount);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
