// A widget that changed parents, when the two parents' records arrive in either order.
//
// They are siblings, so nothing orders them. A departure is read out of what a record stops
// carrying, so the old parent's record says the widget went away and the new parent's says it
// arrived - and whichever is applied first has to leave the widget intact. Two things make that
// possible: a description carries a write stamp, which tells a move from a disposal, and a widget
// let go of is only forgotten once every frame received with it is applied, so a new parent that
// merely names it still finds it.

import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';

import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireChannel, WireKey;

import '../support/batch.dart';
import '../support/channels.dart';
import 'support/deliver.dart';

/// The channel frames of widget records arrive on.
const String _widgetWire = 'swt.evolve.wire';

const int _moverId = 42;
const int _underMoverId = 43;
const String _mover = 'Composite/$_moverId';
const String _underMover = 'Label/$_underMoverId';
const String _oldParent = 'Composite/8';
const String _newParent = 'Composite/7';

/// The new parent, whole, carrying the moved widget described - stamp and all, and what is under
/// it with it. A subtree is what actually moves: the part being dragged is a composite with the
/// editor inside it.
VComposite _newParentTakingIt(int seq, int moverSeq) => VComposite()
  ..id = 7
  ..seq = seq
  ..style = SWT.NONE
  ..children = [
    VComposite()
      ..id = _moverId
      ..seq = moverSeq
      ..style = SWT.NONE
      ..children = [
        VLabel()
          ..id = _underMoverId
          ..seq = moverSeq + 1
          ..style = SWT.NONE
          ..text = 'the part',
      ],
  ];

/// The old parent, as a change naming the one list that changed.
VComposite _oldParentLettingGo(int seq) => VComposite()
  ..id = 8
  ..seq = seq
  ..style = SWT.NONE
  ..children = [];

void main() {
  late VRegistry registry;

  setUp(() {
    deliveryGate.reset();
    registry = VRegistry();
  });
  tearDown(() => registry.clear());

  /// Both parents delivered, the mover - and the subtree under it - held by the old one.
  void settle() {
    final underMover = VLabel()
      ..id = _underMoverId
      ..seq = 1
      ..style = SWT.NONE
      ..text = 'the part';
    final mover = VComposite()
      ..id = _moverId
      ..seq = 1
      ..style = SWT.NONE
      ..children = [underMover];
    registry.register(underMover);
    registry.register(mover);
    registry.register(VComposite()
      ..id = 8
      ..seq = 2
      ..style = SWT.NONE
      ..children = [mover]);
    registry.register(VComposite()
      ..id = 7
      ..seq = 3
      ..style = SWT.NONE
      ..children = []);
    deliveryGate.applied(_oldParent, 2);
    deliveryGate.applied(_newParent, 3);
    deliveryGate.applied(_mover, 1);
    deliveryGate.applied(_underMover, 1);
  }

  void expectTheMoveLanded(String order) {
    expect(registry.isAwaiting(_mover), isFalse,
        reason: '$order: the widget moved; nothing here should be waiting to be told what it is');
    final children = (registry.valueOn(_newParent) as VComposite).children!;
    expect(children.single.id, _moverId,
        reason: "$order: the new parent's list holds the widget");

    // Letting go of a widget lets go of everything under it, so a widget dropped as a disposal
    // takes its subtree with it - which is why one mistaken drop empties a whole stack rather than
    // blanking one thing.
    expect(registry.valueOn(_underMover), isNotNull,
        reason: '$order: the subtree moved with the widget rather than being torn down');
    expect((registry.valueOn(_underMover) as VLabel).text, 'the part',
        reason: '$order: and it kept its state');
  }

  void newParent() => applyWhole(_newParentTakingIt(18, 20), registry: registry);

  void oldParent() => applyChange(_oldParentLettingGo(19),
      keys: [WireKey.k_Composite_children], base: 2, registry: registry);

  test('the new parent applied first', () {
    settle();

    newParent();
    oldParent();

    expectTheMoveLanded('new parent first');
  });

  test('the old parent applied first', () {
    settle();

    oldParent();
    newParent();

    expectTheMoveLanded('old parent first');
  });

  /// The moved widget written first, so its stamp is older than the old parent's.
  WireRecord mover(int seq) => WireRecord.whole(VComposite()
    ..id = _moverId
    ..seq = seq
    ..style = SWT.NONE
    ..children = [
      VLabel()
        ..id = _underMoverId
        ..seq = 1
        ..style = SWT.NONE
        ..text = 'the part',
    ]);

  WireRecord oldParentLettingGo(int seq) =>
      WireRecord.change(_oldParentLettingGo(seq), keys: [WireKey.k_Composite_children], base: 2);

  /// The new parent only naming the widget, as Java writes one the client holds.
  WireRecord newParentNamingIt(int seq) => WireRecord.change(
      VComposite()
        ..id = 7
        ..seq = seq
        ..style = SWT.NONE
        ..children = [
          VComposite()
            ..id = _moverId
            ..style = SWT.NONE,
        ],
      keys: [WireKey.k_Composite_children],
      base: 3);

  List<String> refreshesAsked() {
    final sent = <String>[];
    EquoCommBase.sendObserverForTesting = (actionId, _) => sent.add(actionId);
    addTearDown(() => EquoCommBase.sendObserverForTesting = null);
    return sent;
  }

  test('one run: the widget itself, then the old parent letting go, then the new parent naming it',
      () async {
    // How a move arrives when the client already holds everything that moves: one run of records.
    // The moved widget is written first, so its stamp is older than the old parent's, and the new
    // parent only names it. Applied in that order, the widget is held by nothing between the
    // second record and the third.
    settle();
    final sent = refreshesAsked();

    applyRecords([mover(21), oldParentLettingGo(22), newParentNamingIt(23)], registry: registry);
    await Future<void>.delayed(Duration.zero);

    expectTheMoveLanded('one run');
    expect(sent, isNot(contains(EquoCommBase.widgetRefreshChannel)),
        reason: 'nothing was missing, so nothing is asked for');
  });

  test('one run whose handler awaits between the old parent letting go and the new parent naming it',
      () async {
    // The run is applied in several microtask turns when an entry's handler awaits, so the
    // widget stays unheld across them: only the end of the whole run may forget it.
    registry = VRegistry.instance;
    settle();
    final sent = refreshesAsked();
    final release = Completer<void>();
    // A channel no test listens on, standing for any handler that awaits.
    const awaiting = 'swt.evolve.timing.bare';
    final comm = EquoCommService.commForTesting;
    comm.onNamed(awaiting, (_) => release.future);
    addTearDown(() => comm.removeNamed(awaiting));
    // What the app's entry point registers: the widget wire applies to the client's registry.
    EquoCommService.onBytesAt(WireChannel.swtEvolveWire, VRegistry.instance.applyWire);

    receiveRawBatch([
      (_widgetWire, wireFrameBody([mover(21), oldParentLettingGo(22)])),
      (awaiting, TaggedValues.encode(<String, dynamic>{})),
      (_widgetWire, wireFrameBody([newParentNamingIt(23)])),
    ]);
    await Future<void>.delayed(Duration.zero);
    release.complete();
    await Future<void>.delayed(Duration.zero);

    expectTheMoveLanded('one run, awaiting halfway');
    expect(sent, isNot(contains(EquoCommBase.widgetRefreshChannel)),
        reason: 'nothing was missing, so nothing is asked for');
  });
}
