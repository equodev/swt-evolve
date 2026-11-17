import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
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
import '../impl/button_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class ButtonSwt<V extends VButton> extends ControlSwt<V> {
  const ButtonSwt({super.key, required super.value});

  @override
  State createState() => ButtonImpl<ButtonSwt<VButton>, VButton>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
  }
}

class VButton extends VControl {
  VButton() : this.empty();
  VButton.empty() {
    swt = "Button";
  }

  int? alignment;
  bool? grayed;
  VImage? image;
  bool? primary;
  bool? selection;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VButton) {
      alignment = other.alignment;
      grayed = other.grayed;
      image = other.image;
      primary = other.primary;
      selection = other.selection;
      text = other.text;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 320: // alignment
        alignment = r.int32();
        return true;
      case 321: // grayed
        grayed = r.boolean();
        return true;
      case 322: // image
        image = r.image();
        return true;
      case 323: // primary
        primary = r.boolean();
        return true;
      case 324: // selection
        selection = r.boolean();
        return true;
      case 325: // text
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
      final v_ = alignment;
      if (v_ != null) {
        w.keyInt32(320, v_);
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(321, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(322, v_.id);
      }
    }
    {
      final v_ = primary;
      if (v_ != null) {
        w.keyBoolean(323, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyBoolean(324, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(325);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(320, alignment);
    visit(321, grayed);
    visit(322, image);
    visit(323, primary);
    visit(324, selection);
    visit(325, text);
  }

  @override
  String? get semanticsText => text;
}
