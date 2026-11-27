// A JFace dialog's title area shows its message in a borderless read-only Text that it disables
// while there is no message, and colours like the banner under it. Native draws that empty Text as
// nothing at all; it must not show up as a box of the theme's own colour.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _banner = Color(0xFFFFFFFF);

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VColor _white() => VColor()
  ..alpha = 0xFF
  ..red = 0xFF
  ..green = 0xFF
  ..blue = 0xFF;

const _messageStyle = SWT.MULTI | SWT.WRAP | SWT.READ_ONLY;

/// TitleAreaDialog's message Text while the page has no message.
VText _emptyMessage({
  int style = _messageStyle,
  bool enabled = false,
  bool hasOwnBackground = true,
}) =>
    VText()
      ..id = 2
      ..style = style
      ..enabled = enabled
      ..visible = true
      ..editable = false
      ..bounds = _rect(11, 34, 366, 32)
      ..text = ''
      ..hasOwnBackground = hasOwnBackground
      ..background = _white();

VComposite _titleArea(VText message) => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..hasOwnBackground = true
  ..background = _white()
  ..bounds = _rect(0, 0, 450, 70)
  ..children = [message];

Future<InputDecoration> _pumpDecoration(
  WidgetTester tester,
  VText message, {
  ThemeMode mode = ThemeMode.light,
}) async {
  await tester.pumpWidget(
    ForceLive(
      child: EvolveApp(
        theme: mode,
        contentWidget: CompositeSwt<VComposite>(value: _titleArea(message)),
      ),
    ),
  );
  await tester.pumpAndSettle();
  return tester.widget<TextField>(find.byType(TextField)).decoration!;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  for (final mode in [ThemeMode.light, ThemeMode.dark]) {
    testWidgets(
      'an empty disabled read-only Text paints the banner colour it was given (${mode.name})',
      (tester) async {
        final decoration = await _pumpDecoration(tester, _emptyMessage(), mode: mode);
        final painted = decoration.filled == true ? decoration.fillColor : null;
        expect(
          painted == null || painted == _banner,
          isTrue,
          reason: 'native draws nothing for an empty message; Evolve fills it with $painted',
        );
      },
    );
  }

  testWidgets('an enabled borderless read-only Text paints no ground of its own', (tester) async {
    final decoration = await _pumpDecoration(tester, _emptyMessage(enabled: true));
    expect(decoration.filled, isFalse);
  });

  for (final (name, bit) in [
    ('BORDER', SWT.BORDER),
    ('H_SCROLL', SWT.H_SCROLL),
    ('V_SCROLL', SWT.V_SCROLL),
  ]) {
    testWidgets('a read-only Text with $name keeps its fill', (tester) async {
      final decoration =
          await _pumpDecoration(tester, _emptyMessage(style: _messageStyle | bit, enabled: true));
      expect(decoration.filled, isTrue);
    });
  }

  group('with the application colours on', () {
    setUp(() => setConfigFlags(ConfigFlags()..use_swt_colors = true));

    testWidgets('a borderless read-only Text paints the background it was given', (tester) async {
      final decoration = await _pumpDecoration(tester, _emptyMessage(enabled: true));
      expect(decoration.filled, isTrue);
      expect(decoration.fillColor, _banner);
    });

    testWidgets('a borderless read-only Text that sets no background paints none',
        (tester) async {
      final decoration = await _pumpDecoration(
        tester,
        _emptyMessage(enabled: true, hasOwnBackground: false),
      );
      expect(decoration.filled, isFalse);
    });
  });
}
