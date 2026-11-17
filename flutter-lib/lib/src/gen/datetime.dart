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
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
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
      case 1472: // day
        day = r.int32();
        return true;
      case 1473: // hours
        hours = r.int32();
        return true;
      case 1474: // minutes
        minutes = r.int32();
        return true;
      case 1475: // month
        month = r.int32();
        return true;
      case 1476: // seconds
        seconds = r.int32();
        return true;
      case 1477: // year
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
        w.keyInt32(1472, v_);
      }
    }
    {
      final v_ = hours;
      if (v_ != null) {
        w.keyInt32(1473, v_);
      }
    }
    {
      final v_ = minutes;
      if (v_ != null) {
        w.keyInt32(1474, v_);
      }
    }
    {
      final v_ = month;
      if (v_ != null) {
        w.keyInt32(1475, v_);
      }
    }
    {
      final v_ = seconds;
      if (v_ != null) {
        w.keyInt32(1476, v_);
      }
    }
    {
      final v_ = year;
      if (v_ != null) {
        w.keyInt32(1477, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(1472, day);
    visit(1473, hours);
    visit(1474, minutes);
    visit(1475, month);
    visit(1476, seconds);
    visit(1477, year);
  }
}
