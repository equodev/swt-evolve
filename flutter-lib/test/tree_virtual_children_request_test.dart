// A VIRTUAL tree loads only a first page of an open item's children; there is no paint loop on the
// Java side to ask for the rest, so the client asks for the rows it is about to show.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'delivery/support/deliver.dart';

const int _treeId = 710;
const int _groupId = 711;

class _CapturingTreeSwt extends TreeSwt<VTree> {
  const _CapturingTreeSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTree val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VTree _virtualTreeWithOpenGroup({required int childCount, required int loaded, int seq = 1}) =>
    VTree()
  ..id = _treeId
  ..seq = seq
  ..style = SWT.VIRTUAL
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 400)
  ..items = [
    VTreeItem()
      ..id = _groupId
      ..seq = seq
      ..text = 'General Registers'
      ..expanded = true
      ..itemCount = childCount
      ..items = [
        for (var i = 0; i < loaded; i++)
          VTreeItem()
            ..id = 1000 + i
            ..seq = seq
            ..text = 'r$i'
      ],
  ];

Future<List<VEvent>> _setDataRequests(WidgetTester tester, VTree value) async {
  final requests = <VEvent>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 300,
      height: 400,
      child: _CapturingTreeSwt(
        value: value,
        onEvent: (ev, payload) {
          if (ev == 'SetData/SetData' && payload != null) requests.add(payload);
        },
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return requests;
}

void main() {
  testWidgets('asks for the children of an open item that the viewport reaches past the loaded ones',
      (tester) async {
    final requests =
        await _setDataRequests(tester, _virtualTreeWithOpenGroup(childCount: 98, loaded: 4));

    expect(requests, isNotEmpty, reason: 'the rows after r3 are on screen and nothing else asks for them');
    expect(requests.last.index, _groupId);
    expect(requests.last.end, greaterThan(4));
    expect(requests.last.end, lessThanOrEqualTo(98));
  });

  testWidgets('asks for nothing while the loaded children already reach past the viewport',
      (tester) async {
    final requests =
        await _setDataRequests(tester, _virtualTreeWithOpenGroup(childCount: 10000, loaded: 60));

    expect(requests, isEmpty,
        reason: 'SWT allows SetData for about three times the rows on screen, not the whole item');
  });

  testWidgets('asks again for the rows of an item whose children were loaded anew', (tester) async {
    final requests =
        await _setDataRequests(tester, _virtualTreeWithOpenGroup(childCount: 98, loaded: 4));
    expect(requests, hasLength(1));

    // Java answers with the rows asked for.
    await deliverWhole(_virtualTreeWithOpenGroup(childCount: 98, loaded: 30, seq: 2));
    await tester.pumpAndSettle();
    expect(requests, hasLength(1));

    // A viewer refresh empties the item and gives it its count back: only a page is loaded again.
    await deliverWhole(_virtualTreeWithOpenGroup(childCount: 98, loaded: 4, seq: 3));
    await tester.pumpAndSettle();

    expect(requests, hasLength(2),
        reason: 'the rows asked for before were thrown away with the children');
    expect(requests.last.index, _groupId);
  });
}
