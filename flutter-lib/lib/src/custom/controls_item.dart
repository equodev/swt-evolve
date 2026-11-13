import 'package:flutter/material.dart';

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
