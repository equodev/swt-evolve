import 'dart:convert';
import 'dart:typed_data';

import '../gen/widgets.dart' show wireMapKeyNames;

/// Maps, lists and scalars in binary (Java's `TaggedValues`): what a payload the wire has no class
/// encoding for travels as. Every value carries a tag; a map's keys go by the number both sides know
/// them by, and only one nothing numbers is spelled out, once per payload. Text is the only string.
///
/// A payload opens with [mark], which UTF-8 text cannot start with. It reads back as maps with string
/// keys, lists, ints, doubles, bools, strings and null, plus [Uint8List] for bytes.
abstract final class TaggedValues {
  static const int mark = 0xF5;

  static const int _null = 0;
  static const int _true = 1;
  static const int _false = 2;
  static const int _int = 3;
  static const int _double = 4;
  static const int _text = 5;
  static const int _bytes = 6;
  static const int _list = 7;
  static const int _map = 8;

  static final Map<String, int> _keys = {
    for (final MapEntry(:key, :value) in wireMapKeyNames.entries) value: key,
  };

  /// Whether [data] holds tagged values rather than text.
  static bool isTagged(Uint8List data) => data.isNotEmpty && data[0] == mark;

  /// The value in [data], which opens with [mark].
  static Object? decode(Uint8List data) => _Reader(data).value();

  /// [value], made of maps, lists and scalars, as tagged values.
  static Uint8List encode(Object? value) {
    final w = _Writer()..u8(mark);
    w.value(value);
    return Uint8List.sublistView(w.bytes, 0, w.length);
  }
}

class _Reader {
  final Uint8List _b;
  final ByteData _data;
  int _pos = 1;
  final List<String> _spelled = [];

  _Reader(this._b) : _data = ByteData.sublistView(_b);

  Object? value() {
    final tag = _b[_pos++];
    switch (tag) {
      case TaggedValues._null:
        return null;
      case TaggedValues._true:
        return true;
      case TaggedValues._false:
        return false;
      case TaggedValues._int:
        final z = _varint();
        return z.isEven ? z ~/ 2 : -(z ~/ 2) - 1;
      case TaggedValues._double:
        final v = _data.getFloat64(_pos, Endian.little);
        _pos += 8;
        return v;
      case TaggedValues._text:
        return _text();
      case TaggedValues._bytes:
        final n = _varint();
        final out = Uint8List.fromList(Uint8List.sublistView(_b, _pos, _pos + n));
        _pos += n;
        return out;
      case TaggedValues._list:
        final n = _varint();
        return [for (var i = 0; i < n; i++) value()];
      case TaggedValues._map:
        final n = _varint();
        final out = <String, dynamic>{};
        for (var i = 0; i < n; i++) {
          final key = _key();
          out[key] = value();
        }
        return out;
      default:
        throw FormatException('not a tagged value: $tag');
    }
  }

  String _key() {
    final ref = _varint();
    if (ref.isOdd) return wireMapKeyNames[ref ~/ 2] ?? '';
    if (ref != 0) return _spelled[ref ~/ 2 - 1];
    final name = _text();
    _spelled.add(name);
    return name;
  }

  String _text() {
    final n = _varint();
    final s = utf8.decoder.convert(_b, _pos, _pos + n);
    _pos += n;
    return s;
  }

  /// An unsigned varint, by arithmetic so a value past 2^32 reads the same on the web.
  int _varint() {
    var v = 0;
    var scale = 1;
    while (true) {
      final x = _b[_pos++];
      v += (x & 0x7F) * scale;
      if (x < 0x80) return v;
      scale *= 128;
    }
  }
}

class _Writer {
  Uint8List bytes = Uint8List(64);
  int length = 0;
  final Map<String, int> _spelled = {};

  void _ensure(int n) {
    if (length + n <= bytes.length) return;
    var size = bytes.length * 2;
    while (size < length + n) {
      size *= 2;
    }
    bytes = Uint8List(size)..setRange(0, length, bytes);
  }

  void u8(int b) {
    _ensure(1);
    bytes[length++] = b;
  }

  void value(Object? v) {
    switch (v) {
      case null:
        u8(TaggedValues._null);
      case bool():
        u8(v ? TaggedValues._true : TaggedValues._false);
      case int():
        u8(TaggedValues._int);
        _varint(v >= 0 ? v * 2 : -v * 2 - 1);
      case double():
        u8(TaggedValues._double);
        _ensure(8);
        ByteData.sublistView(bytes).setFloat64(length, v, Endian.little);
        length += 8;
      case String():
        u8(TaggedValues._text);
        _text(v);
      case Uint8List():
        u8(TaggedValues._bytes);
        _varint(v.length);
        _ensure(v.length);
        bytes.setRange(length, length + v.length, v);
        length += v.length;
      case Map():
        u8(TaggedValues._map);
        _varint(v.length);
        for (final MapEntry(:key, :value) in v.entries) {
          _key('$key');
          this.value(value);
        }
      case Iterable():
        final items = v.toList();
        u8(TaggedValues._list);
        _varint(items.length);
        items.forEach(value);
      default:
        throw ArgumentError.value(v, 'value', 'not a map, list, string, number or boolean');
    }
  }

  void _key(String name) {
    final number = TaggedValues._keys[name];
    if (number != null) {
      _varint(number * 2 + 1);
      return;
    }
    final place = _spelled[name];
    if (place != null) {
      _varint((place + 1) * 2);
      return;
    }
    _spelled[name] = _spelled.length;
    _varint(0);
    _text(name);
  }

  void _text(String s) {
    final b = utf8.encode(s);
    _varint(b.length);
    _ensure(b.length);
    bytes.setRange(length, length + b.length, b);
    length += b.length;
  }

  void _varint(int v) {
    _ensure(10);
    while (v >= 0x80) {
      bytes[length++] = (v % 0x80) + 0x80;
      v = v ~/ 0x80;
    }
    bytes[length++] = v;
  }
}
