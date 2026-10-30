import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/decorations_evolve.dart';
import 'widgets.dart';

class DecorationsSwt<V extends VDecorations> extends CanvasSwt<V> {
  const DecorationsSwt({super.key, required super.value});

  @override
  State createState() =>
      DecorationsImpl<DecorationsSwt<VDecorations>, VDecorations>();
}

class VDecorations extends VCanvas {
  VDecorations() : this.empty();
  VDecorations.empty() {
    swt = "Decorations";
  }

  VMenu? menuBar;
  String? text;

  int? menuStripHeight;
  bool? ownMenuStrip;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VDecorations) {
      menuBar = other.menuBar;
      text = other.text;
      menuStripHeight = other.menuStripHeight;
      ownMenuStrip = other.ownMenuStrip;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 163: // menuBar
        menuBar = r.ref<VMenu>();
        return true;
      case 166: // text
        text = r.string();
        return true;
      case 164: // menuStripHeight
        menuStripHeight = r.int32();
        return true;
      case 165: // ownMenuStrip
        ownMenuStrip = r.boolean();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = menuBar;
      if (v_ != null) {
        w.keyRef(163, v_.id);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(166);
        w.string(v_);
      }
    }
    {
      final v_ = menuStripHeight;
      if (v_ != null) {
        w.keyInt32(164, v_);
      }
    }
    {
      final v_ = ownMenuStrip;
      if (v_ != null) {
        w.keyBoolean(165, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(163, menuBar);
    visit(166, text);
    visit(164, menuStripHeight);
    visit(165, ownMenuStrip);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    menuBar = VWidget.adoptOne(menuBar, adopt);
  }

  @override
  String? get semanticsText => text;
}
