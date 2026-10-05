// A Tree with SWT.CHECK draws what the application says. SWT itself never cascades a check to
// children or parents, and a click only toggles the clicked item before the Selection listener
// runs — the listener may then set any item to anything, including putting the clicked one back.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/button_evolve.dart';

const int _treeId = 9100;
const int _parentId = 9110;
const int _childAId = 9111;
const int _childBId = 9112;

VTreeItem _item(
  int id,
  String text, {
  int seq = 1,
  bool checked = false,
  bool grayed = false,
  List<VTreeItem>? items,
  VImage? image,
}) => VTreeItem()
  ..swt = 'TreeItem'
  ..id = id
  ..seq = seq
  ..text = text
  ..checked = checked
  ..grayed = grayed
  ..expanded = items != null
  ..items = items
  ..image = image;

VTree _tree(List<VTreeItem> items) => VTree()
  ..swt = 'Tree'
  ..id = _treeId
  ..seq = 1
  ..style = SWT.CHECK | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..items = items
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 400);

class _RecordingTree extends TreeSwt<VTree> {
  final List<VEvent> checks;

  const _RecordingTree({required super.value, required this.checks});

  @override
  void sendSelectionSelection(VTree val, VEvent? payload) {
    if (payload?.detail == SWT.CHECK) checks.add(payload!);
  }
}

Widget _wrap(VTree value, List<VEvent> checks) => EvolveApp(
  theme: ThemeMode.light,
  contentWidget: SizedBox(
    width: 800,
    height: 400,
    child: _RecordingTree(value: value, checks: checks),
  ),
);

/// Delivers an inbound frame exactly as the transport would (2-byte name length, name, JSON body).
void _receive(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

/// What Java sends when the application calls setChecked/setGrayed on one item.
void _javaSets(VTreeItem item, {required int seq}) =>
    _receive('TreeItem/${item.id}', {...item.toJson(), '_s': seq});

/// A legend swatch, the colour square an inventory row shows next to its name.
VImage _swatch() => VImage()
  ..svgContent =
      '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12">'
      '<rect width="12" height="12" fill="red"/></svg>';

Finder _row(int itemId) => find.byWidgetPredicate(
  (w) => w is TreeItemSwt && w.value.id == itemId,
);

Finder _icon(int itemId) =>
    find.descendant(of: _row(itemId), matching: find.byType(SvgPicture));

/// Pumps frame by frame through a delivery, failing on the first frame a row draws without its icon.
Future<void> _iconsDrawnEveryFrame(WidgetTester tester, List<int> itemIds) async {
  for (var frame = 1; frame <= 5; frame++) {
    await tester.pump();
    for (final id in itemIds) {
      expect(_icon(id), findsOneWidget, reason: 'icon of $id missing in frame $frame');
    }
  }
}

Finder _box(int itemId) => find.byWidgetPredicate(
  (w) => w is ButtonSwt<VButton> && w.value.id == itemId,
);

/// The box as drawn: (selection, grayed) of the state the check box renders from.
(bool, bool) _drawn(WidgetTester tester, int itemId) {
  final state = tester.state<ButtonImpl>(_box(itemId)).state;
  return (state.selection ?? false, state.grayed ?? false);
}

void main() {
  testWidgets('a box follows the check state the application sets', (
    tester,
  ) async {
    final checks = <VEvent>[];
    await tester.pumpWidget(
      _wrap(
        _tree([
          _item(
            _parentId,
            'Reports',
            items: [_item(_childAId, 'Report A'), _item(_childBId, 'Report B')],
          ),
        ]),
        checks,
      ),
    );
    await tester.pumpAndSettle();
    expect(_drawn(tester, _childAId), (false, false));

    _javaSets(_item(_childAId, 'Report A', checked: true), seq: 10);
    _javaSets(
      _item(
        _parentId,
        'Reports',
        checked: true,
        grayed: true,
        items: [
          _item(_childAId, 'Report A', seq: 10, checked: true),
          _item(_childBId, 'Report B'),
        ],
      ),
      seq: 11,
    );
    await tester.pumpAndSettle();

    expect(_drawn(tester, _childAId), (true, false));
    expect(_drawn(tester, _parentId), (true, true));

    _javaSets(_item(_childAId, 'Report A'), seq: 20);
    _javaSets(
      _item(
        _parentId,
        'Reports',
        items: [
          _item(_childAId, 'Report A', seq: 20),
          _item(_childBId, 'Report B'),
        ],
      ),
      seq: 21,
    );
    await tester.pumpAndSettle();

    expect(_drawn(tester, _childAId), (false, false));
    expect(_drawn(tester, _parentId), (false, false));
  });

  testWidgets('a click reports the check and changes no other item', (
    tester,
  ) async {
    final checks = <VEvent>[];
    final childA = _item(_childAId, 'Report A');
    final childB = _item(_childBId, 'Report B');
    final parent = _item(_parentId, 'Reports', items: [childA, childB]);
    await tester.pumpWidget(_wrap(_tree([parent]), checks));
    await tester.pumpAndSettle();

    await tester.tap(_box(_parentId));
    await tester.pumpAndSettle();

    expect(checks, hasLength(1));
    expect(parent.checked, isFalse);
    expect(childA.checked, isFalse);
    expect(childB.checked, isFalse);
    expect(_drawn(tester, _parentId), (false, false));
    expect(_drawn(tester, _childAId), (false, false));
    expect(_drawn(tester, _childBId), (false, false));
  });

  testWidgets('what the application answers a click with is what is drawn', (
    tester,
  ) async {
    final checks = <VEvent>[];
    await tester.pumpWidget(
      _wrap(
        _tree([
          _item(
            _parentId,
            'Well A',
            checked: true,
            items: [
              _item(_childAId, 'Log 1', checked: true),
              _item(_childBId, 'Log 2', checked: true),
            ],
          ),
        ]),
        checks,
      ),
    );
    await tester.pumpAndSettle();

    await tester.tap(_box(_parentId));
    await tester.pumpAndSettle();

    // The application hides the parent and shows its children as partial.
    _javaSets(
      _item(_childAId, 'Log 1', checked: true, grayed: true),
      seq: 10,
    );
    _javaSets(
      _item(_childBId, 'Log 2', checked: true, grayed: true),
      seq: 11,
    );
    _javaSets(
      _item(
        _parentId,
        'Well A',
        items: [
          _item(_childAId, 'Log 1', seq: 10, checked: true, grayed: true),
          _item(_childBId, 'Log 2', seq: 11, checked: true, grayed: true),
        ],
      ),
      seq: 12,
    );
    await tester.pumpAndSettle();

    expect(_drawn(tester, _parentId), (false, false));
    expect(_drawn(tester, _childAId), (true, true));
    expect(_drawn(tester, _childBId), (true, true));

    // A click on a partial box the application puts back: Java answers with the same state.
    await tester.tap(_box(_childAId));
    await tester.pumpAndSettle();
    _javaSets(
      _item(_childAId, 'Log 1', checked: true, grayed: true),
      seq: 20,
    );
    await tester.pumpAndSettle();

    expect(checks, hasLength(2));
    expect(_drawn(tester, _childAId), (true, true));
    expect(_drawn(tester, _childBId), (true, true));
  });
  testWidgets('a check change keeps the row, and its icon stays drawn', (
    tester,
  ) async {
    final checks = <VEvent>[];
    await tester.pumpWidget(
      _wrap(
        _tree([
          _item(_childAId, 'Report A', image: _swatch()),
          _item(_childBId, 'Report B', image: _swatch()),
        ]),
        checks,
      ),
    );
    await tester.pumpAndSettle();
    expect(_icon(_childAId), findsOneWidget);
    final row = tester.state(_row(_childAId));

    _javaSets(
      _item(_childAId, 'Report A', checked: true, image: _swatch()),
      seq: 10,
    );
    await _iconsDrawnEveryFrame(tester, [_childAId, _childBId]);

    expect(tester.state(_row(_childAId)), same(row));
    expect(_drawn(tester, _childAId), (true, false));
  });

  testWidgets('a selection change keeps every row icon drawn', (tester) async {
    final checks = <VEvent>[];
    final childA = _item(_childAId, 'Report A', image: _swatch());
    final childB = _item(_childBId, 'Report B', image: _swatch());
    final tree = _tree([childA, childB]);
    await tester.pumpWidget(_wrap(tree, checks));
    await tester.pumpAndSettle();
    expect(_icon(_childAId), findsOneWidget);
    expect(_icon(_childBId), findsOneWidget);

    _receive('Tree/$_treeId', {
      ...(_tree([
        _item(_childAId, 'Report A', image: _swatch()),
        _item(_childBId, 'Report B', image: _swatch()),
      ])..selection = [_item(_childBId, 'Report B', image: _swatch())]).toJson(),
      '_s': 10,
    });
    await _iconsDrawnEveryFrame(tester, [_childAId, _childBId]);

    expect(tree.selection?.map((item) => item.id), [_childBId]);
  });
}
