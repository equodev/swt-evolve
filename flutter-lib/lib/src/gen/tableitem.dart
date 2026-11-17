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
      case 5376: // background
        background = r.color();
        return true;
      case 5377: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 5378: // checked
        checked = r.boolean();
        return true;
      case 5379: // font
        font = r.object(VFont.new);
        return true;
      case 5380: // fonts
        fonts = r.objects(VFont.new);
        return true;
      case 5381: // foreground
        foreground = r.color();
        return true;
      case 5382: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 5383: // grayed
        grayed = r.boolean();
        return true;
      case 5384: // images
        images = r.list((r) => r.image());
        return true;
      case 5387: // paintedImages
        paintedImages = r.ints();
        return true;
      case 5385: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 5386: // texts
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
        w.key(5376);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(5377);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(5378, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(5379);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(5380);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(5381);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(5382);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(5383, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(5384);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = paintedImages;
      if (v_ != null) {
        w.key(5387);
        w.ints(v_);
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(5385);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(5386);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(5376, background);
    visit(5377, backgrounds);
    visit(5378, checked);
    visit(5379, font);
    visit(5380, fonts);
    visit(5381, foreground);
    visit(5382, foregrounds);
    visit(5383, grayed);
    visit(5384, images);
    visit(5387, paintedImages);
    visit(5385, paintedTexts);
    visit(5386, texts);
  }
}
