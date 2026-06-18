/// A window-drag surface backed by a real DOM element (web) so `window.equo.beginMove`
/// runs inside a genuine `mousedown` with true screen coordinates — which CEF honors,
/// unlike a Flutter-synthesized pointer event. Stub is an inert box off the web.
library;

export 'csd_drag_view_stub.dart'
    if (dart.library.js_interop) 'csd_drag_view_web.dart';
