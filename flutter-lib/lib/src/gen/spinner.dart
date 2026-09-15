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
import '../impl/spinner_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'spinner.g.dart';

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

@JsonSerializable()
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
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'digits':
        digits = (json['digits'] as num?)?.toInt();
      case 'increment':
        increment = (json['increment'] as num?)?.toInt();
      case 'maximum':
        maximum = (json['maximum'] as num?)?.toInt();
      case 'minimum':
        minimum = (json['minimum'] as num?)?.toInt();
      case 'pageIncrement':
        pageIncrement = (json['pageIncrement'] as num?)?.toInt();
      case 'selection':
        selection = (json['selection'] as num?)?.toInt();
      case 'textLimit':
        textLimit = (json['textLimit'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  factory VSpinner.fromJson(Map<String, dynamic> json) =>
      _$VSpinnerFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VSpinnerToJson(this);
}
