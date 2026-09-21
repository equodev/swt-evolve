// An application can paint over a control through that control's GC. On a control that already
// draws itself -- a check box whose label the application redraws to recolour it -- the two
// drawings stack. disable_control_gc_overlay drops the painted copy and keeps the control's own.
//
// It drops an overlay, never a control's own drawing: a Canvas IS its GC content, so it keeps
// painting whatever the flag says.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VRectangle _rect(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

VButton _checkBox() => VButton()
  ..id = 1
  ..style = SWT.CHECK | SWT.LEFT
  ..enabled = true
  ..visible = true
  ..text = 'Show Password'
  ..alignment = SWT.LEFT
  ..bounds = _rect(132, 19);

VCanvas _canvas() => VCanvas()
  ..id = 2
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(120, 60);

/// Whether the control would paint what the application draws through its GC. The overlay itself
/// only goes on screen once there is content to draw, so the flag's effect is this decision; the
/// GC stays mounted either way, listening to Java.
bool _paintsOverlay(WidgetTester tester, Finder control) {
  expect(find.byWidgetPredicate((w) => w is GCSwt, skipOffstage: false), findsWidgets,
      reason: 'the GC has to stay mounted to receive Java events');
  return (tester.state(control) as dynamic).paintsGCOverlay as bool;
}

/// A Canvas paints its GC content unconditionally: its overlay is on screen, not offstage.
bool _canvasOverlayOnStage(WidgetTester tester) =>
    find.byWidgetPredicate((w) => w is GCSwt).evaluate().isNotEmpty;

Future<void> _pump(WidgetTester tester, Widget content) async {
  await tester.pumpWidget(EvolveApp(theme: ThemeMode.light, contentWidget: content));
  await tester.pumpAndSettle();
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a check box paints what the application draws over it by default', (tester) async {
    await _pump(tester, ButtonSwt<VButton>(value: _checkBox()));

    expect(_paintsOverlay(tester, find.byType(ButtonSwt<VButton>)), isTrue);
  });

  testWidgets('disable_control_gc_overlay leaves the check box with its own label only',
      (tester) async {
    setConfigFlags(ConfigFlags()..disable_control_gc_overlay = true);

    await _pump(tester, ButtonSwt<VButton>(value: _checkBox()));

    expect(_paintsOverlay(tester, find.byType(ButtonSwt<VButton>)), isFalse);
    expect(find.text('Show Password'), findsOneWidget,
        reason: "the control's own label is what the opt-out keeps");
  });

  testWidgets('a Canvas keeps painting its GC content with the opt-out on', (tester) async {
    setConfigFlags(ConfigFlags()..disable_control_gc_overlay = true);

    await _pump(tester, CanvasSwt<VCanvas>(value: _canvas()));

    expect(_canvasOverlayOnStage(tester), isTrue,
        reason: 'a Canvas IS its GC drawing; the opt-out drops overlays, not that');
  });
}
