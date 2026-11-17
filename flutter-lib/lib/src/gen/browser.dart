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
    sendEvent(val, 3, payload); // Authentication/authenticate
  }

  void sendCloseWindowclose(V val, VEvent? payload) {
    sendEvent(val, 19, payload); // CloseWindow/close
  }

  void sendLocationchanged(V val, VEvent? payload) {
    sendEvent(val, 59, payload); // Location/changed
  }

  void sendLocationchanging(V val, VEvent? payload) {
    sendEvent(val, 60, payload); // Location/changing
  }

  void sendOpenWindowopen(V val, VEvent? payload) {
    sendEvent(val, 75, payload); // OpenWindow/open
  }

  void sendProgresschanged(V val, VEvent? payload) {
    sendEvent(val, 80, payload); // Progress/changed
  }

  void sendProgresscompleted(V val, VEvent? payload) {
    sendEvent(val, 81, payload); // Progress/completed
  }

  void sendStatusTextchanged(V val, VEvent? payload) {
    sendEvent(val, 96, payload); // StatusText/changed
  }

  void sendTitlechanged(V val, VEvent? payload) {
    sendEvent(val, 99, payload); // Title/changed
  }

  void sendVisibilityWindowhide(V val, VEvent? payload) {
    sendEvent(val, 109, payload); // VisibilityWindow/hide
  }

  void sendVisibilityWindowshow(V val, VEvent? payload) {
    sendEvent(val, 110, payload); // VisibilityWindow/show
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
      case 193: // javascriptEnabled
        javascriptEnabled = r.boolean();
        return true;
      case 194: // text
        text = r.string();
        return true;
      case 195: // url
        url = r.string();
        return true;
      case 192: // functionNames
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
      if (v_ != null) {
        w.keyBoolean(193, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(194);
        w.string(v_);
      }
    }
    {
      final v_ = url;
      if (v_ != null) {
        w.key(195);
        w.string(v_);
      }
    }
    {
      final v_ = functionNames;
      if (v_ != null) {
        w.key(192);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(193, javascriptEnabled);
    visit(194, text);
    visit(195, url);
    visit(192, functionNames);
  }

  @override
  String? get semanticsText => text;
}
