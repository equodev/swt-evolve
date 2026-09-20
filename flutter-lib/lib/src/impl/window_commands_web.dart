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
}

final Map<int, _BrowserWindow> _windows = <int, _BrowserWindow>{};

/// How often a window is checked for having been closed by the user. There is no cross-window
/// close event to subscribe to from here, and the closing page's own `pagehide` is the reliable
/// signal — this only catches a window that went without one.
const Duration _closePollInterval = Duration(seconds: 1);

Timer? _closePoll;
int? _displayId;
final List<Object> _tokens = <Object>[];

Map<int, Object> get openedWindows => Map.unmodifiable(_windows);

void registerWindowCommands(int displayId) {
  _displayId = displayId;
  final prefix = 'Display/$displayId';
  _tokens.add(EquoCommService.onRaw('$prefix/OpenWindow', _onOpenWindow));
  _tokens.add(EquoCommService.onRaw('$prefix/CloseWindow', _onCloseWindow));
  _tokens.add(EquoCommService.onRaw('$prefix/WindowBounds', _onWindowBounds));
  _tokens.add(EquoCommService.onRaw('$prefix/WindowState', _onWindowState));
}

void disposeWindowCommands() {
  _closePoll?.cancel();
  _closePoll = null;
  final displayId = _displayId;
  if (displayId != null) {
    for (final name in const ['OpenWindow', 'CloseWindow', 'WindowBounds', 'WindowState']) {
      EquoCommService.remove('Display/$displayId/$name');
    }
  }
  _tokens.clear();
  _windows.clear();
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
  _windows[shellId] = _BrowserWindow(opened);
  _startClosePoll();
  return true;
}

void _onCloseWindow(Object? raw) {
  final shellId = _shellIdOf(raw);
  if (shellId == null) return;
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
  EquoCommService.sendPayload(
      'Display/$displayId/WindowOpenFailed', {'shellId': shellId});
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
      EquoCommService.send('Shell/$shellId/WinUnload');
    }
  });
}
