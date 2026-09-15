/// Decides whether an arriving frame can be applied to the state the client already holds.
///
/// A full frame describes a widget completely, so it can always be applied and a stale one can
/// simply be dropped. A partial frame cannot: it describes a *change* from one particular state, so
/// applying it to any other state is silent corruption, and dropping it loses the change for good.
/// Neither of the things you can do with a stale snapshot works on a partial update.
///
/// So a partial frame names the state it was computed from, in `_b`, and this gate compares that
/// against what the widget holds. On a mismatch it neither applies nor discards: it asks for the
/// widget to be re-sent whole, which is correct whatever the cause — a frame lost, delivered twice,
/// or overtaken by one from another path.
///
/// [recoveries] is the reason this exists as a countable thing rather than a `try/catch`. Recovery
/// is a net, not a strategy: the delivery design is supposed to make a mismatch impossible, so the
/// suites assert this counter is zero. A recovery firing is a defect that has already been contained
/// — the point is to hear about it, not to survive it quietly.
library;

/// What to do with a frame.
enum FrameAction {
  /// Complete state: take it as the widget's new value.
  replace,

  /// A change computed from the state the widget holds: merge it in.
  merge,

  /// Not applicable to the state the widget holds. Ask for the whole widget.
  recover,

  /// Already applied. Nothing to do, and nothing wrong.
  ignore,
}

/// Channel Java listens on for "send me this widget whole" requests - the one way back when an
/// update cannot be applied to the state held here.
const String widgetRefreshChannel = 'swt.evolve.widget.refresh';

/// Protocol keys. Underscore-prefixed so they cannot collide with a widget property name.
const String kChangedKeys = '_d';
const String kBase = '_b';
const String kSeq = '_s';

/// The client's gate. One per client, because "what state does this widget hold" is a fact about
/// the client as a whole rather than about any widget that happens to be mounted.
final DeliveryGate deliveryGate = DeliveryGate();

class DeliveryGate {
  /// Write stamp of the state currently held, per channel.
  final Map<String, int> _held = <String, int>{};

  /// Mismatches that had to be repaired by re-sending a widget. Expected to stay at zero.
  int recoveries = 0;

  /// Frames that arrived twice and were dropped as already-applied. Wasteful, not wrong.
  int duplicates = 0;

  /// What should be done with [frame] on [channel], given what that channel already holds.
  ///
  /// Recording the outcome is the caller's job ([applied]), because a frame that fails to decode or
  /// that the caller declines to apply must not advance what we believe is held.
  FrameAction decide(String channel, Map<String, dynamic> frame) {
    final int? held = _held[channel];
    final int seq = _intOf(frame[kSeq]) ?? 0;

    if (!frame.containsKey(kChangedKeys)) {
      // A whole widget, but not necessarily news of one. Java stamps every write from one counter,
      // so a description written no later than the state already held describes the past: a frame
      // buffered before anything was listening and replayed on arrival, or the same one delivered
      // twice. Taking it would rewind the widget. This is the rule a value arriving inside its
      // parent is held to as well - what decides is when it was written, never where it came from.
      if (held != null && seq != 0 && seq <= held) {
        duplicates++;
        return FrameAction.ignore;
      }
      return FrameAction.replace;
    }

    // A partial update for a widget we have never seen whole. There is nothing to merge into, and
    // guessing at a starting state is exactly the corruption this gate exists to prevent.
    if (held == null) {
      recoveries++;
      return FrameAction.recover;
    }

    // A change written no later than the state already held, which is the same rule a whole frame
    // is judged by above and for the same reason: the counter is one, so the held state was written
    // afterwards and already describes the widget more recently. Equal is that frame delivered
    // twice; older is one that lost its race - a frame batched to the end of a flush while a whole
    // description of the same widget went straight out, and a widget written twice in a flush is
    // ordinary. Neither is news, and neither is a reason to ask for the widget again.
    if (seq != 0 && seq <= held) {
      duplicates++;
      return FrameAction.ignore;
    }

    final int? base = _intOf(frame[kBase]);
    if (base != null && base == held) return FrameAction.merge;

    // Computed from a state this widget does not hold: something was lost, reordered, or arrived
    // from a path that had a different idea of the widget's state.
    recoveries++;
    return FrameAction.recover;
  }

  /// Records that [channel] now holds the state stamped [seq]. Call only after applying.
  void applied(String channel, int seq) {
    _held[channel] = seq;
  }

  /// Forgets [channel] — the widget is gone, or its state must be re-established from a full frame.
  void forget(String channel) {
    _held.remove(channel);
  }

  /// The write stamp of the state [channel] holds, or null if it holds none.
  int? heldSeq(String channel) => _held[channel];

  /// The stamp a frame carries — the state it leaves the widget in once applied.
  ///
  /// Read from the frame rather than from the widget afterwards: a partial update changes the
  /// properties it names and nothing else, so the widget's own stamp is still the one from before
  /// it. Recording that would tell the next update to fit a state that has already moved on.
  static int seqOf(Map<String, dynamic> frame) => _intOf(frame[kSeq]) ?? 0;

  void reset() {
    _held.clear();
    recoveries = 0;
    duplicates = 0;
  }

  /// Numbers survive JSON as int or double depending on how they were written.
  static int? _intOf(Object? value) {
    if (value is int) return value;
    if (value is double) return value.toInt();
    return null;
  }
}
