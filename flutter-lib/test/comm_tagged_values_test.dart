// A payload the wire has no class encoding for travels as tagged values (Java's TaggedValues): maps,
// lists and scalars in binary, map keys by number, and text the only string. It reads back as the
// same maps, lists and scalars that were encoded, so the handlers receiving it see plain values.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';

void main() {
  final values = <String, dynamic>{
    'doit': true,
    'text': 'déjà vu',
    'x': -12,
    'width': 3000000000,
    'alpha': 0.5,
    'nothing': null,
    'items': [1, 'two', false, null, <String, dynamic>{'y': 2}],
    'aKeyNothingNumbers': 'spelled out',
    'nested': <String, dynamic>{'aKeyNothingNumbers': 1},
  };

  test('maps, lists and scalars read back as the values encoded', () {
    final read = TaggedValues.decode(TaggedValues.encode(values));
    expect(read, {
      'doit': true,
      'text': 'déjà vu',
      'x': -12,
      'width': 3000000000,
      'alpha': 0.5,
      'nothing': null,
      'items': [1, 'two', false, null, {'y': 2}],
      'aKeyNothingNumbers': 'spelled out',
      'nested': {'aKeyNothingNumbers': 1},
    });
    final map = read as Map;
    expect(map['x'], isA<int>());
    expect(map['width'], isA<int>());
    expect(map['alpha'], isA<double>());
    expect(map.containsKey('nothing'), isTrue);
  });

  test('a numbered key is not spelled out; one nothing numbers is spelled out once', () {
    final encoded = TaggedValues.encode(values);
    final text = latin1.decode(encoded);
    expect(text, isNot(contains('doit')));
    expect(text, isNot(contains('width')));
    expect('aKeyNothingNumbers'.allMatches(text).length, 1);
  });

  test('bytes read back as bytes', () {
    final bytes = Uint8List.fromList([0, 1, 255]);
    expect(TaggedValues.decode(TaggedValues.encode({'pixels': bytes})), {'pixels': bytes});
  });

  test('a payload opens with a mark UTF-8 text cannot start with', () {
    expect(TaggedValues.isTagged(TaggedValues.encode('x')), isTrue);
    expect(TaggedValues.isTagged(utf8.encode('{"a":1}')), isFalse);
    expect(TaggedValues.isTagged(utf8.encode('plain text')), isFalse);
  });
}
