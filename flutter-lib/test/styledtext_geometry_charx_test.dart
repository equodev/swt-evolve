// Per-character x is compacted for the wire, and must stay exactly as accurate as the offsets and
// points Java derives from it.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

void main() {
  test('a row whose characters advance evenly travels as an origin and a step', () {
    final raw = [10.0, 17.224, 24.448, 31.672, 38.896];

    final entry = TextShape.charXEntry(raw);

    expect(entry.containsKey('cx'), isFalse, reason: 'not one number per character');
    final cxu = entry['cxu'] as List;
    expect(cxu, hasLength(2));
    expect(cxu[0], closeTo(10.0, 1e-9), reason: 'the origin');
    expect(cxu[1], closeTo(7.224, 1e-9), reason: 'the step, at full precision');

    // What Java reconstructs from it has to be the row that was measured.
    for (var i = 0; i < raw.length; i++) {
      expect(cxu[0] + i * cxu[1], closeTo(raw[i], 0.005));
    }
  });

  test('a proportional row keeps its explicit positions', () {
    final raw = [10.0, 17.2, 21.9, 31.4, 38.0];

    final entry = TextShape.charXEntry(raw);

    expect(entry.containsKey('cxu'), isFalse);
    expect(entry['cx'], hasLength(raw.length));
  });

  test('explicit positions are rounded to a hundredth of a pixel, and no further', () {
    final entry = TextShape.charXEntry([0.0, 41.343999999999994, 60.1, 123.45600000000002]);

    final cx = (entry['cx'] as List).cast<double>();
    expect(cx[1], closeTo(41.34, 1e-9));
    expect(cx[3], closeTo(123.46, 1e-9));
    for (final v in cx) {
      expect((v * 100 - (v * 100).roundToDouble()).abs(), lessThan(1e-6),
          reason: 'a hundredth of a pixel is what Java can use; more is bytes nobody reads');
    }
  });

  test('a row too short to have a step keeps its positions', () {
    expect(TextShape.charXEntry([4.0, 11.2]).containsKey('cx'), isTrue);
  });
}
