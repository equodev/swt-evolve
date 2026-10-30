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
import '../impl/browser_evolve.dart';
import 'event.dart';
import 'widgets.dart';

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
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 421: // javascriptEnabled
        javascriptEnabled = r.boolean();
        return true;
      case 422: // text
        text = r.string();
        return true;
      case 423: // url
        url = r.string();
        return true;
      case 420: // functionNames
        functionNames = r.strings()?.map((e) => e ?? '').toList();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = javascriptEnabled;
      if (v_ != null && w.wants(421)) {
        w.key(421);
        w.boolean(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null && w.wants(422)) {
        w.key(422);
        w.string(v_);
      }
    }
    {
      final v_ = url;
      if (v_ != null && w.wants(423)) {
        w.key(423);
        w.string(v_);
      }
    }
    {
      final v_ = functionNames;
      if (v_ != null && w.wants(420)) {
        w.key(420);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(421, javascriptEnabled);
    visit(422, text);
    visit(423, url);
    visit(420, functionNames);
  }

  @override
  String? get semanticsText => text;
}
