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
import '../impl/progressbar_evolve.dart';
import 'widgets.dart';

class ProgressBarSwt<V extends VProgressBar> extends ControlSwt<V> {
  const ProgressBarSwt({super.key, required super.value});

  @override
  State createState() =>
      ProgressBarImpl<ProgressBarSwt<VProgressBar>, VProgressBar>();
}

class VProgressBar extends VControl {
  VProgressBar() : this.empty();
  VProgressBar.empty() {
    swt = "ProgressBar";
  }

  int? maximum;
  int? minimum;
  int? selection;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VProgressBar) {
      maximum = other.maximum;
      minimum = other.minimum;
      selection = other.selection;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 411: // maximum
        maximum = r.int32();
        return true;
      case 412: // minimum
        minimum = r.int32();
        return true;
      case 413: // selection
        selection = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = maximum;
      if (v_ != null) {
        w.keyInt32(411, v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null) {
        w.keyInt32(412, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(413, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(411, maximum);
    visit(412, minimum);
    visit(413, selection);
  }
}
