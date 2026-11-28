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
import '../gen/shell.dart';
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
    sendEvent(val, 90, payload); // Shell/Activate
  }

  void sendShellClose(V val, VEvent? payload) {
    sendEvent(val, 91, payload); // Shell/Close
  }

  void sendShellDeactivate(V val, VEvent? payload) {
    sendEvent(val, 92, payload); // Shell/Deactivate
  }

  void sendShellDeiconify(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Shell/Deiconify
  }

  void sendShellIconify(V val, VEvent? payload) {
    sendEvent(val, 94, payload); // Shell/Iconify
  }

  void sendShellSetBounds(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Shell/SetBounds
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

  int? presentation;
  VRectangle? pageHost;
  VShell? pageOwner;
  int? pageStrip;
  bool? pageMaximized;
  int? pageHeading;
  String? pageTitle;
  String? pageMessage;
  int? pageMessageType;
  List<String?>? pageSteps;
  int? pageStep;

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
      presentation = other.presentation;
      pageHost = other.pageHost;
      pageOwner = other.pageOwner;
      pageStrip = other.pageStrip;
      pageMaximized = other.pageMaximized;
      pageHeading = other.pageHeading;
      pageTitle = other.pageTitle;
      pageMessage = other.pageMessage;
      pageMessageType = other.pageMessageType;
      pageSteps = other.pageSteps;
      pageStep = other.pageStep;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 4608: // alpha
        alpha = r.int32();
        return true;
      case 4610: // fullScreen
        fullScreen = r.boolean();
        return true;
      case 4611: // maximumSize
        maximumSize = r.point();
        return true;
      case 4612: // minimumSize
        minimumSize = r.point();
        return true;
      case 4613: // modified
        modified = r.boolean();
        return true;
      case 4609: // dialogs
        dialogs = r.polys<VDialog>(newDialogValue);
        return true;
      case 4614: // presentation
        presentation = r.int32();
        return true;
      case 4615: // pageHost
        pageHost = r.rect();
        return true;
      case 4616: // pageOwner
        pageOwner = r.ref<VShell>();
        return true;
      case 4623: // pageStrip
        pageStrip = r.int32();
        return true;
      case 4618: // pageMaximized
        pageMaximized = r.boolean();
        return true;
      case 4617: // pageHeading
        pageHeading = r.int32();
        return true;
      case 4624: // pageTitle
        pageTitle = r.string();
        return true;
      case 4619: // pageMessage
        pageMessage = r.string();
        return true;
      case 4620: // pageMessageType
        pageMessageType = r.int32();
        return true;
      case 4622: // pageSteps
        pageSteps = r.strings();
        return true;
      case 4621: // pageStep
        pageStep = r.int32();
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
        w.keyInt32(4608, v_);
      }
    }
    {
      final v_ = fullScreen;
      if (v_ != null) {
        w.keyBoolean(4610, v_);
      }
    }
    {
      final v_ = maximumSize;
      if (v_ != null) {
        w.key(4611);
        w.pointValue(v_);
      }
    }
    {
      final v_ = minimumSize;
      if (v_ != null) {
        w.key(4612);
        w.pointValue(v_);
      }
    }
    {
      final v_ = modified;
      if (v_ != null) {
        w.keyBoolean(4613, v_);
      }
    }
    {
      final v_ = dialogs;
      if (v_ != null) {
        w.key(4609);
        w.polys(v_, (e) => e.swt);
      }
    }
    {
      final v_ = presentation;
      if (v_ != null) {
        w.keyInt32(4614, v_);
      }
    }
    {
      final v_ = pageHost;
      if (v_ != null) {
        w.key(4615);
        w.rectValue(v_);
      }
    }
    {
      final v_ = pageOwner;
      if (v_ != null) {
        w.keyRef(4616, v_.id);
      }
    }
    {
      final v_ = pageStrip;
      if (v_ != null) {
        w.keyInt32(4623, v_);
      }
    }
    {
      final v_ = pageMaximized;
      if (v_ != null) {
        w.keyBoolean(4618, v_);
      }
    }
    {
      final v_ = pageHeading;
      if (v_ != null) {
        w.keyInt32(4617, v_);
      }
    }
    {
      final v_ = pageTitle;
      if (v_ != null) {
        w.key(4624);
        w.string(v_);
      }
    }
    {
      final v_ = pageMessage;
      if (v_ != null) {
        w.key(4619);
        w.string(v_);
      }
    }
    {
      final v_ = pageMessageType;
      if (v_ != null) {
        w.keyInt32(4620, v_);
      }
    }
    {
      final v_ = pageSteps;
      if (v_ != null) {
        w.key(4622);
        w.strings(v_);
      }
    }
    {
      final v_ = pageStep;
      if (v_ != null) {
        w.keyInt32(4621, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(4608, alpha);
    visit(4610, fullScreen);
    visit(4611, maximumSize);
    visit(4612, minimumSize);
    visit(4613, modified);
    visit(4609, dialogs);
    visit(4614, presentation);
    visit(4615, pageHost);
    visit(4616, pageOwner);
    visit(4623, pageStrip);
    visit(4618, pageMaximized);
    visit(4617, pageHeading);
    visit(4624, pageTitle);
    visit(4619, pageMessage);
    visit(4620, pageMessageType);
    visit(4622, pageSteps);
    visit(4621, pageStep);
  }
}
