import 'dart:typed_data';

import 'wire_names.dart';

/// What a message opens with: its channel, as numbers (Java's `ChannelHeader`). Both sides number
/// channels and widget types alike, so a channel travels as them rather than as its name.
///
/// The first varint says which form follows: odd, a channel addressed to one widget
/// (`Type/id/name`), the name's number shifted right, then the type's number and the id as a zigzag;
/// even, a global channel's number shifted left one. Every channel and type is numbered when the code
/// is generated; names are only for showing a message, in logs and tests.
abstract final class ChannelHeader {
  /// Where the header that starts at [at] in [data] ends, without reading what it names.
  static int skip(Uint8List data, int at) {
    final r = _Reader(data)..pos = at;
    final first = r.varint() ?? 0;
    if (first.isOdd) {
      r.varint();
      r.varint();
    }
    return r.pos;
  }

  /// The channel of the message in [data] by name, and where its payload starts; null when
  /// malformed: for logs and tests, which show a message by name. Messages are dispatched by number.
  static (String, int)? read(Uint8List data) => readAt(data, 0);

  /// [read], for a header that starts at [start] in [data].
  static (String, int)? readAt(Uint8List data, int start) {
    final r = _Reader(data)..pos = start;
    final first = r.varint();
    if (first == null || first == 0) return null;
    final name = WireNames.channels[first ~/ 2];
    if (name == null) return null;
    if (first.isEven) return (name, r.pos);
    final type = r.varint();
    final zigzag = r.varint();
    final typeName = type == null ? null : WireNames.types[type];
    if (typeName == null || zigzag == null) return null;
    final id = zigzag.isEven ? zigzag ~/ 2 : -(zigzag ~/ 2) - 1;
    return (name.isEmpty ? '$typeName/$id' : '$typeName/$id/$name', r.pos);
  }
}

class _Reader {
  final Uint8List _data;
  int pos = 0;

  _Reader(this._data);

  int? varint() {
    var v = 0;
    var scale = 1;
    while (pos < _data.length) {
      final x = _data[pos++];
      v += (x & 0x7F) * scale;
      if (x < 0x80) return v;
      scale *= 128;
    }
    return null;
  }
}
