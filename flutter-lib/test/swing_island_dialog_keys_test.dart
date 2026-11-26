// The Escape of a focused Swing island stays in it. In a dialog Shell that is the root of its own
// window no Display is mounted and the Shell forwards the Escape itself, so one that closes a Swing
// popup would also run SWT's Escape traverse and close the dialog. The guards pin what the island
// leaves alone, in that client and in the whole tree under a Display: its other keys.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/custom/swing_island_evolve.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

import 'support/channels.dart' as channels;

const int displayId = 4799;
const int shellId = 4800;
const int hostId = 4801;
const int islandId = 4802;
const int buttonId = 4803;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VComposite _host({bool withButton = false}) => VComposite()
  ..id = hostId
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 380, 240)
  ..children = [
    VComposite()
      ..swt = 'SwingIsland'
      ..id = islandId
      ..style = SWT.EMBEDDED
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 380, 200),
    if (withButton)
      VButton()
        ..id = buttonId
        ..style = SWT.PUSH
        ..text = 'Beside'
        ..enabled = true
        ..visible = true
        ..bounds = _rect(0, 205, 120, 30),
  ];

Widget _dialog({bool withButton = false}) => ShellSwt(
    value: VShell()
      ..id = shellId
      ..style = SWT.DIALOG_TRIM
      ..text = 'Dialog'
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 400, 300)
      ..children = [_host(withButton: withButton)]);

Widget _underDisplay() => Stack(children: [
      Positioned.fill(child: DisplaySwt(value: VDisplay()..id = displayId)),
      Positioned.fill(child: CompositeSwt<VComposite>(value: _host(withButton: true))),
    ]);

/// Stands in for swing-evolve's mirror, which takes keys from a HardwareKeyboard handler of its own
/// and answers there whether the platform sees them too.
class _Mirror extends StatefulWidget {
  const _Mirror(this.window);

  final SwingIslandWindow window;

  /// The answer; false is the mirror's while a Swing modal blocks its window.
  static bool takesKeys = true;

  static final List<LogicalKeyboardKey> keys = [];

  @override
  State<_Mirror> createState() => _MirrorState();
}

class _MirrorState extends State<_Mirror> {
  @override
  void initState() {
    super.initState();
    HardwareKeyboard.instance.addHandler(_onKey);
  }

  @override
  void dispose() {
    HardwareKeyboard.instance.removeHandler(_onKey);
    super.dispose();
  }

  bool _onKey(KeyEvent event) {
    if (!widget.window.focused.value || !_Mirror.takesKeys) return false;
    if (event is KeyDownEvent) _Mirror.keys.add(event.logicalKey);
    return true;
  }

  @override
  Widget build(BuildContext context) => const SizedBox.expand();
}

List<String> _sent() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .toList();

Future<void> _focusIsland(WidgetTester tester, Widget content, {bool surface = false}) async {
  await tester.pumpWidget(EvolveApp(theme: ThemeMode.light, contentWidget: content));
  await tester.pump();
  EquoCommService.commForTesting.receiveBinary(
    channels.frame('SwingIsland/$islandId/swingIsland',
        TaggedValues.encode({'windowId': 3, 'port': 8765, if (surface) 'surface': true})),
  );
  await tester.pumpAndSettle();
  expect(find.byType(_Mirror), findsOneWidget);
  await tester.tapAt(tester.getCenter(find.byType(_Mirror)));
  await tester.pumpAndSettle();
  expect(keyboardClaimed, isTrue, reason: 'the press focuses the island');
  EquoCommService.commForTesting.clearSentFrames();
}

void main() {
  setUp(() {
    _Mirror.takesKeys = true;
    _Mirror.keys.clear();
    swingMirrorBuilder = (context, window) => _Mirror(window);
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });

  tearDown(() {
    swingMirrorBuilder = null;
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
    VRegistry.instance.clear();
  });

  for (final modifier in [null, LogicalKeyboardKey.shiftLeft, LogicalKeyboardKey.controlLeft]) {
    testWidgets(
        '${modifier == null ? 'an Escape' : 'a ${modifier.keyLabel}+Escape'} in a focused island, held and '
        'released, reaches no SWT control of the dialog', (tester) async {
      await _focusIsland(tester, _dialog());

      if (modifier != null) await tester.sendKeyDownEvent(modifier);
      final handled = await tester.sendKeyDownEvent(LogicalKeyboardKey.escape);
      await tester.sendKeyRepeatEvent(LogicalKeyboardKey.escape);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.escape);
      if (modifier != null) await tester.sendKeyUpEvent(modifier);
      await tester.pumpAndSettle();

      expect(_Mirror.keys, contains(LogicalKeyboardKey.escape));
      expect(
        _sent(),
        isEmpty,
        reason: 'a KeyDown ESC on the island or the Shell runs SWT\'s Escape traverse, which closes '
            'the dialog the island sits in',
      );
      expect(handled, isTrue, reason: 'the mirror took the key');
    });
  }

  testWidgets('an Escape in the Shell of a Swing secondary window does not reach the Shell', (tester) async {
    await _focusIsland(tester, _dialog(), surface: true);

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();

    expect(_sent(), isEmpty, reason: 'Java hands the Shell\'s close to Swing as the window\'s close');
  });

  testWidgets(
      'an Escape the mirror leaves reaches no SWT control of the dialog, and the platform no more than '
      'the Shell lets it', (tester) async {
    await _focusIsland(tester, _dialog());
    _Mirror.takesKeys = false;

    final pressHandled = await tester.sendKeyDownEvent(LogicalKeyboardKey.escape);
    final releaseHandled = await tester.sendKeyUpEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();

    expect(_sent(), isEmpty);
    expect(pressHandled, isTrue, reason: 'the Shell answers a press as handled');
    expect(releaseHandled, isFalse, reason: 'and leaves the release');
  });

  // From here on, what the island's onKeyEvent must leave as it is: each holds without it too.

  for (final underDisplay in [false, true]) {
    final client = underDisplay ? 'under a Display' : 'in a dialog of its own window';

    testWidgets('guard: a letter, Enter, Space, Down and F10 in a focused island $client send nothing to Java',
        (tester) async {
      await _focusIsland(tester, underDisplay ? _underDisplay() : _dialog(withButton: true));

      for (final key in [
        LogicalKeyboardKey.keyA,
        LogicalKeyboardKey.enter,
        LogicalKeyboardKey.space,
        LogicalKeyboardKey.arrowDown,
        LogicalKeyboardKey.f10,
      ]) {
        await tester.sendKeyEvent(key);
        await tester.pumpAndSettle();
        expect(_sent(), isEmpty, reason: key.keyLabel);
      }
      expect(keyboardClaimed, isTrue);
    });

    for (final shift in [false, true]) {
      testWidgets('guard: ${shift ? 'Shift+Tab' : 'Tab'} $client moves the focus out of the island', (tester) async {
        await _focusIsland(tester, underDisplay ? _underDisplay() : _dialog(withButton: true));

        if (shift) await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
        await tester.sendKeyEvent(LogicalKeyboardKey.tab);
        await tester.pumpAndSettle();

        expect(_Mirror.keys, contains(LogicalKeyboardKey.tab), reason: 'the mirror has it first');
        expect(_sent(), contains('SwingIsland/$islandId/Focus/FocusOut'));
        expect(keyboardClaimed, isFalse);

        if (underDisplay) {
          expect(_sent(), contains('Button/$buttonId/Focus/FocusIn'));
          expect(_sent(), isNot(contains('Display/$displayId/Key/KeyDown')));
          EquoCommService.commForTesting.clearSentFrames();

          await tester.sendKeyEvent(LogicalKeyboardKey.tab);
          await tester.pumpAndSettle();
          expect(
            _sent(),
            contains('Display/$displayId/Key/KeyDown'),
            reason: 'the application\'s key bindings see the second press',
          );
        }
        if (shift) await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
      });
    }
  }

  testWidgets('guard: an Escape under a Display sends nothing to Java', (tester) async {
    await _focusIsland(tester, _underDisplay());

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();

    expect(_sent(), isEmpty);
    expect(keyboardClaimed, isTrue);
  });
}
