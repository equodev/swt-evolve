// What the strip reports has to be where the tabs are AFTER the scroll, not before it.
//
// Measured on a running workbench: with the strip scrolled, Java found no tab under any drawn one
// and the workbench read that as a drag of the whole stack, carrying every view in it. At scroll
// offset zero the same gesture was right, which is the signature of a report taken in unscrolled
// coordinates.

import 'package:flutter/gestures.dart';
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

const _labels = [
  'Section 3 (TVDSS)',
  'Data Operations',
  'Stratigraphic Modeling',
  'Facies Trend Modeling',
  'Interpretation Set',
];

void main() {
  testWidgets('a scrolled strip is reported where it is drawn', (WidgetTester tester) async {
    final reports = <String>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        // Narrow enough that the five tabs do not fit, so the strip scrolls.
        width: 260,
        height: 240,
        child: _CapturingCTabFolderSwt(
          value: _folder(_labels),
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

    final atRest = reports.last;

    // Scrolled with a wheel over the strip, which is how it is scrolled in the application - and
    // the path that tells its listeners mid-frame, before the tabs have been painted where they
    // now are.
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    final overTheStrip = tester.getCenter(find.text(_labels.first));
    tester.binding.handlePointerEvent(pointer.hover(overTheStrip));
    tester.binding
        .handlePointerEvent(pointer.scroll(const Offset(120, 0)));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    expect(reports.last, isNot(atRest), reason: 'the tabs moved, so their report has to move too');

    final reported = reports.last.split(',').map(int.parse).toList();
    final folder = tester.getRect(
        find.byWidgetPredicate((w) => w is CTabFolderSwt<VCTabFolder>));
    for (var i = 0; i < _labels.length; i++) {
      final tab = find
          .ancestor(of: find.text(_labels[i]), matching: find.byType(AnimatedContainer))
          .first;
      if (!tab.evaluate().isNotEmpty) continue;
      expect(reported[2 * i], closeTo(tester.getRect(tab).left - folder.left, 1),
          reason: '${_labels[i]} is drawn here once the strip has scrolled');
    }
  });
}
