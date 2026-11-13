import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/tableitem_evolve.dart';
import 'widgets.dart';

class TableItemSwt<V extends VTableItem> extends ItemSwt<V> {
  const TableItemSwt({super.key, required super.value});

  @override
  State createState() => TableItemImpl<TableItemSwt<VTableItem>, VTableItem>();
}

class VTableItem extends VItem {
  VTableItem() : this.empty();
  VTableItem.empty() {
    swt = "TableItem";
  }

  VColor? background;
  List<VColor?>? backgrounds;
  bool? checked;
  VFont? font;
  List<VFont?>? fonts;
  VColor? foreground;
  List<VColor?>? foregrounds;
  bool? grayed;
  List<VImage?>? images;
  List<int>? paintedImages;
  List<int>? paintedTexts;
  List<String?>? texts;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTableItem) {
      background = other.background;
      backgrounds = other.backgrounds;
      checked = other.checked;
      font = other.font;
      fonts = other.fonts;
      foreground = other.foreground;
      foregrounds = other.foregrounds;
      grayed = other.grayed;
      images = other.images;
      paintedImages = other.paintedImages;
      paintedTexts = other.paintedTexts;
      texts = other.texts;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 580: // background
        background = r.color();
        return true;
      case 581: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 582: // checked
        checked = r.boolean();
        return true;
      case 583: // font
        font = r.object(VFont.new);
        return true;
      case 584: // fonts
        fonts = r.objects(VFont.new);
        return true;
      case 585: // foreground
        foreground = r.color();
        return true;
      case 586: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 587: // grayed
        grayed = r.boolean();
        return true;
      case 588: // images
        images = r.list((r) => r.image());
        return true;
      case 589: // paintedImages
        paintedImages = r.ints();
        return true;
      case 590: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 591: // texts
        texts = r.strings();
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
        w.key(580);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(581);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(582, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(583);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(584);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(585);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(586);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(587, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(588);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = paintedImages;
      if (v_ != null) {
        w.key(589);
        w.ints(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(590);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(591);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(580, background);
    visit(581, backgrounds);
    visit(582, checked);
    visit(583, font);
    visit(584, fonts);
    visit(585, foreground);
    visit(586, foregrounds);
    visit(587, grayed);
    visit(588, images);
    visit(589, paintedImages);
    visit(590, paintedTexts);
    visit(591, texts);
  }
}
