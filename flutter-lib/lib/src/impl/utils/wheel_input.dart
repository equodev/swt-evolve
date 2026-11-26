import 'package:flutter/gestures.dart';
import 'package:flutter/services.dart';

import '../../gen/event.dart';
import '../../gen/swt.dart';

/// Desktop embedders report a two-finger trackpad swipe as pan/zoom updates, not scroll signals.
/// The fingers drag the content, so [onScroll] receives the pan reversed as a wheel event; an
/// update that does not pan sends nothing.
void panAsScroll(PointerPanZoomUpdateEvent event, void Function(PointerScrollEvent) onScroll) {
  final delta = -event.panDelta;
  if (delta == Offset.zero) return;
  swipeBeingReported = event.original ?? event;
  try {
    onScroll(PointerScrollEvent(
      timeStamp: event.timeStamp,
      kind: event.kind,
      device: event.device,
      position: event.position,
      scrollDelta: delta,
      embedderId: event.embedderId,
    ).transformed(event.transform));
  } finally {
    swipeBeingReported = null;
  }
}

/// The swipe [panAsScroll] is reporting as a wheel, by its untransformed original, while it does:
/// each listener gets a wheel of its own, so a control that keeps that wheel keeps the swipe.
PointerEvent? swipeBeingReported;

/// The lines one mouse wheel event reports, whatever its delta: SWT's count for one notch on Win32
/// and GTK by default. A trackpad's swipe (see [panAsScroll]) and StyledText scale with the delta
/// instead.
const int wheelLinesPerEvent = 3;

/// Lines per wheel event: three per 120-unit notch, as a desktop scrolls by default, and at least
/// one for any movement.
int wheelLines(double delta) {
  final lines = (delta / 40).round();
  return lines != 0 ? lines : (delta > 0 ? 1 : -1);
}

/// The scroll a wheel event asks for. Shift without Ctrl turns a vertical wheel sideways, as on
/// Windows, which leaves Ctrl + Shift + wheel vertical for zoom; a browser may have turned it
/// already and deliver it as a horizontal delta.
Offset wheelDelta(PointerScrollEvent event) {
  final delta = event.scrollDelta;
  final keyboard = HardwareKeyboard.instance;
  if (delta.dx != 0 || !keyboard.isShiftPressed || keyboard.isControlPressed) return delta;
  return Offset(delta.dy, 0);
}

/// Trackpad travel that makes one SWT wheel event once a gesture is under way, as Cocoa sends one
/// per line of travel; 40 px is the line [wheelLines] counts in.
const double wheelPixelsPerEvent = 40;

/// A mouse-kind wheel at least this large is a notch and reports on its own; a smaller one is a
/// trackpad that the browser reports as a mouse, and gathers travel like a trackpad.
const double wheelNotchPixels = 4;

/// A gap between wheel signals that ends a gesture: the next signal reports at once.
const Duration wheelGesturePause = Duration(milliseconds: 100);

/// The wheel travel of one axis not yet reported, so that a trackpad swipe makes one wheel event
/// per line of travel rather than one per signal.
class WheelTravel {
  double _pending = 0;
  Duration? _last;

  /// The lines a signal moving [delta] at [time] reports, signed as [delta]; 0 for none. A [notch]
  /// and the first signal after a pause report at once; other signals gather travel until a line.
  int report(double delta, Duration time, {required bool notch}) {
    final last = _last;
    _last = time;
    if (notch || last == null || time - last > wheelGesturePause) {
      _pending = 0;
      return wheelLines(delta);
    }
    if (_pending != 0 && (_pending > 0) != (delta > 0)) _pending = 0;
    _pending += delta;
    final lines = (_pending / wheelPixelsPerEvent).truncate();
    _pending -= lines * wheelPixelsPerEvent;
    return lines;
  }
}

/// The unreported wheel travel of one control, per axis.
class WheelGesture {
  final vertical = WheelTravel();
  final horizontal = WheelTravel();
}

/// The SWT wheel events a scroll signal makes: SWT.MouseWheel for its vertical part and
/// SWT.MouseHorizontalWheel for its horizontal part, each counting positive up or left. With
/// [gesture], a mouse wheel notch reports on its own and a trackpad's signals, whatever kind the
/// browser gives them, report once their travel adds up to a line.
List<VEvent> wheelEvents(PointerScrollEvent event, {required int stateMask, WheelGesture? gesture}) {
  final trackpad = event.kind == PointerDeviceKind.trackpad;
  VEvent? wheel(int type, WheelTravel? travel, double along) {
    if (along == 0) return null;
    final notch = !trackpad && along.abs() >= wheelNotchPixels;
    final lines = travel == null
        ? wheelLines(along)
        : travel.report(along, event.timeStamp, notch: notch);
    if (lines == 0) return null;
    return VEvent()
      ..type = type
      ..x = event.localPosition.dx.round()
      ..y = event.localPosition.dy.round()
      ..count = trackpad ? -lines : (along > 0 ? -wheelLinesPerEvent : wheelLinesPerEvent)
      ..stateMask = stateMask;
  }

  final delta = wheelDelta(event);
  return [
    ?wheel(SWT.MouseWheel, gesture?.vertical, delta.dy),
    ?wheel(SWT.MouseHorizontalWheel, gesture?.horizontal, delta.dx),
  ];
}
