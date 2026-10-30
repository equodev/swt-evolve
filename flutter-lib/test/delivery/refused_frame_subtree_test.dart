// What happens to the widgets a change described when the change itself cannot be applied.
//
// A change names the state it was computed from, and one that does not match what is held cannot
// be merged - the widget is asked for again instead. But the frame still carries complete records
// of the widgets the change named, and those are the only descriptions they will ever get: the
// sender counts a widget it wrote as delivered and names it from then on. Dropping them would
// orphan a whole subtree for good, and every later update to any of it is a recovery that cannot
// recover.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';

import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'support/deliver.dart';

const String _parent = 'Composite/7';
const String _carried = 'Label/101';

VLabel _label(int id, String text, int seq) => VLabel()
  ..id = id
  ..seq = seq
  ..style = SWT.NONE
  ..text = text;

void main() {
  late VRegistry registry;

  setUp(() {
    deliveryGate.reset();
    registry = VRegistry();
    registry.register(VComposite()
      ..id = 7
      ..seq = 2
      ..style = SWT.NONE
      ..children = []);
    deliveryGate.applied(_parent, 2);
  });
  tearDown(() => registry.clear());

  /// A change to the parent computed from a state this client does not hold (it holds 2), carrying
  /// two widgets nothing has ever described.
  void refusedChangeCarryingChildren() => applyChange(
      VComposite()
        ..id = 7
        ..seq = 40
        ..style = SWT.NONE
        ..children = [_label(101, 'one', 41), _label(102, 'two', 42)],
      keys: [WireKey.k_Composite_children],
      base: 39,
      registry: registry);

  test('a widget described beside a refused change is still held', () async {
    refusedChangeCarryingChildren();

    expect(deliveryGate.recoveries, 1, reason: 'the parent itself is asked for again');
    expect((registry.valueOn(_carried) as VLabel?)?.text, 'one',
        reason: 'the description it carried is the only one this widget will ever be sent');
    expect((registry.valueOn('Label/102') as VLabel?)?.text, 'two');
    expect(deliveryGate.heldSeq(_carried), 41,
        reason: 'and the stamp with it, or the next update to it fits nothing either');
  });

  test('and its own next update merges instead of recovering again', () async {
    refusedChangeCarryingChildren();

    applyChange(_label(101, 'one changed', 43),
        keys: [WireKey.k_Label_text], base: 41, registry: registry);

    expect((registry.valueOn(_carried) as VLabel).text, 'one changed');
    expect(deliveryGate.recoveries, 1,
        reason: 'one refused change must not turn every widget under it into a recovery of its '
            'own - that is the cascade this guards against');
  });

  test('only the change itself is asked for again, not what it carried', () async {
    refusedChangeCarryingChildren();

    expect(deliveryGate.recoveries, 1,
        reason: 'both widgets the change carried would otherwise have been orphaned, and each one '
            'asked for separately');
    expect(deliveryGate.heldSeq('Label/102'), 42,
        reason: 'held on its own stamp, so it is a widget this client has rather than one it '
            'still has to ask about');
  });
}
