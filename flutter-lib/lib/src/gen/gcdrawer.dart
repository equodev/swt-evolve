import 'dart:convert';
import '../comm/comm.dart';
import '../comm/delivery_gate.dart';
import '../gen/gc.dart';
import '../gen/widgets.dart';

abstract class GCDrawerBase {
  VGC state;

  // Channel -> token, so teardown removes only handlers this drawer registered and never a newer
  // drawer's live handler for the same id.
  final Map<String, Object> _handlerTokens = {};

  GCDrawerBase(this.state) {
    final stateChannel = "${state.swt}/${state.id}";
    _handlerTokens[stateChannel] = EquoCommService.onRaw(stateChannel, (raw) {
      try {
        final map = raw is String
            ? jsonDecode(raw) as Map<String, dynamic>
            : raw as Map<String, dynamic>;
        _trackChanges(map);
      } catch (e) {
        print('[GC DrawerBase] Error in state: $e');
      }
    });
    _registerOps();
  }

  /// Takes a whole description of the GC, or a change to the one already held.
  ///
  /// Java sends a change only when this channel still holds the state it was computed from: the
  /// same GC described it last, and the channel delivers in order.
  void _trackChanges(Map<String, dynamic> frame) {
    if (frame.containsKey(kChangedKeys)) {
      state.mergeJson(frame);
    } else {
      state = mapWidgetValue(frame) as VGC;
    }
    onStateChanged(state);
  }

  void onStateChanged(VGC newState) {}

  void _op(String name, void Function(Object?) fn) {
    final channel = "${state.swt}/${state.id}/$name";
    _handlerTokens[channel] = EquoCommService.onRaw(channel, (raw) {
      try {
        fn(raw is String ? jsonDecode(raw) : raw);
      } catch (e) {
        print('[GC DrawerBase] Error in $name: $e');
      }
    });
  }

  void _registerOps() {
    _op(
      "copyAreaImageintint",
      (p) => onCopyAreaImageintint(VGCCopyAreaImageintint.fromWire(p)),
    );
    _op(
      "copyAreaintintintintintintboolean",
      (p) => onCopyAreaintintintintintintboolean(
        VGCCopyAreaintintintintintintboolean.fromWire(p),
      ),
    );
    _op(
      "drawArcintintintintintint",
      (p) =>
          onDrawArcintintintintintint(VGCDrawArcintintintintintint.fromWire(p)),
    );
    _op(
      "drawFocusintintintint",
      (p) => onDrawFocusintintintint(VGCDrawFocusintintintint.fromWire(p)),
    );
    _op(
      "drawImageImageintint",
      (p) => onDrawImageImageintint(VGCDrawImageImageintint.fromWire(p)),
    );
    _op(
      "drawImageImageintintintint",
      (p) => onDrawImageImageintintintint(
        VGCDrawImageImageintintintint.fromWire(p),
      ),
    );
    _op(
      "drawImageImageintintintintintintintint",
      (p) => onDrawImageImageintintintintintintintint(
        VGCDrawImageImageintintintintintintintint.fromWire(p),
      ),
    );
    _op(
      "drawLineintintintint",
      (p) => onDrawLineintintintint(VGCDrawLineintintintint.fromWire(p)),
    );
    _op(
      "drawOvalintintintint",
      (p) => onDrawOvalintintintint(VGCDrawOvalintintintint.fromWire(p)),
    );
    _op("drawPathPath", (p) => onDrawPathPath(VGCDrawPathPath.fromWire(p)));
    _op(
      "drawPointintint",
      (p) => onDrawPointintint(VGCDrawPointintint.fromWire(p)),
    );
    _op(
      "drawPolygonint",
      (p) => onDrawPolygonint(VGCDrawPolygonint.fromWire(p)),
    );
    _op(
      "drawPolylineint",
      (p) => onDrawPolylineint(VGCDrawPolylineint.fromWire(p)),
    );
    _op(
      "drawRectangleintintintint",
      (p) =>
          onDrawRectangleintintintint(VGCDrawRectangleintintintint.fromWire(p)),
    );
    _op(
      "drawRoundRectangleintintintintintint",
      (p) => onDrawRoundRectangleintintintintintint(
        VGCDrawRoundRectangleintintintintintint.fromWire(p),
      ),
    );
    _op(
      "drawTextStringintintint",
      (p) => onDrawTextStringintintint(VGCDrawTextStringintintint.fromWire(p)),
    );
    _op(
      "fillArcintintintintintint",
      (p) =>
          onFillArcintintintintintint(VGCFillArcintintintintintint.fromWire(p)),
    );
    _op(
      "fillGradientRectangleintintintintboolean",
      (p) => onFillGradientRectangleintintintintboolean(
        VGCFillGradientRectangleintintintintboolean.fromWire(p),
      ),
    );
    _op(
      "fillOvalintintintint",
      (p) => onFillOvalintintintint(VGCFillOvalintintintint.fromWire(p)),
    );
    _op("fillPathPath", (p) => onFillPathPath(VGCFillPathPath.fromWire(p)));
    _op(
      "fillPolygonint",
      (p) => onFillPolygonint(VGCFillPolygonint.fromWire(p)),
    );
    _op(
      "fillRectangleintintintint",
      (p) =>
          onFillRectangleintintintint(VGCFillRectangleintintintint.fromWire(p)),
    );
    _op(
      "fillRoundRectangleintintintintintint",
      (p) => onFillRoundRectangleintintintintintint(
        VGCFillRoundRectangleintintintintintint.fromWire(p),
      ),
    );
  }

  void dispose() {
    _handlerTokens.forEach((channel, token) {
      EquoCommService.remove(channel, token);
    });
    _handlerTokens.clear();
  }

  void onCopyAreaImageintint(VGCCopyAreaImageintint opArgs);
  void onCopyAreaintintintintintintboolean(
    VGCCopyAreaintintintintintintboolean opArgs,
  );
  void onDrawArcintintintintintint(VGCDrawArcintintintintintint opArgs);
  void onDrawFocusintintintint(VGCDrawFocusintintintint opArgs);
  void onDrawImageImageintint(VGCDrawImageImageintint opArgs);
  void onDrawImageImageintintintint(VGCDrawImageImageintintintint opArgs);
  void onDrawImageImageintintintintintintintint(
    VGCDrawImageImageintintintintintintintint opArgs,
  );
  void onDrawLineintintintint(VGCDrawLineintintintint opArgs);
  void onDrawOvalintintintint(VGCDrawOvalintintintint opArgs);
  void onDrawPathPath(VGCDrawPathPath opArgs);
  void onDrawPointintint(VGCDrawPointintint opArgs);
  void onDrawPolygonint(VGCDrawPolygonint opArgs);
  void onDrawPolylineint(VGCDrawPolylineint opArgs);
  void onDrawRectangleintintintint(VGCDrawRectangleintintintint opArgs);
  void onDrawRoundRectangleintintintintintint(
    VGCDrawRoundRectangleintintintintintint opArgs,
  );
  void onDrawTextStringintintint(VGCDrawTextStringintintint opArgs);
  void onFillArcintintintintintint(VGCFillArcintintintintintint opArgs);
  void onFillGradientRectangleintintintintboolean(
    VGCFillGradientRectangleintintintintboolean opArgs,
  );
  void onFillOvalintintintint(VGCFillOvalintintintint opArgs);
  void onFillPathPath(VGCFillPathPath opArgs);
  void onFillPolygonint(VGCFillPolygonint opArgs);
  void onFillRectangleintintintint(VGCFillRectangleintintintint opArgs);
  void onFillRoundRectangleintintintintintint(
    VGCFillRoundRectangleintintintintintint opArgs,
  );
}
