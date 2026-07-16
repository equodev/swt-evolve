import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../impl/gc_evolve.dart';
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
  late V state;

  // GC support - any widget can have a GC overlay
  VGC? gcOverlay;
  final GlobalKey<GCImpl> gcOverlayKey = GlobalKey<GCImpl>();
  GlobalKey? widgetBoundaryKey;

  Object? _onChangeToken;

  @override
  void initState() {
    super.initState();
    state = widget.value as V;
    _onChangeToken = EquoCommService.on("${state.swt}/${state.id}", _onChange);
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

  /// Wrap child widget with GC overlay.
  /// GC is always created so it can listen for events from Java immediately.
  /// Uses Offstage when GC has no state yet (listening but not rendering).
  Widget wrapWithGCOverlay(Widget child) {
    final gc = gcOverlay ?? (VGC()..id = state.id);
    final gcWidget = GCSwt<VGC>(key: gcOverlayKey, value: gc);

    return Stack(
      children: [
        child,
        if (gcOverlay != null)
          Positioned.fill(child: IgnorePointer(child: gcWidget))
        else
          Offstage(child: gcWidget),
      ],
    );
  }

  @override
  void didUpdateWidget(covariant T oldWidget) {
    super.didUpdateWidget(oldWidget);
    state = widget.value as V;
    extraSetState();
  }

  @override
  void dispose() {
    EquoCommService.remove("${state.swt}/${state.id}", _onChangeToken);
    super.dispose();
  }

  @protected
  void setValue(V value) {
    if (!mounted) return;
    // Skip redundant rebuilds: identical state still regenerates Flutter Web
    // semantics nodes and can detach E2E locators mid-action. That is purely an E2E
    // concern, and the JSON compare in _isSameValue is too expensive to run on every
    // update — so only pay it when the semantics tree is actually on (test mode /
    // screen reader). Normal rendering rebuilds unconditionally, as before.
    if (WidgetsBinding.instance.semanticsEnabled &&
        _isSameValue(state, value)) {
      return;
    }
    setState(() {
      state = value;
      extraSetState();
    });
  }

  /// Structural equality between two value objects via their JSON form.
  /// Falls back to "not equal" (i.e. rebuild) if either can't be serialized,
  /// so we never drop a legitimate update.
  bool _isSameValue(V a, V b) {
    if (identical(a, b)) return true;
    try {
      return jsonEncode(a.toJson()) == jsonEncode(b.toJson());
    } catch (_) {
      return false;
    }
  }

  void _onChange(V payload) {
    setValue(payload);
  }

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
  int style;

  factory VWidget.fromJson(Map<String, dynamic> json) => mapWidgetValue(json);
  Map<String, dynamic> toJson() =>
      throw UnsupportedError("Unsupported toJson in Widget");
}
