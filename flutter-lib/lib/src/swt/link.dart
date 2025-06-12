import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/control.dart';
import '../impl/link_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'link.g.dart';

class LinkSwt<V extends LinkValue> extends ControlSwt<V> {
  const LinkSwt({super.key, required super.value});

  @override
  State createState() => LinkImpl<LinkSwt<LinkValue>, LinkValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class LinkValue extends ControlValue {
  LinkValue() : this.empty();
  LinkValue.empty() {
    swt = "Link";
  }

  String? text;

  factory LinkValue.fromJson(Map<String, dynamic> json) =>
      _$LinkValueFromJson(json);
  Map<String, dynamic> toJson() => _$LinkValueToJson(this);
}
