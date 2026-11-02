// Channels by name, for tests: the client sends and listens by number, and a test reads better
// naming the channel it means.

import 'dart:async';
import 'dart:typed_data';

import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames, wireTypeNames;

final Map<String, int> _channels = {for (final e in wireChannelNames.entries) e.value: e.key};
final Map<String, int> _types = {for (final e in wireTypeNames.entries) e.value: e.key};

/// The numbers [name] stands for: `Type/id/channel` (or `Type/id`, the channel named ''), else a
/// global channel, whose type is 0.
(int type, int id, int channel) address(String name) {
  final parts = name.split('/');
  if (parts.length >= 2) {
    final id = int.tryParse(parts[1]);
    final type = _types[parts[0]];
    if (id != null && type != null) {
      final channel = parts.length == 2 ? '' : parts.sublist(2).join('/');
      return (type, id, _channels[channel] ?? (throw ArgumentError('no channel "$channel"')));
    }
  }
  return (0, 0, _channels[name] ?? (throw ArgumentError('no channel "$name"')));
}

/// A frame on the channel [name] carrying [body].
Uint8List frame(String name, Uint8List body) {
  final (type, id, channel) = address(name);
  final header = <int>[];
  void varint(int v) {
    while (v >= 0x80) {
      header.add((v % 0x80) + 0x80);
      v = v ~/ 0x80;
    }
    header.add(v);
  }

  if (type == 0) {
    varint(channel * 2);
  } else {
    varint(channel * 2 + 1);
    varint(type);
    varint(id >= 0 ? id * 2 : -id * 2 - 1);
  }
  return Uint8List(header.length + body.length)
    ..setRange(0, header.length, header)
    ..setRange(header.length, header.length + body.length, body);
}

/// The comm's listeners, by a channel's name.
extension NamedChannels on EquoCommBase {
  Object onNamed(String name, OnSuccessCallback<dynamic> onSuccess) {
    final (type, id, channel) = address(name);
    return on(type, id, channel, onSuccess);
  }

  void onBytesNamed(String name, FutureOr<void> Function(Uint8List) callback) {
    final (type, id, channel) = address(name);
    onBytes(type, id, channel, callback);
  }

  void removeNamed(String name, [Object? token]) {
    final (type, id, channel) = address(name);
    remove(type, id, channel, token);
  }

  /// Sends [payload] on the channel [name].
  void sendNamed(String name, [Object? payload]) {
    final (type, id, channel) = address(name);
    type == 0 ? sendOn(channel, payload) : sendTo(type, id, channel, payload);
  }
}
