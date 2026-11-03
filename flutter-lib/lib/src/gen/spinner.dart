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
      case 462: // digits
        digits = r.int32();
        return true;
      case 463: // increment
        increment = r.int32();
        return true;
      case 465: // maximum
        maximum = r.int32();
        return true;
      case 466: // minimum
        minimum = r.int32();
        return true;
      case 467: // pageIncrement
        pageIncrement = r.int32();
        return true;
      case 468: // selection
        selection = r.int32();
        return true;
      case 469: // textLimit
        textLimit = r.int32();
        return true;
      case 464: // keptText
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
        w.keyInt32(462, v_);
      }
    }
    {
      final v_ = increment;
      if (v_ != null) {
        w.keyInt32(463, v_);
      }
    }
    {
      final v_ = maximum;
      if (v_ != null) {
        w.keyInt32(465, v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null) {
        w.keyInt32(466, v_);
      }
    }
    {
      final v_ = pageIncrement;
      if (v_ != null) {
        w.keyInt32(467, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(468, v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null) {
        w.keyInt32(469, v_);
      }
    }
    {
      final v_ = keptText;
      if (v_ != null) {
        w.key(464);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(462, digits);
    visit(463, increment);
    visit(465, maximum);
    visit(466, minimum);
    visit(467, pageIncrement);
    visit(468, selection);
    visit(469, textLimit);
    visit(464, keptText);
  }
}
