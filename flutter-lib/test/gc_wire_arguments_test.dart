// An op's arguments in Java's GcWire form, read one by one by signature letter, and stepped over by
// signature for an op this side does not draw.

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/gc_wire.dart';

/// Arguments for the signature `iIbfdss`, then a marker byte.
Uint8List _arguments() {
  final b = BytesBuilder()
    ..addByte(3) // i: -2, zigzag
    ..add([4, 2, 1, 6]) // I: three ints, 1, -1, 3
    ..addByte(1) // b
    ..add((ByteData(4)..setFloat32(0, 0.25, Endian.little)).buffer.asUint8List()) // f
    ..add((ByteData(8)..setFloat64(0, -1e-3, Endian.little)).buffer.asUint8List()) // d
    ..addByte(7) // s: three Latin-1 chars, as (3 << 1) + 1
    ..add('abc'.codeUnits)
    ..addByte(6) // s: two UTF-16 chars, as (2 << 1 | 1) + 1
    ..add([0xE5, 0x65, 0x2C, 0x67]) // 日本, little endian
    ..addByte(0x7E);
  return b.toBytes();
}

void main() {
  test('each argument reads as its signature letter says', () {
    final r = GcWireReader(Uint8List.fromList([..._arguments(), 0, 0]));

    expect(r.i(), -2);
    expect(r.ints(), [1, -1, 3]);
    expect(r.b(), isTrue);
    expect(r.f(), 0.25);
    expect(r.d(), -1e-3);
    expect(r.string(), 'abc');
    expect(r.string(), '日本');
    expect(r.varint(), 0x7E);
    expect(r.ints(), isNull);
    expect(r.string(), isNull);
    expect(r.hasMore, isFalse);
  });

  test('an int at either end of its range reads whole', () {
    // Zigzag 2^32 - 1 and 2^32 - 2: five-byte varints whose last group sits past bit 31.
    final r = GcWireReader(Uint8List.fromList([0xFF, 0xFF, 0xFF, 0xFF, 0x0F, 0xFE, 0xFF, 0xFF, 0xFF, 0x0F]));

    expect(r.i(), -2147483648);
    expect(r.i(), 2147483647);
  });

  test('an op this side does not draw is stepped over by its signature', () {
    final r = GcWireReader(_arguments())..skip('iIbfdss');

    expect(r.varint(), 0x7E, reason: 'what follows the op is read from where it starts');
    expect(r.hasMore, isFalse);
  });
}
