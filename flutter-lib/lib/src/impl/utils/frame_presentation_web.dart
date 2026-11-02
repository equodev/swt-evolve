import 'dart:js_interop';

@JS('window.evolve.presentedFrames')
external JSNumber? get _presentedFrames;

@JS('window.evolve.nextPresentedFrame')
external JSPromise<JSAny?> _nextPresentedFrame();

/// The frames the page has put on screen, as index.html counts them; null without its counter.
int? presentedFrames() => _presentedFrames?.toDartInt;

/// Completes when the page shows its next frame.
Future<void> nextPresentedFrame() => _nextPresentedFrame().toDart;
