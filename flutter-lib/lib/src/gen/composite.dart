import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollable.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import 'widgets.dart';

class CompositeSwt<V extends VComposite> extends ScrollableSwt<V> {
  const CompositeSwt({super.key, required super.value});

  @override
  State createState() => CompositeImpl<CompositeSwt<VComposite>, VComposite>();
}

class VComposite extends VScrollable {
  VComposite() : this.empty();
  VComposite.empty() {
    swt = "Composite";
  }

  int? backgroundMode;
  List<VControl>? children;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VComposite) {
      backgroundMode = other.backgroundMode;
      children = other.children;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 85: // backgroundMode
        backgroundMode = r.int32();
        return true;
      case 86: // children
        children = r.refs<VControl>();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = backgroundMode;
      if (v_ != null) {
        w.keyInt32(85, v_);
      }
    }
    {
      final v_ = children;
      if (v_ != null) {
        w.key(86);
        w.refs(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(85, backgroundMode);
    visit(86, children);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(children, adopt);
  }
}
