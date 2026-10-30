import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/ctabitem_evolve.dart';
import 'widgets.dart';

class CTabItemSwt<V extends VCTabItem> extends ItemSwt<V> {
  const CTabItemSwt({super.key, required super.value});

  @override
  State createState() => CTabItemImpl<CTabItemSwt<VCTabItem>, VCTabItem>();
}

class VCTabItem extends VItem {
  VCTabItem() : this.empty();
  VCTabItem.empty() {
    swt = "CTabItem";
  }

  VControl? control;
  VFont? font;
  VColor? foreground;
  VColor? selectionForeground;
  bool? showClose;
  bool? showing;
  String? toolTipText;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCTabItem) {
      control = other.control;
      font = other.font;
      foreground = other.foreground;
      selectionForeground = other.selectionForeground;
      showClose = other.showClose;
      showing = other.showing;
      toolTipText = other.toolTipText;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 413: // control
        control = r.ref<VControl>();
        return true;
      case 414: // font
        font = r.object(VFont.new);
        return true;
      case 415: // foreground
        foreground = r.color();
        return true;
      case 416: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 417: // showClose
        showClose = r.boolean();
        return true;
      case 418: // showing
        showing = r.boolean();
        return true;
      case 419: // toolTipText
        toolTipText = r.string();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = control;
      if (v_ != null && w.wants(413)) {
        w.key(413);
        w.ref(v_.id);
      }
    }
    {
      final v_ = font;
      if (v_ != null && w.wants(414)) {
        w.key(414);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = foreground;
      if (v_ != null && w.wants(415)) {
        w.key(415);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null && w.wants(416)) {
        w.key(416);
        w.colorValue(v_);
      }
    }
    {
      final v_ = showClose;
      if (v_ != null && w.wants(417)) {
        w.key(417);
        w.boolean(v_);
      }
    }
    {
      final v_ = showing;
      if (v_ != null && w.wants(418)) {
        w.key(418);
        w.boolean(v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null && w.wants(419)) {
        w.key(419);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(413, control);
    visit(414, font);
    visit(415, foreground);
    visit(416, selectionForeground);
    visit(417, showClose);
    visit(418, showing);
    visit(419, toolTipText);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }

  @override
  String? get semanticsTooltip => toolTipText;
}
