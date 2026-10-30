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
      case 440: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 441: // content
        content = r.ref<VControl>();
        return true;
      case 442: // expandHorizontal
        expandHorizontal = r.boolean();
        return true;
      case 443: // expandVertical
        expandVertical = r.boolean();
        return true;
      case 444: // minHeight
        minHeight = r.int32();
        return true;
      case 445: // minWidth
        minWidth = r.int32();
        return true;
      case 446: // origin
        origin = r.point();
        return true;
      case 447: // originRequest
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
        w.keyBoolean(440, v_);
      }
    }
    {
      final v_ = content;
      if (v_ != null) {
        w.keyRef(441, v_.id);
      }
    }
    {
      final v_ = expandHorizontal;
      if (v_ != null) {
        w.keyBoolean(442, v_);
      }
    }
    {
      final v_ = expandVertical;
      if (v_ != null) {
        w.keyBoolean(443, v_);
      }
    }
    {
      final v_ = minHeight;
      if (v_ != null) {
        w.keyInt32(444, v_);
      }
    }
    {
      final v_ = minWidth;
      if (v_ != null) {
        w.keyInt32(445, v_);
      }
    }
    {
      final v_ = origin;
      if (v_ != null) {
        w.key(446);
        w.pointValue(v_);
      }
    }
    {
      final v_ = originRequest;
      if (v_ != null) {
        w.keyInt32(447, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(440, alwaysShowScrollBars);
    visit(441, content);
    visit(442, expandHorizontal);
    visit(443, expandVertical);
    visit(444, minHeight);
    visit(445, minWidth);
    visit(446, origin);
    visit(447, originRequest);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    content = VWidget.adoptOne(content, adopt);
  }
}
