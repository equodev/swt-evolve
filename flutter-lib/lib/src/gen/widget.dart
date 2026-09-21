import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../comm/v_registry.dart';
import '../gen/widget.dart';
import '../impl/gc_evolve.dart';
import '../impl/widget_config.dart';
import 'event.dart';
import 'gc.dart';
import 'widgets.dart';

part 'widget.g.dart';

abstract class WidgetSwt<V extends VWidget> extends StatefulWidget {
  final V value;
  const WidgetSwt({super.key, required this.value});

  void sendEvent(V val, String ev, VEvent? payload) {
    if (payload == null) {
      EquoCommService.send("${val.swt}/${val.id}/$ev");
    } else {
      EquoCommService.sendPayload("${val.swt}/${val.id}/$ev", payload);
    }
  }

  void sendDisposeDispose(V val, VEvent? payload) {
    sendEvent(val, "Dispose/Dispose", payload);
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
  V get state =>
      _override ??
      (VRegistry.instance.valueOn(_onChangeChannel ?? '') as V?) ??
      (widget.value as V);

  set state(V value) => _override = value;

  // GC support - any widget can have a GC overlay
  VGC? gcOverlay;
  final GlobalKey<GCImpl> gcOverlayKey = GlobalKey<GCImpl>();
  GlobalKey? widgetBoundaryKey;

  String? _onChangeChannel;

  @override
  void initState() {
    super.initState();
    _register();
  }

  /// Puts this widget's value where values live, and asks to be told when it changes.
  ///
  /// The channel is remembered rather than derived on demand: [state] resolves through
  /// it, so deriving it from the state would be circular.
  void _register() {
    final value = widget.value as V;
    _onChangeChannel = VRegistry.channelOf(value);
    VRegistry.instance.register(value);
    VRegistry.instance.watch(_onChangeChannel!, _onRegistryChanged);
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
    final channel = _onChangeChannel;
    if (channel != null) {
      VRegistry.instance.unwatch(channel, _onRegistryChanged);
    }
    _onChangeChannel = null;
  }

  /// Called by GCSwt when it receives state from Java.
  /// Switches the GC overlay from Offstage to visible.
  void notifyGCReady(VGC gcValue) {
    if (gcOverlay == null) {
      setState(() {
        gcOverlay = gcValue;
        widgetBoundaryKey ??= GlobalKey();
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
  /// GC is always created so it can listen for events from Java immediately.
  /// Uses Offstage when GC has no state yet, or when the overlay is opted out of
  /// (listening but not rendering).
  Widget wrapWithGCOverlay(Widget child) {
    final gc = gcOverlay ?? (VGC()..id = state.id);
    final gcWidget = GCSwt<VGC>(key: gcOverlayKey, value: gc);

    return Stack(
      children: [
        child,
        if (gcOverlay != null && paintsGCOverlay)
          Positioned.fill(child: IgnorePointer(child: gcWidget))
        else
          Offstage(child: gcWidget),
      ],
    );
  }

  @override
  void didUpdateWidget(covariant T oldWidget) {
    super.didUpdateWidget(oldWidget);
    final incoming = widget.value as V;
    // Nothing is adopted from the parent any more. The value it hands down is its own
    // copy of this widget, as of whenever Java last described the parent - which is why
    // a rebuild used to be able to push a child backwards. Only the identity is taken.
    final channel = VRegistry.channelOf(incoming);
    if (channel != _onChangeChannel) {
      _unsubscribeFromState();
      _onChangeChannel = channel;
      VRegistry.instance.register(incoming);
      VRegistry.instance.watch(channel, _onRegistryChanged);
    }
    extraSetState();
  }

  @override
  void dispose() {
    _unsubscribeFromState();
    super.dispose();
  }

  /// An arriving value, already the widget's state by the time this runs: the registry
  /// applied it before telling anyone. This stays the hook an impl overrides, and the
  /// one place the rebuild happens.
  @protected
  void setValue(V value) {
    if (!mounted) return;
    setState(() {
      extraSetState();
    });
  }

  /// An impl's chance to react to the state it is about to render.
  void extraSetState() {}

  void onOp(String op, void Function(dynamic) handler) {
    EquoCommService.onRaw("${state.swt}/${state.id}/$op", (opArgs) {
      print('OnOp: "${state.swt}/${state.id}/$op" args: ${opArgs}');
      handler(jsonDecode(opArgs as String));
    });
  }

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

  Widget _taggedSemantics(VWidget node, Widget child) {
    final identifier = '${node.swt}/${node.id}';
    // Only pay the toJson() cost when the semantics tree is actually being built
    // (screen reader, or test mode forcing it on). Normal rendering stays cheap.
    if (!WidgetsBinding.instance.semanticsEnabled) {
      // No semantics tree is being built, so the identifier would never surface in the
      // DOM anyway — skip the Semantics wrapper entirely and keep rendering cheap.
      return child;
    }
    Map<String, dynamic> json;
    try {
      json = (node as dynamic).toJson() as Map<String, dynamic>;
    } catch (_) {
      json = const {};
    }
    final tooltip = json['toolTipText'];
    final enabled = json['enabled'];
    // Do not set label from json['text']: the child already contributes that text to
    // semantics, and Flutter Web would expose duplicated DOM text.
    return Semantics(
      identifier: identifier,
      tooltip: (tooltip is String && tooltip.isNotEmpty) ? tooltip : null,
      enabled: enabled is bool ? enabled : null,
      child: child,
    );
  }
}

@JsonSerializable()
class VWidget {
  VWidget() : this.empty();
  VWidget.empty() : swt = "Widget", id = 1, style = 0;

  String swt;
  int id;

  /// Write stamp from the Java serializer; a lower value is an older snapshot.
  /// Out of toJson so it never affects value equality or round-trips.
  @JsonKey(name: '_s', includeToJson: false, defaultValue: 0)
  int seq = 0;

  /// True when this object arrived as a name rather than as a description: the sender said
  /// which widget it is and nothing about what it holds, because the widget was already
  /// delivered and had not changed. Read by the registry, which answers with the object it
  /// already holds; nothing here is state, so copying any of it over that would be a loss.
  ///
  /// Out of the JSON both ways: it is a fact about one message, not about the widget.
  @JsonKey(includeToJson: false, includeFromJson: false)
  bool isReference = false;
  @JsonKey(defaultValue: 0)
  int style;

  /// Applies a partial update: the properties it names replace what is held here, in place.
  ///
  /// Costs what the update names, not what the widget holds. Reading the whole value back
  /// to write one field made a change on a widget with many children cost the children.
  void mergeJson(Map<String, dynamic> json) {
    for (final key in json.keys) {
      readProperty(key, json);
    }
  }

  /// Reads one named property out of [json] into this object.
  ///
  /// A key this class does not declare goes up to the class that does; one nothing declares
  /// - a protocol key, or a property this build has never heard of - is ignored.
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'swt':
        swt = json['swt'] as String;
      case 'id':
        id = (json['id'] as num).toInt();
      case 'style':
        style = (json['style'] as num).toInt();
    }
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

  factory VWidget.fromJson(Map<String, dynamic> json) => mapWidgetValue(json);
  Map<String, dynamic> toJson() =>
      throw UnsupportedError("Unsupported toJson in Widget");
}
