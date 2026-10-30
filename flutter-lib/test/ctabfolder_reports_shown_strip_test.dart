// The strip report ends with the part of the strip that is shown: Java decides from it which tabs
// are showing, and the workbench lists the others in bold in the chevron's list. A tab under the
// chevron is laid out inside the folder but not shown.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey, wireChannelNames;

import 'delivery/support/deliver.dart';

class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VCTabFolder _folder(int count) => VCTabFolder()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..selection = 0
  ..items = List.generate(
    count,
    (i) => VCTabItem()
      ..id = 100 + i
      ..text = 'Untitled ${i + 1}'
      ..showing = true,
  );

void main() {
  testWidgets('an overflowing strip reports that it is shown up to the chevron',
      (WidgetTester tester) async {
    final reports = <String>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 240,
        child: _CapturingCTabFolderSwt(
          value: _folder(12),
          onEvent: (ev, payload) {
            if (ev == 'CTabFolder/stripLaidOut' && payload?.text != null) {
              reports.add(payload!.text!);
            }
          },
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    final chevron = find.byIcon(Icons.expand_more);
    expect(chevron, findsOneWidget, reason: 'sanity: twelve tabs overflow a 600 px strip');

    final sections = reports.last.split(';');
    expect(sections, hasLength(3), reason: 'tabs; close buttons; shown part of the strip');
    final shown = sections[2].split(',').map(int.parse).toList();
    final folder = tester.getRect(find.byWidgetPredicate((w) => w is CTabFolderSwt<VCTabFolder>));
    final chevronLeft = tester.getRect(chevron).left - folder.left;

    expect(shown[0], 0);
    expect(shown[0] + shown[1], lessThanOrEqualTo(chevronLeft.round()),
        reason: 'what lies under the chevron is not shown');

  });

  testWidgets('the selected tab stays whole when its label grows',
      (WidgetTester tester) async {
    VRegistry.instance.clear();
    final reports = <String>[];
    final folder = _folder(12)..selection = 11;

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 240,
        child: _CapturingCTabFolderSwt(
          value: folder,
          onEvent: (ev, payload) {
            if (ev == 'CTabFolder/stripLaidOut' && payload?.text != null) {
              reports.add(payload!.text!);
            }
          },
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    // An editor marked dirty, or a label emphasised once its folder takes focus, widens the tab
    // after it was scrolled into view.
    applyChange(
        VCTabItem()
          ..id = 111
          ..swt = 'CTabItem'
          ..seq = 1
          ..text = '*Untitled 12',
        keys: [WireKey.k_Item_text],
        base: 0);
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    final sections = reports.last.split(';');
    final strip = sections[0].split(',').map(int.parse).toList();
    final shown = sections[2].split(',').map(int.parse).toList();
    expect(strip[23], greaterThan(int.parse(reports.first.split(';')[0].split(',')[23])),
        reason: 'sanity: the longer label widens the tab');
    expect(strip[22] + strip[23], lessThanOrEqualTo(shown[0] + shown[1]),
        reason: 'the selected tab is scrolled back into view whole');
  });
}
