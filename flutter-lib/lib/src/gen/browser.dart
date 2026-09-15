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
import '../impl/browser_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'browser.g.dart';

class BrowserSwt<V extends VBrowser> extends CompositeSwt<V> {
  const BrowserSwt({super.key, required super.value});

  @override
  State createState() => BrowserImpl<BrowserSwt<VBrowser>, VBrowser>();

  void sendAuthenticationauthenticate(V val, VEvent? payload) {
    sendEvent(val, "Authentication/authenticate", payload);
  }

  void sendCloseWindowclose(V val, VEvent? payload) {
    sendEvent(val, "CloseWindow/close", payload);
  }

  void sendLocationchanged(V val, VEvent? payload) {
    sendEvent(val, "Location/changed", payload);
  }

  void sendLocationchanging(V val, VEvent? payload) {
    sendEvent(val, "Location/changing", payload);
  }

  void sendOpenWindowopen(V val, VEvent? payload) {
    sendEvent(val, "OpenWindow/open", payload);
  }

  void sendProgresschanged(V val, VEvent? payload) {
    sendEvent(val, "Progress/changed", payload);
  }

  void sendProgresscompleted(V val, VEvent? payload) {
    sendEvent(val, "Progress/completed", payload);
  }

  void sendStatusTextchanged(V val, VEvent? payload) {
    sendEvent(val, "StatusText/changed", payload);
  }

  void sendTitlechanged(V val, VEvent? payload) {
    sendEvent(val, "Title/changed", payload);
  }

  void sendVisibilityWindowhide(V val, VEvent? payload) {
    sendEvent(val, "VisibilityWindow/hide", payload);
  }

  void sendVisibilityWindowshow(V val, VEvent? payload) {
    sendEvent(val, "VisibilityWindow/show", payload);
  }
}

@JsonSerializable()
class VBrowser extends VComposite {
  VBrowser() : this.empty();
  VBrowser.empty() {
    swt = "Browser";
  }

  List<String>? functionNames;
  bool? javascriptEnabled;
  String? text;
  String? url;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VBrowser) {
      javascriptEnabled = other.javascriptEnabled;
      text = other.text;
      url = other.url;
      functionNames = other.functionNames;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'javascriptEnabled':
        javascriptEnabled = json['javascriptEnabled'] as bool?;
      case 'text':
        text = json['text'] as String?;
      case 'url':
        url = json['url'] as String?;
      case 'functionNames':
        functionNames = (json['functionNames'] as List<dynamic>?)
            ?.map((e) => e as String)
            .toList();
      default:
        super.readProperty(key, json);
    }
  }

  factory VBrowser.fromJson(Map<String, dynamic> json) =>
      _$VBrowserFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VBrowserToJson(this);
}
