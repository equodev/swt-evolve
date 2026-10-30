import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
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
import '../impl/spinner_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class SpinnerSwt<V extends VSpinner> extends CompositeSwt<V> {
  const SpinnerSwt({super.key, required super.value});

  @override
  State createState() => SpinnerImpl<SpinnerSwt<VSpinner>, VSpinner>();

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, 65, payload); // Modify/Modify
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, 107, payload); // Verify/Verify
  }
}

class VSpinner extends VComposite {
  VSpinner() : this.empty();
  VSpinner.empty() {
    swt = "Spinner";
  }

  int? digits;
  int? increment;
  int? maximum;
  int? minimum;
  int? pageIncrement;
  int? selection;
  int? textLimit;

  String? keptText;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VSpinner) {
      digits = other.digits;
      increment = other.increment;
      maximum = other.maximum;
      minimum = other.minimum;
      pageIncrement = other.pageIncrement;
      selection = other.selection;
      textLimit = other.textLimit;
      keptText = other.keptText;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 460: // digits
        digits = r.int32();
        return true;
      case 461: // increment
        increment = r.int32();
        return true;
      case 463: // maximum
        maximum = r.int32();
        return true;
      case 464: // minimum
        minimum = r.int32();
        return true;
      case 465: // pageIncrement
        pageIncrement = r.int32();
        return true;
      case 466: // selection
        selection = r.int32();
        return true;
      case 467: // textLimit
        textLimit = r.int32();
        return true;
      case 462: // keptText
        keptText = r.string();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = digits;
      if (v_ != null) {
        w.keyInt32(460, v_);
      }
    }
    {
      final v_ = increment;
      if (v_ != null) {
        w.keyInt32(461, v_);
      }
    }
    {
      final v_ = maximum;
      if (v_ != null) {
        w.keyInt32(463, v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null) {
        w.keyInt32(464, v_);
      }
    }
    {
      final v_ = pageIncrement;
      if (v_ != null) {
        w.keyInt32(465, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(466, v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null) {
        w.keyInt32(467, v_);
      }
    }
    {
      final v_ = keptText;
      if (v_ != null) {
        w.key(462);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(460, digits);
    visit(461, increment);
    visit(463, maximum);
    visit(464, minimum);
    visit(465, pageIncrement);
    visit(466, selection);
    visit(467, textLimit);
    visit(462, keptText);
  }
}
