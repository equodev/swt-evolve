import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/widgets.dart';
import 'package:pointer_interceptor/pointer_interceptor.dart';

/// Wraps [child] in a [PointerInterceptor] on web so that pointer events land
/// on the Flutter widget instead of falling through to an embedded platform
/// view / iframe sitting in the same view (e.g. the Browser widget). Floating
/// SWT surfaces drawn over a Browser (dialogs, menus, tooltips, secondary
/// shells) need this or they become unclickable on web.
///
/// On every non-web platform this is a no-op and returns [child] unchanged, so
/// it is safe to wrap unconditionally.
Widget pointerInterceptor(Widget child) {
  return kIsWeb ? PointerInterceptor(child: child, debug: false) : child;
}
