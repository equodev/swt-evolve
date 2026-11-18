// A sash container nested in an outer one's part (an editor area) panels its children, so the gap it
// leaves along its own edge sits beside the outer band: the user sees one divider, and the outer
// container's SashLayout has to hear the pointer over all of it.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/custom/main_composite.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/pending_move.dart';
import 'package:swtflutter/src/nolayout.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _part(int id, VRectangle bounds) => VLabel()
  ..swt = 'Label'
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..text = 'part $id'
  ..bounds = bounds;

VComposite _composite(String swt, int id, VRectangle bounds, List<VControl> children) => VComposite()
  ..swt = swt
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = bounds
  ..children = children;

const int _outer = 80;
const int _area = 81;
const int _nested = 82;
const double _bandLeft = 198;
const double _y = 150;
const int _mouse = 1;

// The outer container's band is 198..202. Its left child is a plain composite holding a nested
// part sash container, which insets its two part stacks by panelChildGap, its own right edge too.
VComposite _tree() => _composite('MainComposite', _outer, _rect(0, 0, 400, 300), [
      _composite('Composite', _area, _rect(0, 0, _bandLeft.toInt(), 300), [
        _composite('MainComposite', _nested, _rect(0, 0, _bandLeft.toInt(), 300), [
          _part(83, _rect(0, 0, 96, 300)),
          _part(84, _rect(100, 0, 98, 300)),
        ]),
      ]),
      _part(85, _rect(202, 0, 198, 300)),
    ]);

Future<List<(int, String, VEvent?)>> _mount(WidgetTester tester) async {
  final sent = <(int, String, VEvent?)>[];
  EquoCommBase.sendObserverForTesting = (action, payload) {
    final parts = action.split('/');
    if (parts.length > 2 && parts[2].startsWith('Mouse')) {
      sent.add((int.parse(parts[1]), parts.skip(2).join('/'), payload as VEvent?));
    }
  };
  addTearDown(() {
    EquoCommBase.sendObserverForTesting = null;
    PendingMove.reset();
  });
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    // The outer container is one of an enclosing container's panels, as in the e4 workbench.
    contentWidget: SashPanelMarker(
      active: true,
      child: SizedBox(width: 400, height: 300, child: MainComposite(value: _tree())),
    ),
  ));
  await tester.pumpAndSettle();
  return sent;
}

Future<TestGesture> _mouseAt(WidgetTester tester, Offset at) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse, pointer: _mouse);
  await gesture.addPointer(location: at);
  addTearDown(gesture.removePointer);
  await tester.pumpAndSettle();
  return gesture;
}

void main() {
  const onBand = Offset(_bandLeft + 2, _y);
  // Inside the nested container's bounds, in the gap it leaves along its right edge.
  const inNestedGap = Offset(_bandLeft - 3, _y);

  testWidgets('hover in the nested container\'s edge gap reaches the outer container',
      (tester) async {
    final sent = await _mount(tester);
    final gesture = await _mouseAt(tester, onBand);
    sent.clear();

    await gesture.moveTo(inNestedGap);
    await tester.pumpAndSettle();

    expect(sent.where((e) => e.$1 == _outer && e.$2.endsWith('MouseMove')).map((e) => e.$3?.x),
        contains(inNestedGap.dx.round()),
        reason: 'the gap reads as part of the outer divider, so SashLayout must hear the move there');
    expect(sent.where((e) => e.$1 == _outer && e.$2.endsWith('MouseExit')), isEmpty,
        reason: 'SashLayout clears its resize cursor on MouseExit');
    expect(sent.where((e) => e.$1 == _area || e.$1 == _nested), isEmpty,
        reason: 'nothing of the nested container or its parent is painted there');
  });

  testWidgets('a press in the nested container\'s edge gap reaches the outer container',
      (tester) async {
    final sent = await _mount(tester);
    final gesture = await _mouseAt(tester, inNestedGap);
    sent.clear();

    await gesture.down(inNestedGap);
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();

    expect(sent.where((e) => e.$1 == _outer && e.$2.endsWith('MouseDown')).map((e) => e.$3?.x),
        [inNestedGap.dx.round()],
        reason: 'SashLayout engages the resize from a MouseDown on the outer container');
    expect(sent.where((e) => e.$1 == _area || e.$1 == _nested), isEmpty);
  });

  testWidgets('the gap between the nested container\'s own part stacks stays its own',
      (tester) async {
    final sent = await _mount(tester);
    const betweenNestedParts = Offset(98, _y);
    final gesture = await _mouseAt(tester, const Offset(50, _y));
    sent.clear();

    await gesture.moveTo(betweenNestedParts);
    await tester.pumpAndSettle();

    expect(sent.where((e) => e.$1 == _nested && e.$2.endsWith('MouseMove')).map((e) => e.$3?.x),
        contains(betweenNestedParts.dx.round()));
    expect(sent.where((e) => e.$1 == _outer && e.$2.endsWith('MouseMove')), isEmpty);
  });

  testWidgets('a disposed container\'s edge gap no longer takes the pointer from its parent',
      (tester) async {
    await _mount(tester);
    final nested = (_tree().children!.first as VComposite).children!.first;
    const inGap = Offset(_bandLeft - 3, _y);
    expect(PanelEdge.gapAt(nested, inGap), isNotNull);

    await tester.pumpWidget(const SizedBox());

    expect(PanelEdge.gapAt(nested, inGap), isNull,
        reason: 'a later control with the same id would hand the pointer on through a gap it does '
            'not paint');
  });
}
