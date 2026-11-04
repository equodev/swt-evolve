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
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/label_evolve.dart';
import 'widgets.dart';

class LabelSwt<V extends VLabel> extends ControlSwt<V> {
  const LabelSwt({super.key, required super.value});

  @override
  State createState() => LabelImpl<LabelSwt<VLabel>, VLabel>();
}

class VLabel extends VControl {
  VLabel() : this.empty();
  VLabel.empty() {
    swt = "Label";
  }

  VImage? image;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VLabel) {
      image = other.image;
      text = other.text;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 349: // image
        image = r.image();
        return true;
      case 350: // text
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
      final v_ = image;
      if (v_ != null) {
        w.keyRef(349, v_.id);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(350);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(349, image);
    visit(350, text);
  }

  @override
  String? get semanticsText => text;
}
