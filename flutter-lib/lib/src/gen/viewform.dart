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
import '../impl/viewform_evolve.dart';
import 'widgets.dart';

class ViewFormSwt<V extends VViewForm> extends CompositeSwt<V> {
  const ViewFormSwt({super.key, required super.value});

  @override
  State createState() => ViewFormImpl<ViewFormSwt<VViewForm>, VViewForm>();
}

class VViewForm extends VComposite {
  VViewForm() : this.empty();
  VViewForm.empty() {
    swt = "ViewForm";
  }

  bool? borderVisible;

  int? highlight;
  int? separator;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VViewForm) {
      borderVisible = other.borderVisible;
      highlight = other.highlight;
      separator = other.separator;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 6336: // borderVisible
        borderVisible = r.boolean();
        return true;
      case 6337: // highlight
        highlight = r.int32();
        return true;
      case 6338: // separator
        separator = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = borderVisible;
      if (v_ != null) {
        w.keyBoolean(6336, v_);
      }
    }
    {
      final v_ = highlight;
      if (v_ != null) {
        w.keyInt32(6337, v_);
      }
    }
    {
      final v_ = separator;
      if (v_ != null) {
        w.keyInt32(6338, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(6336, borderVisible);
    visit(6337, highlight);
    visit(6338, separator);
  }
}
