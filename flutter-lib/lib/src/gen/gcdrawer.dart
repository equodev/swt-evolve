import 'dart:convert';
import '../comm/comm.dart';
import '../gen/gc.dart';

abstract class GCDrawerBase {
  VGC state;

  // Channel -> token, so teardown removes only handlers this drawer registered and never a newer
  // drawer's live handler for the same id.
  final Map<String, Object> _handlerTokens = {};

  GCDrawerBase(this.state) {
    _handlerTokens["${state.swt}/${state.id}"] = EquoCommService.on(
      "${state.swt}/${state.id}",
      _trackChanges,
    );
    _registerOps();
  }

  void _trackChanges(VGC newState) {
    state = newState;
    onStateChanged(newState);
  }

  void onStateChanged(VGC newState) {}

  void _op(String name, void Function(Map<String, dynamic>) fn) {
    final channel = "${state.swt}/${state.id}/$name";
    _handlerTokens[channel] = EquoCommService.onRaw(channel, (raw) {
      try {
        final map = raw is String
            ? jsonDecode(raw) as Map<String, dynamic>
            : raw as Map<String, dynamic>;
        fn(map);
      } catch (e) {
        print('[GC DrawerBase] Error in $name: $e');
      }
    });
  }

  void _registerOps() {
    _op(
      "copyAreaImageintint",
      (p) => onCopyAreaImageintint(VGCCopyAreaImageintint.fromJson(p)),
    );
    _op(
      "copyAreaintintintintintint",
      (p) => onCopyAreaintintintintintint(
        VGCCopyAreaintintintintintint.fromJson(p),
      ),
    );
    _op(
      "copyAreaintintintintintintboolean",
      (p) => onCopyAreaintintintintintintboolean(
        VGCCopyAreaintintintintintintboolean.fromJson(p),
      ),
    );
    _op(
      "drawArcintintintintintint",
      (p) =>
          onDrawArcintintintintintint(VGCDrawArcintintintintintint.fromJson(p)),
    );
    _op(
      "drawFocusintintintint",
      (p) => onDrawFocusintintintint(VGCDrawFocusintintintint.fromJson(p)),
    );
    _op(
      "drawImageImageintint",
      (p) => onDrawImageImageintint(VGCDrawImageImageintint.fromJson(p)),
    );
    _op(
      "drawImageImageintintintint",
      (p) => onDrawImageImageintintintint(
        VGCDrawImageImageintintintint.fromJson(p),
      ),
    );
    _op(
      "drawImageImageintintintintintintintint",
      (p) => onDrawImageImageintintintintintintintint(
        VGCDrawImageImageintintintintintintintint.fromJson(p),
      ),
    );
    _op(
      "drawLineintintintint",
      (p) => onDrawLineintintintint(VGCDrawLineintintintint.fromJson(p)),
    );
    _op(
      "drawOvalintintintint",
      (p) => onDrawOvalintintintint(VGCDrawOvalintintintint.fromJson(p)),
    );
    _op(
      "drawPointintint",
      (p) => onDrawPointintint(VGCDrawPointintint.fromJson(p)),
    );
    _op(
      "drawPolygonint",
      (p) => onDrawPolygonint(VGCDrawPolygonint.fromJson(p)),
    );
    _op(
      "drawPolylineint",
      (p) => onDrawPolylineint(VGCDrawPolylineint.fromJson(p)),
    );
    _op(
      "drawRectangleRectangle",
      (p) => onDrawRectangleRectangle(VGCDrawRectangleRectangle.fromJson(p)),
    );
    _op(
      "drawRectangleintintintint",
      (p) =>
          onDrawRectangleintintintint(VGCDrawRectangleintintintint.fromJson(p)),
    );
    _op(
      "drawRoundRectangleintintintintintint",
      (p) => onDrawRoundRectangleintintintintintint(
        VGCDrawRoundRectangleintintintintintint.fromJson(p),
      ),
    );
    _op(
      "drawStringStringintint",
      (p) => onDrawStringStringintint(VGCDrawStringStringintint.fromJson(p)),
    );
    _op(
      "drawStringStringintintboolean",
      (p) => onDrawStringStringintintboolean(
        VGCDrawStringStringintintboolean.fromJson(p),
      ),
    );
    _op(
      "drawTextStringintint",
      (p) => onDrawTextStringintint(VGCDrawTextStringintint.fromJson(p)),
    );
    _op(
      "drawTextStringintintboolean",
      (p) => onDrawTextStringintintboolean(
        VGCDrawTextStringintintboolean.fromJson(p),
      ),
    );
    _op(
      "drawTextStringintintint",
      (p) => onDrawTextStringintintint(VGCDrawTextStringintintint.fromJson(p)),
    );
    _op(
      "fillArcintintintintintint",
      (p) =>
          onFillArcintintintintintint(VGCFillArcintintintintintint.fromJson(p)),
    );
    _op(
      "fillGradientRectangleintintintintboolean",
      (p) => onFillGradientRectangleintintintintboolean(
        VGCFillGradientRectangleintintintintboolean.fromJson(p),
      ),
    );
    _op(
      "fillOvalintintintint",
      (p) => onFillOvalintintintint(VGCFillOvalintintintint.fromJson(p)),
    );
    _op(
      "fillPolygonint",
      (p) => onFillPolygonint(VGCFillPolygonint.fromJson(p)),
    );
    _op(
      "fillRectangleRectangle",
      (p) => onFillRectangleRectangle(VGCFillRectangleRectangle.fromJson(p)),
    );
    _op(
      "fillRectangleintintintint",
      (p) =>
          onFillRectangleintintintint(VGCFillRectangleintintintint.fromJson(p)),
    );
    _op(
      "fillRoundRectangleintintintintintint",
      (p) => onFillRoundRectangleintintintintintint(
        VGCFillRoundRectangleintintintintintint.fromJson(p),
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
  void onCopyAreaintintintintintint(VGCCopyAreaintintintintintint opArgs);
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
  void onDrawPointintint(VGCDrawPointintint opArgs);
  void onDrawPolygonint(VGCDrawPolygonint opArgs);
  void onDrawPolylineint(VGCDrawPolylineint opArgs);
  void onDrawRectangleRectangle(VGCDrawRectangleRectangle opArgs);
  void onDrawRectangleintintintint(VGCDrawRectangleintintintint opArgs);
  void onDrawRoundRectangleintintintintintint(
    VGCDrawRoundRectangleintintintintintint opArgs,
  );
  void onDrawStringStringintint(VGCDrawStringStringintint opArgs);
  void onDrawStringStringintintboolean(VGCDrawStringStringintintboolean opArgs);
  void onDrawTextStringintint(VGCDrawTextStringintint opArgs);
  void onDrawTextStringintintboolean(VGCDrawTextStringintintboolean opArgs);
  void onDrawTextStringintintint(VGCDrawTextStringintintint opArgs);
  void onFillArcintintintintintint(VGCFillArcintintintintintint opArgs);
  void onFillGradientRectangleintintintintboolean(
    VGCFillGradientRectangleintintintintboolean opArgs,
  );
  void onFillOvalintintintint(VGCFillOvalintintintint opArgs);
  void onFillPolygonint(VGCFillPolygonint opArgs);
  void onFillRectangleRectangle(VGCFillRectangleRectangle opArgs);
  void onFillRectangleintintintint(VGCFillRectangleintintintint opArgs);
  void onFillRoundRectangleintintintintintint(
    VGCFillRoundRectangleintintintintintint opArgs,
  );
}
