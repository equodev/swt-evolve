import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import '../comm/channel_header.dart';
import '../comm/comm.dart';
import '../comm/delivery_gate.dart';
import '../comm/gc_wire.dart';
import '../comm/tagged_values.dart';
import '../gen/gc.dart';
import '../gen/widgets.dart';

/// Each op's argument types, by the number a frame defines it with (see GcWireReader).
const Map<int, String> gcOpSignatures = {
  150: 'wii',
  152: 'iiiiiib',
  153: 'iiiiii',
  154: 'iiii',
  155: 'wii',
  156: 'wiiii',
  157: 'wiiiiiiii',
  158: 'iiii',
  159: 'iiii',
  160: 'w',
  161: 'ii',
  162: 'I',
  163: 'I',
  164: 'iiii',
  165: 'iiiiii',
  166: 'siii',
  187: 'iiiiii',
  188: 'iiiib',
  189: 'iiii',
  190: 'w',
  191: 'I',
  192: 'iiii',
  193: 'iiiiii',
};

abstract class GCDrawerBase {
  VGC state;

  GCDrawerBase(this.state) {
    live[state.id] = this;
    EquoCommService.replayWidgetOps(state.id);
  }

  /// Registers what every drawer of the kind numbered [type] answers: its paint frames.
  static void registerOps(int type) {
    EquoCommService.onWidgetOp(type, WireChannel.b, _frameOp);
  }

  /// The drawers by id, where a frame's handler finds the drawer it addresses. A drawer is in it
  /// from its creation to [dispose].
  static final Map<int, GCDrawerBase> live = {};

  static FutureOr<bool> _frameOp(int id, Uint8List frame, int start, int end) {
    final drawer = live[id];
    if (drawer == null) return false;
    // One reader for every paint, unless a paint waiting on an escape still holds it.
    if (_readerBusy) {
      final applied = drawer._applyFrame(GcWireReader(frame, start, end));
      return applied is Future ? applied.then(_handled) : true;
    }
    _readerBusy = true;
    FutureOr<void> applied;
    try {
      applied = drawer._applyFrame(_reader..reset(frame, start, end));
    } catch (_) {
      _readerBusy = false;
      rethrow;
    }
    if (applied is Future) {
      return applied.whenComplete(_freeReader).then(_handled);
    }
    _readerBusy = false;
    return true;
  }

  static final GcWireReader _reader = GcWireReader(Uint8List(0));
  static bool _readerBusy = false;
  static void _freeReader() => _readerBusy = false;
  static bool _handled(void _) => true;

  /// A paint as one binary frame: ops, the state they draw with, and entries that travel on channels
  /// of their own, in the order they were drawn.
  FutureOr<void> _applyFrame(GcWireReader r) {
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
        r.readEscape();
        final delivered = EquoCommService.deliverEscape(
          r.bytes,
          r.escapeHeaderAt,
          r.escapePayloadStart,
          r.escapePayloadEnd,
        );
        // Anything after it waits, as the ops behind a frame that awaits do.
        if (delivered is Future) return _resume(delivered, r);
      } else {
        final number = r.opNumber(tag);
        final apply = number >= 0 && number < _binaryOps.length
            ? _binaryOps[number]
            : null;
        if (apply == null) {
          r.skip(r.signatureOf(tag));
        } else {
          apply(this, r);
        }
      }
    }
    return null;
  }

  /// The rest of a paint, once the escape it waits on is delivered. A method of its own: a closure
  /// in [_applyFrame] would make every paint allocate the context it captures.
  Future<void> _resume(Future<void> delivered, GcWireReader r) async {
    await delivered;
    await _applyFrame(r);
  }

  /// How a drawer applies each op, by the op's number: made once, for every drawer of the kind.
  static final List<void Function(GCDrawerBase, GcWireReader)?> _binaryOps =
      () {
        final ops = List<void Function(GCDrawerBase, GcWireReader)?>.filled(
          194,
          null,
        );
        ops[150] = _opCopyAreaImageintint;
        ops[152] = _opCopyAreaintintintintintintboolean;
        ops[153] = _opDrawArcintintintintintint;
        ops[154] = _opDrawFocusintintintint;
        ops[155] = _opDrawImageImageintint;
        ops[156] = _opDrawImageImageintintintint;
        ops[157] = _opDrawImageImageintintintintintintintint;
        ops[158] = _opDrawLineintintintint;
        ops[159] = _opDrawOvalintintintint;
        ops[160] = _opDrawPathPath;
        ops[161] = _opDrawPointintint;
        ops[162] = _opDrawPolygonint;
        ops[163] = _opDrawPolylineint;
        ops[164] = _opDrawRectangleintintintint;
        ops[165] = _opDrawRoundRectangleintintintintintint;
        ops[166] = _opDrawTextStringintintint;
        ops[187] = _opFillArcintintintintintint;
        ops[188] = _opFillGradientRectangleintintintintboolean;
        ops[189] = _opFillOvalintintintint;
        ops[190] = _opFillPathPath;
        ops[191] = _opFillPolygonint;
        ops[192] = _opFillRectangleintintintint;
        ops[193] = _opFillRoundRectangleintintintintintint;
        return ops;
      }();

  static final VGCCopyAreaImageintint _keptCopyAreaImageintint =
      VGCCopyAreaImageintint();

  static void _opCopyAreaImageintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptCopyAreaImageintint..readBinary(r);
    try {
      d.onCopyAreaImageintint(op);
    } catch (e) {
      d._failed("copyAreaImageintint", e);
    }
  }

  static final VGCCopyAreaintintintintintintboolean
  _keptCopyAreaintintintintintintboolean =
      VGCCopyAreaintintintintintintboolean();

  static void _opCopyAreaintintintintintintboolean(
    GCDrawerBase d,
    GcWireReader r,
  ) {
    final op = _keptCopyAreaintintintintintintboolean..readBinary(r);
    try {
      d.onCopyAreaintintintintintintboolean(op);
    } catch (e) {
      d._failed("copyAreaintintintintintintboolean", e);
    }
  }

  static final VGCDrawArcintintintintintint _keptDrawArcintintintintintint =
      VGCDrawArcintintintintintint();

  static void _opDrawArcintintintintintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawArcintintintintintint..readBinary(r);
    try {
      d.onDrawArcintintintintintint(op);
    } catch (e) {
      d._failed("drawArcintintintintintint", e);
    }
  }

  static final VGCDrawFocusintintintint _keptDrawFocusintintintint =
      VGCDrawFocusintintintint();

  static void _opDrawFocusintintintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawFocusintintintint..readBinary(r);
    try {
      d.onDrawFocusintintintint(op);
    } catch (e) {
      d._failed("drawFocusintintintint", e);
    }
  }

  static final VGCDrawImageImageintint _keptDrawImageImageintint =
      VGCDrawImageImageintint();

  static void _opDrawImageImageintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawImageImageintint..readBinary(r);
    try {
      d.onDrawImageImageintint(op);
    } catch (e) {
      d._failed("drawImageImageintint", e);
    }
  }

  static final VGCDrawImageImageintintintint _keptDrawImageImageintintintint =
      VGCDrawImageImageintintintint();

  static void _opDrawImageImageintintintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawImageImageintintintint..readBinary(r);
    try {
      d.onDrawImageImageintintintint(op);
    } catch (e) {
      d._failed("drawImageImageintintintint", e);
    }
  }

  static final VGCDrawImageImageintintintintintintintint
  _keptDrawImageImageintintintintintintintint =
      VGCDrawImageImageintintintintintintintint();

  static void _opDrawImageImageintintintintintintintint(
    GCDrawerBase d,
    GcWireReader r,
  ) {
    final op = _keptDrawImageImageintintintintintintintint..readBinary(r);
    try {
      d.onDrawImageImageintintintintintintintint(op);
    } catch (e) {
      d._failed("drawImageImageintintintintintintintint", e);
    }
  }

  static final VGCDrawLineintintintint _keptDrawLineintintintint =
      VGCDrawLineintintintint();

  static void _opDrawLineintintintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawLineintintintint..readBinary(r);
    try {
      d.onDrawLineintintintint(op);
    } catch (e) {
      d._failed("drawLineintintintint", e);
    }
  }

  static final VGCDrawOvalintintintint _keptDrawOvalintintintint =
      VGCDrawOvalintintintint();

  static void _opDrawOvalintintintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawOvalintintintint..readBinary(r);
    try {
      d.onDrawOvalintintintint(op);
    } catch (e) {
      d._failed("drawOvalintintintint", e);
    }
  }

  static final VGCDrawPathPath _keptDrawPathPath = VGCDrawPathPath();

  static void _opDrawPathPath(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawPathPath..readBinary(r);
    try {
      d.onDrawPathPath(op);
    } catch (e) {
      d._failed("drawPathPath", e);
    }
  }

  static final VGCDrawPointintint _keptDrawPointintint = VGCDrawPointintint();

  static void _opDrawPointintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawPointintint..readBinary(r);
    try {
      d.onDrawPointintint(op);
    } catch (e) {
      d._failed("drawPointintint", e);
    }
  }

  static final VGCDrawPolygonint _keptDrawPolygonint = VGCDrawPolygonint();

  static void _opDrawPolygonint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawPolygonint..readBinary(r);
    try {
      d.onDrawPolygonint(op);
    } catch (e) {
      d._failed("drawPolygonint", e);
    }
  }

  static final VGCDrawPolylineint _keptDrawPolylineint = VGCDrawPolylineint();

  static void _opDrawPolylineint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawPolylineint..readBinary(r);
    try {
      d.onDrawPolylineint(op);
    } catch (e) {
      d._failed("drawPolylineint", e);
    }
  }

  static final VGCDrawRectangleintintintint _keptDrawRectangleintintintint =
      VGCDrawRectangleintintintint();

  static void _opDrawRectangleintintintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawRectangleintintintint..readBinary(r);
    try {
      d.onDrawRectangleintintintint(op);
    } catch (e) {
      d._failed("drawRectangleintintintint", e);
    }
  }

  static final VGCDrawRoundRectangleintintintintintint
  _keptDrawRoundRectangleintintintintintint =
      VGCDrawRoundRectangleintintintintintint();

  static void _opDrawRoundRectangleintintintintintint(
    GCDrawerBase d,
    GcWireReader r,
  ) {
    final op = _keptDrawRoundRectangleintintintintintint..readBinary(r);
    try {
      d.onDrawRoundRectangleintintintintintint(op);
    } catch (e) {
      d._failed("drawRoundRectangleintintintintintint", e);
    }
  }

  static final VGCDrawTextStringintintint _keptDrawTextStringintintint =
      VGCDrawTextStringintintint();

  static void _opDrawTextStringintintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptDrawTextStringintintint..readBinary(r);
    try {
      d.onDrawTextStringintintint(op);
    } catch (e) {
      d._failed("drawTextStringintintint", e);
    }
  }

  static final VGCFillArcintintintintintint _keptFillArcintintintintintint =
      VGCFillArcintintintintintint();

  static void _opFillArcintintintintintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptFillArcintintintintintint..readBinary(r);
    try {
      d.onFillArcintintintintintint(op);
    } catch (e) {
      d._failed("fillArcintintintintintint", e);
    }
  }

  static final VGCFillGradientRectangleintintintintboolean
  _keptFillGradientRectangleintintintintboolean =
      VGCFillGradientRectangleintintintintboolean();

  static void _opFillGradientRectangleintintintintboolean(
    GCDrawerBase d,
    GcWireReader r,
  ) {
    final op = _keptFillGradientRectangleintintintintboolean..readBinary(r);
    try {
      d.onFillGradientRectangleintintintintboolean(op);
    } catch (e) {
      d._failed("fillGradientRectangleintintintintboolean", e);
    }
  }

  static final VGCFillOvalintintintint _keptFillOvalintintintint =
      VGCFillOvalintintintint();

  static void _opFillOvalintintintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptFillOvalintintintint..readBinary(r);
    try {
      d.onFillOvalintintintint(op);
    } catch (e) {
      d._failed("fillOvalintintintint", e);
    }
  }

  static final VGCFillPathPath _keptFillPathPath = VGCFillPathPath();

  static void _opFillPathPath(GCDrawerBase d, GcWireReader r) {
    final op = _keptFillPathPath..readBinary(r);
    try {
      d.onFillPathPath(op);
    } catch (e) {
      d._failed("fillPathPath", e);
    }
  }

  static final VGCFillPolygonint _keptFillPolygonint = VGCFillPolygonint();

  static void _opFillPolygonint(GCDrawerBase d, GcWireReader r) {
    final op = _keptFillPolygonint..readBinary(r);
    try {
      d.onFillPolygonint(op);
    } catch (e) {
      d._failed("fillPolygonint", e);
    }
  }

  static final VGCFillRectangleintintintint _keptFillRectangleintintintint =
      VGCFillRectangleintintintint();

  static void _opFillRectangleintintintint(GCDrawerBase d, GcWireReader r) {
    final op = _keptFillRectangleintintintint..readBinary(r);
    try {
      d.onFillRectangleintintintint(op);
    } catch (e) {
      d._failed("fillRectangleintintintint", e);
    }
  }

  static final VGCFillRoundRectangleintintintintintint
  _keptFillRoundRectangleintintintintintint =
      VGCFillRoundRectangleintintintintintint();

  static void _opFillRoundRectangleintintintintintint(
    GCDrawerBase d,
    GcWireReader r,
  ) {
    final op = _keptFillRoundRectangleintintintintintint..readBinary(r);
    try {
      d.onFillRoundRectangleintintintintintint(op);
    } catch (e) {
      d._failed("fillRoundRectangleintintintintintint", e);
    }
  }

  /// An op that threw is reported and skipped: the rest of the paint still draws.
  void _failed(String name, Object e) {
    print('[GC DrawerBase] Error in $name: $e');
  }

  void onStateChanged(VGC newState) {}

  void dispose() {
    if (identical(live[state.id], this)) live.remove(state.id);
  }

  // Each op's object is refilled for the next op of its kind: a handler reads what it needs before
  // it returns or awaits, and copies the op to keep it.
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
