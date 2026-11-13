import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
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
import '../impl/canvas_evolve.dart';
import 'widgets.dart';

class CanvasSwt<V extends VCanvas> extends CompositeSwt<V> {
  const CanvasSwt({super.key, required super.value});

  @override
  State createState() => CanvasImpl<CanvasSwt<VCanvas>, VCanvas>();
}

class VCanvas extends VComposite {
  VCanvas() : this.empty();
  VCanvas.empty() {
    swt = "Canvas";
  }

  VCaret? caret;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCanvas) {
      caret = other.caret;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 74: // caret
        caret = r.ref<VCaret>();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = caret;
      if (v_ != null) {
        w.keyRef(74, v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(74, caret);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    caret = VWidget.adoptOne(caret, adopt);
  }
}
