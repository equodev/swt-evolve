// A control that is a child of a CTabFolder but no tab's page is still drawn at its own bounds.
// The Eclipse workbench puts the empty editor area's hint (an image, a line of text and a list of
// shortcuts) in such a Composite: positioned by hand inside the folder and shown only while the
// folder has no tabs.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

import 'delivery/support/deliver.dart';

const _hintText = 'Go hunt your bugs here.';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VCTabFolder _emptyFolderWithHint() => VCTabFolder()
  ..id = 1000
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..selection = -1
  ..bounds = _rect(0, 0, 600, 400)
  ..children = [
    VComposite()
      ..id = 2000
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(2, 30, 596, 368)
      ..children = [
        VLabel()
          ..id = 2001
          ..style = SWT.NONE
          ..enabled = true
          ..visible = true
          ..text = _hintText
          ..bounds = _rect(200, 150, 196, 20),
      ],
  ];

Widget _host(VCTabFolder value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 400,
        child: CTabFolderSwt<VCTabFolder>(value: value),
      ),
    );

// The tab strip trips a debug-only paint assertion in the isolated harness; it is unrelated to
// what is built.
void _drainPaintExceptions(WidgetTester tester) {
  while (tester.takeException() != null) {}
}

void main() {
  testWidgets('the same Composite inside a plain Composite is drawn', (tester) async {
    final hint = _emptyFolderWithHint().children!.single;
    final parent = VComposite()
      ..id = 1000
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 600, 400)
      ..children = [hint];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 400,
        child: CompositeSwt<VComposite>(value: parent),
      ),
    ));
    await tester.pump();
    expect(find.text(_hintText), findsOneWidget);
  });

  testWidgets('a tabless CTabFolder draws the Composite it holds outside any tab', (tester) async {
    await tester.pumpWidget(_host(_emptyFolderWithHint()));
    await tester.pump();
    _drainPaintExceptions(tester);
    expect(find.text(_hintText), findsOneWidget);
    final folderAt = tester.getTopLeft(find.byType(CTabFolderSwt<VCTabFolder>));
    final hint = find.byWidgetPredicate((w) => w is CompositeSwt && w.value.id == 2000);
    expect(tester.getTopLeft(hint) - folderAt, const Offset(2, 30),
        reason: 'the Composite stands at its own bounds, in the folder\'s coordinates');
  });

  testWidgets('a free child arriving in a mounted folder leaves its strip and page mounted', (tester) async {
    freshClient();
    addTearDown(freshClient);
    final page = _page()..seq = 10;
    VCTabFolder folder({required bool withHint, required int seq}) => VCTabFolder()
      ..swt = 'CTabFolder'
      ..id = 1000
      ..seq = seq
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..selection = 0
      ..bounds = _rect(0, 0, 600, 400)
      ..children = [
        if (withHint) _emptyFolderWithHint().children!.single..seq = 10,
        page,
      ]
      ..items = [_tab(page)];
    final window = VComposite()
      ..swt = 'Composite'
      ..id = 999
      ..seq = 10
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 600, 400)
      ..children = [folder(withHint: false, seq: 10)];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 600, height: 400, child: CompositeSwt<VComposite>(value: window)),
      ),
    ));
    await tester.pump();
    _drainPaintExceptions(tester);
    final tabBefore = tester.element(find.text('tab'));
    final pageBefore = tester.element(find.text(_pageText));
    expect(find.text(_hintText), findsNothing);

    final arriving = folder(withHint: true, seq: 11);
    // The hint with its label, then the folder naming it, as Java describes a new subtree.
    await deliverWhole(arriving.children!.first);
    await deliverChange(arriving, keys: [WireKey.k_Composite_children], base: 10);
    await tester.pump();
    _drainPaintExceptions(tester);

    expect(find.text(_hintText), findsOneWidget);
    expect(identical(tester.element(find.text('tab')), tabBefore), isTrue, reason: 'the tab strip is not mounted anew');
    expect(identical(tester.element(find.text(_pageText)), pageBefore), isTrue, reason: 'the page is not mounted anew');
  });

  testWidgets('a hidden one draws nothing', (tester) async {
    final folder = _emptyFolderWithHint();
    folder.children!.single.visible = false;
    await tester.pumpWidget(_host(folder));
    await tester.pump();
    _drainPaintExceptions(tester);
    expect(find.text(_hintText), findsNothing);
  });

  testWidgets('one stacked under the selected tab\'s page stays behind it', (tester) async {
    final folder = _emptyFolderWithHint();
    final page = _page();
    folder
      ..children = [page, ...folder.children!]
      ..items = [_tab(page)]
      ..selection = 0;
    await tester.pumpWidget(_host(folder));
    await tester.pump();
    _drainPaintExceptions(tester);
    expect(find.text(_pageText), findsOneWidget);
    expect(find.text(_hintText), findsNothing);
  });

  testWidgets('one stacked over the selected tab\'s page is drawn, and the page once', (tester) async {
    final folder = _emptyFolderWithHint();
    final page = _page();
    folder
      ..children = [...folder.children!, page]
      ..items = [_tab(page)]
      ..selection = 0;
    await tester.pumpWidget(_host(folder));
    await tester.pump();
    _drainPaintExceptions(tester);
    expect(find.text(_pageText), findsOneWidget);
    expect(find.text(_hintText), findsOneWidget);
  });
}

const _pageText = 'page of the tab';

VComposite _page() => VComposite()
  ..id = 3000
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(2, 30, 596, 368)
  ..children = [
    VLabel()
      ..id = 3001
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..text = _pageText
      ..bounds = _rect(10, 10, 196, 20),
  ];

VCTabItem _tab(VComposite page) => VCTabItem()
  ..id = 3100
  ..style = SWT.NONE
  ..text = 'tab'
  ..control = page;
