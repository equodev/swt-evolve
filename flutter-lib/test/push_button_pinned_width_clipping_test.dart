// A PUSH button whose bounds are narrower than its preferred width ellipsizes
// its label instead of giving the text the room the fixed chrome is holding.
//
// An application is free to pin a button's width, and real ones do -- they size
// it from their own font through GC.textExtent, which we report faithfully for
// the application font. The label, however, is painted in the theme font
// (use_swt_fonts off), which is wider: 8px of padding either side plus a 1px
// border either side then leave "Add Food" a 57px viewport for 63.8px of text,
// and it renders as "Add F...".
//
// This is the push-button twin of combo_pinned_width_clipping_test.dart, and
// the rule is the same: the chrome must yield to the text, not the other way
// round.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

// The test fallback font gives every glyph a full em box, which would make the
// deficit an artefact of the harness rather than the one the reporter saw. Load
// the real face the theme asks for so the widths are the app's own.
Future<void> _loadInter() async {
  final loader = FontLoader('Inter')
    ..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
  await loader.load();
}

double _shapedWidth(WidgetTester tester, String label) {
  final Text widget = tester.widget<Text>(find.text(label));
  final painter = TextPainter(
    text: TextSpan(text: label, style: widget.style),
    maxLines: 1,
    textDirection: TextDirection.ltr,
  )..layout();
  return painter.width;
}

EdgeInsets _appliedPadding(WidgetTester tester) {
  final AnimatedContainer container = tester.widget<AnimatedContainer>(
    find.descendant(
      of: find.byType(ButtonSwt<VButton>),
      matching: find.byType(AnimatedContainer),
    ),
  );
  return container.padding! as EdgeInsets;
}

Future<void> _pumpButton(WidgetTester tester, VButton button) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: ButtonSwt<VButton>(value: button),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  setUpAll(_loadInter);

  testWidgets('a PUSH button pinned to a narrow width still shows its label',
      (WidgetTester tester) async {
    const label = 'Add Food';
    final button = VButton()
      ..id = 41
      ..style = SWT.PUSH
      ..enabled = true
      ..text = label
      // What the application pins, sized from its own font.
      ..bounds = _rect(0, 0, 75, 26);

    await _pumpButton(tester, button);

    final double viewportWidth = tester.getSize(find.text(label)).width;
    final double textWidth = _shapedWidth(tester, label);

    expect(viewportWidth, greaterThanOrEqualTo(textWidth),
        reason: 'The label viewport (${viewportWidth.toStringAsFixed(2)}px) is '
            'narrower than the label (${textWidth.toStringAsFixed(2)}px): '
            'it renders ellipsized.');
  });

  testWidgets('a PUSH button with room to spare keeps its full padding',
      (WidgetTester tester) async {
    // The natural width is what ButtonSizes.java's HORIZONTAL_PADDING was
    // measured from, so the fitted padding must not shrink a button that fits.
    const label = 'Add Food';
    final button = VButton()
      ..id = 42
      ..style = SWT.PUSH
      ..enabled = true
      ..text = label
      ..bounds = _rect(0, 0, 140, 26);

    await _pumpButton(tester, button);

    expect(_appliedPadding(tester).horizontal, 16.0);
  });
}
