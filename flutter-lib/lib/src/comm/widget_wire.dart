import 'dart:convert';
import 'dart:typed_data';

import '../gen/color.dart';
import '../gen/image.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/rgb.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart' show wireTypeNames;
import 'comm.dart';

/// Sends [value] to Java in the wire's encoding.
void sendWire(String channel, WireWritable value) =>
    EquoCommService.sendBytes(channel, WidgetWireWriter.encode(value));

/// Sends what [write] writes, for a message Java reads field by field in that order.
void sendWireWith(String channel, void Function(WidgetWireWriter w) write) =>
    EquoCommService.sendBytes(channel, WidgetWireWriter.encodeWith(write));

/// Sends [text] as its UTF-8 bytes, what Java reads a string message as.
void sendString(String channel, String text) =>
    EquoCommService.sendBytes(channel, utf8.encode(text));

/// The channel a flush's binary frame of widget records arrives on.
const String widgetWireChannel = 'swt.evolve.wire';

/// Record kinds; see Java's `WidgetWire`.
const int wireWhole = 1;
const int wireChange = 2;
const int wireImage = 3;
const int wireHolder = 4;

/// A value read from a binary frame property by property, each by its number (wire-ids.txt).
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
  /// With [only], a value's writer writes just the properties it names, as a change carries them.
  static Uint8List encodeWith(void Function(WidgetWireWriter w) write, {Set<int>? only}) {
    final w = _shared
      .._size = 0
      .._lastKey = 0
      .._only = only;
    try {
      write(w);
    } finally {
      w._only = null;
    }
    return Uint8List.sublistView(w._buf, 0, w._size);
  }

  /// The properties [encodeWith] was asked for, or null for all of them.
  Set<int>? _only;

  /// Whether a value's writer is to write the property numbered [key].
  bool wants(int key) => _only?.contains(key) ?? true;

  void u8(int b) => _u8(b);

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

  /// An unsigned varint, by arithmetic so a value past 2^32 writes the same on the web.
  void _varint(int v) {
    _ensure(10);
    while (v >= 0x80) {
      _buf[_size++] = (v % 0x80) + 0x80;
      v = v ~/ 0x80;
    }
    _buf[_size++] = v;
  }

  void _zigzag(int v) => _varint(v >= 0 ? v * 2 : -v * 2 - 1);

  /// A field's number, as the delta from the previous one's.
  void key(int key) {
    _zigzag(key - _lastKey);
    _lastKey = key;
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
  void ref(int? id) => _varint(id == null ? 0 : (id >= 0 ? id * 2 : -id * 2 - 1) + 1);

  /// An object: a presence byte, the fields [fields] writes, a 0.
  void object(void Function(WidgetWireWriter w)? fields) {
    if (!present(fields != null)) return;
    final outer = _lastKey;
    final only = _only;
    _lastKey = 0;
    // A property named is written whole: its object's own fields are not filtered.
    _only = null;
    fields!(this);
    _u8(0);
    _lastKey = outer;
    _only = only;
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

/// Resolves a widget id named in a frame to the value held for it, or null.
/// [accepts] says whether a value is what the reference is read as: two values can share an id.
typedef WireRefResolver = VWidget? Function(int id, bool Function(VWidget value) accepts);

/// Reads Java's `WidgetWire` frame. Values carry no types: each read knows from the property what it
/// is reading.
class WidgetWireReader {
  final Uint8List _bytes;
  final ByteData _data;
  int pos = 0;

  final List<String> _types = [];

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

  WidgetWireReader(this._bytes) : _data = ByteData.sublistView(_bytes);

  bool get hasMore => pos < _bytes.length;

  /// A copy of what was read from [from] up to here.
  Uint8List bytesSince(int from) => Uint8List.fromList(Uint8List.sublistView(_bytes, from, pos));

  int u8() => _bytes[pos++];

  int u32() {
    final v = _data.getUint32(pos, Endian.little);
    pos += 4;
    return v;
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
  String type() {
    final ref = varint();
    if (ref & 1 == 1) return wireTypeNames[ref >> 1] ?? '';
    if (ref != 0) return _types[(ref >> 1) - 1];
    final name = string()!;
    _types.add(name);
    return name;
  }

  /// Reads properties into [target] until [end]: each a key and a value. A key is the property's
  /// number less the previous one's, as a zigzag varint.
  void readProperties(WireReadable target, int end, [List<int>? read]) {
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
    final argb = _data.getInt32(pos, Endian.little);
    pos += 4;
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
    final list = <T>[];
    for (var i = 0; i < count - 1; i++) {
      final id = _id();
      if (id == null) continue;
      final held = resolve(id, (v) => v is T);
      if (held == null) unresolved.add(id);
      if (held is T) list.add(held);
    }
    return list;
  }

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
  List<T?>? list<T>(T? Function() read) {
    final count = varint();
    if (count == 0) return null;
    return List<T?>.generate(count - 1, (_) => read());
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
