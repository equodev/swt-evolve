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
    sendEvent(val, "Modify/Modify", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, "Verify/Verify", payload);
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
      case 61: // digits
        digits = r.int32();
        return true;
      case 62: // increment
        increment = r.int32();
        return true;
      case 63: // maximum
        maximum = r.int32();
        return true;
      case 64: // minimum
        minimum = r.int32();
        return true;
      case 65: // pageIncrement
        pageIncrement = r.int32();
        return true;
      case 66: // selection
        selection = r.int32();
        return true;
      case 67: // textLimit
        textLimit = r.int32();
        return true;
      case 1378: // keptText
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
      if (v_ != null && w.wants(61)) {
        w.key(61);
        w.int32(v_);
      }
    }
    {
      final v_ = increment;
      if (v_ != null && w.wants(62)) {
        w.key(62);
        w.int32(v_);
      }
    }
    {
      final v_ = maximum;
      if (v_ != null && w.wants(63)) {
        w.key(63);
        w.int32(v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null && w.wants(64)) {
        w.key(64);
        w.int32(v_);
      }
    }
    {
      final v_ = pageIncrement;
      if (v_ != null && w.wants(65)) {
        w.key(65);
        w.int32(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(66)) {
        w.key(66);
        w.int32(v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null && w.wants(67)) {
        w.key(67);
        w.int32(v_);
      }
    }
    {
      final v_ = keptText;
      if (v_ != null && w.wants(1378)) {
        w.key(1378);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(61, digits);
    visit(62, increment);
    visit(63, maximum);
    visit(64, minimum);
    visit(65, pageIncrement);
    visit(66, selection);
    visit(67, textLimit);
    visit(1378, keptText);
  }
}
