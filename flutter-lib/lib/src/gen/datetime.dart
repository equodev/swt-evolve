import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
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
import '../impl/datetime_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'datetime.g.dart';

class DateTimeSwt<V extends VDateTime> extends CompositeSwt<V> {
  const DateTimeSwt({super.key, required super.value});

  @override
  State createState() => DateTimeImpl<DateTimeSwt<VDateTime>, VDateTime>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
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
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'day':
        day = (json['day'] as num?)?.toInt();
      case 'hours':
        hours = (json['hours'] as num?)?.toInt();
      case 'minutes':
        minutes = (json['minutes'] as num?)?.toInt();
      case 'month':
        month = (json['month'] as num?)?.toInt();
      case 'seconds':
        seconds = (json['seconds'] as num?)?.toInt();
      case 'year':
        year = (json['year'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  factory VDateTime.fromJson(Map<String, dynamic> json) =>
      _$VDateTimeFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VDateTimeToJson(this);
}
