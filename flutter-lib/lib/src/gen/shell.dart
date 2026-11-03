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
    sendEvent(val, 97, payload); // Shell/Activate
  }

  void sendShellClose(V val, VEvent? payload) {
    sendEvent(val, 98, payload); // Shell/Close
  }

  void sendShellDeactivate(V val, VEvent? payload) {
    sendEvent(val, 99, payload); // Shell/Deactivate
  }

  void sendShellDeiconify(V val, VEvent? payload) {
    sendEvent(val, 100, payload); // Shell/Deiconify
  }

  void sendShellIconify(V val, VEvent? payload) {
    sendEvent(val, 101, payload); // Shell/Iconify
  }

  void sendShellSetBounds(V val, VEvent? payload) {
    sendEvent(val, 102, payload); // Shell/SetBounds
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
      case 451: // alpha
        alpha = r.int32();
        return true;
      case 453: // fullScreen
        fullScreen = r.boolean();
        return true;
      case 454: // maximumSize
        maximumSize = r.point();
        return true;
      case 455: // minimumSize
        minimumSize = r.point();
        return true;
      case 456: // modified
        modified = r.boolean();
        return true;
      case 452: // dialogs
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
      if (v_ != null) {
        w.keyInt32(451, v_);
      }
    }
    {
      final v_ = fullScreen;
      if (v_ != null) {
        w.keyBoolean(453, v_);
      }
    }
    {
      final v_ = maximumSize;
      if (v_ != null) {
        w.key(454);
        w.pointValue(v_);
      }
    }
    {
      final v_ = minimumSize;
      if (v_ != null) {
        w.key(455);
        w.pointValue(v_);
      }
    }
    {
      final v_ = modified;
      if (v_ != null) {
        w.keyBoolean(456, v_);
      }
    }
    {
      final v_ = dialogs;
      if (v_ != null) {
        w.key(452);
        w.polys(v_, (e) => e.swt);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(451, alpha);
    visit(453, fullScreen);
    visit(454, maximumSize);
    visit(455, minimumSize);
    visit(456, modified);
    visit(452, dialogs);
  }
}
