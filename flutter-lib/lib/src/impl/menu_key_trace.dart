import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import '../gen/widget.dart';
import 'menu_evolve.dart';

/// An opt-in trace of what happens to each key while a pop-up menu is up or opening, and of every
/// focus change in that time: a key a menu should take can otherwise vanish without a single event
/// on either side. Off unless the client is launched with it (`getTraceMenuKeys`).
///
/// One `[menu-keys] <epoch ms> ...` line per decision, on the client's stdout.
class MenuKeyTrace {
  MenuKeyTrace._();

  static bool _enabled = false;

  static bool get enabled => _enabled;

  static void enable() {
    if (_enabled) return;
    _enabled = true;
    FocusManager.instance.addListener(_focusChanged);
  }

  static void log(String Function() line) {
    if (!_enabled) return;
    print('[menu-keys] ${DateTime.now().millisecondsSinceEpoch} ${line()} ${_state()}');
  }

  static String key(KeyEvent event) {
    final kind = event is KeyDownEvent ? 'down' : event is KeyUpEvent ? 'up' : 'repeat';
    return '$kind ${event.logicalKey.keyLabel.isEmpty ? event.logicalKey.debugName : event.logicalKey.keyLabel}';
  }

  static FocusNode? _lastFocus;

  static void _focusChanged() {
    final focus = FocusManager.instance.primaryFocus;
    if (identical(focus, _lastFocus)) return;
    _lastFocus = focus;
    if (OpenMenus.any || OpenMenus.holding) log(() => 'focus moved');
  }

  static String _state() {
    final focus = FocusManager.instance.primaryFocus;
    final inMenu = focus?.context?.findAncestorStateOfType<MenuImpl>() != null;
    return '[open=${OpenMenus.any} opening=${OpenMenus.opening} holding=${OpenMenus.holding}'
        ' focus=${_describe(focus)} inMenu=$inMenu]';
  }

  /// The focus by the SWT widget that owns it: a release build drops every `debugLabel`.
  static String _describe(FocusNode? focus) {
    if (focus == null) return 'none';
    final context = focus.context;
    final owner = context?.findAncestorStateOfType<WidgetSwtState>();
    final swt = owner == null ? 'no widget' : '${owner.state.swt}/${owner.state.id}';
    return '${focus.runtimeType}:${context?.widget.runtimeType}@$swt';
  }
}
