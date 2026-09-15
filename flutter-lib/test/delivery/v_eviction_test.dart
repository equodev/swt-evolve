// Forgetting a widget.
//
// A value outlives the tree that renders it, which is the whole point of the registry - a hidden
// subtree has to keep receiving. The cost is that nothing is forgotten by going off screen, so
// something else has to say when a widget is actually gone.
//
// Java never says it. A disposed widget is simply not carried any more: it drops out of its
// parent's child list, its table's item list, its shell's dialog list. So a departure is read out
// of what arrives, and a widget is forgotten when the last thing describing it lets go.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';

VLabel _label(int id, {required int seq}) => VLabel()
  ..id = id
  ..seq = seq
  ..style = SWT.NONE
  ..text = 'label $id';

VComposite _composite(int id, {required int seq, List<VControl> children = const []}) =>
    VComposite()
      ..id = id
      ..seq = seq
      ..style = SWT.NONE
      ..children = [...children];

void main() {
  late VRegistry registry;

  setUp(() => registry = VRegistry());

  test('a child that stops being carried is forgotten', () {
    registry.register(_composite(1, seq: 10, children: [_label(9, seq: 10)]));
    expect(registry.size, 2);

    registry.apply('Composite/1', _composite(1, seq: 20).toJson()..['_s'] = 20);

    expect(registry.valueOn('Label/9'), isNull);
    expect(registry.size, 1,
        reason: 'nothing describes that widget any more, and holding its state forever is the '
            'price of a value that outlives what renders it');
  });

  test('what the departed widget was holding goes with it', () {
    registry.register(_composite(1, seq: 10, children: [
      _composite(2, seq: 10, children: [_label(9, seq: 10)])
    ]));
    expect(registry.size, 3);

    registry.apply('Composite/1', _composite(1, seq: 20).toJson()..['_s'] = 20);

    expect(registry.size, 1,
        reason: 'a disposed composite takes its subtree with it, and only the root of that subtree '
            'is ever named in what arrives');
  });

  test('a widget two places refer to survives one of them letting go', () {
    final shared = _label(9, seq: 10);
    registry.register(_composite(1, seq: 10, children: [shared]));
    registry.register(_composite(2, seq: 10, children: [shared]));
    expect(registry.size, 3);

    registry.apply('Composite/1', _composite(1, seq: 20).toJson()..['_s'] = 20);

    expect(registry.valueOn('Label/9'), isNotNull,
        reason: 'a tab body is both a child of its folder and the control of its item; one of them '
            'dropping it is not a disposal');
    expect(registry.size, 3, reason: 'both composites and the widget they share');
  });

  test('a widget that moved on is not forgotten by the parent it left', () {
    // The reparent, in the order that makes it dangerous: the new parent is described first, and
    // the old parent's update - written earlier, arriving later - still carries the widget gone.
    registry.register(_composite(1, seq: 10, children: [_label(9, seq: 10)]));

    registry.register(_composite(2, seq: 30, children: [_label(9, seq: 30)]));
    registry.apply('Composite/1', _composite(1, seq: 20).toJson()..['_s'] = 20);

    expect(registry.valueOn('Label/9'), isNotNull,
        reason: 'the widget was described after the frame that stopped carrying it was written, so '
            'that frame is the old parent catching up rather than news of a disposal');
  });

  test('a widget that moved on is forgotten by neither ordering', () {
    // The same reparent the other way round: the old parent lets go first, and the new parent's
    // description arrives after. Forgetting in between is allowed - what may not happen is the
    // widget ending up gone once both have been heard.
    registry.register(_composite(1, seq: 10, children: [_label(9, seq: 10)]));

    registry.apply('Composite/1', _composite(1, seq: 20).toJson()..['_s'] = 20);
    registry.register(_composite(2, seq: 30, children: [_label(9, seq: 30)]));

    expect(registry.valueOn('Label/9'), isNotNull);
    expect(registry.size, 3);
  });

  test('re-describing the same tree forgets nothing and counts nothing twice', () {
    registry.register(_composite(1, seq: 10, children: [_label(9, seq: 10)]));

    for (var seq = 20; seq <= 60; seq += 10) {
      registry.apply(
          'Composite/1',
          _composite(1, seq: seq, children: [_label(9, seq: seq)]).toJson()
            ..['_s'] = seq
            ..['children'] = [
              (_label(9, seq: seq).toJson()..['_s'] = seq)
            ]);
    }

    expect(registry.size, 2,
        reason: 'a tree described over and over is one tree; growing here would be the leak this '
            'exists to prevent');
    expect(registry.valueOn('Label/9'), isNotNull);

    registry.apply('Composite/1', _composite(1, seq: 70).toJson()..['_s'] = 70);
    expect(registry.size, 1,
        reason: 'and the count has to still be right after all those repeats, or the widget is '
            'held by a reference nothing can let go of');
  });

  // The display is the top of the tree and the only thing holding the shells, and it is a device
  // rather than a widget - so it has no value here for its references to be read out of. It says
  // what it is carrying instead. Without that, every shell ever opened would be kept for the life
  // of the session, along with everything under it.
  group('a holder that is not a widget', () {
    test('what it stops carrying is forgotten', () {
      registry.register(_composite(1, seq: 10, children: [_label(9, seq: 10)]));
      registry.holds('Display/1', [registry.valueOn('Composite/1')!]);
      expect(registry.size, 2);

      registry.holds('Display/1', const []);

      expect(registry.size, 0,
          reason: 'a shell that closes takes its subtree with it; nothing else could have said so');
    });

    test('what it keeps carrying is kept', () {
      registry.register(_composite(1, seq: 10, children: [_label(9, seq: 10)]));
      registry.register(_composite(2, seq: 10));
      registry.holds('Display/1', [
        registry.valueOn('Composite/1')!,
        registry.valueOn('Composite/2')!,
      ]);

      registry.holds('Display/1', [registry.valueOn('Composite/2')!]);

      expect(registry.valueOn('Composite/2'), isNotNull);
      expect(registry.valueOn('Composite/1'), isNull);
      expect(registry.size, 1);
    });

    test('it can name a widget before that widget arrives', () {
      // The display lists a shell in the same update that first describes it, and the shell only
      // reaches the registry when something mounts it.
      registry.holds('Display/1', [_composite(1, seq: 10)]);
      registry.register(_composite(1, seq: 10, children: [_label(9, seq: 10)]));

      registry.holds('Display/1', const []);

      expect(registry.size, 0,
          reason: 'the interest was recorded before the widget existed and still has to be the '
              'thing that lets go of it');
    });
  });

  test('a widget nothing referred to in the first place is not swept up', () {
    // A shell is registered by the widget that mounts it and named by nothing else. Forgetting it
    // for want of a referrer would empty the screen.
    registry.register(_composite(1, seq: 10, children: [_label(9, seq: 10)]));

    registry.apply(
        'Composite/1',
        _composite(1, seq: 20, children: [_label(9, seq: 20)]).toJson()
          ..['_s'] = 20
          ..['children'] = [
            (_label(9, seq: 20).toJson()..['_s'] = 20)
          ]);

    expect(registry.valueOn('Composite/1'), isNotNull);
    expect(registry.size, 2);
  });
}
