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
/// So the registry owns the value for the value's whole life, and a mounted widget is only an
/// observer: it reads, it asks to be rebuilt, and it owns nothing.
library;

import 'dart:async';
import 'dart:collection';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';

import '../gen/widget.dart';
import '../gen/display.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/menu.dart';
import '../gen/widgets.dart';
import 'comm.dart';
import 'comm_frame.dart';
import 'delivery_gate.dart';
import 'widget_wire.dart';

/// What one delivery did to a value, as much as is knowable from the delivery itself.
///
/// Which is: the properties a change named, by [WireKey] number, and nothing at all for a whole
/// description - a whole widget says what it is, never what moved. There is no "before" to offer
/// either way, because a value is changed in place rather than replaced; that is what keeps every
/// reference to a widget pointing at the widget.
class VChange {
  const VChange({this.keys}) : ofHeld = false;

  /// Not this widget's own change: a widget it holds changed, one that has no state of its own and
  /// that this widget may draw.
  const VChange.ofHeld()
      : keys = null,
        ofHeld = true;

  /// Whether a widget this one holds changed rather than this one; see [VChange.ofHeld].
  final bool ofHeld;

  /// The properties a change named, by number, or null for a whole description.
  final List<int>? keys;

  /// Whether this delivery could have touched the property numbered [key].
  bool touches(int key) => keys?.contains(key) ?? true;

  /// Whether this delivery named nothing outside [properties] - a delivery about one thing, which
  /// a listener can then handle on its own instead of rebuilding for it.
  bool isOnly(Set<int> properties) => keys?.every(properties.contains) ?? false;
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

  /// Mounted widgets waiting to be rebuilt. Attaching and detaching touches only this, which is
  /// why an offstage or disposed widget does not stop the value updating.
  final List<void Function(VChange change)> listeners = [];

  /// The channels this value refers to, as of the last thing applied to it. Kept so the next
  /// arrival can be compared against it: what it no longer mentions is what it let go of.
  Set<String> references = const {};

  /// True once something named this widget without describing it and Java was asked for it: the
  /// entry has to survive until the answer arrives, whoever stops watching it meanwhile.
  bool asked = false;
}

class VRegistry {
  VRegistry();

  /// The client's registry. One per client, because widget identity is a fact about the client.
  static final VRegistry instance = VRegistry();

  final _NodeMap _nodes = _NodeMap();

  /// The channels being walked right now, so a reference leading back to something already on the
  /// path stops there instead of going round forever.
  final Set<String> _adopting = {};

  /// Which held values refer to each channel. A widget is reachable while this is not empty, and a
  /// widget reachable from two places — a tab body that is both a child of its folder and the
  /// control of its item — survives one of them letting go.
  ///
  /// Also which way to look when a widget nothing renders changes: see [_notify].
  ///
  /// A channel's one holder is kept as that holder's channel, and a [Set] only once a second one
  /// refers to it: nearly every widget has a single parent.
  final Map<String, Object> _referrers = {};

  void _addReferrer(String channel, String holder) {
    final held = _referrers[channel];
    if (held == null) {
      _referrers[channel] = holder;
    } else if (held is String) {
      if (held != holder) _referrers[channel] = <String>{held, holder};
    } else {
      (held as Set<String>).add(holder);
    }
  }

  /// Takes [holder] off what refers to [channel]; true when something still does.
  bool _removeReferrer(String channel, String holder) {
    final held = _referrers[channel];
    if (held == null) return false;
    if (held is String) {
      if (held != holder) return true;
      _referrers.remove(channel);
      return false;
    }
    final holders = held as Set<String>;
    holders.remove(holder);
    if (holders.isNotEmpty) return true;
    _referrers.remove(channel);
    return false;
  }

  /// What refers to [channel], as a list taken now: a holder may let go while being told.
  List<String> _referrersOf(String channel) {
    final held = _referrers[channel];
    if (held == null) return const [];
    if (held is String) return [held];
    return List.of(held as Set<String>);
  }

  /// What each holder that is not itself a registered value refers to. See [holds].
  final Map<String, Set<String>> _holdings = {};

  /// One per channel anything has asked to be notified about. See [changesOn].
  /// Told when a delivery could have moved or resized the widget on that channel.
  final Map<String, _ChannelTick> _boundsTicks = {};

  /// Channels an ask is already scheduled for, so several references to one widget in a single
  /// payload - a control that is both a child of its parent and the content of an item - cost one
  /// request rather than one each.
  final Set<String> _asking = {};

  /// How many widgets the client is holding state for. Watched by tests: values now outlive the
  /// widgets that render them, so this growing without bound is the failure mode to guard against.
  int get size => _nodes.values.where((node) => !node.awaitingValue).length;

  /// The channel a value is addressed by, and the key everything here is stored under.
  static String channelOf(VWidget value) => '${value.swt}/${value.id}';

  static String _idOf(String channel) => channel.substring(channel.indexOf('/') + 1);

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
    // The stand-in an unresolved reference left, handed back by whatever was built from the name:
    // nothing has described the widget, so it is still awaited and still asked for.
    if (node != null && identical(node.value, value)) return value;

    if (node == null) {
      // Entered before listening: a payload held for this channel is replayed as soon as it is
      // listened on, and it has to find the entry it is about.
      _nodes[channel] = VNode(value);
    } else if (isSameWidget(node.value, value) && !value.isReference) {
      // A stand-in left by a reference: whatever named this widget holds that object (a parent's
      // child list), so the description is copied into it, as a whole record is. Swapping it would
      // leave the parent laying out a child with no state at all.
      return _fill(channel, node, value);
    } else {
      // Something asked to hear about this widget before it arrived, and what it has been waiting
      // on is a stand-in of the wrong type to copy a real widget into. The entry is rebuilt around
      // the value that turned up, carrying the interest over; nothing can be holding the stand-in,
      // which is the difference between this and every other arrival.
      _nodes[channel] = VNode(value)
        ..asked = node.asked
        ..listeners.addAll(node.listeners);
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
  /// Moves on whenever a channel starts or stops holding a value, or holds a different one: what
  /// [valueOn] answers for a channel stays the same until it does.
  int get epoch => _nodes.epoch;

  VWidget? valueOn(String channel) {
    final node = _nodes[channel];
    return (node == null || node.awaitingValue) ? null : node.value;
  }

  /// Per holder type, what its record is read into; see [onHolder].
  final Map<String, WireReadable Function()> _holderValues = {};
  final Map<String, List<void Function(int id, Object value)>> _holderListeners = {};

  /// Tells [listener] about every record of [type] - something that holds widgets without being one,
  /// such as the display - read into what [create] makes.
  ///
  /// Returns what stops it.
  void Function() onHolder<T extends WireReadable>(String type, T Function() create, void Function(int id, T value) listener) {
    _holderValues[type] = create;
    void told(int id, Object value) => listener(id, value as T);
    final listeners = _holderListeners[type] ??= [];
    listeners.add(told);
    // A holder's record can come before anything listens: the display's own arrives before the widget
    // that draws it is mounted. The last one per holder is kept, and a listener starts from it.
    for (final held in (_holderRecords[type] ?? const <int, Object>{}).entries) {
      told(held.key, held.value);
    }
    return () => listeners.remove(told);
  }

  /// The write stamp of the last record applied for each holder, by type and id.
  final Map<String, Map<int, int>> _holderStamps = {};

  /// The last record of each holder, by type and id; see [onHolder].
  final Map<String, Map<int, Object>> _holderRecords = {};

  /// What each holder's record is read into, before anything has asked to hear about it.
  static final Map<String, WireReadable Function()> _holderTypes = {
    'Display': VDisplay.new,
    'ShellPopups': VShellPopups.new,
  };

  /// The value held for the widget a frame names by [id], or null.
  VWidget? _heldById(int id, bool Function(VWidget value) accepts) {
    final node = _nodes.indexedOf(id, accepts)?.node;
    return node == null || node.awaitingValue ? null : node.value;
  }

  /// Applies one flush's binary frame (see Java's `WidgetWire`).
  ///
  /// Read twice. The first pass reads only the records' headers, and makes an entry for every widget
  /// the frame describes that nothing here holds yet: the frame names widgets by id, and a parent's
  /// record names children whose records come after it. The second reads every record's properties,
  /// in order, so each name resolves to the object that will be held for that widget.
  void applyWire(Uint8List body) {
    final r = WidgetWireReader(body)..resolve = _heldById;
    final count = r.u32();
    final kinds = List<int>.filled(count, 0);
    final starts = List<int>.filled(count, 0);
    final ends = List<int>.filled(count, 0);
    final ids = List<int>.filled(count, 0);
    final stamps = List<int>.filled(count, 0);
    final bases = List<int>.filled(count, 0);
    final types = List<String>.filled(count, '');
    final fresh = List<bool>.filled(count, false);
    var stamp = 0;
    for (var i = 0; i < count; i++) {
      final kind = kinds[i] = r.u8();
      final length = r.varint();
      final end = ends[i] = r.pos + length;
      switch (kind) {
        case wireWhole:
        case wireChange:
          final type = types[i] = r.type();
          final id = ids[i] = r.zigzag();
          stamp += r.varint();
          stamps[i] = stamp;
          if (kind == wireChange) bases[i] = stamp - r.varint();
          if (kind == wireWhole) fresh[i] = _enter(type, id);
        case wireImage:
          ids[i] = r.zigzag();
        case wireHolder:
          types[i] = r.type();
          ids[i] = r.zigzag();
          stamp += r.varint();
          stamps[i] = stamp;
      }
      starts[i] = r.pos;
      r.pos = end;
    }
    final outermost = _heldChangedInFrame == null;
    final heldChanged = _heldChangedInFrame ??= {};
    for (var i = 0; i < count; i++) {
      r.pos = starts[i];
      final end = ends[i];
      r.namingRecord = kinds[i] == wireWhole || kinds[i] == wireChange ? ids[i] : 0;
      try {
        switch (kinds[i]) {
          case wireWhole:
            _applyWhole(r, types[i], ids[i], stamps[i], end, fresh[i]);
          case wireChange:
            final held = _nodes.indexedOf(ids[i]);
            if (held != null && _isOfType(held.channel, types[i])) {
              _applyChange(r, held.channel, held.node, stamps[i], bases[i], end);
            } else {
              final channel = '${types[i]}/${ids[i]}';
              final node = _nodes[channel];
              if (node != null) _applyChange(r, channel, node, stamps[i], bases[i], end);
            }
          case wireImage:
            final image = r.imageFor(ids[i]);
            r.readProperties(image, end);
            image.isReference = false;
            rememberOrResolveImage(image);
          case wireHolder:
            _applyHolder(r, types[i], ids[i], stamps[i], end);
        }
      } catch (e) {
        // A record says how long it is, so one that cannot be read costs only its widget.
        if (kDebugMode) print('[wire] ${types[i]}/${ids[i]} could not be read ($e): asking for it whole');
        if (kinds[i] == wireWhole || kinds[i] == wireChange) {
          EquoCommService.sendPayload(widgetRefreshChannel, '${ids[i]}');
        }
      }
      r.pos = end;
      if (r.unresolved.isNotEmpty) {
        // Named, and neither held here nor described in the frame: the two sides disagree about what
        // this client holds. Ask for the widget, then for what named it, so the name resolves.
        for (final id in r.unresolved) {
          if (kDebugMode) print('[delivery] unresolved reference $id in ${types[i]}/${ids[i]}: asking for it');
          EquoCommService.sendPayload(widgetRefreshChannel, '$id');
        }
        EquoCommService.sendPayload(widgetRefreshChannel, '${ids[i]}');
        r.unresolved.clear();
      }
    }
    if (r.imagesNamedUnheld.isNotEmpty) _askForUnheldImages(r);
    if (!outermost) return;
    _heldChangedInFrame = null;
    // Seeded with every changed widget: one that is itself in the set tells its own holders.
    final seen = Set.of(heldChanged);
    for (final channel in heldChanged) {
      _notifyHolders(channel, seen);
    }
  }

  /// Asks for each widget that named an image this client neither held nor was given in the frame.
  /// Java counted that image as delivered when it was not; the request makes it describe the widget
  /// again with its images.
  void _askForUnheldImages(WidgetWireReader r) {
    final asked = <int>{};
    r.imagesNamedUnheld.forEach((image, widget) {
      if (widget == 0 || r.images[image]?.isReference != true || !asked.add(widget)) return;
      if (kDebugMode) print('[delivery] image $image named by $widget never arrived: asking for the widget');
      EquoCommService.sendPayload(widgetRefreshChannel, '$widget');
    });
    r.imagesNamedUnheld.clear();
  }

  /// The widgets the frame being applied changed that nobody listens to, whose holders are told once
  /// the whole frame is in: a table described with its items would otherwise hear once per item.
  Set<String>? _heldChangedInFrame;

  /// [wireRefKeys] as a flag per property number, which needs no hashing to test.
  static final List<bool> _refKeyFlags = () {
    final flags = List<bool>.filled(wireRefKeys.fold(0, (a, b) => a > b ? a : b) + 1, false);
    for (final key in wireRefKeys) {
      flags[key] = true;
    }
    return flags;
  }();

  static bool _isRefKey(int key) => key >= 0 && key < _refKeyFlags.length && _refKeyFlags[key];


  /// The channel of the widget of [type] a frame names by [id]: the string the entry is held under
  /// when there is one, whose hash is already known to every map keyed by it.
  String _channel(String type, int id) {
    final held = _nodes.indexedOf(id);
    return held != null && _isOfType(held.channel, type) ? held.channel : '$type/$id';
  }

  static bool _isOfType(String channel, String type) =>
      channel.length > type.length && channel.codeUnitAt(type.length) == 0x2F && channel.startsWith(type);

  /// Makes the entry for a widget the frame describes in full, when nothing here holds it. True when
  /// it did: the record is then read straight into the entry's value.
  bool _enter(String type, int id) {
    final channel = _channel(type, id);
    final node = _nodes[channel];
    if (node != null && !node.awaitingValue) return false;
    final value = newWidgetValue(type);
    if (value == null) return false;
    value.id = id;
    final entry = VNode(value);
    // Something asked to hear about this widget before it arrived; the interest carries over.
    if (node != null) {
      entry.listeners.addAll(node.listeners);
      entry.asked = node.asked;
    }
    _nodes.putIndexed(channel, entry, id);
    return true;
  }

  void _applyWhole(WidgetWireReader r, String type, int id, int stamp, int end, bool fresh) {
    final channel = _channel(type, id);
    final node = _nodes[channel];
    if (node == null) return;
    if (fresh) {
      r.readProperties(node.value, end);
    } else {
      final whole = newWidgetValue(type);
      if (whole == null) return;
      whole.id = id;
      r.readProperties(whole, end);
      if (deliveryGate.decideWhole(channel, stamp) != FrameAction.replace) return;
      // Copied into the held object, never swapped for it: everything that refers to this widget
      // refers to that object.
      node.value.copyFrom(whole);
    }
    node.value.seq = stamp;
    deliveryGate.applied(channel, stamp);
    _reconcile(channel, node, stamp);
    _notify(channel, node, const VChange());
  }

  void _applyChange(WidgetWireReader r, String channel, VNode node, int stamp, int base, int end) {
    switch (deliveryGate.decideChange(channel, stamp, base)) {
      case FrameAction.merge:
        final changed = <int>[];
        r.readProperties(node.value, end, changed);
        node.value.seq = stamp;
        deliveryGate.applied(channel, stamp);
        if (changed.any(_isRefKey)) _reconcile(channel, node, stamp);
        _notify(channel, node, VChange(keys: changed));
      case FrameAction.recover:
        EquoCommService.sendPayload(widgetRefreshChannel, '${node.value.id}');
      case FrameAction.replace:
      case FrameAction.ignore:
        break;
    }
  }

  void _applyHolder(WidgetWireReader r, String type, int id, int stamp, int end) {
    final create = _holderValues[type] ?? _holderTypes[type];
    if (create == null) return;
    // Written before the description already applied: the past, arriving late.
    final stamps = _holderStamps[type] ??= {};
    if ((stamps[id] ?? 0) >= stamp) return;
    stamps[id] = stamp;
    final value = create();
    r.readProperties(value, end);
    _tellHolder(type, id, value);
  }

  void _tellHolder(String type, int id, Object value) {
    (_holderRecords[type] ??= {})[id] = value;
    for (final listener in List.of(_holderListeners[type] ?? const <void Function(int, Object)>[])) {
      listener(id, value);
    }
  }

  /// A holder's record as it is handed on once read, for a test that describes it directly.
  @visibleForTesting
  void deliverHolderForTesting(String type, int id, Object value) => _tellHolder(type, id, value);

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

  /// Notifies whenever any of [channels] is told something that could have changed its bounds.
  ///
  /// Lets a layout that depends on its children's bounds re-run. Bounds only: a layout pass
  /// repaints up to the nearest repaint boundary.
  Listenable boundsChangesOn(Iterable<String> channels) {
    final ticks = <Listenable>[
      for (final channel in channels) _boundsTicks.putIfAbsent(channel, _ChannelTick.new),
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
    // One that was asked for is waiting on an answer something named it for: a widget that
    // rendered it before a move unmounting afterwards must not drop the answer on the floor.
    if (node.awaitingValue && node.listeners.isEmpty && !node.asked) {
      _nodes.remove(channel);
    }
  }

  /// Forgets a widget: its state and its listeners.
  ///
  /// Called when the widget is gone — not when it stops being rendered. That distinction is the
  /// reason this exists at all.
  void evict(String channel) {
    final node = _nodes.remove(channel);
    if (node == null) return;
    // The overlay a widget is drawn on goes with the widget. Every control mounts a GC keyed by its
    // own id, whether or not anything ever draws on it, and nothing refers to that GC - so it is
    // the one value with no holder to let go of it. An item is drawn by its control, on its GC.
    if (node.value is! VItem && node.value.swt != 'GC') evict('GC/${node.value.id}');
    node.listeners.clear();
    _boundsTicks.remove(channel)?.dispose();
    deliveryGate.forget(channel);
    // Frames held for the widget until it listened: it never will now.
    EquoCommService.forgetHeld(channel);
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
    _unheld.clear();
    for (final tick in _boundsTicks.values) {
      tick.dispose();
    }
    _boundsTicks.clear();
  }

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
      waiting.asked = true;
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
    return _fill(channel, node, incoming);
  }

  /// Copies [incoming] into the object held for [channel] and tells whoever is listening. The
  /// subtree is adopted before anyone is told, for the reason apply() gives.
  VWidget _fill(String channel, VNode node, VWidget incoming) {
    final held = node.value..copyFrom(incoming);
    held.seq = incoming.seq;
    node.awaitingValue = false;
    deliveryGate.applied(channel, held.seq);
    _adoptSubtreeOf(channel, held);
    _reconcile(channel, node, held.seq);
    _notify(channel, node, const VChange());
    return held;
  }

  /// Asks for [channel]'s widget, unless the delivery that named it goes on to describe it.
  ///
  /// A record can name a widget that a later record of the same delivery describes, so the widget
  /// can already be on its way at the moment it looks missing. Deferring by one microtask, past the
  /// rest of the delivery, is what tells the two apart.
  void _askUnlessItArrives(String channel, int id) {
    if (!_asking.add(channel)) return;
    scheduleMicrotask(() {
      _asking.remove(channel);
      if (!isAwaiting(channel)) return;
      // Nothing was held for it and nothing describes it: the sender named a widget this client
      // was never given. Recovering works, but it costs a round trip and should not be reachable -
      // said out loud in debug builds so that it is noticed rather than absorbed; a release build
      // stays quiet, since the app's console is the user's.
      if (kDebugMode) print('[delivery] unresolved reference $channel: asking for it');
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
        _addReferrer(channel, holder);
      }
    }
    for (final channel in before) {
      if (!after.contains(channel)) _release(holder, channel, writtenAt);
    }
  }

  /// One reference to [channel] goes away. The widget goes with it when it was the last - once
  /// every frame received so far is applied, and only if nothing has taken it by then.
  ///
  /// A widget that moves is dropped by its old parent and taken by its new one, and the two frames
  /// can arrive in either order in one run: applied in between, it is held by nothing.
  void _release(String holder, String channel, int? writtenAt) {
    if (!_letGo(holder, channel, writtenAt)) return;
    if (_unheld.isEmpty) EquoCommBase.whenApplied(_evictUnheld);
    _unheld[channel] = writtenAt;
  }

  /// Widgets that lost their last holder, and when the value that let go of each was written.
  final Map<String, int?> _unheld = {};

  void _evictUnheld() {
    final unheld = Map.of(_unheld);
    _unheld.clear();
    unheld.forEach(_evictIfUnheld);
  }

  void _evictIfUnheld(String channel, int? writtenAt) {
    if (_referrers.containsKey(channel)) return;
    final node = _nodes[channel];
    if (node == null || _movedSince(node, writtenAt)) return;
    final held = node.references;
    evict(channel);
    for (final reference in held) {
      if (_letGo(channel, reference, writtenAt)) _evictIfUnheld(reference, writtenAt);
    }
  }

  /// Takes [holder] off [channel]'s holders; whether that left [channel] held by nothing.
  bool _letGo(String holder, String channel, int? writtenAt) {
    if (_removeReferrer(channel, holder)) return false;
    final node = _nodes[channel];
    return node != null && !_movedSince(node, writtenAt);
  }

  // Described more recently than the value letting go of it: the widget moved, and the frame
  // dropping it is the old parent catching up rather than news of a disposal. Java writes one
  // stamp per write from one counter, so "newer" is a fact about the two writes, not a guess.
  static bool _movedSince(VNode node, int? writtenAt) =>
      writtenAt != null && !node.awaitingValue && node.value.seq > writtenAt;

  /// The channels a value refers to directly. Handed the identity of each widget it holds, which
  /// is all this needs — the values themselves are already the ones the registry holds.
  Set<String> _referencesOf(VWidget value) {
    Set<String>? channels;
    value.adoptChildren((child) {
      (channels ??= <String>{}).add(_heldChannelOf(child));
      return child;
    });
    return channels ?? const <String>{};
  }

  /// [child]'s channel, as the string its entry is held under when it is the held value: that one
  /// is already hashed, and compares by identity, where a new string would be built and hashed.
  String _heldChannelOf(VWidget child) {
    final held = _nodes.indexedOf(child.id);
    if (held != null && identical(held.node.value, child)) return held.channel;
    return channelOf(child);
  }

  void _notify(String channel, VNode node, VChange change) {
    if (change.touches(WireKey.k_Control_bounds) || change.touches(WireKey.k_Caret_bounds)) {
      _boundsTicks[channel]?.tick();
    }
    // Copied before iterating: a listener may unwatch itself while being told, and a widget
    // disposing in response to an update is an ordinary thing rather than an error.
    if (node.listeners.isNotEmpty) {
      for (final listener in List<void Function(VChange)>.of(node.listeners)) {
        listener(change);
      }
    }
    if (node.listeners.isEmpty) {
      final inFrame = _heldChangedInFrame;
      if (inFrame != null) {
        inFrame.add(channel);
      } else {
        _notifyHolders(channel, {channel});
      }
    }
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
    for (final holder in _referrersOf(channel)) {
      if (!seen.add(holder)) continue;
      final node = _nodes[holder];
      if (node == null) continue;
      // Not a bounds change: what the holder draws changed, not where the holder is.
      for (final listener in List<void Function(VChange)>.of(node.listeners)) {
        listener(const VChange.ofHeld());
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

/// The registry's entries by channel, with an index from a widget's id to its channel: a binary frame
/// names widgets by id alone.
class _NodeMap extends MapBase<String, VNode> {
  final Map<String, VNode> _map = {};
  final Map<int, _Indexed> _byId = {};

  /// Entries indexed under an id [_byId] already holds for another channel: a value the client
  /// makes for itself, such as a CHECK tree item's box, takes the id of the widget it belongs to.
  final Map<int, List<_Indexed>> _sharingId = {};

  /// See [VRegistry.epoch].
  int epoch = 0;

  String? channelOf(int id) => _byId[id]?.channel;

  /// The entry a frame names by [id], with the channel it is held under: one lookup by number where
  /// the channel would take a second by string. With [accepts], the one whose value it accepts.
  _Indexed? indexedOf(int id, [bool Function(VWidget value)? accepts]) {
    final first = _byId[id];
    if (first == null || accepts == null || accepts(first.node.value)) return first;
    for (final other in _sharingId[id] ?? const <_Indexed>[]) {
      if (accepts(other.node.value)) return other;
    }
    return first;
  }

  /// The id a channel indexes, or null. A control's GC overlay shares its control's id, and a frame
  /// never names a GC, so GC channels are not indexed: they would shadow the control.
  static int? _idOf(String channel) =>
      channel.startsWith('GC/') ? null : int.tryParse(channel.substring(channel.lastIndexOf('/') + 1));

  @override
  VNode? operator [](Object? key) => _map[key];

  @override
  void operator []=(String key, VNode value) {
    _map[key] = value;
    epoch++;
    final id = _idOf(key);
    if (id != null) _index(id, key, value);
  }

  /// [operator []=] for an entry whose [id] the caller has in hand, as a frame naming it does:
  /// not parsed back out of the channel. Never a GC's, which a frame does not name.
  void putIndexed(String key, VNode value, int id) {
    _map[key] = value;
    epoch++;
    _index(id, key, value);
  }

  void _index(int id, String key, VNode value) {
    final held = _byId[id];
    if (held == null || held.channel == key) {
      _byId[id] = _Indexed(key, value);
      return;
    }
    final sharing = _sharingId[id] ??= [];
    sharing.removeWhere((e) => e.channel == key);
    sharing.add(_Indexed(key, value));
  }

  @override
  VNode? remove(Object? key) {
    final node = _map.remove(key);
    if (node != null) epoch++;
    if (node != null && key is String) {
      // Indexed under its value's id, unless the entry is still waiting for its value.
      final held = node.value.id;
      final id = _byId[held]?.channel == key ? held : _idOf(key);
      if (id != null) _unindex(id, key);
    }
    return node;
  }

  void _unindex(int id, String channel) {
    final sharing = _sharingId[id];
    if (_byId[id]?.channel == channel) {
      if (sharing == null || sharing.isEmpty) {
        _byId.remove(id);
      } else {
        _byId[id] = sharing.removeAt(0);
      }
    } else {
      sharing?.removeWhere((e) => e.channel == channel);
    }
    if (sharing != null && sharing.isEmpty) _sharingId.remove(id);
  }

  @override
  void clear() {
    _map.clear();
    epoch++;
    _byId.clear();
    _sharingId.clear();
  }

  @override
  Iterable<String> get keys => _map.keys;
}

/// An entry of [_NodeMap] as its id index holds it.
class _Indexed {
  _Indexed(this.channel, this.node);

  final String channel;
  final VNode node;
}

/// A shell's popups as a binary frame describes them: the menus, already resolved to held values.
class VShellPopups implements WireReadable {
  List<VMenu>? popups;

  @override
  bool readWire(int key, WidgetWireReader r) {
    if (key != WireKey.k_Display_popups) return false;
    popups = r.refs<VMenu>();
    return true;
  }
}
