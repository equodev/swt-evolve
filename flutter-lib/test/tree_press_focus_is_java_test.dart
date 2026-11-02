// A press on a tree's rows leaves the focus report to Java: Java focuses the tree and announces the
// activation as the MouseDown's (ControlHelper.pressed), as Win32, GTK and Cocoa do. A FocusIn sent
// by the client ahead of it activates the tree as a programmatic focus change instead, and one sent
// after it takes the focus back from a cell editor the MouseDown opened.

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

class _CapturingTreeSwt extends TreeSwt<VTree> {
  const _CapturingTreeSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VTree val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!);
}

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Row'
      ..itemCount = 0,
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

void main() {
  final desktop = TargetPlatformVariant(<TargetPlatform>{
    TargetPlatform.macOS,
    TargetPlatform.linux,
    TargetPlatform.windows,
  });

  for (final button in [kPrimaryButton, kSecondaryButton]) {
    testWidgets('a press on a row sends its MouseDown and no FocusIn of its own (button $button)',
        (tester) async {
      final events = <String>[];
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 400,
          height: 300,
          child: _CapturingTreeSwt(value: _tree(), onEvent: events.add),
        ),
      ));
      await tester.pump();

      await tester.tap(find.text('Row'), buttons: button);
      await tester.pump();

      expect(events, contains('Mouse/MouseDown'));
      expect(events, isNot(contains('Focus/FocusIn')), reason: '$events');
    }, variant: desktop);
  }
}
