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

/// Drags with the primary mouse button from the centre of [from] to the centre of [to] (each
/// `{swt}/{id}`) in [steps] moves [intervalMs] apart, then releases where it lands: a drop when a
/// drop target is there. Answers how many moves were made and how long the drag took.
Future<Map<String, Object?>> dragByIds(String from, String to, int steps, int intervalMs) async {
  final source = _renderBoxByIdentifier(from);
  final target = _renderBoxByIdentifier(to);
  if (source == null || target == null) return {'error': 'not mounted: ${source == null ? from : to}'};
  final start = source.localToGlobal(source.size.center(Offset.zero));
  final end = target.localToGlobal(target.size.center(Offset.zero));
  final int pointer = _pointerSeq++;
  final binding = GestureBinding.instance;
  final clock = Stopwatch()..start();
  binding.handlePointerEvent(PointerDownEvent(
      pointer: pointer, position: start, kind: PointerDeviceKind.mouse, buttons: kPrimaryButton));
  var at = start;
  var dispatched = 0;
  for (var i = 1; i <= steps; i++) {
    await Future<void>.delayed(Duration(milliseconds: intervalMs));
    final next = Offset.lerp(start, end, i / steps)!;
    // Drag recognizers follow the delta, not the position.
    final t0 = clock.elapsedMicroseconds;
    binding.handlePointerEvent(PointerMoveEvent(
        pointer: pointer,
        position: next,
        delta: next - at,
        kind: PointerDeviceKind.mouse,
        buttons: kPrimaryButton));
    dispatched += clock.elapsedMicroseconds - t0;
    at = next;
  }
  await Future<void>.delayed(Duration(milliseconds: intervalMs));
  binding.handlePointerEvent(PointerUpEvent(pointer: pointer, position: end, kind: PointerDeviceKind.mouse));
  return {'moves': steps, 'elapsedMicros': clock.elapsedMicroseconds, 'dispatchMicros': dispatched};
}

// ---- a mouse of its own ------------------------------------------------------

/// The injected mouse: a device apart from the real one, added on its first event, so it hovers,
/// enters and leaves as a mouse does.
const int _injectedDevice = 0x5157;
bool _injectedAdded = false;
Offset _injectedAt = Offset.zero;
int _injectedView = 0;
int? _injectedPressed;
int _lastFrameAt = -1;
int _framesDrawn = 0;
bool _framesWatched = false;

/// When the last frame finished building and painting, on [EquoCommBase.clock]; -1 before any.
int lastFrameAtMicros() {
  _watchFrames();
  return _lastFrameAt;
}

void _watchFrames() {
  if (_framesWatched) return;
  _framesWatched = true;
  // Added after the binding's own: it runs once the frame is built, laid out and painted.
  SchedulerBinding.instance
      .addPersistentFrameCallback((_) {
    _lastFrameAt = EquoCommBase.clock.elapsedMicroseconds;
    _framesDrawn++;
  });
}

/// Where [map] points, and in which view: `x`,`y` in the widget `target` (`{swt}/{id}`) or
/// `targetId` (its SWT id) when it names one, else `dx`,`dy` from where the mouse is.
(Offset, int)? _aim(Map<String, dynamic> map) {
  final target = map['target'] as String?;
  final targetId = map['targetId'] as num?;
  if (target == null && targetId == null) {
    return (
      _injectedAt + Offset(((map['dx'] ?? 0) as num).toDouble(), ((map['dy'] ?? 0) as num).toDouble()),
      _injectedView,
    );
  }
  final host = target != null ? _hostElementByIdentifier(target) : _hostElementById(targetId!.toInt());
  final box = host?.renderObject;
  if (box is! RenderBox || !box.attached || !box.hasSize) return null;
  final view = View.maybeOf(host!)?.viewId ?? _injectedView;
  return (box.localToGlobal(Offset((map['x'] as num).toDouble(), (map['y'] as num).toDouble())), view);
}

/// The injected device's time: the clock its frames and answers are stamped on.
Duration _now() => Duration(microseconds: EquoCommBase.clock.elapsedMicroseconds);

/// Moves the injected mouse to [to] in [view]: a drag while pressed, a hover otherwise.
void _moveTo(Offset to, [int? view]) {
  final binding = GestureBinding.instance;
  if (view != null && view != _injectedView && _injectedAdded && _injectedPressed == null) {
    binding.handlePointerEvent(PointerRemovedEvent(
        viewId: _injectedView, device: _injectedDevice, position: _injectedAt, kind: PointerDeviceKind.mouse,
        timeStamp: _now()));
    _injectedAdded = false;
  }
  if (view != null && _injectedPressed == null) _injectedView = view;
  if (!_injectedAdded) {
    _injectedAdded = true;
    binding.handlePointerEvent(PointerAddedEvent(
        viewId: _injectedView, device: _injectedDevice, position: to, kind: PointerDeviceKind.mouse,
        timeStamp: _now()));
    _injectedAt = to;
  }
  final delta = to - _injectedAt;
  _injectedAt = to;
  final pressed = _injectedPressed;
  binding.handlePointerEvent(pressed != null
      ? PointerMoveEvent(
          viewId: _injectedView,
          pointer: pressed,
          device: _injectedDevice,
          position: to,
          delta: delta,
          kind: PointerDeviceKind.mouse,
          buttons: _injectedButtons,
          timeStamp: _now())
      : PointerHoverEvent(
          viewId: _injectedView,
          device: _injectedDevice,
          position: to,
          delta: delta,
          kind: PointerDeviceKind.mouse,
          timeStamp: _now()));
}

/// The buttons the injected mouse holds down.
int _injectedButtons = 0;

/// The flag of SWT's mouse button [button] (1 left, 2 middle, 3 right).
int _buttonFlag(int button) => switch (button) {
      2 => kMiddleMouseButton,
      3 => kSecondaryMouseButton,
      _ => kPrimaryButton,
    };

void _press([int button = 1]) {
  if (_injectedPressed != null) return;
  final pointer = _pointerSeq++;
  _injectedPressed = pointer;
  _injectedButtons = _buttonFlag(button);
  GestureBinding.instance.handlePointerEvent(PointerDownEvent(
      viewId: _injectedView,
      pointer: pointer,
      device: _injectedDevice,
      position: _injectedAt,
      kind: PointerDeviceKind.mouse,
      buttons: _injectedButtons,
      timeStamp: _now()));
}

void _release() {
  final pointer = _injectedPressed;
  if (pointer == null) return;
  _injectedPressed = null;
  _injectedButtons = 0;
  GestureBinding.instance.handlePointerEvent(PointerUpEvent(
      viewId: _injectedView,
      pointer: pointer,
      device: _injectedDevice,
      position: _injectedAt,
      kind: PointerDeviceKind.mouse,
      timeStamp: _now()));
}

// ---- the real mouse --------------------------------------------------------

/// Where the OS's own mouse last was, as the engine reported it: its device, position and view.
int? _realDevice;
Offset _realAt = Offset.zero;
int _realView = 0;
bool _realWatched = false;

/// Follows the OS's mouse through every pointer event the binding routes, so an injected click or
/// wheel can land where it is, on its device.
void _watchReal() {
  if (_realWatched) return;
  _realWatched = true;
  GestureBinding.instance.pointerRouter.addGlobalRoute((PointerEvent e) {
    if (e.device == _injectedDevice || e.kind != PointerDeviceKind.mouse) return;
    if (e is PointerHoverEvent || e is PointerMoveEvent || e is PointerAddedEvent) {
      _realDevice = e.device;
      _realAt = e.position;
      _realView = e.viewId;
    }
  });
}

/// Waits until the OS's mouse has reached [to] in [view], or [timeout] passes; whether it did.
Future<bool> _awaitReal(Offset to, int view, Duration timeout) async {
  final deadline = DateTime.now().add(timeout);
  bool there() => _realDevice != null && _realView == view && (_realAt - to).distance <= 1.0;
  while (!there()) {
    if (DateTime.now().isAfter(deadline)) return false;
    await Future<void>.delayed(const Duration(milliseconds: 2));
  }
  return true;
}

/// Presses ([down]) or releases [button] of the OS's mouse where it is, as the engine would.
int? _realPressed;

void _realButton(bool down, int button) {
  final device = _realDevice;
  if (device == null) return;
  if (down) {
    if (_realPressed != null) return;
    final pointer = _pointerSeq++;
    _realPressed = pointer;
    GestureBinding.instance.handlePointerEvent(PointerDownEvent(
        viewId: _realView,
        pointer: pointer,
        device: device,
        position: _realAt,
        kind: PointerDeviceKind.mouse,
        buttons: _buttonFlag(button),
        timeStamp: _now()));
  } else {
    final pointer = _realPressed;
    if (pointer == null) return;
    _realPressed = null;
    GestureBinding.instance.handlePointerEvent(PointerUpEvent(
        viewId: _realView, pointer: pointer, device: device, position: _realAt, kind: PointerDeviceKind.mouse,
        timeStamp: _now()));
  }
}


/// The pixels a desktop embedder scrolls for one wheel notch (Linux's `kScrollOffsetMultiplier`).
const double _pixelsPerNotch = 53.0;

/// Turns the wheel [notches] notches, one scroll signal each, as a desktop embedder reports a wheel.
void _wheel(int notches) {
  for (var i = 0; i < notches.abs(); i++) {
    GestureBinding.instance.handlePointerEvent(PointerScrollEvent(
        viewId: _injectedView,
        device: _injectedDevice,
        position: _injectedAt,
        kind: PointerDeviceKind.mouse,
        scrollDelta: Offset(0, notches.sign * _pixelsPerNotch),
        timeStamp: _now()));
  }
}

/// One step of the injected mouse or keyboard, stamped on [EquoCommBase.clock] (`atMicros`) the
/// moment it is dispatched, which is also the clock of the frame barrier's `lastFrameAtMicros`:
///   `hover`/`move` {target|targetId, x, y} or {dx, dy} — moves there, dragging while pressed;
///   `down` {target|targetId, x, y}? {button}? — moves there when aimed, then presses `button`
///   (1 left, the default, 2 middle, 3 right);
///   `up` — releases it; `leave` — the mouse leaves the window;
///   `wheel` {notches} — turns the wheel where the mouse is, positive towards the user;
///   with `real`, `down` and `up` act on the OS's own mouse where it is instead;
///   `awaitReal` {target|targetId, x, y, timeoutMs} — waits until the OS's mouse has reached the
///   point; answers `arrived`; `watch` — starts following the OS's mouse and the text input
///   connection ([InjectedKeys.watch]);
///   `keyDown`/`keyUp` {vk} — presses or releases the key with AWT virtual key code `vk`
///   ([InjectedKeys]); answers `handled`;
///   `path` {target, points: [x0, y0, x1, y1, ...], press, intervalMs} — the whole gesture at a
///   user's pace: to the first point, pressed when `press`, through the rest [intervalMs] apart, and
///   released at the last. Answers `firstAtMicros`, `lastAtMicros`, `moves` and `frames`, the frames
///   drawn from the first input to the last.
Future<Map<String, Object?>> pointerOp(Map<String, dynamic> map) async {
  _watchFrames();
  _watchReal();
  final clock = EquoCommBase.clock;
  final op = map['op'] as String;
  final real = map['real'] == true;
  try {
    switch (op) {
      case 'watch':
        InjectedKeys.watch();
        return {'atMicros': clock.elapsedMicroseconds};
      case 'awaitReal':
        final to = _aim(map);
        if (to == null) return {'error': 'not laid out: ${map['target'] ?? map['targetId']}'};
        final arrived = await _awaitReal(to.$1, to.$2, Duration(milliseconds: ((map['timeoutMs'] ?? 500) as num).toInt()));
        return {'atMicros': clock.elapsedMicroseconds, 'arrived': arrived};
      case 'down' when real:
        _realButton(true, ((map['button'] ?? 1) as num).toInt());
        return {'atMicros': clock.elapsedMicroseconds};
      case 'up' when real:
        _realButton(false, 1);
        return {'atMicros': clock.elapsedMicroseconds};
      case 'hover':
      case 'move':
      case 'down':
        final aimed = map['target'] != null || map['targetId'] != null || map['dx'] != null || map['dy'] != null;
        final to = aimed ? _aim(map) : (_injectedAt, _injectedView);
        if (to == null) return {'error': 'not laid out: ${map['target'] ?? map['targetId']}'};
        final at = clock.elapsedMicroseconds;
        if (aimed) _moveTo(to.$1, to.$2);
        if (op == 'down') _press(((map['button'] ?? 1) as num).toInt());
        return {'atMicros': at, 'x': to.$1.dx, 'y': to.$1.dy};
      case 'up':
        final at = clock.elapsedMicroseconds;
        _release();
        return {'atMicros': at};
      case 'wheel':
        final at = clock.elapsedMicroseconds;
        _wheel((map['notches'] as num).toInt());
        return {'atMicros': at};
      case 'keyDown':
      case 'keyUp':
        final at = clock.elapsedMicroseconds;
        return {'atMicros': at, ...InjectedKeys.key((map['vk'] as num).toInt(), op == 'keyDown', _now())};
      case 'leave':
        final at = clock.elapsedMicroseconds;
        _release();
        if (_injectedAdded) {
          _injectedAdded = false;
          GestureBinding.instance.handlePointerEvent(PointerRemovedEvent(
              viewId: _injectedView, device: _injectedDevice, position: _injectedAt, kind: PointerDeviceKind.mouse,
              timeStamp: _now()));
        }
        return {'atMicros': at};
      case 'path':
        final target = map['target'] as String;
        final box = _renderBoxByIdentifier(target);
        if (box == null || !box.attached || !box.hasSize) return {'error': 'not laid out: $target'};
        final coords = (map['points'] as List).cast<num>();
        // Placed once, against where the target is now: a pointer does not follow what it moves.
        final points = [
          for (var i = 0; i + 1 < coords.length; i += 2)
            box.localToGlobal(Offset(coords[i].toDouble(), coords[i + 1].toDouble())),
        ];
        final interval = Duration(milliseconds: (map['intervalMs'] as num).toInt());
        final press = map['press'] == true;
        final first = clock.elapsedMicroseconds;
        final frames0 = _framesDrawn;
        _moveTo(points.first);
        if (press) _press();
        var last = first;
        for (var i = 1; i < points.length; i++) {
          await Future<void>.delayed(interval);
          last = clock.elapsedMicroseconds;
          _moveTo(points[i]);
        }
        if (press) {
          await Future<void>.delayed(interval);
          last = clock.elapsedMicroseconds;
          _release();
        }
        return {
          'firstAtMicros': first,
          'lastAtMicros': last,
          'moves': points.length - 1,
          'frames': _framesDrawn - frames0,
        };
    }
    return {'error': 'unknown op $op'};
  } catch (e) {
    return {'error': '$e'};
  }
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

// ============================================================================
// UI-thread runner triggers — reach app surfaces the Flutter action layer can't.
//
// A native OS menu (macOS Cmd+, / Window > Preferences) can't be driven by any
// synthetic Flutter gesture, and it gates every Preferences-dialog bug. These
// send a request on `swt.evolve.test.runUi` to a debug-gated Java hook
// (TestUiRunner) that runs a reflective action on the SWT Display thread — e.g.
// opening the Preferences dialog through the app's own workbench layer. Once the
// dialog is open its content is Flutter-rendered again, so `tapById`/`typeById`
// above take over.
//
// The opener/registry class names are app-specific and come from the caller (the
// private flutter-mcp wrapper), so nothing here — or in the Java hook — names an
// application. Each returns the small `{"ok":...}` envelope; the request itself is
// fire-and-forget (Java acts asynchronously on the UI thread).
// ============================================================================

/// Latest `listPrefPages` result cached from the Java response channel (see
/// test_harness.dart). Read via [lastPrefPages] after a [listPrefPages] request.
Map<String, dynamic>? _lastRunUiResponse;

/// Open the app's Preferences dialog via [opener] (an app-supplied opener class),
/// optionally jumping straight to page [id]; [style] selects the opener shape
/// (`builder` — a JFace preference-dialog builder; `util` — Eclipse's
/// PreferencesUtil). With no [id] it opens at the default page and the caller
/// navigates the Flutter-rendered preference tree with `tapById`.
String openPref(String opener, [String? id, String style = 'builder']) {
  EquoCommService.sendOn(WireChannel.swtEvolveTestRunUi, {
    'action': 'openPreferences',
    'opener': opener,
    'style': style,
    if (id != null && id.isNotEmpty) 'id': id,
  });
  return _ok('sent', 'openPreferences ${id == null || id.isEmpty ? "(default page)" : id} via $style');
}

/// Open the app's Preferences dialog on a preference manager built from an
/// app-supplied [registry] class and its [xpField] extension-point constant,
/// optionally jumping straight to page [id]. Reaches preference registries the
/// app exposes outside Eclipse's PreferencesUtil set and the project-settings
/// builder — e.g. an app's "general settings" pages that live under a non-project
/// extension point. The Java hook builds a plain JFace `PreferenceDialog` on that
/// manager, so nothing here names an application.
String openPrefViaRegistry(String registry, String xpField, [String? id]) {
  EquoCommService.sendOn(WireChannel.swtEvolveTestRunUi, {
    'action': 'openPreferences',
    'style': 'registry',
    'registry': registry,
    'xpField': xpField,
    if (id != null && id.isNotEmpty) 'id': id,
  });
  return _ok('sent',
      'openPreferences[registry] ${id == null || id.isEmpty ? "(default page)" : id} via $registry');
}

/// Open an application [project] (a path) so project-scoped surfaces can build —
/// some preference pages NPE until a project is open. Posts the app's own
/// project-open workbench event, whose topic is read at runtime from the
/// app-supplied [eventClass] `#`[topicField] constant.
String openProject(String project, String eventClass, String topicField) {
  EquoCommService.sendOn(WireChannel.swtEvolveTestRunUi, {
    'action': 'openProject',
    'project': project,
    'eventClass': eventClass,
    'topicField': topicField,
  });
  return _ok('sent', 'openProject $project');
}

/// Open a file (by absolute [path]) in its default editor.
String openFile(String path) {
  EquoCommService.sendOn(WireChannel.swtEvolveTestRunUi, {
    'action': 'openFile',
    'path': path,
  });
  return _ok('sent', 'openFile $path');
}

/// Ask the Java hook to enumerate the preference pages via [registry] (an
/// app-supplied registry class) and its [xpField] extension-point constant. The
/// result arrives asynchronously on the response channel — read it with
/// [lastPrefPages] a beat later.
String listPrefPages(String registry, [String? xpField]) {
  EquoCommService.sendOn(WireChannel.swtEvolveTestRunUi, {
    'action': 'listPrefPages',
    'registry': registry,
    if (xpField != null && xpField.isNotEmpty) 'xpField': xpField,
  });
  return _ok('sent', 'listPrefPages via $registry');
}

/// The most recent [listPrefPages] result (`{"action":...,"pages":[{id,label}...]}`)
/// as a JSON string, or an `ok:false` envelope if none has arrived yet.
String lastPrefPages() => _lastRunUiResponse == null
    ? _fail('lastPrefPages', 'no listPrefPages response yet — send listPrefPages first, then retry')
    : jsonEncode(_lastRunUiResponse);

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
