import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/control.dart';
import '../impl/sash_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'sash.g.dart';

class SashSwt<V extends SashValue> extends ControlSwt<V> {
  const SashSwt({super.key, required super.value});

  @override
  State createState() => SashImpl<SashSwt<SashValue>, SashValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class SashValue extends ControlValue {
  SashValue() : this.empty();
  SashValue.empty() {
    swt = "Sash";
  }

  factory SashValue.fromJson(Map<String, dynamic> json) =>
      _$SashValueFromJson(json);
  Map<String, dynamic> toJson() => _$SashValueToJson(this);
}
