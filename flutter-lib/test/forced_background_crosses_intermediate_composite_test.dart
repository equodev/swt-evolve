import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart';

/// Distinct from anything a theme would resolve to, so a match can only come from the value this
/// test published.
const Color _forced = Color(0xFF2F2F2F);

/// Reads whatever [ParentBackgroundScope] is visible at its own position.
class _ScopeProbe extends StatelessWidget {
  const _ScopeProbe({required this.onBuild});

  final void Function(Color?) onBuild;

  @override
  Widget build(BuildContext context) {
    onBuild(ParentBackgroundScope.backgroundOf(context));
    return const SizedBox.shrink();
  }
}

/// One level of the chain: a composite that re-publishes background inheritance for its children.
Widget _level({
  required int? backgroundMode,
  required bool inheritsBackground,
  required Color effectiveBackground,
  required Widget child,
}) =>
    Builder(
      builder: (context) => wrapBackgroundInheritanceScope(
        context: context,
        backgroundMode: backgroundMode,
        inheritsBackground: inheritsBackground,
        effectiveBackground: effectiveBackground,
        backgroundImage: null as VImage?,
        child: child,
      ),
    );

void main() {
  testWidgets(
    'a forcing ancestor reaches a grandchild through a default-mode composite',
    (tester) async {
      Color? seen;
      await tester.pumpWidget(
        _level(
          backgroundMode: SWT.INHERIT_FORCE,
          inheritsBackground: false,
          effectiveBackground: _forced,
          // The middle composite never opted in itself; SWT's PARENT_BACKGROUND bit is what makes
          // it keep passing the colour down, and Java sends that resolved answer.
          child: _level(
            backgroundMode: SWT.INHERIT_NONE,
            inheritsBackground: true,
            effectiveBackground: _forced,
            child: _ScopeProbe(onBuild: (c) => seen = c),
          ),
        ),
      );

      expect(
        seen,
        _forced,
        reason: 'a grandchild under a forcing ancestor must see the forced background; '
            'cutting the chain at the middle level makes it paint its own colour instead',
      );
    },
  );

  testWidgets(
    'a composite that neither forces nor inherits still stops the colour',
    (tester) async {
      Color? seen;
      await tester.pumpWidget(
        _level(
          backgroundMode: SWT.INHERIT_FORCE,
          inheritsBackground: false,
          effectiveBackground: _forced,
          child: _level(
            backgroundMode: SWT.INHERIT_NONE,
            inheritsBackground: false,
            effectiveBackground: _forced,
            child: _ScopeProbe(onBuild: (c) => seen = c),
          ),
        ),
      );

      expect(
        seen,
        isNull,
        reason: 'INHERIT_NONE with nothing forcing above must not leak a colour to grandchildren',
      );
    },
  );
}
