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
      case 568: // background
        background = r.color();
        return true;
      case 569: // backgrounds
        backgrounds = r.list((r) => r.color());
        return true;
      case 570: // checked
        checked = r.boolean();
        return true;
      case 571: // font
        font = r.object(VFont.new);
        return true;
      case 572: // fonts
        fonts = r.list((r) => r.object(VFont.new));
        return true;
      case 573: // foreground
        foreground = r.color();
        return true;
      case 574: // foregrounds
        foregrounds = r.list((r) => r.color());
        return true;
      case 575: // grayed
        grayed = r.boolean();
        return true;
      case 576: // images
        images = r.list((r) => r.image());
        return true;
      case 577: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 578: // texts
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
        w.key(568);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null) {
        w.key(569);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null) {
        w.keyBoolean(570, v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(571);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null) {
        w.key(572);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(573);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null) {
        w.key(574);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(575, v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null) {
        w.key(576);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null) {
        w.key(577);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null) {
        w.key(578);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(568, background);
    visit(569, backgrounds);
    visit(570, checked);
    visit(571, font);
    visit(572, fonts);
    visit(573, foreground);
    visit(574, foregrounds);
    visit(575, grayed);
    visit(576, images);
    visit(577, paintedTexts);
    visit(578, texts);
  }
}
