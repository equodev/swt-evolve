import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/scrolledcomposite_evolve.dart';
import 'widgets.dart';

class ScrolledCompositeSwt<V extends VScrolledComposite>
    extends CompositeSwt<V> {
  const ScrolledCompositeSwt({super.key, required super.value});

  @override
  State createState() =>
      ScrolledCompositeImpl<
        ScrolledCompositeSwt<VScrolledComposite>,
        VScrolledComposite
      >();
}

class VScrolledComposite extends VComposite {
  VScrolledComposite() : this.empty();
  VScrolledComposite.empty() {
    swt = "ScrolledComposite";
  }

  bool? alwaysShowScrollBars;
  VControl? content;
  bool? expandHorizontal;
  bool? expandVertical;
  int? minHeight;
  int? minWidth;
  VPoint? origin;

  int? originRequest;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VScrolledComposite) {
      alwaysShowScrollBars = other.alwaysShowScrollBars;
      content = other.content;
      expandHorizontal = other.expandHorizontal;
      expandVertical = other.expandVertical;
      minHeight = other.minHeight;
      minWidth = other.minWidth;
      origin = other.origin;
      originRequest = other.originRequest;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 450: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 451: // content
        content = r.ref<VControl>();
        return true;
      case 452: // expandHorizontal
        expandHorizontal = r.boolean();
        return true;
      case 453: // expandVertical
        expandVertical = r.boolean();
        return true;
      case 454: // minHeight
        minHeight = r.int32();
        return true;
      case 455: // minWidth
        minWidth = r.int32();
        return true;
      case 456: // origin
        origin = r.point();
        return true;
      case 457: // originRequest
        originRequest = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = alwaysShowScrollBars;
      if (v_ != null) {
        w.keyBoolean(450, v_);
      }
    }
    {
      final v_ = content;
      if (v_ != null) {
        w.keyRef(451, v_.id);
      }
    }
    {
      final v_ = expandHorizontal;
      if (v_ != null) {
        w.keyBoolean(452, v_);
      }
    }
    {
      final v_ = expandVertical;
      if (v_ != null) {
        w.keyBoolean(453, v_);
      }
    }
    {
      final v_ = minHeight;
      if (v_ != null) {
        w.keyInt32(454, v_);
      }
    }
    {
      final v_ = minWidth;
      if (v_ != null) {
        w.keyInt32(455, v_);
      }
    }
    {
      final v_ = origin;
      if (v_ != null) {
        w.key(456);
        w.pointValue(v_);
      }
    }
    {
      final v_ = originRequest;
      if (v_ != null) {
        w.keyInt32(457, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(450, alwaysShowScrollBars);
    visit(451, content);
    visit(452, expandHorizontal);
    visit(453, expandVertical);
    visit(454, minHeight);
    visit(455, minWidth);
    visit(456, origin);
    visit(457, originRequest);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    content = VWidget.adoptOne(content, adopt);
  }
}
