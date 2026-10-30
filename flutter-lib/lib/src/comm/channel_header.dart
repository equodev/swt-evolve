import 'dart:convert';
import 'dart:typed_data';

import '../gen/widgets.dart' show wireChannelNames, wireTypeNames;

/// What a message opens with: its channel, as numbers (Java's `ChannelHeader`). Both sides number
/// channels and widget types alike, so a channel travels as them rather than as its name.
///
/// The first varint says which form follows: odd, a channel addressed to one widget
/// (`Type/id/name`), the name's number shifted right, then the type's number and the id as a zigzag;
/// even and not 0, a global channel's number shifted left one; 0, a name nothing numbers, spelled out
/// as its length and its UTF-8 bytes.
abstract final class ChannelHeader {
  static final Map<String, int> _channels = {
    for (final MapEntry(:key, :value) in wireChannelNames.entries) value: key,
  };
  static final Map<String, int> _types = {
    for (final MapEntry(:key, :value) in wireTypeNames.entries) value: key,
  };

  /// A frame on [channel] carrying [body].
  static Uint8List frame(String channel, Uint8List body) {
    final header = _Bytes();
    _write(header, channel);
    final out = Uint8List(header.length + body.length);
    out.setRange(0, header.length, header.bytes);
    if (body.isNotEmpty) out.setRange(header.length, out.length, body);
    return out;
  }

  static void _write(_Bytes out, String channel) {
    final slash = channel.indexOf('/');
    if (slash > 0) {
      final second = channel.indexOf('/', slash + 1);
      final id = _id(channel, slash + 1, second < 0 ? channel.length : second);
      if (id != null) {
        final type = _types[channel.substring(0, slash)];
        final name = _channels[second < 0 ? '' : channel.substring(second + 1)];
        if (type != null && name != null) {
          out.varint(name * 2 + 1);
          out.varint(type);
          out.varint(id >= 0 ? id * 2 : -id * 2 - 1);
          return;
        }
      }
    }
    final global = _channels[channel];
    if (global != null && global != 0) {
      out.varint(global * 2);
      return;
    }
    final name = utf8.encode(channel);
    out.varint(0);
    out.varint(name.length);
    out.addAll(name);
  }

  /// The decimal id in `s[from, to)`, or null when it is not one.
  static int? _id(String s, int from, int to) {
    if (from >= to || to - from > 20) return null;
    final negative = s.codeUnitAt(from) == 0x2D;
    var i = negative ? from + 1 : from;
    if (i >= to) return null;
    var v = 0;
    for (; i < to; i++) {
      final c = s.codeUnitAt(i) - 0x30;
      if (c < 0 || c > 9) return null;
      v = v * 10 + c;
    }
    return negative ? -v : v;
  }

  /// The channel of the message in [data], and where its payload starts; null when malformed.
  static (String, int)? read(Uint8List data) => readAt(data, 0);

  /// [read], for a header that starts at [start] in [data].
  static (String, int)? readAt(Uint8List data, int start) {
    final r = _Reader(data)..pos = start;
    final first = r.varint();
    if (first == null) return null;
    if (first == 0) {
      final length = r.varint();
      if (length == null || r.pos + length > data.length) return null;
      final name = utf8.decoder.convert(data, r.pos, r.pos + length);
      return (name, r.pos + length);
    }
    final name = wireChannelNames[first ~/ 2];
    if (name == null) return null;
    if (first.isEven) return (name, r.pos);
    final type = r.varint();
    final zigzag = r.varint();
    final typeName = type == null ? null : wireTypeNames[type];
    if (typeName == null || zigzag == null) return null;
    final id = zigzag.isEven ? zigzag ~/ 2 : -(zigzag ~/ 2) - 1;
    return (name.isEmpty ? '$typeName/$id' : '$typeName/$id/$name', r.pos);
  }
}

/// Growable bytes, written with varints by arithmetic so a value past 2^32 writes the same on the web.
class _Bytes {
  Uint8List bytes = Uint8List(32);
  int length = 0;

  void _ensure(int n) {
    if (length + n <= bytes.length) return;
    var size = bytes.length * 2;
    while (size < length + n) {
      size *= 2;
    }
    bytes = Uint8List(size)..setRange(0, length, bytes);
  }

  void varint(int v) {
    _ensure(10);
    while (v >= 0x80) {
      bytes[length++] = (v % 0x80) + 0x80;
      v = v ~/ 0x80;
    }
    bytes[length++] = v;
  }

  void addAll(List<int> values) {
    _ensure(values.length);
    bytes.setRange(length, length + values.length, values);
    length += values.length;
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
