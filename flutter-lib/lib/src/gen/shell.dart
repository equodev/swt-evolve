import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/decorations.dart';
import '../gen/dialog.dart';
import '../gen/dialogs.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/shell_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class ShellSwt<V extends VShell> extends DecorationsSwt<V> {
  const ShellSwt({super.key, required super.value});

  @override
  State createState() => ShellImpl<ShellSwt<VShell>, VShell>();

  void sendShellActivate(V val, VEvent? payload) {
    sendEvent(val, "Shell/Activate", payload);
  }

  void sendShellClose(V val, VEvent? payload) {
    sendEvent(val, "Shell/Close", payload);
  }

  void sendShellDeactivate(V val, VEvent? payload) {
    sendEvent(val, "Shell/Deactivate", payload);
  }

  void sendShellDeiconify(V val, VEvent? payload) {
    sendEvent(val, "Shell/Deiconify", payload);
  }

  void sendShellIconify(V val, VEvent? payload) {
    sendEvent(val, "Shell/Iconify", payload);
  }

  void sendShellSetBounds(V val, VEvent? payload) {
    sendEvent(val, "Shell/SetBounds", payload);
  }
}

class VShell extends VDecorations {
  VShell() : this.empty();
  VShell.empty() {
    swt = "Shell";
  }

  List<VDialog>? dialogs;
  int? alpha;
  bool? fullScreen;
  VPoint? maximumSize;
  VPoint? minimumSize;
  bool? modified;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VShell) {
      alpha = other.alpha;
      fullScreen = other.fullScreen;
      maximumSize = other.maximumSize;
      minimumSize = other.minimumSize;
      modified = other.modified;
      dialogs = other.dialogs;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 577: // alpha
        alpha = r.int32();
        return true;
      case 578: // fullScreen
        fullScreen = r.boolean();
        return true;
      case 579: // maximumSize
        maximumSize = r.point();
        return true;
      case 580: // minimumSize
        minimumSize = r.point();
        return true;
      case 581: // modified
        modified = r.boolean();
        return true;
      case 582: // dialogs
        dialogs = r.polys<VDialog>(newDialogValue);
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = alpha;
      if (v_ != null && w.wants(577)) {
        w.key(577);
        w.int32(v_);
      }
    }
    {
      final v_ = fullScreen;
      if (v_ != null && w.wants(578)) {
        w.key(578);
        w.boolean(v_);
      }
    }
    {
      final v_ = maximumSize;
      if (v_ != null && w.wants(579)) {
        w.key(579);
        w.pointValue(v_);
      }
    }
    {
      final v_ = minimumSize;
      if (v_ != null && w.wants(580)) {
        w.key(580);
        w.pointValue(v_);
      }
    }
    {
      final v_ = modified;
      if (v_ != null && w.wants(581)) {
        w.key(581);
        w.boolean(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(577, alpha);
    visit(578, fullScreen);
    visit(579, maximumSize);
    visit(580, minimumSize);
    visit(581, modified);
    visit(582, dialogs);
  }
}
