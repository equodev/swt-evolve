// A main-toolbar ToolItem can show its tooltip as a card -- icon, title, body, shortcut -- instead
// of the one-line strip.
//
// Two properties keep it safe to turn on: it is off unless asked for, and even then it reaches
// nothing outside the main toolbar. The same bar rendered in the window body must keep the plain
// Tooltip, which is what the widget tests below pin.
//
// The parsing tests cover what the card can make of what an application already says. An e4 tooltip
// of the form "Label (Ctrl+X)" already holds a title, a body and a shortcut; an application whose
// tooltips are bare labels has nothing to derive, and gets the sample body instead.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/main_toolbar_scope.dart';
import 'package:swtflutter/src/custom/rich_tooltip.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/canvas_evolve.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/table_evolve.dart';
import 'package:swtflutter/src/impl/tree_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _barWidth = 300.0;
const _barHeight = 48.0;

VToolBar _bar({String? text = 'Open', String? toolTipText = 'Open a project (Ctrl+O)'}) => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 2
      ..style = SWT.CHECK
      ..enabled = true
      ..selection = false
      ..text = text
      ..toolTipText = toolTipText,
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _barWidth.toInt()
    ..height = _barHeight.toInt());

/// The bar inside a trim composite. `swt` decides whether it is the main toolbar or an ordinary
/// one -- the only difference the feature is allowed to see.
Widget _host({
  required String swt,
  String? text = 'Open',
  String? toolTipText = 'Open a project (Ctrl+O)',
  Alignment alignment = Alignment.topLeft,
}) =>
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: alignment,
        child: SizedBox(
          width: _barWidth,
          height: _barHeight,
          child: ToolbarComposite(
            value: VComposite()
              ..id = 10
              ..swt = swt
              ..style = SWT.NONE
              ..enabled = true
              ..visible = true
              ..bounds = (VRectangle()
                ..x = 0
                ..y = 0
                ..width = _barWidth.toInt()
                ..height = _barHeight.toInt())
              ..children = [_bar(text: text, toolTipText: toolTipText)],
            useBoundsLayout: true,
          ),
        ),
      ),
    );

Future<void> _pump(WidgetTester tester, Widget app) async {
  await tester.pumpWidget(app);
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}
}

/// Same bar, but the item carries an image -- the card then leads with the item's own artwork,
/// which is the case the alignment has to hold for.
Future<void> _pumpWithArtwork(WidgetTester tester) async {
  final bar = _bar()..items![0].image = (VImage()..filename = 'square.png');
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: _barWidth,
      height: _barHeight,
      child: ToolbarComposite(
        value: VComposite()
          ..id = 10
          ..swt = 'MainToolbar'
          ..style = SWT.NONE
          ..enabled = true
          ..visible = true
          ..bounds = (VRectangle()
            ..x = 0
            ..y = 0
            ..width = _barWidth.toInt()
            ..height = _barHeight.toInt())
          ..children = [bar],
        useBoundsLayout: true,
      ),
    ),
  ));
  for (var i = 0; i < 5; i++) {
    await tester.pump(const Duration(milliseconds: 50));
  }
  while (tester.takeException() != null) {}
}

/// A context inside the main toolbar, and one outside it, for the control-level entry point.
Widget _scoped(void Function(BuildContext) capture) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: MainToolbarScope(
        child: Builder(builder: (c) {
          capture(c);
          return const SizedBox(width: 100, height: 40);
        }),
      ),
    );

Widget _unscoped(void Function(BuildContext) capture) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Builder(builder: (c) {
        capture(c);
        return const SizedBox(width: 100, height: 40);
      }),
    );

Future<TestGesture> _hover(WidgetTester tester) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await gesture.addPointer(location: Offset.zero);
  addTearDown(gesture.removePointer);
  await gesture.moveTo(tester.getCenter(find.byType(RichToolTip)));
  await tester.pumpAndSettle(const Duration(seconds: 1));
  return gesture;
}

void main() {
  group('what the card makes of the item', () {
    test('splits the e4 "Label (Ctrl+X)" tooltip into a title, a body and a shortcut', () {
      final data = RichToolTipData.of(text: 'Open', toolTipText: 'Open a project (Ctrl+O)')!;
      expect(data.title, 'Open');
      expect(data.body, 'Open a project');
      expect(data.shortcut, 'Ctrl+O');
    });

    test('a tooltip that only restates the label gets the sample body, not a stutter', () {
      final data = RichToolTipData.of(text: 'Save', toolTipText: 'Save (Ctrl+S)')!;
      expect(data.title, 'Save');
      expect(data.body, 'Example description.');
      expect(data.shortcut, 'Ctrl+S');
    });

    test('a bare one-word tooltip gets the sample body', () {
      // Measured on a real application: its toolbar items say things like this and nothing else.
      final data = RichToolTipData.of(toolTipText: 'Tools')!;
      expect(data.title, 'Tools');
      expect(data.body, 'Example description.');
      expect(data.shortcut, isNull);
    });

    test('strips the SWT mnemonic marker from the label but keeps a literal ampersand', () {
      expect(RichToolTipData.of(text: 'Save &As', toolTipText: 'x (Ctrl+S)')!.title, 'Save As');
      expect(RichToolTipData.of(text: 'Fish && Chips', toolTipText: 'x (Ctrl+S)')!.title,
          'Fish & Chips');
    });

    test('leaves a parenthesised sentence alone -- only a keybinding is a shortcut', () {
      final data = RichToolTipData.of(
        text: 'Index',
        toolTipText: 'Rebuild the index (this can take a while)',
      )!;
      expect(data.shortcut, isNull);
      expect(data.body, 'Rebuild the index (this can take a while)');
    });

    test('an unlabelled item takes its title from the first line of the tooltip', () {
      final data = RichToolTipData.of(
        toolTipText: 'Sync\nPush local changes to the server.',
      )!;
      expect(data.title, 'Sync');
      expect(data.body, 'Push local changes to the server.');
    });

    test('nothing to say yields no card at all', () {
      expect(RichToolTipData.of(), isNull);
      expect(RichToolTipData.of(text: '  ', toolTipText: ''), isNull);
    });
  });

  // A control that paints its own content has no ToolItem behind it, only the tooltip the
  // application set on the control. It reaches the card through EvolveToolTip.maybeCard, which the
  // control's own pointer-following overlay places.
  //
  // This path is deliberately NOT confined to the main toolbar -- a canvas in a view has the same
  // problem as one in the toolbar. Which controls take it is decided by
  // ControlImpl.usesCustomTooltipCard, which only CanvasImpl overrides, so a Tree or a Table keeps
  // the plain panel.
  group('a control that paints its own content', () {
    setUp(resetConfigFlags);
    tearDown(resetConfigFlags);

    testWidgets('off by default: no card', (tester) async {
      late BuildContext ctx;
      await _pump(tester, _unscoped((c) => ctx = c));

      expect(EvolveToolTip.maybeCard(ctx, toolTipText: 'Colour bar'), isNull);
    });

    testWidgets('on: the tooltip the application set becomes a card', (tester) async {
      setConfigFlags(ConfigFlags()..custom_tooltip = true);
      late BuildContext ctx;
      await _pump(tester, _unscoped((c) => ctx = c));

      expect(EvolveToolTip.maybeCard(ctx, toolTipText: 'Colour bar'), isA<RichToolTipCard>());
    });

    testWidgets('outside the main toolbar it still gets the card', (tester) async {
      setConfigFlags(ConfigFlags()..custom_tooltip = true);
      late BuildContext inToolbar;
      late BuildContext outside;
      await _pump(tester, _scoped((c) => inToolbar = c));
      expect(EvolveToolTip.maybeCard(inToolbar, toolTipText: 'Colour bar'),
          isA<RichToolTipCard>());

      await _pump(tester, _unscoped((c) => outside = c));
      expect(EvolveToolTip.maybeCard(outside, toolTipText: 'Colour bar'),
          isA<RichToolTipCard>());
    });

    testWidgets('a control with no tooltip gets no card', (tester) async {
      setConfigFlags(ConfigFlags()..custom_tooltip = true);
      late BuildContext ctx;
      await _pump(tester, _unscoped((c) => ctx = c));

      expect(EvolveToolTip.maybeCard(ctx, toolTipText: null), isNull);
      expect(EvolveToolTip.maybeCard(ctx, toolTipText: '   '), isNull);
    });

    testWidgets('the card is sized to its content, so a caller can place it at the pointer',
        (tester) async {
      setConfigFlags(ConfigFlags()..custom_tooltip = true);
      // The control's tooltip overlay measures the card and then positions it, under constraints
      // loosened to the whole window. A card that expanded to fill would measure as the window and
      // be pushed into the top-left corner instead of following the pointer.
      await _pump(
        tester,
        EvolveApp(
          theme: ThemeMode.light,
          contentWidget: Align(
            alignment: Alignment.topLeft,
            child: RichToolTipCard(
              data: RichToolTipData.of(toolTipText: 'Colour bar')!,
            ),
          ),
        ),
      );

      final card = tester.getRect(find.byType(RichToolTipCard));
      final window = tester.view.physicalSize / tester.view.devicePixelRatio;
      expect(card.width, lessThan(window.width / 2),
          reason: 'a card that fills the window cannot be positioned');
      expect(card.height, lessThan(window.height / 2));
    });

    test('only a Canvas opts in; other controls keep the plain panel', () {
      expect(CanvasImpl().usesCustomTooltipCard, isTrue);
      expect(TreeImpl().usesCustomTooltipCard, isFalse);
      expect(TableImpl().usesCustomTooltipCard, isFalse);
    });
  });

  group('scope and opt-in', () {
    setUp(resetConfigFlags);
    tearDown(resetConfigFlags);

    testWidgets('off by default: a main-toolbar item keeps the plain tooltip', (tester) async {
      await _pump(tester, _host(swt: 'MainToolbar'));

      expect(find.byType(RichToolTip), findsNothing);
      expect(find.byType(Tooltip), findsWidgets);
    });

    testWidgets('on: a main-toolbar item shows the card', (tester) async {
      setConfigFlags(ConfigFlags()..custom_tooltip = true);
      await _pump(tester, _host(swt: 'MainToolbar'));

      expect(find.byType(RichToolTip), findsOneWidget);
    });

    testWidgets('on: the same item outside the main toolbar keeps the plain tooltip',
        (tester) async {
      setConfigFlags(ConfigFlags()..custom_tooltip = true);
      await _pump(tester, _host(swt: 'Composite'));

      expect(find.byType(RichToolTip), findsNothing);
      expect(find.byType(Tooltip), findsWidgets);
    });

    testWidgets('the card opens on hover and closes when the pointer leaves', (tester) async {
      setConfigFlags(ConfigFlags()..custom_tooltip = true);
      await _pump(tester, _host(swt: 'MainToolbar'));

      final gesture = await _hover(tester);
      expect(find.text('Ctrl+O'), findsOneWidget);
      expect(find.text('Open a project'), findsOneWidget);

      await gesture.moveTo(const Offset(-100, -100));
      await tester.pumpAndSettle();
      expect(find.text('Ctrl+O'), findsNothing);
    });

    testWidgets('an item near the right edge opens a card that stays inside the window',
        (tester) async {
      setConfigFlags(ConfigFlags()..custom_tooltip = true);
      // The bar sits against the right edge, the way a main toolbar's trailing items do, and the
      // body is long enough that the card reaches its full width -- otherwise a narrow card would
      // fit on its own and the clamping would never be exercised.
      final windowWidth = tester.view.physicalSize.width / tester.view.devicePixelRatio;
      await _pump(
        tester,
        _host(
          swt: 'MainToolbar',
          alignment: Alignment.topRight,
          toolTipText: 'Pick a folder to load. Recent projects stay in the File menu, and the '
              'workspace you had open last is restored automatically. (Ctrl+O)',
        ),
      );

      await _hover(tester);

      final body = tester.getRect(find.textContaining('Recent projects'));
      expect(body.right, lessThanOrEqualTo(windowWidth));
      expect(body.left, greaterThanOrEqualTo(0));
    });

    testWidgets('the artwork lines up with the title, not with the top of the card',
        (tester) async {
      setConfigFlags(ConfigFlags()..custom_tooltip = true);
      await _pumpWithArtwork(tester);

      final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
      await gesture.addPointer(location: Offset.zero);
      addTearDown(gesture.removePointer);
      await gesture.moveTo(tester.getCenter(find.byType(RichToolTip)));
      // Not pumpAndSettle: an image never finishes loading in a unit test, so settling times out.
      for (var i = 0; i < 12; i++) {
        await tester.pump(const Duration(milliseconds: 100));
      }

      // The artwork slot takes part in layout as one line tall and paints centred on it. Hanging it
      // from the top of a multi-line column instead is what leaves it visibly high.
      // "Open" appears twice -- the item's own label and the card's title -- so scope the title to
      // the card's row, the one holding the artwork slot.
      final cardRow =
          find.ancestor(of: find.byKey(artworkSlotKey), matching: find.byType(Row)).first;
      final slot = tester.getRect(find.byKey(artworkSlotKey));
      final title = tester.getRect(find.descendant(of: cardRow, matching: find.text('Open')));
      expect((slot.center.dy - title.center.dy).abs(), lessThan(1.0));
    });
  });
}
