// The geometry table goes to Java on opening every editor and again whenever its layout changes, a
// row per visual line. Rows of one height in a monospaced editor are the common case: each is
// written in a few bytes, as steps from the row before, and a pixel value that is not a whole
// hundredth still travels exactly, as a double.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

int sizeOf(List<GeometryRow> rows) {
  final out = WidgetWireWriter();
  final writer = GeometryRowWriter(out);
  for (final row in rows) {
    writer.write(row);
  }
  return out.size;
}

void main() {
  test('rows of one height take a few bytes each', () {
    final rows = <GeometryRow>[];
    var offset = 0;
    for (var line = 0; line < 1000; line++) {
      final length = 30 + line % 50;
      // Rounded to hundredths, as the table is.
      final width = (length * 7.83 * 100).roundToDouble() / 100;
      rows.add(GeometryRow(line, offset, offset + length, 0, line * 17.0, width, 17, null, null));
      offset += length + 1;
    }

    expect(sizeOf(rows), lessThan(1000 * 12));
  });

  test('a pixel value that is no whole hundredth is written as the double it is', () {
    GeometryRow row(double x) => GeometryRow(0, 0, 10, x, 0, 50, 17, null, null);

    expect(sizeOf([row(1.005)]) - sizeOf([row(1.25)]), greaterThanOrEqualTo(8 - 3));
  });
}
