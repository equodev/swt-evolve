import 'package:flutter/gestures.dart';

/// Desktop embedders report a two-finger trackpad swipe as pan/zoom updates, not scroll signals.
/// The fingers drag the content, so [onScroll] receives the pan reversed as a wheel event; an
/// update that does not pan sends nothing.
void panAsScroll(PointerPanZoomUpdateEvent event, void Function(PointerScrollEvent) onScroll) {
  final delta = -event.panDelta;
  if (delta == Offset.zero) return;
  onScroll(PointerScrollEvent(
    timeStamp: event.timeStamp,
    kind: event.kind,
    device: event.device,
    position: event.position,
    scrollDelta: delta,
    embedderId: event.embedderId,
  ).transformed(event.transform));
}

/// Lines per wheel event: three per 120-unit notch, as a desktop scrolls by default, and at least
/// one for any movement.
int wheelLines(double delta) {
  final lines = (delta / 40).round();
  return lines != 0 ? lines : (delta > 0 ? 1 : -1);
}
