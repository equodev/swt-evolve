// A widget's key is what the registry and the delivery gate hold its state under, reached from a
// frame's numbers on one side and from a channel string on the other: both must give the same key,
// and two widgets never the same one, on the web as on the VM.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/widget_key.dart';

void main() {
  test("a channel's key is the key of the numbers it spells out", () {
    final label = WidgetKey.typeOf('Label');
    for (final id in [0, 1, 800001, 2147483647, -1, -2147483648]) {
      expect(WidgetKey.ofChannel('Label/$id'), WidgetKey.of(label, id));
    }
  });

  test("a control and its GC overlay share an id but not a key", () {
    expect(WidgetKey.ofChannel('GC/41'), isNot(WidgetKey.ofChannel('Canvas/41')));
  });

  test('neighbouring ids and types do not collide', () {
    final keys = <int>{
      for (final type in ['Label', 'Button', 'GC', 'TableItem'])
        for (var id = -3; id <= 3; id++) WidgetKey.ofChannel('$type/$id'),
      WidgetKey.ofChannel('Label/2147483647'),
      WidgetKey.ofChannel('Label/-2147483648'),
    };
    expect(keys, hasLength(4 * 7 + 2));
  });

  test('a type is named back from its number, and one the wire has no number for is refused', () {
    expect(WidgetKey.typeName(WidgetKey.typeOf('Label')), 'Label');
    expect(() => WidgetKey.typeOf('ProbeOnlyType'), throwsArgumentError);
    expect(() => WidgetKey.ofChannel('ProbeOnlyType/7'), throwsArgumentError);
  });

  test('a channel naming no widget has a key of its own, above every widget', () {
    final display = WidgetKey.ofChannel('Display');
    expect(WidgetKey.ofChannel('Display'), display);
    expect(WidgetKey.ofChannel('Display/popups'), isNot(display));
    expect(display, greaterThan(WidgetKey.ofChannel('Label/2147483647')));
  });
}
