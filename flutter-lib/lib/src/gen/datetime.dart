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
      case 159: // day
        day = r.int32();
        return true;
      case 160: // hours
        hours = r.int32();
        return true;
      case 161: // minutes
        minutes = r.int32();
        return true;
      case 162: // month
        month = r.int32();
        return true;
      case 163: // seconds
        seconds = r.int32();
        return true;
      case 164: // year
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
        w.keyInt32(159, v_);
      }
    }
    {
      final v_ = hours;
      if (v_ != null) {
        w.keyInt32(160, v_);
      }
    }
    {
      final v_ = minutes;
      if (v_ != null) {
        w.keyInt32(161, v_);
      }
    }
    {
      final v_ = month;
      if (v_ != null) {
        w.keyInt32(162, v_);
      }
    }
    {
      final v_ = seconds;
      if (v_ != null) {
        w.keyInt32(163, v_);
      }
    }
    {
      final v_ = year;
      if (v_ != null) {
        w.keyInt32(164, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(159, day);
    visit(160, hours);
    visit(161, minutes);
    visit(162, month);
    visit(163, seconds);
    visit(164, year);
  }
}
