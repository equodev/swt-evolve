// Java decides where the menu bar goes (`Sizes.ownMenuStripHeight`) and how tall its strip is
// (`Sizes.menuStripHeight`). Told to, the Shell draws the strip above its client area in every
// `decorations_align` mode; otherwise a visible main toolbar hosts it, even an empty one.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/decorations_align.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';
import 'support/menu_bar_fixtures.dart';

const int _stripHeight = 28;

/// What the Shell's client area holds: placed at the client origin, as Java lays it out.
VLabel _body() => VLabel()
  ..id = 20
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = rect(0, 0, 200, 20)
  ..text = 'Body';

VComposite _emptyMainToolbar(DecorationsAlign align) =>
    mainToolbar(withMenuStrip: !align.isVertical, children: const <VControl>[]);

Future<void> _pumpShell(
  WidgetTester tester, {
  required DecorationsAlign align,
  required VMenu? menuBar,
  required List<VControl> children,
  required bool ownMenuStrip,
  int stripHeight = _stripHeight,
}) async {
  setConfigFlags(ConfigFlags()..decorations_align = align);
  freshClient();
  await pumpDecorations(
      tester,
      decorations(
        menuBar: menuBar,
        children: children,
        ownMenuStrip: ownMenuStrip,
        menuStripHeight: stripHeight,
      ));
}

final _barItem = find.text('File');
final _menuIcon = find.byIcon(Icons.menu);
final _bodyLabel = find.text('Body');

void main() {
  setUp(resetConfigFlags);
  tearDown(() {
    resetConfigFlags();
    appScaleNotifier.value = 1.0;
  });

  testWidgets('the strip is as tall as Java laid it out when the zoom keeps text at its true size',
      (tester) async {
    // SWT laying out at 100 on a 150% monitor: the tree is drawn at 2/3 and text 1.5 times larger,
    // so Java gives the strip 42 units.
    const scale = 100 / 150;
    appScaleNotifier.value = scale;
    await _pumpShell(tester,
        align: DecorationsAlign.vleft,
        menuBar: fileMenuBar(),
        children: [_body()],
        ownMenuStrip: true,
        stripHeight: 42);

    final strip = tester.getRect(find.byType(MenuSwt<VMenu>).first);
    final text = tester.getRect(_barItem);
    expect(strip.height / scale, closeTo(42, 0.01));
    expect(text.bottom, lessThanOrEqualTo(strip.bottom), reason: 'the item text fits in the strip');
    expect(tester.getTopLeft(_bodyLabel).dy, greaterThanOrEqualTo(strip.bottom),
        reason: 'the client area starts below the taller strip');
  });

  testWidgets('a strip the main toolbar hosts is as tall as Java laid it out', (tester) async {
    await _pumpShell(tester,
        align: DecorationsAlign.hleft,
        menuBar: fileMenuBar(),
        children: [_emptyMainToolbar(DecorationsAlign.hleft)],
        ownMenuStrip: false,
        stripHeight: 42);

    expect(tester.getRect(find.byType(MenuSwt<VMenu>).first).height, closeTo(42, 0.01));
  });

  for (final align in DecorationsAlign.values) {
    group('$align', () {
      testWidgets('a Shell told to draw its own strip draws it above the client area',
          (tester) async {
        await _pumpShell(tester,
            align: align, menuBar: fileMenuBar(), children: [_body()], ownMenuStrip: true);

        expect(_barItem, findsOneWidget);
        expect(tester.getTopLeft(_barItem).dy, lessThan(_stripHeight));
        expect(tester.getTopLeft(_bodyLabel).dy, greaterThanOrEqualTo(_stripHeight),
            reason: 'the client area starts below the strip, as Java lays it out');
      });

      testWidgets('a hidden main toolbar draws nothing and leaves the menu to the Shell',
          (tester) async {
        await _pumpShell(tester,
            align: align,
            menuBar: fileMenuBar(),
            children: [mainToolbar(visible: false), _body()],
            ownMenuStrip: true);

        expect(_barItem, findsOneWidget);
        expect(_menuIcon, findsNothing);
        expect(tester.getTopLeft(_bodyLabel).dy, greaterThanOrEqualTo(_stripHeight));
      });

      testWidgets('an itemless menu bar, which Java gives no strip, draws nothing', (tester) async {
        await _pumpShell(tester,
            align: align,
            menuBar: fileMenuBar(withItems: false),
            children: [_body()],
            ownMenuStrip: false);

        expect(_barItem, findsNothing);
        expect(_menuIcon, findsNothing);
        expect(tester.getTopLeft(_bodyLabel).dy, lessThan(_stripHeight));
      });

      testWidgets('an empty main toolbar still hosts the menu', (tester) async {
        await _pumpShell(tester,
            align: align,
            menuBar: fileMenuBar(),
            children: [_emptyMainToolbar(align)],
            ownMenuStrip: false);

        if (align.isVertical) {
          expect(_menuIcon, findsOneWidget);
          expect(_barItem, findsNothing, reason: 'the Shell must not add a strip of its own');
        } else {
          expect(_barItem, findsOneWidget);
        }
      });
    });
  }

  testWidgets('vleft: a toolbar with tools shows the button and no Shell strip', (tester) async {
    await _pumpShell(tester,
        align: DecorationsAlign.vleft,
        menuBar: fileMenuBar(),
        children: [mainToolbar()],
        ownMenuStrip: false);

    expect(_menuIcon, findsOneWidget);
    expect(_barItem, findsNothing);
  });
}
