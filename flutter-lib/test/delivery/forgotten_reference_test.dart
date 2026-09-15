// A reference to a widget the client has let go of.
//
// The sender writes a name instead of a description when its own record says the widget was
// delivered. The client forgets a widget when the last thing describing it stops carrying it. Both
// are right, and they disagree: a tree node that collapses drops its children from what is sent, so
// the client forgets them, and expanding it again names widgets the client no longer holds.
//
// Rendered, that is an expanded node with blank children, or a restored tab stack whose tabs have
// no labels. The widget has to be asked for instead - and the answer has to have somewhere to land.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

const int _treeId = 1;
const int _rootItemId = 11;
const int _childItemId = 12;

Map<String, dynamic> _childWhole(String text, int seq) =>
    {'swt': 'TreeItem', 'id': _childItemId, '_s': seq, 'style': SWT.NONE, 'texts': [text]};

Map<String, dynamic> _reference(int id, String swt) =>
    {'swt': swt, 'id': id, 'style': SWT.NONE, '_r': 1};

Map<String, dynamic> _tree(List<Map<String, dynamic>> rootItems, int seq) =>
    {'swt': 'Tree', 'id': _treeId, '_s': seq, 'style': SWT.NONE, 'items': rootItems};

Map<String, dynamic> _rootItem(List<Map<String, dynamic>>? children, int seq) => {
      'swt': 'TreeItem',
      'id': _rootItemId,
      '_s': seq,
      'style': SWT.NONE,
      'texts': ['root'],
      if (children != null) 'items': children,
    };

Future<void> _deliver(String channel, Object payload) async {
  final action = utf8.encode(channel);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + action.length + body.length);
  frame[0] = (action.length >> 8) & 0xFF;
  frame[1] = action.length & 0xFF;
  frame.setRange(2, 2 + action.length, action);
  frame.setRange(2 + action.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
  await Future<void>.microtask(() {});
}

void main() {
  late VRegistry registry;

  setUp(() => registry = VRegistry());

  test('a reference is recognised wherever it appears, not only among children', () {
    // Children decode through the shared widget decoder and always carried the marker; items decode
    // through their own class and did not, so a reference to one read as a real widget with nothing
    // in it - which is precisely a blank row.
    final tree = VTree.fromJson(_tree([_reference(_rootItemId, 'TreeItem')], 30));

    expect(tree.items!.single.isReference, isTrue,
        reason: 'an item named rather than described has to be recognisable as such');
  });

  test('a reference to a widget still held resolves to it', () async {
    registry.register(VTree.fromJson(_tree([_rootItem([_childWhole('leaf', 10)], 10)], 10)));

    await _deliver('Tree/$_treeId', _tree([_reference(_rootItemId, 'TreeItem')], 30));

    expect((registry.valueOn('TreeItem/$_childItemId') as VTreeItem).texts!.single, 'leaf');
  });

  test('a reference to a widget the client let go of is asked for, not rendered empty', () async {
    // Expanded: the whole subtree is described.
    registry.register(VTree.fromJson(_tree([_rootItem([_childWhole('leaf', 10)], 10)], 10)));
    expect(registry.valueOn('TreeItem/$_childItemId'), isNotNull);

    // Collapsed: the node stops carrying its children, so nothing describes them any more.
    await _deliver('Tree/$_treeId', _tree([_rootItem(null, 20)], 20));
    expect(registry.valueOn('TreeItem/$_childItemId'), isNull,
        reason: 'precondition: the client has genuinely forgotten it');

    // Expanded again. The sender's own record still says this widget was delivered, so it names it.
    await _deliver(
        'Tree/$_treeId',
        _tree([
          {
            'swt': 'TreeItem',
            'id': _rootItemId,
            '_s': 30,
            'style': SWT.NONE,
            'texts': ['root'],
            'items': [_reference(_childItemId, 'TreeItem')],
          }
        ], 30));

    expect(registry.valueOn('TreeItem/$_childItemId'), isNull,
        reason: 'the empty shell that arrived is not the widget, and holding it as one is the blank '
            'row the user sees');

    // The answer to that request, on the widget's own channel.
    await _deliver('TreeItem/$_childItemId', _childWhole('leaf', 40));

    expect((registry.valueOn('TreeItem/$_childItemId') as VTreeItem).texts!.single, 'leaf',
        reason: 'asking is only half of it - there has to be an entry listening for the answer');
  });

  test('what arrives fills in the object the tree is drawing', () async {
    // The row is drawn from the item list the tree holds, so the content has to land in the object
    // that list is pointing at. Anything else renders the node with nothing in it - the row is
    // there, indented and expandable, and has no text.
    registry.register(VTree.fromJson(_tree([_rootItem([_childWhole('leaf', 10)], 10)], 10)));
    await _deliver('Tree/$_treeId', _tree([_rootItem(null, 20)], 20));

    await _deliver(
        'Tree/$_treeId',
        _tree([
          {
            'swt': 'TreeItem',
            'id': _rootItemId,
            '_s': 30,
            'style': SWT.NONE,
            'texts': ['root'],
            'items': [_reference(_childItemId, 'TreeItem')],
          }
        ], 30));
    await _deliver('TreeItem/$_childItemId', _childWhole('leaf', 40));

    final drawnByTheTree = (registry.valueOn('TreeItem/$_rootItemId') as VTreeItem).items!.single;
    expect(drawnByTheTree.texts!.single, 'leaf',
        reason: 'the list held this object before the widget arrived and holds it still');
    expect(identical(drawnByTheTree, registry.valueOn('TreeItem/$_childItemId')), isTrue,
        reason: 'one widget, one object - whoever named it and whoever answered for it agree');
  });
  test('a frame refused as a description of the past still hands over what it alone describes',
      () async {
    // Where the references above came from, on an Eclipse startup. Java describes one widget twice
    // in a flush; the two frames reach the client out of the order they were written, so the older
    // one arrives second and is refused - rightly, since taking it would rewind the widget.
    //
    // But a whole frame carries everything beneath it, and the sender counts each of those
    // delivered by writing them: from then on it names them instead of describing them. The refused
    // frame was the only description of the item below, so dropping it whole left the next
    // reference pointing at a widget the client had never been given.
    const int treeId = 71;
    const int itemId = 72;

    Map<String, dynamic> tree(List<Map<String, dynamic>> items, int seq) =>
        {'swt': 'Tree', 'id': treeId, '_s': seq, 'style': SWT.NONE, 'items': items};
    Map<String, dynamic> item(int seq) =>
        {'swt': 'TreeItem', 'id': itemId, '_s': seq, 'style': SWT.NONE, 'texts': ['described once']};

    // The later of the two frames arrives first, and carries no item.
    registry.register(VTree.fromJson(tree([], 40)));

    // The earlier one arrives second. It describes the tree as it used to be - and the item as
    // nothing else ever will.
    await _deliver('Tree/$treeId', tree([item(39)], 30));

    expect((registry.valueOn('TreeItem/$itemId') as VTreeItem?)?.texts?.single, 'described once',
        reason: 'the item was in the payload and nothing else has ever described it');
    expect((registry.valueOn('Tree/$treeId') as VTree).items, isEmpty,
        reason: 'the tree itself is not rewound: that frame is still older than what it holds');
  });
}
