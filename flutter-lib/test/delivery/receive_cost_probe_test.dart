// Where the time goes on the receiving end of a layout pass.
//
// Not a regression test - a measurement, printed rather than asserted, for deciding whether the
// shape of what we send is the thing worth changing.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'support/deliver.dart';
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

int _timeMicros(void Function() body) {
  final sw = Stopwatch()..start();
  body();
  sw.stop();
  return sw.elapsedMicroseconds;
}

void main() {
  test('MEASURE: applying a layout pass, per-child changes vs one parent change', () {
    final registry = VRegistry();
    final kids = [for (var i = 0; i < _children; i++) _leaf(1000 + i, i * 20, 1)];
    applyWhole(_parent(kids, 1), registry: registry);

    // One change per child, as a layout pass sends them.
    final applied = _timeMicros(() {
      for (var i = 0; i < _children; i++) {
        applyChange(_leaf(1000 + i, i * 20 + 1, 100 + i),
            keys: [WireKey.k_Control_bounds], base: 1, registry: registry);
      }
    });
    print('MEASURE apply 500 per-child changes: ${applied}us '
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

    // What one change costs on a value that holds a lot: the parent with its 500 children.
    final oneBigChange = _timeMicros(() {
      applyChange(
          _parent(kids, 950)
            ..bounds = (VRectangle()
              ..width = 401
              ..height = 10000),
          keys: [WireKey.k_Control_bounds],
          base: 1,
          registry: registry);
    });
    print('MEASURE one change to the 500-child parent: ${oneBigChange}us');
  });
}
