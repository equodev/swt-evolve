// Without a whole-tree Display to forward keys, a focused control forwards its own keystrokes from
// its wrap, the release as well as the press, as SWT reports KeyDown then KeyUp to its KeyListener.
// With a whole-tree Display the Display forwards them, so the control sends none.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';

import 'delivery/support/deliver.dart';

class _CapturingButtonSwt extends ButtonSwt<VButton> {
  const _CapturingButtonSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VButton val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VButton _button() => VButton()
  ..id = 7
  ..style = SWT.PUSH
  ..enabled = true
  ..visible = true
  ..text = 'Button'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 90
    ..height = 26);

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

/// A tree holding a field, as a cell editor sits in its tree.
VTree _treeWithField() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..visible = true
  ..columns = []
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'a',
  ]
  ..children = [
    VText()
      ..id = 60
      ..style = SWT.SINGLE
      ..enabled = true
      ..editable = true
      ..visible = true
      ..text = ''
      ..bounds = _rect(0, 0, 120, 22),
  ]
  ..bounds = _rect(0, 0, 400, 300);

/// A grid drawn on a Canvas, with the cell editors it placed over itself.
VCanvas _grid(List<VText> editors, int seq) => VCanvas()
  ..id = 30
  ..seq = seq
  ..style = SWT.NONE
  ..visible = true
  ..enabled = true
  ..bounds = _rect(0, 0, 300, 200)
  ..children = editors;

VText _cellEditor() => VText()
  ..id = 32
  ..style = SWT.SINGLE
  ..enabled = true
  ..editable = true
  ..visible = true
  ..text = ''
  ..bounds = _rect(40, 20, 100, 22);

/// The Key channels of the frames sent to Java.
List<String> _sentKeys() => [
      for (final frame in EquoCommService.commForTesting.sentFrames)
        if (EquoCommBase.decodeFrame(frame).$1 case final channel when channel.contains('/Key/')) channel,
    ];

void _recordSentFrames() {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() {
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
  });
}

/// Focuses the button, presses and releases `a`, and returns the Key events it sent.
Future<List<String>> _pressA(WidgetTester tester) async {
  final sent = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: _CapturingButtonSwt(
        value: _button(),
        onEvent: (ev, payload) {
          if (ev.startsWith('Key/')) sent.add('$ev ${payload?.character}');
        },
      ),
    ),
  ));
  FocusRequests.instance.request(7);
  await tester.pumpAndSettle();
  sent.clear();

  await tester.sendKeyDownEvent(LogicalKeyboardKey.keyA, character: 'a');
  await tester.sendKeyUpEvent(LogicalKeyboardKey.keyA);
  await tester.pumpAndSettle(const Duration(milliseconds: 300));
  return sent;
}

void main() {
  final a = 'a'.codeUnitAt(0);
  tearDown(FocusRequests.instance.reset);

  testWidgets('a focused control forwards the release of a key', (tester) async {
    expect(displayLevelKeyForwardingActive, isFalse, reason: 'sanity: no whole-tree Display forwards keys');

    expect(await _pressA(tester), ['Key/KeyDown $a', 'Key/KeyUp $a']);
  });

  testWidgets('a key typed into a field inside a control is the field\'s alone', (tester) async {
    _recordSentFrames();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 400, height: 300, child: TreeSwt<VTree>(value: _treeWithField())),
      ),
    ));
    await tester.pumpAndSettle();
    await tester.tap(find.byType(TextSwt<VText>));
    await tester.pumpAndSettle();
    EquoCommService.commForTesting.clearSentFrames();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.keyA, character: 'a');
    tester.testTextInput.enterText('a');
    await tester.sendKeyUpEvent(LogicalKeyboardKey.keyA);
    await tester.pumpAndSettle(const Duration(milliseconds: 300));

    expect(_sentKeys(), ['Text/60/Key/KeyDown', 'Text/60/Key/KeyUp'],
        reason: 'SWT sends a key to the focus control only, not to the controls holding it');
  });

  testWidgets('a held key\'s repeats reach Java as KeyDown, as a native control reports them',
      (tester) async {
    final sent = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: _CapturingButtonSwt(
          value: _button(),
          onEvent: (ev, payload) {
            if (ev.startsWith('Key/')) sent.add(ev);
          },
        ),
      ),
    ));
    FocusRequests.instance.request(7);
    await tester.pumpAndSettle();
    sent.clear();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.arrowDown);
    await tester.sendKeyRepeatEvent(LogicalKeyboardKey.arrowDown);
    await tester.sendKeyRepeatEvent(LogicalKeyboardKey.arrowDown);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.arrowDown);
    await tester.pumpAndSettle();

    expect(sent, ['Key/KeyDown', 'Key/KeyDown', 'Key/KeyDown', 'Key/KeyUp']);
  });

  testWidgets('a release goes to the editor its press opened, which holds the focus by then',
      (tester) async {
    VRegistry.instance.clear();
    _recordSentFrames();
    await tester.pumpWidget(ForceLive(child: EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, height: 200, child: CanvasSwt<VCanvas>(value: _grid([], 1))),
      ),
    )));
    await tester.pumpAndSettle();
    await tester.tapAt(tester.getCenter(find.byType(CanvasSwt<VCanvas>)));
    await tester.pumpAndSettle();
    EquoCommService.commForTesting.clearSentFrames();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.keyA, character: 'a');
    // Java answers the press with a cell editor over the grid, and focuses it.
    applyWhole(_grid([_cellEditor()], 2));
    await tester.pump();
    FocusRequests.instance.request(32);
    await tester.pumpAndSettle();
    await tester.sendKeyUpEvent(LogicalKeyboardKey.keyA);
    await tester.pumpAndSettle(const Duration(milliseconds: 300));

    expect(_sentKeys(), ['Canvas/30/Key/KeyDown', 'Text/32/Key/KeyUp'],
        reason: 'SWT sends the release to the control focused when it comes');
  });

  testWidgets('a focused control forwards no key while a whole-tree Display does', (tester) async {
    startDisplayKeyForwarding();
    addTearDown(stopDisplayKeyForwarding);

    expect(await _pressA(tester), isEmpty);
  });
}
