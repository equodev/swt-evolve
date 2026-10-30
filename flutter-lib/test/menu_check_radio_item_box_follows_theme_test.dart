// A CHECK or RADIO menu item draws its box or ring in the menu theme's colours. The default theme
// draws it; a theme that makes those colours transparent, or the transparent_menu_check_box flag,
// leaves only the mark, which then shows only while the item is selected, as native menus do.
// Labels keep one column either way.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/menuitem_theme_extension.dart';

import 'support/menu_shown_ack.dart';

const _checkOff = 'Check off';
const _checkOn = 'Check on';
const _radioOff = 'Radio off';
const _radioOn = 'Radio on';
const _labels = [_checkOff, _checkOn, _radioOff, _radioOn];

VMenuItem _menuItem(int id, int style, String text, bool selected) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..selection = selected
  ..text = text;

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.MULTI
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Node 0',
  ]
  ..menu = (VMenu()
    ..id = 100
    ..style = SWT.POP_UP
    ..enabled = true
    ..items = [
      _menuItem(101, SWT.CHECK, _checkOff, false),
      _menuItem(102, SWT.CHECK, _checkOn, true),
      _menuItem(103, SWT.RADIO, _radioOff, false),
      _menuItem(104, SWT.RADIO, _radioOn, true),
    ])
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 400);

MenuItemThemeExtension _transparentBox(MenuItemThemeExtension t) => t.copyWith(
      checkboxColor: Colors.transparent,
      checkboxSelectedColor: Colors.transparent,
      checkboxHoverColor: Colors.transparent,
      checkboxBorderColor: Colors.transparent,
      checkboxCheckmarkColor: t.textColor,
      radioButtonColor: Colors.transparent,
      radioButtonSelectedColor: Colors.transparent,
      radioButtonHoverColor: Colors.transparent,
      radioButtonSelectedHoverColor: Colors.transparent,
      radioButtonBorderColor: Colors.transparent,
      radioButtonInnerColor: t.textColor,
    );

Future<void> _openMenu(WidgetTester tester,
    {MenuItemThemeExtension Function(MenuItemThemeExtension)? menuTheme}) async {
  Widget tree = TreeSwt<VTree>(value: _tree());
  if (menuTheme != null) {
    final inner = tree;
    tree = Builder(builder: (context) {
      final theme = Theme.of(context);
      final menuItem = menuTheme(theme.extension<MenuItemThemeExtension>()!);
      return Theme(
        data: theme.copyWith(extensions: [
          ...theme.extensions.values.where((e) => e is! MenuItemThemeExtension),
          menuItem,
        ]),
        child: inner,
      );
    });
  }
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 800, height: 400, child: tree),
  ));
  await tester.pump();
  await tester.tap(find.text('Node 0'), buttons: kSecondaryButton);
  await tester.pump();
  await tester.pump();
  await ackMenuShown(tester, 100);
  for (final label in _labels) {
    expect(find.text(label), findsOneWidget, reason: 'the menu must show "$label"');
  }
}

bool _inLeadingOf(WidgetTester tester, Rect rect, String label) {
  final labelRect = tester.getRect(find.text(label));
  return rect.right <= labelRect.left &&
      rect.center.dy >= labelRect.top &&
      rect.center.dy <= labelRect.bottom;
}

/// The bordered box or ring drawn before [label], if any: read off what paints decorations, which a
/// [DecoratedBox] or an animated container's own box can be.
BoxDecoration? _indicatorOf(WidgetTester tester, String label) {
  for (final element in find.byWidgetPredicate((_) => true).evaluate()) {
    if (element is! RenderObjectElement) continue;
    final box = element.renderObject;
    if (box is! RenderDecoratedBox) continue;
    final decoration = box.decoration;
    if (decoration is! BoxDecoration || decoration.border == null) continue;
    if (_inLeadingOf(tester, box.localToGlobal(Offset.zero) & box.size, label)) return decoration;
  }
  return null;
}

bool _visible(Color? c) => c != null && c.a > 0;

bool _drawsBox(BoxDecoration? d) =>
    d != null && (_visible(d.color) || _visible((d.border as Border).top.color));

void _expectLabelsAligned(WidgetTester tester) {
  final labelX = _labels.map((l) => tester.getTopLeft(find.text(l)).dx).toSet();
  expect(labelX, hasLength(1), reason: 'all labels start at the same x whatever the state');
}

void _expectMarkOnly(WidgetTester tester, Color markColor) {
  for (final label in _labels) {
    expect(_drawsBox(_indicatorOf(tester, label)), isFalse,
        reason: '"$label" draws no visible box or ring when the theme makes it transparent');
  }

  final checks = find.byIcon(Icons.check);
  expect(checks, findsOneWidget, reason: 'only the selected CHECK item shows a check mark');
  expect(_inLeadingOf(tester, tester.getRect(checks), _checkOn), isTrue);
  expect((checks.evaluate().single.widget as Icon).color, markColor,
      reason: 'the check mark is drawn in the item text colour');

  final dots = find.byType(DecoratedBox).evaluate().where((e) {
    final d = (e.widget as DecoratedBox).decoration;
    if (d is! BoxDecoration || d.border != null || !_visible(d.color)) return false;
    if (d.shape != BoxShape.circle) return false;
    final box = e.renderObject as RenderBox;
    return _labels.any((l) => _inLeadingOf(tester, box.localToGlobal(Offset.zero) & box.size, l));
  }).toList();
  expect(dots, hasLength(1), reason: 'only the selected RADIO item shows a dot');
  final dotBox = dots.single.renderObject as RenderBox;
  expect(_inLeadingOf(tester, dotBox.localToGlobal(Offset.zero) & dotBox.size, _radioOn), isTrue);

  expect(((dots.single.widget as DecoratedBox).decoration as BoxDecoration).color, markColor,
      reason: 'the dot is drawn in the item text colour');
  _expectLabelsAligned(tester);
}

Color _textColor(WidgetTester tester) =>
    Theme.of(tester.element(find.text(_checkOn))).extension<MenuItemThemeExtension>()!.textColor;

void main() {
  testWidgets('the default theme draws a box or ring on every CHECK and RADIO item',
      (tester) async {
    await _openMenu(tester);
    for (final label in _labels) {
      expect(_drawsBox(_indicatorOf(tester, label)), isTrue,
          reason: '"$label" draws its box or ring in the default theme');
    }
    _expectLabelsAligned(tester);
  });

  testWidgets('a theme with transparent box colours leaves only the mark, while selected',
      (tester) async {
    await _openMenu(tester, menuTheme: _transparentBox);
    _expectMarkOnly(tester, _textColor(tester));
  });

  testWidgets('the transparent_menu_check_box flag leaves only the mark, while selected',
      (tester) async {
    setConfigFlags(ConfigFlags()..transparent_menu_check_box = true);
    addTearDown(resetConfigFlags);
    await _openMenu(tester);
    _expectMarkOnly(tester, _textColor(tester));
  });
}
