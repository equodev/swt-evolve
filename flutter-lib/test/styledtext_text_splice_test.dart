// Guards that style runs, carets and selections move with a spliced edit, as StyledText's renderer
// moves its ranges, until the frame describing the widget after the edit arrives.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

/// A palette index: its name, then name, start and length per run.
List<int> index(List<List<int>> runs) => [99, for (final r in runs) ...r];

void main() {
  group('style runs', () {
    test('typing moves every run after the edit and leaves those before it', () {
      final before = index([
        [0, 0, 5],
        [1, 20, 4],
      ]);
      expect(spliceStyleIndex(before, 10, 0, 1), index([
        [0, 0, 5],
        [1, 21, 4],
      ]));
    });

    test('typing inside a run grows it', () {
      expect(spliceStyleIndex(index([
        [0, 10, 5]
      ]), 12, 0, 1), index([
        [0, 10, 6]
      ]));
    });

    test('typing just past a run does not take its style, and just before it moves it', () {
      final runs = index([
        [0, 10, 5]
      ]);
      expect(spliceStyleIndex(runs, 15, 0, 1), runs, reason: 'the typed character is after the run');
      expect(spliceStyleIndex(runs, 10, 0, 1), index([
        [0, 11, 5]
      ]));
    });

    test('deleting shrinks a run, and a run it swallows is dropped', () {
      expect(spliceStyleIndex(index([
        [0, 10, 5]
      ]), 12, 1, 0), index([
        [0, 10, 4]
      ]));
      expect(spliceStyleIndex(index([
        [0, 10, 5],
        [1, 30, 2]
      ]), 8, 10, 0), index([
        [1, 20, 2]
      ]));
    });

    test('the palette name stays first', () {
      expect(spliceStyleIndex(index([
        [3, 40, 2]
      ]), 0, 0, 5).first, 99);
    });
  });

  group('carets and selections', () {
    test('a caret at the edit ends up after what was typed', () {
      expect(spliceOffset(10, 10, 0, 1), 11);
    });

    test('a backspace leaves the caret where the character was', () {
      expect(spliceOffset(10, 9, 1, 0), 9);
    });

    test('a caret before the edit stays', () {
      expect(spliceOffset(4, 10, 0, 3), 4);
    });

    test('a selection after the edit moves, and an empty one moves as a caret', () {
      expect(spliceRangePairs([20, 5, 10, 0], 10, 0, 2), [22, 5, 12, 0]);
    });
  });
}
