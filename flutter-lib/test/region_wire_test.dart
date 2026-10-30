// A shell's region arrives on the binary wire as an object holding its rectangles. The Eclipse
// workbench shapes its drag-and-drop feedback with one: a shell the size of the window cut down to
// a few thin bars. A region the client cannot read takes the rest of the shell's description with
// it, so the feedback shell stays whole and covers the application.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

void main() {
  test("a shell's region is read with its rectangles, and the fields after it are read too", () {
    const bars = [358, 45, 772, 2, 358, 313, 772, 2, 1399, 812, 1, 1];
    final bytes = WidgetWireWriter.encodeWith((w) {
      w.key(WireKey.k_Control_region);
      w.object((o) {
        o.key(WireKey.k_Region_rects);
        o.ints(bars);
      });
      w.key(WireKey.k_Shell_alpha);
      w.int32(150);
      w.u8(0);
    });

    final shell = WidgetWireReader(bytes).fields(VShell());

    expect(shell.region?.rects, bars);
    expect(shell.alpha, 150);
  });
}
