// The registry's own contract, before anything reads from it.
//
// What it has to guarantee: one object per widget for as long as that widget exists, an update
// reaching every reference rather than the last one to subscribe, and a value that keeps receiving
// while nothing renders it. The last of those is the one the old model could not do at all, and the
// reason a hidden subtree used to come back empty.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';

import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'support/deliver.dart';

const int _id = 42;
const String _channel = 'Label/$_id';

VLabel _label(String text, {int seq = 1}) => VLabel()
  ..id = _id
  ..seq = seq
  ..style = SWT.NONE
  ..text = text;

void main() {
  late VRegistry registry;

  /// [text] described whole, stamped [seq].
  void whole(String text, int seq) => applyWhole(_label(text, seq: seq), registry: registry);

  /// A change to [text], stamped [seq] and computed from [base].
  void partial(String text, {required int seq, required int base}) =>
      applyChange(_label(text, seq: seq), keys: [WireKey.k_Label_text], base: base, registry: registry);

  setUp(() {
    deliveryGate.reset();
    registry = VRegistry();
  });

  tearDown(() => registry.clear());

  group('identity', () {
    test('registering the same widget twice yields the same object', () async {
      final first = registry.register(_label('a'));
      final second = registry.register(_label('a'));

      expect(identical(first, second), isTrue,
          reason: 'the second arrival describes a widget already known; taking its object instead '
              'would be the two-copies bug arriving through the front door');
    });

    test('a caller hands over its object and takes back the one that counts', () async {
      registry.register(_label('original'));
      final mine = _label('built from a parent payload');

      final canonical = registry.register(mine);

      expect(identical(canonical, mine), isFalse);
      expect((canonical as VLabel).text, 'original');
    });

    test('a newer copy of a widget already held is read, not only answered', () async {
      registry.register(_label('original', seq: 10));

      // What the display does with its shells, and what any holder that carries widgets in its own
      // payload does: the copy it hands over was written later than the state held, and the sender
      // counts a widget it wrote as delivered. Taking only the identity off it drops that
      // description, and the write stamp with it.
      final canonical = registry.register(_label('carried in a holder payload', seq: 20));

      expect((canonical as VLabel).text, 'carried in a holder payload');
      expect(deliveryGate.heldSeq(_channel), 20);

      partial('and the next update fits', seq: 21, base: 20);

      expect(deliveryGate.recoveries, 0,
          reason: 'the update after it is computed from the state that was handed over, so a '
              'registry that kept up has nothing to ask for');
      expect((registry.valueOn(_channel) as VLabel).text, 'and the next update fits');
    });

    test('different widgets are kept apart', () async {
      final a = registry.register(_label('a'));
      final b = registry.register(VLabel()
        ..id = 99
        ..style = SWT.NONE
        ..text = 'b');

      expect(identical(a, b), isFalse);
      expect(registry.size, 2);
    });
  });

  group('applying records', () {
    test('a whole record changes the value everyone reads', () async {
      final held = registry.register(_label('before'));

      whole('after', 10);

      expect((registry.valueOn(_channel) as VLabel).text, 'after');
      expect(identical(registry.valueOn(_channel), held), isTrue,
          reason: 'the object outlives every frame about it: something holding this widget - a '
              "parent's child list, a layout - is holding the widget, not a description of it that "
              'was true when it took the reference');
    });

    test('a change changes only what it names', () async {
      registry.register(_label('before'));
      whole('whole', 10);

      partial('partial', seq: 11, base: 10);

      expect((registry.valueOn(_channel) as VLabel).text, 'partial');
      expect(deliveryGate.recoveries, 0);
    });

    test('a change that does not fit is refused, not guessed at', () async {
      registry.register(_label('before'));
      whole('whole', 10);

      partial('never applied', seq: 30, base: 29);

      expect((registry.valueOn(_channel) as VLabel).text, 'whole');
      expect(deliveryGate.recoveries, 1);
    });
  });

  group('listeners', () {
    test('a watcher is told when the value changes', () async {
      registry.register(_label('before'));
      var told = 0;
      registry.watch(_channel, (_) => told++);

      whole('after', 10);

      expect(told, 1);
    });

    test('every watcher is told, not just the last to arrive', () async {
      registry.register(_label('before'));
      var first = 0;
      var second = 0;
      registry.watch(_channel, (_) => first++);
      registry.watch(_channel, (_) => second++);

      whole('after', 10);

      expect([first, second], [1, 1],
          reason: 'this is the defect the registry exists for: one widget rendered in two places '
              'used to leave whichever subscribed first hearing nothing');
    });

    test('a watcher that leaves stops being told, and the value keeps updating', () async {
      registry.register(_label('before'));
      var told = 0;
      void listener(VChange change) => told++;
      registry.watch(_channel, listener);
      registry.unwatch(_channel, listener);

      whole('after', 10);

      expect(told, 0);
      expect((registry.valueOn(_channel) as VLabel).text, 'after',
          reason: 'nothing is rendering it and it still keeps up - which is what a hidden subtree '
              'needs, and what the value dying with its State could never do');
    });

    test('a watcher can leave while being told', () async {
      registry.register(_label('before'));
      late void Function(VChange) listener;
      listener = (_) => registry.unwatch(_channel, listener);
      registry.watch(_channel, listener);

      expect(() => whole('after', 10), returnsNormally,
          reason: 'a widget disposing in response to an update is ordinary, not an error');
    });
  });

  // What a delivery reports about itself. A widget that only wants to be rebuilt ignores all of
  // this; one that has to tell what actually moved - the shell, for an alpha-only animation frame -
  // has nothing else to go on once an update stops carrying the whole widget.
  group('what a delivery reports', () {
    test('a whole record claims nothing about what moved', () async {
      registry.register(_label('before'));
      VChange? seen;
      registry.watch(_channel, (change) => seen = change);

      whole('after', 10);

      expect(seen!.keys, isNull,
          reason: 'the whole widget arrived, so naming what changed would be claiming to know '
              'something the record never said');
      expect(seen!.touches(WireKey.k_Label_text), isTrue,
          reason: 'anything it carries may have moved, and a listener has to assume so');
    });

    test('a change names what it carried', () async {
      registry.register(_label('before'));
      whole('whole', 10);
      VChange? seen;
      registry.watch(_channel, (change) => seen = change);

      partial('partial', seq: 11, base: 10);

      expect(seen!.keys, [WireKey.k_Label_text]);
      expect(seen!.touches(WireKey.k_Label_text), isTrue);
      expect(seen!.touches(WireKey.k_Control_toolTipText), isFalse);
      expect(seen!.isOnly(const {WireKey.k_Label_text}), isTrue);
      expect(seen!.isOnly(const {WireKey.k_Control_toolTipText}), isFalse);
    });

    test('a refused change tells nobody', () async {
      registry.register(_label('before'));
      whole('whole', 10);
      var told = 0;
      registry.watch(_channel, (_) => told++);

      partial('never applied', seq: 30, base: 29);

      expect(told, 0,
          reason: 'nothing was applied, so a rebuild would draw the same state and a listener '
              'comparing against it would be comparing against a change that never happened');
    });
  });

  // Everything a value carries below it. A widget arrives inside its parent as often as on its own
  // channel - the fold sends a whole ancestor when it carries a dirty descendant - and both are
  // descriptions of the same widget.
  group('a value arriving inside another', () {
    test('is registered as a widget in its own right', () async {
      registry.register(VComposite()
        ..id = 7
        ..seq = 5
        ..style = SWT.NONE
        ..children = [_label('inside a parent', seq: 5)]);

      expect((registry.valueOn(_channel) as VLabel?)?.text, 'inside a parent');
    });

    test('is substituted for the one already held', () async {
      final held = registry.register(_label('held'));

      final parent = VComposite()
        ..id = 7
        ..seq = 5
        ..style = SWT.NONE
        ..children = [_label('a copy the parent was written with', seq: 1)];
      registry.register(parent);

      expect(identical(parent.children!.first, held), isTrue,
          reason: "the parent's list has to be a list of the widgets themselves - anything reading "
              'a child through its parent otherwise reads a snapshot');
    });

    test('replaces what is held when it was written later', () async {
      registry.register(_label('older', seq: 5));

      registry.register(VComposite()
        ..id = 7
        ..seq = 20
        ..style = SWT.NONE
        ..children = [_label('written after the child was last told anything', seq: 20)]);

      expect((registry.valueOn(_channel) as VLabel).text,
          'written after the child was last told anything');
    });

    test('can take a partial update straight after', () async {
      // Java counts a widget written inside an ancestor as delivered, and computes its next update
      // against that state. A client that had not noticed would have nothing to merge into.
      registry.register(VComposite()
        ..id = 7
        ..seq = 5
        ..style = SWT.NONE
        ..children = [_label('inside a parent', seq: 5)]);

      partial('updated', seq: 6, base: 5);

      expect((registry.valueOn(_channel) as VLabel).text, 'updated');
      expect(deliveryGate.recoveries, 0,
          reason: 'asking for the whole widget back here would undo the fold: the ancestor sent it '
              'precisely so it would not have to be sent again');
    });
  });

  group('a layout that places its children by their bounds', () {
    // Only a child's move may relayout its composite: a layout pass repaints up to the nearest boundary.
    Future<int> ticksDuring(Future<void> Function() delivery) async {
      var ticks = 0;
      final listenable = registry.boundsChangesOn([_channel]);
      void count() => ticks++;
      listenable.addListener(count);
      await delivery();
      listenable.removeListener(count);
      return ticks;
    }

    void bounds(int width, {required int seq, required int base}) => applyChange(
        _label('label', seq: seq)
          ..bounds = (VRectangle()
            ..width = width
            ..height = 20),
        keys: [WireKey.k_Control_bounds],
        base: base,
        registry: registry);

    test('is not asked to run again for a change that moves nothing', () async {
      registry.register(_label('before'));
      final ticks = await ticksDuring(() async => partial('after', seq: 2, base: 1));
      expect(ticks, 0, reason: 'the text changed; where the label is did not');
    });

    test('is asked to run again when a child moves', () async {
      registry.register(_label('label'));
      final ticks = await ticksDuring(() async => bounds(80, seq: 2, base: 1));
      expect(ticks, 1);
    });

    test('is asked to run again when a child arrives whole', () async {
      registry.register(_label('label'));
      final ticks = await ticksDuring(() async => whole('replaced', 2));
      expect(ticks, 1, reason: 'a whole widget may have moved as well');
    });

    test('is not asked to run again for its holder when what it holds changes', () async {
      registry.register(VComposite()
        ..swt = 'Composite'
        ..id = 7
        ..seq = 1
        ..style = SWT.NONE
        ..children = [_label('inside a parent')]);
      var ticks = 0;
      final holder = registry.boundsChangesOn(['Composite/7']);
      void count() => ticks++;
      holder.addListener(count);
      // Nothing renders the label on its own, so the parent is told on its behalf.
      partial('changed', seq: 2, base: 1);
      holder.removeListener(count);
      expect(ticks, 0, reason: 'what the holder draws changed, not where the holder is');
    });
  });

  group('watching a widget that has not arrived', () {
    test('the interest is honoured once it does', () async {
      var told = 0;
      registry.watch(_channel, (_) => told++);

      registry.register(_label('arrived'));
      whole('and then updated', 10);

      expect(told, 1);
      expect((registry.valueOn(_channel) as VLabel).text, 'and then updated');
    });

    test('nothing is held until it does', () async {
      registry.watch(_channel, (_) {});

      expect(registry.valueOn(_channel), isNull,
          reason: 'a stand-in for a widget that has not arrived is not that widget, and handing it '
              'out would be handing out an empty one');
      expect(registry.size, 0);
    });
  });

  // A widget written as a name rather than as a description: the sender knows the far side already
  // holds it and that it has not changed, so it says which widget it is and nothing else. Without
  // this, adding one row to a table re-describes every other row.
  group('a value that arrives as a reference', () {
    /// The parent carrying [children], [_label] among them only named, as Java names a widget it
    /// counts delivered.
    void parentCarrying(List<VControl> children, int seq) => applyWhole(
        VComposite()
          ..id = 7
          ..seq = seq
          ..style = SWT.NONE
          ..children = children,
        registry: registry,
        named: {_id});

    void registerParent() {
      registry.register(VComposite()
        ..id = 7
        ..seq = 1
        ..style = SWT.NONE);
    }

    test('resolves to the widget already held, untouched', () async {
      registerParent();
      final held = registry.register(_label('the real state'));

      parentCarrying([_label('named')], 30);

      expect(identical(registry.valueOn(_channel), held), isTrue);
      expect((registry.valueOn(_channel) as VLabel).text, 'the real state',
          reason: 'a reference carries no state, so there is nothing in it to apply - copying it '
              'over what is held would erase the widget to say it had not changed');
    });

    test('is substituted into the list that carried it', () async {
      registerParent();
      final held = registry.register(_label('the real state'));

      parentCarrying([_label('named')], 30);

      final children = (registry.valueOn('Composite/7') as VComposite).children!;
      expect(identical(children.single, held), isTrue,
          reason: "the parent's list has to end up holding the widget, however it was named");
    });

    test('for a widget this client does not hold, asks for it', () async {
      registerParent();

      parentCarrying([_label('named')], 30);

      expect(registry.valueOn(_channel), isNull,
          reason: 'the empty shell that arrived is not the widget and must not be held as it - the '
              'two sides disagree about what was delivered, and the widget is asked for instead');
    });

    test('does not stop a changed sibling in the same list from arriving', () async {
      registerParent();
      registry.register(_label('unchanged'));

      parentCarrying([
        _label('named'),
        VLabel()
          ..id = 99
          ..seq = 31
          ..style = SWT.NONE
          ..text = 'the new one',
      ], 30);

      expect((registry.valueOn(_channel) as VLabel).text, 'unchanged');
      expect((registry.valueOn('Label/99') as VLabel).text, 'the new one');
    });
  });

  group('eviction', () {
    test('an evicted widget is forgotten entirely', () async {
      registry.register(_label('before'));
      expect(registry.size, 1);

      registry.evict(_channel);

      expect(registry.size, 0);
      expect(registry.valueOn(_channel), isNull);
    });

    test('eviction forgets what the widget held, so a later change is refused', () async {
      registry.register(_label('before'));
      whole('whole', 10);
      registry.evict(_channel);

      registry.register(_label('fresh'));
      partial('partial', seq: 11, base: 10);

      expect(deliveryGate.recoveries, 1,
          reason: 'a new widget under a recycled id holds none of the old state, so an update '
              'computed against that state does not fit it');
    });
  });
}
