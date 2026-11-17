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
      case 576: // control
        control = r.ref<VControl>();
        return true;
      case 577: // font
        font = r.object(VFont.new);
        return true;
      case 578: // foreground
        foreground = r.color();
        return true;
      case 579: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 580: // showClose
        showClose = r.boolean();
        return true;
      case 583: // showDirty
        showDirty = r.boolean();
        return true;
      case 581: // showing
        showing = r.boolean();
        return true;
      case 582: // toolTipText
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
        w.keyRef(576, v_.id);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(577);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(578);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(579);
        w.colorValue(v_);
      }
    }
    {
      final v_ = showClose;
      if (v_ != null) {
        w.keyBoolean(580, v_);
      }
    }
    {
      final v_ = showDirty;
      if (v_ != null) {
        w.keyBoolean(583, v_);
      }
    }
    {
      final v_ = showing;
      if (v_ != null) {
        w.keyBoolean(581, v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null) {
        w.key(582);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(576, control);
    visit(577, font);
    visit(578, foreground);
    visit(579, selectionForeground);
    visit(580, showClose);
    visit(583, showDirty);
    visit(581, showing);
    visit(582, toolTipText);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }

  @override
  String? get semanticsTooltip => toolTipText;
}
