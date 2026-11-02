// A change names the property numbers its frame carried, and is asked about them by number. It
// keeps the first few inline and the rest beside them, so a question has to see past the inline
// ones as well.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

VChange _changeOf(List<int> keys) {
  final collected = WireKeys();
  for (final key in keys) {
    collected.add(key);
  }
  return VChange.ofKeys(collected);
}

void main() {
  for (final key in [WireKey.k_Control_bounds, WireKey.k_Table_items, WireKey.k_Tree_items]) {
    test('a change naming $key touches it, inline or past the inline four', () {
      expect(_changeOf([key]).touches(key), isTrue);
      expect(_changeOf([1, 2, 3, 4, key]).touches(key), isTrue);
      expect(_changeOf([1, 2, 3, 4]).touches(key), isFalse);
    });
  }

  test("a control's or a caret's bounds count as moving the widget, and nothing else does", () {
    expect(_changeOf([WireKey.k_Control_bounds]).touchesBounds, isTrue);
    expect(_changeOf([WireKey.k_Caret_bounds]).touchesBounds, isTrue);
    expect(_changeOf([1, 2, 3, 4, WireKey.k_Control_bounds]).touchesBounds, isTrue);
    expect(_changeOf([WireKey.k_Label_text]).touchesBounds, isFalse);
  });

  test('a change names only what it carried', () {
    final change = _changeOf([WireKey.k_Label_text]);
    expect(change.touches(WireKey.k_Label_text), isTrue);
    expect(change.touches(WireKey.k_Control_bounds), isFalse);
    expect(change.isOnly({WireKey.k_Label_text}), isTrue);
    expect(change.isOnly({WireKey.k_Control_bounds}), isFalse);
  });
}
