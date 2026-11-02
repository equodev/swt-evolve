// A shell nobody placed is centred by this side, and the position is reported to Java in whole
// pixels, which Java then sends back as the shell's bounds. Centring a shell whose leftover space
// is odd lands on a half pixel; reported rounded and repeated by Java, that half pixel moved the
// shell, and repainted it, once per shell shown this way.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/shell_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

// 1280 - 945 is odd: the centred x of a 945-wide shell is 167.5.
const _viewport = Size(1280, 768);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _shell({required int id, required String text, required VRectangle bounds}) => VShell()
  ..id = id
  ..style = SWT.SHELL_TRIM
  ..text = text
  ..bounds = bounds
  ..visible = true
  ..children = [
    VComposite()
      ..id = id + 1
      ..style = 0
      ..bounds = _rect(0, 0, 100, 100),
  ];

VShell _main() => _shell(
  id: 1001,
  text: 'Main',
  bounds: _rect(0, 0, _viewport.width.toInt(), _viewport.height.toInt()),
);

/// Sized by Java (Cocoa's 5/8 of a 1512x982 screen), never placed: x and y are still 0.
VShell _dialog({int x = 0, int y = 0}) =>
    _shell(id: 2002, text: 'Dialog', bounds: _rect(x, y, 945, 614));

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  Future<void> pumpDisplay(WidgetTester tester, List<VShell> shells) async {
    await tester.pumpWidget(
      EvolveApp(
        theme: ThemeMode.light,
        contentWidget: DisplaySwt(
          value: VDisplay()
            ..id = 7
            ..shells = shells
            ..mainShellId = shells.first.id,
        ),
      ),
    );
    await tester.pumpAndSettle();
  }

  testWidgets(
    'a shell nobody placed is drawn where it says it is, and stays there when Java repeats it',
    (tester) async {
      tester.view.physicalSize = _viewport;
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);
      addTearDown(tester.view.resetDevicePixelRatio);
      EquoCommBase.recordSentFrames = true;
      addTearDown(() => EquoCommBase.recordSentFrames = false);
      EquoCommService.commForTesting.clearSentFrames();

      await pumpDisplay(tester, [_main(), _dialog()]);
      await tester.pump();

      final drawn = tester.getRect(find.byType(FloatingShellChromeScope));
      final setBounds = EquoCommService.commForTesting.sentFrames
          .where((f) => EquoCommBase.decodeFrame(f).$1 == 'Shell/2002/Shell/SetBounds')
          .toList();
      expect(setBounds, isNotEmpty, reason: 'the shell reports its geometry after the first frame');
      final report = EquoCommBase.decodeEvent(setBounds.last);
      expect(
        Offset(report.x!.toDouble(), report.y!.toDouble()),
        drawn.topLeft,
        reason: 'the report is where the shell is drawn, to the pixel: Java keeps it as the bounds',
      );

      // Java repeats the report as the shell's bounds.
      await pumpDisplay(tester, [_main(), _dialog(x: report.x!, y: report.y!)]);

      expect(
        tester.getRect(find.byType(FloatingShellChromeScope)),
        drawn,
        reason: 'placed where it already was, the shell must not move',
      );
    },
  );
}
