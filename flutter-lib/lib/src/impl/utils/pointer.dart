import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/widgets.dart';
import 'package:pointer_interceptor/pointer_interceptor.dart';

/// Marks [child] as a floating surface that must keep receiving pointer events where it is drawn
/// over an embedded platform view / iframe in the same view (e.g. the Browser widget). On web that
/// takes a real DOM element over the child's bounds, because the browser routes a click to the
/// topmost DOM element at that point regardless of what Flutter painted on its canvas above it.
/// Floating SWT surfaces drawn over a Browser (dialogs, menus, tooltips, secondary shells) need
/// this or they become unclickable on web.
///
/// The wrapper is in the tree on every platform, not only on web, so a widget test running on the
/// VM can assert that a floating surface has one. Nothing else fails when a new overlay forgets it:
/// it just silently stops taking clicks, and only over a Browser.
class PointerInterceptorScope extends StatelessWidget {
  const PointerInterceptorScope({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) =>
      kIsWeb ? PointerInterceptor(child: child, debug: false) : child;
}

/// Wraps [child] in a [PointerInterceptorScope]. Safe to apply unconditionally: off web it adds no
/// DOM element and leaves layout and hit testing untouched.
Widget pointerInterceptor(Widget child) => PointerInterceptorScope(child: child);
