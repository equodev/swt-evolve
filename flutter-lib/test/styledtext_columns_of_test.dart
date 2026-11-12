// A line's width before it is laid out comes from its columns, tabs reaching the next stop.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

/// Character by character, as the columns are defined.
int byCharacter(String line, int tabColumns) {
  int columns = 0;
  for (int k = 0; k < line.length; k++) {
    columns = line.codeUnitAt(k) == 0x09 ? (columns ~/ tabColumns + 1) * tabColumns : columns + 1;
  }
  return columns;
}

void main() {
  const lines = [
    '',
    'plain text',
    '\t',
    '\t\t',
    '\tindented',
    '  \tmixed',
    'a\tb\tc',
    'abc\t',
    'abcd\tstop at a full column',
    '\t\t\tx\t\ty',
    '        eight spaces',
  ];

  for (final tabColumns in [1, 2, 4, 8]) {
    test('the columns of each line, tabs every $tabColumns, are the ones counted by character', () {
      for (final line in lines) {
        expect(columnsOf(line, tabColumns), byCharacter(line, tabColumns), reason: '"$line"');
      }
    });
  }
}
