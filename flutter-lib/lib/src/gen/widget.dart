import 'dart:convert';
import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/comm_frame.dart';
import '../comm/v_registry.dart';
import '../comm/widget_key.dart';
import '../comm/widget_wire.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/gc_evolve.dart';
import '../impl/utils/pending_move.dart';
import '../impl/utils/perf_marks.dart';
import '../impl/widget_config.dart';
import 'event.dart';
import 'gc.dart';
import 'widgets.dart';

abstract class WidgetSwt<V extends VWidget> extends StatefulWidget {
  final V value;
  const WidgetSwt({super.key, required this.value});

  /// Sends [payload] to Java on [val]'s channel numbered [channel] (see [WireChannel]).
  void sendEvent(V val, int channel, VEvent? payload) {
    // A move held back by the throttle happened before this event, so it goes first.
    PendingMove.flush();
    EquoCommService.sendTo(val.wireType, val.id, channel, payload);
  }

  void sendDisposeDispose(V val, VEvent? payload) {
    sendEvent(val, 22, payload); // Dispose/Dispose
  }
}

abstract class WidgetSwtState<T extends WidgetSwt, V extends VWidget>
    extends State<T> {
  /// Set only for an impl driven by hand rather than mounted in the tree - a row
  /// rendered by its table, which has no channel of its own to read from.
  V? _override;

  /// The widget's state, read from where it lives rather than stored here.
  ///
  /// A parent passes its own copy of every child down, and that copy is whatever Java
  /// last told the *parent* - older than anything the child has since been told
  /// directly. Reading through the registry makes the passed value carry identity only:
  /// which widget this is, never what it holds. A parent rebuilding, by update or by
  /// remount, then has nothing stale to hand over.
  V get state {
    final override = _override;
    if (override != null) return override;
    // A build reads the state dozens of times; the registry is asked again only once what it holds
    // has changed shape.
    final registry = VRegistry.instance;
    final key = _registryKey;
    if (_heldEpoch == registry.epoch && _heldKey == key) {
      return _held!;
    }
    final held = (key == null ? null : registry.valueAt(key)) as V?;
    if (held == null) return widget.value as V;
    _held = held;
    _heldEpoch = registry.epoch;
    _heldKey = key;
    return held;
  }

  V? _held;
  int _heldEpoch = -1;
  int? _heldKey;

  set state(V value) => _override = value;

  // GC support - any widget can have a GC overlay
  VGC? gcOverlay;
  final GlobalKey<GCImpl> gcOverlayKey = GlobalKey<GCImpl>();

  /// The [VRegistry.keyOf] this widget's value is held and watched under.
  int? _registryKey;

  @override
  void initState() {
    super.initState();
    _register();
  }

  /// Puts this widget's value where values live, and asks to be told when it changes.
  ///
  /// The key is remembered rather than derived on demand: [state] resolves through it, so
  /// deriving it from the state would be circular.
  void _register() {
    final value = widget.value as V;
    final key = _registryKey = VRegistry.keyOf(value);
    VRegistry.instance.register(value);
    VRegistry.instance.watchAt(key, _onRegistryChanged);
    _enterLive(value.id);
  }

  /// The mounted widgets of this one's kind by id, for a kind that answers channels of its
  /// own (see `EquoCommService.onWidgetOp`): where the handler finds the widget a frame
  /// addresses. Per kind, as the channel names it: ids of different kinds can coincide. This
  /// widget is in it from mount to dispose, so nothing registered for it can outlive it.
  Map<int, WidgetSwtState>? get liveOfKind => null;

  int? _liveId;

  void _enterLive(int id) {
    final live = liveOfKind;
    if (live == null) return;
    _leaveLive();
    _liveId = id;
    live[id] = this;
    // Queued, not applied here: they run once this widget has finished initialising.
    EquoCommService.replayWidgetOps(id);
  }

  void _leaveLive() {
    final id = _liveId;
    final live = liveOfKind;
    if (id != null && live != null && identical(live[id], this)) {
      live.remove(id);
    }
    _liveId = null;
  }

  /// The value this widget renders changed. Nothing is adopted here - the registry has
  /// already applied it - so this hands the new state to [setValue], which is still
  /// where an impl hooks an arriving value.
  void _onRegistryChanged(VChange change) {
    if (!mounted) return;
    _lastChange = change;
    setValue(state);
  }

  VChange? _lastChange;

  /// What the delivery currently being handled did: the value it replaced, or the
  /// properties it named. Read by an impl that has to tell what actually moved -
  /// null outside a delivery, when nothing is being handled.
  VChange? get lastChange => _lastChange;

  /// Stops listening. Deliberately does not evict: the widget is going away, its state
  /// is not. A hidden subtree keeps receiving while nothing renders it, which is the
  /// whole reason the value does not live here.
  void _unsubscribeFromState() {
    final key = _registryKey;
    if (key != null) {
      VRegistry.instance.unwatchAt(key, _onRegistryChanged);
    }
    _registryKey = null;
    _leaveLive();
  }

  /// Called by GCSwt when it receives state from Java.
  /// Switches the GC overlay from Offstage to visible.
  void notifyGCReady(VGC gcValue) {
    if (gcOverlay == null) {
      setState(() {
        gcOverlay = gcValue;
      });
    }
  }

  /// Clear shapes from the GC overlay
  void clearGCShapes() {
    gcOverlayKey.currentState?.clearShapes();
  }

  /// Whether what the application paints through this control's GC is shown. Canvas and
  /// StyledText override wrapWithGCOverlay and keep painting either way: the opt-out
  /// drops an overlay, never the drawing a control is made of.
  bool get paintsGCOverlay =>
      !(getConfigFlags().disable_control_gc_overlay ?? false);

  /// Wrap child widget with GC overlay.
  /// The GC is mounted when the first thing Java sends it arrives: most controls are never
  /// drawn through, and a mounted GC costs each of them its channels and its subtree. What
  /// arrives before it is mounted is held and replayed to it. Uses Offstage when GC has no
  /// state yet, or when the overlay is opted out of (listening but not rendering).
  Widget wrapWithGCOverlay(Widget child) {
    if (!_gcMounted && gcOverlay == null) {
      _awaitGC();
      // The same structure as with the GC, so that mounting it keeps [child].
      return Stack(children: [child, const SizedBox.shrink()]);
    }
    final painted = gcOverlay != null && paintsGCOverlay;
    // The same layer instance while nothing it is made of changed, so a rebuild of the
    // control does not rebuild its GC: what the GC draws reaches it on its own channel.
    if (_gcLayer == null ||
        !identical(_gcLayerValue, gcOverlay) ||
        _gcLayerPainted != painted ||
        _gcLayerId != state.id) {
      final gcWidget = GCSwt<VGC>(
        key: gcOverlayKey,
        value: gcOverlay ?? (VGC()..id = state.id),
      );
      _gcLayer = painted
          ? Positioned.fill(child: IgnorePointer(child: gcWidget))
          : Offstage(child: gcWidget);
      _gcLayerValue = gcOverlay;
      _gcLayerPainted = painted;
      _gcLayerId = state.id;
    }
    return Stack(children: [child, _gcLayer!]);
  }

  Widget? _gcLayer;
  VGC? _gcLayerValue;
  bool? _gcLayerPainted;
  int? _gcLayerId;

  bool _gcMounted = false;
  void Function()? _gcArrival;
  int? _gcAwaitedId;

  /// Whether this widget waits for its GC's first message rather than having the GC mounted.
  bool get awaitsGC => _gcArrival != null;

  /// Whether this widget's GC is mounted, or will be by its next build.
  bool get gcMounted => _gcMounted || gcOverlay != null;

  void _awaitGC() {
    final id = state.id;
    if (_gcArrival != null && _gcAwaitedId == id) return;
    _cancelGCWait();
    _gcAwaitedId = id;
    final arrival = _gcArrival = () {
      _gcArrival = null;
      if (mounted) setState(() => _gcMounted = true);
    };
    EquoCommService.onFirstGcMessage(id, arrival);
    onGCAwaited();
  }

  void _cancelGCWait() {
    final arrival = _gcArrival;
    final id = _gcAwaitedId;
    if (arrival != null && id != null) {
      EquoCommService.cancelFirstGcMessage(id, arrival);
    }
    _gcArrival = null;
  }

  /// Called when this widget starts waiting for its GC's first message.
  void onGCAwaited() {}

  @override
  void didUpdateWidget(covariant T oldWidget) {
    super.didUpdateWidget(oldWidget);
    final incoming = widget.value as V;
    // Nothing is adopted from the parent any more. The value it hands down is its own
    // copy of this widget, as of whenever Java last described the parent - which is why
    // a rebuild used to be able to push a child backwards. Only the identity is taken.
    final key = VRegistry.keyOf(incoming);
    if (key != _registryKey) {
      _unsubscribeFromState();
      _registryKey = key;
      VRegistry.instance.register(incoming);
      VRegistry.instance.watchAt(key, _onRegistryChanged);
      _enterLive(incoming.id);
    }
    extraSetState();
  }

  @override
  void dispose() {
    _cancelGCWait();
    _unsubscribeFromState();
    super.dispose();
  }

  /// An arriving value, already the widget's state by the time this runs: the registry
  /// applied it before telling anyone. This stays the hook an impl overrides, and the
  /// one place the rebuild happens.
  @protected
  void setValue(V value) {
    if (!mounted) return;
    // Profiler mark per widget kind, only when the page asked for marks (window.evolve.perf):
    // every update comes through here, so its name is built only then.
    if (perfMarksEnabled) {
      perfMarkDetail('${state.swt}.apply', _rebuild);
    } else {
      _rebuild();
    }
  }

  /// [_rebuildNow] and [extraSetState], torn off once: an update rebuilds without a closure
  /// made for it.
  late final void Function() _rebuild = _rebuildNow;
  late final void Function() _extraSetState = extraSetState;

  void _rebuildNow() => setState(_extraSetState);

  /// An impl's chance to react to the state it is about to render.
  void extraSetState() {}

  /// Stable id for E2E tooling. Flutter Web exposes this as
  /// `flt-semantics-identifier`, keyed by the same swt/id pair as the comm channel.
  ///
  /// Tooltip and enabled state are additive when semantics are active. We deliberately
  /// do not set `label` here; see `_taggedSemantics`.
  Widget tagSemantics(Widget child) => _taggedSemantics(state, child);

  /// Same as [tagSemantics] but for an SWT Item (TableItem, MenuItem, etc.) rendered
  /// inline by its parent's build() rather than via its own State — `item` is that
  /// item's own VWidget (its swt/id), not this state's.
  Widget tagItemSemantics(VWidget item, Widget child) =>
      _taggedSemantics(item, child);

  static const _valueTextSwt = {
    'Text',
    'Combo',
    'CCombo',
    'StyledText',
    'Browser',
  };

  Widget _taggedSemantics(VWidget node, Widget child) {
    final identifier = '${node.swt}/${node.id}';
    // No semantics tree is being built, so the identifier would never surface in the
    // DOM anyway — skip the Semantics wrapper entirely and keep rendering cheap.
    if (!WidgetsBinding.instance.semanticsEnabled) return child;
    final tooltip = node.semanticsTooltip;
    final text = node.semanticsText;
    final namedByOwnText =
        !_valueTextSwt.contains(node.swt) &&
        text != null &&
        text.trim().isNotEmpty;
    // Do not set a label from the widget's text: the child already contributes that
    // text to semantics, and Flutter Web would expose duplicated DOM text.
    return Semantics(
      identifier: identifier,
      // A tooltip-only-named node has no accessible name, so Flutter Web collapses it
      // into its unnamed child leaf and drops the flt-semantics-identifier; keep it an
      // explicit container instead. Nodes named by their own text, or by a child's
      // merged label (a GC-drawn Canvas), rely on that merge and are left alone.
      explicitChildNodes:
          !namedByOwnText && tooltip != null && tooltip.isNotEmpty,
      tooltip: namedByOwnText || tooltip == null || tooltip.isEmpty
          ? null
          : tooltip,
      hint: namedByOwnText && tooltip != null && tooltip.isNotEmpty
          ? tooltip
          : null,
      enabled: node.semanticsEnabled,
      child: child,
    );
  }
}

class VWidget implements WireReadable, WireWritable, WireVisitable {
  VWidget() : this.empty();
  VWidget.empty() : swt = "Widget", id = 1, style = 0;

  String swt;
  int id;

  /// Write stamp from the Java serializer; a lower value is an older snapshot.
  int seq = 0;

  /// The number of this widget's type on the wire, worked out once.
  int get wireType => _wireType ??= WidgetKey.typeOf(swt);
  int? _wireType;

  /// True when this object arrived as a name rather than as a description: the sender said
  /// which widget it is and nothing about what it holds, because the widget was already
  /// delivered and had not changed. Read by the registry, which answers with the object it
  /// already holds; nothing here is state, so copying any of it over that would be a loss.
  bool isReference = false;
  int style;

  /// Sets one property from a binary frame: [key] is its [WireKey], [type] the type Java wrote
  /// its value with. False, with nothing read, when no class up to this one declares the
  /// property as something that type can be read into.
  @override
  bool readWire(int key, WidgetWireReader r) {
    if (key != 657) return false; // style
    style = r.int32();
    return true;
  }

  /// Writes every property this value holds, as Java writes them.
  @override
  void writeWire(WidgetWireWriter w) {
    w.keyInt32(657, style);
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(657, style);
  }

  /// Copies the properties this class declares from [other].
  void copyFrom(VWidget other) {
    style = other.style;
  }

  /// Hands every widget this one references to [adopt], keeping whatever comes back.
  ///
  /// A value that arrives inside another - a child in a composite, an item in a table -
  /// describes a widget that exists in its own right and may already be held elsewhere,
  /// possibly in a newer state than the copy that just arrived. Substituting what comes
  /// back is what makes a parent's child list a list of the actual widgets rather than of
  /// snapshots taken whenever the parent was last written.
  void adoptChildren(VWidget Function(VWidget) adopt) {}

  /// [adoptChildren] for a single reference. Returns what to keep.
  static T adoptOne<T extends VWidget?>(
    T value,
    VWidget Function(VWidget) adopt,
  ) => value == null ? value : adopt(value) as T;

  /// [adoptChildren] for a list of them, substituted in place.
  static void adoptEach<T extends VWidget>(
    List<T>? values,
    VWidget Function(VWidget) adopt,
  ) {
    if (values == null) return;
    for (var i = 0; i < values.length; i++) {
      values[i] = adopt(values[i]) as T;
    }
  }

  /// The tooltip the semantics tree announces; see `WidgetSwtState.tagSemantics`.
  String? get semanticsTooltip => null;

  /// Whether the semantics tree announces this widget as enabled, when it says.
  bool? get semanticsEnabled => null;

  /// The caption the widget draws, which names it in the semantics tree before its tooltip.
  String? get semanticsText => null;
}
