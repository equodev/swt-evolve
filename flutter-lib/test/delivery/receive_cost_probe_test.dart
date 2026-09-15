// Where the time goes on the receiving end of a layout pass.
//
// Not a regression test - a measurement, printed rather than asserted, for deciding whether the
// shape of what we send is the thing worth changing.

import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

const int _children = 500;

VLabel _leaf(int id, int y, int seq) => VLabel()
  ..id = id
  ..seq = seq
  ..style = SWT.NONE
  ..text = 'row $id'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = y
    ..width = 100
    ..height = 18);

VComposite _parent(List<VControl> kids, int seq) => VComposite()
  ..id = 1
  ..seq = seq
  ..style = SWT.NONE
  ..children = kids
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 10000);

Map<String, dynamic> _boundsUpdate(int id, int y, int seq, int base) => {
      'swt': 'Label',
      'id': id,
      '_s': seq,
      '_b': base,
      '_d': ['bounds'],
      'bounds': {'x': 0, 'y': y, 'width': 100, 'height': 18},
    };

int _timeMicros(void Function() body) {
  final sw = Stopwatch()..start();
  body();
  sw.stop();
  return sw.elapsedMicroseconds;
}

void main() {
  test('MEASURE: applying a layout pass, per-child updates vs one parent update', () {
    final registry = VRegistry();
    final kids = [for (var i = 0; i < _children; i++) _leaf(1000 + i, i * 20, 1)];
    registry.register(_parent(kids, 1));

    // What the wire carries today: one batched message holding one update per child.
    final perChild = [
      for (var i = 0; i < _children; i++) _boundsUpdate(1000 + i, i * 20 + 1, 100 + i, 1),
    ];
    final batchBody = jsonEncode([
      for (final f in perChild) ['Label/${f['id']}', f],
    ]);
    print('MEASURE per-child batch bytes: ${utf8.encode(batchBody).length}');

    // The same information addressed to the parent instead, each child named rather than
    // re-described - the shape a nested update would take.
    final nestedBody = jsonEncode({
      'swt': 'Composite',
      'id': 1,
      '_s': 900,
      '_b': 1,
      '_d': ['children'],
      'children': [
        for (var i = 0; i < _children; i++)
          {
            'swt': 'Label',
            'id': 1000 + i,
            '_s': 100 + i,
            '_b': 1,
            '_d': ['bounds'],
            'bounds': {'x': 0, 'y': i * 20 + 1, 'width': 100, 'height': 18},
          }
      ],
    });
    print('MEASURE one-parent nested bytes: ${utf8.encode(nestedBody).length}');

    // Decoding is one pass over the bytes either way; the interesting cost is what applying does.
    final decodeBatch = _timeMicros(() => jsonDecode(batchBody));
    final decodeNested = _timeMicros(() => jsonDecode(nestedBody));
    print('MEASURE decode batch=${decodeBatch}us nested=${decodeNested}us');

    // Applying the per-child updates, which is what the client does now.
    final applied = _timeMicros(() {
      for (final f in perChild) {
        registry.apply('Label/${f['id']}', f);
      }
    });
    print('MEASURE apply 500 per-child updates: ${applied}us '
        '(${(applied / _children).toStringAsFixed(1)}us each)');

    // The same 500 properties written straight onto the held values, with none of the delivery
    // machinery - the floor any scheme has to beat.
    final floor = _timeMicros(() {
      for (var i = 0; i < _children; i++) {
        final held = registry.valueOn('Label/${1000 + i}') as VLabel;
        held.bounds!.y = i * 20 + 2;
      }
    });
    print('MEASURE write 500 bounds directly: ${floor}us');

    // What one merge costs on a value that holds a lot: the parent with its 500 children.
    final oneBigMerge = _timeMicros(() {
      registry.apply('Composite/1', {
        'swt': 'Composite',
        'id': 1,
        '_s': 950,
        '_b': 1,
        '_d': ['bounds'],
        'bounds': {'x': 0, 'y': 0, 'width': 401, 'height': 10000},
      });
    });
    print('MEASURE one update to the 500-child parent: ${oneBigMerge}us');
  });
}
