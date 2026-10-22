// A drop on a CTabFolder's tab strip reports where it landed relative to the folder, as every drop
// target does, not relative to the strip: Java maps the point to the display through the folder.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/drag_gesture.dart';

Offset? _lastDrop() {
  final drops = EquoCommService.commForTesting.sentFrames
      .map(EquoCommBase.decodeFrame)
      .where((f) => f.$1.endsWith('/Drop/drop'))
      .map((f) => jsonDecode(f.$2) as Map<String, dynamic>)
      .toList();
  if (drops.isEmpty) return null;
  return Offset((drops.last['x'] as num).toDouble(), (drops.last['y'] as num).toDouble());
}

void main() {
  testWidgets('a drop on a bottom tab strip reports the position local to the folder', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);

    final source = VLabel()
      ..id = 1
      ..style = SWT.NONE
      ..enabled = true
      ..text = 'source'
      ..dragSource = true
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 100
        ..height = 30);
    final folder = VCTabFolder()
      ..id = 2
      ..style = SWT.BOTTOM
      ..tabPosition = SWT.BOTTOM
      ..enabled = true
      ..selection = 0
      ..dropTargetId = 9
      ..items = [
        VCTabItem()
          ..id = 200
          ..text = 'Tab one'
          ..showing = true,
      ];
    const folderOrigin = Offset(50, 60);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 500,
        height: 400,
        child: Stack(children: [
          Positioned(left: 0, top: 0, width: 100, height: 30, child: LabelSwt<VLabel>(value: source)),
          Positioned(
            left: folderOrigin.dx,
            top: folderOrigin.dy,
            width: 350,
            height: 240,
            child: CTabFolderSwt<VCTabFolder>(value: folder),
          ),
        ]),
      ),
    ));
    await tester.pumpAndSettle();
    EquoCommService.commForTesting.clearSentFrames();

    final end = tester.getCenter(find.text('Tab one'));
    await dragInSteps(tester, tester.getCenter(find.text('source')), end);
    await tester.pumpAndSettle();
    // Nothing answers the dragStart verdict here; let the fail-open timer run out.
    await tester.pump(const Duration(milliseconds: 400));

    final dropped = _lastDrop();
    expect(dropped, isNotNull, reason: 'the drop must reach Java');
    expect((dropped! - (end - folderOrigin)).distance, lessThan(1));
  });
}
