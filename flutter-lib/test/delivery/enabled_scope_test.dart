// SWT's isEnabled() is a widget's own flag AND every ancestor's. That used to be computed in Java
// and sent as a property, which meant disabling one Composite had to be reported on every widget
// beneath it - something Java can only do by walking the subtree, and did not do at all once
// updates stopped carrying whole widgets.
//
// It is computed here now, from the tree the answer is about. These tests pin the behaviour that
// property was carrying: a control under a disabled ancestor takes no input, whatever its own flag
// says, and one under an enabled ancestor still does.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/group.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

import 'support/deliver.dart';

VButton _button(int id) => VButton()
  ..id = id
  ..style = SWT.PUSH
  ..text = 'press'
  ..enabled = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 80
    ..height = 24);

VComposite _composite(int id, {required bool enabled, required List<VControl> children}) =>
    VComposite()
      ..id = id
      ..style = SWT.NONE
      ..enabled = enabled
      ..children = children
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 200
        ..height = 100);

VGroup _group(int id, {required bool enabled, required List<VControl> children}) => VGroup()
  ..id = id
  ..style = SWT.NONE
  // Untitled: a title row makes the content taller than the bounds the group is laid out at, and
  // the overflow would fail the test for an unrelated reason.
  ..text = ''
  ..enabled = enabled
  ..children = children
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 100);

VToolBar _toolBar(int id, {required List<VToolItem> items}) => VToolBar()
  ..id = id
  ..style = SWT.FLAT
  ..enabled = true
  ..items = items
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 120
    ..height = 30);

VToolItem _toolItem(int id) => VToolItem()
  ..id = id
  ..style = SWT.PUSH
  ..text = 'Action'
  ..enabled = true;

Future<void> _mount(WidgetTester tester, VComposite root) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 300,
      height: 200,
      child: CompositeSwt<VComposite>(value: root),
    ),
  ));
  await tester.pumpAndSettle();
}

VTabFolder _tabFolder(int id, {required bool enabled, required VControl page}) => VTabFolder()
  ..id = id
  ..style = SWT.NONE
  ..enabled = enabled
  ..items = [
    VTabItem()
      ..id = id + 1
      ..style = SWT.NONE
      ..text = 'Tab'
      ..control = page,
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

VCTabFolder _cTabFolder(int id, {required bool enabled, required VControl page}) => VCTabFolder()
  ..id = id
  ..style = SWT.NONE
  ..enabled = enabled
  ..selection = 0
  ..items = [
    VCTabItem()
      ..id = id + 1
      ..style = SWT.NONE
      ..text = 'Tab'
      ..control = page,
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

/// Whether anything on the path to [widget]'s content is refusing pointer input.
///
/// Looks both above and below it: a control disabled by an ancestor is blocked by that ancestor's
/// wrapper, while one disabled on its own is blocked by a wrapper inside its own subtree. Either
/// way the control cannot be reached, which is the thing being asserted.
bool _blocked(WidgetTester tester, Finder widget) {
  bool anyIgnoring(Finder finder) => tester
      .widgetList<IgnorePointer>(finder)
      .any((w) => w.ignoring);
  return anyIgnoring(find.ancestor(of: widget, matching: find.byType(IgnorePointer))) ||
      anyIgnoring(find.descendant(of: widget, matching: find.byType(IgnorePointer)));
}

void main() {
  testWidgets('a control under a disabled ancestor takes no input, though its own flag is set',
      (tester) async {
    final child = _button(9);
    await _mount(tester, _composite(1, enabled: false, children: [child]));

    expect(child.enabled, isTrue, reason: 'the widget itself was never disabled');
    expect(_blocked(tester, find.byType(ButtonSwt<VButton>)), isTrue,
        reason: 'being inside something disabled is what makes it unreachable, and nothing had to '
            'tell the child about it');
  });

  testWidgets('a control under an enabled ancestor still takes input', (tester) async {
    await _mount(tester, _composite(1, enabled: true, children: [_button(9)]));

    expect(_blocked(tester, find.byType(ButtonSwt<VButton>)), isFalse);
  });

  testWidgets('a control disabled on its own takes no input', (tester) async {
    final child = _button(9)..enabled = false;
    await _mount(tester, _composite(1, enabled: true, children: [child]));

    expect(_blocked(tester, find.byType(ButtonSwt<VButton>)), isTrue);
  });

  // A Group lays its children out itself instead of going through the plain Composite path, so it
  // reaches neither wrap() nor blockWhenDisabled — the answer has to come from where the children
  // are placed, or a disabled Group would leave everything inside it live.
  testWidgets('a control under a disabled Group takes no input', (tester) async {
    final child = _button(9);
    await _mount(tester, _composite(1, enabled: true, children: [_group(5, enabled: false, children: [child])]));

    expect(child.enabled, isTrue, reason: 'the widget itself was never disabled');
    expect(_blocked(tester, find.byType(ButtonSwt<VButton>)), isTrue);
  });

  testWidgets('a control under an enabled Group still takes input', (tester) async {
    await _mount(tester, _composite(1, enabled: true, children: [_group(5, enabled: true, children: [_button(9)])]));

    expect(_blocked(tester, find.byType(ButtonSwt<VButton>)), isFalse);
  });

  // An Item is not a Control: nothing ever calls setEnabled on it, and it is not in
  // Composite.getChildren(), so Java could not have told it about the ancestor even when the
  // answer was still being sent. It is blocked because of where it is drawn.
  testWidgets('an item under a disabled ancestor takes no input, though its own flag is set',
      (tester) async {
    final item = _toolItem(21);
    await _mount(tester, _composite(1, enabled: false, children: [_toolBar(20, items: [item])]));

    expect(item.enabled, isTrue, reason: 'the item itself was never disabled');
    expect(_blocked(tester, find.byType(ToolItemSwt<VToolItem>)), isTrue);
  });

  testWidgets('an item under an enabled ancestor still takes input', (tester) async {
    await _mount(tester, _composite(1, enabled: true, children: [_toolBar(20, items: [_toolItem(21)])]));

    expect(_blocked(tester, find.byType(ToolItemSwt<VToolItem>)), isFalse);
  });

  testWidgets('re-enabling the ancestor reaches the descendant', (tester) async {
    await _mount(tester, _composite(1, enabled: false, children: [_button(9)]));
    expect(_blocked(tester, find.byType(ButtonSwt<VButton>)), isTrue);

    // Only the composite is told - which is the whole point: nothing has to be sent to anything
    // underneath it for the subtree's answer to change.
    await deliverWhole(_composite(1, enabled: true, children: [_button(9)])..seq = 2);
    await tester.pumpAndSettle();

    expect(_blocked(tester, find.byType(ButtonSwt<VButton>)), isFalse,
        reason: 'the answer follows the tree, so it changes for the whole subtree the moment the '
            'ancestor does - no update has to be sent to anything below');
  });
  // A tab folder holds its pages through its items and renders them itself, so like a Group it
  // reaches neither wrap() nor blockWhenDisabled on its own. SWT reads enablement down the parent
  // chain, so a disabled folder disables the page it is showing.
  //
  // Only the disabled direction is covered, and only for TabFolder. The other three cases cannot be
  // mounted here: a selected tab in an *enabled* TabFolder builds a Container with a negative
  // margin, and CTabFolder paints a hairline border inside a rounded rectangle - both trip a
  // framework assertion before anything can be asserted about input.
  testWidgets('a page inside a disabled TabFolder takes no input', (tester) async {
    final page = _button(9);
    await _mount(tester,
        _composite(1, enabled: true, children: [_tabFolder(5, enabled: false, page: page)]));

    expect(page.enabled, isTrue, reason: 'the page itself was never disabled');
    expect(_blocked(tester, find.byType(ButtonSwt<VButton>)), isTrue);
  });
}
