// The main window's view stacks are listed as icons on rails at the window's sides, from the
// ViewRails record Java sends: each stack's views at the rail end its place names, a press on an
// icon shows that view, a press on the active view's icon minimizes its stack, and a listed stack
// draws only its selected view.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/custom/view_rails.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/impl/utils/test_anchors.dart';
import 'package:swtflutter/src/theme/theme_extensions/view_rails_theme_extension.dart';

import 'support/sent_events.dart';

const _mainShellId = 7;

VCTabFolder _folder(int id, List<String> views, {int selection = 0}) => VCTabFolder()
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..selection = selection
  ..items = [
    for (var i = 0; i < views.length; i++)
      VCTabItem()
        ..id = id * 10 + i
        ..text = views[i]
        ..showing = true,
  ];

/// The Java perspective: Package Explorer at the left's top, Outline at the right's top, Problems
/// and Console under the editor, which is the left rail's bottom, and a minimized stack.
void _deliver({int? minimized, int? size}) {
  final explorer = VRegistry.instance.register(_folder(1, ['Package Explorer', 'Git'])) as VCTabFolder;
  final outline = VRegistry.instance.register(_folder(2, ['Outline'])) as VCTabFolder;
  final bottom = VRegistry.instance.register(_folder(3, ['Problems', 'Console'], selection: 1)) as VCTabFolder;
  final left = VRegistry.instance.register(VComposite()..id = 50) as VControl;
  final right = VRegistry.instance.register(VComposite()..id = 51) as VControl;
  int place(int id, int p) => id == minimized ? p | ViewRailPlace.minimized : p;
  VRegistry.instance.deliverHolderForTesting(
    'ViewRails',
    _mainShellId,
    VViewRails()
      ..stacks = [explorer, outline, bottom]
      ..places = [
        1, place(1, ViewRailPlace.leftTop),
        2, place(2, ViewRailPlace.rightTop),
        3, place(3, ViewRailPlace.leftBottom),
      ]
      ..rails = [left, right]
      ..railSides = [50, SWT.LEFT, 51, SWT.RIGHT]
      ..size = size,
  );
}

Future<void> _pumpRail(WidgetTester tester, int side) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        height: 500,
        child: ViewRail(
          side: side,
          rail: VRegistry.instance.valueAt(VRegistry.keyOf(VComposite()..id = side == SWT.LEFT ? 50 : 51)) as VControl?,
        ),
      ),
    ),
  ));
  await tester.pump();
}

Finder _icon(String view) => find.byTooltip(view);

List<Map<String, dynamic>> _shows() =>
    sentEvents().where((e) => e.$1.endsWith('ViewRails/show')).map((e) => e.$2).toList();

List<String> _showTargets() =>
    sentEvents().where((e) => e.$1.endsWith('ViewRails/show')).map((e) => e.$1).toList();

void main() {
  setUp(() {
    ViewRails.start();
    ViewRails.activeFolder.value = null;
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });

  tearDown(() {
    VRegistry.instance.clear();
    ViewRails.current.value = ViewRails.empty;
  });

  testWidgets('the left rail lists the left stacks at its top and the stacks under the editor at its bottom',
      (tester) async {
    _deliver();
    await _pumpRail(tester, SWT.LEFT);

    for (final view in ['Package Explorer', 'Git', 'Problems', 'Console']) {
      expect(_icon(view), findsOneWidget, reason: '$view is on the left rail');
    }
    expect(_icon('Outline'), findsNothing, reason: 'Outline is on the right rail');
    expect(tester.getCenter(_icon('Git')).dy, lessThan(tester.getCenter(_icon('Problems')).dy));
    expect(tester.getCenter(_icon('Problems')).dy, greaterThan(400),
        reason: 'the bottom stacks sit at the bottom end of the rail');
  });

  testWidgets('the right rail lists the right stacks', (tester) async {
    _deliver();
    await _pumpRail(tester, SWT.RIGHT);

    expect(_icon('Outline'), findsOneWidget);
    expect(_icon('Package Explorer'), findsNothing);
  });

  testWidgets('a test finds each rail button by its view, under the rail anchor', (tester) async {
    TestAnchors.enabled = true;
    addTearDown(() => TestAnchors.enabled = false);
    _deliver();
    await _pumpRail(tester, SWT.LEFT);

    final nodes = TestAnchors.nodes([ViewRails.railAnchor]);
    expect([for (final n in nodes) n['text']], ['Package Explorer', 'Git', 'Problems', 'Console']);
    expect([for (final n in nodes) n['id']], [
      '${ViewRails.railAnchor}10',
      '${ViewRails.railAnchor}11',
      '${ViewRails.railAnchor}30',
      '${ViewRails.railAnchor}31',
    ]);
  });

  testWidgets('a press on a view that is not on screen shows it', (tester) async {
    _deliver();
    await _pumpRail(tester, SWT.LEFT);

    await tester.tap(_icon('Git'));
    await tester.pump();

    expect(_shows(), [
      {'detail': ViewRailAction.press, 'index': 1}
    ]);
  });

  testWidgets('a press on the active view is a press too: Java, which knows the active view, minimizes its stack',
      (tester) async {
    _deliver();
    ViewRails.activeFolder.value = 1;
    await _pumpRail(tester, SWT.LEFT);

    await tester.tap(_icon('Package Explorer'));
    await tester.pump();

    expect(_shows(), [
      {'detail': ViewRailAction.press, 'index': 0}
    ]);
  });

  testWidgets('a press on the selected view of a minimized stack shows it again', (tester) async {
    _deliver(minimized: 1);
    ViewRails.activeFolder.value = 1;
    await _pumpRail(tester, SWT.LEFT);

    await tester.tap(_icon('Package Explorer'));
    await tester.pump();

    expect(_shows(), [
      {'detail': ViewRailAction.press, 'index': 0}
    ]);
  });

  testWidgets('a right-click on a view\'s button asks Java for that view\'s menu, where it was clicked on the rail',
      (tester) async {
    _deliver();
    await _pumpRail(tester, SWT.LEFT);

    final at = tester.getCenter(_icon('Git'));
    await tester.tapAt(at, buttons: kSecondaryButton);
    await tester.pump();

    final rail = tester.getTopLeft(find.byType(ViewRail));
    expect(_shows(), [
      {'detail': ViewRailAction.viewMenu, 'index': 1, 'x': (at.dx - rail.dx).round(), 'y': (at.dy - rail.dy).round()}
    ]);
  });

  testWidgets('a right-click on the rail between its ends asks for the rail\'s menu, for the end it is nearer',
      (tester) async {
    _deliver();
    await _pumpRail(tester, SWT.LEFT);

    final rail = tester.getRect(find.byType(ViewRail));
    await tester.tapAt(Offset(rail.center.dx, rail.top + rail.height * 0.4), buttons: kSecondaryButton);
    await tester.tapAt(Offset(rail.center.dx, rail.top + rail.height * 0.6), buttons: kSecondaryButton);
    await tester.pump();

    expect([for (final e in _shows()) (e['detail'], e['index'])], [
      (ViewRailAction.railMenu, 0),
      (ViewRailAction.railMenu, 1),
    ]);
    expect(_showTargets(), everyElement(isNot(contains('CTabFolder'))),
        reason: 'the rail\'s menu is asked for on the rail, not on a stack');
  });

  testWidgets('the rails take the size the record names', (tester) async {
    _deliver(size: ViewRailSize.large);
    await _pumpRail(tester, SWT.LEFT);

    final theme = Theme.of(tester.element(find.byType(ViewRail))).extension<ViewRailsThemeExtension>()!;
    expect(tester.getSize(find.byType(ViewRail)).width, theme.railWidth(ViewRailSize.large));
    final button = find.ancestor(of: find.text('G'), matching: find.byType(Container)).first;
    expect(tester.getSize(button).width, theme.buttonSizes[ViewRailSize.large],
        reason: 'Git has no image: its button shows its initial');
  });

  testWidgets('a view header closes from its controls, after minimize and maximize, not from its title',
      (tester) async {
    _deliver();
    final folder = VRegistry.instance.valueAt(VRegistry.keyOf(_folder(3, const []))) as VCTabFolder
      ..minimizeVisible = true
      ..maximizeVisible = true;
    for (final item in folder.items!.whereType<VCTabItem>()) {
      item.showClose = true;
    }
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 600, height: 240, child: CTabFolderSwt(value: folder)),
    ));
    await tester.pump();
    while (tester.takeException() != null) {}

    expect(find.byIcon(Icons.close), findsOneWidget, reason: 'one close button, the header\'s');
    expect(tester.getCenter(find.byIcon(Icons.close)).dx,
        greaterThan(tester.getCenter(find.byIcon(Icons.fullscreen)).dx));
  });

  testWidgets('a listed view stack draws only its selected view', (tester) async {
    _deliver();
    final folder = VRegistry.instance.valueAt(VRegistry.keyOf(_folder(3, const []))) as VCTabFolder;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 600, height: 240, child: CTabFolderSwt(value: folder)),
    ));
    await tester.pump();
    while (tester.takeException() != null) {}

    expect(find.text('Console'), findsOneWidget, reason: 'the selected view titles the stack');
    expect(find.text('Problems'), findsNothing, reason: 'the rail lists the other views');
  });

  testWidgets('a stack the rails do not list keeps its tabs', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 240,
        child: CTabFolderSwt(value: _folder(9, ['Editor A', 'Editor B'])),
      ),
    ));
    await tester.pump();
    while (tester.takeException() != null) {}

    expect(find.text('Editor A'), findsOneWidget);
    expect(find.text('Editor B'), findsOneWidget);
  });

  testWidgets('a view header shows its controls while the pointer is anywhere on the view', (tester) async {
    _deliver();
    final folder = VRegistry.instance.valueAt(VRegistry.keyOf(_folder(3, const []))) as VCTabFolder
      ..minimizeVisible = true
      ..maximizeVisible = true;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 600, height: 240, child: CTabFolderSwt(value: folder)),
      ),
    ));
    await tester.pump();
    while (tester.takeException() != null) {}
    bool shown() => tester
        .widgetList<AnimatedOpacity>(find.ancestor(of: find.byIcon(Icons.fullscreen), matching: find.byType(AnimatedOpacity)))
        .any((o) => o.opacity == 1);
    expect(shown(), isFalse);

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(300, 200));
    await tester.pumpAndSettle();
    expect(shown(), isTrue, reason: 'the pointer is on the view, below its header');

    await mouse.moveTo(const Offset(700, 200));
    await tester.pumpAndSettle();
    expect(shown(), isFalse, reason: 'the pointer left the view');
    await mouse.removePointer();
  });

  testWidgets('header controls wider than a view on the right rail run past its left edge', (tester) async {
    _deliver();
    final toolbar = VRegistry.instance.register(VComposite()
      ..id = 77
      ..bounds = (VRectangle()
        ..x = -90
        ..y = 0
        ..width = 150
        ..height = 22)) as VComposite;
    final folder = VRegistry.instance.valueAt(VRegistry.keyOf(_folder(2, const []))) as VCTabFolder
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 100
        ..height = 240)
      ..topRight = toolbar
      ..minimizeVisible = true;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: Padding(
          padding: const EdgeInsets.only(left: 400),
          child: SizedBox(width: 100, height: 240, child: CTabFolderSwt(value: folder)),
        ),
      ),
    ));
    await tester.pump();
    while (tester.takeException() != null) {}

    final drawn = tester.getRect(find.byType(ToolbarComposite));
    expect(drawn.width, 150, reason: 'the toolbar keeps its full width');
    expect(drawn.left, lessThan(400), reason: 'it runs past the view\'s left edge');
    expect(tester.getRect(find.byIcon(Icons.minimize)).right, lessThanOrEqualTo(500),
        reason: 'the row ends at the view\'s right edge');

  });

  testWidgets('hidden header controls that run past a view leave its title to the pointer', (tester) async {
    _deliver();
    final toolbar = VRegistry.instance.register(VComposite()
      ..id = 77
      ..bounds = (VRectangle()
        ..x = -60
        ..y = 0
        ..width = 250
        ..height = 22)) as VComposite;
    final folder = VRegistry.instance.valueAt(VRegistry.keyOf(_folder(2, const []))) as VCTabFolder
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 240
        ..height = 240)
      ..topRight = toolbar
      ..minimizeVisible = true;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: Padding(
          padding: const EdgeInsets.only(left: 400),
          child: SizedBox(width: 240, height: 240, child: CTabFolderSwt(value: folder)),
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}
    final title = tester.getCenter(find.text('Outline'));
    expect(tester.getRect(find.byType(ToolbarComposite)).contains(title), isTrue,
        reason: 'the toolbar runs over the title');

    final overlay = tester.renderObject(find.byType(CompositedTransformFollower));
    final top = tester.hitTestOnBinding(title).path.map((entry) => entry.target).whereType<RenderObject>().first;
    var inOverlay = false;
    for (RenderObject? node = top; node != null; node = node.parent) {
      if (identical(node, overlay)) inOverlay = true;
    }
    expect(inOverlay, isFalse);
  });

  testWidgets('a rail keeps its room on the window side and little on the views side', (tester) async {
    _deliver();
    await _pumpRail(tester, SWT.LEFT);
    final theme = Theme.of(tester.element(find.byType(ViewRail))).extension<ViewRailsThemeExtension>()!;
    final rail = tester.getRect(find.byType(ViewRail));
    final button = tester.getRect(find.ancestor(of: find.text('G'), matching: find.byType(Container)).first);

    expect(button.left - rail.left, theme.outerPaddings[ViewRailSize.medium]);
    expect(rail.right - button.right, theme.innerPaddings[ViewRailSize.medium]);
    expect(theme.innerPaddings[ViewRailSize.medium], lessThan(theme.outerPaddings[ViewRailSize.medium]));
  });

  Future<void> dragTo(WidgetTester tester, Finder from, Offset to) async {
    final gesture = await tester.startGesture(tester.getCenter(from), kind: PointerDeviceKind.mouse);
    await tester.pump();
    for (final t in [0.25, 0.5, 0.75, 1.0]) {
      await gesture.moveTo(Offset.lerp(tester.getCenter(from), to, t)!);
      await tester.pump();
    }
    await gesture.up();
    await tester.pump();
  }

  List<Map<String, dynamic>> moves() => [
        for (final e in _shows())
          if (e['detail'] == ViewRailAction.move) e,
      ];

  testWidgets('a button dropped on the upper half of another goes before it in that stack', (tester) async {
    _deliver();
    await _pumpRail(tester, SWT.LEFT);
    final target = tester.getRect(_icon('Package Explorer'));

    await dragTo(tester, _icon('Git'), target.topCenter + const Offset(0, 4));

    expect(moves(), [
      {'detail': ViewRailAction.move, 'index': 1, 'start': ViewRailPlace.leftTop, 'x': 1, 'end': 0},
    ]);
  });

  testWidgets('a button dropped past the last one of a rail end goes last there, as Move To does', (tester) async {
    _deliver();
    await _pumpRail(tester, SWT.LEFT);
    final problems = tester.getRect(_icon('Problems'));

    await dragTo(tester, _icon('Git'), problems.topCenter - const Offset(0, 16));

    expect(moves(), [
      {'detail': ViewRailAction.move, 'index': 1, 'start': ViewRailPlace.leftBottom, 'x': 0, 'end': -1},
    ]);
  });

  testWidgets('a button dropped anywhere on the empty lower half of a rail goes to its bottom end', (tester) async {
    _deliver();
    await _pumpRail(tester, SWT.RIGHT);
    final rail = tester.getRect(find.byType(ViewRail));

    await dragTo(tester, _icon('Outline'), Offset(rail.center.dx, rail.top + rail.height * 0.6));

    expect(moves(), [
      {'detail': ViewRailAction.move, 'index': 0, 'start': ViewRailPlace.rightBottom, 'x': 0, 'end': -1},
    ]);
  });

  testWidgets('a button dropped on the empty upper half of a rail goes last at its top end', (tester) async {
    _deliver();
    await _pumpRail(tester, SWT.LEFT);
    final rail = tester.getRect(find.byType(ViewRail));

    await dragTo(tester, _icon('Problems'), Offset(rail.center.dx, rail.top + rail.height * 0.4));

    expect(moves(), [
      {'detail': ViewRailAction.move, 'index': 0, 'start': ViewRailPlace.leftTop, 'x': 0, 'end': -1},
    ]);
  });
}
