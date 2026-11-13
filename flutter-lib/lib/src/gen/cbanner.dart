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
import '../impl/cbanner_evolve.dart';
import 'widgets.dart';

class CBannerSwt<V extends VCBanner> extends CompositeSwt<V> {
  const CBannerSwt({super.key, required super.value});

  @override
  State createState() => CBannerImpl<CBannerSwt<VCBanner>, VCBanner>();
}

class VCBanner extends VComposite {
  VCBanner() : this.empty();
  VCBanner.empty() {
    swt = "CBanner";
  }

  List<int>? curveLine;
  int? bottomLine;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCBanner) {
      curveLine = other.curveLine;
      bottomLine = other.bottomLine;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 19: // curveLine
        curveLine = r.ints();
        return true;
      case 18: // bottomLine
        bottomLine = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = curveLine;
      if (v_ != null) {
        w.key(19);
        w.ints(v_);
      }
    }
    {
      final v_ = bottomLine;
      if (v_ != null) {
        w.keyInt32(18, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(19, curveLine);
    visit(18, bottomLine);
  }
}
