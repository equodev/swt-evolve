// What listens for a widget's own channels is released with it. A Shell that hosts Trackers, a drop
// target and a control's drag-start or menu-detect gate used to stay registered for good: one comm
// handler each, and whatever it held, after the widget was gone.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/dnd_session.dart';
import 'package:swtflutter/src/impl/utils/menu_detect_gate.dart';
import 'package:swtflutter/src/impl/utils/tracker_session.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireType;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('a Shell that goes away is no longer a Tracker host', () {
    final before = TrackerSession.hosts;
    TrackerSession.attachHost('Shell', 7101, toDisplay: (p) => p);
    TrackerSession.attachHost('Shell', 7102);
    expect(TrackerSession.hosts, before + 2);

    TrackerSession.detachHost(7101);
    TrackerSession.detachHost(7102);

    expect(TrackerSession.hosts, before);
  });

  test('a drop target nothing shows is no longer listened for', () {
    final before = DndSession.listenedDropTargets;
    void first(DndNegotiationState _) {}
    void second(DndNegotiationState _) {}
    DndSession.listen(7201, first);
    DndSession.listen(7201, second);
    expect(DndSession.listenedDropTargets, before + 1);

    DndSession.unlisten(7201, first);
    expect(DndSession.listenedDropTargets, before + 1, reason: 'one copy still shows it');
    DndSession.unlisten(7201, second);

    expect(DndSession.listenedDropTargets, before);
  });

  test('a control that goes away drops its drag-start gate', () {
    final before = DragStartVeto.gates;
    DragStartVeto.begin(WireType.tree, 7301);
    expect(DragStartVeto.gates, before + 1);

    DragStartVeto.forget(7301);

    expect(DragStartVeto.gates, before);
  });

  test('a table that goes away drops its menu-detect gate', () {
    final before = MenuDetectGate.gates;
    MenuDetectGate.withhold(WireType.table, 7401, () {});
    expect(MenuDetectGate.gates, before + 1);

    MenuDetectGate.forget(7401);

    expect(MenuDetectGate.gates, before);
  });
}
