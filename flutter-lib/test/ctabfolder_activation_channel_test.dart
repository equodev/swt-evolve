// Activation reaches a CTabFolder on its own channel, so moving between stacks repaints the frame
// without re-sending the folder and the view nested inside it.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

import 'delivery/support/deliver.dart';

const _left = 1000;
const _right = 2000;

void _receiveJson(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VCTabFolder _folder(int id) => VCTabFolder()
  ..swt = 'CTabFolder'
  ..id = id
  ..style = SWT.NONE
  ..selection = 0
  ..enabled = true
  ..visible = true
  ..highlight = false
  ..highlightEnabled = true
  ..items = [
    VCTabItem()
      ..swt = 'CTabItem'
      ..id = id + 1
      ..text = 'tab $id'
      ..control = (VText()
        ..swt = 'Text'
        ..id = id + 2
        ..style = SWT.SINGLE
        ..enabled = true
        ..visible = true
        ..text = 'text $id'
        ..bounds = _rect(0, 32, 280, 30)),
  ];

Future<void> _pump(WidgetTester tester, List<int> ids) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Row(
      children: [
        for (final id in ids)
          SizedBox(width: 300, height: 120, child: CTabFolderSwt<VCTabFolder>(value: _folder(id))),
      ],
    ),
  ));
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}
}

Finder _folderWidget(int id) =>
    find.byWidgetPredicate((w) => w is CTabFolderSwt && w.value.id == id);

Finder _text(int textId) =>
    find.byWidgetPredicate((w) => w is TextSwt && w.value.id == textId);

bool _focusInside(int textId) {
  final focused = FocusManager.instance.primaryFocus?.context;
  return focused != null &&
      find
          .ancestor(of: find.byElementPredicate((e) => e == focused), matching: _text(textId))
          .evaluate()
          .isNotEmpty;
}

Future<void> _focus(WidgetTester tester, int textId) async {
  await tester.tap(find.descendant(of: _text(textId), matching: find.byType(EditableText)),
      warnIfMissed: false);
  await tester.pump();
  while (tester.takeException() != null) {}
  expect(_focusInside(textId), isTrue, reason: 'sanity: the tap must focus Text $textId');
}

Future<void> _activate(WidgetTester tester, int folderId, bool active) async {
  _receiveJson('CTabFolder/$folderId/activation', {'active': active});
  await tester.pump();
  await tester.pump();
  while (tester.takeException() != null) {}
}

bool _framed(WidgetTester tester, int folderId) {
  final frame = tester.widget<Container>(
    find.descendant(of: _folderWidget(folderId), matching: find.byType(Container)).first,
  );
  return (frame.foregroundDecoration as BoxDecoration?)?.border != null;
}

void main() {
  testWidgets('the activation message moves the frame with no value push',
      (WidgetTester tester) async {
    await _pump(tester, [_left, _right]);

    await _activate(tester, _left, true);
    expect(_framed(tester, _left), isTrue,
        reason: 'Java activated the left stack; its pushed highlight is still false');
    expect(_framed(tester, _right), isFalse);

    await _activate(tester, _left, false);
    await _activate(tester, _right, true);
    expect(_framed(tester, _right), isTrue);
    expect(_framed(tester, _left), isFalse,
        reason: 'the stack Java deactivated must drop its frame');
  });

  testWidgets('the activation message keeps focus inside the folder',
      (WidgetTester tester) async {
    await _pump(tester, [_right]);

    await _focus(tester, _right + 2);
    await _activate(tester, _right, true);

    expect(_framed(tester, _right), isTrue);
    expect(_focusInside(_right + 2), isTrue,
        reason: 'framing the folder must not remount its content and drop the focus it holds');
  });

  testWidgets('a pushed highlight still frames the folder and keeps its focus',
      (WidgetTester tester) async {
    await _pump(tester, [_left]);

    await _focus(tester, _left + 2);
    await deliverWhole(_folder(_left)
      ..highlight = true
      ..seq = 2);
    await tester.pump();
    while (tester.takeException() != null) {}

    expect(_framed(tester, _left), isTrue);
    expect(_focusInside(_left + 2), isTrue,
        reason: 'framing the folder must not remount its content and drop the focus it holds');
  });
}
