import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import '../comm/comm.dart';
import '../comm/delivery_gate.dart';
import '../comm/gc_wire.dart';
import '../gen/gc.dart';
import '../gen/widgets.dart';

/// Each op's argument types, by the number a frame defines it with (see GcWireReader).
const Map<int, String> gcOpSignatures = {
  865: 'wii',
  866: 'iiiiiib',
  867: 'iiiiii',
  868: 'iiii',
  869: 'wii',
  870: 'wiiii',
  871: 'wiiiiiiii',
  872: 'iiii',
  873: 'iiii',
  874: 'w',
  875: 'ii',
  876: 'I',
  877: 'I',
  878: 'iiii',
  879: 'iiiiii',
  880: 'siii',
  881: 'iiiiii',
  882: 'iiiib',
  883: 'iiii',
  884: 'w',
  885: 'I',
  886: 'iiii',
  887: 'iiiiii',
};

abstract class GCDrawerBase {
  VGC state;

  // Channel -> token, so teardown removes only handlers this drawer registered and never a newer
  // drawer's live handler for the same id.
  final Map<String, Object> _handlerTokens = {};

  GCDrawerBase(this.state) {
    final frameChannel = "${state.swt}/${state.id}/b";
    _handlerTokens[frameChannel] = EquoCommService.onRaw(
      frameChannel,
      (raw) => _applyFrame(GcWireReader(raw as Uint8List), []),
    );
  }

  /// A paint as one binary frame: ops, the state they draw with, and entries that travel on channels
  /// of their own, in the order they were drawn. [handlers] holds, per op number, how this drawer
  /// applies the op.
  FutureOr<void> _applyFrame(
    GcWireReader r,
    List<void Function(GcWireReader)?> handlers,
  ) {
    while (r.hasMore) {
      final tag = r.varint();
      if (tag == GcWireReader.state) {
        // A change to the state already held: each property goes straight to it.
        r.wire().fields(state);
        onStateChanged(state);
      } else if (tag == GcWireReader.whole) {
        // The whole state, from the value class's defaults: what it leaves out is at its default.
        state = r.wire().fields(
          VGC.empty()
            ..id = state.id
            ..swt = state.swt
            ..style = state.style,
        );
        onStateChanged(state);
      } else if (tag == GcWireReader.escape) {
        final channel = r.channel();
        final delivered = EquoCommService.deliverEntry(channel, r.payload());
        // Anything after it waits, as the ops behind a frame that awaits do.
        if (delivered is Future) {
          return delivered.then((_) => _applyFrame(r, handlers));
        }
      } else {
        final number = r.opNumber(tag);
        if (number == handlers.length) {
          handlers.add(_binaryOp(r.opNames[number]));
        }
        final handler = handlers[number];
        if (handler == null) {
          r.skip(r.signatures[number]);
        } else {
          handler(r);
        }
      }
    }
    return null;
  }

  void Function(GcWireReader)? _binaryOp(String name) {
    switch (name) {
      case "copyAreaImageintint":
        return (r) {
          final op = VGCCopyAreaImageintint.fromBinary(r);
          try {
            onCopyAreaImageintint(op);
          } catch (e) {
            _failed("copyAreaImageintint", e);
          }
        };
      case "copyAreaintintintintintintboolean":
        return (r) {
          final op = VGCCopyAreaintintintintintintboolean.fromBinary(r);
          try {
            onCopyAreaintintintintintintboolean(op);
          } catch (e) {
            _failed("copyAreaintintintintintintboolean", e);
          }
        };
      case "drawArcintintintintintint":
        return (r) {
          final op = VGCDrawArcintintintintintint.fromBinary(r);
          try {
            onDrawArcintintintintintint(op);
          } catch (e) {
            _failed("drawArcintintintintintint", e);
          }
        };
      case "drawFocusintintintint":
        return (r) {
          final op = VGCDrawFocusintintintint.fromBinary(r);
          try {
            onDrawFocusintintintint(op);
          } catch (e) {
            _failed("drawFocusintintintint", e);
          }
        };
      case "drawImageImageintint":
        return (r) {
          final op = VGCDrawImageImageintint.fromBinary(r);
          try {
            onDrawImageImageintint(op);
          } catch (e) {
            _failed("drawImageImageintint", e);
          }
        };
      case "drawImageImageintintintint":
        return (r) {
          final op = VGCDrawImageImageintintintint.fromBinary(r);
          try {
            onDrawImageImageintintintint(op);
          } catch (e) {
            _failed("drawImageImageintintintint", e);
          }
        };
      case "drawImageImageintintintintintintintint":
        return (r) {
          final op = VGCDrawImageImageintintintintintintintint.fromBinary(r);
          try {
            onDrawImageImageintintintintintintintint(op);
          } catch (e) {
            _failed("drawImageImageintintintintintintintint", e);
          }
        };
      case "drawLineintintintint":
        return (r) {
          final op = VGCDrawLineintintintint.fromBinary(r);
          try {
            onDrawLineintintintint(op);
          } catch (e) {
            _failed("drawLineintintintint", e);
          }
        };
      case "drawOvalintintintint":
        return (r) {
          final op = VGCDrawOvalintintintint.fromBinary(r);
          try {
            onDrawOvalintintintint(op);
          } catch (e) {
            _failed("drawOvalintintintint", e);
          }
        };
      case "drawPathPath":
        return (r) {
          final op = VGCDrawPathPath.fromBinary(r);
          try {
            onDrawPathPath(op);
          } catch (e) {
            _failed("drawPathPath", e);
          }
        };
      case "drawPointintint":
        return (r) {
          final op = VGCDrawPointintint.fromBinary(r);
          try {
            onDrawPointintint(op);
          } catch (e) {
            _failed("drawPointintint", e);
          }
        };
      case "drawPolygonint":
        return (r) {
          final op = VGCDrawPolygonint.fromBinary(r);
          try {
            onDrawPolygonint(op);
          } catch (e) {
            _failed("drawPolygonint", e);
          }
        };
      case "drawPolylineint":
        return (r) {
          final op = VGCDrawPolylineint.fromBinary(r);
          try {
            onDrawPolylineint(op);
          } catch (e) {
            _failed("drawPolylineint", e);
          }
        };
      case "drawRectangleintintintint":
        return (r) {
          final op = VGCDrawRectangleintintintint.fromBinary(r);
          try {
            onDrawRectangleintintintint(op);
          } catch (e) {
            _failed("drawRectangleintintintint", e);
          }
        };
      case "drawRoundRectangleintintintintintint":
        return (r) {
          final op = VGCDrawRoundRectangleintintintintintint.fromBinary(r);
          try {
            onDrawRoundRectangleintintintintintint(op);
          } catch (e) {
            _failed("drawRoundRectangleintintintintintint", e);
          }
        };
      case "drawTextStringintintint":
        return (r) {
          final op = VGCDrawTextStringintintint.fromBinary(r);
          try {
            onDrawTextStringintintint(op);
          } catch (e) {
            _failed("drawTextStringintintint", e);
          }
        };
      case "fillArcintintintintintint":
        return (r) {
          final op = VGCFillArcintintintintintint.fromBinary(r);
          try {
            onFillArcintintintintintint(op);
          } catch (e) {
            _failed("fillArcintintintintintint", e);
          }
        };
      case "fillGradientRectangleintintintintboolean":
        return (r) {
          final op = VGCFillGradientRectangleintintintintboolean.fromBinary(r);
          try {
            onFillGradientRectangleintintintintboolean(op);
          } catch (e) {
            _failed("fillGradientRectangleintintintintboolean", e);
          }
        };
      case "fillOvalintintintint":
        return (r) {
          final op = VGCFillOvalintintintint.fromBinary(r);
          try {
            onFillOvalintintintint(op);
          } catch (e) {
            _failed("fillOvalintintintint", e);
          }
        };
      case "fillPathPath":
        return (r) {
          final op = VGCFillPathPath.fromBinary(r);
          try {
            onFillPathPath(op);
          } catch (e) {
            _failed("fillPathPath", e);
          }
        };
      case "fillPolygonint":
        return (r) {
          final op = VGCFillPolygonint.fromBinary(r);
          try {
            onFillPolygonint(op);
          } catch (e) {
            _failed("fillPolygonint", e);
          }
        };
      case "fillRectangleintintintint":
        return (r) {
          final op = VGCFillRectangleintintintint.fromBinary(r);
          try {
            onFillRectangleintintintint(op);
          } catch (e) {
            _failed("fillRectangleintintintint", e);
          }
        };
      case "fillRoundRectangleintintintintintint":
        return (r) {
          final op = VGCFillRoundRectangleintintintintintint.fromBinary(r);
          try {
            onFillRoundRectangleintintintintintint(op);
          } catch (e) {
            _failed("fillRoundRectangleintintintintintint", e);
          }
        };
    }
    return null;
  }

  /// An op that threw is reported and skipped: the rest of the paint still draws.
  void _failed(String name, Object e) {
    print('[GC DrawerBase] Error in $name: $e');
  }

  void onStateChanged(VGC newState) {}

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
