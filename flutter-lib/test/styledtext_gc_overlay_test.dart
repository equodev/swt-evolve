// Issue #752 — the "Enter text to search..." hint (drawn by an app SWT.Paint
// listener via GC) was invisible on StyledText-based search fields.
//
// StyledText extends Canvas in SWT, so StyledTextImpl extends CanvasImpl and
// inherited its wrapWithGCOverlay, which stacks the GC UNDER the child (correct
// for a Canvas, whose content IS the GC). But StyledText paints its own opaque
// background, which buried the GC. The overlay must go on top instead.

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

VStyledText _styledText(int id) => VStyledText()
  ..id = id
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 20);

void main() {
  testWidgets('StyledText stacks the GC overlay on top of its content', (tester) async {
    final key = GlobalKey<StyledTextImpl>();

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 20,
        child: StyledTextSwt<VStyledText>(key: key, value: _styledText(42)),
      ),
    ));

    final state = key.currentState!;
    state.gcOverlay = VGC()..id = 42;

    const marker = SizedBox.shrink();
    final stack = state.wrapWithGCOverlay(marker) as Stack;

    final contentIndex = stack.children.indexOf(marker);
    final gcIndex = stack.children.indexWhere((w) => w is Positioned);

    expect(contentIndex, isNonNegative);
    expect(gcIndex, isNonNegative);
    expect(contentIndex, lessThan(gcIndex),
        reason: 'GC overlay must paint over StyledText content, not under it');
  });
}
