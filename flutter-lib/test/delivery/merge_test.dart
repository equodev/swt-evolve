// Applying a change must leave the object in the state a whole description would have put it in.
// That equality is the acceptance bar for delivery, checked here at the level of the value object;
// the rendered tree is checked separately.
//
// Every case is written as "whole, then a change" against "whole, of the result", so a merge that
// drops a property, keeps a stale one, or reads a value differently from the whole record shows up
// as a difference rather than as something someone has to notice.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/test_harness.dart' show describeState;

import 'support/canon.dart';
import 'support/deliver.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VLabel label({String? text, String? toolTipText, bool? enabled, VRectangle? bounds}) => VLabel()
  ..id = 7
  ..style = SWT.NONE
  ..text = text
  ..toolTipText = toolTipText
  ..enabled = enabled
  ..bounds = bounds;

/// The state a widget's value holds, in the canonical form two states are compared in.
String _state(VWidget value) => canonText(describeState(value));

/// Applies [whole], then [update] naming [keys], and asserts the result is what [expected] holds.
void mergeYields(VWidget whole, VWidget update, List<int> keys, VWidget expected, {String? because}) {
  final registry = VRegistry();
  deliveryGate.reset();
  applyWhole(whole..seq = 1, registry: registry);
  applyChange(update..seq = 2, keys: keys, base: 1, registry: registry);
  expect(_state(registry.valueOn(VRegistry.channelOf(whole))!), _state(expected), reason: because);
}

void main() {
  tearDown(deliveryGate.reset);

  group('a widget holding a nested value', () {
    test('takes a change that never mentions it', () {
      final registry = VRegistry();
      applyWhole(label(text: 'before', bounds: _rect(1, 2, 80, 20))..seq = 1, registry: registry);

      applyChange(label(text: 'after')..seq = 2,
          keys: [WireKey.k_Label_text], base: 1, registry: registry);

      final held = registry.valueOn('Label/7') as VLabel;
      expect(held.text, 'after');
      expect(held.bounds!.width, 80,
          reason: 'what the change did not name is what the widget already held');
    });

    test('takes a change that changes it', () {
      final registry = VRegistry();
      applyWhole(label(text: 'before', bounds: _rect(1, 2, 80, 20))..seq = 1, registry: registry);

      applyChange(label(bounds: _rect(5, 6, 160, 40))..seq = 2,
          keys: [WireKey.k_Control_bounds], base: 1, registry: registry);

      final held = registry.valueOn('Label/7') as VLabel;
      expect(held.bounds!.width, 160);
      expect(held.bounds!.x, 5);
      expect(held.text, 'before');
    });
  });

  group('a change', () {
    test('replaces the property it names', () {
      mergeYields(label(text: 'before'), label(text: 'after'), [WireKey.k_Label_text], label(text: 'after'));
    });

    test('leaves the properties it does not name alone', () {
      mergeYields(
        label(text: 'before', toolTipText: 'tip'),
        label(text: 'after'),
        [WireKey.k_Label_text],
        label(text: 'after', toolTipText: 'tip'),
        because: 'a change carries what changed; everything else is still whatever it was, and '
            'losing it would be the difference between a change and a replacement',
      );
    });

    test('applies several properties at once', () {
      mergeYields(
        label(text: 'before', toolTipText: 'old'),
        label(text: 'after', toolTipText: 'new'),
        [WireKey.k_Label_text, WireKey.k_Control_toolTipText],
        label(text: 'after', toolTipText: 'new'),
      );
    });

    test('carries a property back to a default', () {
      mergeYields(
        label(text: 'before', enabled: true),
        label(enabled: false),
        [WireKey.k_Control_enabled],
        label(text: 'before', enabled: false),
        because: 'a value returning to a default is still a change, and it has to be applied as '
            'one - it is exactly the case a writer that omits defaults would lose',
      );
    });

    test('applies a property that is an object, not a scalar', () {
      mergeYields(
        label(text: 'x', bounds: _rect(0, 0, 10, 4)),
        label(bounds: _rect(5, 6, 20, 8)),
        [WireKey.k_Control_bounds],
        label(text: 'x', bounds: _rect(5, 6, 20, 8)),
        because: 'a nested value must be read the same way a whole record reads it',
      );
    });
  });

  group('identity', () {
    test('the object a change is applied to is the object that keeps it', () {
      final registry = VRegistry();
      applyWhole(label(text: 'before')..seq = 1, registry: registry);
      final held = registry.valueOn('Label/7');

      applyChange(label(text: 'after')..seq = 2, keys: [WireKey.k_Label_text], base: 1, registry: registry);

      expect(identical(registry.valueOn('Label/7'), held), isTrue);
      expect((held as VLabel).text, 'after',
          reason: 'every reference to a widget points at this object - a parent list, an item '
              'control. Replacing it would update one of them and leave the rest stale');
    });

    test('a widget referenced from two places sees one change', () {
      final registry = VRegistry();
      final shared = label(text: 'before')..seq = 1;
      applyWhole(VComposite()
        ..id = 1
        ..seq = 1
        ..style = SWT.NONE
        ..children = [shared], registry: registry);
      applyWhole(VComposite()
        ..id = 2
        ..seq = 1
        ..style = SWT.NONE
        ..children = [label()..seq = 1], registry: registry);

      applyChange(label(text: 'after')..seq = 2, keys: [WireKey.k_Label_text], base: 1, registry: registry);

      final a = registry.valueOn('Composite/1') as VComposite;
      final b = registry.valueOn('Composite/2') as VComposite;
      expect((a.children!.first as VLabel).text, 'after');
      expect((b.children!.first as VLabel).text, 'after',
          reason: 'this is what merging in place buys, and the reason it is not a fresh object');
    });
  });

  group('the whole state still round-trips', () {
    test('a value merged to a state equals that state built fresh', () {
      final registry = VRegistry();
      applyWhole(label(text: 'a', toolTipText: 't', enabled: true)..seq = 1, registry: registry);
      applyChange(label(text: 'b')..seq = 2, keys: [WireKey.k_Label_text], base: 1, registry: registry);
      applyChange(label(toolTipText: 'u')..seq = 3,
          keys: [WireKey.k_Control_toolTipText], base: 2, registry: registry);

      expect(_state(registry.valueOn('Label/7')!), _state(label(text: 'b', toolTipText: 'u', enabled: true)),
          reason: 'a sequence of changes has to arrive at the same state as being told it outright');
    });
  });

  group('one property reads back the way the whole record reads it', () {
    // A change and a whole record are two ways into the same reader. A type they disagree about -
    // a list of children, a nullable image, a rectangle - surfaces only as a property that quietly
    // stops arriving, so every property the value holds is walked rather than a chosen few.
    void everyPropertyReadsBack(VWidget Function() build) {
      final expected = build();
      final keys = <int>[];
      expected.visitWire((key, v) {
        if (v != null) keys.add(key);
      });
      expect(keys, isNotEmpty, reason: 'a value holding no properties checks nothing');
      final wanted = describeState(expected);

      for (final key in keys) {
        final registry = VRegistry();
        deliveryGate.reset();
        applyWhole(newBare(build())..seq = 4, registry: registry);
        applyChange(build()..seq = 5, keys: [key], base: 4, registry: registry);

        expect(canonText(describeState(registry.valueOn(VRegistry.channelOf(expected))!)['$key']),
            canonText(wanted['$key']),
            reason: 'property $key read alone differs from the same value read whole');
      }
    }

    test('scalars and a nested rectangle', () {
      everyPropertyReadsBack(() => label(text: 'hello', toolTipText: 'tip', enabled: true, bounds: _rect(1, 2, 3, 4))
        ..visible = false);
    });

    test('a list of children', () {
      everyPropertyReadsBack(() => VComposite()
        ..id = 1
        ..style = SWT.NONE
        ..bounds = _rect(0, 0, 100, 200)
        ..children = [
          VLabel()
            ..id = 2
            ..style = SWT.NONE
            ..text = 'a',
          VLabel()
            ..id = 3
            ..style = SWT.NONE
            ..text = 'b',
        ]);
    });

    test('parallel lists with holes in them', () {
      everyPropertyReadsBack(() => VTableItem()
        ..id = 11
        ..style = SWT.NONE
        ..checked = true
        ..grayed = false
        ..texts = ['one', null, 'three']
        ..images = [VImage()..filename = 'a.png', null]);
    });

    test('a nested value that is not a widget', () {
      everyPropertyReadsBack(() => VButton()
        ..id = 12
        ..style = SWT.PUSH
        ..text = 'Go'
        ..selection = true
        ..alignment = SWT.CENTER
        ..image = (VImage()..filename = 'go.png'));
    });
  });
}

/// A value of [like]'s class and identity holding nothing else.
VWidget newBare(VWidget like) => (switch (like) {
      VLabel() => VLabel(),
      VComposite() => VComposite(),
      VTableItem() => VTableItem(),
      VButton() => VButton(),
      _ => throw ArgumentError(like.runtimeType),
    })
  ..id = like.id
  ..style = like.style;
