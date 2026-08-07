// When the window is too narrow to fit every trim contribution on one line, e4's TrimBarLayout
// wraps the overflow onto further rows and says so in each child's y. The toolbar must render that:
// laying every child out on a single line instead puts the wrapped rows back at x=0, drawn on top
// of the first one, which is what made icons pile up on each other while resizing.
//
// Java side of the same behaviour: DartMainToolbar reports the wrapped height from computeSize and
// leaves the wrapped geometry alone instead of squeezing it into one compact row.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/decorations.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/decorations_align.dart';
import 'package:swtflutter/src/impl/decorations_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const double _viewportWidth = 400;
const double _viewportHeight = 300;

/// Bar height for one compact row, and for two rows as the trim lays them out.
const int _oneRow = 40;
const int _twoRows = 80;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

/// A trim contribution: one toolbar holding a single item, positioned by the bounds the trim layout
/// would have assigned it.
VToolBar _contribution({required int id, required String label, required VRectangle bounds}) =>
    VToolBar()
      ..id = id
      ..style = SWT.FLAT
      ..enabled = true
      ..visible = true
      ..bounds = bounds
      ..items = [
        VToolItem()
          ..id = id + 1
          ..style = SWT.PUSH
          ..enabled = true
          ..text = label,
      ];

/// A bar menu as Java serializes it: a BAR menu whose items are the CASCADE entries.
VMenu _menuBarWithItems() => VMenu()
  ..id = 900
  ..style = SWT.BAR
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = 901
      ..style = SWT.CASCADE
      ..enabled = true
      ..text = 'File'
      ..menu = (VMenu()
        ..id = 910
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..items = [
          VMenuItem()
            ..id = 911
            ..style = SWT.PUSH
            ..enabled = true
            ..text = 'Exit',
        ]),
  ];

VComposite _mainToolbar({required int height, required List<VToolBar> children}) => VComposite()
  ..id = 10
  ..swt = 'MainToolbar'
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, _viewportWidth.toInt(), height)
  ..children = children;

Future<void> _pumpToolbar(
  WidgetTester tester,
  VComposite toolbar, {
  DecorationsAlign align = DecorationsAlign.vright,
  VMenu? menuBar,
}) async {
  setConfigFlags(ConfigFlags()..decorations_align = align);
  final content = menuBar == null
      ? customWidgetFromValue(toolbar)!
      : DecorationsSwt<VDecorations>(
          value: VDecorations()
            ..id = 1
            ..style = SWT.NONE
            ..enabled = true
            ..visible = true
            ..bounds = _rect(0, 0, _viewportWidth.toInt(), _viewportHeight.toInt())
            ..menuBar = menuBar
            ..children = [toolbar],
        );
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: _viewportWidth,
      height: _viewportHeight,
      child: content,
    ),
  ));
  // A second pump lets the toolbar's post-layout measurement of the vertical menu button settle.
  await tester.pump();
}

/// The rendered box of a contribution, located by the id its value object carries. Measuring the
/// toolbar itself rather than its label keeps the assertions on the row geometry: a toolbar scales
/// its own items down to fit, so label positions shift by a pixel or two with the allotted width.
Rect _rectOf(WidgetTester tester, int id) =>
    tester.getRect(find.byKey(ValueKey(id)).first);

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a wrapped contribution is drawn on its own row, not over the first', (tester) async {
    // Two rows, exactly as the trim layout reports them: the third contribution did not fit, so it
    // restarted at x=0 one row down.
    await _pumpToolbar(
      tester,
      _mainToolbar(height: _twoRows, children: [
        _contribution(id: 100, label: 'First', bounds: _rect(0, 5, 120, 30)),
        _contribution(id: 200, label: 'Second', bounds: _rect(120, 5, 120, 30)),
        _contribution(id: 300, label: 'Wrapped', bounds: _rect(0, 45, 120, 30)),
      ]),
    );

    final first = _rectOf(tester, 100);
    final wrapped = _rectOf(tester, 300);

    expect(wrapped.top, greaterThanOrEqualTo(first.bottom),
        reason: 'the wrapped row must sit below the first, not on top of it');
    expect(wrapped.overlaps(first), isFalse);
    expect(wrapped.overlaps(_rectOf(tester, 200)), isFalse);
    // It restarted at x=0, so it shares the horizontal band of the first row's opening item.
    expect(wrapped.left, closeTo(first.left, 1));
  });

  testWidgets('every row keeps its own horizontal flow', (tester) async {
    await _pumpToolbar(
      tester,
      _mainToolbar(height: _twoRows, children: [
        _contribution(id: 100, label: 'First', bounds: _rect(0, 5, 120, 30)),
        _contribution(id: 200, label: 'Wrapped', bounds: _rect(0, 45, 120, 30)),
        _contribution(id: 300, label: 'AfterWrapped', bounds: _rect(120, 45, 120, 30)),
      ]),
    );

    final wrapped = _rectOf(tester, 200);
    final afterWrapped = _rectOf(tester, 300);

    expect(afterWrapped.overlaps(wrapped), isFalse);
    expect(afterWrapped.left, greaterThan(wrapped.left),
        reason: 'the second row flows left to right on its own, like the first');
    expect(afterWrapped.top, closeTo(wrapped.top, 1),
        reason: 'both belong to the same row, so they share its vertical band');
  });

  testWidgets('an unwrapped bar still lays its contributions out on one row', (tester) async {
    await _pumpToolbar(
      tester,
      _mainToolbar(height: _oneRow, children: [
        _contribution(id: 100, label: 'First', bounds: _rect(0, 5, 120, 30)),
        _contribution(id: 200, label: 'Second', bounds: _rect(120, 5, 120, 30)),
      ]),
    );

    final first = _rectOf(tester, 100);
    final second = _rectOf(tester, 200);

    expect(second.top, closeTo(first.top, 1));
    expect(second.overlaps(first), isFalse);
    expect(second.left, greaterThan(first.left));
  });

  testWidgets('a horizontal menu bar does not push the row off the bottom of the bar',
      (tester) async {
    // hleft puts the menu bar in its own band above the toolbar row, and Java measures every child's
    // y from the top of the whole bar. The row is laid out below that band, so a child placed at its
    // raw y lands a menu-height too low and is clipped to a sliver.
    const menuBand = 28;
    const barHeight = 68;
    await _pumpToolbar(
      tester,
      _mainToolbar(height: barHeight, children: [
        _contribution(id: 100, label: 'First', bounds: _rect(0, menuBand + 5, 120, 30)),
        _contribution(id: 200, label: 'Second', bounds: _rect(120, menuBand + 5, 120, 30)),
      ]),
      align: DecorationsAlign.hleft,
      menuBar: _menuBarWithItems(),
    );

    final first = _rectOf(tester, 100);

    expect(first.height, closeTo(30, 1),
        reason: 'the contribution must render at its full height, not clipped to a sliver');
    expect(first.bottom, lessThanOrEqualTo(barHeight + 1),
        reason: 'it must stay inside the bar');
    expect(_rectOf(tester, 200).top, closeTo(first.top, 1));
  });

  testWidgets('wrapped rows under a horizontal menu bar stay inside the bar', (tester) async {
    // The same geometry Java produces when the trim wraps under a menu strip: the rows keep the
    // layout's own spacing and the whole block is pushed down by the strip's band. Neither row may
    // run off an edge of the bar.
    const menuBand = 28;
    const barHeight = menuBand + 94;
    await _pumpToolbar(
      tester,
      _mainToolbar(height: barHeight, children: [
        _contribution(id: 100, label: 'First', bounds: _rect(0, menuBand + 17, 120, 30)),
        _contribution(id: 200, label: 'Second', bounds: _rect(120, menuBand + 17, 120, 30)),
        _contribution(id: 300, label: 'Wrapped', bounds: _rect(0, menuBand + 64, 120, 30)),
      ]),
      align: DecorationsAlign.hleft,
      menuBar: _menuBarWithItems(),
    );

    final first = _rectOf(tester, 100);
    final wrapped = _rectOf(tester, 300);

    expect(first.height, closeTo(30, 1), reason: 'the first row must not be clipped from the top');
    expect(wrapped.height, closeTo(30, 1));
    expect(first.top, greaterThanOrEqualTo(menuBand - 1.0),
        reason: 'the rows start below the menu strip, never over it');
    expect(wrapped.top, greaterThanOrEqualTo(first.bottom));
    expect(wrapped.bottom, lessThanOrEqualTo(barHeight + 1.0));
  });
}
