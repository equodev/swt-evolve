import 'package:flutter/widgets.dart';

import '../comm/comm.dart';

/// Keyboard focus asked for by Java -- `Control.setFocus()`, a JFace cell editor activating, a
/// part activation. The render side owns the real focus, so the request has to cross the bridge or
/// the control never receives a keystroke.
///
/// It travels on one Display-wide channel and is held until a control claims it, rather than being
/// delivered on the target's own channel: Java can focus a control it created in the same
/// event-loop pass, and that control only exists client-side one frame after its parent's state
/// push, so a per-widget listener would not be registered yet when the request lands.
class FocusRequests extends ChangeNotifier {
  FocusRequests._();

  static final FocusRequests instance = FocusRequests._();

  static const String channel = 'swt.evolve.focus';

  /// Where each request's `seq` is echoed back as soon as it arrives. Java holds a request until
  /// then and drops the focus reports that reach it first: they were sent before this client knew
  /// of the request, so they name the focus it held before, and applying them would undo it.
  static const String seenChannel = 'swt.evolve.focus.seen';

  /// Which control Java holds as its focus control, when it changes without a Display push (which
  /// carries it as `VDisplay.focusHolderId`). It never moves the client's keyboard focus; Tab
  /// traverses from it (see `TraversalVetoScope`).
  static const String holderChannel = 'swt.evolve.focusHolder';

  static bool _listening = false;

  int? _pendingId;

  int? _holderId;

  final Map<int, BuildContext> _controls = {};

  /// Subscribes the process-wide listener. Idempotent.
  static void listen() {
    if (_listening) return;
    _listening = true;
    EquoCommService.onRaw(channel, (dynamic decoded) {
      if (decoded is! Map) return;
      final seq = decoded['seq'];
      if (seq is num) EquoCommService.sendPayload(seenChannel, {'seq': seq});
      final id = (decoded['id'] as num?)?.toInt();
      if (id != null) instance.request(id);
    });
    EquoCommService.onRaw(holderChannel, (dynamic decoded) {
      if (decoded is! Map) return;
      instance._holderId = (decoded['id'] as num?)?.toInt();
    });
  }

  /// The holder named by a Display push; 0 means none.
  void holderFromDisplay(int? id) => _holderId = id == 0 ? null : id;

  void register(int id, BuildContext context) => _controls[id] = context;

  void unregister(int id, BuildContext context) {
    if (identical(_controls[id], context)) _controls.remove(id);
  }

  /// Every mounted control.
  Set<BuildContext> get controls => _controls.values.toSet();

  /// The mounted control Java holds as its focus control, or null.
  BuildContext? get holder {
    final context = _holderId == null ? null : _controls[_holderId];
    return context != null && context.mounted ? context : null;
  }

  /// The latest request, claimed or not.
  int? _requestedId;

  void request(int id) {
    _requestedId = id;
    _pendingId = id;
    notifyListeners();
  }

  /// Whether [id] is the outstanding request, consuming it if so. A request is honoured once, so a
  /// later rebuild cannot pull focus back from wherever the user has moved it since.
  bool claim(int? id) {
    if (id == null || _pendingId != id) return false;
    _pendingId = null;
    return true;
  }

  /// Hands back a request [claim]ed by an element disposed before it could apply it, for whichever
  /// element now shows that control, unless a later request has replaced it.
  void giveBack(int id) {
    if (_pendingId == null && _requestedId == id) request(id);
  }

  @visibleForTesting
  void reset() {
    _pendingId = null;
    _requestedId = null;
    _holderId = null;
  }
}
