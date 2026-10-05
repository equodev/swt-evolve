// A read-only StyledText is text to read, not a place to type: it sits on the surface like a Label
// does. Painting it with the editor fill shows a band of a different color across a dialog that
// uses one as a hyperlink row.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/styledtext_theme_extension.dart';

VStyledText _styledText({required bool editable}) => VStyledText()
  ..swt = 'StyledText'
  ..id = 1
  ..style = editable ? SWT.MULTI | SWT.WRAP : SWT.READ_ONLY | SWT.MULTI | SWT.WRAP
  ..enabled = true
  ..visible = true
  // false is a default the Java side never sends: a read-only StyledText arrives without it.
  ..editable = editable ? true : null
  ..caretOffset = 0
  ..text = 'What are these icons?'
  // An application color, ignored without use_swt_colors.
  ..background = (VColor()
    ..alpha = 0xFF
    ..red = 0xF0
    ..green = 0xF0
    ..blue = 0xF0)
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 600
    ..height = 15);

Future<StyledTextImpl> _pump(WidgetTester tester, VStyledText value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 600,
      height: 15,
      child: StyledTextSwt<VStyledText>(value: value),
    ),
  ));
  await tester.pump();
  return tester.state<StyledTextImpl>(find.byType(StyledTextSwt<VStyledText>));
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a read-only StyledText paints the surface', (tester) async {
    final impl = await _pump(tester, _styledText(editable: false));
    final theme = Theme.of(impl.context);
    final editor = theme.extension<StyledTextThemeExtension>()!.backgroundColor;

    expect(editor, isNot(theme.colorScheme.surface),
        reason: 'the two theme colors must differ, or this test cannot see the defect');
    expect(impl.bg, theme.colorScheme.surface);
  });

  testWidgets('an editable StyledText keeps the editor fill', (tester) async {
    final impl = await _pump(tester, _styledText(editable: true));
    final editor =
        Theme.of(impl.context).extension<StyledTextThemeExtension>()!.backgroundColor;

    expect(impl.bg, editor);
  });
}
