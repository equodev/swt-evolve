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
      paintedTexts = other.paintedTexts;
      texts = other.texts;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 564: // background
        background = r.color();
        return true;
      case 565: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 566: // checked
        checked = r.boolean();
        return true;
      case 567: // font
        font = r.object(VFont.new);
        return true;
      case 568: // fonts
        fonts = r.list((r) => r.object(VFont.new));
        return true;
      case 569: // foreground
        foreground = r.color();
        return true;
      case 570: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 571: // grayed
        grayed = r.boolean();
        return true;
      case 572: // images
        images = r.list((r) => r.image());
        return true;
      case 573: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 574: // texts
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
        w.key(564);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(565);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(566, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(567);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(568);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(569);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(570);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(571, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(572);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(573);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(574);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(564, background);
    visit(565, backgrounds);
    visit(566, checked);
    visit(567, font);
    visit(568, fonts);
    visit(569, foreground);
    visit(570, foregrounds);
    visit(571, grayed);
    visit(572, images);
    visit(573, paintedTexts);
    visit(574, texts);
  }
}
