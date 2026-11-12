// A tab whose item is dirty (CTabItem.setShowDirty) in a folder that shows dirty items that way
// (CTabFolder.setDirtyIndicatorStyle) draws a dot where its close button is, as SWT's renderer does: an
// editor with unsaved changes. The dot gives way to the close button while the pointer is on it.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/swt.dart';

VCTabFolder _folder({required bool indicatorStyle, required bool dirty}) => VCTabFolder()
  ..id = 1
  ..style = SWT.CLOSE
  ..enabled = true
  ..selection = 0
  ..dirtyIndicatorStyle = indicatorStyle
  ..items = [
    VCTabItem()
      ..id = 10
      ..text = 'Editor.java'
      ..showClose = true
      ..showDirty = dirty
      ..showing = true,
  ];

Future<void> _pump(WidgetTester tester, VCTabFolder folder) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 600, height: 240, child: CTabFolderSwt<VCTabFolder>(value: folder)),
  ));
  await tester.pumpAndSettle();
}

final _dot = find.byWidgetPredicate(
    (w) => w is DecoratedBox && w.decoration is BoxDecoration && (w.decoration as BoxDecoration).shape == BoxShape.circle);
final _close = find.byIcon(Icons.close);

void main() {
  testWidgets('a dirty tab shows a dot instead of its close button', (tester) async {
    await _pump(tester, _folder(indicatorStyle: true, dirty: true));

    expect(_dot, findsOneWidget);
    expect(_close, findsNothing);
  });

  testWidgets('the dot gives way to the close button under the pointer', (tester) async {
    await _pump(tester, _folder(indicatorStyle: true, dirty: true));
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: Offset.zero);
    addTearDown(mouse.removePointer);

    await mouse.moveTo(tester.getCenter(_dot));
    await tester.pumpAndSettle();

    expect(_close, findsOneWidget);
    expect(_dot, findsNothing);
  });

  testWidgets('a clean tab shows its close button', (tester) async {
    await _pump(tester, _folder(indicatorStyle: true, dirty: false));

    expect(_close, findsOneWidget);
    expect(_dot, findsNothing);
  });

  testWidgets('a folder that does not use the dot style shows no dot for a dirty tab', (tester) async {
    await _pump(tester, _folder(indicatorStyle: false, dirty: true));

    expect(_dot, findsNothing);
    expect(_close, findsOneWidget);
  });
}
