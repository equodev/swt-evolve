import 'package:flutter_test/flutter_test.dart';

import '../tool/font_metrics_core.dart';

Map<String, dynamic> _face(
  Map<String, int>? kerning, {
  double avgWidth = 5.5,
}) => {
  'ascent': {'m': 0.9},
  'descent': {'m': 0.2},
  'height': {'m': 1.1},
  'avgWidth': avgWidth,
  'glyphs': {for (var cp = 32; cp <= 126; cp++) '$cp': avgWidth},
  if (kerning != null) 'kerning': kerning,
};

/// The value of the string constant `<method>()` returns in [java], its literal pieces joined and
/// unescaped as javac reads them.
String _kerningConstant(String java, String method) {
  final body = RegExp(
    'private static String $method\\(\\) \\{\\n([\\s\\S]*?)\\n    \\}',
  ).firstMatch(java)!.group(1)!;
  final pieces = RegExp(
    r'"((?:[^"\\]|\\.)*)"',
  ).allMatches(body).map((m) => m.group(1)!);
  return pieces.join().replaceAllMapped(
    RegExp(r'\\u([0-9a-fA-F]{4})|\\(.)'),
    (m) => m.group(1) != null
        ? String.fromCharCode(int.parse(m.group(1)!, radix: 16))
        : m.group(2)!,
  );
}

void main() {
  const special = {'"A': -120, r'\t': 35, '#1': -7, ',.': 12, 'AV': -1953};

  test(
    'a kerning table with quotes, backslashes, # and commas reads back as its sorted pairs',
    () {
      final java = generateJava({
        'Face-0-3': _face(Map<String, int>.of(special)),
      });

      expect(
        _kerningConstant(java, 'Face_0_3_kerning'),
        '"A-120,#1-7,,.12,AV-1953,\\t35,',
      );
      expect(
        java,
        contains(
          'new Metrics(0.9000, 0.2000, 1.1000, 5.5000, Face_0_3(), Face_0_3_kerning())',
        ),
      );
    },
  );

  test('a # in a kerning table is written as its unicode escape', () {
    final java = generateJava({
      'Face-0-3': _face({'#1': -7}),
    });

    expect(
      java,
      contains(
        'return "\\u'
        '00231-7,";',
      ),
    );
    expect(java.contains('"#'), isFalse);
  });

  test(
    'a face with the same kerning as an earlier one reads it from that face',
    () {
      final java = generateJava({
        'Face-0-3': _face(Map<String, int>.of(special)),
        'Face Bold-0-6': _face(Map<String, int>.of(special), avgWidth: 6.0),
      });

      expect(
        java,
        contains('Face_Bold_0_6(), DATA.get("Face-0-3").kerning())'),
      );
      expect('private static String '.allMatches(java).length, 1);
      expect(java.contains('Face_Bold_0_6_kerning'), isFalse);
    },
  );

  test('a face without kerning gets no kerning argument or method', () {
    final java = generateJava({'Mono-0-3': _face(null)});

    expect(
      java,
      contains('new Metrics(0.9000, 0.2000, 1.1000, 5.5000, Mono_0_3())'),
    );
    expect(java.contains('_kerning'), isFalse);
  });

  test(
    'a long kerning table is split over several literals that join back to the table',
    () {
      final kerning = <String, int>{
        for (var a = 65; a < 75; a++)
          for (var b = 97; b < 107; b++) String.fromCharCodes([a, b]): -(a * b),
      };
      final java = generateJava({'Face-0-3': _face(kerning)});
      final expected = (kerning.keys.toList()..sort())
          .map((p) => '$p${kerning[p]},')
          .join();

      expect(
        RegExp(r'^            \+ "', multiLine: true).allMatches(java).length,
        greaterThan(1),
      );
      expect(_kerningConstant(java, 'Face_0_3_kerning'), expected);
    },
  );

  test(
    'a kerning table longer than a Java string constant fails the generation',
    () {
      Map<String, int> table(int adjustment) => {
        for (var a = 32; a <= 126; a++)
          for (var b = 32; b <= 126; b++)
            String.fromCharCodes([a, b]): adjustment,
      };

      // 9025 pairs: 5 characters each with -1 (45125), 8 each with -1000 (72200, over 65535).
      expect(
        () => generateJava({'Face-0-3': _face(table(-1))}),
        returnsNormally,
      );
      expect(
        () => generateJava({'Face-0-3': _face(table(-1000))}),
        throwsStateError,
      );
    },
  );
}
