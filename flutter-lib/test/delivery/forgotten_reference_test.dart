// A reference to a widget the client has let go of.
//
// The sender names a widget by id instead of describing it when its own record says the widget was
// delivered. The client forgets a widget when the last thing describing it stops carrying it. Both
// are right, and they disagree: a tree node that collapses drops its children from what is sent, so
// the client forgets them, and expanding it again names widgets the client no longer holds.
//
// Rendered, that is an expanded node with blank children, or a restored tab stack whose tabs have
// no labels. The widget has to be asked for instead - and the answer has to land in what draws it.

import 'dart:async';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'support/deliver.dart';

/// Every frame received so far applied: when the registry forgets what was let go of.
Future<void> _applied() {
  final applied = Completer<void>();
  EquoCommBase.whenApplied(applied.complete);
  return applied.future;
}

const int _treeId = 1;
const int _rootItemId = 11;
const int _childItemId = 12;

VTreeItem _item(int id, String text, int seq, {List<VTreeItem>? items}) => VTreeItem()
  ..id = id
  ..seq = seq
  ..style = SWT.NONE
  ..texts = [text]
  ..items = items;

VTreeItem _child(String text, int seq) => _item(_childItemId, text, seq);

VTreeItem _root(List<VTreeItem>? children, int seq) => _item(_rootItemId, 'root', seq, items: children);

VTree _tree(List<VTreeItem> rootItems, int seq) => VTree()
  ..id = _treeId
  ..seq = seq
  ..style = SWT.NONE
  ..items = rootItems;

/// The ids the client asked Java to describe again, in order.
List<String> _asked() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .where((frame) => frame.$1 == widgetRefreshChannel)
    .map((frame) => frame.$2)
    .toList();

void main() {
  late VRegistry registry;

  setUp(() {
    registry = VRegistry();
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });

  tearDown(() {
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
  });

  test('a widget named while still held resolves to it', () {
    applyWhole(_tree([_root([_child('leaf', 10)], 10)], 10), registry: registry);

    applyWhole(_tree([_root(null, 10)], 30), registry: registry, named: {_rootItemId});

    expect((registry.valueOn('TreeItem/$_childItemId') as VTreeItem).texts!.single, 'leaf');
    expect(identical((registry.valueOn('Tree/$_treeId') as VTree).items!.single,
            registry.valueOn('TreeItem/$_rootItemId')),
        isTrue,
        reason: 'an item named rather than described is the one already held');
    expect(_asked(), isEmpty);
  });

  test('a widget named after the client let go of it is asked for, not rendered empty', () async {
    // Expanded: the whole subtree is described.
    applyWhole(_tree([_root([_child('leaf', 10)], 10)], 10), registry: registry);
    expect(registry.valueOn('TreeItem/$_childItemId'), isNotNull);

    // Collapsed: the node stops carrying its children, so nothing describes them any more.
    applyWhole(_tree([_root(null, 20)], 20), registry: registry);
    await _applied();
    expect(registry.valueOn('TreeItem/$_childItemId'), isNull,
        reason: 'precondition: the client has genuinely forgotten it');

    // Expanded again. The sender's own record still says this widget was delivered, so it names it.
    applyWhole(_tree([_root([_child('leaf', 10)], 30)], 30), registry: registry, named: {_childItemId});

    expect(registry.valueOn('TreeItem/$_childItemId'), isNull,
        reason: 'nothing described it, and an empty shell held as it is the blank row the user sees');
    expect(_asked(), ['$_childItemId', '$_rootItemId'],
        reason: 'the widget is asked for, then what named it, so the name resolves once both arrive');

    // The answer.
    applyWhole(_child('leaf', 40), registry: registry);

    expect((registry.valueOn('TreeItem/$_childItemId') as VTreeItem).texts!.single, 'leaf');
  });

  test('what arrives lands in the object the tree draws', () {
    // The row is drawn from the item list the tree holds, so that list has to point at the object
    // the answer filled. Anything else renders the node with nothing in it - the row is there,
    // indented and expandable, and has no text.
    applyWhole(_tree([_root([_child('leaf', 10)], 10)], 10), registry: registry);
    applyWhole(_tree([_root(null, 20)], 20), registry: registry);
    applyWhole(_tree([_root([_child('leaf', 10)], 30)], 30), registry: registry, named: {_childItemId});

    // Java answers both requests: the child, then the item that named it.
    applyWhole(_child('leaf', 40), registry: registry);
    applyWhole(_root([_child('leaf', 40)], 41), registry: registry);

    final drawnByTheTree = (registry.valueOn('TreeItem/$_rootItemId') as VTreeItem).items!.single;
    expect(drawnByTheTree.texts!.single, 'leaf');
    expect(identical(drawnByTheTree, registry.valueOn('TreeItem/$_childItemId')), isTrue,
        reason: 'one widget, one object - whoever named it and whoever answered for it agree');
  });

  test('an answer still lands when what rendered the widget before it moved lets go afterwards', () {
    // A widget moved to another parent: the old parent's record drops it, the new one names it, and
    // the client asks. The state that drew it in the old place unmounts only after that, and stops
    // watching the channel the answer is about to arrive on.
    applyWhole(_tree([_root([_child('leaf', 10)], 10)], 10), registry: registry);
    void rendered(VChange _) {}
    registry.watch('TreeItem/$_childItemId', rendered);
    applyWhole(_tree([_root(null, 20)], 20), registry: registry);
    applyWhole(_tree([_root([_child('leaf', 10)], 30)], 30), registry: registry, named: {_childItemId});

    registry.unwatch('TreeItem/$_childItemId', rendered);
    applyWhole(_child('leaf', 40), registry: registry);

    expect((registry.valueOn('TreeItem/$_childItemId') as VTreeItem?)?.texts?.single, 'leaf');
  });

  test('a record refused as a description of the past does not cost the records beside it', () {
    // Java describes one widget twice in a flush; the two descriptions reach the client out of the
    // order they were written, so the older one arrives second and is refused - rightly, since
    // taking it would rewind the widget. The item described next to it is described nowhere else.
    const int treeId = 71;
    const int itemId = 72;
    VTree tree(List<VTreeItem> items, int seq) => VTree()
      ..id = treeId
      ..seq = seq
      ..style = SWT.NONE
      ..items = items;

    // The later of the two arrives first, and carries no item.
    applyWhole(tree([], 40), registry: registry);

    // The earlier one arrives second, with the item as nothing else will ever describe it.
    applyWhole(tree([_item(itemId, 'described once', 39)], 30), registry: registry);

    expect((registry.valueOn('TreeItem/$itemId') as VTreeItem?)?.texts?.single, 'described once',
        reason: 'the item had a record of its own, and nothing else has ever described it');
    expect((registry.valueOn('Tree/$treeId') as VTree).items, isEmpty,
        reason: 'the tree itself is not rewound: its record is still older than what it holds');
  });

  test('a widget named before it is described resolves once its parent is described again', () {
    // A parent built around one child: the parent's record names the child, whose own description
    // lands in another record a moment later. The parent lays the child out from the object in its
    // child list, so it has to be the one that description filled.
    const int parentId = 81;
    const int otherId = 82;
    const int childId = 83;
    VComposite composite(int id, List<VControl> children, int seq) => VComposite()
      ..id = id
      ..seq = seq
      ..style = SWT.NONE
      ..children = children;
    VComposite described() => VComposite()
      ..id = childId
      ..seq = 50
      ..style = SWT.NONE
      ..bounds = (VRectangle()
        ..width = 500
        ..height = 326);

    applyWhole(composite(parentId, [described()], 40), registry: registry, named: {childId});
    expect(_asked(), ['$childId', '$parentId']);
    applyWhole(composite(otherId, [], 45), registry: registry);
    applyWhole(composite(otherId, [described()], 50), registry: registry);

    // Java answers the parent's request.
    applyWhole(composite(parentId, [described()], 51), registry: registry);

    final laidOutByTheParent = (registry.valueOn('Composite/$parentId') as VComposite).children!.single;
    expect(laidOutByTheParent.bounds?.width, 500,
        reason: 'the parent lays its child out from this object, and an empty one gets no size');
    expect(identical(laidOutByTheParent, registry.valueOn('Composite/$childId')), isTrue,
        reason: 'one widget, one object - whoever named it and whoever described it agree');
  });

  test('a widget named but never described is asked for, and not held as its state', () {
    // Nothing describes it anywhere else, so the request is the only way it ever gets content, and
    // an empty value held for it would be drawn as the control.
    const int parentId = 91;
    const int childId = 92;
    final child = VComposite()
      ..id = childId
      ..style = SWT.NONE;

    applyWhole(
        VComposite()
          ..id = parentId
          ..seq = 60
          ..style = SWT.NONE
          ..children = [child],
        registry: registry,
        named: {childId});

    expect(_asked(), ['$childId', '$parentId'],
        reason: 'nothing else will ever describe this widget, so it has to be asked for');
    expect(registry.valueOn('Composite/$childId'), isNull);
    expect((registry.valueOn('Composite/$parentId') as VComposite).children, isEmpty,
        reason: 'a name nothing resolves is left out of the list rather than drawn empty');
  });
}
