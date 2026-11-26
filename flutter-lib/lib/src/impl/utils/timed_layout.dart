import 'dart:developer' as developer;

import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';

import 'perf_marks.dart';

/// [child], whose layout (and so a LayoutBuilder's rebuild of its subtree, which happens in its
/// layout) is a timeline span named `Layout:<name>` when perf marks are on; [child] itself otherwise.
/// For finding which layout builders a frame spends its time rebuilding.
Widget timedLayout(BuildContext context, String kind, Widget child) {
  if (!perfMarksEnabled) return child;
  return _TimedLayout(name: '$kind:${_owner(context)}', child: child);
}

/// The nearest control implementation above [context] (a State named `...Impl`): the control the
/// builder belongs to.
String _owner(BuildContext context) {
  String? found;
  context.visitAncestorElements((e) {
    if (e is StatefulElement) {
      final name = e.state.runtimeType.toString();
      final generic = name.indexOf('<');
      final bare = generic < 0 ? name : name.substring(0, generic);
      if (bare.endsWith('Impl')) {
        found = bare;
        return false;
      }
    }
    return true;
  });
  return found ?? '?';
}

class _TimedLayout extends SingleChildRenderObjectWidget {
  const _TimedLayout({required this.name, super.child});

  final String name;

  @override
  RenderObject createRenderObject(BuildContext context) => _RenderTimedLayout(name);

  @override
  void updateRenderObject(BuildContext context, _RenderTimedLayout renderObject) => renderObject.name = name;
}

class _RenderTimedLayout extends RenderProxyBox {
  _RenderTimedLayout(this.name);

  String name;

  @override
  void performLayout() {
    developer.Timeline.timeSync('Layout:$name', super.performLayout);
  }
}
