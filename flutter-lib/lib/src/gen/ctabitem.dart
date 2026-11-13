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
  bool? showDirty;
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
      showDirty = other.showDirty;
      showing = other.showing;
      toolTipText = other.toolTipText;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 66: // control
        control = r.ref<VControl>();
        return true;
      case 67: // font
        font = r.object(VFont.new);
        return true;
      case 68: // foreground
        foreground = r.color();
        return true;
      case 69: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 70: // showClose
        showClose = r.boolean();
        return true;
      case 71: // showDirty
        showDirty = r.boolean();
        return true;
      case 72: // showing
        showing = r.boolean();
        return true;
      case 73: // toolTipText
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
      if (v_ != null) {
        w.keyRef(66, v_.id);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(67);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(68);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(69);
        w.colorValue(v_);
      }
    }
    {
      final v_ = showClose;
      if (v_ != null) {
        w.keyBoolean(70, v_);
      }
    }
    {
      final v_ = showDirty;
      if (v_ != null) {
        w.keyBoolean(71, v_);
      }
    }
    {
      final v_ = showing;
      if (v_ != null) {
        w.keyBoolean(72, v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null) {
        w.key(73);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(66, control);
    visit(67, font);
    visit(68, foreground);
    visit(69, selectionForeground);
    visit(70, showClose);
    visit(71, showDirty);
    visit(72, showing);
    visit(73, toolTipText);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }

  @override
  String? get semanticsTooltip => toolTipText;
}
