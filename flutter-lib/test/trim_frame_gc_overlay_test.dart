// A trim contribution's frame is a Canvas that paints its drag-handle grip through its GC, with the
// contribution's ToolBar beside it. The trim composites build that Canvas themselves, so they have to
// mount its GC: without one, what Java draws on the frame has nothing to receive it.

import 'dart:math' show Rectangle;

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

void main() {
  testWidgets('a Canvas frame in the main toolbar mounts its GC and receives its grip',
      (tester) async {
    freshClient();
    final bar = _bar(
        'MainToolbar', _frame(const Rectangle(40, 9, 89, 22), _toolBar(5, 0)), rectOf(0, 0, 600, 40));
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
          width: 600, height: 40, child: ToolbarComposite(value: bar, useBoundsLayout: true)),
    ));
    await tester.pumpAndSettle();

    await _expectGripPainted(tester, const Size(89, 22));
  });

  testWidgets('a Canvas frame in a side trim mounts its GC and receives its grip', (tester) async {
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

    expect(_frameGC, findsOneWidget, reason: 'the frame Canvas mounts a GC for its own id');
    await _paintGrip(tester, 20, 5);
    final gc = tester.state(_frameGC) as dynamic;
    expect(gc.paintOps as List, isNotEmpty, reason: 'the grip Java draws on the frame reaches its GC');
    expect(tester.getSize(_frameGC), tester.getSize(find.byWidgetPredicate(
        (w) => w is SideBarComposite && w.value.id == _frameId)),
        reason: 'the GC layer covers the whole frame');
  });
}
