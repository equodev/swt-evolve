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
  static Object? decode(Uint8List data) => _Reader.shared.read(data, 1);

  /// The value in [data] from [start], or null when nothing is there or it is not tagged values.
  static Object? decodeAt(Uint8List data, int start) =>
      start < data.length && data[start] == mark ? _Reader.shared.read(data, start + 1) : null;

  /// The number a map key travels as, for [boolField] and [intField]. Only a numbered key can be
  /// read in place: one nothing numbers is spelled out, and would never match.
  static int keyNumber(String name) {
    final number = _keys[name];
    assert(number != null, 'map key "$name" has no number; add it to the generator\'s map keys');
    return number ?? -1;
  }

  /// The bool under map key number [key] of the map at [start] in [data] (a payload opening with
  /// [mark]), or null when the map has no such key or it holds something else. Read in place.
  static bool? boolField(Uint8List data, int start, int key) {
    final at = _field(data, start, key);
    if (at < 0) return null;
    final tag = data[at];
    return tag == _true ? true : (tag == _false ? false : null);
  }

  /// The int under map key number [key] of the map at [start] in [data], or null. Read in place.
  static int? intField(Uint8List data, int start, int key) {
    final at = _field(data, start, key);
    if (at < 0 || data[at] != _int) return null;
    _at = at + 1;
    final z = _varintAt(data);
    return z.isEven ? z ~/ 2 : -(z ~/ 2) - 1;
  }

  static int _at = 0;

  /// Where the value of map key number [key] starts in the map at [start], or -1.
  static int _field(Uint8List data, int start, int key) {
    if (key < 0 || start + 1 >= data.length || data[start] != mark || data[start + 1] != _map) return -1;
    _at = start + 2;
    final n = _varintAt(data);
    for (var i = 0; i < n; i++) {
      final ref = _varintAt(data);
      // A key spelled out is the name itself; its number is never what was asked for.
      if (ref == 0) _at += _varintAt(data);
      if (ref.isOdd && ref ~/ 2 == key) return _at;
      _skip(data);
    }
    return -1;
  }

  static void _skip(Uint8List data) {
    final tag = data[_at++];
    switch (tag) {
      case _int:
        _varintAt(data);
      case _double:
        _at += 8;
      case _text || _bytes:
        _at += _varintAt(data);
      case _list:
        for (var n = _varintAt(data); n > 0; n--) {
          _skip(data);
        }
      case _map:
        for (var n = _varintAt(data); n > 0; n--) {
          if (_varintAt(data) == 0) _at += _varintAt(data);
          _skip(data);
        }
    }
  }

  static int _varintAt(Uint8List data) {
    var v = 0;
    var scale = 1;
    while (true) {
      final x = data[_at++];
      v += (x & 0x7F) * scale;
      if (x < 0x80) return v;
      scale *= 128;
    }
  }

  /// [value], made of maps, lists and scalars, as tagged values, in a buffer of its own.
  static Uint8List encode(Object? value) {
    final w = _Writer.shared.._encode(value);
    return Uint8List(w.length)..setRange(0, w.length, w.bytes);
  }

  /// [encode] into the start of [sharedBytes], a buffer every call reuses, returning how many bytes
  /// it wrote there: valid until the next call, for a caller that copies them at once.
  static int encodeShared(Object? value) => (_Writer.shared.._encode(value)).length;

  /// The buffer [encodeShared] writes into.
  static Uint8List get sharedBytes => _Writer.shared.bytes;
}

class _Reader {
  /// One reader for every decode: a decode runs to its end before the next starts.
  static final _Reader shared = _Reader();

  static final Uint8List _empty = Uint8List(0);

  Uint8List _b = _empty;
  ByteData? _data;
  int _pos = 0;

  /// The keys this payload spells out, in order: the first [_spelledCount].
  List<String> _spelled = List.filled(8, '');
  int _spelledCount = 0;

  Object? read(Uint8List data, int start) {
    _b = data;
    _data = null;
    _pos = start;
    _spelledCount = 0;
    try {
      return value();
    } finally {
      _b = _empty;
      _data = null;
    }
  }

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
        final v = (_data ??= ByteData.sublistView(_b)).getFloat64(_pos, Endian.little);
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
    if (_spelledCount == _spelled.length) {
      _spelled = List.filled(_spelledCount * 2, '')..setRange(0, _spelledCount, _spelled);
    }
    _spelled[_spelledCount++] = name;
    return name;
  }

  /// Text as the widget wire writes a string: Latin-1 bytes or UTF-16 code units, copied, not decoded.
  String _text() {
    final header = _varint();
    final n = (header - 1) >> 1;
    if ((header - 1).isEven) {
      final end = _pos + n;
      final s = String.fromCharCodes(_b, _pos, end);
      _pos = end;
      return s;
    }
    final units = Uint16List(n);
    for (var i = 0; i < n; i++) {
      units[i] = _b[_pos] | (_b[_pos + 1] << 8);
      _pos += 2;
    }
    return String.fromCharCodes(units);
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
  /// One writer for every encode, its buffer kept: an encode runs to its end before the next starts.
  static final _Writer shared = _Writer();

  Uint8List bytes = Uint8List(256);
  int length = 0;

  /// [bytes] as ByteData, for doubles; remade only when [bytes] is.
  ByteData? _data;

  /// The keys spelled out so far in this payload, in order: the first [_spelledCount]. A payload
  /// spells few, so they are looked up in order.
  List<String> _spelled = List.filled(8, '');
  int _spelledCount = 0;

  /// [_writeEntry], torn off once: walking a map with it allocates no iterator or closure.
  late final void Function(Object?, Object?) _entry = _writeEntry;

  void _writeEntry(Object? key, Object? v) {
    _key(key is String ? key : '$key');
    value(v);
  }

  void _encode(Object? v) {
    length = 0;
    _spelledCount = 0;
    u8(TaggedValues.mark);
    value(v);
  }

  void _ensure(int n) {
    if (length + n <= bytes.length) return;
    var size = bytes.length * 2;
    while (size < length + n) {
      size *= 2;
    }
    bytes = Uint8List(size)..setRange(0, length, bytes);
    _data = null;
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
        (_data ??= ByteData.sublistView(bytes)).setFloat64(length, v, Endian.little);
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
        v.forEach(_entry);
      case List():
        u8(TaggedValues._list);
        _varint(v.length);
        for (var i = 0; i < v.length; i++) {
          value(v[i]);
        }
      case Iterable():
        value(v.toList());
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
    for (var place = 0; place < _spelledCount; place++) {
      if (_spelled[place] == name) {
        _varint((place + 1) * 2);
        return;
      }
    }
    if (_spelledCount == _spelled.length) {
      _spelled = List.filled(_spelledCount * 2, '')..setRange(0, _spelledCount, _spelled);
    }
    _spelled[_spelledCount++] = name;
    _varint(0);
    _text(name);
  }

  /// [s] as the widget wire writes a string: `varint(n << 1 | wide) + 1`, then Latin-1 bytes or
  /// UTF-16 code units little endian. Copied, not encoded.
  void _text(String s) {
    final n = s.length;
    var latin1 = 0;
    while (latin1 < n && s.codeUnitAt(latin1) <= 0xFF) {
      latin1++;
    }
    final wide = latin1 < n;
    _varint(n * 2 + (wide ? 2 : 1));
    _ensure(wide ? 2 * n : n);
    final buf = bytes;
    var at = length;
    if (!wide) {
      for (var i = 0; i < n; i++) {
        buf[at++] = s.codeUnitAt(i);
      }
    } else {
      for (var i = 0; i < n; i++) {
        final c = s.codeUnitAt(i);
        buf[at++] = c & 0xFF;
        buf[at++] = c >> 8;
      }
    }
    length = at;
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
