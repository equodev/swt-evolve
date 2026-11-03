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
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
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
      case 12: // alignment
        alignment = r.int32();
        return true;
      case 13: // grayed
        grayed = r.boolean();
        return true;
      case 14: // image
        image = r.image();
        return true;
      case 15: // primary
        primary = r.boolean();
        return true;
      case 16: // selection
        selection = r.boolean();
        return true;
      case 17: // text
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
        w.keyInt32(12, v_);
      }
    }
    {
      final v_ = grayed;
      if (v_ != null) {
        w.keyBoolean(13, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(14, v_.id);
      }
    }
    {
      final v_ = primary;
      if (v_ != null) {
        w.keyBoolean(15, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyBoolean(16, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(17);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(12, alignment);
    visit(13, grayed);
    visit(14, image);
    visit(15, primary);
    visit(16, selection);
    visit(17, text);
  }

  @override
  String? get semanticsText => text;
}
