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
      case 581: // background
        background = r.color();
        return true;
      case 582: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 583: // checked
        checked = r.boolean();
        return true;
      case 584: // font
        font = r.object(VFont.new);
        return true;
      case 585: // fonts
        fonts = r.objects(VFont.new);
        return true;
      case 586: // foreground
        foreground = r.color();
        return true;
      case 587: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 588: // grayed
        grayed = r.boolean();
        return true;
      case 589: // images
        images = r.list((r) => r.image());
        return true;
      case 590: // paintedImages
        paintedImages = r.ints();
        return true;
      case 591: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 592: // texts
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
        w.key(581);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(582);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(583, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(584);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(585);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(586);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(587);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(588, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(589);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = paintedImages;
      if (v_ != null) {
        w.key(590);
        w.ints(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(591);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(592);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(581, background);
    visit(582, backgrounds);
    visit(583, checked);
    visit(584, font);
    visit(585, fonts);
    visit(586, foreground);
    visit(587, foregrounds);
    visit(588, grayed);
    visit(589, images);
    visit(590, paintedImages);
    visit(591, paintedTexts);
    visit(592, texts);
  }
}
