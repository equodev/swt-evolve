// A control that takes Java's focus request focuses itself a frame later. When the element that
// took it is replaced in between -- a cell editor rebuilt in its parent's overlay -- the control's
// new element must still get the focus.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

const int editorId = 51;

VText _editor() => VText()
  ..id = editorId
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = ''
  ..visible = true
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

  testWidgets('a focus request taken by an element replaced before it applied focuses the new one',
      (tester) async {
    final generation = ValueNotifier(0);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: ValueListenableBuilder<int>(
          valueListenable: generation,
          builder: (_, n, __) => Stack(children: [
            KeyedSubtree(key: ValueKey(n), child: TextSwt<VText>(value: _editor())),
          ]),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    // Taken at once by the mounted element, which is replaced in the very next frame.
    FocusRequests.instance.request(editorId);
    generation.value++;
    await tester.pump();
    await tester.pump();

    final field = tester.widget<EditableText>(find.byType(EditableText));
    expect(field.focusNode.hasPrimaryFocus, isTrue, reason: 'the replacing element holds the focus');
  });

  testWidgets('a control holding the focus keeps it when its element is replaced', (tester) async {
    final generation = ValueNotifier(0);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: ValueListenableBuilder<int>(
          valueListenable: generation,
          builder: (_, n, __) => Stack(children: [
            KeyedSubtree(key: ValueKey(n), child: TextSwt<VText>(value: _editor())),
          ]),
        ),
      ),
    ));
    await tester.pumpAndSettle();
    FocusRequests.instance.request(editorId);
    await tester.pump();
    await tester.pump();
    expect(tester.widget<EditableText>(find.byType(EditableText)).focusNode.hasPrimaryFocus, isTrue);

    // Rebuilt elsewhere, as a part restored from a maximized stack is: Java hears nothing of it.
    generation.value++;
    await tester.pump();
    await tester.pump();

    final field = tester.widget<EditableText>(find.byType(EditableText));
    expect(field.focusNode.hasPrimaryFocus, isTrue, reason: 'the element showing the control now holds the keys');
  });
}
