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
import '../impl/group_evolve.dart';
import 'widgets.dart';

class GroupSwt<V extends VGroup> extends CompositeSwt<V> {
  const GroupSwt({super.key, required super.value});

  @override
  State createState() => GroupImpl<GroupSwt<VGroup>, VGroup>();
}

class VGroup extends VComposite {
  VGroup() : this.empty();
  VGroup.empty() {
    swt = "Group";
  }

  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VGroup) {
      text = other.text;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 175: // text
        text = r.string();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = text;
      if (v_ != null && w.wants(175)) {
        w.key(175);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(175, text);
  }

  @override
  String? get semanticsText => text;
}
