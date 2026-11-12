// A list of objects carries each run of nulls as one marker and its length, which Java's generated
// writers put where a presence byte would be; a lone null keeps its single 0.

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/styledtextrenderer.dart';

List<VLineInfo?>? read(List<int> bytes) => WidgetWireReader(Uint8List.fromList(bytes)).objects(VLineInfo.new);

void main() {
  test('runs of nulls come back in place', () {
    // Five entries: an object, a run of three nulls, an object.
    final list = read([6, 1, 0, WidgetWireReader.nullRun, 3, 1, 0])!;
    expect(list.map((e) => e != null), [true, false, false, false, true]);
  });

  test('lone nulls and objects written one by one come back as they were', () {
    final list = read([4, 0, 1, 0, 0])!;
    expect(list.map((e) => e != null), [false, true, false]);
  });

  test('a long list with one object set', () {
    // 6,456 lines, line 21 set: a run of 21, the object, a run of 6,434.
    final list = read([0xB9, 0x32, WidgetWireReader.nullRun, 21, 1, 0, WidgetWireReader.nullRun, 0xA2, 0x32])!;
    expect(list.length, 6456);
    expect([for (var i = 0; i < list.length; i++) if (list[i] != null) i], [21]);
  });

  test('none is null', () {
    expect(read([0]), isNull);
  });
}
