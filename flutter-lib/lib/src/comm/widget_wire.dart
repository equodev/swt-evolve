import 'dart:typed_data';

import '../gen/color.dart';
import '../gen/image.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/rgb.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart' show wireTypeNames;
import 'comm.dart';
import 'tagged_values.dart';
import 'widget_key.dart';

/// Sends what [write] writes on the channel numbered [channel] of the widget of type [type] numbered
/// [id], for a message Java reads field by field in that order.
void sendWireWith(int type, int id, int channel, void Function(WidgetWireWriter w) write) {
  final w = WidgetWireWriter.startTo(type, id, channel);
  write(w);
  EquoCommService.sendWritten(w);
}


/// Record kinds; see Java's `WidgetWire`.
const int wireWhole = 1;
const int wireChange = 2;
const int wireImage = 3;
const int wireHolder = 4;

/// A value read from a binary frame property by property, each by its number (the generated WireKey).
abstract interface class WireReadable {
  /// Reads the value of the property numbered [key] into this object; false, with nothing read, when
  /// this class has no such property.
  bool readWire(int key, WidgetWireReader r);
}

/// A property number a value does not know. The frame says nothing about the value's type, so it
/// cannot be stepped over: the record it is in is abandoned.
class WireUnknownProperty implements Exception {
  final int key;
  final Object target;

  const WireUnknownProperty(this.key, this.target);

  @override
  String toString() => '${target.runtimeType} has no property $key';
}

/// A value written in the wire's encoding: an event Java's `WireReader` reads, or a value as Java
/// writes it, which [WidgetWireReader] reads back.
abstract interface class WireWritable {
  /// Writes every field that is set, each as [WidgetWireWriter.key] and value.
  void writeWire(WidgetWireWriter w);
}

/// A value whose fields can be walked by the numbers the wire gives them.
abstract interface class WireVisitable {
  /// Hands every field this value holds to [visit], with its property number.
  void visitWire(void Function(int key, Object? value) visit);
}

/// Writes in the wire's encoding: what [WidgetWireReader] and Java's `WireReader` read.
class WidgetWireWriter {
  Uint8List _buf = Uint8List(256);
  ByteData _data = ByteData(0);
  int _size = 0;
  int _lastKey = 0;

  WidgetWireWriter() {
    _data = ByteData.sublistView(_buf);
  }

  static final WidgetWireWriter _shared = WidgetWireWriter();

  /// [value]'s fields closed by a 0, as a view of a buffer the next call reuses: for a send, which
  /// copies it into its frame before returning.
  static Uint8List encode(WireWritable value) {
    final w = _shared
      .._size = 0
      .._lastKey = 0;
    value.writeWire(w);
    w._u8(0);
    return Uint8List.sublistView(w._buf, 0, w._size);
  }

  /// What [write] writes, as [encode] returns it: for a message Java reads field by field in order.
  static Uint8List encodeWith(void Function(WidgetWireWriter w) write) {
    final w = _shared
      .._size = 0
      .._lastKey = 0;
    write(w);
    return Uint8List.sublistView(w._buf, 0, w._size);
  }

  /// The frame of the channel numbered [channel] of the widget of type [type] numbered [id]: its
  /// header, written from those numbers, and [payload] after it, in a buffer of its own.
  static Uint8List frameTo(int type, int id, int channel, Object? payload) {
    final w = _shared
      .._size = 0
      .._lastKey = 0
      .._header(type, id, channel);
    return w._close(payload);
  }

  /// The frame of the global channel numbered [channel], [payload] after its header.
  static Uint8List frameOn(int channel, Object? payload) {
    final w = _shared
      .._size = 0
      .._lastKey = 0;
    w._varint(channel * 2);
    return w._close(payload);
  }

  /// [frameTo], left in the shared writer: valid until its next use.
  static WidgetWireWriter writeTo(int type, int id, int channel, Object? payload) {
    final w = _shared
      .._size = 0
      .._lastKey = 0
      .._header(type, id, channel);
    w._payload(payload);
    return w;
  }

  /// [frameOn], left in the shared writer: valid until its next use.
  static WidgetWireWriter writeOn(int channel, Object? payload) {
    final w = _shared
      .._size = 0
      .._lastKey = 0;
    w._varint(channel * 2);
    w._payload(payload);
    return w;
  }

  /// What has been written lies in its first [size] bytes; the next write may move it.
  Uint8List get buffer => _buf;

  /// The shared writer, started on the frame of the channel numbered [channel] of the widget of
  /// type [type] numbered [id]: the caller writes the body, then [finish]es it.
  static WidgetWireWriter startTo(int type, int id, int channel) => _shared
    .._size = 0
    .._lastKey = 0
    .._header(type, id, channel);

  /// The header of a widget's channel.
  void _header(int type, int id, int channel) {
    _varint(channel * 2 + 1);
    _varint(type);
    _zigzag(id);
  }

  /// [startTo] for the global channel numbered [channel].
  static WidgetWireWriter startOn(int channel) {
    final w = _shared
      .._size = 0
      .._lastKey = 0;
    w._varint(channel * 2);
    return w;
  }

  /// The frame [startTo] or [startOn] began, in a buffer of its own.
  Uint8List finish() => _frame();

  /// The frame with [payload] after the header written: bytes are copied once, into the frame.
  Uint8List _close(Object? payload) {
    if (payload is Uint8List) {
      final header = _size;
      return Uint8List(header + payload.length)
        ..setRange(0, header, _buf)
        ..setRange(header, header + payload.length, payload);
    }
    _payload(payload);
    return _frame();
  }

  /// A message's body as Java reads it: a value's fields closed by a 0, bytes as they are, a string
  /// as the widget wire writes a string, anything else as tagged values.
  void _payload(Object? payload) {
    switch (payload) {
      case null:
        return;
      case WireWritable():
        payload.writeWire(this);
        _u8(0);
      case Uint8List():
        _raw(payload);
      case String():
        string(payload);
      default:
        final length = TaggedValues.encodeShared(payload);
        _ensure(length);
        _buf.setRange(_size, _size + length, TaggedValues.sharedBytes);
        _size += length;
    }
  }

  Uint8List _frame() => Uint8List(_size)..setRange(0, _size, _buf);

  void _raw(Uint8List bytes) {
    _ensure(bytes.length);
    _buf.setRange(_size, _size + bytes.length, bytes);
    _size += bytes.length;
  }

  void u8(int b) => _u8(b);

  /// How many bytes have been written.
  int get size => _size;

  /// What has been written, as a view the next write may change.
  Uint8List get written => Uint8List.sublistView(_buf, 0, _size);

  /// An unsigned varint: a count or a length.
  void varint(int v) => _varint(v);

  void _ensure(int n) {
    if (_size + n <= _buf.length) return;
    var length = _buf.length * 2;
    while (length < _size + n) {
      length *= 2;
    }
    _buf = Uint8List(length)..setRange(0, _size, _buf);
    _data = ByteData.sublistView(_buf);
  }

  void _u8(int b) {
    _ensure(1);
    _buf[_size++] = b;
  }

  /// Whether this runs as JavaScript, where bit operators truncate to 32 bits: a varint is written by
  /// arithmetic there, so a value past 2^32 writes the same as on the VM.
  static const bool _web = identical(0, 0.0);

  /// The longest varint: an int64's zigzag.
  static const int _maxVarint = 10;

  void _varint(int v) {
    _ensure(_maxVarint);
    _size = _put(_buf, _size, v);
  }

  /// Writes the unsigned varint [v] into [buf] at [at], which has room for it; returns its end.
  @pragma('vm:prefer-inline')
  @pragma('dart2js:prefer-inline')
  static int _put(Uint8List buf, int at, int v) {
    if (_web) {
      while (v >= 0x80) {
        buf[at++] = (v % 0x80) + 0x80;
        v = v ~/ 0x80;
      }
    } else {
      while (v >= 0x80) {
        buf[at++] = (v & 0x7F) | 0x80;
        v >>= 7;
      }
    }
    buf[at] = v;
    return at + 1;
  }

  @pragma('vm:prefer-inline')
  @pragma('dart2js:prefer-inline')
  static int _zz(int v) => _web ? (v >= 0 ? v * 2 : -v * 2 - 1) : (v << 1) ^ (v >> 63);

  void _zigzag(int v) => _varint(_zz(v));

  /// A field's number, as the delta from the previous one's.
  void key(int key) {
    _zigzag(key - _lastKey);
    _lastKey = key;
  }

  /// Room for a key and a value of up to [value] bytes after it, and where the key goes: what a
  /// field writer below starts with, so it checks the buffer once.
  @pragma('vm:prefer-inline')
  @pragma('dart2js:prefer-inline')
  int _keyAt(int key, int value) {
    _ensure(_maxVarint + value);
    final at = _put(_buf, _size, _zz(key - _lastKey));
    _lastKey = key;
    return at;
  }

  /// [key] and then [int32] of [v].
  void keyInt32(int key, int v) {
    final at = _keyAt(key, _maxVarint);
    _size = _put(_buf, at, _zz(v));
  }

  /// [key] and then [int64] of [v].
  void keyInt64(int key, int v) {
    final at = _keyAt(key, _maxVarint);
    _size = _put(_buf, at, _zz(v));
  }

  /// [key] and then [char16] of [v].
  void keyChar16(int key, int v) {
    final at = _keyAt(key, _maxVarint);
    _size = _put(_buf, at, v);
  }

  /// [key] and then [boolean] of [v].
  void keyBoolean(int key, bool v) {
    final at = _keyAt(key, 1);
    _buf[at] = v ? 1 : 0;
    _size = at + 1;
  }

  /// [key] and then [ref] of [id].
  void keyRef(int key, int? id) {
    final at = _keyAt(key, _maxVarint);
    _size = _put(_buf, at, id == null ? 0 : _zz(id) + 1);
  }

  /// Whether a value follows, as a presence byte; returns [present] so a writer can go on to it.
  bool present(bool present) {
    _u8(present ? 1 : 0);
    return present;
  }

  /// A colour as its ARGB word, or null.
  void color(int? argb) {
    if (!present(argb != null)) return;
    _ensure(4);
    _data.setUint32(_size, argb!, Endian.little);
    _size += 4;
  }

  void point(int x, int y) {
    present(true);
    _zigzag(x);
    _zigzag(y);
  }

  void rect(int x, int y, int width, int height) {
    present(true);
    _zigzag(x);
    _zigzag(y);
    _zigzag(width);
    _zigzag(height);
  }

  /// [c] as its ARGB word, or null.
  void colorValue(VColor? c) =>
      color(c == null ? null : ((c.alpha & 0xFF) << 24) | ((c.red & 0xFF) << 16) | ((c.green & 0xFF) << 8) | (c.blue & 0xFF));

  void pointValue(VPoint? p) {
    if (p == null) {
      present(false);
    } else {
      point(p.x, p.y);
    }
  }

  void rectValue(VRectangle? r) {
    if (r == null) {
      present(false);
    } else {
      rect(r.x, r.y, r.width, r.height);
    }
  }

  void rgbValue(VRGB? v) {
    if (!present(v != null)) return;
    _u8(v!.red);
    _u8(v.green);
    _u8(v.blue);
  }

  /// Widgets by id, length first; null for none.
  void refs(List<VWidget?>? v) {
    if (v == null) {
      _varint(0);
      return;
    }
    _varint(v.length + 1);
    for (final e in v) {
      ref(e?.id);
    }
  }

  void floats(List<double>? v) {
    if (v == null) {
      _varint(0);
      return;
    }
    _varint(v.length + 1);
    for (final e in v) {
      float32(e);
    }
  }

  void doubles(List<double>? v) {
    if (v == null) {
      _varint(0);
      return;
    }
    _varint(v.length + 1);
    for (final e in v) {
      float64(e);
    }
  }

  /// A list, length first, each element written by [write]; null for none.
  void list<T>(List<T>? v, void Function(T e) write) {
    if (v == null) {
      _varint(0);
      return;
    }
    _varint(v.length + 1);
    v.forEach(write);
  }

  /// Objects that each name their class by number ([typeName] gives it by name), length first; null
  /// for none.
  void polys<T extends WireWritable>(List<T?>? v, String Function(T e) typeName) {
    if (v == null) {
      _varint(0);
      return;
    }
    _varint(v.length + 1);
    for (final e in v) {
      if (!present(e != null)) continue;
      final type = _typeNumbers[typeName(e!)];
      if (type == null) throw StateError('no type number for ${typeName(e)}');
      _varint(type);
      final outer = _lastKey;
      _lastKey = 0;
      e.writeWire(this);
      _u8(0);
      _lastKey = outer;
    }
  }

  static final Map<String, int> _typeNumbers = {
    for (final e in wireTypeNames.entries) e.value: e.key,
  };

  /// Bytes as they are, length first; null for none.
  void bytes(List<int>? v) {
    if (v == null) {
      _varint(0);
      return;
    }
    _varint(v.length + 1);
    _ensure(v.length);
    _buf.setRange(_size, _size + v.length, v);
    _size += v.length;
  }

  /// An image or a widget by id; null for none.
  void ref(int? id) => _varint(id == null ? 0 : _zz(id) + 1);

  /// An object: a presence byte, the fields [fields] writes, a 0.
  void object(void Function(WidgetWireWriter w)? fields) {
    if (!present(fields != null)) return;
    final outer = _lastKey;
    _lastKey = 0;
    fields!(this);
    _u8(0);
    _lastKey = outer;
  }

  void boolean(bool v) => _u8(v ? 1 : 0);

  void boolBoxed(bool v) => _u8(v ? 2 : 1);

  void int32(int v) => _zigzag(v);

  void int64(int v) => _zigzag(v);

  void char16(int v) => _varint(v);

  void float32(double v) {
    _ensure(4);
    _data.setFloat32(_size, v, Endian.little);
    _size += 4;
  }

  void float64(double v) {
    _ensure(8);
    _data.setFloat64(_size, v, Endian.little);
    _size += 8;
  }

  void intBoxed(int v) {
    _u8(1);
    _zigzag(v);
  }

  void longBoxed(int v) => intBoxed(v);

  void floatBoxed(double v) {
    _u8(1);
    float32(v);
  }

  void doubleBoxed(double v) {
    _u8(1);
    float64(v);
  }

  /// A string as both ends hold one: `varint(n << 1 | wide) + 1`, then Latin-1 bytes or UTF-16 code
  /// units little endian.
  void string(String v) {
    final n = v.length;
    var latin1 = 0;
    while (latin1 < n && v.codeUnitAt(latin1) <= 0xFF) {
      latin1++;
    }
    if (latin1 == n) {
      _varint(n * 2 + 1);
      _ensure(n);
      for (var i = 0; i < n; i++) {
        _buf[_size++] = v.codeUnitAt(i);
      }
      return;
    }
    _varint(n * 2 + 2);
    _ensure(2 * n);
    for (var i = 0; i < n; i++) {
      final c = v.codeUnitAt(i);
      _buf[_size++] = c & 0xFF;
      _buf[_size++] = c >> 8;
    }
  }

  void ints(List<int> v) {
    _varint(v.length + 1);
    for (final e in v) {
      _zigzag(e);
    }
  }

  void longs(List<int> v) => ints(v);

  void chars(List<int> v) {
    _varint(v.length + 1);
    for (final e in v) {
      _varint(e);
    }
  }

  void strings(List<String?> v) {
    _varint(v.length + 1);
    for (final e in v) {
      if (e == null) {
        _u8(0);
      } else {
        string(e);
      }
    }
  }
}

/// The property numbers a read named, in order: kept and refilled, so collecting them allocates
/// nothing once it has grown to the largest change.
class WireKeys {
  List<int> _keys = List.filled(8, 0);
  int length = 0;

  int operator [](int i) => _keys[i];

  void add(int key) {
    if (length == _keys.length) _keys = List.filled(length * 2, 0)..setRange(0, length, _keys);
    _keys[length++] = key;
  }

  void clear() => length = 0;

  bool any(bool Function(int key) test) {
    for (var i = 0; i < length; i++) {
      if (test(_keys[i])) return true;
    }
    return false;
  }

  List<int> toList() => List.generate(length, (i) => _keys[i], growable: false);
}

/// Resolves a widget id named in a frame to the value held for it, or null.
/// [accepts] says whether a value is what the reference is read as: two values can share an id.
typedef WireRefResolver = VWidget? Function(int id, bool Function(VWidget value) accepts);

/// Reads Java's `WidgetWire` frame. Values carry no types: each read knows from the property what it
/// is reading.
class WidgetWireReader {
  Uint8List _bytes;
  ByteData? _view;
  int pos = 0;

  /// The frame as ByteData, made the first time a float is read from it.
  ByteData get _data => _view ??= ByteData.sublistView(_bytes);

  /// Resolves widget references.
  WireRefResolver resolve = (_, __) => null;

  /// Widget ids a value named that nothing here holds.
  final List<int> unresolved = [];

  /// Images named by id in this frame: the one held, or a stand-in its record further down fills.
  final Map<int, VImage> images = {};

  /// The widget whose record is being read, which names what [imageFor] is asked for.
  int namingRecord = 0;

  /// Images named in this frame that nothing here held, by id, with the widget that named each: one
  /// still a stand-in once the frame is read never reached this client.
  final Map<int, int> imagesNamedUnheld = {};

  /// A reader over [_bytes] from [start] up to [end], its end when not given.
  WidgetWireReader(this._bytes, [int start = 0, int? end])
      : pos = start,
        _end = end ?? _bytes.length;

  int _end;

  /// Reads [bytes] from [start] to [end] next, as a new reader would: for one kept and reused.
  void reset(Uint8List bytes, int start, int end) {
    _bytes = bytes;
    _view = null;
    pos = start;
    _end = end;
    unresolved.clear();
    images.clear();
    imagesNamedUnheld.clear();
  }

  bool get hasMore => pos < _end;

  /// A copy of what was read from [from] up to here.
  Uint8List bytesSince(int from) => Uint8List.fromList(Uint8List.sublistView(_bytes, from, pos));

  int u8() => _bytes[pos++];

  int u32() {
    final b = _bytes;
    final at = pos;
    pos += 4;
    // By arithmetic: a shift into bit 31 is negative on the web.
    return b[at] + (b[at + 1] << 8) + (b[at + 2] << 16) + b[at + 3] * 0x1000000;
  }

  /// An unsigned varint, by arithmetic so a value past 2^32 reads the same on the web.
  int varint() {
    var x = _bytes[pos++];
    if (x < 0x80) return x;
    var v = x & 0x7F;
    var scale = 128;
    while (true) {
      x = _bytes[pos++];
      v += (x & 0x7F) * scale;
      if (x < 0x80) return v;
      scale *= 128;
    }
  }

  /// A zigzag varint.
  int zigzag() {
    final v = varint();
    // Arithmetic, not `^`: on the web a bitwise result is unsigned.
    return v.isEven ? v ~/ 2 : -(v ~/ 2) - 1;
  }

  /// A record's type: odd, its number; 0, its name spelled out; otherwise a name spelled out earlier
  /// in the frame, by its place.
  String type() => WidgetKey.typeName(typeNumber());

  /// A widget's type as its number (see [WidgetKey]): written odd, as every type has one.
  int typeNumber() {
    final ref = varint();
    if (ref & 1 == 0) throw FormatException('a widget type without a number at $pos');
    return ref >> 1;
  }

  /// Reads properties into [target] until [end]: each a key and a value. A key is the property's
  /// number less the previous one's, as a zigzag varint.
  void readProperties(WireReadable target, int end, [WireKeys? read]) {
    var key = 0;
    while (pos < end) {
      key += zigzag();
      if (!target.readWire(key, this)) throw WireUnknownProperty(key, target);
      read?.add(key);
    }
  }

  // --- values: each read knows what it reads ---

  bool boolean() => _bytes[pos++] != 0;

  bool? boolBoxed() {
    final b = _bytes[pos++];
    return b == 0 ? null : b == 2;
  }

  int int32() => zigzag();

  int int64() => zigzag();

  int char16() => varint();

  bool _present() => _bytes[pos++] != 0;

  int? intBoxed() => _present() ? zigzag() : null;

  int? longBoxed() => _present() ? zigzag() : null;

  double float32() {
    final v = _data.getFloat32(pos, Endian.little);
    pos += 4;
    return v;
  }

  double float64() {
    final v = _data.getFloat64(pos, Endian.little);
    pos += 8;
    return v;
  }

  double? floatBoxed() => _present() ? float32() : null;

  double? doubleBoxed() => _present() ? float64() : null;

  /// A string in Java's `WidgetWire` form: Latin-1 bytes or UTF-16 code units, copied, not decoded.
  String? string() {
    final header = varint();
    if (header == 0) return null;
    final n = (header - 1) >> 1;
    if ((header - 1).isEven) {
      final end = pos + n;
      final s = String.fromCharCodes(_bytes, pos, end);
      pos = end;
      return s;
    }
    final units = Uint16List(n);
    for (var i = 0; i < n; i++) {
      units[i] = _bytes[pos] | (_bytes[pos + 1] << 8);
      pos += 2;
    }
    return String.fromCharCodes(units);
  }

  List<int>? ints() {
    final count = varint();
    if (count == 0) return null;
    final list = List<int>.filled(count - 1, 0);
    for (var i = 0; i < list.length; i++) {
      list[i] = zigzag();
    }
    return list;
  }

  List<int>? longs() => ints();

  List<int>? chars() {
    final count = varint();
    if (count == 0) return null;
    final list = List<int>.filled(count - 1, 0);
    for (var i = 0; i < list.length; i++) {
      list[i] = varint();
    }
    return list;
  }

  /// Raw bytes, copied out of the frame: the frame's buffer is not kept.
  Uint8List? bytes() {
    final count = varint();
    if (count == 0) return null;
    final end = pos + count - 1;
    final bytes = Uint8List.fromList(Uint8List.sublistView(_bytes, pos, end));
    pos = end;
    return bytes;
  }

  List<double>? floats() {
    final count = varint();
    if (count == 0) return null;
    return List<double>.generate(count - 1, (_) => float32());
  }

  List<double>? doubles() {
    final count = varint();
    if (count == 0) return null;
    return List<double>.generate(count - 1, (_) => float64());
  }

  List<String?>? strings() {
    final count = varint();
    if (count == 0) return null;
    final list = List<String?>.filled(count - 1, null);
    for (var i = 0; i < list.length; i++) {
      list[i] = string();
    }
    return list;
  }

  VColor? color() {
    if (!_present()) return null;
    final argb = u32();
    return VColor()
      ..alpha = (argb >> 24) & 0xFF
      ..red = (argb >> 16) & 0xFF
      ..green = (argb >> 8) & 0xFF
      ..blue = argb & 0xFF;
  }

  VPoint? point() {
    if (!_present()) return null;
    return VPoint()
      ..x = zigzag()
      ..y = zigzag();
  }

  VRectangle? rect() {
    if (!_present()) return null;
    return VRectangle()
      ..x = zigzag()
      ..y = zigzag()
      ..width = zigzag()
      ..height = zigzag();
  }

  VRGB? rgb() {
    if (!_present()) return null;
    return VRGB()
      ..red = _bytes[pos++]
      ..green = _bytes[pos++]
      ..blue = _bytes[pos++];
  }

  int? _id() {
    final v = varint();
    if (v == 0) return null;
    final z = v - 1;
    return z.isEven ? z ~/ 2 : -(z ~/ 2) - 1;
  }

  /// A widget named by id, as the value held for it.
  T? ref<T extends VWidget>() {
    final id = _id();
    if (id == null) return null;
    final held = resolve(id, (v) => v is T);
    if (held == null) unresolved.add(id);
    return held is T ? held : null;
  }

  /// Widgets named by id. One nothing here holds is left out, and asked for by the caller.
  List<T>? refs<T extends VWidget>() {
    final count = varint();
    if (count == 0) return null;
    // Resolved into a list kept for it, then copied once at the size it came to: a list grown one
    // child at a time is reallocated as it doubles. One type test for the whole list.
    final accepts = _acceptsOf<T>();
    if (_refsScratch.length < count) _refsScratch = List<VWidget?>.filled(count * 2, null);
    final resolved = _refsScratch;
    var n = 0;
    for (var i = 0; i < count - 1; i++) {
      final id = _id();
      if (id == null) continue;
      final held = resolve(id, accepts);
      if (held == null) unresolved.add(id);
      if (held is T) resolved[n++] = held;
    }
    final list = List<T>.generate(n, (i) => resolved[i] as T, growable: true);
    resolved.fillRange(0, n, null);
    return list;
  }

  /// What [refs] resolves into before copying, grown and never shrunk: a growable list truncated to
  /// empty gives its storage back, and growing it again reallocates at every doubling.
  List<VWidget?> _refsScratch = const [];

  /// The type test [refs] resolves its ids with, made once per element type.
  static bool Function(VWidget value) _acceptsOf<T extends VWidget>() =>
      (_accepts[T] ??= (VWidget v) => v is T);

  static final Map<Type, bool Function(VWidget value)> _accepts = {};

  /// An image named by id: the one held, or a stand-in its record further down the frame fills.
  VImage? image() {
    final id = _id();
    return id == null ? null : imageFor(id);
  }

  /// The image this frame means by [id]: one already held, else the stand-in its record fills.
  VImage imageFor(int id) => images.putIfAbsent(id, () {
        final standIn = VImage()
          ..id = id
          ..isReference = true;
        final held = rememberOrResolveImage(standIn);
        if (identical(held, standIn)) imagesNamedUnheld[id] = namingRecord;
        return held;
      });

  /// An object: what [create] makes, its properties read until the 0 that closes it.
  T? object<T extends WireReadable>(T Function() create) {
    if (!_present()) return null;
    final value = create();
    _fields(value);
    return value;
  }

  /// Reads [value]'s fields until the 0 that closes them.
  T fields<T extends WireReadable>(T value) {
    _fields(value);
    return value;
  }

  void _fields(WireReadable value) {
    var key = 0;
    while (true) {
      final delta = zigzag();
      if (delta == 0) return;
      key += delta;
      if (!value.readWire(key, this)) throw WireUnknownProperty(key, value);
    }
  }

  /// A list, each element read by [read].
  /// A list, each element read by [read] from this reader. [read] is handed the reader rather than
  /// closing over it: a closure over `readWire`'s own reader would make every call of it allocate,
  /// whichever property it reads.
  List<T?>? list<T>(T? Function(WidgetWireReader r) read) {
    final count = varint();
    if (count == 0) return null;
    final list = List<T?>.filled(count - 1, null);
    for (var i = 0; i < list.length; i++) {
      list[i] = read(this);
    }
    return list;
  }

  /// Objects whose class each one names by number, made by [create] from that class's name.
  List<T>? polys<T>(Object? Function(String name) create) {
    final count = varint();
    if (count == 0) return null;
    final list = <T>[];
    for (var i = 0; i < count - 1; i++) {
      if (!_present()) continue;
      final value = create(wireTypeNames[varint()] ?? '');
      if (value is! WireReadable) throw StateError('no value class for a polymorphic element');
      _fields(value);
      if (value is T) list.add(value as T);
    }
    return list;
  }

  Map<String, String>? stringMap() {
    final count = varint();
    if (count == 0) return null;
    final map = <String, String>{};
    for (var i = 0; i < count - 1; i++) {
      final k = string() ?? '';
      map[k] = string() ?? '';
    }
    return map;
  }

  Map<String, bool>? boolMap() {
    final count = varint();
    if (count == 0) return null;
    final map = <String, bool>{};
    for (var i = 0; i < count - 1; i++) {
      final k = string() ?? '';
      final b = boolBoxed();
      if (b != null) map[k] = b;
    }
    return map;
  }
}
