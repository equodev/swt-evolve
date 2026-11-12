// A parent that rebuilds hands its children the widgets it made for them before, so Flutter does
// not rebuild a child whose value did not change: a child takes its state from the registry.

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/widgets.dart';

void main() {
  test('the same value gets the same widget', () {
    final label = VLabel()
      ..swt = 'Label'
      ..id = 40101;
    expect(identical(mapWidgetFromValue(label), mapWidgetFromValue(label)), isTrue);
  });

  test('another value, or the same one renumbered, gets a widget of its own', () {
    final table = VTable()
      ..swt = 'Table'
      ..id = 40102;
    final first = mapWidgetFromValue(table);
    final copy = VTable()
      ..swt = 'Table'
      ..id = 40102;
    expect(identical(mapWidgetFromValue(copy), first), isFalse);

    table.id = 40103;
    final renumbered = mapWidgetFromValue(table);
    expect(identical(renumbered, first), isFalse);
    expect(renumbered.key, const ValueKey(40103));
  });
}
