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
      case 446: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 447: // content
        content = r.ref<VControl>();
        return true;
      case 448: // expandHorizontal
        expandHorizontal = r.boolean();
        return true;
      case 449: // expandVertical
        expandVertical = r.boolean();
        return true;
      case 450: // minHeight
        minHeight = r.int32();
        return true;
      case 451: // minWidth
        minWidth = r.int32();
        return true;
      case 452: // origin
        origin = r.point();
        return true;
      case 453: // originRequest
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
        w.keyBoolean(446, v_);
      }
    }
    {
      final v_ = content;
      if (v_ != null) {
        w.keyRef(447, v_.id);
      }
    }
    {
      final v_ = expandHorizontal;
      if (v_ != null) {
        w.keyBoolean(448, v_);
      }
    }
    {
      final v_ = expandVertical;
      if (v_ != null) {
        w.keyBoolean(449, v_);
      }
    }
    {
      final v_ = minHeight;
      if (v_ != null) {
        w.keyInt32(450, v_);
      }
    }
    {
      final v_ = minWidth;
      if (v_ != null) {
        w.keyInt32(451, v_);
      }
    }
    {
      final v_ = origin;
      if (v_ != null) {
        w.key(452);
        w.pointValue(v_);
      }
    }
    {
      final v_ = originRequest;
      if (v_ != null) {
        w.keyInt32(453, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(446, alwaysShowScrollBars);
    visit(447, content);
    visit(448, expandHorizontal);
    visit(449, expandVertical);
    visit(450, minHeight);
    visit(451, minWidth);
    visit(452, origin);
    visit(453, originRequest);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    content = VWidget.adoptOne(content, adopt);
  }
}
