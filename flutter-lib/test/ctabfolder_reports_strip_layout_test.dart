// Java answers "which tab is at this point?" from what this side reports, and the point it is
// asked about is in the folder's own coordinates - e4 maps the cursor with display.map(null, ctf,
// pos) before asking. So what is reported has to be in those same coordinates: each tab's x and
// width relative to the folder's left edge. An origin off by the strip's own inset would put every
// answer on a neighbouring tab, which is how a drag comes to move a view nobody grabbed.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VCTabFolder _folder(List<String> labels) => VCTabFolder()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..selection = 0
  ..items = List.generate(
    labels.length,
    (i) => VCTabItem()
      ..id = 100 + i
      ..text = labels[i]
      ..showing = true,
  );

void main() {
  testWidgets('the reported strip is each tab measured from the folder itself',
      (WidgetTester tester) async {
    final reports = <String>[];
    const labels = ['Section 3 (TVDSS)', 'Data Operations', 'Stratigraphic Modeling'];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 900,
        height: 240,
        child: _CapturingCTabFolderSwt(
          value: _folder(labels),
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

    expect(reports, isNotEmpty, reason: 'a laid-out strip says where its tabs are');

    final reported = reports.last.split(',').map(int.parse).toList();
    expect(reported, hasLength(labels.length * 2));

    final folder = tester.getRect(
        find.byWidgetPredicate((w) => w is CTabFolderSwt<VCTabFolder>));
    for (var i = 0; i < labels.length; i++) {
      final tab = tester.getRect(find
          .ancestor(of: find.text(labels[i]), matching: find.byType(AnimatedContainer))
          .first);
      expect(reported[2 * i], closeTo(tab.left - folder.left, 1),
          reason: '${labels[i]} starts here, measured from the folder\'s left edge');
      expect(reported[2 * i + 1], closeTo(tab.width, 1), reason: '${labels[i]} is this wide');
    }
  });
}
