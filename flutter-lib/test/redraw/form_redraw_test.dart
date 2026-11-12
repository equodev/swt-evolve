// What a new value in one field of a form repaints. A form's fields sit in composites, as a preference
// page's or a dialog's do; a field's new text repaints its composite, not the window around it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import '../delivery/support/deliver.dart';
import 'support/budget.dart';
import 'support/redraw_probe.dart';
import 'support/workbench.dart' show JavaSide, rect;

const double _width = 900;
const double _height = 600;
const int _groups = 3;
const int _rows = 8;

int _textId(int group, int row) => 1000 + group * 100 + row * 2;

T _control<T extends VControl>(T v, int id, int x, int y, int w, int h) => v
  ..id = id
  ..seq = 1
  ..enabled = true
  ..visible = true
  ..bounds = rect(x, y, w, h);

VComposite _group(int g) => _control(
      VComposite()
        ..style = SWT.NONE
        ..children = [
          for (var r = 0; r < _rows; r++) ...[
            _control(VLabel()..style = SWT.NONE..text = 'Setting $g.$r', _textId(g, r) - 1, 0, r * 30, 120, 24),
            _control(
                VText()
                  ..style = SWT.SINGLE | SWT.BORDER
                  ..editable = true
                  ..text = 'value $g.$r',
                _textId(g, r),
                130,
                r * 30,
                150,
                24),
          ],
        ],
      100 + g,
      g * 300,
      40,
      290,
      _rows * 30,
    );

VComposite _form() => _control(
      VComposite()
        ..style = SWT.NONE
        ..children = [
          _control(VLabel()..style = SWT.NONE..text = 'Preferences', 2, 0, 0, 300, 30),
          for (var g = 0; g < _groups; g++) _group(g),
        ],
      1,
      0,
      0,
      _width.toInt(),
      _height.toInt(),
    );

const budgets = <String, Budget>{
  // The first new text gives the field a layer of its own: its composite repaints once, the window
  // around it does not.
  'form.text (first new text)': Budget(1, 314, 0.14),
  'form.text (next new text)': Budget(1, 2, 0.01),
};

void main() {
  testWidgets('a field of a form', (tester) async {
    tester.view.physicalSize = const Size(_width, _height);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.reset);
    freshClient();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        // A Shell's own layer, as the Display gives every shell it shows.
        child: RepaintBoundary(
          child: SizedBox(width: _width, height: _height, child: CompositeSwt<VComposite>(value: _form())),
        ),
      ),
    ));
    await tester.pumpAndSettle();
    final probe = RedrawProbe(tester, const Size(_width, _height));
    final java = JavaSide();
    final costs = <RedrawCost>[];
    final field = _textId(1, 3);
    for (final (name, text) in [('form.text (first new text)', 'changed'), ('form.text (next new text)', 'changed again')]) {
      costs.add(await probe.measure(name, () async {
        java.flush([(VText()..id = field..text = text, [WireKey.k_Text_text])]);
      }));
    }
    probe.uninstall();
    // ignore: avoid_print
    print('\n[redraw] summary\n${costs.join('\n')}');
    expectWithinBudgets(costs, budgets);
  });
}
