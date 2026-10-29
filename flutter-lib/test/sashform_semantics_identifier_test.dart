// A SashForm with children carries its own semantics identifier, so a test can read its weights.

import 'package:flutter/material.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/sashform.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _label(int id, int x) => VLabel()
  ..swt = 'Label'
  ..id = id
  ..style = SWT.NONE
  ..text = 'pane $id'
  ..visible = true
  ..enabled = true
  ..bounds = _rect(x, 0, 195, 300);

VSashForm _sashForm() => VSashForm()
  ..swt = 'SashForm'
  ..id = 42
  ..style = SWT.HORIZONTAL
  ..enabled = true
  ..visible = true
  ..sashWidth = 10
  ..weights = [1, 1]
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    _label(1, 0),
    VSash()
      ..swt = 'Sash'
      ..id = 3
      ..style = SWT.VERTICAL
      ..visible = true
      ..enabled = true
      ..bounds = _rect(195, 0, 10, 300),
    _label(2, 205),
  ];

List<String> _identifiers(SemanticsNode node) {
  final ids = <String>[];
  void visit(SemanticsNode n) {
    final id = n.getSemanticsData().identifier;
    if (id.isNotEmpty) ids.add(id);
    n.visitChildren((child) {
      visit(child);
      return true;
    });
  }

  visit(node);
  return ids;
}

void main() {
  testWidgets('a SashForm with panes and a sash is tagged once, around its children',
      (tester) async {
    final handle = tester.ensureSemantics();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 400,
          height: 300,
          child: SashFormSwt(value: _sashForm()),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    final ids = _identifiers(tester.binding.pipelineOwner.semanticsOwner!.rootSemanticsNode!);
    expect(ids.where((id) => id == 'SashForm/42'), hasLength(1));
    expect(ids, containsAll(['Label/1', 'Sash/3', 'Label/2']));
    handle.dispose();
  });
}
