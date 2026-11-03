import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
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
import '../impl/sashform_evolve.dart';
import 'widgets.dart';

class SashFormSwt<V extends VSashForm> extends CompositeSwt<V> {
  const SashFormSwt({super.key, required super.value});

  @override
  State createState() => SashFormImpl<SashFormSwt<VSashForm>, VSashForm>();
}

class VSashForm extends VComposite {
  VSashForm() : this.empty();
  VSashForm.empty() {
    swt = "SashForm";
  }

  VControl? maximizedControl;
  int? sashWidth;
  List<int>? weights;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VSashForm) {
      maximizedControl = other.maximizedControl;
      sashWidth = other.sashWidth;
      weights = other.weights;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 428: // maximizedControl
        maximizedControl = r.ref<VControl>();
        return true;
      case 429: // sashWidth
        sashWidth = r.int32();
        return true;
      case 430: // weights
        weights = r.ints();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = maximizedControl;
      if (v_ != null) {
        w.keyRef(428, v_.id);
      }
    }
    {
      final v_ = sashWidth;
      if (v_ != null) {
        w.keyInt32(429, v_);
      }
    }
    {
      final v_ = weights;
      if (v_ != null) {
        w.key(430);
        w.ints(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(428, maximizedControl);
    visit(429, sashWidth);
    visit(430, weights);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    maximizedControl = VWidget.adoptOne(maximizedControl, adopt);
  }
}
