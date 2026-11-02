// What a reader keeps between frames starts small and grows: a paint using more kinds of op than
// it first holds, and a change naming more properties, must read as any other.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/gc_wire.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/gcdrawer.dart' show gcOpSignatures;
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

import 'support/gc_frames.dart';

void main() {
  test('a paint using many kinds of op reads every one', () {
    // Ops taking only ints, so their arguments write the same way.
    final ops = [
      for (final e in gcOpSignatures.entries)
        if (e.value.isNotEmpty && e.value.split('').every((c) => c == 'i')) e.key,
    ].take(20).toList();
    expect(ops.length, greaterThan(8));
    final frame = GcFrame();
    for (var round = 0; round < 2; round++) {
      for (final number in ops) {
        frame.numbered(wireChannelNames[number]!, (f) {
          for (var k = 0; k < gcOpSignatures[number]!.length; k++) {
            f.i(k);
          }
        });
      }
    }

    final r = GcWireReader(frame.bytes);
    final read = <int>[];
    while (r.hasMore) {
      final tag = r.varint();
      read.add(r.opNumber(tag));
      r.skip(r.signatureOf(tag));
    }

    expect(read, [...ops, ...ops]);
  });

  test('a change naming many properties keeps every one', () {
    final keys = WireKeys();
    for (var k = 0; k < 40; k++) {
      keys.add(k * 3);
    }
    expect(keys.length, 40);
    expect([for (var i = 0; i < keys.length; i++) keys[i]], [for (var k = 0; k < 40; k++) k * 3]);
    expect(keys.toList().length, 40);
  });
}
