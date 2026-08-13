// Dart-side half of "Color dialog (color wheel) does not open".
//
// A ColorDialog reaches Flutter the same way a MessageBox does: DartDialog's
// openDialogWithFlutter parks it in the owning Shell's `dialogs` list and blocks
// until the close channel answers. ShellImpl._openDialog only knew 'MessageBox',
// so a 'ColorDialog' entry rendered nothing at all — the Java side then blocked
// forever on a dialog no one could ever close, which is what "clicking Edit…
// does nothing" looks like from the outside.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/colordialog.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/rgb.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/colordialog_evolve.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VRGB _rgb(int r, int g, int b) => VRGB()
  ..red = r
  ..green = g
  ..blue = b;

VColorDialog _colorDialog(int id, {VRGB? rgb}) => VColorDialog()
  ..id = id
  ..swt = 'ColorDialog'
  ..style = SWT.APPLICATION_MODAL
  ..text = 'Color'
  ..rgb = rgb;

void main() {
  testWidgets('a ColorDialog on the Shell opens the colour picker',
      (WidgetTester tester) async {
    VShell shellValue({List<VColorDialog>? dialogs}) => VShell()
      ..id = 1
      ..style = SWT.SHELL_TRIM
      ..text = 'shell'
      ..bounds = _rect(0, 0, 800, 600)
      ..dialogs = dialogs;

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: VDisplay()..shells = [shellValue()]),
    ));
    await tester.pumpAndSettle();

    // What DartDialog.openDialogWithFlutter does on the Java side: park the
    // dialog on the owning Shell and re-serialize it.
    final shellState = tester.state(find.byType(ShellSwt<VShell>)) as dynamic;
    shellState.setValue(
        shellValue(dialogs: [_colorDialog(4242, rgb: _rgb(0x12, 0x34, 0x56))]));
    await tester.pumpAndSettle();

    expect(find.byType(ColorDialogContent), findsOneWidget,
        reason: 'ColorDialog.open() must put a colour picker on screen; '
            'rendering nothing leaves the SWT thread blocked forever.');
    final content = tester.widget<ColorDialogContent>(find.byType(ColorDialogContent));
    expect(packRgb(content.initial), 0x123456,
        reason: 'the picker must start on the colour the caller passed to setRGB()');
  });

  testWidgets('OK reports the picked colour packed as 0xRRGGBB, Cancel reports -1',
      (WidgetTester tester) async {
    final results = <int>[];

    Widget picker() => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: ColorDialogContent(
            title: 'Color',
            initial: const Color(0xFF000000),
            customColors: const [],
            onResult: results.add,
          ),
        );

    await tester.pumpWidget(picker());
    await tester.pumpAndSettle();

    // Pure red is one of the 16 basic swatches; picking it then confirming must
    // hand Java 0xFF0000 — not the win32 struct's 0x0000BB_GG_RR byte order.
    await tester.tap(find.byKey(const ValueKey('color-swatch-16711680')));
    await tester.pumpAndSettle();
    await tester.tap(find.text('OK'));
    await tester.pumpAndSettle();

    expect(results, [0xFF0000]);

    await tester.pumpWidget(picker());
    await tester.pumpAndSettle();
    await tester.tap(find.text('Cancel'));
    await tester.pumpAndSettle();

    expect(results.last, -1, reason: 'cancelling must not report a colour');
  });
}
