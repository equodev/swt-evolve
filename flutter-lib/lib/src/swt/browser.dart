import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/browser_impl.dart'
    if (dart.library.js_interop) '../impl/browser_impl_web.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'browser.g.dart';

class BrowserSwt<V extends BrowserValue> extends CompositeSwt<V> {
  const BrowserSwt({super.key, required super.value});

  @override
  State createState() => BrowserImpl<BrowserSwt<BrowserValue>, BrowserValue>();
}

@JsonSerializable()
class BrowserValue extends CompositeValue {
  BrowserValue() : this.empty();
  BrowserValue.empty() {
    swt = "Browser";
  }

  bool? javascriptEnabled;
  int style = 0;
  String? text;
  String? url;

  factory BrowserValue.fromJson(Map<String, dynamic> json) =>
      _$BrowserValueFromJson(json);
  Map<String, dynamic> toJson() => _$BrowserValueToJson(this);
}
