// A Shell drawn inside the window is the size the application gave it, as a native one is: a small
// dialog stays small. Its size also goes back to Java as the Shell's bounds, so anything the
// client adds to it becomes the application's size too -- and an application that sizes its own
// content to the Shell (a layout, an embedded toolkit's window) then grows to fill the extra room.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _viewport = Size(1200, 900);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _shell({
  required int id,
  required String text,
  required VRectangle bounds,
  required int style,
}) => VShell()
  ..id = id
  ..style = style
  ..text = text
  ..bounds = bounds
  ..visible = true
  ..children = [
    VComposite()
      ..id = id + 1
      ..style = 0
      ..bounds = _rect(0, 0, bounds.width, bounds.height),
  ];

VShell _main() => _shell(
  id: 1001,
  text: 'Main',
  bounds: _rect(0, 0, _viewport.width.toInt(), _viewport.height.toInt()),
  style: SWT.SHELL_TRIM,
);

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  Future<Map<String, dynamic>> pumpAndReadReport(WidgetTester tester, VShell dialog) async {
    tester.view.physicalSize = _viewport;
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    EquoCommService.commForTesting.clearSentFrames();

    await tester.pumpWidget(
      EvolveApp(
        theme: ThemeMode.light,
        contentWidget: DisplaySwt(
          value: VDisplay()
            ..id = 7
            ..shells = [_main(), dialog]
            ..mainShellId = 1001,
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.pump();

    final setBounds = EquoCommService.commForTesting.sentFrames
        .map(EquoCommBase.decodeFrame)
        .where((f) => f.$1 == 'Shell/${dialog.id}/Shell/SetBounds')
        .toList();
    expect(setBounds, isNotEmpty, reason: 'the shell reports its geometry after the first frame');
    return jsonDecode(setBounds.last.$2) as Map<String, dynamic>;
  }

  // The body: the clipped box the Shell lays its client area out in, under the title bar.
  Size drawnBody(WidgetTester tester, int id) => tester.getSize(
    find
        .descendant(
          of: find.byKey(ValueKey(id)),
          matching: find.byWidgetPredicate((w) => w is ClipRect && w.child is SizedBox),
        )
        .first,
  );

  for (final (name, style) in [('a dialog', SWT.DIALOG_TRIM), ('a bordered shell', SWT.BORDER)]) {
    testWidgets('$name smaller than 200x150 keeps its size', (tester) async {
      final dialog = _shell(
        id: 2002,
        text: 'Message',
        bounds: _rect(400, 300, 262, 90),
        style: style,
      );
      final report = await pumpAndReadReport(tester, dialog);

      expect(
        [report['width'], report['height']],
        [262, 90],
        reason: 'the size reported back becomes the Shell\'s size in Java',
      );
      expect(drawnBody(tester, 2002), const Size(262, 90));
    });
  }

  testWidgets('a minimum the application set is kept', (tester) async {
    final dialog =
        _shell(id: 2002, text: 'Message', bounds: _rect(400, 300, 262, 90), style: SWT.DIALOG_TRIM)
          ..minimumSize = (VPoint()
            ..x = 300
            ..y = 120);
    final report = await pumpAndReadReport(tester, dialog);

    expect([report['width'], report['height']], [300, 120]);
    expect(drawnBody(tester, 2002), const Size(300, 120));
  });

  // A Shell Java has not sized yet reaches the client at 0x0 (or -1x-1): drawn at that size it
  // would not be on screen at all, so it gets a default, as a native Shell does.
  for (final (w, h) in [(0, 0), (-1, -1)]) {
    testWidgets('a dialog Java has not sized (${w}x$h) is still drawn', (tester) async {
      final dialog = _shell(
        id: 2002,
        text: 'Unsized',
        bounds: _rect(0, 0, w, h),
        style: SWT.DIALOG_TRIM,
      );
      final report = await pumpAndReadReport(tester, dialog);

      expect(report['width'], greaterThan(0));
      expect(report['height'], greaterThan(0));
    });
  }
}
