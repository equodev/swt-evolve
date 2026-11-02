import 'dart:async';
import 'dart:typed_data';

import '../../comm/comm.dart';
import '../../comm/comm_api.dart' show WidgetOpHandler;
import '../../comm/tagged_values.dart';
import '../../gen/widgets.dart' show WireChannel;

/// Client half of the `Event.doit` round trip: the widget's vetoable channel arms the gate when Java
/// has a listener that could veto; its verdict channel carries the answer.
///
/// Verdicts resolve the queue head rather than naming a proposal — `Event` is upstream SWT with no
/// field for a correlation id, and the per-control channel is already ordered by `Display.asyncExec`.
///
/// The gate never knows what the default action is; each call site supplies it to [propose].
class VetoGate {
  /// The gate on the channels numbered [vetoable] and [verdict] (see [WireChannel]).
  VetoGate(int vetoable, int verdict, {this.timeout = const Duration(milliseconds: 400)})
      : _vetoable = vetoable,
        _verdict = verdict,
        _optimistic = false;

  /// For an action that cannot be withheld, so it happens and is undone on a veto (`DragDetect`:
  /// Flutter's gesture arena commits before any listener could run). No arming leg, one channel.
  VetoGate.optimistic(int verdict, {this.timeout = const Duration(milliseconds: 400)})
      : _vetoable = -1,
        _verdict = verdict,
        _optimistic = true,
        _armed = true;

  /// The gate of key presses.
  VetoGate.key() : this(WireChannel.keyVetoable, WireChannel.keyVerdict);

  /// The gate of edits.
  VetoGate.modify({Duration timeout = const Duration(milliseconds: 400)})
      : this(WireChannel.modifyVetoable, WireChannel.modifyVerdict, timeout: timeout);

  /// The gate of traversals.
  VetoGate.traverse() : this(WireChannel.traverseVetoable, WireChannel.traverseVerdict);

  final int _vetoable;
  final int _verdict;

  /// Fail-open deadline: a lost verdict must never swallow the user's input for good.
  final Duration timeout;

  final bool _optimistic;
  bool _armed = false;
  bool? _earlyVerdict;
  final List<_Proposal> _queue = [];
  int? _id;
  int _type = -1;
  int _vetoableChannel = -1;
  int _verdictChannel = -1;

  /// The attached gates by id, per kind of widget and channel they listen on (see [_slot]): where
  /// the channel's handler finds the gate a frame addresses.
  static final Map<int, Map<int, VetoGate>> _gates = {};

  /// One handler per kind of widget and channel, made once.
  static final Map<int, WidgetOpHandler> _handlers = {};

  /// A kind of widget and a channel as one key; channel numbers stay well below 4096.
  static int _slot(int type, int channel) => type * 4096 + channel;

  static final int _valueKey = TaggedValues.keyNumber('value');
  static final int _doitKey = TaggedValues.keyNumber('doit');

  static WidgetOpHandler _vetoableOp(int slot) => _handlers[slot] ??= () {
        final gates = _gates[slot] ??= {};
        return (int id, Uint8List frame, int start, int end) {
          final gate = gates[id];
          if (gate == null) return false;
          gate._armed = TaggedValues.boolField(frame, start, _valueKey) ?? false;
          return true;
        };
      }();

  static WidgetOpHandler _verdictOp(int slot) => _handlers[slot] ??= () {
        final gates = _gates[slot] ??= {};
        return (int id, Uint8List frame, int start, int end) {
          final gate = gates[id];
          if (gate == null) return false;
          gate._resolveHead(TaggedValues.boolField(frame, start, _doitKey) ?? true);
          return true;
        };
      }();

  /// Verdict for an action that was applied optimistically rather than withheld — the path backends
  /// that never arm the gate (the embedded one) take.
  void Function(bool doit)? onUnmatched;

  /// False means no veto is possible: callers must take their fast path.
  bool get armed => _armed;

  /// Cleared on focus loss; Java re-arms per focus.
  set armed(bool value) {
    _armed = value;
    if (!value) cancelAll();
  }

  /// Listens for the answers to widget [id], of type number [type].
  void attach(int type, int id) {
    detach();
    _id = id;
    _type = type;
    if (_optimistic) {
      _armed = true;
      _verdictChannel = _verdict;
    } else {
      _vetoableChannel = _vetoable;
      final slot = _slot(type, _vetoableChannel);
      EquoCommService.onWidgetOp(type, _vetoableChannel, _vetoableOp(slot));
      _gates[slot]![id] = this;
      _verdictChannel = _verdict;
    }
    final slot = _slot(type, _verdictChannel);
    EquoCommService.onWidgetOp(type, _verdictChannel, _verdictOp(slot));
    _gates[slot]![id] = this;
    // What Java answered before this gate listened applies now.
    EquoCommService.replayWidgetOps(id);
  }

  void detach() {
    final id = _id;
    if (id != null) {
      for (final channel in [_vetoableChannel, _verdictChannel]) {
        final gates = _gates[_slot(_type, channel)];
        if (gates != null && identical(gates[id], this)) gates.remove(id);
      }
    }
    _id = null;
    _type = -1;
    _vetoableChannel = -1;
    _verdictChannel = -1;
    cancelAll();
  }

  /// Registers a withheld action. [onVerdict] runs exactly once — with Java's answer, or `true` on
  /// [timeout]. Call before sending the event so queue order matches wire order.
  void propose(void Function(bool doit) onVerdict) {
    final early = _earlyVerdict;
    if (early != null) {
      _earlyVerdict = null;
      onVerdict(early);
      return;
    }
    final proposal = _Proposal(onVerdict);
    _queue.add(proposal);
    proposal.timer = Timer(timeout, () => _failOpen(proposal));
  }

  /// Abandons every held action without committing it.
  void cancelAll() {
    for (final proposal in _queue) {
      proposal.timer?.cancel();
    }
    _queue.clear();
    _earlyVerdict = null;
  }

  void _resolveHead(bool doit) {
    if (_queue.isEmpty) {
      // The Display forwards a keystroke to Java before the focused widget's text pipeline has
      // produced the edit to hold, so an armed gate's verdict can beat its own proposal. Keep it
      // for that proposal; dropping it made every keystroke wait out the fail-open timer.
      if (_armed && !_optimistic) {
        _earlyVerdict = doit;
      } else {
        onUnmatched?.call(doit);
      }
      return;
    }
    final head = _queue.removeAt(0);
    head.timer?.cancel();
    // Exactly one entry per verdict: a spent head consumes the late answer to the proposal its
    // timer already committed, instead of letting it resolve the proposal behind it.
    if (head.spent) return;
    head.onVerdict(doit);
  }

  void _failOpen(_Proposal proposal) {
    proposal.timer = null;
    if (proposal.spent || !_queue.contains(proposal)) return;
    proposal.spent = true;
    // Java isn't answering: back to the fast path until the next focus re-arms us. Set directly —
    // the `armed` setter would drop the spent entry needed to absorb this proposal's late verdict.
    if (!_optimistic) _armed = false;
    proposal.onVerdict(true);
  }
}

class _Proposal {
  _Proposal(this.onVerdict);

  final void Function(bool doit) onVerdict;
  Timer? timer;

  /// Committed by the fail-open timer; stays queued to absorb its own late verdict.
  bool spent = false;
}
