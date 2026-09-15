// Driving a widget's state the way Java does, for tests that need a value to change rather than a
// tree to be built.
//
// Mounting a widget with a different value no longer changes anything: a parent's copy of a child
// carries identity, and what the child renders comes from the registry. So a test that used to
// express "this widget changed" by pumping a new value has to say it on the wire instead, which is
// also the only path production has.

import 'dart:convert';
import 'dart:typed_data';

import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/widget.dart';

/// Sends [value] to whoever is holding it, whole - the frame Java writes for a widget it has not
/// described before, or has changed too much to describe by parts.
///
/// Awaited: the transport hands a frame to its handlers in a microtask, so a caller that asserted
/// straight after this would be asserting before the frame was handled. A microtask rather than a
/// zero delay, because a widget test runs on fake time and would sit on the timer forever.
Future<void> deliverWhole(VWidget value) async {
  await deliverFrame(VRegistry.channelOf(value), _stamped(value));
}

/// [value] as JSON, with every write stamp in the subtree put back.
///
/// `toJson()` leaves `seq` out - it is not state, and including it would make two equal values
/// compare unequal - but the wire carries it, written straight into the bytes by the Java
/// serializer. A test frame without stamps would say every widget in it was written at time zero,
/// which is the one thing that makes a value never worth taking.
Map<String, dynamic> _stamped(VWidget value) {
  final stamps = <String, int>{};
  void collect(VWidget node) {
    stamps[VRegistry.channelOf(node)] = node.seq;
    node.adoptChildren((child) {
      collect(child);
      return child;
    });
  }

  collect(value);

  Object? restamp(Object? node) {
    if (node is List) return node.map(restamp).toList();
    if (node is! Map) return node;
    final map = Map<String, dynamic>.from(node);
    for (final entry in map.entries.toList()) {
      map[entry.key] = restamp(entry.value);
    }
    final swt = map['swt'];
    final id = map['id'];
    if (swt is String && id is int) {
      final seq = stamps['$swt/$id'];
      if (seq != null) map[kSeq] = seq;
    }
    return map;
  }

  // Encoded and decoded first: `toJson()` leaves nested values as objects, and only the encoder
  // turns them into maps this can reach into.
  final encoded = json.decode(json.encode(value.toJson()));
  return restamp(encoded)! as Map<String, dynamic>;
}

/// Sends a partial frame naming [changed], as an update relative to [base] would arrive.
Future<void> deliverChange(
  VWidget value, {
  required List<String> changed,
  required int base,
}) async {
  final json = _stamped(value);
  final frame = <String, dynamic>{
    'swt': json['swt'],
    'id': json['id'],
    kSeq: json[kSeq],
    kBase: base,
    kChangedKeys: changed,
    for (final key in changed) key: json[key],
  };
  await deliverFrame(VRegistry.channelOf(value), frame);
}

/// The raw path: a JSON body arriving on [channel], framed the way the transport frames it.
Future<void> deliverFrame(String channel, Map<String, dynamic> body) async {
  final action = utf8.encode(channel);
  final payload = utf8.encode(json.encode(body));
  final frame = Uint8List(2 + action.length + payload.length);
  frame[0] = (action.length >> 8) & 0xFF;
  frame[1] = action.length & 0xFF;
  frame.setRange(2, 2 + action.length, action);
  frame.setRange(2 + action.length, frame.length, payload);
  EquoCommService.commForTesting.receiveBinary(frame);
  await Future<void>.microtask(() {});
}

/// Everything this client knew, forgotten. What a reconnect is, and what a test needs between two
/// scenarios that both mount a widget under the same id.
void freshClient() {
  VRegistry.instance.clear();
  deliveryGate.reset();
}
