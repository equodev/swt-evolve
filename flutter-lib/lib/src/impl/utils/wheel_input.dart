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

/// The SWT wheel events a scroll signal makes: SWT.MouseWheel for its vertical part and
/// SWT.MouseHorizontalWheel for its horizontal part, each counting positive up or left.
List<VEvent> wheelEvents(PointerScrollEvent event, {required int stateMask}) {
  VEvent wheel(int type, double along) => VEvent()
    ..type = type
    ..x = event.localPosition.dx.round()
    ..y = event.localPosition.dy.round()
    ..count = event.kind == PointerDeviceKind.trackpad
        ? -wheelLines(along)
        : (along > 0 ? -wheelLinesPerEvent : wheelLinesPerEvent)
    ..stateMask = stateMask;
  final delta = wheelDelta(event);
  return [
    if (delta.dy != 0) wheel(SWT.MouseWheel, delta.dy),
    if (delta.dx != 0) wheel(SWT.MouseHorizontalWheel, delta.dx),
  ];
}
