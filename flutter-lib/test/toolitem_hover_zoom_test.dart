// A ToolItem grows its icon while the pointer is over it, in every ToolBar, unless the application
// opts out with disable_hover_zoom.
//
// The two properties that keep it safe: a disabled item never reacts, and it is a paint-time scale
// -- the item keeps the box Sizes.computeSize laid the toolbar out from, so hovering can never
// reflow the row.
//
// A CHECK item carrying text but no image is used as the probe: it renders a plain Icon, so what
// the test measures is the transform and not an asset load. (Text is what keeps the item in the
// bar at all -- ToolBarImpl.getToolItems drops a non-separator item with neither text nor image.)

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/toolitem_theme_extension.dart';

const _barWidth = 200.0;
const _barHeight = 48.0;

VToolBar _bar({required bool enabled}) => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 2
      ..style = SWT.CHECK
      ..enabled = enabled
      ..selection = false
      ..text = 'Toggle'
      ..toolTipText = 'Toggle',
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _barWidth.toInt()
    ..height = _barHeight.toInt());

/// A plain standalone ToolBar, the way most of an application's toolbars are rendered.
Widget _host({bool enabled = true}) => EvolveApp(
  theme: ThemeMode.light,
  contentWidget: SizedBox(
    width: _barWidth,
    height: _barHeight,
    child: ToolBarSwt(value: _bar(enabled: enabled)),
  ),
);

/// The same bar rendered through a trim composite, under the given `swt` name.
Widget _hostComposite({required String swt}) => EvolveApp(
  theme: ThemeMode.light,
  contentWidget: SizedBox(
    width: _barWidth,
    height: _barHeight,
    child: ToolbarComposite(
      value: VComposite()
        ..id = 10
        ..swt = swt
        ..style = SWT.NONE
        ..enabled = true
        ..visible = true
        ..bounds = (VRectangle()
          ..x = 0
          ..y = 0
          ..width = _barWidth.toInt()
          ..height = _barHeight.toInt())
        ..children = [_bar(enabled: true)],
      useBoundsLayout: true,
    ),
  ),
);

final _icon = find.byIcon(Icons.check_box_outline_blank);

/// Rect of the probe icon and of the item that owns it, once the hover animation has settled.
Future<(Rect icon, Rect item)> _settle(WidgetTester tester) async {
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}
  return (tester.getRect(_icon), tester.getRect(find.byType(ToolItemSwt)));
}

/// Moves the pointer onto the probe icon and lets the animation finish.
Future<TestGesture> _hover(WidgetTester tester) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await gesture.addPointer(location: Offset.zero);
  addTearDown(gesture.removePointer);
  await gesture.moveTo(tester.getCenter(_icon));
  await tester.pumpAndSettle();
  return gesture;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('opted out: hovering leaves the icon exactly as it was', (tester) async {
    setConfigFlags(ConfigFlags()..disable_hover_zoom = true);
    await tester.pumpWidget(_host());
    final (iconAtRest, itemAtRest) = await _settle(tester);

    await _hover(tester);
    final (iconHovered, itemHovered) = await _settle(tester);

    expect(iconHovered, iconAtRest);
    expect(itemHovered, itemAtRest);
  });

  testWidgets('the icon grows on hover and shrinks back on exit', (tester) async {
    await tester.pumpWidget(_host());
    final (iconAtRest, _) = await _settle(tester);

    final gesture = await _hover(tester);
    final (iconHovered, _) = await _settle(tester);
    expect(iconHovered.width, greaterThan(iconAtRest.width));
    expect(
      iconHovered.center.dx,
      closeTo(iconAtRest.center.dx, 0.01),
      reason: 'the icon must grow around its own centre, not drift',
    );

    await gesture.moveTo(const Offset(-100, -100));
    final (iconAfterExit, _) = await _settle(tester);
    expect(iconAfterExit, iconAtRest);
  });

  testWidgets('the zoom is paint-only -- the item keeps its laid-out box', (tester) async {
    await tester.pumpWidget(_host());
    final (_, itemAtRest) = await _settle(tester);

    await _hover(tester);
    final (_, itemHovered) = await _settle(tester);

    expect(itemHovered, itemAtRest);
  });

  testWidgets("the theme's scale is the one applied", (tester) async {
    await tester.pumpWidget(_host());
    final (iconAtRest, _) = await _settle(tester);

    final theme = Theme.of(
      tester.element(find.byType(ToolItemSwt)),
    ).extension<ToolItemThemeExtension>()!;

    await _hover(tester);
    final (iconHovered, _) = await _settle(tester);

    expect(iconHovered.width, closeTo(iconAtRest.width * theme.hoverZoomScale, 0.5));
  });

  testWidgets('a disabled item does not zoom', (tester) async {
    await tester.pumpWidget(_host(enabled: false));
    final (iconAtRest, _) = await _settle(tester);

    await _hover(tester);
    final (iconHovered, _) = await _settle(tester);

    expect(iconHovered, iconAtRest);
  });

  // Deliberately not scoped to one toolbar: the trim's own bars zoom on the same terms as a plain
  // one, so the whole application reads consistently.
  for (final swt in const ['MainToolbar', 'StatusBar']) {
    testWidgets('a ToolBar inside the $swt trim zooms too', (tester) async {
      await tester.pumpWidget(_hostComposite(swt: swt));
      final (iconAtRest, _) = await _settle(tester);

      await _hover(tester);
      final (iconHovered, _) = await _settle(tester);

      expect(iconHovered.width, greaterThan(iconAtRest.width));
    });
  }
}
