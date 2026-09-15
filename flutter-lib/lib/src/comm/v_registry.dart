/// Where a widget's state lives.
///
/// One object per widget, for as long as that widget exists, independent of whether anything is
/// currently rendering it. Every reference — a parent's child list, an item's control, a shell in
/// the display — resolves to that object, so an update reaches all of them or none.
///
/// The alternative, and what this replaces, is a value owned by the Flutter `State` that renders it.
/// That couples two things with different lifetimes: a value has to keep receiving while nothing
/// renders it, and a widget has to stop being told when it goes away. One object doing both is why a
/// hidden subtree loses its state, and why a widget reachable from two places ends up with two
/// values and one delivery.
///
/// So the registry owns the value **and** its channel subscription for the value's whole life, and a
/// mounted widget is only an observer: it reads, it asks to be rebuilt, and it owns nothing.
library;

import 'dart:async';

import 'package:flutter/foundation.dart';

import '../gen/widget.dart';
import '../gen/widgets.dart';
import 'comm.dart';
import 'delivery_gate.dart';

/// What one delivery did to a value, as much as is knowable from the delivery itself.
///
/// Which is: the names a partial delivery carried, and nothing at all for a whole one — a whole
/// widget says what it is, never what moved. There is no "before" to offer either way, because a
/// value is changed in place rather than replaced; that is what keeps every reference to a widget
/// pointing at the widget.
class VChange {
  const VChange({this.changed});

  /// The properties a partial delivery named, or null when the whole widget arrived.
  final Set<String>? changed;

  /// Whether this delivery could have touched [property].
  bool touches(String property) => changed == null || changed!.contains(property);

  /// Whether this delivery named nothing outside [properties] - a delivery about one thing, which
  /// a listener can then handle on its own instead of rebuilding for it.
  bool isOnly(Set<String> properties) =>
      changed != null && changed!.every(properties.contains);
}

/// The state of one widget, and everyone waiting to hear about it.
class VNode {
  VNode(this.value, {this.awaitingValue = false});


  /// The one instance for this widget, for as long as the widget exists. Never reassigned: what
  /// arrives is copied into it, so a list holding this object is holding the widget rather than a
  /// description of it that was true once.
  final VWidget value;

  /// True while [value] is a stand-in: something asked to hear about this widget before the widget
  /// arrived, and that interest has to survive until it does.
  bool awaitingValue;

  /// Mounted widgets waiting to be rebuilt. Attaching and detaching touches only this — never the
  /// subscription, which is why an offstage or disposed widget does not stop the value updating.
  final List<void Function(VChange change)> listeners = [];

  /// The channels this value refers to, as of the last thing applied to it. Kept so the next
  /// arrival can be compared against it: what it no longer mentions is what it let go of.
  Set<String> references = const {};

  /// The channel subscription, held for as long as the value is registered.
  Object? subscription;
}

class VRegistry {
  VRegistry();

  /// The client's registry. One per client, because widget identity is a fact about the client.
  static final VRegistry instance = VRegistry();

  final Map<String, VNode> _nodes = {};

  /// The channels being walked right now, so a reference leading back to something already on the
  /// path stops there instead of going round forever.
  final Set<String> _adopting = {};

  /// Which held values refer to each channel. A widget is reachable while this is not empty, and a
  /// widget reachable from two places — a tab body that is both a child of its folder and the
  /// control of its item — survives one of them letting go.
  ///
  /// Also which way to look when a widget nothing renders changes: see [_notify].
  final Map<String, Set<String>> _referrers = {};

  /// What each holder that is not itself a registered value refers to. See [holds].
  final Map<String, Set<String>> _holdings = {};

  /// One per channel anything has asked to be notified about. See [changesOn].
  final Map<String, _ChannelTick> _ticks = {};

  /// Channels an ask is already scheduled for, so several references to one widget in a single
  /// payload - a control that is both a child of its parent and the content of an item - cost one
  /// request rather than one each.
  final Set<String> _asking = {};

  /// How many widgets the client is holding state for. Watched by tests: values now outlive the
  /// widgets that render them, so this growing without bound is the failure mode to guard against.
  int get size => _nodes.values.where((node) => !node.awaitingValue).length;

  /// The channel a value is addressed by, and the key everything here is stored under.
  static String channelOf(VWidget value) => '${value.swt}/${value.id}';

  /// Whether two values are about the same widget. Identity is the pair that addresses it, which is
  /// all a value named rather than described carries.
  static bool isSameWidget(VWidget a, VWidget b) => a.id == b.id && a.swt == b.swt;

  /// The one instance for this widget, registering [value] if it is the first to arrive.
  ///
  /// Returns whatever is already held when the widget is known, so a caller that built a second
  /// object from a payload can drop it and take this one instead. That substitution is the whole
  /// point: it is what stops a parent's copy of a child from being a different object.
  ///
  /// Dropping the object is not dropping what it carried: an arrival written later than the state
  /// held is the newest there is, and is adopted like any other. Answering with the held object
  /// and reading nothing off the arrival loses a description the sender counted as delivered —
  /// and with it the write stamp the next update is computed from, so that update fits nothing
  /// and has to be asked for again. Which of two copies is newer is [_adopt]'s question, asked
  /// the same way here as for a widget arriving inside another.
  VWidget register(VWidget value) {
    final channel = channelOf(value);
    final node = _nodes[channel];
    if (node != null && !node.awaitingValue) return _takeNewerOf(channel, node, value);

    if (node == null) {
      // Assigned before subscribing: a payload held for this channel is replayed on subscription,
      // and it has to find the entry it is about.
      final fresh = VNode(value);
      _nodes[channel] = fresh;
      fresh.subscription = _subscribe(channel);
    } else {
      // Something asked to hear about this widget before it arrived, and what it has been waiting
      // on is a stand-in of the wrong type to copy a real widget into. The entry is rebuilt around
      // the value that turned up, carrying the interest over; nothing can be holding the stand-in,
      // which is the difference between this and every other arrival.
      final fresh = VNode(value)..listeners.addAll(node.listeners);
      _nodes[channel] = fresh;
      fresh.subscription = node.subscription ?? _subscribe(channel);
    }
    // The stamp comes with the value, and recording it is what makes the next update mergeable.
    // Java counts a widget written inside an ancestor's payload as delivered, so it computes that
    // widget's next update against this state; a client that had not noticed would refuse it and
    // ask for the whole widget back.
    deliveryGate.applied(channel, value.seq);
    _adoptSubtreeOf(channel, value);
    _reconcile(channel, _nodes[channel]!, value.seq);
    return value;
  }

  /// Whether [channel] is known only as a name: something referred to this widget before it had
  /// ever been described, and the answer to that request has not come back yet. What is held for it
  /// until then is an identity stub, which is nothing to render.
  bool isAwaiting(String channel) => _nodes[channel]?.awaitingValue ?? false;

  /// The value held for [channel], or null when the widget is unknown here.
  VWidget? valueOn(String channel) {
    final node = _nodes[channel];
    return (node == null || node.awaitingValue) ? null : node.value;
  }

  /// Applies an arriving frame, and tells whoever is listening.
  ///
  /// The decision of what a frame means is the gate's, unchanged from where it sat before: a whole
  /// frame replaces, a partial one that fits merges, one that does not fit asks for the widget
  /// again rather than guessing.
  void apply(String channel, Map<String, dynamic> frame) {
    var node = _nodes[channel];
    if (node == null) return;

    switch (deliveryGate.decide(channel, frame)) {
      case FrameAction.replace:
        final whole = mapWidgetValue(frame);
        if (node.awaitingValue && !isSameWidget(node.value, whole)) {
          // A stand-in for a widget nothing had described yet: it is the wrong type to copy a
          // widget into, and nothing can be pointing at it, so the entry is rebuilt around what
          // arrived. A shell left by an unresolved reference is not this case - that one is this
          // widget, already held by whatever named it, and is filled in below.
          node = VNode(whole)
            ..subscription = node.subscription
            ..listeners.addAll(node.listeners);
          _nodes[channel] = node;
        } else {
          // Copied into the object, not swapped for it. Every reference to this widget — a parent's
          // child list, an item's control, a layout delegate holding it — points at this object, and
          // replacing it would leave all of them describing the widget as it used to be. That is the
          // same defect as a parent's private copy, arriving from the other direction.
          node.value.copyFrom(whole);
        }
        node.value.seq = DeliveryGate.seqOf(frame);
        node.awaitingValue = false;
        deliveryGate.applied(channel, node.value.seq);
        // Before anyone is told: a listener that rebuilds reads its children through this value,
        // and a half-walked subtree would be part real widgets and part copies of them.
        _adoptSubtreeOf(channel, node.value);
        _reconcile(channel, node, node.value.seq);
        _notify(channel, node, const VChange());
      case FrameAction.merge:
        node.value.mergeJson(frame);
        node.value.seq = DeliveryGate.seqOf(frame);
        deliveryGate.applied(channel, node.value.seq);
        _adoptSubtreeOf(channel, node.value);
        _reconcile(channel, node, node.value.seq);
        _notify(channel, node, VChange(changed: _namesIn(frame)));
      case FrameAction.recover:
        // An update that does not fit what is held. Asking for the widget again is the safe answer,
        // but never the expected one: it means an update was lost, reordered, or computed from a
        // state this side never had. Said out loud so that it is noticed rather than absorbed.
        print('[delivery] update does not fit $channel '
            '(held ${node.value.seq}, base ${frame[kBase]}): asking for it again');
        EquoCommService.sendPayload(widgetRefreshChannel, '${node.value.id}');
      case FrameAction.ignore:
        // Nothing here changes - the frame describes this widget as it used to be. But a whole
        // frame carries every widget beneath it, and one of those can be a widget nothing else has
        // ever described: the sender counted it delivered inside this payload, and from then on
        // names it rather than describing it. Dropping the payload whole drops that description
        // too, and the reference that follows resolves to nothing.
        //
        // So the subtree is still offered. Each widget in it is judged on its own write stamp, the
        // same as on any other path, so an older copy of something already held is refused there
        // rather than here.
        if (!frame.containsKey(kChangedKeys)) {
          _adoptSubtreeOf(channel, mapWidgetValue(frame));
        }
    }
  }

  /// What [holder] refers to now, for a holder that is not a registered value itself.
  ///
  /// The display is one: it holds the shells and the popup menus, and is a device rather than a
  /// widget, so it has no value here to read references out of. Calling this again with a different
  /// set is how it lets go of what is no longer in it.
  ///
  /// No write stamp, and none is wanted. The stamp exists to tell a disposal from a widget that
  /// moved, and a shell cannot move: it belongs to one display for its whole life, so a shell the
  /// display stops carrying is a shell that closed.
  void holds(String holder, Iterable<VWidget> values) {
    final after = values.map(channelOf).toSet();
    final before = _holdings[holder] ?? const <String>{};
    _holdings[holder] = after;
    _applyReferenceChange(holder, before, after, null);
  }

  /// Notifies whenever any of [channels] is told anything.
  ///
  /// For code that depends on a widget's state without being that widget. A composite lays its
  /// children out from the bounds it finds on them, and a child's own update changes those bounds
  /// with the composite never hearing about it — nothing rebuilt it, and nothing had to. This is
  /// how such a layout asks to be re-run.
  Listenable changesOn(Iterable<String> channels) {
    final ticks = <Listenable>[
      for (final channel in channels) _ticks.putIfAbsent(channel, _ChannelTick.new),
    ];
    return Listenable.merge(ticks);
  }

  /// Asks to be rebuilt when [channel]'s value changes. Safe to call for a widget that has not
  /// arrived yet — the interest is recorded and honoured once it does.
  void watch(String channel, void Function(VChange change) listener) {
    _nodes
        .putIfAbsent(channel, () => VNode(VWidget.empty(), awaitingValue: true))
        .listeners
        .add(listener);
  }

  void unwatch(String channel, void Function(VChange change) listener) {
    final node = _nodes[channel];
    if (node == null) return;
    node.listeners.remove(listener);
    // An entry that only ever existed to record the interest, and nothing is interested any more.
    // Leaving it would accumulate one empty entry per widget that was watched before it arrived.
    if (node.awaitingValue && node.listeners.isEmpty) _nodes.remove(channel);
  }

  /// Forgets a widget: its state, its listeners and its subscription.
  ///
  /// Called when the widget is gone — not when it stops being rendered. That distinction is the
  /// reason this exists at all.
  void evict(String channel) {
    final node = _nodes.remove(channel);
    if (node == null) return;
    // The overlay a widget is drawn on goes with the widget. Every control mounts a GC keyed by its
    // own id, whether or not anything ever draws on it, and nothing refers to that GC - so it is
    // the one value with no holder to let go of it.
    if (node.value.swt != 'GC') evict('GC/${node.value.id}');
    final subscription = node.subscription;
    if (subscription != null) EquoCommService.remove(channel, subscription);
    node.listeners.clear();
    _ticks.remove(channel)?.dispose();
    deliveryGate.forget(channel);
  }

  /// Drops everything. For a fresh client, and between tests.
  void clear() {
    for (final channel in _nodes.keys.toList()) {
      evict(channel);
    }
    _nodes.clear();
    _adopting.clear();
    _referrers.clear();
    _holdings.clear();
    for (final tick in _ticks.values) {
      tick.dispose();
    }
    _ticks.clear();
  }

  Object? _subscribe(String channel) => EquoCommService.onRaw(channel, (payload) {
        if (payload is Map) apply(channel, Map<String, dynamic>.from(payload));
      });

  void _adoptSubtreeOf(String channel, VWidget value) {
    if (!_adopting.add(channel)) return;
    try {
      value.adoptChildren(_adopt);
    } finally {
      _adopting.remove(channel);
    }
  }

  /// Resolves one widget arriving inside another to the object that should be held for it.
  ///
  /// A parent's payload carries a copy of every widget beneath it, written when the parent was
  /// written, and [VWidget.seq] stamps that moment. So an older copy is a description of the past —
  /// a parent handing its children the state they had when it was last described, which is the
  /// whole of the stale-subtree problem — and a newer one is Java re-describing the subtree, which
  /// is the newest information there is and has to be taken.
  VWidget _adopt(VWidget incoming) {
    final channel = channelOf(incoming);
    final node = _nodes[channel];

    // A name, not a description: the sender knows this widget was already delivered and has not
    // changed, so it sent only which one it is. The answer is the object already held.
    if (incoming.isReference) {
      final held = node == null || node.awaitingValue ? null : node.value;
      if (held != null) return held;

      // Named as known, and not known here. The two sides disagree about what the client is
      // holding, and both are right: the sender did deliver this widget once, and the client did
      // let go of it when the last thing describing it stopped carrying it - a tree item under a
      // node that collapsed, a tab body in a stack that was minimised.
      //
      // So it is asked for, and this object becomes where the answer goes. Whatever named it - a
      // tree's item list, a folder's tabs - is holding this exact object and will go on holding it,
      // so filling it in when the widget arrives is what puts the content where it is being drawn.
      // Handing back anything else would leave the list pointing at a shell that never gains
      // anything: the row that renders with no text in it.
      //
      // Marked as awaiting until then, so nothing reads the empty shell as the widget's state.
      final waiting = _nodes.putIfAbsent(channel, () => VNode(incoming, awaitingValue: true));
      waiting.subscription ??= _subscribe(channel);
      _askUnlessItArrives(channel, incoming.id);
      return waiting.value;
    }

    if (node == null || node.awaitingValue) return register(incoming);

    return _takeNewerOf(channel, node, incoming);
  }

  /// The object to hold for [channel], given one already held and one that just turned up.
  ///
  /// The held object always wins the identity - everything pointing at this widget points at it,
  /// and swapping it would leave those references describing the widget as it used to be. What it
  /// does not always win is the content: a copy written later than the state held is the newest
  /// there is, and the sender counts a widget it wrote as delivered, so reading nothing off it
  /// loses that description and the write stamp the next update will be computed from. That update
  /// then fits nothing and has to be asked for.
  ///
  /// One answer for both ways a widget can turn up - carried inside another's payload, or handed
  /// over by a holder registering what it carries - because it is the same question either way.
  VWidget _takeNewerOf(String channel, VNode node, VWidget incoming) {
    final held = node.value;
    if (identical(held, incoming) || incoming.seq <= held.seq) return held;

    held.copyFrom(incoming);
    held.seq = incoming.seq;
    deliveryGate.applied(channel, held.seq);
    _adoptSubtreeOf(channel, held);
    _reconcile(channel, node, held.seq);
    _notify(channel, node, const VChange());
    return held;
  }

  /// Asks for [channel]'s widget, unless subscribing to it just produced it.
  ///
  /// Subscribing replays whatever arrived on the channel before anything was listening, and that
  /// replay lands in a microtask - so the widget can already be on its way at the moment it looks
  /// missing. Deferring by one microtask is what tells the two apart.
  void _askUnlessItArrives(String channel, int id) {
    if (!_asking.add(channel)) return;
    scheduleMicrotask(() {
      _asking.remove(channel);
      if (!isAwaiting(channel)) return;
      // Nothing was held for it and nothing describes it: the sender named a widget this client
      // was never given. Recovering works, but it costs a round trip and should not be reachable -
      // said out loud so that it is noticed rather than absorbed.
      print('[delivery] unresolved reference $channel: asking for it');
      EquoCommService.sendPayload(widgetRefreshChannel, '$id');
    });
  }

  /// Notes what a node's value refers to now, and forgets whatever it just let go of.
  ///
  /// Java never says a widget was disposed; it simply stops carrying it. So a departure has to be
  /// read out of what arrives — a child no longer in its parent's list, an item no longer in its
  /// table — and [writtenAt] is when the value doing the dropping was written, which is what tells
  /// a disposal apart from a reparent still in flight.
  void _reconcile(String channel, VNode node, int writtenAt) {
    final before = node.references;
    final after = _referencesOf(node.value);
    node.references = after;
    _applyReferenceChange(channel, before, after, writtenAt);
  }

  void _applyReferenceChange(
      String holder, Set<String> before, Set<String> after, int? writtenAt) {
    for (final channel in after) {
      if (!before.contains(channel)) {
        (_referrers[channel] ??= <String>{}).add(holder);
      }
    }
    for (final channel in before) {
      if (!after.contains(channel)) _release(holder, channel, writtenAt);
    }
  }

  /// One reference to [channel] goes away. The widget goes with it when it was the last.
  void _release(String holder, String channel, int? writtenAt) {
    final holders = _referrers[channel];
    if (holders != null) {
      holders.remove(holder);
      if (holders.isNotEmpty) return;
      _referrers.remove(channel);
    }

    final node = _nodes[channel];
    if (node == null) return;
    // Described more recently than the value letting go of it: the widget moved, and the frame
    // dropping it is the old parent catching up rather than news of a disposal. Java writes one
    // stamp per write from one counter, so "newer" is a fact about the two writes, not a guess.
    if (writtenAt != null && !node.awaitingValue && node.value.seq > writtenAt) return;

    final held = node.references;
    evict(channel);
    for (final reference in held) {
      _release(channel, reference, writtenAt);
    }
  }

  /// The channels a value refers to directly. Handed the identity of each widget it holds, which
  /// is all this needs — the values themselves are already the ones the registry holds.
  Set<String> _referencesOf(VWidget value) {
    final channels = <String>{};
    value.adoptChildren((child) {
      channels.add(channelOf(child));
      return child;
    });
    return channels;
  }

  /// The properties a partial frame named. Empty rather than null when it named none, so a
  /// listener is told "this changed nothing you care about" instead of "anything may have changed".
  static Set<String> _namesIn(Map<String, dynamic> frame) {
    final names = frame[kChangedKeys];
    return names is List ? names.map((n) => '$n').toSet() : const <String>{};
  }

  void _notify(String channel, VNode node, VChange change) {
    _ticks[channel]?.tick();
    // Copied before iterating: a listener may unwatch itself while being told, and a widget
    // disposing in response to an update is an ordinary thing rather than an error.
    for (final listener in List<void Function(VChange)>.of(node.listeners)) {
      listener(change);
    }
    if (node.listeners.isEmpty) _notifyHolders(channel, {channel});
  }

  /// Tells whatever holds [channel] that something it draws has changed.
  ///
  /// A table row, a menu item, a scrollbar: an SWT widget in its own right, with its own channel
  /// and its own updates, but no [State] of its own — the thing above it draws it from its own
  /// list. Nobody listening is exactly what that looks like from here, so it needs no list of
  /// which widgets those are, and a widget that starts or stops being rendered separately needs
  /// nothing changed.
  ///
  /// This is what Java used to arrange by dirtying the ancestor, which re-sent the whole table to
  /// redraw one cell.
  void _notifyHolders(String channel, Set<String> seen) {
    for (final holder in _referrers[channel] ?? const <String>{}) {
      if (!seen.add(holder)) continue;
      final node = _nodes[holder];
      if (node == null) continue;
      _ticks[holder]?.tick();
      for (final listener in List<void Function(VChange)>.of(node.listeners)) {
        listener(const VChange());
      }
      // Still nobody: an item inside an item, or a subtree nothing is rendering yet.
      if (node.listeners.isEmpty) _notifyHolders(holder, seen);
    }
  }
}

/// Something to hang a listener on, per channel. Separate from [VNode.listeners] because it
/// outlives no widget and belongs to no widget: whoever holds it attaches and detaches on its own
/// schedule, which is what a layout delegate needs.
class _ChannelTick extends ChangeNotifier {
  void tick() => notifyListeners();
}
