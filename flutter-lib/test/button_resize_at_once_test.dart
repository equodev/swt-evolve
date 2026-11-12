// A button Java resizes takes its new size in the frame that brings it, as a native one does: the
// press and hover colours animate, the button's edges do not.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/button_evolve.dart';
import 'package:swtflutter/src/impl/utils/quiet_animations.dart';

VRectangle _bounds(int width, int height) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = width
  ..height = height;

void main() {
  for (final (name, style) in [('push', SWT.PUSH), ('arrow', SWT.ARROW | SWT.RIGHT)]) {
    testWidgets('a $name button laid out larger is drawn at its new size in the next frame', (tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(
          alignment: Alignment.topLeft,
          child: ButtonSwt<VButton>(
            value: VButton()
              ..id = 1
              ..style = style
              ..enabled = true
              ..visible = true
              ..text = 'Section'
              ..bounds = _bounds(90, 26),
          ),
        ),
      ));
      await tester.pumpAndSettle();
      final surface = find.byType(QuietAnimatedContainer).first;
      final before = tester.getSize(surface);

      // As an update from Java arrives: the registry has made it the state before telling the widget.
      final button = tester.state<ButtonImpl>(find.byType(ButtonSwt<VButton>));
      button.state.bounds = _bounds(290, 126);
      button.setValue(button.state);
      await tester.pump(const Duration(milliseconds: 16));

      expect(tester.getSize(surface), before + const Offset(200, 100));
    });
  }
}
