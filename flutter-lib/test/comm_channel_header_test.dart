// A message names its channel by numbers (Java's ChannelHeader): a channel addressed to one widget
// by the name's, the type's and the id, a global one by its number alone. Only a name nothing
// numbers is spelled out.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'support/channels.dart' as channels;

String _roundTrip(String channel) {
  final frame = channels.frame(channel, Uint8List.fromList([7, 8, 9]));
  final (read, start) = ChannelHeader.read(frame)!;
  expect(frame.sublist(start), [7, 8, 9]);
  return read;
}

bool _spellsOut(String channel, String part) =>
    latin1.decode(channels.frame(channel, Uint8List(0))).contains(part);

void main() {
  test('a channel addressed to a widget travels as numbers', () {
    expect(_roundTrip('Label/123456789/Paint/Paint'), 'Label/123456789/Paint/Paint');
    expect(_spellsOut('Label/123456789/Paint/Paint', 'Paint'), isFalse);
    expect(_spellsOut('Label/123456789/Paint/Paint', 'Label'), isFalse);
    expect(channels.frame('Label/123456789/Paint/Paint', Uint8List(0)).length, lessThanOrEqualTo(8));
  });

  test('a negative id and a widget\'s own channel round-trip', () {
    expect(_roundTrip('Button/-2147483648/MouseTrack/MouseEnter'), 'Button/-2147483648/MouseTrack/MouseEnter');
    expect(_roundTrip('Shell/42'), 'Shell/42');
  });

  test('a global channel travels as its number', () {
    expect(_roundTrip('swt.evolve.wire'), 'swt.evolve.wire');
    expect(_roundTrip('GC/create'), 'GC/create');
    expect(_spellsOut('swt.evolve.wire', 'wire'), isFalse);
  });
}
