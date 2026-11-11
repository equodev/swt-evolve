// A focus request applies a frame after Java sends it. When Java hides the control in between, the
// request is stale: the control must not take the focus then, nor later when it is shown again.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

import 'delivery/support/deliver.dart';

const int fieldId = 61;

class _RecordingTextSwt extends TextSwt<VText> {
  final List<String> sent;
  const _RecordingTextSwt({required super.value, required this.sent});

  @override
  void sendFocusFocusIn(VText val, VEvent? payload) => sent.add('FocusIn');
}

VText _field(int seq, {required bool visible}) => VText()
  ..id = fieldId
  ..seq = seq
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = ''
  ..visible = visible
  ..enabled = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 100
    ..height = 22);

void main() {
  setUp(() {
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
  });
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('a control hidden before its focus request applies does not take the focus when shown',
      (tester) async {
    final sent = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: _RecordingTextSwt(value: _field(1, visible: true), sent: sent),
      ),
    ));
    await tester.pumpAndSettle();

    FocusRequests.instance.request(fieldId);
    applyWhole(_field(2, visible: false));
    await tester.pump();
    await tester.pump();
    expect(sent, isEmpty, reason: 'a hidden control takes no focus');

    applyWhole(_field(3, visible: true));
    await tester.pump();
    await tester.pump();

    expect(sent, isEmpty, reason: 'Java hid the control after asking to focus it');
    final focused = FocusManager.instance.primaryFocus?.context;
    expect(focused?.findAncestorWidgetOfExactType<_RecordingTextSwt>(), isNull,
        reason: 'shown again with no new request, the control must not take the focus');
  });
}
