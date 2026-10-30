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
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
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
      case 527: // alignment
        alignment = r.int32();
        return true;
      case 528: // grayed
        grayed = r.boolean();
        return true;
      case 529: // image
        image = r.image();
        return true;
      case 530: // primary
        primary = r.boolean();
        return true;
      case 531: // selection
        selection = r.boolean();
        return true;
      case 532: // text
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
      if (v_ != null && w.wants(527)) {
        w.key(527);
        w.int32(v_);
      }
    }
    {
      final v_ = grayed;
      if (v_ != null && w.wants(528)) {
        w.key(528);
        w.boolean(v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null && w.wants(529)) {
        w.key(529);
        w.ref(v_.id);
      }
    }
    {
      final v_ = primary;
      if (v_ != null && w.wants(530)) {
        w.key(530);
        w.boolean(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(531)) {
        w.key(531);
        w.boolean(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null && w.wants(532)) {
        w.key(532);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(527, alignment);
    visit(528, grayed);
    visit(529, image);
    visit(530, primary);
    visit(531, selection);
    visit(532, text);
  }

  @override
  String? get semanticsText => text;
}
