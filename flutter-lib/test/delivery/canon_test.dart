// The canonicalizer is an oracle: every convergence probe in the delivery suite reports what it
// reports because this code said two states were the same. So it is tested against the shared case
// file rather than against itself, and its failure direction is checked explicitly - it must be
// willing to call things different, or a green run downstream means nothing.

import 'dart:convert';
import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

import 'support/canon.dart';

Map<String, dynamic> _cases() {
  final file = File('test/delivery/canon_cases.json');
  return json.decode(file.readAsStringSync()) as Map<String, dynamic>;
}

void main() {
  final cases = _cases();

  group('shared contract', () {
    for (final entry in (cases['equal'] as List)) {
      final c = entry as Map<String, dynamic>;
      test('equal: ${c['why']}', () {
        expect(canonJson(c['a']), canonJson(c['b']),
            reason: 'these two payloads carry the same state and must canonicalise alike');
      });
    }

    for (final entry in (cases['differ'] as List)) {
      final c = entry as Map<String, dynamic>;
      test('differ: ${c['why']}', () {
        expect(canonJson(c['a']), isNot(canonJson(c['b'])),
            reason: 'these two payloads carry different state; collapsing them would hide a real '
                'divergence from every probe that relies on this comparison');
      });
    }
  });

  group('properties of the reduction', () {
    test('is idempotent', () {
      final once = canon({'swt': 'Label', 'id': 1, '_s': 3, 'text': 'x', 'enabled': false});
      expect(canon(once), once);
    });

    test('does not mutate its input', () {
      final input = {'swt': 'Label', 'id': 1, '_s': 3, 'children': <Object>[]};
      final before = json.encode(input);
      canon(input);
      expect(json.encode(input), before);
    });

    test('recurses to arbitrary depth', () {
      Object nest(int depth) =>
          depth == 0 ? {'swt': 'Label', 'id': 1, '_s': 9} : {'swt': 'Composite', 'id': depth, '_s': depth, 'children': [nest(depth - 1)]};
      expect(canonJson(nest(20)).contains('_s'), isFalse,
          reason: 'a write stamp buried 20 levels down is still not state');
    });

    // The bytes, not just the verdict. Java's Canon asserts these exact same strings, so an
    // end-to-end probe can compare Java's view of a widget against the client's directly. If either
    // side changed its spacing or escaping that comparison would fail on formatting rather than on
    // state, and the failure would read as a delivery bug.
    test('emits the agreed byte format', () {
      expect(canonJson({'text': 'x', 'id': 1, 'swt': 'Label', '_s': 4, 'enabled': false}),
          '{"id":1,"swt":"Label","text":"x"}');
      expect(canonJson({'a': 'q"q', 'b': 'tab\there'}), r'{"a":"q\"q","b":"tab\there"}');
      expect(canonJson({'n': 3.0, 'm': 2.5}), '{"m":2.5,"n":3}');
    });

    test('a deeply buried change survives the reduction', () {
      Object nest(String leaf) => {
            'swt': 'Composite',
            'id': 1,
            'children': [
              {'swt': 'Composite', 'id': 2, 'children': [
                {'swt': 'Label', 'id': 3, 'text': leaf}
              ]}
            ]
          };
      expect(canonJson(nest('a')), isNot(canonJson(nest('b'))),
          reason: 'the reduction must not flatten away a nested difference');
    });
  });
}
