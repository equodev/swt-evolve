import '../impl/config_flags.dart';
import 'menu.dart';
import 'shell.dart';
import 'tooltip.dart';
import '../comm/widget_wire.dart';
import 'widgets.dart' show WireKey;

class VDisplay implements WireReadable {
  String? swt;
  int? id;
  List<VShell>? shells;
  List<VMenu>? popups;
  VMenu? systemMenu;
  List<VToolTip>? tooltips;
  ConfigFlags? config;

  /// The shell SWT considers active, or null/0 when none is. A shell takes keyboard focus only
  /// when it is named here — see [ShellImpl].
  int? activeShellId;

  /// The shell that drives (and is slaved to) the viewport, or null/0 when Java named none. Only
  /// this shell renders full-bleed; every other one is a window with its own chrome.
  int? mainShellId;

  /// The control SWT holds as its focus control, or null/0 when none. Tab starts from it while the
  /// client's own focus is on no control — see [FocusRequests].
  int? focusHolderId;

  /// The shells drawn in a window of their own — a second native window, a second browser window —
  /// each by its own Flutter client rooted at that shell. They are left out of this client's stack;
  /// drawing them here as well would put them on screen twice. See [WindowPolicy] on the Java side.
  List<int>? windowedShellIds;

  VDisplay();

  /// The display's record of a binary frame, read property by property.
  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case WireKey.k_Display_shells:
        shells = r.refs<VShell>();
      case WireKey.k_Display_popups:
        popups = r.refs<VMenu>();
      case WireKey.k_Display_systemMenu:
        systemMenu = r.ref<VMenu>();
      case WireKey.k_Display_tooltips:
        tooltips = r.refs<VToolTip>();
      case WireKey.k_Display_activeShellId:
        activeShellId = r.int64();
      case WireKey.k_Display_mainShellId:
        mainShellId = r.int64();
      case WireKey.k_Display_windowedShellIds:
        windowedShellIds = r.longs();
      case WireKey.k_Display_focusHolderId:
        focusHolderId = r.int64();
      case WireKey.k_Display_config:
        final from = r.pos;
        config = r.object(ConfigFlags.new)?..wire = r.bytesSince(from);
      default:
        return false;
    }
    return true;
  }
}
