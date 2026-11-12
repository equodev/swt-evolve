// A style index travels as runs: each start as the step from the end of the run before, which takes
// a byte where the offset itself takes three, and none when the run touches the one before; a run's
// name and length as one number. A change to an index, and anything not made of whole runs, travels
// as the ints it is.

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/widget_wire.dart';

List<int>? roundTrip(List<int> v, {bool asRuns = true}) {
  final w = WidgetWireWriter();
  asRuns ? w.runs(v) : w.ints(v);
  return WidgetWireReader(Uint8List.fromList(w.written)).runs();
}

int size(List<int> v, {required bool asRuns}) {
  final w = WidgetWireWriter();
  asRuns ? w.runs(v) : w.ints(v);
  return w.size;
}

/// A palette name, then a run of style `i % 7` every 12 characters of a long document.
List<int> index(int runs) => [
      123456,
      for (var i = 0; i < runs; i++) ...[i % 7, 200000 + i * 12, 5 + i % 4],
    ];

/// A palette name, then runs that each start where the one before ends, as a highlighted line's do.
List<int> touching(int runs) {
  final v = [123456];
  for (var i = 0, start = 0; i < runs; i++) {
    v.addAll([i % 5 - 1, start, 1 + i % 9]);
    start += 1 + i % 9;
  }
  return v;
}

/// A change's header, then [runs] touching runs it inserts deep in a document.
List<int> change(int runs) {
  final t = touching(runs);
  return [
    -2147483648, 987654, 3, 123456, 64, 4425,
    for (var i = 0; i < runs; i++) ...[(i ~/ 3) % 4, 200000 + t[2 + 3 * i], t[3 + 3 * i]],
  ];
}

void main() {
  test('a whole index comes back as it was', () {
    expect(roundTrip(index(500)), index(500));
  });

  test('runs that touch come back as they were', () {
    expect(roundTrip(touching(500)), touching(500));
  });

  test('a run that touches the one before takes two bytes', () {
    expect(size(touching(1000), asRuns: true) - size(const [123456], asRuns: true),
        lessThanOrEqualTo(2 * 1000 + 2));
  });

  test('a short run that touches the one before takes one byte', () {
    final v = touching(1000);
    for (var i = 1; i < v.length; i += 3) {
      v[i] = (i ~/ 3) % 4;
    }
    expect(roundTrip(v), v);
    expect(size(v, asRuns: true) - size(const [123456], asRuns: true), lessThanOrEqualTo(1000 + 2));
  });

  test('the largest packed name and length come back as they were', () {
    const v = [7, 65535, 0, 2147483646, 0, 2147483646, 1];
    expect(roundTrip(v), v);
  });

  test('names past what packs come back as they were', () {
    const v = [7, 65536, 0, 4, 1, 4, 2];
    expect(roundTrip(v), v);
  });

  test('extreme names and steps come back as they were', () {
    const v = [7, 2147483647, 0, 1, -2147483648, 5, 2, -1, 5, 3];
    expect(roundTrip(v), v);
  });

  test('a change to an index comes back as it was', () {
    const change = [-2147483648, 987654, 3, 123456, 4, 2, 1, 200012, 6, 2];
    expect(roundTrip(change), change);
  });

  test('a change inserting whole runs comes back as it was', () {
    expect(roundTrip(change(500)), change(500));
  });

  test('a change inserting whole runs takes far fewer bytes than its offsets', () {
    final v = change(1000);
    expect(size(v, asRuns: true), lessThan(size(v, asRuns: false) / 3));
  });

  test('a list not made of whole runs comes back as it was', () {
    expect(roundTrip([5, 1, 2]), [5, 1, 2]);
    expect(roundTrip(const []), const <int>[]);
  });

  test('a long document index takes far fewer bytes than its offsets', () {
    final v = index(10000);
    expect(size(v, asRuns: true), lessThan(size(v, asRuns: false) * 0.65));
  });
}
