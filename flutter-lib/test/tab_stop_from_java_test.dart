// Whether Tab stops on a Canvas, a Composite or a Link is Java's call (VControl.tabStop, absent when
// false): natively a Canvas or Composite takes Tab when it hooks a key event, children or not, and a
// Link when it holds a link. The client's focus nodes follow the field and keep no rule of their own.
// TabStopRuleFlutterTest drives the Tab itself against a live client.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
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

Future<void> _pump(WidgetTester tester, VControl control) async {
  // A fresh tree each time: a mounted Composite keeps the children it was built with.
  await tester.pumpWidget(const SizedBox.shrink());
  VRegistry.instance.clear();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 100,
      child: CompositeSwt<VComposite>(
        value: VComposite()
          ..id = 1
          ..style = SWT.NONE
          ..visible = true
          ..enabled = true
          ..bounds = _rect(0, 0, 400, 100)
          ..children = [control],
      ),
    ),
  ));
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
}
