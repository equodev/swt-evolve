// Whether Tab stops on a Canvas, a Composite, an ExpandBar or a Link is Java's call (VControl.tabStop, absent when
// false): natively a Canvas or Composite takes Tab when it hooks a key event, children or not, and a
// Link when it holds a link. The client's focus nodes follow the field; the one rule of their own is
// that an ExpandBar, like a ToolBar, has nowhere to stop while it has no item.
// TabStopRuleFlutterTest drives the Tab itself against a live client.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/expandbar.dart';
import 'package:swtflutter/src/gen/expanditem.dart';
import 'package:swtflutter/src/gen/link.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VText _text(int id, int x) => VText()
  ..id = id
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = ''
  ..visible = true
  ..enabled = true
  ..bounds = _rect(x, 2, 60, 24);

T _control<T extends VControl>(T control, {bool? tabStop, int id = 5}) => control
  ..id = id
  ..style = SWT.NONE
  ..visible = true
  ..enabled = true
  ..tabStop = tabStop
  ..bounds = _rect(10, 10, 200, 40);

Future<void> _pump(WidgetTester tester, VControl control,
    {List<VControl> more = const [], Widget Function(Widget content)? wrap}) async {
  // A fresh tree each time: a mounted Composite keeps the children it was built with.
  await tester.pumpWidget(const SizedBox.shrink());
  VRegistry.instance.clear();
  final Widget content = SizedBox(
    width: 400,
    height: 100,
    child: CompositeSwt<VComposite>(
      value: VComposite()
        ..id = 1
        ..style = SWT.NONE
        ..visible = true
        ..enabled = true
        ..bounds = _rect(0, 0, 400, 100)
        ..children = [control, ...more],
    ),
  );
  await tester.pumpWidget(EvolveApp(theme: ThemeMode.light, contentWidget: wrap == null ? content : wrap(content)));
  await tester.pumpAndSettle();
}

FocusNode? _node(String label) {
  for (final node in FocusManager.instance.rootScope.descendants) {
    if (node.debugLabel == label) return node;
  }
  return null;
}

/// Whether Tab can land on [node]: mounted, focusable and not skipped.
bool _isTabStop(FocusNode? node) => node != null && node.canRequestFocus && !node.skipTraversal;

void main() {
  // As on desk and web, where the Display forwards keys and controls mount no Focus of their own.
  setUp(() {
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
    startDisplayKeyForwarding();
  });
  tearDown(() {
    stopDisplayKeyForwarding();
    FocusRequests.instance.reset();
  });

  testWidgets('a childless Composite is a Tab stop only when Java says so', (tester) async {
    await _pump(tester, _control(VComposite()));
    expect(_isTabStop(_node('CompositeSurface')), isFalse);

    await _pump(tester, _control(VComposite(), tabStop: true, id: 7));
    expect(_isTabStop(_node('CompositeSurface')), isTrue);
  });

  testWidgets('a Composite with children that is a Tab stop is one itself', (tester) async {
    await _pump(tester, _control(VComposite()..children = [_text(6, 4)], tabStop: true));
    expect(_isTabStop(_node('CompositeSurface')), isTrue);
  });

  testWidgets('a Composite with children that is no Tab stop leaves Tab to them', (tester) async {
    await _pump(tester, _control(VComposite()..children = [_text(6, 4)]));
    expect(_isTabStop(_node('CompositeSurface')), isFalse);
    expect(_isTabStop(_node('CompositeChildren')), isFalse);
  });

  testWidgets('a Canvas is a Tab stop only when Java says so, children or not', (tester) async {
    await _pump(tester, _control(VCanvas()));
    expect(_isTabStop(_node('CanvasKeyboard')), isFalse);

    await _pump(tester, _control(VCanvas()..children = [_text(6, 4)], tabStop: true, id: 7));
    expect(_isTabStop(_node('CanvasKeyboard')), isTrue);
    expect(_isTabStop(_node('CompositeSurface')), isFalse, reason: 'one stop, as natively');
  });

  testWidgets('a Link is no Tab stop when Java says so, whatever its text', (tester) async {
    await _pump(tester, _control(VLink()..text = 'see <a>this</a>'));
    final focusable = FocusManager.instance.rootScope.descendants
        .where((n) => n is! FocusScopeNode && n.canRequestFocus && !n.skipTraversal && n.rect.overlaps(const Rect.fromLTWH(10, 10, 200, 40)));
    expect(focusable, isEmpty);
  });

  testWidgets('an ExpandBar Java makes a Tab stop is one while it has an item', (tester) async {
    await _pump(tester, _expandBar());
    expect(_isTabStop(_node('CompositeSurface')), isTrue);

    await _pump(tester, _control(VExpandBar()..items = [], tabStop: true, id: 7));
    expect(_isTabStop(_node('CompositeSurface')), isFalse, reason: 'no header to stop on');
  });

  testWidgets('an arrow key does not walk the focus onto an ExpandBar: SWT moves it by Tab only', (tester) async {
    // The Shell's scope holds the focus with no focused child, as after a click on the bar.
    final shellScope = FocusScopeNode(debugLabel: 'Shell');
    addTearDown(shellScope.dispose);
    await _pump(tester, _expandBar(), wrap: (content) => FocusScope(node: shellScope, child: content));
    shellScope.requestFocus();
    await tester.pumpAndSettle();
    final bar = _node('CompositeSurface')!;
    expect(bar.hasFocus, isFalse);

    for (final key in _arrows) {
      await tester.sendKeyEvent(key);
      await tester.pumpAndSettle();
      expect(bar.hasFocus, isFalse, reason: '$key');
    }
  });

  testWidgets('an arrow key does not walk the focus off a focused ExpandBar onto a neighbour', (tester) async {
    await _pump(tester, _expandBar()..bounds = _rect(10, 10, 150, 80), more: [
      VButton()
        ..id = 9
        ..style = SWT.PUSH
        ..text = 'OK'
        ..visible = true
        ..enabled = true
        ..bounds = _rect(200, 10, 80, 28),
    ]);
    final bar = _node('CompositeSurface')!;
    bar.requestFocus();
    await tester.pumpAndSettle();
    expect(bar.hasPrimaryFocus, isTrue);

    for (final key in _arrows) {
      await tester.sendKeyEvent(key);
      await tester.pumpAndSettle();
      expect(bar.hasPrimaryFocus, isTrue, reason: '$key');
    }
  });
}

VExpandBar _expandBar() => _control(VExpandBar()..items = [VExpandItem()..id = 6..text = 'One'], tabStop: true);

const _arrows = [
  LogicalKeyboardKey.arrowLeft,
  LogicalKeyboardKey.arrowRight,
  LogicalKeyboardKey.arrowUp,
  LogicalKeyboardKey.arrowDown,
];
