// A primary click that dismisses an open menu is consumed by the dismissal: the control under the
// pointer gets no MouseDown, MouseUp or focus from it. The next click reaches it as usual, and so
// does a secondary click made while the menu is open.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/decorations.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/styledtextrenderer.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/decorations_align.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';
import 'support/menu_bar_fixtures.dart';
import 'support/menu_shown_ack.dart';
import 'support/sent_events.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _CapturingStyledTextSwt extends StyledTextSwt<VStyledText> {
  const _CapturingStyledTextSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VStyledText val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!);
}

const _menuId = 100;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..menu = (VMenu()
    ..id = _menuId
    ..style = SWT.POP_UP
    ..enabled = true
    ..items = [
      VMenuItem()
        ..id = 101
        ..style = SWT.PUSH
        ..enabled = true
        ..text = 'Reload',
    ])
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Node',
  ]
  ..bounds = _rect(0, 0, 200, 300);

VStyledText _editor() => VStyledText()
  ..swt = 'StyledText'
  ..id = 2
  ..renderer = (VStyledTextRenderer()
    ..ascent = 1
    ..descent = 1)
  ..style = SWT.MULTI
  ..enabled = true
  ..editable = true
  ..text = 'first line\nsecond line'
  ..bounds = _rect(0, 0, 400, 300);

void main() {
  late List<String> editorEvents;

  Future<void> openMenu(WidgetTester tester) async {
    editorEvents = [];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(width: 200, height: 300, child: TreeSwt<VTree>(value: _tree())),
          SizedBox(
            width: 400,
            height: 300,
            child: _CapturingStyledTextSwt(value: _editor(), onEvent: editorEvents.add),
          ),
        ],
      ),
    ));
    await tester.pump();
    await tester.tapAt(tester.getCenter(find.text('Node')), buttons: kSecondaryButton);
    await tester.pump();
    await ackMenuShown(tester, _menuId);
    expect(find.text('Reload'), findsOneWidget);
    editorEvents.clear();
  }

  // Right of the tree, clear of the popup that hangs below the pointer that opened it.
  Offset inEditor(WidgetTester tester) => tester.getTopLeft(find.byType(_CapturingStyledTextSwt)) + const Offset(300, 20);

  testWidgets('a primary click that closes a context menu does not reach the control below', (tester) async {
    await openMenu(tester);

    await tester.tapAt(inEditor(tester));
    await tester.pumpAndSettle();

    expect(find.text('Reload'), findsNothing, reason: 'the click closes the menu');
    expect(editorEvents, isNot(contains('Mouse/MouseDown')));
    expect(editorEvents, isNot(contains('Mouse/MouseUp')));
    expect(editorEvents, isNot(contains('Focus/FocusIn')));

    await tester.tapAt(inEditor(tester));
    await tester.pumpAndSettle();
    expect(editorEvents, containsAllInOrder(['Mouse/MouseDown', 'Mouse/MouseUp']),
        reason: 'the next click, with no menu open, is delivered');
  });

  testWidgets('a secondary click outside an open context menu still reaches the control below', (tester) async {
    await openMenu(tester);

    await tester.tapAt(inEditor(tester), buttons: kSecondaryButton);
    await tester.pumpAndSettle();

    expect(editorEvents, containsAllInOrder(['Mouse/MouseDown', 'Mouse/MouseUp']));
  });

  group('the menu bar button of a vertically aligned menu bar', () {
    const paneId = 20;
    String paneMouse(String action) => 'Composite/$paneId/Mouse/$action';
    List<String> sent() => sentEvents().map((f) => f.$1).toList();

    setUp(() => EquoCommBase.recordSentFrames = true);
    tearDown(() => EquoCommBase.recordSentFrames = false);

    // A composite with a child: its own area goes through the composite's interaction chrome, not
    // through ControlImpl.wrap().
    VComposite pane() => VComposite()
      ..id = paneId
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = rect(0, 60, viewportWidth.toInt(), 200)
      ..children = [
        VComposite()
          ..id = 21
          ..style = SWT.NONE
          ..enabled = true
          ..visible = true
          ..bounds = rect(0, 0, 20, 20),
      ];

    testWidgets('a primary click that closes its menu does not reach the composite below', (tester) async {
      setConfigFlags(ConfigFlags()..decorations_align = DecorationsAlign.vleft);
      freshClient();
      await pumpDecorations(
          tester, decorations(menuBar: fileMenuBar(withItems: true), children: [mainToolbar(), pane()]));
      final inPane = tester.getTopLeft(find.byType(DecorationsSwt<VDecorations>)) + const Offset(400, 160);

      await tester.tap(find.byIcon(Icons.menu));
      await tester.pumpAndSettle();
      expect(find.text('File'), findsOneWidget, reason: 'the button opens the menu');
      EquoCommService.commForTesting.clearSentFrames();

      await tester.tapAt(inPane);
      await tester.pumpAndSettle();

      expect(find.text('File'), findsNothing, reason: 'the click closes the menu');
      expect(sent(), isNot(contains(paneMouse('MouseDown'))));
      expect(sent(), isNot(contains(paneMouse('MouseUp'))));

      await tester.tapAt(inPane);
      await tester.pumpAndSettle();
      expect(sent(), containsAllInOrder([paneMouse('MouseDown'), paneMouse('MouseUp')]),
          reason: 'the next click, with no menu open, is delivered');
    });

    testWidgets('a secondary click outside its menu opens the context menu of the control below',
        (tester) async {
      setConfigFlags(ConfigFlags()..decorations_align = DecorationsAlign.vleft);
      freshClient();
      await pumpDecorations(tester,
          decorations(menuBar: fileMenuBar(withItems: true), children: [mainToolbar(), pane()..menu = paneMenu()]));
      final inPane = tester.getTopLeft(find.byType(DecorationsSwt<VDecorations>)) + const Offset(400, 160);

      await tester.tap(find.byIcon(Icons.menu));
      await tester.pumpAndSettle();
      expect(find.text('File'), findsOneWidget, reason: 'the button opens the menu');

      await tester.tapAt(inPane, buttons: kSecondaryButton);
      await tester.pump();
      await ackMenuShown(tester, paneMenuId);

      expect(find.text('File'), findsNothing, reason: 'the click closes the menu');
      expect(find.text('Refresh'), findsOneWidget, reason: 'the same click opens the context menu');
    });
  });

  group('a menu bar drop-down', () {
    const buttonId = 30;
    List<String> sentBy(int id) =>
        sentEvents().map((f) => f.$1).where((channel) => channel.contains('/$id/')).toList();

    setUp(() => EquoCommBase.recordSentFrames = true);
    tearDown(() {
      EquoCommBase.recordSentFrames = false;
      resetConfigFlags();
    });

    VButton button() => VButton()
      ..id = buttonId
      ..style = SWT.PUSH
      ..enabled = true
      ..visible = true
      ..text = 'Run'
      ..bounds = rect(300, 120, 120, 40);

    testWidgets('a primary click that closes it does not reach the button below', (tester) async {
      setConfigFlags(ConfigFlags()..decorations_align = DecorationsAlign.hleft);
      freshClient();
      await pumpDecorations(tester,
          decorations(menuBar: fileMenuBar(withItems: true), children: [button()], ownMenuStrip: true, menuStripHeight: 28));

      await tester.tap(find.text('File'));
      await tester.pump();
      await ackMenuShown(tester, 202);
      await tester.pumpAndSettle();
      expect(find.text('Exit'), findsOneWidget, reason: 'the bar item opens its drop-down');
      EquoCommService.commForTesting.clearSentFrames();

      await tester.tap(find.text('Run'));
      await tester.pumpAndSettle();

      expect(find.text('Exit'), findsNothing, reason: 'the click closes the drop-down');
      expect(sentBy(buttonId), isEmpty, reason: 'no MouseDown, MouseUp or Selection');

      await tester.tap(find.text('Run'));
      await tester.pumpAndSettle();
      expect(sentBy(buttonId), containsAllInOrder(['Button/$buttonId/Mouse/MouseDown', 'Button/$buttonId/Selection/Selection']),
          reason: 'the next click, with no menu open, is delivered');
    });

    testWidgets('a click on another bar title opens that title\'s drop-down', (tester) async {
      setConfigFlags(ConfigFlags()..decorations_align = DecorationsAlign.hleft);
      freshClient();
      final bar = fileMenuBar(withItems: true)..items = [...fileMenuBar().items!, cascade(210, 'Edit', 'Copy')];
      await pumpDecorations(
          tester, decorations(menuBar: bar, children: [button()], ownMenuStrip: true, menuStripHeight: 28));

      await tester.tap(find.text('File'));
      await tester.pump();
      await ackMenuShown(tester, 202);
      await tester.pumpAndSettle();
      expect(find.text('Exit'), findsOneWidget);

      await tester.tap(find.text('Edit'));
      await tester.pump();
      await ackMenuShown(tester, 211);
      await tester.pumpAndSettle();

      expect(find.text('Exit'), findsNothing, reason: "File's drop-down closes");
      expect(find.text('Copy'), findsOneWidget, reason: "the same click opens Edit's");
    });
  });
}

const paneMenuId = 300;

VMenu paneMenu() => VMenu()
  ..id = paneMenuId
  ..style = SWT.POP_UP
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = 301
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Refresh',
  ];
