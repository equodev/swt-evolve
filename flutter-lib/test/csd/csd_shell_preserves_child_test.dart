import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/custom/csd/csd_scaffold.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// Regression guard for the desk/web "only the first tab shows, others blank until resized" bug.
///
/// The CSD config flags arrive asynchronously *after* the first frame: `csd_placement` is null
/// initially (CsdShell falls back to 'toolbar' and wraps the app in `Stack > Positioned.fill`),
/// then becomes e.g. 'false' in desk mode. If CsdShell restructures so the child changes depth
/// in the tree, Flutter destroys and recreates every descendant `State` — which drops each
/// widget's Java-update subscription (`EquoCommService.on/remove` is one handler per channel, and
/// the disposed old State's `remove()` clobbers the new State's handler). The whole tree then goes
/// deaf and never applies later updates (tab selection, tab content, ...).
///
/// CsdShell must therefore keep its child at a stable tree depth across any `csd_placement` change.
class _Probe extends StatefulWidget {
  final List<String> log;
  const _Probe(this.log);

  @override
  State<_Probe> createState() => _ProbeState();
}

class _ProbeState extends State<_Probe> {
  @override
  void initState() {
    super.initState();
    widget.log.add('init');
  }

  @override
  void dispose() {
    widget.log.add('dispose');
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => const SizedBox.shrink();
}

void main() {
  tearDown(() => configFlags = ConfigFlags());

  testWidgets('CsdShell keeps its child State when csd_placement flips (null -> false)',
      (tester) async {
    final log = <String>[];
    late StateSetter rebuild;

    // First frame: no CSD flags yet -> CsdShell defaults to 'toolbar' (Stack-wrapped).
    configFlags = ConfigFlags();

    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: StatefulBuilder(builder: (context, setState) {
          rebuild = setState;
          return CsdShell(child: _Probe(log));
        }),
      ),
    ));
    expect(log, ['init'], reason: 'child is created once on the first frame');

    // Properties arrive: csd_placement becomes 'false' (desk native window, CSD off).
    configFlags = ConfigFlags()..csd_placement = 'false';
    rebuild(() {});
    await tester.pump();

    // The child's State must survive the flip: no dispose + re-init.
    expect(
      log,
      ['init'],
      reason: 'CsdShell must not reparent/recreate its child when csd_placement flips; '
          'recreating it drops the widget-update subscription (the tabs-blank regression)',
    );
  });
}
