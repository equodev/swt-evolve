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
import '../gen/styledtext.dart';
import '../gen/widgets.dart';
import '../impl/utils/style_index_change.dart';
import 'comm.dart';
import 'comm_frame.dart';
import 'delivery_gate.dart';
import 'widget_key.dart';
import 'widget_wire.dart';

/// What one delivery did to a value, as much as is knowable from the delivery itself.
///
/// Which is: the properties a change named, by [WireKey] number, and nothing at all for a whole
/// description - a whole widget says what it is, never what moved. There is no "before" to offer
/// either way, because a value is changed in place rather than replaced; that is what keeps every
/// reference to a widget pointing at the widget.
class VChange {
  const VChange()
      : ofHeld = false,
        _count = -1,
        _k0 = 0,
        _k1 = 0,
        _k2 = 0,
        _k3 = 0,
        _more = null;

  /// Not this widget's own change: a widget it holds changed, one that has no state of its own and
  /// that this widget may draw.
  const VChange.ofHeld()
      : ofHeld = true,
        _count = -1,
        _k0 = 0,
        _k1 = 0,
        _k2 = 0,
        _k3 = 0,
        _more = null;

  /// A partial binary delivery naming the properties [keys], by number. Up to four are held in the
  /// object itself, so the common change is one allocation; [keys] is copied, never kept.
  VChange.ofKeys(WireKeys keys)
      : ofHeld = false,
        _count = keys.length,
        _k0 = keys.length > 0 ? keys[0] : 0,
        _k1 = keys.length > 1 ? keys[1] : 0,
        _k2 = keys.length > 2 ? keys[2] : 0,
        _k3 = keys.length > 3 ? keys[3] : 0,
        _more = keys.length > 4 ? keys.toList() : null;

  /// Whether a widget this one holds changed rather than this one; see [VChange.ofHeld].
  final bool ofHeld;

  /// How many properties a change named, or -1 for any other delivery.
  final int _count;
  final int _k0, _k1, _k2, _k3;
  final List<int>? _more;

  /// The properties a change named, by number, or null for a whole description.
  List<int>? get keys {
    if (_count < 0) return null;
    return _more ?? [_k0, _k1, _k2, _k3].sublist(0, _count);
  }

  /// Whether this delivery could have touched the property numbered [key].
  bool touches(int key) {
    if (_count < 0) return true;
    final more = _more;
    if (more != null) return more.contains(key);
    return (_count > 0 && _k0 == key) ||
        (_count > 1 && _k1 == key) ||
        (_count > 2 && _k2 == key) ||
        (_count > 3 && _k3 == key);
  }

  /// Whether this delivery could have moved or resized the widget.
  bool get touchesBounds => touches(WireKey.k_Control_bounds) || touches(WireKey.k_Caret_bounds);

  /// Whether this delivery named nothing outside [properties] - a delivery about one thing, which
  /// a listener can then handle on its own instead of rebuilding for it.
  bool isOnly(Set<int> properties) {
    if (_count < 0) return false;
    final more = _more;
    if (more != null) return more.every(properties.contains);
    return (_count < 1 || properties.contains(_k0)) &&
        (_count < 2 || properties.contains(_k1)) &&
        (_count < 3 || properties.contains(_k2)) &&
        (_count < 4 || properties.contains(_k3));
  }
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

  /// The widget's [WidgetKey], set when the entry is held under its channel.
  int key = 0;

  /// Mounted widgets waiting to be rebuilt. Attaching and detaching touches only this, which is
  /// why an offstage or disposed widget does not stop the value updating.
  ///
  /// Replaced rather than changed, so telling them needs no copy: a listener that unwatches itself
  /// while being told leaves the list being iterated alone.
  List<void Function(VChange change)> listeners = const [];

  void listen(void Function(VChange change) listener) =>
      listeners = List.of(listeners)..add(listener);

  void unlisten(void Function(VChange change) listener) {
    final at = listeners.indexOf(listener);
    if (at < 0) return;
    listeners = List.of(listeners)..removeAt(at);
  }

  /// The widgets this value refers to, by [WidgetKey], as of the last thing applied to it. Kept so
  /// the next arrival can be compared against it: what it no longer mentions is what it let go of.
  Set<int> references = const {};

  /// True once something named this widget without describing it and Java was asked for it: the
  /// entry has to survive until the answer arrives, whoever stops watching it meanwhile.
  bool asked = false;

  /// The frame this widget was last noted in as changed with nobody listening; see
  /// [VRegistry._heldChangedInFrame]. A mark rather than a set, so noting it allocates nothing.
  int changedInFrame = 0;

  /// The pass that last told this widget a widget it holds changed, so one pass tells it once.
  int toldInPass = 0;
}

class VRegistry {
  VRegistry();

  /// The client's registry. One per client, because widget identity is a fact about the client.
  static final VRegistry instance = VRegistry();

  final _NodeMap _nodes = _NodeMap();

  /// The widgets being walked right now, so a reference leading back to something already on the
  /// path stops there instead of going round forever.
  final Set<int> _adopting = {};

  /// Which held values refer to each channel. A widget is reachable while this is not empty, and a
  /// widget reachable from two places — a tab body that is both a child of its folder and the
  /// control of its item — survives one of them letting go.
  ///
  /// Also which way to look when a widget nothing renders changes: see [_notify].
  ///
  /// A channel's one holder is kept as that holder's channel, and a [Set] only once a second one
  /// refers to it: nearly every widget has a single parent, and a set per widget was most of what a
  /// frame describing a new subtree allocated.
  ///
  /// By [WidgetKey], each holder as its key.
  final Map<int, Object> _referrers = {};

  void _addReferrer(int key, int holderKey) {
    final held = _referrers[key];
    if (held == null) {
      _referrers[key] = holderKey;
    } else if (held is int) {
      if (held != holderKey) _referrers[key] = <int>{held, holderKey};
    } else {
      (held as Set<int>).add(holderKey);
    }
  }

  /// Takes [holderKey] off what refers to [key]; true when something still does.
  bool _removeReferrer(int key, int holderKey) {
    final held = _referrers[key];
    if (held == null) return false;
    if (held is int) {
      if (held != holderKey) return true;
      _referrers.remove(key);
      return false;
    }
    final holders = held as Set<int>;
    holders.remove(holderKey);
    if (holders.isNotEmpty) return true;
    _referrers.remove(key);
    return false;
  }

  /// What each holder that is not itself a registered value refers to. See [holds].
  final Map<int, Set<int>> _holdings = {};

  /// One per channel anything has asked to be notified about. See [changesOn].
  /// Told when a delivery could have moved or resized the widget on that channel.
  final Map<int, _ChannelTick> _boundsTicks = {};

  /// Channels an ask is already scheduled for, so several references to one widget in a single
  /// payload - a control that is both a child of its parent and the content of an item - cost one
  /// request rather than one each.
  final Set<int> _asking = {};

  /// How many widgets the client is holding state for. Watched by tests: values now outlive the
  /// widgets that render them, so this growing without bound is the failure mode to guard against.
  int get size => _nodes.values.where((node) => !node.awaitingValue).length;

  /// The [WidgetKey] a value is stored under here: its wire type and its id, as a frame names it.
  static int keyOf(VWidget value) => WidgetKey.of(value.wireType, value.id);

  /// The channel a value is addressed by, `Type/id`: for tests and logs. Nothing here is stored
  /// under it; see [keyOf].
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
    final key = keyOf(value);
    final node = _nodes[key];
    if (node != null && !node.awaitingValue) return _takeNewerOf(node, value);
    // The stand-in an unresolved reference left, handed back by whatever was built from the name:
    // nothing has described the widget, so it is still awaited and still asked for.
    if (node != null && identical(node.value, value)) return value;

    if (node == null) {
      // Entered before listening: a payload held for this channel is replayed as soon as it is
      // listened on, and it has to find the entry it is about.
      _nodes[key] = VNode(value);
    } else if (isSameWidget(node.value, value) && !value.isReference) {
      // A stand-in left by a reference: whatever named this widget holds that object (a parent's
      // child list), so the description is copied into it, as a whole record is. Swapping it would
      // leave the parent laying out a child with no state at all.
      return _fill(node, value);
    } else {
      // Something asked to hear about this widget before it arrived, and what it has been waiting
      // on is a stand-in of the wrong type to copy a real widget into. The entry is rebuilt around
      // the value that turned up, carrying the interest over; nothing can be holding the stand-in,
      // which is the difference between this and every other arrival.
      _nodes[key] = VNode(value)
        ..asked = node.asked
        ..listeners = node.listeners;
    }
    // The stamp comes with the value, and recording it is what makes the next update mergeable.
    // Java counts a widget written inside an ancestor's payload as delivered, so it computes that
    // widget's next update against this state; a client that had not noticed would refuse it and
    // ask for the whole widget back.
    deliveryGate.appliedAt(key, value.seq);
    final entry = _nodes[key]!;
    _adoptSubtreeOf(entry, value);
    _reconcile(entry, value.seq);
    return value;
  }

  /// Whether [channel] is known only as a name: something referred to this widget before it had
  /// ever been described, and the answer to that request has not come back yet. What is held for it
  /// until then is an identity stub, which is nothing to render.
  bool isAwaitingAt(int key) => _nodes[key]?.awaitingValue ?? false;

  /// [isAwaitingAt], by channel name: for tests.
  @visibleForTesting
  bool isAwaiting(String channel) => isAwaitingAt(WidgetKey.ofChannel(channel));

  /// The value held for [channel], or null when the widget is unknown here.
  /// Moves on whenever a channel starts or stops holding a value, or holds a different one: what
  /// [valueOn] answers for a channel stays the same until it does.
  int get epoch => _nodes.epoch;

  VWidget? valueAt(int key) {
    final node = _nodes[key];
    return (node == null || node.awaitingValue) ? null : node.value;
  }

  /// [valueAt], by channel name: for tests, and for a request that names a widget as text.
  VWidget? valueOn(String channel) => valueAt(WidgetKey.ofChannel(channel));

  /// Per holder type, what its record is read into; see [onHolder].
  final Map<int, WireReadable Function()> _holderValues = {};
  final Map<int, List<void Function(int id, Object value)>> _holderListeners = {};

  /// Tells [listener] about every record of [type] - something that holds widgets without being one,
  /// such as the display - read into what [create] makes.
  ///
  /// Returns what stops it.
  void Function() onHolder<T extends WireReadable>(String typeName, T Function() create, void Function(int id, T value) listener) {
    final type = WidgetKey.typeOf(typeName);
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
  final Map<int, Map<int, int>> _holderStamps = {};

  /// The last record of each holder, by type and id; see [onHolder].
  final Map<int, Map<int, Object>> _holderRecords = {};

  /// What each holder's record is read into, before anything has asked to hear about it.
  static final Map<int, WireReadable Function()> _holderTypes = {
    WireType.display: VDisplay.new,
    WireType.displayPopups: VShellPopups.new,
    WireType.shellPopups: VShellPopups.new,
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
  void applyWire(Uint8List body, [int start = 0, int? end]) {
    // A listener may apply a frame of its own while this one applies: only the outermost uses what
    // the registry keeps for it.
    final outer = _wireDepth++ == 0;
    try {
      final reader = outer
          ? (_wireReader..reset(body, start, end ?? body.length))
          : (WidgetWireReader(body, start, end)
            ..resolve = _heldById
            ..enterItem = _enterInPlace);
      _applyWire(reader, outer ? _wireScratch : _WireScratch());
    } finally {
      _wireDepth--;
    }
  }

  int _wireDepth = 0;
  late final WidgetWireReader _wireReader = WidgetWireReader(Uint8List(0))
    ..resolve = _heldById
    ..enterItem = _enterInPlace;
  final _WireScratch _wireScratch = _WireScratch();
  /// The list [_heldChangedInFrame] is when the frame is the registry's own, kept between frames.
  final _NodeBuffer _heldChangedScratch = _NodeBuffer();

  /// Numbers the outermost frames, for [VNode.changedInFrame].
  int _frame = 0;

  /// Numbers the passes that tell holders, for [VNode.toldInPass].
  int _pass = 0;

  void _applyWire(WidgetWireReader r, _WireScratch scratch) {
    final count = r.u32();
    scratch.ensure(count);
    final kinds = scratch.kinds;
    final starts = scratch.starts;
    final ends = scratch.ends;
    final ids = scratch.ids;
    final stamps = scratch.stamps;
    final bases = scratch.bases;
    final types = scratch.types;
    final fresh = scratch.fresh;
    var stamp = 0;
    for (var i = 0; i < count; i++) {
      final kind = kinds[i] = r.u8();
      final length = r.varint();
      final end = ends[i] = r.pos + length;
      fresh[i] = false;
      switch (kind) {
        case wireWhole:
        case wireChange:
          final type = types[i] = r.typeNumber();
          final id = ids[i] = r.zigzag();
          stamp += r.varint();
          stamps[i] = stamp;
          if (kind == wireChange) bases[i] = stamp - r.varint();
          if (kind == wireWhole) fresh[i] = _enter(type, id);
        case wireImage:
          types[i] = 0;
          ids[i] = r.zigzag();
        case wireHolder:
          types[i] = r.typeNumber();
          ids[i] = r.zigzag();
          stamp += r.varint();
          stamps[i] = stamp;
      }
      starts[i] = r.pos;
      r.pos = end;
    }
    final outermost = _heldChangedInFrame == null;
    final kept = identical(scratch, _wireScratch);
    if (outermost) _frame++;
    final heldChanged = _heldChangedInFrame ??= kept ? (_heldChangedScratch..clear()) : _NodeBuffer();
    for (var i = 0; i < count; i++) {
      r.pos = starts[i];
      final end = ends[i];
      r.namingRecord = kinds[i] == wireWhole || kinds[i] == wireChange ? ids[i] : 0;
      r.inPlaceStamp = stamps[i];
      try {
        switch (kinds[i]) {
          case wireWhole:
            _applyWhole(r, types[i], ids[i], stamps[i], end, fresh[i]);
          case wireChange:
            final node = _nodes[WidgetKey.of(types[i], ids[i])];
            if (node != null) _applyChange(r, node, stamps[i], bases[i], end);
          case wireImage:
            final image = r.imageFor(ids[i]);
            r.readProperties(image, end);
            image.isReference = false;
            rememberOrResolveImage(image);
          case wireHolder:
            _applyHolder(r, types[i], ids[i], stamps[i], end);
        }
        _applyInPlace(r);
      } catch (e) {
        r.inPlaceKeys.clear();
        r.inPlaceStamps.clear();
        // A record says how long it is, so one that cannot be read costs only its widget.
        if (kDebugMode) {
          print('[wire] ${WidgetKey.typeName(types[i])}/${ids[i]} could not be read ($e): '
              'asking for it whole');
        }
        if (kinds[i] == wireWhole || kinds[i] == wireChange) {
          EquoCommService.sendOn(WireChannel.swtEvolveWidgetRefresh, '${ids[i]}');
        }
      }
      r.pos = end;
      if (r.unresolved.isNotEmpty) {
        // Named, and neither held here nor described in the frame: the two sides disagree about what
        // this client holds. Ask for the widget, then for what named it, so the name resolves.
        for (final id in r.unresolved) {
          if (kDebugMode) {
            print('[delivery] unresolved reference $id in '
                '${WidgetKey.typeName(types[i])}/${ids[i]}: asking for it');
          }
          EquoCommService.sendOn(WireChannel.swtEvolveWidgetRefresh, '$id');
        }
        EquoCommService.sendOn(WireChannel.swtEvolveWidgetRefresh, '${ids[i]}');
        r.unresolved.clear();
      }
    }
    if (r.imagesNamedUnheld.isNotEmpty) _askForUnheldImages(r);
    if (!outermost) return;
    _heldChangedInFrame = null;
    if (heldChanged.isEmpty) return;
    // Every changed widget counts as told already: one that is itself among them tells its own
    // holders.
    final pass = ++_pass;
    for (var i = 0; i < heldChanged.length; i++) {
      heldChanged[i].toldInPass = pass;
    }
    for (var i = 0; i < heldChanged.length; i++) {
      _notifyHolders(heldChanged[i].key, pass);
    }
    heldChanged.clear();
  }

  /// Asks for each widget that named an image this client neither held nor was given in the frame.
  /// Java counted that image as delivered when it was not; the request makes it describe the widget
  /// again with its images.
  void _askForUnheldImages(WidgetWireReader r) {
    final asked = <int>{};
    r.imagesNamedUnheld.forEach((image, widget) {
      if (widget == 0 || r.images[image]?.isReference != true || !asked.add(widget)) return;
      if (kDebugMode) print('[delivery] image $image named by $widget never arrived: asking for the widget');
      EquoCommService.sendOn(WireChannel.swtEvolveWidgetRefresh, '$widget');
    });
    r.imagesNamedUnheld.clear();
  }

  /// The widgets the frame being applied changed that nobody listens to, whose holders are told once
  /// the whole frame is in: a table described with its items would otherwise hear once per item.
  _NodeBuffer? _heldChangedInFrame;

  /// [wireRefKeys] as a flag per property number, which needs no hashing to test.
  static final List<bool> _refKeyFlags = () {
    final flags = List<bool>.filled(wireRefKeys.fold(0, (a, b) => a > b ? a : b) + 1, false);
    for (final key in wireRefKeys) {
      flags[key] = true;
    }
    return flags;
  }();

  static bool _isRefKey(int key) => key >= 0 && key < _refKeyFlags.length && _refKeyFlags[key];


  /// Makes the entry for a widget the frame describes in full, when nothing here holds it. True when
  /// it did: the record is then read straight into the entry's value.
  bool _enter(int type, int id) {
    final key = WidgetKey.of(type, id);
    final node = _nodes[key];
    if (node != null && !node.awaitingValue) return false;
    final value = newWidgetValue(WidgetKey.typeName(type));
    if (value == null) return false;
    value.id = id;
    final entry = VNode(value);
    // Something asked to hear about this widget before it arrived; the interest carries over.
    if (node != null) {
      entry.listeners = node.listeners;
      entry.asked = node.asked;
    }
    _nodes[key] = entry;
    return true;
  }

  /// What an item a list describes in place is read into: as a whole record's, the entry held for it,
  /// made if there is none; a throwaway when the description is older than the state held.
  VWidget? _enterInPlace(WidgetWireReader r, int type, int id, int stamp) {
    final fresh = _enter(type, id);
    final key = WidgetKey.of(type, id);
    final node = _nodes[key];
    if (node == null) return null;
    if (!fresh) {
      if (deliveryGate.decideWholeAt(key, stamp) != FrameAction.replace) {
        return newWidgetValue(WidgetKey.typeName(type));
      }
      final empty = _emptyOf(type);
      if (empty == null) return null;
      node.value.copyFrom(empty);
    }
    r.inPlaceKeys.add(key);
    r.inPlaceStamps.add(stamp);
    return node.value;
  }

  /// Applies what the record just read described in place, in the order it was read: after the
  /// record, as the records Java would otherwise have written for them follow it.
  void _applyInPlace(WidgetWireReader r) {
    if (r.inPlaceKeys.isEmpty) return;
    for (var i = 0; i < r.inPlaceKeys.length; i++) {
      final node = _nodes[r.inPlaceKeys[i]];
      if (node == null) continue;
      final stamp = r.inPlaceStamps[i];
      node.value.seq = stamp;
      deliveryGate.appliedAt(node.key, stamp);
      _reconcile(node, stamp);
      _notify(node, const VChange());
    }
    r.inPlaceKeys.clear();
    r.inPlaceStamps.clear();
  }

  void _applyWhole(WidgetWireReader r, int type, int id, int stamp, int end, bool fresh) {
    final node = _nodes[WidgetKey.of(type, id)];
    if (node == null) return;
    if (fresh) {
      r.readProperties(node.value, end);
    } else {
      // Asked before reading: a description of the past is not read at all.
      if (deliveryGate.decideWholeAt(node.key, stamp) != FrameAction.replace) return;
      final empty = _emptyOf(type);
      if (empty == null) return;
      // Read into the held object, never swapped for it: everything that refers to this widget
      // refers to that object. A whole description names every property it sets, so the object
      // starts from the defaults of what it leaves out.
      node.value.copyFrom(empty);
      r.readProperties(node.value, end);
    }
    node.value.seq = stamp;
    deliveryGate.appliedAt(node.key, stamp);
    _reconcile(node, stamp);
    _notify(node, const VChange());
  }

  /// A value of each type with every property at its default, made once: what a whole description
  /// starts the held value from.
  final Map<int, VWidget?> _empties = {};

  VWidget? _emptyOf(int type) => _empties.putIfAbsent(type, () => newWidgetValue(WidgetKey.typeName(type)));

  final WireKeys _changedScratch = WireKeys();

  void _applyChange(WidgetWireReader r, VNode node, int stamp, int base, int end) {
    switch (deliveryGate.decideChangeAt(node.key, stamp, base)) {
      case FrameAction.merge:
        // A state showing the editor resolves its style index, which it carries across edits; with
        // none, the index held is what the change was made from.
        final value = node.value;
        final unshownIndex = value is VStyledText && node.listeners.isEmpty ? value.styleIndex : null;
        // Read into a list kept for it: the change copies what it names.
        final changed = _changedScratch..clear();
        r.readProperties(value, end, changed);
        if (unshownIndex != null) resolveUnshownStyleIndexChange(value as VStyledText, unshownIndex);
        node.value.seq = stamp;
        deliveryGate.appliedAt(node.key, stamp);
        // Made only for someone to read: a row nobody listens to tells its holder, which needs
        // no change of its own.
        final change = node.listeners.isNotEmpty || _boundsTicks.containsKey(node.key)
            ? VChange.ofKeys(changed)
            : null;
        if (changed.any(_isRefKey)) _reconcile(node, stamp);
        if (change != null) {
          _notify(node, change);
        } else if (node.listeners.isNotEmpty) {
          // Someone started listening while the references were reconciled.
          _notify(node, const VChange());
        } else {
          _notifyUnheard(node);
        }
      case FrameAction.recover:
        EquoCommService.sendOn(WireChannel.swtEvolveWidgetRefresh, '${node.value.id}');
      case FrameAction.replace:
      case FrameAction.ignore:
        break;
    }
  }

  void _applyHolder(WidgetWireReader r, int type, int id, int stamp, int end) {
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

  void _tellHolder(int type, int id, Object value) {
    (_holderRecords[type] ??= {})[id] = value;
    for (final listener in List.of(_holderListeners[type] ?? const <void Function(int, Object)>[])) {
      listener(id, value);
    }
  }

  /// A holder's record as it is handed on once read, for a test that describes it directly.
  @visibleForTesting
  void deliverHolderForTesting(String type, int id, Object value) => _tellHolder(WidgetKey.typeOf(type), id, value);

  /// What [holder] refers to now, for a holder that is not a registered value itself.
  ///
  /// The display is one: it holds the shells and the popup menus, and is a device rather than a
  /// widget, so it has no value here to read references out of. Calling this again with a different
  /// set is how it lets go of what is no longer in it.
  ///
  /// No write stamp, and none is wanted. The stamp exists to tell a disposal from a widget that
  /// moved, and a shell cannot move: it belongs to one display for its whole life, so a shell the
  /// display stops carrying is a shell that closed.
  void holdsAt(int holderKey, Iterable<VWidget> values) {
    final after = {for (final value in values) keyOf(value)};
    final before = _holdings[holderKey] ?? const <int>{};
    _holdings[holderKey] = after;
    _applyReferenceChange(holderKey, before, after, null);
  }

  /// [holdsAt], with the holder named as a channel: for tests.
  @visibleForTesting
  void holds(String holder, Iterable<VWidget> values) => holdsAt(WidgetKey.ofChannel(holder), values);

  /// Notifies whenever any of [keys] is told something that could have changed its bounds.
  ///
  /// Lets a layout that depends on its children's bounds re-run. Bounds only: a layout pass
  /// repaints up to the nearest repaint boundary.
  Listenable boundsChangesAt(Iterable<int> keys) {
    final ticks = <Listenable>[
      for (final key in keys) _boundsTicks.putIfAbsent(key, _ChannelTick.new),
    ];
    return Listenable.merge(ticks);
  }

  /// [boundsChangesAt], by channel names: for tests.
  @visibleForTesting
  Listenable boundsChangesOn(Iterable<String> channels) => boundsChangesAt(channels.map(WidgetKey.ofChannel));

  /// Asks to be rebuilt when [channel]'s value changes. Safe to call for a widget that has not
  /// arrived yet — the interest is recorded and honoured once it does.
  void watchAt(int key, void Function(VChange change) listener) {
    _nodes.putIfAbsent(key, () => VNode(VWidget.empty(), awaitingValue: true)).listen(listener);
  }

  /// [watchAt], by channel name: for tests.
  @visibleForTesting
  void watch(String channel, void Function(VChange change) listener) => watchAt(WidgetKey.ofChannel(channel), listener);

  /// [unwatchAt], by channel name: for tests.
  @visibleForTesting
  void unwatch(String channel, void Function(VChange change) listener) =>
      unwatchAt(WidgetKey.ofChannel(channel), listener);

  void unwatchAt(int key, void Function(VChange change) listener) {
    final node = _nodes[key];
    if (node == null) return;
    node.unlisten(listener);
    // An entry that only ever existed to record the interest, and nothing is interested any more.
    // Leaving it would accumulate one empty entry per widget that was watched before it arrived.
    // One that was asked for is waiting on an answer something named it for: a widget that
    // rendered it before a move unmounting afterwards must not drop the answer on the floor.
    if (node.awaitingValue && node.listeners.isEmpty && !node.asked) {
      _nodes.remove(key);
    }
  }

  /// Forgets a widget: its state and its listeners.
  ///
  /// Called when the widget is gone — not when it stops being rendered. That distinction is the
  /// reason this exists at all.
  void evictAt(int key) {
    final node = _nodes.remove(key);
    if (node == null) return;
    // The overlay a widget is drawn on goes with the widget. Every control mounts a GC keyed by its
    // own id, whether or not anything ever draws on it, and nothing refers to that GC - so it is
    // the one value with no holder to let go of it. An item is drawn by its control, on its GC.
    if (node.value is! VItem && WidgetKey.typeOfKey(key) != WireType.gc) {
      evictAt(WidgetKey.of(WireType.gc, WidgetKey.idOfKey(key)));
    }
    node.listeners = const [];
    _boundsTicks.remove(key)?.dispose();
    deliveryGate.forgetAt(key);
    // Frames held for the widget until it could answer: it never will now.
    if (node.value.id case final int id) EquoCommService.forgetWidgetOps(id);
  }

  /// [evictAt], by channel name: for tests.
  @visibleForTesting
  void evict(String channel) => evictAt(WidgetKey.ofChannel(channel));

  /// Drops everything. For a fresh client, and between tests.
  void clear() {
    for (final key in _nodes.keys.toList()) {
      evictAt(key);
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

  void _adoptSubtreeOf(VNode node, VWidget value) {
    if (!_adopting.add(node.key)) return;
    try {
      value.adoptChildren(_adopt);
    } finally {
      _adopting.remove(node.key);
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
    final key = keyOf(incoming);
    final node = _nodes[key];

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
      final waiting = _nodes.putIfAbsent(key, () => VNode(incoming, awaitingValue: true));
      waiting.asked = true;
      _askUnlessItArrives(key, incoming.id);
      return waiting.value;
    }

    if (node == null || node.awaitingValue) return register(incoming);

    return _takeNewerOf(node, incoming);
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
  VWidget _takeNewerOf(VNode node, VWidget incoming) {
    final held = node.value;
    if (identical(held, incoming) || incoming.seq <= held.seq) return held;
    return _fill(node, incoming);
  }

  /// Copies [incoming] into the object held for [channel] and tells whoever is listening. The
  /// subtree is adopted before anyone is told, for the reason apply() gives.
  VWidget _fill(VNode node, VWidget incoming) {
    final held = node.value..copyFrom(incoming);
    held.seq = incoming.seq;
    node.awaitingValue = false;
    deliveryGate.appliedAt(node.key, held.seq);
    _adoptSubtreeOf(node, held);
    _reconcile(node, held.seq);
    _notify(node, const VChange());
    return held;
  }

  /// Asks for [channel]'s widget, unless the delivery that named it goes on to describe it.
  ///
  /// A record can name a widget that a later record of the same delivery describes, so the widget
  /// can already be on its way at the moment it looks missing. Deferring by one microtask, past the
  /// rest of the delivery, is what tells the two apart.
  void _askUnlessItArrives(int key, int id) {
    if (!_asking.add(key)) return;
    scheduleMicrotask(() {
      _asking.remove(key);
      if (!isAwaitingAt(key)) return;
      // Nothing was held for it and nothing describes it: the sender named a widget this client
      // was never given. Recovering works, but it costs a round trip and should not be reachable -
      // said out loud in debug builds so that it is noticed rather than absorbed; a release build
      // stays quiet, since the app's console is the user's.
      if (kDebugMode) print('[delivery] unresolved reference ${WidgetKey.describe(key)}: asking for it');
      EquoCommService.sendOn(WireChannel.swtEvolveWidgetRefresh, '$id');
    });
  }

  /// Notes what a node's value refers to now, and forgets whatever it just let go of.
  ///
  /// Java never says a widget was disposed; it simply stops carrying it. So a departure has to be
  /// read out of what arrives — a child no longer in its parent's list, an item no longer in its
  /// table — and [writtenAt] is when the value doing the dropping was written, which is what tells
  /// a disposal apart from a reparent still in flight.
  void _reconcile(VNode node, int writtenAt) {
    final before = node.references;
    // Collected into a list kept for it first: nearly every change names the same widgets the value
    // already held, and then nothing is built or compared twice.
    _referenceCount = 0;
    node.value.adoptChildren(_collectReference);
    final keys = _referenceScratch;
    final count = _referenceCount;
    if (_sameReferences(before, keys, count)) return;
    final after = count == 0 ? const <int>{} : {for (var i = 0; i < count; i++) keys[i]};
    node.references = after;
    _applyReferenceChange(node.key, before, after, writtenAt);
  }

  /// The child keys [_reconcile] collects, grown and never shrunk; [_referenceCount] are in use.
  List<int> _referenceScratch = List<int>.filled(16, 0);
  int _referenceCount = 0;

  /// [VWidget.adoptChildren]'s callback for [_reconcile], torn off once.
  late final VWidget Function(VWidget) _collectReference = _collectReferenceOf;

  VWidget _collectReferenceOf(VWidget child) {
    if (_referenceCount == _referenceScratch.length) {
      _referenceScratch = List<int>.filled(_referenceCount * 2, 0)..setRange(0, _referenceCount, _referenceScratch);
    }
    _referenceScratch[_referenceCount++] = keyOf(child);
    return child;
  }

  /// Whether the first [count] of [keys] name exactly the widgets in [held], in any order and with
  /// any repeats.
  static bool _sameReferences(Set<int> held, List<int> keys, int count) {
    for (var i = 0; i < count; i++) {
      if (!held.contains(keys[i])) return false;
    }
    // Every key is held; the two are the same when nothing held went unnamed.
    if (count == held.length) return true;
    final seen = <int>{};
    for (var i = 0; i < count; i++) {
      seen.add(keys[i]);
    }
    return seen.length == held.length;
  }

  void _applyReferenceChange(int holder, Set<int> before, Set<int> after, int? writtenAt) {
    for (final key in after) {
      if (!before.contains(key)) _addReferrer(key, holder);
    }
    for (final key in before) {
      if (!after.contains(key)) _release(holder, key, writtenAt);
    }
  }

  /// One reference to [channel] goes away. The widget goes with it when it was the last - once
  /// every frame received so far is applied, and only if nothing has taken it by then.
  ///
  /// A widget that moves is dropped by its old parent and taken by its new one, and the two frames
  /// can arrive in either order in one run: applied in between, it is held by nothing.
  void _release(int holder, int key, int? writtenAt) {
    if (!_letGo(holder, key, writtenAt)) return;
    if (_unheld.isEmpty) EquoCommBase.whenApplied(_evictUnheld);
    _unheld[key] = writtenAt;
  }

  /// Widgets that lost their last holder, and when the value that let go of each was written.
  final Map<int, int?> _unheld = {};

  void _evictUnheld() {
    final unheld = Map.of(_unheld);
    _unheld.clear();
    unheld.forEach(_evictIfUnheld);
  }

  void _evictIfUnheld(int key, int? writtenAt) {
    if (_referrers.containsKey(key)) return;
    final node = _nodes[key];
    if (node == null || _movedSince(node, writtenAt)) return;
    final held = node.references;
    evictAt(key);
    for (final reference in held) {
      if (_letGo(key, reference, writtenAt)) _evictIfUnheld(reference, writtenAt);
    }
  }

  /// Takes [holder] off [key]'s holders; whether that left [key] held by nothing.
  bool _letGo(int holder, int key, int? writtenAt) {
    if (_removeReferrer(key, holder)) return false;
    final node = _nodes[key];
    return node != null && !_movedSince(node, writtenAt);
  }

  // Described more recently than the value letting go of it: the widget moved, and the frame
  // dropping it is the old parent catching up rather than news of a disposal. Java writes one
  // stamp per write from one counter, so "newer" is a fact about the two writes, not a guess.
  static bool _movedSince(VNode node, int? writtenAt) =>
      writtenAt != null && !node.awaitingValue && node.value.seq > writtenAt;

  void _notify(VNode node, VChange change) {
    if (change.touchesBounds) _boundsTicks[node.key]?.tick();
    // A listener may unwatch itself while being told, and a widget disposing in response to an
    // update is an ordinary thing rather than an error: the list is replaced, not changed.
    final listeners = node.listeners;
    for (var i = 0; i < listeners.length; i++) {
      listeners[i](change);
    }
    if (node.listeners.isEmpty) _notifyUnheard(node);
  }

  /// [_notify] for [node], which nobody listens to: its holders hear, once the frame is in.
  void _notifyUnheard(VNode node) {
    final inFrame = _heldChangedInFrame;
    if (inFrame != null) {
      if (node.changedInFrame == _frame) return;
      node.changedInFrame = _frame;
      inFrame.add(node);
    } else {
      final pass = ++_pass;
      node.toldInPass = pass;
      _notifyHolders(node.key, pass);
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
  void _notifyHolders(int key, int pass) {
    final held = _referrers[key];
    if (held == null) return;
    // Nearly always one holder, kept as its key; several are copied, since telling one may change
    // who holds what.
    if (held is int) {
      _tellReferrer(held, pass);
    } else {
      for (final holder in List.of(held as Set<int>)) {
        _tellReferrer(holder, pass);
      }
    }
  }

  void _tellReferrer(int holder, int pass) {
    final node = _nodes[holder];
    if (node == null || node.toldInPass == pass) return;
    node.toldInPass = pass;
    // Not a bounds change: what the holder draws changed, not where the holder is.
    final listeners = node.listeners;
    for (var i = 0; i < listeners.length; i++) {
      listeners[i](const VChange.ofHeld());
    }
    // Still nobody: an item inside an item, or a subtree nothing is rendering yet.
    if (node.listeners.isEmpty) _notifyHolders(holder, pass);
  }
}

/// Something to hang a listener on, per channel. Separate from [VNode.listeners] because it
/// outlives no widget and belongs to no widget: whoever holds it attaches and detaches on its own
/// schedule, which is what a layout delegate needs.
class _ChannelTick extends ChangeNotifier {
  void tick() => notifyListeners();
}

/// The registry's entries by [WidgetKey], with an index from a widget's id to its entry: a reference
/// in a binary frame names a widget by id alone.
class _NodeMap {
  final Map<int, VNode> _byKey = {};
  final Map<int, _Indexed> _byId = {};

  /// Entries indexed under an id [_byId] already holds for another widget: a value the client
  /// makes for itself, such as a CHECK tree item's box, takes the id of the widget it belongs to.
  final Map<int, List<_Indexed>> _sharingId = {};

  /// See [VRegistry.epoch].
  int epoch = 0;

  /// The entry a frame names by [id]. With [accepts], the one whose value it accepts.
  _Indexed? indexedOf(int id, [bool Function(VWidget value)? accepts]) {
    final first = _byId[id];
    if (first == null || accepts == null || accepts(first.node.value)) return first;
    for (final other in _sharingId[id] ?? const <_Indexed>[]) {
      if (accepts(other.node.value)) return other;
    }
    return first;
  }

  /// Whether [key]'s entry is indexed by id. A control's GC overlay shares its control's id, and a
  /// frame never names a GC, so GCs are not indexed: they would shadow the control.
  static bool _indexed(int key) => WidgetKey.typeOfKey(key) != WireType.gc;

  VNode? operator [](int key) => _byKey[key];

  void operator []=(int key, VNode value) {
    _byKey[value.key = key] = value;
    epoch++;
    if (_indexed(key)) _index(WidgetKey.idOfKey(key), key, value);
  }

  VNode putIfAbsent(int key, VNode Function() create) => this[key] ?? (this[key] = create());

  void _index(int id, int key, VNode value) {
    final held = _byId[id];
    if (held == null || held.key == key) {
      _byId[id] = _Indexed(key, value);
      return;
    }
    final sharing = _sharingId[id] ??= [];
    sharing.removeWhere((e) => e.key == key);
    sharing.add(_Indexed(key, value));
  }

  VNode? remove(int key) {
    final node = _byKey.remove(key);
    if (node == null) return null;
    epoch++;
    if (_indexed(key)) _unindex(WidgetKey.idOfKey(key), key);
    return node;
  }

  void _unindex(int id, int key) {
    final sharing = _sharingId[id];
    if (_byId[id]?.key == key) {
      if (sharing == null || sharing.isEmpty) {
        _byId.remove(id);
      } else {
        _byId[id] = sharing.removeAt(0);
      }
    } else {
      sharing?.removeWhere((e) => e.key == key);
    }
    if (sharing != null && sharing.isEmpty) _sharingId.remove(id);
  }

  void clear() {
    _byKey.clear();
    epoch++;
    _byId.clear();
    _sharingId.clear();
  }

  Iterable<int> get keys => _byKey.keys;

  Iterable<VNode> get values => _byKey.values;
}

/// Nodes in the order added, kept in storage that is grown and never given back: a growable list
/// emptied releases its storage, and filling it again reallocates at every doubling.
class _NodeBuffer {
  List<VNode?> _items = List<VNode?>.filled(16, null);
  int length = 0;

  bool get isEmpty => length == 0;

  VNode operator [](int i) => _items[i]!;

  void add(VNode node) {
    if (length == _items.length) {
      _items = List<VNode?>.filled(length * 2, null)..setRange(0, length, _items);
    }
    _items[length++] = node;
  }

  /// Empties it, letting go of the nodes it held.
  void clear() {
    _items.fillRange(0, length, null);
    length = 0;
  }
}

/// An entry of [_NodeMap] as its id index holds it.
class _Indexed {
  _Indexed(this.key, this.node);

  final int key;
  final VNode node;
}

/// A shell's popups, or the display's own, as a binary frame describes them: the menus, already
/// resolved to held values.
class VShellPopups implements WireReadable {
  List<VMenu>? popups;

  @override
  bool readWire(int key, WidgetWireReader r) {
    if (key != WireKey.k_Display_popups) return false;
    popups = r.refs<VMenu>();
    return true;
  }
}

/// What [VRegistry.applyWire] reads a flush's record headers into: kept, and grown for a longer
/// flush.
class _WireScratch {
  List<int> kinds = const [], starts = const [], ends = const [], ids = const [];
  List<int> stamps = const [], bases = const [];
  List<int> types = const [];
  List<bool> fresh = const [];

  void ensure(int count) {
    if (count <= kinds.length) return;
    final capacity = count < 16 ? 16 : count * 2;
    kinds = List.filled(capacity, 0);
    starts = List.filled(capacity, 0);
    ends = List.filled(capacity, 0);
    ids = List.filled(capacity, 0);
    stamps = List.filled(capacity, 0);
    bases = List.filled(capacity, 0);
    types = List.filled(capacity, 0);
    fresh = List.filled(capacity, false);
  }
}
