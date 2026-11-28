// A trim contribution's frame is a Canvas that paints its drag-handle grip through its GC. Under
// trim_drag_handles the trim composites mount that GC; without it they mount none.

import 'dart:math' show Rectangle;

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';
import 'support/gc_frames.dart';

const int _frameId = 4101;

const String _grip = '<svg xmlns="http://www.w3.org/2000/svg" width="5" height="20">'
    '<rect width="5" height="20" fill="#000000"/></svg>';

VToolBar _toolBar(int x, int y) => VToolBar()
  ..id = _frameId + 1
  ..style = SWT.FLAT | SWT.HORIZONTAL
  ..enabled = true
  ..visible = true
  ..items = const []
  ..bounds = rectOf(x, y, 40, 22);

VCanvas _frame(Rectangle<int> bounds, VToolBar toolBar) => VCanvas()
  ..id = _frameId
  ..swt = 'Canvas'
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..children = [toolBar]
  ..bounds = rectOf(bounds.left, bounds.top, bounds.width, bounds.height);

VComposite _bar(String swt, VCanvas frame, VRectangle bounds) => VComposite()
  ..id = _frameId - 1
  ..swt = swt
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..children = [frame]
  ..bounds = bounds;

Finder get _frameWidget => find.byWidgetPredicate(
    (w) => (w is ToolbarComposite || w is SideBarComposite) && (w as CompositeSwt).value.id == _frameId);

Finder get _frameToolBar => find.byWidgetPredicate((w) => w is ToolBarSwt && w.value.id == _frameId + 1);

/// The contribution sits where Java put it in its frame, clear of the drag-handle strip.
void _expectToolBarAt(WidgetTester tester, Offset inFrame) {
  expect(tester.getTopLeft(_frameToolBar) - tester.getTopLeft(_frameWidget), inFrame,
      reason: 'the ToolBar leaves the grip strip uncovered');
}

Finder get _frameGC => find.byWidgetPredicate(
    (w) => w is GCSwt && w.value.id == _frameId,
    skipOffstage: false);

/// Java's paint of the frame: its GC's state, the grip image, and the dispose that ends the paint.
Future<void> _paintGrip(WidgetTester tester, int width, int height) async {
  final grip = heldImage(VImage()..svgContent = _grip);
  receive(
      'GC/$_frameId/b',
      GcFrame()
          .whole(VGC.empty()..id = _frameId)
          .opNamed('drawImageImageintintintintintintintint',
              [grip, 0, 0, width, height, 0, 1, width, height])
          .escape('GC/$_frameId/gcDispose', {'fullRepaint': true})
          .bytes);
  await tester.pumpAndSettle();
}

Future<void> _expectGripPainted(WidgetTester tester, Size frameSize) async {
  expect(_frameGC, findsOneWidget, reason: 'the frame Canvas mounts a GC for its own id');

  await _paintGrip(tester, frameSize.width.toInt(), frameSize.height.toInt());

  final gc = tester.state(_frameGC) as dynamic;
  expect(gc.paintOps as List, isNotEmpty, reason: 'the grip Java draws on the frame reaches its GC');
  expect(tester.getSize(_frameGC), frameSize, reason: 'the GC layer covers the whole frame');
}

Future<void> _pumpMainToolbar(WidgetTester tester) async {
  freshClient();
  final bar = _bar(
      'MainToolbar', _frame(const Rectangle(40, 9, 89, 22), _toolBar(5, 0)), rectOf(0, 0, 600, 40));
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
        width: 600, height: 40, child: ToolbarComposite(value: bar, useBoundsLayout: true)),
  ));
  await tester.pumpAndSettle();
}

double _gripOpacity(WidgetTester tester) => tester
    .widget<AnimatedOpacity>(find.ancestor(of: _frameGC, matching: find.byType(AnimatedOpacity)).first)
    .opacity;

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('without trim_drag_handles a Canvas frame mounts no GC, so its grip is not drawn',
      (tester) async {
    await _pumpMainToolbar(tester);

    _expectToolBarAt(tester, const Offset(5, 0));
    expect(_frameGC, findsNothing);
  });

  testWidgets('under trim_drag_handles the grip shows toned down only while the pointer is over its frame',
      (tester) async {
    setConfigFlags(ConfigFlags()..trim_drag_handles = true);
    await _pumpMainToolbar(tester);
    await _paintGrip(tester, 89, 22);

    expect(_gripOpacity(tester), 0.0, reason: 'the grip stays hidden until the pointer reaches the frame');

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: tester.getCenter(_frameWidget));
    await tester.pumpAndSettle();
    expect(_gripOpacity(tester), allOf(greaterThan(0.0), lessThan(1.0)),
        reason: 'over its frame the grip shows, toned towards the ground');

    await mouse.moveTo(const Offset(590, 35));
    await tester.pumpAndSettle();
    expect(_gripOpacity(tester), 0.0, reason: 'leaving the frame hides the grip again');
    await mouse.removePointer();
  });

  testWidgets('a Canvas frame in the main toolbar mounts its GC and receives its grip',
      (tester) async {
    setConfigFlags(ConfigFlags()..trim_drag_handles = true);
    await _pumpMainToolbar(tester);

    _expectToolBarAt(tester, const Offset(5, 0));
    await _expectGripPainted(tester, const Size(89, 22));
  });

  testWidgets('a Canvas frame in a side trim mounts its GC and receives its grip', (tester) async {
    setConfigFlags(ConfigFlags()..trim_drag_handles = true);
    freshClient();
    final bar = _bar('SideBar', _frame(const Rectangle(0, 0, 24, 80), _toolBar(0, 5)),
        rectOf(0, 0, 24, 200));
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(width: 24, height: 200, child: SideBarComposite(value: bar))),
    ));
    await tester.pumpAndSettle();

    _expectToolBarAt(tester, const Offset(0, 5));
    expect(_frameGC, findsOneWidget, reason: 'the frame Canvas mounts a GC for its own id');
    await _paintGrip(tester, 20, 5);
    final gc = tester.state(_frameGC) as dynamic;
    expect(gc.paintOps as List, isNotEmpty, reason: 'the grip Java draws on the frame reaches its GC');
    expect(tester.getSize(_frameGC), tester.getSize(_frameWidget),
        reason: 'the GC layer covers the whole frame');
  });
}
