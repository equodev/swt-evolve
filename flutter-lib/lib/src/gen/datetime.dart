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
import '../impl/datetime_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class DateTimeSwt<V extends VDateTime> extends CompositeSwt<V> {
  const DateTimeSwt({super.key, required super.value});

  @override
  State createState() => DateTimeImpl<DateTimeSwt<VDateTime>, VDateTime>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }
}

class VDateTime extends VComposite {
  VDateTime() : this.empty();
  VDateTime.empty() {
    swt = "DateTime";
  }

  int? day;
  int? hours;
  int? minutes;
  int? month;
  int? seconds;
  int? year;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VDateTime) {
      day = other.day;
      hours = other.hours;
      minutes = other.minutes;
      month = other.month;
      seconds = other.seconds;
      year = other.year;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 167: // day
        day = r.int32();
        return true;
      case 168: // hours
        hours = r.int32();
        return true;
      case 169: // minutes
        minutes = r.int32();
        return true;
      case 170: // month
        month = r.int32();
        return true;
      case 171: // seconds
        seconds = r.int32();
        return true;
      case 172: // year
        year = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = day;
      if (v_ != null) {
        w.keyInt32(167, v_);
      }
    }
    {
      final v_ = hours;
      if (v_ != null) {
        w.keyInt32(168, v_);
      }
    }
    {
      final v_ = minutes;
      if (v_ != null) {
        w.keyInt32(169, v_);
      }
    }
    {
      final v_ = month;
      if (v_ != null) {
        w.keyInt32(170, v_);
      }
    }
    {
      final v_ = seconds;
      if (v_ != null) {
        w.keyInt32(171, v_);
      }
    }
    {
      final v_ = year;
      if (v_ != null) {
        w.keyInt32(172, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(167, day);
    visit(168, hours);
    visit(169, minutes);
    visit(170, month);
    visit(171, seconds);
    visit(172, year);
  }
}
