import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/link_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class LinkSwt<V extends VLink> extends ControlSwt<V> {
  const LinkSwt({super.key, required super.value});

  @override
  State createState() => LinkImpl<LinkSwt<VLink>, VLink>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }
}

class VLink extends VControl {
  VLink() : this.empty();
  VLink.empty() {
    swt = "Link";
  }

  VColor? linkForeground;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VLink) {
      linkForeground = other.linkForeground;
      text = other.text;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 363: // linkForeground
        linkForeground = r.color();
        return true;
      case 364: // text
        text = r.string();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = linkForeground;
      if (v_ != null) {
        w.key(363);
        w.colorValue(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(364);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(363, linkForeground);
    visit(364, text);
  }

  @override
  String? get semanticsText => text;
}
