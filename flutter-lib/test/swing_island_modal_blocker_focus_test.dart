// A modal Swing dialog drawn in a Shell inside the Display's window, beside the invisible modal Shell
// its application blocks SWT with. The client moves the keyboard into the blocker on its own, as into
// any Shell Java activates; the dialog's island takes it on Java's word, whichever of the two arrives
// first, so a key goes to the dialog's mirror and not, through the Display, to the blocker.

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
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

import 'support/channels.dart' as channels;

// Ids of their own per test: what a widget last held stays under its id.
int _base = 5300;
int get displayId => _base;
int get mainId => _base + 1;
int get fieldId => _base + 2;
int get blockerId => _base + 3;
int get dialogId => _base + 4;
int get islandId => _base + 5;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _main() => VShell()
  ..swt = 'Shell'
  ..id = mainId
  ..style = SWT.SHELL_TRIM
  ..text = 'workbench'
  ..bounds = _rect(0, 0, 800, 600)
  ..children = [
    VText()
      ..swt = 'Text'
      ..id = fieldId
      ..style = SWT.SINGLE
      ..enabled = true
      ..visible = true
      ..editable = true
      ..bounds = _rect(10, 10, 200, 24)
  ];

VShell _blocker() => VShell()
  ..swt = 'Shell'
  ..id = blockerId
  ..style = SWT.APPLICATION_MODAL
  ..alpha = 0
  ..bounds = _rect(0, 0, 2, 2);

VShell _dialog() => VShell()
  ..swt = 'Shell'
  ..id = dialogId
  ..style = SWT.DIALOG_TRIM | SWT.RESIZE
  ..text = 'Background Color'
  ..bounds = _rect(166, 120, 400, 300)
  ..children = [
    VComposite()
      ..swt = 'SwingIsland'
      ..id = islandId
      ..style = SWT.EMBEDDED
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 400, 300)
  ];

VDisplay _display(List<VShell> shells, int activeShellId) => VDisplay()
  ..swt = 'Display'
  ..id = displayId
  ..shells = shells
  ..activeShellId = activeShellId;

/// Stands in for swing-evolve's mirror: it reports its window's focus to Java as it attaches, and as it
/// moves from there, having assumed the window focused when shown.
class _Mirror extends StatefulWidget {
  const _Mirror(this.window);

  final SwingIslandWindow window;

  static final List<bool> reports = [];
  static final List<LogicalKeyboardKey> keys = [];

  @override
  State<_Mirror> createState() => _MirrorState();
}

class _MirrorState extends State<_Mirror> {
  bool _focused = true;

  @override
  void initState() {
    super.initState();
    HardwareKeyboard.instance.addHandler(_onKey);
    widget.window.focused.addListener(_report);
    _report();
  }

  @override
  void dispose() {
    widget.window.focused.removeListener(_report);
    HardwareKeyboard.instance.removeHandler(_onKey);
    super.dispose();
  }

  void _report() {
    final focused = widget.window.focused.value;
    if (focused == _focused) return;
    _focused = focused;
    _Mirror.reports.add(focused);
  }

  bool _onKey(KeyEvent event) {
    if (!widget.window.focused.value) return false;
    if (event is KeyDownEvent) _Mirror.keys.add(event.logicalKey);
    return true;
  }

  @override
  Widget build(BuildContext context) => const SizedBox.expand();
}

List<String> _sentAbout(String event) => [
      for (final frame in EquoCommService.commForTesting.sentFrames)
        if (EquoCommBase.decodeFrame(frame).$1.contains(event)) EquoCommBase.decodeFrame(frame).$1
    ];

Future<void> _settle(WidgetTester tester) async {
  for (var i = 0; i < 4; i++) {
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 50));
  }
}

Future<void> _openMain(WidgetTester tester) async {
  await tester.pumpWidget(
      EvolveApp(theme: ThemeMode.light, contentWidget: DisplaySwt(value: _display([_main()], mainId))));
  await _settle(tester);
}

void _push(List<VShell> shells, int active) =>
    VRegistry.instance.deliverHolderForTesting('Display', displayId, _display(shells, active));

void _answerIsland() => EquoCommService.commForTesting.receiveBinary(channels.frame(
    'SwingIsland/$islandId/swingIsland', TaggedValues.encode({'windowId': 12, 'port': 8765, 'surface': true})));

Future<void> _escape(WidgetTester tester) async {
  EquoCommService.commForTesting.clearSentFrames();
  await tester.sendKeyEvent(LogicalKeyboardKey.escape);
  await tester.pumpAndSettle();
}

Future<void> _expectDialogHasTheKeyboard(WidgetTester tester) async {
  await _settle(tester);
  expect(tester.widget<_Mirror>(find.byType(_Mirror)).window.focused.value, isTrue);
  expect(keyboardClaimed, isTrue);
  expect(_Mirror.reports.lastOrNull ?? true, isTrue, reason: 'the last Java hears of its window is that it is focused');
  await _escape(tester);
  expect(_Mirror.keys, [LogicalKeyboardKey.escape]);
  expect(_sentAbout('Key/KeyDown'), isEmpty, reason: 'forwarded by the Display it would close the blocker');
}

void main() {
  setUp(() {
    _base += 20;
    FocusRequests.instance.reset();
    _Mirror.reports.clear();
    _Mirror.keys.clear();
    swingMirrorBuilder = (context, window) => _Mirror(window);
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });

  tearDown(() {
    swingMirrorBuilder = null;
    FocusRequests.instance.reset();
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
    VRegistry.instance.clear();
  });

  testWidgets('a blocker Java leaves active keeps the keyboard, and the dialog reports its window unfocused',
      (tester) async {
    await _openMain(tester);
    _push([_main(), _blocker()], blockerId);
    await _settle(tester);
    expect(_sentAbout('Shell/$blockerId/Shell/Activate'), isNotEmpty);
    _push([_main(), _blocker(), _dialog()], blockerId);
    await tester.pump();
    _answerIsland();
    await _settle(tester);

    expect(_Mirror.reports, [false]);
    expect(keyboardClaimed, isFalse);
    await _escape(tester);
    expect(_sentAbout('Key/KeyDown'), hasLength(1));
    expect(_Mirror.keys, isEmpty);
  });

  testWidgets('a dialog Java focuses takes the keyboard from a blocker that has it', (tester) async {
    await _openMain(tester);
    _push([_main(), _blocker()], blockerId);
    await _settle(tester);

    _push([_main(), _blocker(), _dialog()], dialogId);
    FocusRequests.instance.request(islandId);
    await tester.pump();
    _answerIsland();

    await _expectDialogHasTheKeyboard(tester);
  });

  testWidgets('a dialog Java focuses takes the keyboard from a blocker whose own focus is on its way',
      (tester) async {
    await _openMain(tester);
    _push([_main(), _blocker()], blockerId);
    await tester.pump();
    _push([_main(), _blocker(), _dialog()], dialogId);
    FocusRequests.instance.request(islandId);
    _answerIsland();

    await _expectDialogHasTheKeyboard(tester);
  });

  testWidgets('a dialog Java focuses long after it opened takes the keyboard from the blocker', (tester) async {
    await _openMain(tester);
    _push([_main(), _blocker()], blockerId);
    await _settle(tester);
    _push([_main(), _blocker(), _dialog()], blockerId);
    await tester.pump();
    _answerIsland();
    await _settle(tester);
    expect(keyboardClaimed, isFalse);

    _push([_main(), _blocker(), _dialog()], dialogId);
    FocusRequests.instance.request(islandId);

    await _expectDialogHasTheKeyboard(tester);
  });
}
