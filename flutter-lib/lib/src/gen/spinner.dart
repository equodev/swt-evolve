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
    sendEvent(val, 72, payload); // Modify/Modify
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, 114, payload); // Verify/Verify
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
      case 470: // digits
        digits = r.int32();
        return true;
      case 471: // increment
        increment = r.int32();
        return true;
      case 473: // maximum
        maximum = r.int32();
        return true;
      case 474: // minimum
        minimum = r.int32();
        return true;
      case 475: // pageIncrement
        pageIncrement = r.int32();
        return true;
      case 476: // selection
        selection = r.int32();
        return true;
      case 477: // textLimit
        textLimit = r.int32();
        return true;
      case 472: // keptText
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
        w.keyInt32(470, v_);
      }
    }
    {
      final v_ = increment;
      if (v_ != null) {
        w.keyInt32(471, v_);
      }
    }
    {
      final v_ = maximum;
      if (v_ != null) {
        w.keyInt32(473, v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null) {
        w.keyInt32(474, v_);
      }
    }
    {
      final v_ = pageIncrement;
      if (v_ != null) {
        w.keyInt32(475, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(476, v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null) {
        w.keyInt32(477, v_);
      }
    }
    {
      final v_ = keptText;
      if (v_ != null) {
        w.key(472);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(470, digits);
    visit(471, increment);
    visit(473, maximum);
    visit(474, minimum);
    visit(475, pageIncrement);
    visit(476, selection);
    visit(477, textLimit);
    visit(472, keptText);
  }
}
