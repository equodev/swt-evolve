import 'dart:convert';
import 'package:flutter/foundation.dart';
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
    // print("send ${val.swt}/${val.id}/$ev");
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

// Global map: widget id → latest value received from Java via onChange.
// Used in didUpdateWidget to avoid a stale parent value overwriting a fresher
// child state that already arrived from Java.
final Map<int, VWidget> _latestKnownValues = {};

@visibleForTesting
void injectLatestKnownValue(int id, VWidget value) {
  _latestKnownValues[id] = value;
}

abstract class WidgetSwtState<T extends WidgetSwt, V extends VWidget>
    extends State<T> {
  late V state;

  // GC support - any widget can have a GC overlay
  VGC? gcOverlay;
  final GlobalKey<GCImpl> gcOverlayKey = GlobalKey<GCImpl>();
  GlobalKey? widgetBoundaryKey;

  @override
  void initState() {
    super.initState();
    state = widget.value as V;
    EquoCommService.on("${state.swt}/${state.id}", _onChange);
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
        // This causes it not to work composite widget
        // if (gcOverlay != null)
        //   RepaintBoundary(key: widgetBoundaryKey, child: child)
        // else
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
    final incoming = widget.value as V;
    final latest = _latestKnownValues[incoming.id];
    state = (latest is V) ? latest : incoming;
    extraSetState();
  }

  @override
  void dispose() {
    _latestKnownValues.remove(state.id);
    EquoCommService.remove("${state.swt}/${state.id}");
    super.dispose();
  }

  @protected
  void setValue(V value) {
    if (mounted) {
      setState(() {
        state = value!;
        extraSetState();
      });
    }
  }

  void _onChange(V payload) {
    _latestKnownValues[payload.id] = payload;
    setValue(payload);
  }

  void extraSetState() {}

  void onOp(String op, void Function(dynamic) handler) {
    EquoCommService.onRaw("${state.swt}/${state.id}/$op", (opArgs) {
      print('OnOp: "${state.swt}/${state.id}/$op" args: ${opArgs}');
      handler(jsonDecode(opArgs as String));
    });
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
