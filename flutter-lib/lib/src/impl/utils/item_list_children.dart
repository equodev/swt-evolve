import 'package:flutter/widgets.dart';

import '../../gen/composite.dart';
import '../../gen/control.dart';
import '../../nolayout.dart';

/// The controls a Tree or a Table holds besides its items -- a TreeCursor or a TableCursor, or a control
/// an application placed over the rows itself -- laid out at their own bounds, which SWT gives in the
/// list's own coordinates. A cell editor's control is left out: the editor overlay draws it, and building
/// it here as well would mount the same control twice.
///
/// Null when there is nothing to lay out. Where no child stands the layer takes no pointer, so the rows
/// under it keep their clicks.
Widget? buildChildControlLayer(VComposite list, Iterable<VControl?> editorControls) {
  final children = list.children;
  if (children == null || children.isEmpty) return null;
  final editorIds = {for (final c in editorControls) if (c != null) c.id};
  final hosted = [for (final c in children) if (!editorIds.contains(c.id)) c];
  if (hosted.isEmpty) return null;
  return Positioned.fill(child: NoLayout(children: hosted, composite: list));
}
