// A style-index change is applied with Java's exact arithmetic, and one whose result fails its
// checksum is never drawn.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

import 'delivery/support/deliver.dart';

/// A change as Java writes it.
List<int> change(List<int> target, int from, int removed, List<int> inserted) =>
    [styleIndexChangeMark, styleIndexChecksum(target), 1, target[0], from, removed, ...inserted];

void main() {
  test('the checksum is the one Java computes', () {
    // Reference values from the Java side's arithmetic.
    expect(styleIndexChecksum([7, 0, 0, 5, 1, 10, 5]), 158737335);
    expect(styleIndexChecksum([-2147483647, 3, 1, 2, 1, 5, 7]), 289145817);
    expect(styleIndexChecksum([-5]), 26);
  });

  test('a change replaces the part it names and the palette name', () {
    final base = [7, 0, 0, 5, 1, 10, 5, 0, 20, 5];
    final target = [8, 0, 0, 5, 2, 10, 5, 0, 20, 5];

    expect(applyStyleIndexChange(base, change(target, 4, 1, [2])), target);
  });

  test('a change that changes nothing is the index already held, not a copy of it', () {
    final base = [7, 0, 0, 5, 1, 10, 5];

    expect(applyStyleIndexChange(base, change(base, base.length, 0, const [])), same(base),
        reason: 'a scroll that dirtied the renderer must not look like new styling');
  });

  test('a change that does not produce the index it names is refused', () {
    final base = [7, 0, 0, 5, 1, 10, 5];
    final wrong = change([7, 0, 0, 5, 1, 10, 5], 4, 1, [2]);

    expect(applyStyleIndexChange(base, wrong), isNull,
        reason: 'computed against an index this side does not hold');
    expect(applyStyleIndexChange(base, change(base, 6, 5, const [])), isNull,
        reason: 'reaches past the end of what is held');
  });

  VStyledText value(List<int> styleIndex, {String text = 'alpha beta gamma'}) => VStyledText()
      ..swt = 'StyledText'
      ..id = 1
      ..style = SWT.H_SCROLL | SWT.V_SCROLL
      ..enabled = true
      ..editable = true
      ..caretOffset = 0
      ..text = text
      ..styleIndex = styleIndex
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 300
        ..height = 200);
  Widget app(GlobalKey<StyledTextImpl> key, VStyledText v) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
            width: 300, height: 200, child: StyledTextSwt<VStyledText>(key: key, value: v)),
      );

  testWidgets('an arriving change becomes the index it stands for', (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    final held = [7, 0, 0, 5, 1, 6, 4];
    await tester.pumpWidget(app(key, value(held)));
    await deliverWhole(value(held)..seq = 2);
    final target = [8, 0, 0, 5, 1, 6, 4, 0, 11, 5];
    await deliverWhole(value(change(target, 7, 0, [0, 11, 5]))..seq = 3);
    await tester.pump();

    expect(key.currentState!.state.styleIndex, target);
  });

  testWidgets('a change after an edit applies to the index carried across it, whatever else '
      'told the widget something in between', (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    final held = [7, 0, 0, 5, 1, 6, 4];
    await tester.pumpWidget(app(key, value(held)));
    await deliverWhole(value(held)..seq = 2);

    // Two characters typed at the start: this side moves every run along itself.
    await deliverFrame('StyledText/1/TextChange',
        {'start': 0, 'replaced': 0, 'text': 'xy', 'charCount': 18});
    // The caret, which nothing else renders, tells its holder it changed - ahead of the frame
    // the edit belongs to, in the same batch.
    final state = key.currentState!;
    // ignore: invalid_use_of_protected_member
    state.setValue(state.state);
    // Java's change is relative to the carried index [7, 0, 2, 5, 1, 8, 4].
    final target = [8, 0, 2, 5, 1, 8, 4, 0, 13, 5];
    await deliverWhole(
        value(change(target, 7, 0, [0, 13, 5]), text: 'xyalpha beta gamma')..seq = 3);
    await tester.pump();

    expect(key.currentState!.state.styleIndex, target);
  });

  testWidgets('a frame that does not carry the index leaves the one carried across the edit',
      (tester) async {
    final key = GlobalKey<StyledTextImpl>();
    final held = [7, 0, 0, 5, 1, 6, 4];
    await tester.pumpWidget(app(key, value(held)));
    await deliverWhole(value(held)..seq = 2);

    await deliverFrame('StyledText/1/TextChange',
        {'start': 0, 'replaced': 0, 'text': 'xy', 'charCount': 18});
    // The caret moving on its own, ahead of the restyling.
    await deliverChange(value(held, text: 'xyalpha beta gamma')..caretOffset = 2..seq = 3,
        changed: ['caretOffset'], base: 2);
    final target = [8, 0, 2, 5, 1, 8, 4, 0, 13, 5];
    await deliverChange(
        value(change(target, 7, 0, [0, 13, 5]), text: 'xyalpha beta gamma')..seq = 4,
        changed: ['styleIndex'], base: 3);
    await tester.pump();

    expect(key.currentState!.state.styleIndex, target);
  });
}
