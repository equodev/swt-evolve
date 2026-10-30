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
      case 273: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 274: // content
        content = r.ref<VControl>();
        return true;
      case 275: // expandHorizontal
        expandHorizontal = r.boolean();
        return true;
      case 276: // expandVertical
        expandVertical = r.boolean();
        return true;
      case 277: // minHeight
        minHeight = r.int32();
        return true;
      case 278: // minWidth
        minWidth = r.int32();
        return true;
      case 1381: // origin
        origin = r.point();
        return true;
      case 1382: // originRequest
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
      if (v_ != null && w.wants(273)) {
        w.key(273);
        w.boolean(v_);
      }
    }
    {
      final v_ = content;
      if (v_ != null && w.wants(274)) {
        w.key(274);
        w.ref(v_.id);
      }
    }
    {
      final v_ = expandHorizontal;
      if (v_ != null && w.wants(275)) {
        w.key(275);
        w.boolean(v_);
      }
    }
    {
      final v_ = expandVertical;
      if (v_ != null && w.wants(276)) {
        w.key(276);
        w.boolean(v_);
      }
    }
    {
      final v_ = minHeight;
      if (v_ != null && w.wants(277)) {
        w.key(277);
        w.int32(v_);
      }
    }
    {
      final v_ = minWidth;
      if (v_ != null && w.wants(278)) {
        w.key(278);
        w.int32(v_);
      }
    }
    {
      final v_ = origin;
      if (v_ != null && w.wants(1381)) {
        w.key(1381);
        w.pointValue(v_);
      }
    }
    {
      final v_ = originRequest;
      if (v_ != null && w.wants(1382)) {
        w.key(1382);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(273, alwaysShowScrollBars);
    visit(274, content);
    visit(275, expandHorizontal);
    visit(276, expandVertical);
    visit(277, minHeight);
    visit(278, minWidth);
    visit(1381, origin);
    visit(1382, originRequest);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    content = VWidget.adoptOne(content, adopt);
  }
}
