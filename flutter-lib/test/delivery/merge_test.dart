// Applying a partial update must leave the object in the state a whole message would have put it
// in. That equality is the acceptance bar for the whole delivery change, checked here at the level
// of the value object; the rendered tree is checked separately.
//
// Every case is written as "whole, then a change" against "whole, of the result", so a merge that
// drops a property, keeps a stale one, or converts a value differently from the ordinary decode
// shows up as a difference rather than as something someone has to notice.

import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart';

import 'support/canon.dart';

Map<String, dynamic> label({
  String? text,
  String? toolTipText,
  bool? enabled,
  Map<String, dynamic>? bounds,
}) =>
    {
      'swt': 'Label',
      'id': 7,
      'style': SWT.NONE,
      if (text != null) 'text': text,
      if (toolTipText != null) 'toolTipText': toolTipText,
      if (enabled != null) 'enabled': enabled,
      if (bounds != null) 'bounds': bounds,
    };

Map<String, dynamic> patch(List<String> changed, Map<String, dynamic> values) =>
    {'swt': 'Label', 'id': 7, '_s': 99, '_b': 98, '_d': changed, ...values};

/// Mounts [whole], applies [update], and asserts the result is what [expected] decodes to.
void mergeYields(
  Map<String, dynamic> whole,
  Map<String, dynamic> update,
  Map<String, dynamic> expected, {
  String? because,
}) {
  final value = mapWidgetValue(whole);
  value.mergeJson(update);
  expect(canonJson(value.toJson()), canonJson(mapWidgetValue(expected).toJson()),
      reason: because);
}

// A widget holding something that is itself serialized: bounds, a colour, a font, a child. Nearly
// every widget does, and the merge has to survive it - the properties an update does not name are
// carried over from what is held, and those are objects until something encodes them.
VLabel _labelWithBounds(String text) => VLabel()
  ..id = 77
  ..seq = 1
  ..style = SWT.NONE
  ..text = text
  ..bounds = (VRectangle()
    ..x = 1
    ..y = 2
    ..width = 80
    ..height = 20);

void main() {
  group('a widget holding a nested value', () {
    test('takes an update that never mentions it', () {
      final label = _labelWithBounds('before');

      label.mergeJson({
        'swt': 'Label',
        'id': 77,
        '_s': 2,
        '_b': 1,
        '_d': ['text'],
        'text': 'after',
      });

      expect(label.text, 'after');
      expect(label.bounds!.width, 80,
          reason: 'what the update did not name is what the widget already held, and carrying it '
              'over must not depend on how it happens to be represented in memory');
    });

    test('takes an update that changes it', () {
      final label = _labelWithBounds('before');

      label.mergeJson({
        'swt': 'Label',
        'id': 77,
        '_s': 2,
        '_b': 1,
        '_d': ['bounds'],
        'bounds': {'x': 5, 'y': 6, 'width': 160, 'height': 40},
      });

      expect(label.bounds!.width, 160);
      expect(label.bounds!.x, 5);
      expect(label.text, 'before');
    });
  });

  group('a partial update', () {
    test('replaces the property it names', () {
      mergeYields(
        label(text: 'before'),
        patch(['text'], {'text': 'after'}),
        label(text: 'after'),
      );
    });

    test('leaves the properties it does not name alone', () {
      mergeYields(
        label(text: 'before', toolTipText: 'tip'),
        patch(['text'], {'text': 'after'}),
        label(text: 'after', toolTipText: 'tip'),
        because: 'an update carries what changed; everything else is still whatever it was, and '
            'losing it would be the difference between an update and a replacement',
      );
    });

    test('applies several properties at once', () {
      mergeYields(
        label(text: 'before', toolTipText: 'old'),
        patch(['text', 'toolTipText'], {'text': 'after', 'toolTipText': 'new'}),
        label(text: 'after', toolTipText: 'new'),
      );
    });

    test('carries a property back to a default', () {
      mergeYields(
        label(text: 'before', enabled: true),
        patch(['enabled'], {'enabled': false}),
        label(text: 'before', enabled: false),
        because: 'a value returning to a default is still a change, and it has to be applied as '
            'one - it is exactly the case a writer that omits defaults would lose',
      );
    });

    test('applies a property that is an object, not a scalar', () {
      mergeYields(
        label(text: 'x', bounds: {'x': 0, 'y': 0, 'width': 10, 'height': 4}),
        patch(['bounds'], {
          'bounds': {'x': 5, 'y': 6, 'width': 20, 'height': 8}
        }),
        label(text: 'x', bounds: {'x': 5, 'y': 6, 'width': 20, 'height': 8}),
        because: 'nested values must be decoded the same way a whole message decodes them',
      );
    });

    test('ignores the protocol keys it travels with', () {
      final value = mapWidgetValue(label(text: 'before'));
      value.mergeJson(patch(['text'], {'text': 'after'}));
      final json = value.toJson();
      expect(json.containsKey('_d'), isFalse);
      expect(json.containsKey('_b'), isFalse,
          reason: 'these describe the update, not the widget, and a widget that stored them would '
              'hand them back on its next whole message');
    });
  });

  group('identity', () {
    test('the object applying the update is the object that keeps it', () {
      final value = mapWidgetValue(label(text: 'before'));
      final same = value;

      value.mergeJson(patch(['text'], {'text': 'after'}));

      expect(identical(value, same), isTrue);
      expect((value as VLabel).text, 'after',
          reason: 'every reference to a widget points at this object - a parent list, an item '
              'control. Replacing it would update one of them and leave the rest stale');
    });

    test('a widget referenced from two places sees one update', () {
      final shared = mapWidgetValue(label(text: 'before')) as VLabel;
      final parentA = VComposite()
        ..id = 1
        ..style = SWT.NONE
        ..children = [shared];
      final parentB = VComposite()
        ..id = 2
        ..style = SWT.NONE
        ..children = [shared];

      shared.mergeJson(patch(['text'], {'text': 'after'}));

      expect((parentA.children!.first as VLabel).text, 'after');
      expect((parentB.children!.first as VLabel).text, 'after',
          reason: 'this is what merging in place buys, and the reason it is not a fresh object');
    });
  });

  group('the whole state still round-trips', () {
    test('a value merged to a state equals that state built fresh', () {
      final merged = mapWidgetValue(label(text: 'a', toolTipText: 't', enabled: true));
      merged.mergeJson(patch(['text'], {'text': 'b'}));
      merged.mergeJson(patch(['toolTipText'], {'toolTipText': 'u'}));

      final fresh = mapWidgetValue(label(text: 'b', toolTipText: 'u', enabled: true));

      expect(canonJson(merged.toJson()), canonJson(fresh.toJson()),
          reason: 'a sequence of updates has to arrive at the same state as being told it outright');
    });
  });

  group('one property reads back the way the whole value decodes', () {
    // Reading a named property and decoding a whole value are two ways into the same conversion.
    // A type they disagree about - a list of children, a nullable image, a rectangle - surfaces
    // only as a property that quietly stops arriving, so every property the payload names is
    // walked rather than a chosen few.
    void everyPropertyReadsBack(Map<String, dynamic> whole) {
      final expected = mapWidgetValue(whole);
      final named = expected.toJson().keys.where(whole.containsKey).toList();
      expect(named, isNotEmpty, reason: 'a payload naming no properties checks nothing');

      for (final key in named) {
        final one = mapWidgetValue({
          'swt': whole['swt'],
          'id': whole['id'],
          'style': whole['style'],
        });
        one.mergeJson({
          'swt': whole['swt'],
          'id': whole['id'],
          '_s': 5,
          '_b': 4,
          '_d': [key],
          key: whole[key],
        });

        // Encoded on both sides: `toJson()` leaves a nested value as the object it is, and two
        // equal rectangles are still two objects.
        expect(jsonEncode(one.toJson()[key]), jsonEncode(expected.toJson()[key]),
            reason: 'property "$key" read alone differs from the same payload decoded whole');
      }
    }

    test('scalars and a nested rectangle', () {
      everyPropertyReadsBack({
        'swt': 'Label',
        'id': 7,
        'style': SWT.NONE,
        'text': 'hello',
        'toolTipText': 'tip',
        'enabled': true,
        'visible': false,
        'bounds': {'x': 1, 'y': 2, 'width': 3, 'height': 4},
      });
    });

    test('a list of children', () {
      everyPropertyReadsBack({
        'swt': 'Composite',
        'id': 1,
        'style': SWT.NONE,
        'bounds': {'x': 0, 'y': 0, 'width': 100, 'height': 200},
        'children': [
          {'swt': 'Label', 'id': 2, 'style': SWT.NONE, 'text': 'a'},
          {'swt': 'Label', 'id': 3, 'style': SWT.NONE, 'text': 'b'},
        ],
      });
    });

    test('parallel lists with holes in them', () {
      everyPropertyReadsBack({
        'swt': 'TableItem',
        'id': 11,
        'style': SWT.NONE,
        'checked': true,
        'grayed': false,
        'texts': ['one', null, 'three'],
        'images': [
          {'filename': 'a.png'},
          null,
        ],
      });
    });

    test('a nested value that is not a widget', () {
      everyPropertyReadsBack({
        'swt': 'Button',
        'id': 12,
        'style': SWT.PUSH,
        'text': 'Go',
        'selection': true,
        'alignment': SWT.CENTER,
        'image': {'filename': 'go.png'},
      });
    });
  });
}
