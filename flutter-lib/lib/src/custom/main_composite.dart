import 'package:flutter/material.dart';

import '../gen/composite.dart';
import '../impl/composite_evolve.dart';
import '../nolayout.dart';
import '../theme/theme_extensions/composite_theme_extension.dart';
import '../theme/theme_settings/composite_theme_settings.dart';

class MainComposite extends CompositeSwt<VComposite> {
  const MainComposite({super.key, required super.value});

  @override
  State<StatefulWidget> createState() => MainCompositeImpl();
}

class MainCompositeImpl extends CompositeImpl<MainComposite, VComposite> {
  /// A main composite panels its children unless it is itself one of another's panels.
  @override
  bool get laysOutChildrenAsPanels => !SashPanelMarker.of(context);

  @override
  Widget buildComposite() => buildMainCompositeLayout(this);
}

Widget buildMainCompositeLayout(CompositeImpl impl) {
  final context = impl.context;
  final state = impl.state;
  final widgetTheme = Theme.of(context).extension<CompositeThemeExtension>()!;
  final enabled = state.enabled ?? true;
  final children = state.children;

  final backgroundColor = getCompositeBackgroundColor(
    context,
    state,
    widgetTheme,
    isEnabled: enabled,
    isMain: true,
  );

  if (children == null || children.isEmpty) {
    final content = impl.wrap(
      ColoredBox(color: backgroundColor, child: const SizedBox.expand()),
    );
    return wrapCompositeInteractionChrome(impl, content);
  }

  final rawLayout = NoLayout(children: children, composite: state);
  if (state.visible != null && !state.visible!) {
    return Visibility(visible: false, child: rawLayout);
  }

  final inner = ColoredBox(
    color: backgroundColor,
    child: SashPanelMarker(
      active: impl.laysOutChildrenAsPanels,
      child: rawLayout,
    ),
  );

  return wrapCompositeInteractionChrome(impl, impl.wrapWithGCOverlay(inner));
}
