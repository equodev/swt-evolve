import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';

import '../impl/config_flags.dart';
import '../impl/widget_config.dart';
import '../theme/theme_extensions/scaling_scale_theme_extension.dart';
import 'scaling_control.dart';

/// A control an app built on this one adds to the main toolbar's optional controls row.
class ToolbarControl {
  /// Whether the control shows under these flags. Java reserves the row's room from the same flags
  /// (DartMainToolbar), so the two must agree.
  final bool Function(ConfigFlags flags) shown;
  final WidgetBuilder builder;

  const ToolbarControl({required this.shown, required this.builder});
}

/// The controls an app built on this one adds ahead of the scaling control, in order.
final List<ToolbarControl> toolbarControls = [];

/// Whether a main toolbar is mounted in this client. While none is, [StandaloneToolbarControls]
/// shows the optional controls row in the window's own top strip instead.
final ValueNotifier<bool> mainToolbarMounted = ValueNotifier(false);

final Set<Object> _mainToolbars = {};

/// Records a main toolbar coming or going. Applied after the frame: the notifier rebuilds widgets,
/// which a mount or an unmount must not do in the middle of a build.
void trackMainToolbar(Object toolbar, {required bool mounted}) {
  SchedulerBinding.instance.addPostFrameCallback((_) {
    mounted ? _mainToolbars.add(toolbar) : _mainToolbars.remove(toolbar);
    mainToolbarMounted.value = _mainToolbars.isNotEmpty;
  });
  SchedulerBinding.instance.ensureVisualUpdate();
}

/// Forgets every main toolbar, for a test that pumps a fresh app.
@visibleForTesting
void resetMainToolbars() {
  _mainToolbars.clear();
  mainToolbarMounted.value = false;
}

/// The optional controls row for an app without a main toolbar, at the trailing end of the strip
/// that holds it (the CSD title strip, or the Shell's own menu strip). Empty while a main toolbar
/// is mounted, which hosts the row itself.
class StandaloneToolbarControls extends StatelessWidget {
  const StandaloneToolbarControls({super.key});

  @override
  Widget build(BuildContext context) => ValueListenableBuilder<bool>(
        valueListenable: mainToolbarMounted,
        builder: (context, hasMainToolbar, _) => hasMainToolbar
            ? const SizedBox.shrink()
            : const ToolbarOptionalControlsRow(useBoundsLayout: true),
      );
}

class ToolbarOptionalControlsRow extends StatefulWidget {
  final bool useBoundsLayout;

  const ToolbarOptionalControlsRow({super.key, required this.useBoundsLayout});

  @override
  State<ToolbarOptionalControlsRow> createState() =>
      _ToolbarOptionalControlsRowState();
}

class _ToolbarOptionalControlsRowState extends State<ToolbarOptionalControlsRow> {
  bool _expanded = false;

  @override
  Widget build(BuildContext context) {
    final configFlags = getConfigFlags();
    final added = widget.useBoundsLayout
        ? toolbarControls.where((c) => c.shown(configFlags)).toList()
        : const <ToolbarControl>[];
    final showScalingControl = widget.useBoundsLayout &&
        (configFlags.show_scaling_control == true);

    if (added.isEmpty && !showScalingControl) {
      return const SizedBox.shrink();
    }

    final chevronTheme = Theme.of(context).extension<ScalingScaleThemeExtension>();
    final chevronPadding = chevronTheme?.chevronPadding ?? const EdgeInsets.all(4);
    final chevronIconSize = chevronTheme?.chevronIconSize ?? 16.0;
    final chevronIconColor = chevronTheme?.chevronIconColor ?? Theme.of(context).iconTheme.color;
    const chevronRadius = 4.0;

    return Material(
      type: MaterialType.transparency,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (_expanded) ...[
            for (final control in added) control.builder(context),
            if (showScalingControl) const ScalingScaleControl(),
          ],
          Tooltip(
            message: _expanded ? 'Collapse controls' : 'Expand controls',
            child: InkWell(
              onTap: () => setState(() => _expanded = !_expanded),
              borderRadius: BorderRadius.circular(chevronRadius),
              child: Padding(
                padding: chevronPadding,
                child: Icon(
                  _expanded ? Icons.chevron_right : Icons.chevron_left,
                  size: chevronIconSize,
                  color: chevronIconColor,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
