// Frames as Java sends them, for tests that feed the client directly.

import 'dart:typed_data';

import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';

/// The channel a run of frames sent as one arrives on (Java's `MessageBatch`).
const batchEvent = 'swt.evolve.batch';

/// [payload] on [channel], as tagged values.
void receiveTagged(String channel, Object? payload) => EquoCommService.commForTesting
    .receiveBinary(ChannelHeader.frame(channel, TaggedValues.encode(payload)));

/// A run of frames sent as one, delivered on [batchEvent].
void receiveBatch(List<(String, Object?)> entries) =>
    EquoCommService.commForTesting.receiveBinary(ChannelHeader.frame(batchEvent, batchBody(entries)));

/// A run of frames sent as one whose payloads are already encoded, as a widget wire frame is.
void receiveRawBatch(List<(String, Uint8List)> entries) => EquoCommService.commForTesting
    .receiveBinary(ChannelHeader.frame(batchEvent, _body(entries)));

/// The body of a run of frames sent as one: per entry, its channel's header, its payload's length and
/// its payload as tagged values.
Uint8List batchBody(List<(String, Object?)> entries) =>
    _body([for (final (channel, payload) in entries) (channel, TaggedValues.encode(payload))]);

Uint8List _body(List<(String, Uint8List)> entries) {
  final body = BytesBuilder();
  for (final (channel, value) in entries) {
    body.add(ChannelHeader.frame(channel, Uint8List(0)));
    var n = value.length;
    while (n >= 0x80) {
      body.addByte((n & 0x7F) | 0x80);
      n >>= 7;
    }
    body.addByte(n);
    body.add(value);
  }
  return body.takeBytes();
}
