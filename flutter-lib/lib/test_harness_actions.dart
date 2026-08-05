part of 'test_harness.dart';

// ============================================================================
// App-agnostic action layer over a live SWT-Evolve app.
//
// Callable via `flutter-mcp eval '<fn>(...)'` (runs in this library's scope) or,
// on web, through window.evolveTest. Targets a widget by its `{swt}/{id}`
// semantics identifier, by visible text, or by raw coordinates. Together with
// the read side (queryAllStatesJson) this closes the read → act → verify loop
// that lets an agent drive a real app unattended.
//
// Designed for the real-app DTD isolate, where two things break the naive
// approaches: `flutter_driver` is absent (so `ext.flutter.driver` gestures don't
// exist), and off-screen / drag-reorder subtrees carry unlaid-out
// RenderTransforms (so a hit-test can throw `RenderBox was not laid out`). So
// each action tries strategies in order of robustness and never lets one throw
// escape — every call returns a small JSON envelope instead:
//
//   {"ok": bool, "via": "<strategy>", "detail": "<free text>"}
//
// Strategy order for a tap:
//   1. semantics performAction(tap) — layout-independent; works for controls
//      wired with GestureDetector(onTap:) (Button, ...). Immune to the
//      unlaid-out RenderTransform crash because it never hit-tests.
//   2. guarded pointer injection at the target's center — covers controls wired
//      with a raw Listener(onPointerDown:) (tabs, Text, Combo, ...). Guarded by
//      attached/hasSize and wrapped so a layout-state throw becomes ok:false.
//   3. explicit tapByXY(x, y) — the caller's escape hatch for canvas-painted
//      controls that have no node of their own (click a DOM/semantics rect).
//
// type drives the field's EditableTextState.updateEditingValue directly (anchored
// on the {swt}/{id} element, not semantics — Flutter web omits the setText action);
// scroll rides the semantics scroll action Scrollable exposes.
// ============================================================================

int _pointerSeq = 1000;

String _ok(String via, [String detail = '']) =>
    jsonEncode({'ok': true, 'via': via, if (detail.isNotEmpty) 'detail': detail});

String _fail(String via, String detail) =>
    jsonEncode({'ok': false, 'via': via, 'detail': detail});

/// Turn the semantics tree on if it isn't already, so identifier-targeted actions
/// have nodes to find. An embedding app that sets `enableTestSemantics=true` already
/// has it; a plain gallery run does not — call this once after connecting there. The
/// tree is built on the next frame, which this schedules; give it a beat before the
/// first action.
SemanticsHandle? _semanticsHandle;
String ensureTestSemantics() {
  final already = WidgetsBinding.instance.semanticsEnabled;
  _semanticsHandle ??= WidgetsBinding.instance.ensureSemantics();
  if (!already) {
    // Widgets built while semantics was off skipped their `{swt}/{id}` identifier
    // wrapper (tagSemantics early-returns unless semanticsEnabled). Enabling the
    // tree now does not rebuild them, so force it — otherwise identifier-targeted
    // actions find nothing. When semantics is already on at launch this is a no-op.
    void markDirty(Element el) {
      if (el is StatefulElement && el.state is WidgetSwtState) el.markNeedsBuild();
      el.visitChildren(markDirty);
    }

    WidgetsBinding.instance.rootElement?.visitChildren(markDirty);
  }
  WidgetsBinding.instance.scheduleFrame();
  return _ok('ensureSemantics', already ? 'was already on' : 'enabled + rebuilt; wait one frame');
}

/// Schedule a frame after an action so the caller's follow-up read reflects it.
/// Deliberately does NOT synchronously pump (handleBeginFrame/handleDrawFrame),
/// which is what makes forcing a frame over an unlaid-out subtree throw.
void _tick() => WidgetsBinding.instance.scheduleFrame();

// ---- semantics traversal ---------------------------------------------------

/// Visit every (owner, rootSemanticsNode) pair across all views' pipeline owners.
void _forEachSemantics(void Function(SemanticsOwner owner, SemanticsNode root) fn) {
  void collect(PipelineOwner owner) {
    final SemanticsOwner? so = owner.semanticsOwner;
    final SemanticsNode? root = so?.rootSemanticsNode;
    if (so != null && root != null) fn(so, root);
    owner.visitChildren(collect);
  }

  collect(WidgetsBinding.instance.rootPipelineOwner);
}

/// The (owner, node) whose semantics identifier is [identifier], or null.
(SemanticsOwner, SemanticsNode)? _nodeByIdentifier(String identifier) {
  (SemanticsOwner, SemanticsNode)? hit;
  _forEachSemantics((owner, root) {
    if (hit != null) return;
    bool visit(SemanticsNode n) {
      if (n.identifier == identifier) {
        hit = (owner, n);
        return false;
      }
      n.visitChildren(visit);
      return hit == null;
    }

    visit(root);
  });
  return hit;
}

/// The (owner, node) whose semantics label equals [text], or null.
(SemanticsOwner, SemanticsNode)? _nodeByLabel(String text) {
  (SemanticsOwner, SemanticsNode)? hit;
  _forEachSemantics((owner, root) {
    if (hit != null) return;
    bool visit(SemanticsNode n) {
      final data = n.getSemanticsData();
      if (data.label == text || data.value == text) {
        hit = (owner, n);
        return false;
      }
      n.visitChildren(visit);
      return hit == null;
    }

    visit(root);
  });
  return hit;
}

/// [start] itself, or the nearest descendant, that handles [action]; else null.
SemanticsNode? _actionNode(SemanticsNode start, SemanticsAction action) {
  if (start.getSemanticsData().hasAction(action)) return start;
  SemanticsNode? found;
  bool visit(SemanticsNode n) {
    if (n.getSemanticsData().hasAction(action)) {
      found = n;
      return false;
    }
    n.visitChildren(visit);
    return found == null;
  }

  start.visitChildren(visit);
  return found;
}

// ---- element / render traversal (semantics-independent fallback) -----------

/// The mounted [WidgetSwtState] Element whose `state` has this `{swt}/{id}`, or null.
/// This is the semantics-independent anchor: it works whether or not the semantics
/// tree is on, so type/pointer fall-backs don't depend on identifiers being present.
Element? _hostElementByIdentifier(String identifier) {
  final int slash = identifier.lastIndexOf('/');
  if (slash < 0) return null;
  final String swt = identifier.substring(0, slash);
  final int? id = int.tryParse(identifier.substring(slash + 1));
  if (id == null) return null;
  final root = WidgetsBinding.instance.rootElement;
  if (root == null) return null;
  Element? found;
  void visit(Element el) {
    if (found != null) return;
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic v = (el.state as WidgetSwtState).state;
      if (v != null && v.swt == swt && v.id == id) {
        found = el;
        return;
      }
    }
    el.visitChildren(visit);
  }

  root.visitChildren(visit);
  return found;
}

/// The RenderBox of the mounted widget with this `{swt}/{id}`, or null.
RenderBox? _renderBoxByIdentifier(String identifier) {
  final ro = _hostElementByIdentifier(identifier)?.renderObject;
  return ro is RenderBox ? ro : null;
}

/// The first [EditableTextState] in [host]'s subtree — the field's real
/// TextInputClient, whose updateEditingValue drives the same edit pipeline the
/// platform uses (so onChanged fires and the SWT Modify event reaches Java).
EditableTextState? _editableUnder(Element host) {
  EditableTextState? found;
  void visit(Element el) {
    if (found != null) return;
    if (el is StatefulElement && el.state is EditableTextState) {
      found = el.state as EditableTextState;
      return;
    }
    el.visitChildren(visit);
  }

  host.visitChildren(visit);
  return found;
}

/// Dispatch a down+up pointer pair at a global point. Wrapped by callers.
void _pointerTapAt(Offset global) {
  final int pointer = _pointerSeq++;
  final binding = GestureBinding.instance;
  binding.handlePointerEvent(PointerDownEvent(pointer: pointer, position: global));
  binding.handlePointerEvent(PointerUpEvent(pointer: pointer, position: global));
}

// ---- public actions --------------------------------------------------------

String _tid(String swt, int id) => '$swt/$id';

String tapBySwtId(String swt, int id) => tapById(_tid(swt, id));
String typeBySwtId(String swt, int id, String text) => typeById(_tid(swt, id), text);
String scrollBySwtId(String swt, int id, String direction) => scrollById(_tid(swt, id), direction);

/// Tap the widget with semantics identifier [identifier] (`{swt}/{id}`).
String tapById(String identifier) {
  // 1. semantics tap — layout-independent.
  final located = _nodeByIdentifier(identifier);
  if (located != null) {
    final (owner, node) = located;
    final target = _actionNode(node, SemanticsAction.tap);
    if (target != null) {
      try {
        owner.performAction(target.id, SemanticsAction.tap);
        _tick();
        return _ok('semantics.tap', 'node=${target.id} id=$identifier');
      } catch (e) {
        return _fail('semantics.tap', '$e');
      }
    }
  }
  // 2. guarded pointer injection — covers Listener-based controls.
  final box = _renderBoxByIdentifier(identifier);
  if (box != null) {
    if (!box.attached || !box.hasSize) {
      return _fail('pointer', 'render box for $identifier is unlaid-out (attached=${box.attached}, '
          'hasSize=${box.hasSize}); pass explicit coords to tapByXY');
    }
    try {
      _pointerTapAt(box.localToGlobal(box.size.center(Offset.zero)));
      _tick();
      return _ok('pointer', 'id=$identifier');
    } catch (e) {
      // A RenderTransform in the hit path was unlaid-out, or localToGlobal threw.
      return _fail('pointer', '$e; pass explicit coords to tapByXY');
    }
  }
  return _fail('tapById', 'no semantics node and no render box for $identifier '
      '(is the semantics tree on? see ensureTestSemantics)');
}

/// Tap the first widget whose semantics label/value is exactly [text].
String tapByText(String text) {
  final located = _nodeByLabel(text);
  if (located == null) return _fail('tapByText', 'no semantics node labelled "$text"');
  final (owner, node) = located;
  final target = _actionNode(node, SemanticsAction.tap);
  if (target == null) return _fail('tapByText', 'node "$text" has no tap action');
  try {
    owner.performAction(target.id, SemanticsAction.tap);
    _tick();
    return _ok('semantics.tap', 'label="$text"');
  } catch (e) {
    return _fail('semantics.tap', '$e');
  }
}

/// Tap at an explicit global coordinate — the escape hatch for canvas-painted
/// controls with no node of their own (use a DOM/semantics rect's center).
String tapByXY(double x, double y) {
  try {
    _pointerTapAt(Offset(x, y));
    _tick();
    return _ok('pointer.xy', 'x=$x y=$y');
  } catch (e) {
    return _fail('pointer.xy', '$e');
  }
}

/// Set the text of the editable field under [identifier] by driving its
/// EditableTextState.updateEditingValue — the same entry point the platform's
/// text input uses, so onChanged fires and the SWT Modify event reaches Java.
/// (Semantics `setText` is deliberately NOT used: Flutter web routes text input
/// through the DOM and doesn't expose that action, so it would silently no-op.)
/// Element-anchored, so it works whether or not the semantics tree is on.
String typeById(String identifier, String text) {
  final host = _hostElementByIdentifier(identifier);
  if (host == null) {
    return _fail('typeById', 'no widget element for $identifier (wrong id?)');
  }
  final ed = _editableUnder(host);
  if (ed == null) {
    return _fail('typeById', 'no editable field under $identifier');
  }
  try {
    ed.updateEditingValue(TextEditingValue(
      text: text,
      selection: TextSelection.collapsed(offset: text.length),
    ));
    _tick();
    return _ok('editable.updateEditingValue', 'id=$identifier len=${text.length}');
  } catch (e) {
    return _fail('editable.updateEditingValue', '$e');
  }
}

/// Scroll the scrollable at/under [identifier] one step in [direction]
/// (up|down|left|right), via the semantics scroll action Scrollable exposes.
String scrollById(String identifier, String direction) {
  final action = switch (direction.toLowerCase()) {
    'up' => SemanticsAction.scrollUp,
    'down' => SemanticsAction.scrollDown,
    'left' => SemanticsAction.scrollLeft,
    'right' => SemanticsAction.scrollRight,
    _ => null,
  };
  if (action == null) return _fail('scrollById', 'direction must be up|down|left|right, got "$direction"');
  final located = _nodeByIdentifier(identifier);
  if (located == null) return _fail('scrollById', 'no semantics node for $identifier');
  final (owner, node) = located;
  final target = _actionNode(node, action);
  if (target == null) return _fail('scrollById', 'nothing under $identifier handles scroll-$direction');
  try {
    owner.performAction(target.id, action);
    _tick();
    return _ok('semantics.scroll', 'id=$identifier dir=$direction');
  } catch (e) {
    return _fail('semantics.scroll', '$e');
  }
}
