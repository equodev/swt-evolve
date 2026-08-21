// ControlImpl.wrap() returned before wrapWithGCOverlay() for a disabled control, so a disabled
// control mounted no GC at all. Java still dispatches SWT.Paint and emits the draw ops on the
// widget's GC channels, so with nothing subscribed everything the control draws itself is dropped
// -- and the one Paint request a Canvas sends while its overlay is unmounted is never repeated.
// A Canvas that draws its own label from a PaintListener (an org.eclipse.ui.forms Hyperlink, which
// applications create disabled) therefore stayed blank until something enabled it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _bounds(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

/// A Hyperlink-shaped Canvas: no children, its whole visible content comes from a PaintListener.
VCanvas _paintedCanvas({required bool enabled}) => VCanvas()
  ..id = 979
  ..style = SWT.NONE
  ..enabled = enabled
  ..visible = true
  ..bounds = _bounds(120, 18);

Future<void> _pump(WidgetTester tester, {required bool enabled}) =>
    tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 120,
        height: 18,
        child: CanvasSwt<VCanvas>(value: _paintedCanvas(enabled: enabled)),
      ),
    ));

void main() {
  testWidgets('an enabled Canvas mounts its GC', (tester) async {
    await _pump(tester, enabled: true);
    expect(find.byType(GCSwt<VGC>, skipOffstage: false), findsOneWidget);
  });

  testWidgets('a disabled Canvas still mounts its GC', (tester) async {
    await _pump(tester, enabled: false);
    expect(find.byType(GCSwt<VGC>, skipOffstage: false), findsOneWidget,
        reason: 'nothing is subscribed when Java emits the draw ops for a disabled control');
  });
}
