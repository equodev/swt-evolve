// A new title image on a CTabItem (an editor gaining its error decorator) keeps the icon already
// shown in the tab until the new one is ready, never an empty frame in its place.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/ctab_folder_fixtures.dart';
import 'support/icon_renders.dart';
import 'support/java_push.dart';

const _plainColor = Color(0xFF2060C0);
const _decoratedColor = Color(0xFFC02020);

void main() {
  setUp(() {
    resetConfigFlags();
    ImageUtils.clearCache();
  });
  tearDown(resetConfigFlags);

  testWidgets('a new CTabItem image keeps the old icon on screen until the new one is ready',
      (tester) async {
    final plain = await iconPng(tester, color: _plainColor);
    final decorated = await iconPng(tester, color: _decoratedColor);

    await tester.pumpWidget(ctabFolderWithIcon(plain, tabText: 'Hello.java'));
    await settleIconRenders(tester);
    expect(ctabIcon, findsOneWidget, reason: 'the first image is drawn');
    expect(await paintedColorAtCenter(tester, ctabIcon), _plainColor,
        reason: 'the first image is drawn in its own colour');

    // Java sends the new image, and one frame is pumped: no render can have landed yet.
    await javaPushes(tester, ctabItem, (state) => state.image = iconImage(decorated));
    expect(ctabIcon, findsOneWidget,
        reason: 'the frame that sets the new image lost the icon already shown');

    await settleIconRenders(tester);
    expect(ctabIcon, findsOneWidget, reason: 'the new image is drawn');
    expect(await paintedColorAtCenter(tester, ctabIcon), _decoratedColor,
        reason: 'the tab does not draw the new image');
  });

  testWidgets('no frame of a CTabItem image change draws the tab without icon pixels',
      (tester) async {
    final plain = await iconPng(tester, color: _plainColor);
    final decorated = await iconPng(tester, color: _decoratedColor);

    await tester.pumpWidget(ctabFolderWithIcon(plain, tabText: 'Hello.java'));
    await settleIconRenders(tester);
    expect(await paintedColorAtCenter(tester, ctabIcon), _plainColor,
        reason: 'the first image is drawn in its own colour');

    // A bitmap decodes on the real clock: let it run a little between frames, as a browser does,
    // and check every frame until the new image is shown.
    await javaPushes(tester, ctabItem, (state) => state.image = iconImage(decorated));
    final raw = find.descendant(of: ctabIcon, matching: find.byType(RawImage));
    for (var frame = 0; frame < 50; frame++) {
      expect(tester.widget<RawImage>(raw).image, isNotNull,
          reason: 'frame $frame draws the tab with an icon that has no pixels yet');
      await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 1)));
      await tester.pump(const Duration(milliseconds: 16));
    }
    await settleIconRenders(tester);
    expect(await paintedColorAtCenter(tester, ctabIcon), _decoratedColor,
        reason: 'the tab does not draw the new image');
  });
}
