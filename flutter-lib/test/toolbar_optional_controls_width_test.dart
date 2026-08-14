// The optional controls row (theme palette / scaling control) renders at the trailing end of the
// main toolbar on the Flutter side only. Java reserves room for it in
// DartMainToolbar.widthReserve() so the trim layout never hands that strip to a contribution —
// and a reserve is only correct while both sides agree on the number.
//
// This pins the collapsed width the Java constant assumes:
// dev.equo.swt.size.ToolbarControlsSizes.COLLAPSED_WIDTH.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/controls_item.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// Keep in sync with ToolbarControlsSizes.COLLAPSED_WIDTH.
const double _collapsedWidth = 26;

Future<double> _rowWidth(WidgetTester tester, ConfigFlags flags) async {
  setConfigFlags(flags);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: const Align(
      alignment: Alignment.topLeft,
      child: ToolbarOptionalControlsRow(useBoundsLayout: true),
    ),
  ));
  await tester.pump();
  final finder = find.byType(ToolbarOptionalControlsRow);
  return tester.getSize(finder).width;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('the collapsed row is exactly the width Java reserves', (tester) async {
    final width = await _rowWidth(tester, ConfigFlags()..show_theme_color_palette = true);

    expect(width, _collapsedWidth,
        reason: 'ToolbarControlsSizes.COLLAPSED_WIDTH must match what this actually renders');
  });

  testWidgets('the scaling control alone reserves the same chevron', (tester) async {
    // Both flags share one chevron, which is why a single Java constant covers either.
    final width = await _rowWidth(tester, ConfigFlags()..show_scaling_control = true);

    expect(width, _collapsedWidth);
  });

  testWidgets('with neither flag on the row takes no width at all', (tester) async {
    final width = await _rowWidth(tester, ConfigFlags());

    expect(width, 0, reason: 'nothing to reserve when no build flag asked for these controls');
  });
}
