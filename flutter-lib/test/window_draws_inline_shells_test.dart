// A shell drawn inline belongs to the window its parent is drawn in. A window of its own is a client
// rooted at its shell: Java names the shells that client draws inside it in a WindowShells record, and
// has the Display's client leave them out (see display_windowed_shells_test.dart).

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tooltip.dart';
import 'package:swtflutter/src/gen/widgets.dart' as gen;
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/shell_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/display_theme_extension.dart';

import 'support/menu_shown_ack.dart';

const _viewport = Size(900, 600);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// The second workbench window, 300, 200 from the screen's origin.
VShell _window(int id) => VShell()
  ..swt = 'Shell'
  ..id = id
  ..style = SWT.SHELL_TRIM
  ..bounds = _rect(300, 200, _viewport.width.toInt(), _viewport.height.toInt())
  ..visible = true
  ..children = [
    VButton()
      ..swt = 'Button'
      ..id = id + 1
      ..style = SWT.PUSH
      ..text = 'Window content'
      ..bounds = _rect(10, 10, 160, 30),
  ];

VShell _dialog(int id, {int style = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, String text = 'Save changes?'}) =>
    VShell()
      ..swt = 'Shell'
      ..id = id
      ..style = style
      ..text = text
      ..bounds = _rect(400, 350, 320, 160)
      ..visible = true
      ..children = [
        VComposite()
          ..swt = 'Composite'
          ..id = id + 1
          ..style = SWT.NONE
          ..bounds = _rect(0, 0, 320, 160)
      ];

void main() {
  setUp(resetConfigFlags);
  tearDown(() {
    resetConfigFlags();
    VRegistry.instance.clear();
  });

  Future<void> pumpWindow(WidgetTester tester, VShell window) async {
    tester.view.physicalSize = _viewport;
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: gen.mapWidgetFromValue(window),
    ));
    await tester.pumpAndSettle();
  }

  /// Java telling the window rooted at [window] what it draws inside it.
  Future<void> drawInside(WidgetTester tester, VShell window, List<VShell> shells, {int? active}) async {
    VRegistry.instance.deliverHolderForTesting(
        'WindowShells',
        window.id!,
        VWindowShells()
          ..shells = shells
          ..activeShellId = active);
    await tester.pumpAndSettle();
  }

  Finder overlay(WidgetTester tester) {
    final theme = Theme.of(tester.element(find.byType(Scaffold))).extension<DisplayThemeExtension>()!;
    return find.byWidgetPredicate((w) => w is ColoredBox && w.color == theme.modalOverlayColor);
  }

  testWidgets('a modal of this window is drawn in it, over what it blocks', (tester) async {
    final window = _window(5100);
    await pumpWindow(tester, window);
    expect(find.text('Window content'), findsOneWidget);
    expect(overlay(tester), findsNothing);

    await drawInside(tester, window, [_dialog(5200)]);

    expect(find.text('Save changes?'), findsOneWidget);
    expect(find.byType(FloatingShellChromeScope), findsOneWidget);
    expect(overlay(tester), findsOneWidget, reason: 'the overlay is what keeps the window blocked');
    expect(find.text('Window content').hitTestable(), findsNothing);
  });

  testWidgets('its bounds are a screen position, and this window is not at the screen origin', (tester) async {
    final window = _window(5110);
    await pumpWindow(tester, window);

    await drawInside(tester, window, [_dialog(5210)]);

    final title = tester.getTopLeft(find.text('Save changes?'));
    expect(title.dx, inInclusiveRange(100, 120));
    expect(title.dy, inInclusiveRange(150, 180));
  });

  testWidgets('a closed dialog leaves the window as it was', (tester) async {
    final window = _window(5120);
    await pumpWindow(tester, window);
    await drawInside(tester, window, [_dialog(5220)]);

    await drawInside(tester, window, const []);

    expect(find.text('Save changes?'), findsNothing);
    expect(overlay(tester), findsNothing);
    expect(find.text('Window content').hitTestable(), findsOneWidget);
  });

  testWidgets('a dialog that comes and goes keeps the state of the window behind it', (tester) async {
    final window = _window(5130);
    await pumpWindow(tester, window);
    final content = tester.element(find.text('Window content'));

    await drawInside(tester, window, [_dialog(5230)]);
    expect(tester.element(find.text('Window content')), same(content));
    await drawInside(tester, window, const []);
    expect(tester.element(find.text('Window content')), same(content));
  });

  testWidgets('a modeless pane dims nothing', (tester) async {
    final window = _window(5140);
    await pumpWindow(tester, window);

    await drawInside(tester, window, [_dialog(5240, style: SWT.DIALOG_TRIM, text: 'Find')]);

    expect(find.text('Find'), findsOneWidget);
    expect(overlay(tester), findsNothing);
    expect(find.text('Window content').hitTestable(), findsOneWidget);
  });

  Future<void> showTooltips(WidgetTester tester, VShell window, List<VToolTip> tooltips) async {
    VRegistry.instance
        .deliverHolderForTesting('ShellPopups', window.id!, VShellPopups()..tooltips = tooltips);
    await tester.pumpAndSettle();
  }

  VToolTip tip(int id, int x, int y) => VToolTip()
    ..swt = 'ToolTip'
    ..id = id
    ..style = SWT.BALLOON
    ..message = 'Saved'
    ..location = (VPoint()
      ..x = x
      ..y = y)
    ..visible = true;

  testWidgets('a tooltip of this window is drawn in it, at its screen position', (tester) async {
    final window = _window(5160);
    await pumpWindow(tester, window);

    await showTooltips(tester, window, [tip(5260, 360, 250)]);

    final at = tester.getTopLeft(find.text('Saved'));
    expect(at.dx, inInclusiveRange(60, 90));
    expect(at.dy, inInclusiveRange(50, 80));
    expect(find.text('Window content').hitTestable(), findsOneWidget);

    await showTooltips(tester, window, const []);
    expect(find.text('Saved'), findsNothing);
  });

  testWidgets('a tooltip over a dialog of this window is not covered by it', (tester) async {
    final window = _window(5170);
    await pumpWindow(tester, window);
    await drawInside(tester, window, [_dialog(5270)]);

    await showTooltips(tester, window, [tip(5280, 450, 400)]);

    expect(find.text('Saved').hitTestable(), findsOneWidget);
  });

  testWidgets('a tooltip of another window draws nothing here', (tester) async {
    final window = _window(5180);
    await pumpWindow(tester, window);

    await showTooltips(tester, _window(5181), [tip(5290, 360, 250)]);

    expect(find.text('Saved'), findsNothing);
  });

  testWidgets('a tooltip over a dialog of the Display is not covered by it', (tester) async {
    tester.view.physicalSize = _viewport;
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
          value: VDisplay()
            ..id = 5300
            ..shells = [_window(5310), _dialog(5320)]
            ..mainShellId = 5310
            ..tooltips = [tip(5330, 450, 400)]),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Saved').hitTestable(), findsOneWidget);
  });

  testWidgets('a popup opened over a dialog of this window leaves the window inactive', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() {
      EquoCommBase.recordSentFrames = false;
      EquoCommService.commForTesting.clearSentFrames();
    });
    final window = _window(5190);
    await pumpWindow(tester, window);
    await drawInside(tester, window, [_dialog(5290)], active: 5290);
    final dialogScope = FocusScope.of(tester.element(find.text('Save changes?')));
    dialogScope.requestFocus();
    await tester.pumpAndSettle();
    EquoCommService.commForTesting.clearSentFrames();

    VRegistry.instance.deliverHolderForTesting(
        'ShellPopups',
        window.id!,
        VShellPopups()
          ..popups = [
            VMenu()
              ..id = 5295
              ..style = SWT.POP_UP
              ..enabled = true
              ..visible = true
              ..location = (VPoint()
                ..x = 450
                ..y = 400)
              ..items = [
                VMenuItem()
                  ..id = 5296
                  ..style = SWT.PUSH
                  ..enabled = true
                  ..text = 'Copy'
              ]
          ]);
    await tester.pumpAndSettle();
    await ackMenuShown(tester, 5295);

    expect(find.text('Copy'), findsOneWidget);
    final channels = EquoCommService.commForTesting.sentFrames.map((f) => EquoCommBase.decodeFrame(f).$1);
    expect(channels.where((c) => c.endsWith('ctivate')), isEmpty);
  });

  testWidgets('a record for another window draws nothing here', (tester) async {
    final window = _window(5150);
    await pumpWindow(tester, window);

    await drawInside(tester, _window(5151), [_dialog(5250)]);

    expect(find.text('Save changes?'), findsNothing);
  });
}
