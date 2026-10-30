// A message names its channel by numbers (Java's ChannelHeader): a channel addressed to one widget
// by the name's, the type's and the id, a global one by its number alone. Only a name nothing
// numbers is spelled out.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';

String _roundTrip(String channel) {
  final frame = ChannelHeader.frame(channel, Uint8List.fromList([7, 8, 9]));
  final (read, start) = ChannelHeader.read(frame)!;
  expect(frame.sublist(start), [7, 8, 9]);
  return read;
}

bool _spellsOut(String channel, String part) =>
    latin1.decode(ChannelHeader.frame(channel, Uint8List(0))).contains(part);

void main() {
  test('a channel addressed to a widget travels as numbers', () {
    expect(_roundTrip('Label/123456789/Paint/Paint'), 'Label/123456789/Paint/Paint');
    expect(_spellsOut('Label/123456789/Paint/Paint', 'Paint'), isFalse);
    expect(_spellsOut('Label/123456789/Paint/Paint', 'Label'), isFalse);
    expect(ChannelHeader.frame('Label/123456789/Paint/Paint', Uint8List(0)).length, lessThanOrEqualTo(8));
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

  test('a name nothing numbers is spelled out', () {
    expect(_roundTrip('evolve.test.frameSync'), 'evolve.test.frameSync');
    expect(_roundTrip('Label/12/noSuchChannel'), 'Label/12/noSuchChannel');
    expect(_spellsOut('evolve.test.frameSync', 'evolve.test.frameSync'), isTrue);
  });

  test('the old two-byte header reads as a spelled-out name', () {
    final name = utf8.encode('evolve.test.query');
    final frame = Uint8List(2 + name.length + 1)..[1] = name.length;
    frame.setRange(2, 2 + name.length, name);
    expect(ChannelHeader.read(frame), ('evolve.test.query', 2 + name.length));
  });
}
