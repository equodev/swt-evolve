/// Decides whether an arriving frame can be applied to the state the client already holds.
///
/// A full frame describes a widget completely, so it can always be applied and a stale one can
/// simply be dropped. A partial frame cannot: it describes a *change* from one particular state, so
/// applying it to any other state is silent corruption, and dropping it loses the change for good.
/// Neither of the things you can do with a stale snapshot works on a partial update.
///
/// So a change names the state it was computed from, its base, and this gate compares that
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

  /// What should be done with a whole description of [channel] stamped [seq]: Java stamps every
  /// write from one counter, so one written no later than the state held describes the past.
  FrameAction decideWhole(String channel, int seq) {
    final int? held = _held[channel];
    if (held != null && seq <= held) {
      duplicates++;
      return FrameAction.ignore;
    }
    return FrameAction.replace;
  }

  /// What should be done with a change to [channel] stamped [seq] and computed from [base]: merged
  /// only into exactly the state it was computed from.
  FrameAction decideChange(String channel, int seq, int base) {
    final int? held = _held[channel];
    if (held == null) {
      recoveries++;
      return FrameAction.recover;
    }
    if (seq <= held) {
      duplicates++;
      return FrameAction.ignore;
    }
    if (base == held) return FrameAction.merge;
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

  void reset() {
    _held.clear();
    recoveries = 0;
    duplicates = 0;
  }
}
