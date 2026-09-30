// A tab's drawn width is also its width in Java: the tab bounds Java lays out are what answers
// `CTabFolder.getItem(Point)`, and the workbench asks that to decide which view a drag picked up.
// So the geometry drawn here is mirrored by CTabFolderHelper, and this pins the side that draws it:
// tabHorizontalPadding on each side of the label, and the close button's spacing plus its icon when
// the button is there. Changing either without the Java side moves the wrong view on a drag.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/ctabfolder_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/ctabitem_theme_extension.dart';

VCTabFolder _folder(List<String> labels, {bool close = false, int id = 1}) =>
    VCTabFolder()
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..selection = 0
  ..items = List.generate(
    labels.length,
    (i) => VCTabItem()
      ..id = id * 100 + i
      ..text = labels[i]
      ..showClose = close
      ..showing = true,
  );

Widget _host(Widget folder) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 900, height: 200, child: folder),
    );

/// The box that holds one tab: the container the padding is applied to.
Finder _tabBox(String label) => find
    .ancestor(of: find.text(label), matching: find.byType(AnimatedContainer))
    .first;

void main() {
  late CTabFolderThemeExtension theme;
  late CTabItemThemeExtension itemTheme;

  Future<void> pumpFolder(WidgetTester tester, VCTabFolder value) async {
    await tester.pumpWidget(_host(CTabFolderSwt<VCTabFolder>(value: value)));
    await tester.pump();
    while (tester.takeException() != null) {}
    final themeData = Theme.of(tester.element(find.byType(CTabFolderSwt<VCTabFolder>)));
    theme = themeData.extension<CTabFolderThemeExtension>()!;
    itemTheme = themeData.extension<CTabItemThemeExtension>()!;
  }

  double labelWidth(WidgetTester tester, String label) {
    final text = tester.widget<Text>(find.text(label));
    final painter = TextPainter(
      text: TextSpan(text: label, style: text.style),
      textDirection: TextDirection.ltr,
    )..layout();
    return painter.width;
  }

  testWidgets('a tab is its label plus the horizontal padding on each side',
      (WidgetTester tester) async {
    await pumpFolder(tester, _folder(['Section 3 (TVDSS)']));

    expect(
      tester.getSize(_tabBox('Section 3 (TVDSS)')).width,
      moreOrLessEquals(
        labelWidth(tester, 'Section 3 (TVDSS)') +
            2 * theme.tabHorizontalPadding +
            itemTheme.tabItemHorizontalPadding,
        epsilon: 0.5,
      ),
      reason: 'CTabFolderHelper computes this same width on the Java side',
    );
  });

  testWidgets('the close button adds its spacing and its icon, and nothing else',
      (WidgetTester tester) async {
    await pumpFolder(tester, _folder(['One tab'], close: true, id: 2));
    final withClose = tester.getSize(_tabBox('One tab')).width;

    await pumpFolder(tester, _folder(['One tab'], id: 3));
    final withoutClose = tester.getSize(_tabBox('One tab')).width;

    expect(
      withClose - withoutClose,
      moreOrLessEquals(
        theme.tabCloseButtonSpacing + theme.tabCloseIconSize,
        epsilon: 0.5,
      ),
    );
  });

  testWidgets('two labels of the same length but different extents differ by exactly that',
      (WidgetTester tester) async {
    await pumpFolder(tester, _folder(['llllllllll', 'WWWWWWWWWW']));

    final narrow = tester.getSize(_tabBox('llllllllll')).width;
    final wide = tester.getSize(_tabBox('WWWWWWWWWW')).width;

    expect(
      wide - narrow,
      moreOrLessEquals(
        labelWidth(tester, 'WWWWWWWWWW') - labelWidth(tester, 'llllllllll'),
        epsilon: 0.5,
      ),
      reason: 'a width counted in characters rather than measured would make these equal',
    );
  });
}
