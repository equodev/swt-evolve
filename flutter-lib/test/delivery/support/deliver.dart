// Driving a widget's state the way Java does, for tests that need a value to change rather than a
// tree to be built.
//
// Mounting a widget with a different value no longer changes anything: a parent's copy of a child
// carries identity, and what the child renders comes from the registry. So a test that used to
// express "this widget changed" by pumping a new value has to say it on the wire instead, which is
// also the only path production has: a frame of widget records, as Java's WidgetWire writes one.

import 'dart:typed_data';
import 'dart:ui' show Offset;

import 'package:flutter/painting.dart' show BoxDecoration;
import 'package:flutter/rendering.dart' show RenderBox;
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireTypeNames;
import 'package:swtflutter/src/impl/utils/dnd_utils.dart';
import '../../support/fields_only.dart';
import '../../support/channels.dart' as channels;

/// [applyWhole] into the client's registry, awaited as a delivery through the transport is.
Future<void> deliverWhole(VWidget value) async {
  applyWhole(value);
  await Future<void>.microtask(() {});
}

/// [applyChange] into the client's registry, awaited as a delivery through the transport is.
Future<void> deliverChange(VWidget value, {required List<int> keys, required int base}) async {
  applyChange(value, keys: keys, base: base);
  await Future<void>.microtask(() {});
}

/// Applies [value] whole to [registry] (the client's by default) - the record Java writes for a
/// widget it has not described before, or has changed too much to describe by parts - with a record
/// for every widget beneath it the client does not hold yet or holds an older state of, and for
/// every image they show.
///
/// A value stamped 0 is given the stamp after the one held, so it is taken, as an unstamped
/// description always was. A widget whose id is in [named] is only named, as Java names one it
/// counts delivered, whether or not the client still holds it.
///
/// With [describeImages] false the frame names images without describing them, as Java does for one
/// it counts as delivered.
void applyWhole(VWidget value, {VRegistry? registry, Set<int> named = const {}, bool describeImages = true}) {
  final records = <_Record>[];
  final seen = <VWidget>{};
  void describe(VWidget v, {bool always = false}) {
    if (!seen.add(v)) return;
    if (!always && named.contains(v.id)) return;
    final channel = VRegistry.channelOf(v);
    final held = deliveryGate.heldSeq(channel);
    final stamp = _stampOf(v);
    if (always || held == null || stamp > held) {
      records.add(_Record(wireWhole, v, stamp));
    }
    v.adoptChildren((child) {
      describe(child);
      return child;
    });
  }

  describe(value, always: true);
  (registry ?? VRegistry.instance).applyWire(_frameBody(records, describeImages: describeImages));
}

/// Applies a change to [value] naming the properties [keys] (their [WireKey] numbers), computed
/// from the state stamped [base], to [registry] (the client's by default): what Java writes for a
/// widget the client holds. A widget the change names that the client lacks gets a record of its
/// own, as Java gives it one.
void applyChange(VWidget value, {required List<int> keys, required int base, VRegistry? registry}) {
  final into = registry ?? VRegistry.instance;
  final records = [_Record(wireChange, value, _stampOf(value), base: base, keys: keys.toSet())];
  value.adoptChildren((child) {
    if (into.valueOn(VRegistry.channelOf(child)) == null) {
      records.add(_Record(wireWhole, child, _stampOf(child)));
    }
    return child;
  });
  _apply(records, into);
}

/// One record of a frame of widget records, for a test that writes the frame itself.
class WireRecord {
  /// [value] described whole.
  WireRecord.whole(VWidget value) : _record = _Record(wireWhole, value, _stampOf(value));

  /// A change to [value] naming the properties [keys], computed from the state stamped [base].
  WireRecord.change(VWidget value, {required List<int> keys, required int base})
      : _record = _Record(wireChange, value, _stampOf(value), base: base, keys: keys.toSet());

  final _Record _record;
}

/// [records] as the body of one frame on the widget wire's channel, as Java writes one.
Uint8List wireFrameBody(List<WireRecord> records) => _frameBody([for (final r in records) r._record]);

/// Applies [records] as one frame to [registry] (the client's by default).
void applyRecords(List<WireRecord> records, {VRegistry? registry}) =>
    (registry ?? VRegistry.instance).applyWire(wireFrameBody(records));

/// Delivers [body] on [channel] as tagged values, framed the way the transport frames it: a
/// message with no class of its own, such as an answer Java builds as a map.
Future<void> deliverFrame(String channel, Map<String, dynamic> body) async {
  EquoCommService.commForTesting.receiveBinary(channels.frame(channel, TaggedValues.encode(body)));
  await Future<void>.microtask(() {});
}

/// [event]'s fields arriving on [channel], as Java sends an answer that is an event.
Future<void> deliverEvent(String channel, VEvent event) async {
  EquoCommService.commForTesting.receiveBinary(channels.frame(channel, WidgetWireWriter.encode(event)));
  await Future<void>.microtask(() {});
}

/// The drop feedback drawn over [at], wherever a drop target draws it, or null.
BoxDecoration? dropFeedbackAt(WidgetTester tester, Offset at) {
  for (final e in find.byType(DropRowLayer).evaluate()) {
    final layer = e.widget as DropRowLayer;
    final row = layer.rows.row;
    if (row == null) continue;
    final box = e.renderObject! as RenderBox;
    final local = layer.rowRect(row, box.size.width);
    final drawn = box.localToGlobal(local.topLeft) & local.size;
    if (drawn.contains(at)) return layer.decorationOver(row);
  }
  return null;
}

/// Everything this client knew, forgotten. What a reconnect is, and what a test needs between two
/// scenarios that both mount a widget under the same id.
void freshClient() {
  VRegistry.instance.clear();
  deliveryGate.reset();
  DndDropAnswer.reset();
}

int _stampOf(VWidget v) =>
    v.seq != 0 ? v.seq : (deliveryGate.heldSeq(VRegistry.channelOf(v)) ?? 0) + 1;

class _Record {
  _Record(this.kind, this.value, this.stamp, {this.base = 0, this.keys});

  final int kind;
  final VWidget value;
  final int stamp;
  final int base;
  final Set<int>? keys;
}

final Map<String, int> _typeNumbers = {
  for (final MapEntry(:key, :value) in wireTypeNames.entries) value: key,
};

/// Image ids for test images that have none: Java names every image it sends by one.
int _nextImageId = 1 << 30;

/// Applies [records] as one frame, in stamp order: a frame's stamps only ever grow.
void _apply(List<_Record> records, VRegistry registry) => registry.applyWire(_frameBody(records));

Uint8List _frameBody(List<_Record> records, {bool describeImages = true}) {
  records.sort((a, b) => a.stamp.compareTo(b.stamp));
  final images = <VImage>{};
  if (describeImages) {
    for (final r in records) {
      _imagesIn(r.value, images);
    }
  }
  final out = BytesBuilder(copy: false);
  final count = ByteData(4)..setUint32(0, images.length + records.length, Endian.little);
  out.add(count.buffer.asUint8List());
  for (final image in images) {
    final id = image.id ??= _nextImageId++;
    out.add(_record(wireImage, (w) {
      w.int64(id);
      image.writeWire(w);
    }));
  }
  var last = 0;
  for (final r in records) {
    final delta = r.stamp - last;
    last = r.stamp;
    out.add(_record(r.kind, (w) {
      final number = _typeNumbers[r.value.swt];
      if (number != null) {
        w.varint(number * 2 + 1);
      } else {
        w
          ..varint(0)
          ..string(r.value.swt);
      }
      w
        ..int64(r.value.id)
        ..varint(delta);
      if (r.kind == wireChange) w.varint(r.stamp - r.base);
    }, fields: fieldsOnly(r.value, r.keys)));
  }
  return out.toBytes();
}

/// A record: its kind, its length, then what [write] writes and [fields].
Uint8List _record(int kind, void Function(WidgetWireWriter w) write, {Uint8List? fields}) {
  final rest = Uint8List.fromList([...WidgetWireWriter.encodeWith(write), ...?fields]);
  final head = Uint8List.fromList(WidgetWireWriter.encodeWith((w) => w
    ..u8(kind)
    ..varint(rest.length)));
  return Uint8List.fromList([...head, ...rest]);
}

/// The images [value] shows that carry something to draw, into [images].
void _imagesIn(WireVisitable value, Set<VImage> images) {
  void visit(Object? v) {
    if (v is VImage) {
      if (!v.isReference) images.add(v);
    } else if (v is List) {
      v.forEach(visit);
    } else if (v is WireVisitable && v is! VWidget) {
      v.visitWire((_, e) => visit(e));
    }
  }

  value.visitWire((_, v) => visit(v));
}
