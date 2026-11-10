// Clicking the radio that is already selected keeps it selected: SWT only moves a group's
// selection to another radio, and Java never pushes the unchanged value back to undo a local flip.
// Under a parent with SWT.NO_RADIO_GROUP each radio toggles on its own and leaves the others alone.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/button_evolve.dart';
import 'package:swtflutter/src/impl/toolitem_evolve.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VButton _radio(int id, {bool selected = false}) => VButton()
  ..id = id
  ..style = SWT.RADIO
  ..enabled = true
  ..visible = true
  ..selection = selected
  ..text = 'Option $id'
  ..bounds = _rect(0, (id - 1) * 40, 200, 30);

VComposite _group(int style, List<VButton> radios) => VComposite()
  ..id = 100
  ..style = style
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 300, 200)
  ..children = radios;

bool _selected(WidgetTester tester, int id) =>
    tester
        .state<ButtonImpl>(find
            .ancestor(of: find.text('Option $id'), matching: find.byType(ButtonSwt<VButton>))
            .first)
        .state
        .selection ??
    false;

Future<void> _pumpGroup(WidgetTester tester, int style) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: CompositeSwt<VComposite>(
        value: _group(style, [_radio(1, selected: true), _radio(2)])),
  ));
  await tester.pumpAndSettle();
  expect(_selected(tester, 1), isTrue);
  expect(_selected(tester, 2), isFalse);
}

void main() {
  testWidgets('clicking the selected radio of a group keeps it selected',
      (WidgetTester tester) async {
    await _pumpGroup(tester, SWT.NONE);

    await tester.tap(find.text('Option 1'));
    await tester.pump();

    expect(_selected(tester, 1), isTrue,
        reason: 'a click on the selected radio must not deselect it');
    expect(_selected(tester, 2), isFalse);
  });

  testWidgets('clicking another radio of a group moves the selection to it',
      (WidgetTester tester) async {
    await _pumpGroup(tester, SWT.NONE);

    await tester.tap(find.text('Option 2'));
    await tester.pump();

    expect(_selected(tester, 1), isFalse);
    expect(_selected(tester, 2), isTrue);
  });

  testWidgets('under NO_RADIO_GROUP a click toggles only the radio clicked',
      (WidgetTester tester) async {
    await _pumpGroup(tester, SWT.NO_RADIO_GROUP);

    await tester.tap(find.text('Option 2'));
    await tester.pump();
    expect(_selected(tester, 1), isTrue,
        reason: 'NO_RADIO_GROUP leaves the other radios alone');
    expect(_selected(tester, 2), isTrue);

    await tester.tap(find.text('Option 1'));
    await tester.pump();
    expect(_selected(tester, 1), isFalse,
        reason: 'under NO_RADIO_GROUP a click on a selected radio deselects it');
    expect(_selected(tester, 2), isTrue);
  });

  testWidgets('clicking the selected radio tool item keeps it selected',
      (WidgetTester tester) async {
    final item = VToolItem()
      ..id = 201
      ..style = SWT.RADIO
      ..enabled = true
      ..selection = true
      ..text = 'Tool radio';
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 40,
        child: ToolBarSwt(
          value: VToolBar()
            ..id = 200
            ..style = SWT.HORIZONTAL | SWT.FLAT
            ..enabled = true
            ..visible = true
            ..items = [item]
            ..bounds = _rect(0, 0, 400, 40),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Tool radio'));
    await tester.pump();

    expect(tester.state<ToolItemImpl>(find.byType(ToolItemSwt)).state.selection, isTrue,
        reason: 'a click on the selected radio tool item must not deselect it');
  });

  testWidgets('under NO_RADIO_GROUP a click leaves a radio tool item as Java has it',
      (WidgetTester tester) async {
    final item = VToolItem()
      ..id = 301
      ..style = SWT.RADIO
      ..enabled = true
      ..selection = false
      ..text = 'Ungrouped radio';
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 40,
        child: ToolBarSwt(
          value: VToolBar()
            ..id = 300
            ..style = SWT.HORIZONTAL | SWT.FLAT | SWT.NO_RADIO_GROUP
            ..enabled = true
            ..visible = true
            ..items = [item]
            ..bounds = _rect(0, 0, 400, 40),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Ungrouped radio'));
    await tester.pump();

    expect(tester.state<ToolItemImpl>(find.byType(ToolItemSwt)).state.selection, isFalse,
        reason: 'Java leaves an ungrouped radio tool item unselected on a click');
  });
}
