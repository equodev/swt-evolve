// A right click on a tab is one MenuDetect on the folder, as natively: the folder reports the press
// like any control does, so the tab must not report one of its own as well.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VCTabFolder _folder() => VCTabFolder()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..selection = 0
  ..items = List.generate(
    3,
    (i) => VCTabItem()
      ..id = 100 + i
      ..text = 'Tab $i'
      ..showing = true,
  );

void main() {
  testWidgets('a right click on a tab is one MenuDetect', (WidgetTester tester) async {
    final menuDetects = <VEvent?>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 200,
        child: _CapturingCTabFolderSwt(
          value: _folder(),
          onEvent: (ev, payload) {
            if (ev.endsWith('MenuDetect')) menuDetects.add(payload);
          },
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    await tester.tap(find.text('Tab 1'), buttons: kSecondaryButton);
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    expect(menuDetects, hasLength(1));
  });
}
