// A Swing island inside a control that has a context menu. Natively the embedded AWT window takes
// every click over it, so the host never opens its menu by itself: an application that updates its
// selection on the AWT release and then opens the menu gets a menu for the new target. Here the
// mirror only forwards pointers to AWT, so the host's own secondary-tap handling must not run for a
// press over the mirror.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/custom/swing_island_evolve.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/control_evolve.dart' show ControlImpl;

import 'support/channels.dart' as channels;
import 'support/menu_detect_ack.dart';
import 'support/menu_shown_ack.dart';

const int hostId = 1;
const int islandId = 7;
const int menuId = 100;

/// A point over the island, which covers the host's top 200 pixels.
const Offset overIsland = Offset(200, 100);

/// A point on the host, below the island.
const Offset belowIsland = Offset(200, 250);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VMenu _menu() => VMenu()
  ..id = menuId
  ..style = SWT.POP_UP
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = 101
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Properties...',
  ];

/// The host control with the menu; the composite SWT_AWT.new_Frame was given fills its top part.
VComposite _host() => VComposite()
  ..id = hostId
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..menu = _menu()
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    VComposite()
      ..swt = 'SwingIsland'
      ..id = islandId
      ..style = SWT.EMBEDDED
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 400, 200),
  ];

/// Stands in for swing-evolve's mirror: it forwards raw pointers to AWT and joins no gesture.
class _Mirror extends StatelessWidget {
  const _Mirror(this.forwarded);

  final List<String> forwarded;

  @override
  Widget build(BuildContext context) => Listener(
    behavior: HitTestBehavior.opaque,
    onPointerDown: (_) => forwarded.add('down'),
    onPointerMove: (_) => forwarded.add('move'),
    onPointerUp: (_) => forwarded.add('up'),
    onPointerSignal: (event) {
      if (event is PointerScrollEvent) forwarded.add('wheel');
    },
    child: const SizedBox.expand(),
  );
}

/// The channels sent so far.
List<String> _sent() =>
    EquoCommService.commForTesting.sentFrames.map(EquoCommBase.decodeFrame).map((f) => f.$1).toList();

Iterable<String> _menuShown() => _sent().where((c) => c.startsWith('Menu/$menuId/'));

final _pastTheHover = Duration(milliseconds: 2 * ControlImpl.hoverDelayMs);

Future<void> _pumpIsland(WidgetTester tester) async {
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 300, child: CompositeSwt<VComposite>(value: _host())),
    ),
  );
  await tester.pump();
  EquoCommService.commForTesting.receiveBinary(
    channels.frame('SwingIsland/$islandId/swingIsland', TaggedValues.encode({'windowId': 3, 'port': 8765})),
  );
  await tester.pumpAndSettle();
  expect(find.byType(_Mirror), findsOneWidget);
  EquoCommService.commForTesting.clearSentFrames();
}

/// Answers what Java would for a menu: every MenuDetect allowed, every menu shown filled, so nothing
/// the client withholds for an answer can hide a Show.
Future<void> _answerAsJava(WidgetTester tester) async {
  await ackMenuDetect(tester, 'Composite', hostId);
  await ackMenuDetect(tester, 'SwingIsland', islandId);
  await ackMenuShown(tester, menuId);
}

void main() {
  final forwarded = <String>[];

  setUp(() {
    forwarded.clear();
    swingMirrorBuilder = (context, window) => _Mirror(forwarded);
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });

  tearDown(() {
    swingMirrorBuilder = null;
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
    VRegistry.instance.clear();
  });

  testWidgets('a right-click on the island leaves the host menu to the application', (tester) async {
    await _pumpIsland(tester);

    await tester.tapAt(overIsland, buttons: kSecondaryButton);
    await tester.pump();
    await _answerAsJava(tester);

    final sent = _sent();
    expect(forwarded, ['down', 'up'], reason: 'the press and the release reach AWT');
    expect(
      _menuShown(),
      isEmpty,
      reason: 'the host showed its menu on the release, before AWT had it; the application opens '
          'that menu itself once its AWT listener has updated the selection',
    );
    expect(find.text('Properties...'), findsNothing);
    expect(
      sent.where((c) => c.startsWith('Composite/$hostId/')),
      isEmpty,
      reason: 'natively the embedded window takes the press: the host reports no MouseDown, '
          'MenuDetect or MouseUp for it',
    );
    expect(
      sent.where((c) => c.startsWith('SwingIsland/$islandId/Focus/')),
      ['SwingIsland/$islandId/Focus/FocusIn'],
      reason: 'the press focuses the island, and no menu takes the focus away from it',
    );
    await tester.pump(_pastTheHover);
  });

  testWidgets('a Ctrl+Click on the island leaves the host menu to the application on macOS',
      variant: TargetPlatformVariant.only(TargetPlatform.macOS), (tester) async {
    await _pumpIsland(tester);

    await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
    await tester.tapAt(overIsland);
    await tester.pump();
    await _answerAsJava(tester);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);

    expect(forwarded, ['down', 'up']);
    expect(_menuShown(), isEmpty);
    expect(find.text('Properties...'), findsNothing);
    await tester.pump(_pastTheHover);
  });

  testWidgets('a right-click on the host outside the island still opens its menu', (tester) async {
    await _pumpIsland(tester);

    await tester.tapAt(belowIsland, buttons: kSecondaryButton);
    await tester.pump();
    await _answerAsJava(tester);

    expect(forwarded, isEmpty);
    expect(_menuShown(), contains('Menu/$menuId/Menu/Show'));
    expect(find.text('Properties...'), findsOneWidget);
    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();
    await tester.pump(_pastTheHover);
  });

  testWidgets('a drag and the wheel over the island reach AWT', (tester) async {
    await _pumpIsland(tester);

    await tester.dragFrom(overIsland, const Offset(60, 40));
    await tester.pump();
    await tester.sendEventToBinding(PointerScrollEvent(position: overIsland, scrollDelta: const Offset(0, 50)));
    await tester.pump();

    expect(forwarded.first, 'down');
    expect(forwarded, contains('move'));
    expect(forwarded.where((e) => e == 'up'), hasLength(1));
    expect(forwarded.last, 'wheel');
    expect(_menuShown(), isEmpty);
    await tester.pump(_pastTheHover);
  });
}
