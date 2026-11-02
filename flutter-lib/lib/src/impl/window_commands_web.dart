/// Opens and drives the extra browser windows Java asks for — one per shell it put in a window of
/// its own (see `WindowPolicy` on the Java side).
///
/// Only a page can open a window, so on the web the Display's own client is what does it. Each
/// window loads the same application with `?widgetName=Shell&widgetId=N`, which boots a client
/// rooted at that shell rather than at the Display; it connects back to the same comm, so the
/// detached shell is still an ordinary shell of the same `Display`.
///
/// A browser may refuse outright (a popup blocker, with no gesture to attribute the open to). That
/// is reported back rather than swallowed: Java then draws the shell inside this window again,
/// which is the only alternative to it existing nowhere at all.
library;

import 'dart:async';
import 'dart:js_interop';

import '../comm/comm.dart';
import '../comm/widget_wire.dart' show WidgetWireWriter;
import '../gen/widgets.dart' show WireChannel, WireType;

@JS('window.open')
external JSObject? _open(JSString url, JSString target, JSString features);

@JS('window.screen.availWidth')
external JSNumber? get _availWidth;

@JS('window.screen.availHeight')
external JSNumber? get _availHeight;

/// The handful of window members this needs. Same-origin, so they are all reachable.
extension type _BrowserWindow(JSObject _) implements JSObject {
  external bool get closed;
  external void close();
  external void moveTo(int x, int y);
  external void resizeTo(int width, int height);
  external void focus();
  external _BrowserDocument get document;
  external void addEventListener(String type, JSFunction listener);
}

extension type _BrowserDocument(JSObject _) implements JSObject {
  external set title(String value);
}

/// Each window's title, held so it can be set again once the window's page has loaded its own.
final Map<int, String> _titles = <int, String>{};

final Map<int, _BrowserWindow> _windows = <int, _BrowserWindow>{};

/// How often a window is checked for having been closed by the user. There is no cross-window
/// close event to subscribe to from here, and the closing page's own `pagehide` is the reliable
/// signal — this only catches a window that went without one.
const Duration _closePollInterval = Duration(seconds: 1);

Timer? _closePoll;
int? _displayId;
final List<Object> _tokens = <Object>[];

Map<int, Object> get openedWindows => Map.unmodifiable(_windows);

const _commands = [
  (WireChannel.openWindow, _onOpenWindow),
  (WireChannel.closeWindow, _onCloseWindow),
  (WireChannel.windowBounds, _onWindowBounds),
  (WireChannel.windowState, _onWindowState),
  (WireChannel.windowTitle, _onWindowTitle),
];

void registerWindowCommands(int displayId) {
  _displayId = displayId;
  for (final (channel, handler) in _commands) {
    _tokens.add(EquoCommService.on(WireType.display, displayId, channel, handler));
  }
}

void disposeWindowCommands() {
  _closePoll?.cancel();
  _closePoll = null;
  final displayId = _displayId;
  if (displayId != null) {
    for (final (channel, _) in _commands) {
      EquoCommService.remove(WireType.display, displayId, channel);
    }
  }
  _tokens.clear();
  _windows.clear();
  _titles.clear();
  _displayId = null;
}

Map<String, dynamic>? _asMap(Object? raw) =>
    raw is Map<String, dynamic> ? raw : null;

int? _shellIdOf(Object? raw) => (_asMap(raw)?['shellId'] as num?)?.toInt();

/// Opens are taken one at a time rather than as they arrive.
///
/// Several shells can be shown in one turn — an application restoring a saved layout, or simply a
/// loop — and the requests then arrive together. A browser that allows popups for the origin still
/// declines a burst of them from a single task, so opening them as they arrived got the first
/// window and lost the rest: each refusal was reported, and the shells behind them were drawn inside
/// the page while one of their siblings sat in a window. Spacing them out is what makes a batch
/// behave like the same shells opened one by one.
final List<Map<String, dynamic>> _pending = <Map<String, dynamic>>[];
bool _draining = false;

/// Long enough to leave the burst the browser is declining, short enough not to read as a delay.
const Duration _openSpacing = Duration(milliseconds: 120);

/// A refusal is only final on the second attempt: the first can be the burst rather than the
/// user's settings, and reporting it as final would draw the shell inside the page for good.
const int _openAttempts = 2;

void _onOpenWindow(Object? raw) {
  final data = _asMap(raw);
  if (data == null) return;
  final shellId = (data['shellId'] as num?)?.toInt();
  if (shellId == null || data['url'] is! String) return;
  if (_windows.containsKey(shellId)) {
    _windows[shellId]!.focus();
    return;
  }
  _pending.add(data);
  unawaited(_drainPending());
}

/// How many times each shell's window has been attempted, while it is being attempted.
final Map<int, int> _attempts = <int, int>{};

Future<void> _drainPending() async {
  if (_draining) return;
  _draining = true;
  try {
    while (_pending.isNotEmpty) {
      final data = _pending.removeAt(0);
      final shellId = (data['shellId'] as num?)?.toInt();
      if (shellId == null || _windows.containsKey(shellId)) continue;

      if (_openOne(shellId, data)) {
        _attempts.remove(shellId);
      } else {
        final tried = (_attempts[shellId] ?? 0) + 1;
        if (tried < _openAttempts) {
          _attempts[shellId] = tried;
          _pending.add(data); // behind whatever else is waiting, so the burst has cleared by then
        } else {
          _attempts.remove(shellId);
          _reportOpenFailed(shellId);
        }
      }
      if (_pending.isNotEmpty) await Future<void>.delayed(_openSpacing);
    }
  } finally {
    _draining = false;
  }
}

/// True once a window is open for this shell. Retries [_openAttempts] times, spaced, because the
/// first refusal can be a burst the browser declined rather than a settled answer.
bool _openOne(int shellId, Map<String, dynamic> data) {
  final url = data['url'] as String;
  final width = (data['width'] as num?)?.toInt() ?? 640;
  final height = (data['height'] as num?)?.toInt() ?? 480;
  final x = (data['x'] as num?)?.toInt() ?? 0;
  final y = (data['y'] as num?)?.toInt() ?? 0;
  // `popup=yes` is what makes a browser open a window rather than a tab; without it the shell
  // would land in a tab, which cannot be positioned or sized and reads as a different app.
  final features =
      'popup=yes,width=$width,height=$height,left=$x,top=$y,resizable=yes,scrollbars=no';

  final opened = _open(url.toJS, 'equo-shell-$shellId'.toJS, features.toJS);
  if (opened == null) return false;
  final window = _BrowserWindow(opened);
  _windows[shellId] = window;
  // The window's page brings a title of its own when it loads, replacing any set before.
  window.addEventListener('load', (() => _applyTitle(shellId)).toJS);
  _startClosePoll();
  return true;
}

void _onWindowTitle(Object? raw) {
  final data = _asMap(raw);
  final shellId = (data?['shellId'] as num?)?.toInt();
  final title = data?['title'];
  if (shellId == null || title is! String) return;
  _titles[shellId] = title;
  _applyTitle(shellId);
}

void _applyTitle(int shellId) {
  final window = _windows[shellId];
  final title = _titles[shellId];
  if (window == null || window.closed || title == null) return;
  window.document.title = title;
}

void _onCloseWindow(Object? raw) {
  final shellId = _shellIdOf(raw);
  if (shellId == null) return;
  _titles.remove(shellId);
  final window = _windows.remove(shellId);
  if (window != null && !window.closed) window.close();
}

void _onWindowBounds(Object? raw) {
  final data = _asMap(raw);
  final shellId = (data?['shellId'] as num?)?.toInt();
  if (data == null || shellId == null) return;
  final window = _windows[shellId];
  if (window == null || window.closed) return;
  final x = (data['x'] as num?)?.toInt();
  final y = (data['y'] as num?)?.toInt();
  final width = (data['width'] as num?)?.toInt();
  final height = (data['height'] as num?)?.toInt();
  if (x != null && y != null) window.moveTo(x, y);
  if (width != null && height != null && width > 0 && height > 0) {
    window.resizeTo(width, height);
  }
}

/// Window state, as far as a browser allows. Maximize is the work area; minimize and true
/// fullscreen are not reachable for another window from here, so they are left alone rather than
/// approximated into something that would look like a bug.
void _onWindowState(Object? raw) {
  final data = _asMap(raw);
  final shellId = (data?['shellId'] as num?)?.toInt();
  if (data == null || shellId == null) return;
  final window = _windows[shellId];
  if (window == null || window.closed) return;
  final state = (data['state'] as num?)?.toInt() ?? 0;
  if (state != 1) return;
  final width = _availWidth?.toDartInt;
  final height = _availHeight?.toDartInt;
  if (width == null || height == null) return;
  window.moveTo(0, 0);
  window.resizeTo(width, height);
}

void _reportOpenFailed(int shellId) {
  final displayId = _displayId;
  if (displayId == null) return;
  EquoCommService.sendWritten(WidgetWireWriter.startTo(WireType.display, displayId, WireChannel.windowOpenFailed)..int64(shellId));
}

void _startClosePoll() {
  _closePoll ??= Timer.periodic(_closePollInterval, (_) {
    if (_windows.isEmpty) {
      _closePoll?.cancel();
      _closePoll = null;
      return;
    }
    final gone = <int>[];
    _windows.forEach((shellId, window) {
      if (window.closed) gone.add(shellId);
    });
    for (final shellId in gone) {
      _windows.remove(shellId);
      EquoCommService.sendTo(WireType.shell, shellId, WireChannel.winUnload);
    }
  });
}
