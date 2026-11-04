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
      case 445: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 446: // content
        content = r.ref<VControl>();
        return true;
      case 447: // expandHorizontal
        expandHorizontal = r.boolean();
        return true;
      case 448: // expandVertical
        expandVertical = r.boolean();
        return true;
      case 449: // minHeight
        minHeight = r.int32();
        return true;
      case 450: // minWidth
        minWidth = r.int32();
        return true;
      case 451: // origin
        origin = r.point();
        return true;
      case 452: // originRequest
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
        w.keyBoolean(445, v_);
      }
    }
    {
      final v_ = content;
      if (v_ != null) {
        w.keyRef(446, v_.id);
      }
    }
    {
      final v_ = expandHorizontal;
      if (v_ != null) {
        w.keyBoolean(447, v_);
      }
    }
    {
      final v_ = expandVertical;
      if (v_ != null) {
        w.keyBoolean(448, v_);
      }
    }
    {
      final v_ = minHeight;
      if (v_ != null) {
        w.keyInt32(449, v_);
      }
    }
    {
      final v_ = minWidth;
      if (v_ != null) {
        w.keyInt32(450, v_);
      }
    }
    {
      final v_ = origin;
      if (v_ != null) {
        w.key(451);
        w.pointValue(v_);
      }
    }
    {
      final v_ = originRequest;
      if (v_ != null) {
        w.keyInt32(452, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(445, alwaysShowScrollBars);
    visit(446, content);
    visit(447, expandHorizontal);
    visit(448, expandVertical);
    visit(449, minHeight);
    visit(450, minWidth);
    visit(451, origin);
    visit(452, originRequest);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    content = VWidget.adoptOne(content, adopt);
  }
}
