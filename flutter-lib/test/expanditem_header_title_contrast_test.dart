// An ExpandItem header's title has to stay readable on the ground the header paints, expanded as
// well as collapsed, in the light and the dark theme, whether the theme or the application picks
// the title's colour.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/expandbar.dart';
import 'package:swtflutter/src/gen/expanditem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/expanditem_header.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/expanditem_theme_extension.dart';

// Each mount takes fresh ids: a value is held per id across mounts.
var _nextId = 100;

VColor _vColor(int r, int g, int b) => VColor()
  ..alpha = 0xFF
  ..red = r
  ..green = g
  ..blue = b;

VExpandBar _bar({VColor? foreground}) => VExpandBar()
  ..id = _nextId++
  ..style = SWT.NONE
  ..enabled = true
  ..foreground = foreground
  ..spacing = 0
  ..items = [
    VExpandItem()
      ..id = _nextId++
      ..text = 'Expanded'
      ..expanded = true
      ..headerHeight = 24,
    VExpandItem()
      ..id = _nextId++
      ..text = 'Collapsed'
      ..expanded = false
      ..headerHeight = 24,
  ];

Future<void> _pump(WidgetTester tester, ThemeMode mode, {VColor? foreground}) async {
  await tester.binding.setSurfaceSize(const Size(400, 300));
  addTearDown(() => tester.binding.setSurfaceSize(null));
  await tester.pumpWidget(
    EvolveApp(
      theme: mode,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, child: ExpandBarSwt(value: _bar(foreground: foreground))),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

/// The title's colour as drawn, and the ground of the header it sits in.
({Color text, Color ground}) _header(WidgetTester tester, String title) {
  final paragraph = tester.renderObject<RenderParagraph>(find.text(title));
  final header = tester.widget<ExpandItemHeader>(
    find.ancestor(of: find.text(title), matching: find.byType(ExpandItemHeader)),
  );
  return (text: paragraph.text.style!.color!, ground: header.color!);
}

void main() {
  for (final mode in [ThemeMode.light, ThemeMode.dark]) {
    for (final title in ['Expanded', 'Collapsed']) {
      testWidgets('${mode.name}: the ${title.toLowerCase()} header title is legible on its ground',
          (tester) async {
        await _pump(tester, mode);
        final header = _header(tester, title);
        expect(
          contrastRatio(header.text, header.ground),
          greaterThanOrEqualTo(kMinTextContrast),
          reason: 'title ${header.text} on ground ${header.ground}',
        );
      });
    }
  }

  group('with the application\'s foreground', () {
    tearDown(resetConfigFlags);

    // The bar's text colour as an application on that theme would pick it: dark in light, light in dark.
    final appForeground = {
      ThemeMode.light: _vColor(0x1F, 0x23, 0x28),
      ThemeMode.dark: _vColor(0xE6, 0xE6, 0xE6),
    };

    for (final mode in [ThemeMode.light, ThemeMode.dark]) {
      for (final title in ['Expanded', 'Collapsed']) {
        testWidgets('${mode.name}: the ${title.toLowerCase()} header title is legible on its ground',
            (tester) async {
          setConfigFlags(ConfigFlags()..use_swt_font_colors = true);
          await _pump(tester, mode, foreground: appForeground[mode]);
          final header = _header(tester, title);
          expect(
            contrastRatio(header.text, header.ground),
            greaterThanOrEqualTo(kMinTextContrast),
            reason: 'title ${header.text} on ground ${header.ground}',
          );
        });
      }

      testWidgets('${mode.name}: the hovered collapsed header title is legible on its ground',
          (tester) async {
        setConfigFlags(ConfigFlags()..use_swt_font_colors = true);
        await _pump(tester, mode, foreground: appForeground[mode]);
        final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
        await mouse.addPointer(location: Offset.zero);
        addTearDown(mouse.removePointer);
        await mouse.moveTo(tester.getCenter(find.text('Collapsed')));
        await tester.pumpAndSettle();
        final header = _header(tester, 'Collapsed');
        expect(header.ground, Theme.of(tester.element(find.text('Collapsed')))
            .extension<ExpandItemThemeExtension>()!.headerBackgroundHoveredColor);
        expect(
          contrastRatio(header.text, header.ground),
          greaterThanOrEqualTo(kMinTextContrast),
          reason: 'title ${header.text} on ground ${header.ground}',
        );
      });
    }
  });
}
