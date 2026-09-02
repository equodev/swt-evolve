import 'dart:async';
import 'dart:convert';

import '../../comm/comm.dart';

/// Client half of the `Event.doit` round trip: `{swt}/{id}/{gate}/vetoable` arms the gate when Java
/// has a listener that could veto; `{swt}/{id}/{gate}/verdict` carries the answer.
///
/// Verdicts resolve the queue head rather than naming a proposal — `Event` is upstream SWT with no
/// field for a correlation id, and the per-control channel is already ordered by `Display.asyncExec`.
///
/// The gate never knows what the default action is; each call site supplies it to [propose].
class VetoGate {
  VetoGate(this.gate, {this.timeout = const Duration(milliseconds: 400)})
      : _verdictOnlyChannel = null;

  /// For an action that cannot be withheld, so it happens and is undone on a veto (`DragDetect`:
  /// Flutter's gesture arena commits before any listener could run). No arming leg, one channel.
  VetoGate.optimistic(String verdictChannel,
      {this.timeout = const Duration(milliseconds: 400)})
      : gate = verdictChannel,
        _verdictOnlyChannel = verdictChannel,
        _armed = true;

  final String gate;

  /// Fail-open deadline: a lost verdict must never swallow the user's input for good.
  final Duration timeout;

  final String? _verdictOnlyChannel;
  bool _armed = false;
  bool? _earlyVerdict;
  final List<_Proposal> _queue = [];
  Object? _vetoableToken;
  Object? _verdictToken;
  String? _vetoableChannel;
  String? _verdictChannel;

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

  void attach(String swt, int id) {
    detach();
    if (_verdictOnlyChannel != null) {
      _armed = true;
      _verdictChannel = '$swt/$id/$_verdictOnlyChannel';
    } else {
      _vetoableChannel = '$swt/$id/$gate/vetoable';
      _vetoableToken = EquoCommService.onRaw(
        _vetoableChannel!,
        (args) => _armed = _boolArg(args, 'value') ?? false,
      );
      _verdictChannel = '$swt/$id/$gate/verdict';
    }
    _verdictToken = EquoCommService.onRaw(
      _verdictChannel!,
      (args) => _resolveHead(_boolArg(args, 'doit') ?? true),
    );
  }

  void detach() {
    if (_vetoableChannel != null) {
      EquoCommService.remove(_vetoableChannel!, _vetoableToken);
    }
    if (_verdictChannel != null) {
      EquoCommService.remove(_verdictChannel!, _verdictToken);
    }
    _vetoableChannel = null;
    _verdictChannel = null;
    _vetoableToken = null;
    _verdictToken = null;
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
      if (_armed && _verdictOnlyChannel == null) {
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
    if (_verdictOnlyChannel == null) _armed = false;
    proposal.onVerdict(true);
  }

  static bool? _boolArg(dynamic args, String key) {
    final decoded = args is String ? jsonDecode(args) : args;
    return decoded is Map ? decoded[key] as bool? : null;
  }
}

class _Proposal {
  _Proposal(this.onVerdict);

  final void Function(bool doit) onVerdict;
  Timer? timer;

  /// Committed by the fail-open timer; stays queued to absorb its own late verdict.
  bool spent = false;
}
