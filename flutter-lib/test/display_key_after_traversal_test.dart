// A Tab that traverses moves the focus while its KeyDown is handled, natively, so its KeyUp goes to
// the control it focused. Here the client moves the focus once Java answers the Traverse, and Java
// sends each key to the control focused when the key arrives: a release forwarded before that
// answer reached the control the Tab left.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';
import 'package:swtflutter/src/impl/utils/traversal_order.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/batch.dart';
import 'support/channels.dart' as channels;

const int _displayId = 7;
const int _firstId = 51;
const int _secondId = 52;
const int _thirdId = 53;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VText _text(int id, String text, int y) => VText()
  ..id = id
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = text
  ..visible = true
  ..enabled = true
  ..bounds = _rect(0, y, 120, 24);

VButton _button(int id, String text, int y) => VButton()
  ..id = id
  ..style = SWT.PUSH
  ..text = text
  ..visible = true
  ..enabled = true
  ..bounds = _rect(0, y, 120, 28);

void _deliver(String actionId, Object payload) {
  EquoCommService.commForTesting.receiveBinary(channels.frame(actionId, TaggedValues.encode(payload)));
}

Future<void> _pumpShell(WidgetTester tester, List<VControl> children, {bool forceLive = true}) async {
  final app = EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
        value: VDisplay()
          ..id = _displayId
          ..mainShellId = 1001
          ..shells = [
            VShell()
              ..id = 1001
              ..style = SWT.SHELL_TRIM
              ..text = 'Main'
              ..visible = true
              ..bounds = _rect(0, 0, 400, 300)
              ..children = children,
          ],
      ),
  );
  await tester.pumpWidget(forceLive ? ForceLive(child: app) : app);
  await tester.pumpAndSettle();
}

/// Three Buttons in a Display, the first one focused by a click, with a TraverseListener in Java
/// that could veto a Tab: the client waits for Java's answer before it moves the focus.
Future<void> _pumpButtonsWithVetoableTraverse(WidgetTester tester) async {
  await _pumpShell(tester, [
    _button(_firstId, 'First', 0),
    _button(_secondId, 'Second', 30),
    _button(_thirdId, 'Third', 60),
  ]);
  receiveTagged('swt.evolve.focusHolder', {'id': _firstId});
  await tester.tap(find.text('First'));
  await tester.pumpAndSettle();
  _deliver('Display/$_displayId/traverse/vetoable', {'value': true});
  await tester.pump();

  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  EquoCommService.commForTesting.clearSentFrames();
}

bool _hasFocus(WidgetTester tester, int id) {
  final primary = FocusManager.instance.primaryFocus?.context;
  if (primary == null) return false;
  final button = tester.element(
      find.byWidgetPredicate((w) => (w is ButtonSwt && w.value.id == id) || (w is TextSwt && w.value.id == id)));
  var inside = primary == button;
  primary.visitAncestorElements((e) {
    if (e == button) inside = true;
    return !inside;
  });
  return inside;
}

/// The keys and FocusIns sent to Java, in order.
List<String> _sent() => EquoCommService.commForTesting.sentFrames
    .map((f) => EquoCommBase.decodeFrame(f).$1)
    .where((name) => name.contains('/Key/') || name.endsWith('/FocusIn'))
    .toList();

void main() {
  setUp(() {
    TraversalOrder.reset();
    resetConfigFlags();
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
    FocusRequests.listen();
  });
  tearDown(() {
    resetConfigFlags();
    FocusRequests.instance.reset();
  });

  testWidgets('a Tab released before Java answers its Traverse reaches Java after the focus it moves',
      (tester) async {
    await _pumpButtonsWithVetoableTraverse(tester);

    await tester.sendKeyDownEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.tab);
    expect(_hasFocus(tester, _secondId), isFalse, reason: 'held while Java is thinking');

    _deliver('Display/$_displayId/traverse/verdict', {'doit': true});
    await tester.pumpAndSettle();

    expect(_hasFocus(tester, _secondId), isTrue, reason: 'Java allowed the traversal');
    expect(_sent(), [
      'Display/$_displayId/Key/KeyDown',
      'Button/$_secondId/Focus/FocusIn',
      'Display/$_displayId/Key/KeyUp',
    ], reason: 'Java sends the KeyUp to the control focused when it arrives');
  });

  testWidgets('a second Tab reaches Java once the first is answered, so Java can answer it too',
      (tester) async {
    await _pumpButtonsWithVetoableTraverse(tester);

    for (var i = 0; i < 2; i++) {
      await tester.sendKeyDownEvent(LogicalKeyboardKey.tab);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.tab);
    }
    _deliver('Display/$_displayId/traverse/verdict', {'doit': true});
    await tester.pump();
    expect(_sent().where((name) => name.endsWith('/KeyDown')), hasLength(2),
        reason: 'the second Tab waits for the first answer only, not for its own: ${_sent()}');

    _deliver('Display/$_displayId/traverse/verdict', {'doit': true});
    await tester.pumpAndSettle();

    expect(_hasFocus(tester, _thirdId), isTrue);
    expect(_sent(), [
      'Display/$_displayId/Key/KeyDown',
      'Button/$_secondId/Focus/FocusIn',
      'Display/$_displayId/Key/KeyUp',
      'Display/$_displayId/Key/KeyDown',
      'Button/$_thirdId/Focus/FocusIn',
      'Display/$_displayId/Key/KeyUp',
    ], reason: 'each key follows the focus the Tab before it moved');
  });

  testWidgets('a held key keeps the modifiers it was typed with', (tester) async {
    await _pumpButtonsWithVetoableTraverse(tester);
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    _deliver('Display/$_displayId/traverse/verdict', {'doit': true});
    await tester.pumpAndSettle();
    EquoCommService.commForTesting.clearSentFrames();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    _deliver('Display/$_displayId/traverse/verdict', {'doit': true});
    await tester.pumpAndSettle();

    expect(_hasFocus(tester, _firstId), isTrue, reason: 'Shift+Tab went back');
    final tabUp = EquoCommService.commForTesting.sentFrames
        .where((f) => EquoCommBase.decodeFrame(f).$1.endsWith('/Key/KeyUp'))
        .map(EquoCommBase.decodeEvent)
        .firstWhere((e) => e.keyCode == SWT.TAB);
    expect(tabUp.stateMask! & SWT.SHIFT, SWT.SHIFT,
        reason: 'Shift was down when the Tab was released, though it is up when the key is sent');
  });

  /// The key code of every key event sent to Java, `down:` or `up:`, in order.
  List<String> keyCodes() => EquoCommService.commForTesting.sentFrames
      .where((f) => EquoCommBase.decodeFrame(f).$1.contains('/Key/'))
      .map((f) => '${EquoCommBase.decodeFrame(f).$1.endsWith('KeyDown') ? 'down' : 'up'}:${EquoCommBase.decodeEvent(f).keyCode}')
      .toList();

  Future<void> recordFrom(WidgetTester tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    EquoCommService.commForTesting.clearSentFrames();
  }

  testWidgets('a Tab with nowhere to go sends its KeyUp before any frame is drawn', (tester) async {
    await _pumpShell(tester, [_button(_firstId, 'First', 0)]);
    await tester.tap(find.text('First'));
    await tester.pumpAndSettle();
    await recordFrom(tester);

    await tester.sendKeyDownEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.tab);

    expect(keyCodes(), ['down:${SWT.TAB}', 'up:${SWT.TAB}'],
        reason: 'the focus stayed where it was: nothing to wait for, no frame drawn yet');
  });

  testWidgets('a vetoed Tab releases the keys behind it', (tester) async {
    await _pumpButtonsWithVetoableTraverse(tester);

    await tester.sendKeyDownEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.tab);
    expect(keyCodes(), ['down:${SWT.TAB}'], reason: 'held while Java is thinking');

    _deliver('Display/$_displayId/traverse/verdict', {'doit': false});
    await tester.pump();

    expect(_hasFocus(tester, _secondId), isFalse, reason: 'vetoed');
    expect(keyCodes(), ['down:${SWT.TAB}', 'up:${SWT.TAB}'],
        reason: 'a veto moves nothing, so the KeyUp goes at once, to the control that kept the focus');
  });

  testWidgets('a Tab Java never answers goes through, and its KeyUp follows the focus', (tester) async {
    await _pumpButtonsWithVetoableTraverse(tester);

    await tester.sendKeyDownEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.tab);
    expect(_sent(), ['Display/$_displayId/Key/KeyDown'], reason: 'held while Java is thinking');

    // No verdict: the gate fails open after 400 ms.
    await tester.pump(const Duration(milliseconds: 400));
    await tester.pumpAndSettle();

    expect(_hasFocus(tester, _secondId), isTrue, reason: 'a lost verdict must not trap the keyboard');
    expect(_sent(), [
      'Display/$_displayId/Key/KeyDown',
      'Button/$_secondId/Focus/FocusIn',
      'Display/$_displayId/Key/KeyUp',
    ]);
  });

  testWidgets('a letter typed while a Tab waits for Java goes at once, with the edit it makes',
      (tester) async {
    await _pumpShell(tester, [_text(_firstId, 'first', 0), _text(_secondId, 'second', 30)]);
    receiveTagged('swt.evolve.focusHolder', {'id': _firstId});
    await tester.tap(find.byWidgetPredicate((w) => w is TextSwt && w.value.id == _firstId));
    await tester.pumpAndSettle();
    _deliver('Display/$_displayId/traverse/vetoable', {'value': true});
    await tester.pump();
    await recordFrom(tester);

    await tester.sendKeyDownEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.keyA, character: 'a');
    await tester.sendKeyUpEvent(LogicalKeyboardKey.keyA);
    // KeyEditOrder holds a typed key's release until its edit lands; a widget test makes no edit,
    // so it gives up after 250 ms. The Tab's answer is still pending: the gate waits 400 ms.
    await tester.pump(const Duration(milliseconds: 300));

    expect(keyCodes(), ['down:${SWT.TAB}', 'down:${'a'.codeUnitAt(0)}', 'up:${'a'.codeUnitAt(0)}'],
        reason: 'the field the client has focused takes the letter, so Java must get the key now; '
            'the Tab\'s own KeyUp still waits for the focus it moves');
  });

  testWidgets('a Tab onto a field drawn at rest reports its focus before the field is mounted', (tester) async {
    await _pumpShell(tester, [_button(_firstId, 'First', 0), _text(_secondId, 'second', 30)], forceLive: false);
    receiveTagged('swt.evolve.focusHolder', {'id': _firstId});
    await tester.tap(find.text('First'));
    await tester.pumpAndSettle();
    _deliver('Display/$_displayId/traverse/vetoable', {'value': true});
    await tester.pump();
    await recordFrom(tester);

    await tester.sendKeyDownEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.tab);
    _deliver('Display/$_displayId/traverse/verdict', {'doit': true});
    await tester.idle();

    final beforeAnyFrame = [
      'Display/$_displayId/Key/KeyDown',
      'Text/$_secondId/Focus/FocusIn',
      'Display/$_displayId/Key/KeyUp',
    ];
    expect(_sent(), beforeAnyFrame,
        reason: 'Java focuses the field when the client does, not a frame later: the KeyUp held '
            'behind the Tab gives up waiting after 400 ms, and a slow frame took longer');

    await tester.pumpAndSettle();
    expect(_hasFocus(tester, _secondId), isTrue);
    expect(_sent(), beforeAnyFrame,
        reason: 'mounting the field moves the focus within it, which is no second FocusIn');
  });
}
