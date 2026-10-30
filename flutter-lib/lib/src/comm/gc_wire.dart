import 'dart:typed_data';

import '../gen/gcdrawer.dart' show gcOpSignatures;
import 'channel_header.dart';
import 'tagged_values.dart';
import 'widget_wire.dart';

/// Reads one paint of a GC as Java's `GcWire` writes it: a run of records, each an op with its
/// arguments, a change to the GC's state or the whole of it, or an escape carrying a channel and its
/// payload.
///
/// An op goes by its number the first time a frame uses it, and by its place in the frame after.
class GcWireReader {
  static const int state = 1;
  static const int escape = 2;
  static const int opNumbered = 3;
  static const int whole = 4;
  static const int opFirst = 5;

  Uint8List _bytes;
  ByteData? _view;
  int _pos;
  int _end;

  /// The frame as ByteData, made the first time a float is read from it.
  ByteData get _data => _view ??= ByteData.sublistView(_bytes);

  /// What a [state] or [whole] record, or a `w` argument, is read with: kept, and read before the
  /// next one is asked for.
  final WidgetWireReader _wire = WidgetWireReader(Uint8List(0));

  /// Per op by its place in the frame: its number.
  List<int> _numbers = List.filled(8, 0);
  int _ops = 0;

  /// A reader over [_bytes] from [start] up to [end], its end when not given.
  GcWireReader(this._bytes, [int start = 0, int? end])
      : _pos = start,
        _end = end ?? _bytes.length;

  /// Reads [bytes] from [start] to [end] next, as a new reader would: for one kept and reused.
  void reset(Uint8List bytes, int start, int end) {
    _bytes = bytes;
    _view = null;
    _pos = start;
    _end = end;
    _ops = 0;
  }

  /// The frame read, for an [escape] record's header (see [readEscape]).
  Uint8List get bytes => _bytes;

  /// Where the last [readEscape] found its channel's header in [bytes].
  int escapeHeaderAt = 0;

  /// Where the last [readEscape]'s payload lies in [bytes].
  int escapePayloadStart = 0, escapePayloadEnd = 0;

  /// Reads an [escape] record without naming its channel: the comm reads the header where it is.
  void readEscape() {
    escapeHeaderAt = _pos;
    _pos = ChannelHeader.skip(_bytes, _pos);
    final length = varint();
    escapePayloadStart = _pos;
    escapePayloadEnd = _pos + length - 1;
    _pos += length - 1;
  }

  bool get hasMore => _pos < _end;


  /// An unsigned varint, by arithmetic as [WidgetWireReader.varint] reads it, so a value past 2^31
  /// reads the same on the web.
  int varint() {
    var x = _bytes[_pos++];
    if (x < 0x80) return x;
    var v = x & 0x7F;
    var scale = 128;
    while (true) {
      x = _bytes[_pos++];
      v += (x & 0x7F) * scale;
      if (x < 0x80) return v;
      scale *= 128;
    }
  }

  /// The number of the op [tag] introduces (see [opNumbered]), defining its place in the frame
  /// first when the frame numbers it here.
  int opNumber(int tag) {
    if (tag >= opFirst) return _numbers[tag - opFirst];
    if (tag != opNumbered) throw StateError('GC frame tag $tag is not one');
    final number = varint();
    if (_ops == _numbers.length) {
      _numbers = List.filled(_ops * 2, 0)..setRange(0, _ops, _numbers);
    }
    _numbers[_ops++] = number;
    return number;
  }

  /// The argument signature of the op [tag] just introduced, for skipping it.
  String signatureOf(int tag) => gcOpSignatures[_numbers[tag >= opFirst ? tag - opFirst : _ops - 1]] ?? '';

  /// A [state] or [whole] record's properties, or a `w` argument: a reader over what the widget wire
  /// wrote there.
  WidgetWireReader wire() {
    final length = varint();
    _wire.reset(_bytes, _pos, _pos + length - 1);
    _pos += length - 1;
    return _wire;
  }

  // Arguments, by signature letter.

  /// `i`
  int i() {
    final v = varint();
    // Arithmetic, not `^`: on the web a bitwise result is unsigned, so -1 would come back as 2^32-1.
    return v.isEven ? v ~/ 2 : -(v ~/ 2) - 1;
  }

  /// `I`
  List<int>? ints() {
    final length = varint();
    if (length == 0) return null;
    return List<int>.generate(length - 1, (_) => i(), growable: false);
  }

  /// `b`
  bool b() => _bytes[_pos++] != 0;

  /// `s`: as the widget wire writes a string, Latin-1 bytes or UTF-16 code units, copied, not decoded.
  String? string() {
    final header = varint();
    if (header == 0) return null;
    final n = (header - 1) >> 1;
    if ((header - 1).isEven) {
      final end = _pos + n;
      final s = String.fromCharCodes(_bytes, _pos, end);
      _pos = end;
      return s;
    }
    final units = Uint16List(n);
    for (var i = 0; i < n; i++) {
      units[i] = _bytes[_pos] | (_bytes[_pos + 1] << 8);
      _pos += 2;
    }
    return String.fromCharCodes(units);
  }

  /// `f`
  double f() {
    final v = _data.getFloat32(_pos, Endian.little);
    _pos += 4;
    return v;
  }

  /// `d`
  double d() {
    final v = _data.getFloat64(_pos, Endian.little);
    _pos += 8;
    return v;
  }

  /// An escape's payload: tagged values, read back as maps, lists and scalars.
  Object? payload() {
    final length = varint();
    if (length == 0) return null;
    final bytes = Uint8List.sublistView(_bytes, _pos, _pos + length - 1);
    _pos += length - 1;
    return bytes.isEmpty ? null : TaggedValues.decode(bytes);
  }

  /// Steps over an op's arguments by its signature, for an op this side does not draw.
  void skip(String signature) {
    for (final c in signature.codeUnits) {
      switch (c) {
        case 0x69: // i
          varint();
        case 0x49: // I
          ints();
        case 0x62: // b
          _pos++;
        case 0x66: // f
          _pos += 4;
        case 0x64: // d
          _pos += 8;
        case 0x73: // s
          // Latin-1: n bytes after (n << 1) + 1; UTF-16: 2n bytes after (n << 1 | 1) + 1.
          final header = varint();
          if (header > 0) _pos += (header - 1).isEven ? (header - 1) >> 1 : header - 2;
        default: // w
          final length = varint();
          if (length > 0) _pos += length - 1;
      }
    }
  }
}
