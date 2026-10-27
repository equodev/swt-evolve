// SWT.CLOSE on a folder puts a close button on every tab, and Java reserves the room for it
// (CTabFolderHelper), so the tab has to draw it even though no tab carries the flag itself.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/ctabfolder_theme_extension.dart';

VCTabFolder _folder(int style, {required int id}) => VCTabFolder()
  ..id = id
  ..style = style
  ..enabled = true
  ..selection = 0
  ..items = [
    VCTabItem()
      ..id = id * 100
      ..text = 'One tab'
      ..showClose = false
      ..showing = true,
  ];

Widget _host(Widget folder) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 900, height: 200, child: folder),
    );

void main() {
  Future<double> tabWidth(WidgetTester tester, VCTabFolder value) async {
    await tester.pumpWidget(_host(CTabFolderSwt<VCTabFolder>(value: value)));
    await tester.pump();
    while (tester.takeException() != null) {}
    return tester
        .getSize(find
            .ancestor(of: find.text('One tab'), matching: find.byType(AnimatedContainer))
            .first)
        .width;
  }

  testWidgets('a folder with SWT.CLOSE draws a close button on a tab that does not carry the flag',
      (WidgetTester tester) async {
    final plain = await tabWidth(tester, _folder(SWT.NONE, id: 1));
    final closing = await tabWidth(tester, _folder(SWT.CLOSE, id: 2));
    final theme = Theme.of(tester.element(find.byType(CTabFolderSwt<VCTabFolder>)))
        .extension<CTabFolderThemeExtension>()!;

    expect(
      closing - plain,
      moreOrLessEquals(theme.tabCloseButtonSpacing + theme.tabCloseIconSize, epsilon: 0.5),
    );
  });
}
