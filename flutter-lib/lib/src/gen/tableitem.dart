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
      case 113: // background
        background = r.color();
        return true;
      case 114: // backgrounds
        backgrounds = r.list(() => r.color());
        return true;
      case 115: // checked
        checked = r.boolean();
        return true;
      case 116: // font
        font = r.object(VFont.new);
        return true;
      case 117: // fonts
        fonts = r.list(() => r.object(VFont.new));
        return true;
      case 118: // foreground
        foreground = r.color();
        return true;
      case 119: // foregrounds
        foregrounds = r.list(() => r.color());
        return true;
      case 120: // grayed
        grayed = r.boolean();
        return true;
      case 121: // images
        images = r.list(() => r.image());
        return true;
      case 122: // paintedTexts
        paintedTexts = r.ints();
        return true;
      case 123: // texts
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
      if (v_ != null && w.wants(113)) {
        w.key(113);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgrounds;
      if (v_ != null && w.wants(114)) {
        w.key(114);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = checked;
      if (v_ != null && w.wants(115)) {
        w.key(115);
        w.boolean(v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null && w.wants(116)) {
        w.key(116);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = fonts;
      if (v_ != null && w.wants(117)) {
        w.key(117);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = foreground;
      if (v_ != null && w.wants(118)) {
        w.key(118);
        w.colorValue(v_);
      }
    }
    {
      final v_ = foregrounds;
      if (v_ != null && w.wants(119)) {
        w.key(119);
        w.list(v_, (e) => w.colorValue(e));
      }
    }
    {
      final v_ = grayed;
      if (v_ != null && w.wants(120)) {
        w.key(120);
        w.boolean(v_);
      }
    }
    {
      final v_ = images;
      if (v_ != null && w.wants(121)) {
        w.key(121);
        w.list(v_, (e) => w.ref(e?.id));
      }
    }
    {
      final v_ = paintedTexts;
      if (v_ != null && w.wants(122)) {
        w.key(122);
        w.ints(v_);
      }
    }
    {
      final v_ = texts;
      if (v_ != null && w.wants(123)) {
        w.key(123);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(113, background);
    visit(114, backgrounds);
    visit(115, checked);
    visit(116, font);
    visit(117, fonts);
    visit(118, foreground);
    visit(119, foregrounds);
    visit(120, grayed);
    visit(121, images);
    visit(122, paintedTexts);
    visit(123, texts);
  }
}
