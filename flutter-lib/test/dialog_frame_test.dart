// The window's single dialog frame: a dialog opened from a dialog replaces what the frame shows,
// the one below stays mounted, the top one's header is a breadcrumb back, and an alert overlays the
// frame instead of joining it. Java decides each shell's presentation (DialogPresentationNativeTest).

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/messagebox.dart';
import 'package:swtflutter/src/impl/messagebox_evolve.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/dialog_frame.dart';
import 'package:swtflutter/src/theme/theme_settings/dialog_frame_theme_settings.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

const _dialog = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;

VShell _main() => VShell()
  ..id = 1
  ..style = SWT.SHELL_TRIM
  ..text = 'Workbench'
  ..visible = true
  ..bounds = _rect(0, 0, 1000, 700);

VShell _shell(int id, String title, int presentation) => VShell()
  ..id = id
  ..style = _dialog
  ..text = title
  ..visible = true
  ..presentation = presentation
  ..bounds = _rect(100, 80, 420 - id * 10, 300 - id * 10);

VDisplay _display(List<VShell> dialogs) => VDisplay()
  ..id = 900
  ..mainShellId = 1
  ..shells = [_main(), ...dialogs];

Future<void> _show(WidgetTester tester, List<VShell> dialogs) async {
  await tester.pumpWidget(EvolveApp(theme: ThemeMode.light, contentWidget: DisplaySwt(value: _display(dialogs))));
  await tester.pumpAndSettle();
}

var _generation = 0;

/// The display again with [dialogs], what is left once some have closed: a display built afresh,
/// since the pop walk lives in [dialogFramePopTarget], not in any one shell's state.
Future<void> _update(WidgetTester tester, List<VShell> dialogs) async {
  await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light, contentWidget: DisplaySwt(key: ValueKey(++_generation), value: _display(dialogs))));
  await tester.pumpAndSettle();
}

Finder _shellFinder(int id) => find.byWidgetPredicate((w) => w is ShellSwt && w.value.id == id, skipOffstage: false);

/// Whether shell [id] is mounted and drawn: what it presents is not under an Offstage.
bool _shown(int id) =>
    find.descendant(of: _shellFinder(id), matching: find.byType(DialogAppear)).evaluate().isNotEmpty;

/// The channels sent that name [part], in order.
List<String> _sent(String part) => EquoCommService.commForTesting.sentFrames
    .map((f) => EquoCommBase.decodeFrame(f).$1)
    .where((channel) => channel.contains(part))
    .toList();

/// The shell ids a Shell/Close was sent for, in order.
List<String> _closes() => EquoCommService.commForTesting.sentFrames
    .map((f) => EquoCommBase.decodeFrame(f).$1)
    .where((channel) => channel.contains('Close'))
    .toList();

void main() {
  setUp(() {
    freshClient();
    resetConfigFlags();
    applyConfigFlags(ConfigFlags()..modern_dialogs = true);
    dialogFramePopTarget.value = null;
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });
  tearDown(() {
    EquoCommBase.recordSentFrames = false;
    resetConfigFlags();
  });

  group('DialogFrame.of', () {
    test('only the stacking shell opened last is shown; the top one lists the stack', () {
      final frame = DialogFrame.of([
        _shell(2, 'Preferences', DialogPresentation.page),
        _shell(3, 'Add JRE', DialogPresentation.sheet),
        _shell(4, 'Not a JDK', DialogPresentation.alert),
      ], modern: true);
      expect(frame.tops.keys, [3]);
      expect(frame.hidden, {2});
      expect(frame.crumbsOf(3), const [FrameCrumb(2, 'Preferences'), FrameCrumb(3, 'Add JRE')]);
      expect(frame.crumbsOf(4), isEmpty, reason: 'an alert overlays the frame, it is not in it');
      expect(frame.isHidden(4), isFalse);
    });

    test('a shell left a window joins no frame, and the flag off makes none', () {
      final shells = [_shell(2, 'A', DialogPresentation.sheet), _shell(3, 'B', DialogPresentation.none)];
      expect(DialogFrame.of(shells, modern: true).hidden, isEmpty);
      expect(DialogFrame.of([_shell(2, 'A', DialogPresentation.sheet), _shell(3, 'B', DialogPresentation.sheet)], modern: false),
          same(DialogFrame.empty));
    });

    test('a crumb closes what is above it, and nothing once it is the top', () {
      const crumbs = [FrameCrumb(2, 'A'), FrameCrumb(3, 'B'), FrameCrumb(4, 'C')];
      final walk = PopTarget.to(2, crumbs);
      expect(walk.above, {3, 4});
      expect(closesForPopTarget(4, crumbs, walk), isTrue);
      expect(closesForPopTarget(2, const [], walk), isFalse);
      expect(closesForPopTarget(4, crumbs, null), isFalse);
      expect(closesForPopTarget(4, crumbs, const PopTarget(99, {4})), isFalse, reason: 'a target no longer in the stack stops the walk');
      expect(closesForPopTarget(5, const [...crumbs, FrameCrumb(5, 'D')], walk), isFalse,
          reason: 'a dialog opened after a refused close was not above the crumb, so it stays open');
    });
  });

  testWidgets('a dialog opened from a dialog replaces it in the frame, the one below kept mounted', (tester) async {
    await _show(tester, [_shell(2, 'Preferences', DialogPresentation.sheet), _shell(3, 'Add JRE', DialogPresentation.sheet)]);

    expect(_shown(3), isTrue);
    expect(_shown(2), isFalse, reason: 'the dialog below is not shown');
    expect(_shellFinder(2), findsOneWidget, reason: 'but stays mounted, with its state');
    expect(find.byKey(const ValueKey('dialogFrame.crumb.2')), findsOneWidget, reason: 'the breadcrumb names it');
    expect(find.byKey(const ValueKey('dialogFrame.back.3')), findsOneWidget);
  });

  testWidgets('Back closes the top dialog through its own close', (tester) async {
    await _show(tester, [_shell(2, 'Preferences', DialogPresentation.sheet), _shell(3, 'Add JRE', DialogPresentation.sheet)]);
    EquoCommService.commForTesting.clearSentFrames();

    await tester.tap(find.byKey(const ValueKey('dialogFrame.back.3')));
    await tester.pump();

    expect(_closes(), hasLength(1));
    expect(_closes().single, contains('3'));
  });

  testWidgets('a crumb closes the dialogs above it, top first, one at a time', (tester) async {
    final shells = [
      _shell(2, 'Preferences', DialogPresentation.sheet),
      _shell(3, 'Add JRE', DialogPresentation.sheet),
      _shell(4, 'Choose Directory', DialogPresentation.sheet),
    ];
    await _show(tester, shells);
    EquoCommService.commForTesting.clearSentFrames();

    await tester.tap(find.byKey(const ValueKey('dialogFrame.crumb.2')));
    await tester.pumpAndSettle();
    expect(_closes(), hasLength(1), reason: 'only the top closes; the next waits until it has');
    expect(_closes().single, contains('4'));

    await _update(tester, shells.sublist(0, 2));
    expect(_closes(), hasLength(2), reason: 'the new top closes itself on the way to the crumb');
    expect(_closes().last, contains('3'));

    await _update(tester, shells.sublist(0, 1));
    expect(_closes(), hasLength(2), reason: 'the crumb reached, the walk stops');
  });

  testWidgets('an alert overlays the frame: the top dialog stays shown under it', (tester) async {
    await _show(tester, [
      _shell(2, 'Add JRE', DialogPresentation.sheet),
      _shell(3, 'Not a JDK', DialogPresentation.alert),
    ]);
    expect(_shown(2), isTrue);
    expect(_shown(3), isTrue);
  });

  testWidgets('a presented dialog has no maximize and is centred in the window', (tester) async {
    await _show(tester, [_shell(2, 'Add JRE', DialogPresentation.sheet)..style = _dialog | SWT.MAX | SWT.RESIZE]);
    expect(find.byIcon(Icons.crop_square), findsNothing);
    final r = tester.getRect(_shellFinder(2));
    final page = tester.getRect(_shellFinder(1));
    expect(r.center.dx, moreOrLessEquals(page.center.dx, epsilon: 2));
  });

  group('progress', () {
    Future<void> showProgress(WidgetTester tester, List<VShell> dialogs) async {
      await tester.pumpWidget(EvolveApp(theme: ThemeMode.light, contentWidget: DisplaySwt(value: _display(dialogs))));
      await tester.pump();
      await tester.pump(const Duration(milliseconds: 700));
    }

    testWidgets('a progress dialog is a card in the window corner, not in the frame', (tester) async {
      await showProgress(tester, [_shell(2, 'Progress Information', DialogPresentation.progress)]);
      final card = tester.getRect(_shellFinder(2));
      final page = tester.getRect(_shellFinder(1));
      expect(page.right - card.right, moreOrLessEquals(16, epsilon: 3), reason: 'at the right edge');
      expect(page.bottom - card.bottom, moreOrLessEquals(16, epsilon: 3), reason: 'at the bottom edge');
      expect(DialogFrame.of([_shell(2, 'P', DialogPresentation.progress)], modern: true).tops, isEmpty);
    });

    testWidgets('the window is not dimmed, but its input is held while the operation runs', (tester) async {
      await showProgress(tester, [_shell(2, 'Progress Information', DialogPresentation.progress)]);
      expect(find.byKey(const ValueKey('dialogFrame.scrim')), findsNothing);
      expect(find.byKey(const ValueKey('dialogFrame.inputPaused')), findsOneWidget);
    });

    testWidgets('a dialog shown under it keeps its scrim', (tester) async {
      await showProgress(tester, [
        _shell(2, 'New Java Project', DialogPresentation.sheet),
        _shell(3, 'Progress Information', DialogPresentation.progress),
      ]);
      expect(find.byKey(const ValueKey('dialogFrame.scrim')), findsOneWidget);
      expect(_shown(2), isTrue);
    });

    testWidgets('the card counts the time the operation has taken', (tester) async {
      await showProgress(tester, [_shell(2, 'Progress Information', DialogPresentation.progress)]);
      expect(find.text('0:00'), findsOneWidget);
      await tester.pump(const Duration(seconds: 65));
      expect(find.text('1:05'), findsOneWidget);
    });
  });

  group('pages', () {
    // The editor area Java placed the page over, and the page under the strip the client draws.
    final host = _rect(200, 40, 560, 500);
    VShell page(int id, String title) => _shell(id, title, DialogPresentation.page)
      ..bounds = _rect(200, 74, 560, 466)
      ..pageHost = host
      ..pageStrip = 34;
    VShell ownedSheet(int id, String title, VShell owner) =>
        _shell(id, title, DialogPresentation.sheet)..pageOwner = owner;
    Rect hostRect() => const Rect.fromLTWH(200, 40, 560, 500);

    testWidgets('a page is drawn where Java placed it, under a tab naming it, the window not dimmed', (tester) async {
      await _show(tester, [page(2, 'New Java Project')]);
      expect(tester.getRect(_shellFinder(2)).topLeft, const Offset(200, 74));
      expect(find.byKey(const ValueKey('dialogFrame.pageTab.2')), findsOneWidget);
      expect(tester.getRect(find.byKey(const ValueKey('dialogFrame.pageStrip'))), hostRect());
      expect(find.byKey(const ValueKey('dialogFrame.scrim')), findsNothing);
    });

    testWidgets('a page keeps to the editor area whatever minimum size the dialog set', (tester) async {
      await _show(tester, [page(2, 'New Java Project')..minimumSize = (VPoint()..x = 900..y = 900)]);
      expect(tester.getRect(_shellFinder(2)).bottom, lessThanOrEqualTo(hostRect().bottom + 2));
    });

    testWidgets('a dialog the page opens is centred over the page, dimming only the page', (tester) async {
      final p = page(2, 'New Java Project');
      await _show(tester, [p, ownedSheet(3, 'Select Folder', p)]);
      expect(tester.getRect(find.byKey(const ValueKey('dialogFrame.scrim'))), hostRect());
      expect(tester.getRect(_shellFinder(3)).center.dx, moreOrLessEquals(hostRect().center.dx, epsilon: 2));
      expect(_shown(2), isTrue, reason: 'the page stays under the dialog it opened');
      expect(find.byKey(const ValueKey('dialogFrame.crumb.2')), findsOneWidget, reason: 'the page leads the breadcrumb');
    });

    testWidgets('of two pages the newer is shown; a tab brings the other back', (tester) async {
      await _show(tester, [page(2, 'New Java Project'), page(3, 'Preferences')]);
      expect(_shown(3), isTrue);
      expect(_shown(2), isFalse);
      await tester.tap(find.byKey(const ValueKey('dialogFrame.pageTab.2')));
      await tester.pumpAndSettle();
      expect(_shown(2), isTrue);
      expect(_shown(3), isFalse);
    });

    testWidgets('Editors puts the pages aside, and the return chip brings them back', (tester) async {
      await _show(tester, [page(2, 'New Java Project')]);
      await tester.tap(find.byKey(const ValueKey('dialogFrame.editors')));
      await tester.pumpAndSettle();
      expect(_shown(2), isFalse);
      expect(find.byKey(const ValueKey('dialogFrame.pageStrip')), findsNothing);
      await tester.tap(find.byKey(const ValueKey('dialogFrame.returnToPage')));
      await tester.pumpAndSettle();
      expect(_shown(2), isTrue);
    });

    testWidgets("the strip's toggle and a double click on the tab ask Java to maximize the editor area", (tester) async {
      await _show(tester, [page(2, 'New Java Project')..pageStrip = 34]);
      EquoCommService.commForTesting.clearSentFrames();
      await tester.tap(find.byKey(const ValueKey('dialogFrame.maximize')));
      await tester.pump();
      expect(_sent('DialogFrame/maximize'), hasLength(1));
      expect(find.byIcon(Icons.open_in_full), findsOneWidget, reason: 'not maximized yet: the toggle offers to');
      await _update(tester, [page(2, 'New Java Project')..pageStrip = 34..pageMaximized = true]);
      expect(find.byIcon(Icons.close_fullscreen), findsOneWidget, reason: 'maximized: the toggle offers to restore');
    });

    testWidgets("the strip follows the editor area when only the page's own record changes", (tester) async {
      await _show(tester, [page(2, 'New Java Project')..pageStrip = 34]);
      await deliverChange(
          page(2, 'New Java Project')
            ..pageStrip = 34
            ..pageHost = _rect(20, 40, 740, 500)
            ..seq = 20,
          keys: [WireKey.k_Shell_pageHost],
          base: 0);
      await tester.pumpAndSettle();
      expect(tester.getRect(find.byKey(const ValueKey('dialogFrame.pageStrip'))), const Rect.fromLTWH(20, 40, 740, 500));
    });

    testWidgets("a wizard's steps show in the strip, the current one named", (tester) async {
      await _show(tester, [
        page(2, 'New Plug-in Project')
          ..pageStrip = 34
          ..pageSteps = ['Plug-in Project', 'Content', 'Templates']
          ..pageStep = 1
      ]);
      expect(find.byKey(const ValueKey('dialogFrame.step.0')), findsOneWidget);
      expect(find.byKey(const ValueKey('dialogFrame.step.2')), findsOneWidget);
      expect(find.text('Content'), findsOneWidget);
      expect(find.text('Templates'), findsNothing, reason: 'only the current step is named; the others say so on hover');
    });

    testWidgets('a heading is drawn over the title area, the message on a strip of its kind', (tester) async {
      await _show(tester, [
        page(2, 'New Plug-in Project')
          ..pageStrip = 34
          ..pageHeading = 70
          ..pageTitle = 'Plug-in Project'
          ..pageMessage = 'Project name must be specified'
          ..pageMessageType = 3
      ]);
      final heading = find.byKey(const ValueKey('dialogFrame.heading.2'));
      expect(heading, findsOneWidget);
      expect(tester.getRect(heading).top, moreOrLessEquals(74, epsilon: 1));
      expect(tester.getRect(heading).height, moreOrLessEquals(70, epsilon: 1));
      expect(find.descendant(of: heading, matching: find.text('Plug-in Project')), findsOneWidget);
      expect(find.byKey(const ValueKey('dialogFrame.message.error')), findsOneWidget);
    });

    testWidgets('a page taller than the editor area is clipped to it and scrolls by moving the shell', (tester) async {
      await _show(tester, [page(2, 'New Plug-in Project')..pageStrip = 34..bounds = _rect(200, 74, 560, 900)]);
      expect(find.byKey(const ValueKey('dialogFrame.scrollThumb.2')), findsOneWidget);
      EquoCommService.commForTesting.clearSentFrames();
      final pointer = TestPointer(1, PointerDeviceKind.mouse);
      await tester.sendEventToBinding(pointer.hover(const Offset(400, 300)));
      await tester.sendEventToBinding(pointer.scroll(const Offset(0, 120)));
      await tester.pump();
      final moved = EquoCommService.commForTesting.sentFrames
          .where((f) => EquoCommBase.decodeFrame(f).$1.contains('Bounds'))
          .map((f) => EquoCommBase.decodeEvent(f).y)
          .toList();
      expect(moved, isNotEmpty);
      expect(moved.last, 74 - 120);
    });

    testWidgets("a page tab's close closes the page through its own close", (tester) async {
      await _show(tester, [page(2, 'New Java Project')]);
      EquoCommService.commForTesting.clearSentFrames();
      await tester.tap(find.byKey(const ValueKey('dialogFrame.pageClose.2')));
      await tester.pump();
      expect(_closes().single, contains('2'));
    });
  });

  testWidgets('an SWT message box is drawn as the same alert card as a JFace question', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Builder(
        builder: (context) => TextButton(
          onPressed: () => showMessageBoxDialog(
              context,
              VMessageBox()
                ..id = 5
                ..style = SWT.ICON_WARNING | SWT.YES | SWT.NO
                ..text = 'Delete'
                ..message = "Delete 'OrderService.java'?",
              5),
          child: const Text('open'),
        ),
      ),
    ));
    await tester.tap(find.text('open'));
    await tester.pumpAndSettle();
    final shape = tester.widget<Dialog>(find.byType(Dialog)).shape as RoundedRectangleBorder;
    expect(shape.borderRadius, BorderRadius.circular(dialogCardRadius));
    expect(find.descendant(of: find.byType(Dialog), matching: find.byIcon(Icons.warning)), findsOneWidget);
  });

  testWidgets('the flag off draws every dialog as a window, as before', (tester) async {
    applyConfigFlags(ConfigFlags()..modern_dialogs = false);
    await _show(tester, [_shell(2, 'Preferences', DialogPresentation.sheet), _shell(3, 'Add JRE', DialogPresentation.sheet)]);
    expect(find.descendant(of: _shellFinder(2), matching: find.text('Preferences')), findsOneWidget);
    expect(find.descendant(of: _shellFinder(3), matching: find.text('Add JRE')), findsOneWidget);
    expect(find.byType(DialogFrameHeader), findsNothing);
    expect(find.byKey(const ValueKey('dialogFrame.back.3')), findsNothing);
  });
}
