import 'package:flutter/material.dart';

import '../impl/widget_config.dart';
import '../theme/theme_extensions/theme_color_palette_theme_extension.dart';
import 'scaling_control.dart';
import 'theme_color_palette_control.dart';

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
    final showPalette = widget.useBoundsLayout &&
        (configFlags.show_theme_color_palette == true);
    final showScalingControl = widget.useBoundsLayout &&
        (configFlags.show_scaling_control == true);

    if (!showPalette && !showScalingControl) {
      return const SizedBox.shrink();
    }

    final paletteTheme =
        Theme.of(context).extension<ThemeColorPaletteThemeExtension>();
    final chevronPadding = paletteTheme?.chevronPadding ?? const EdgeInsets.all(4);
    final chevronIconSize = paletteTheme?.chevronIconSize ?? 16.0;
    final chevronIconColor = paletteTheme?.chevronIconColor ?? Theme.of(context).iconTheme.color;
    final chevronRadius = paletteTheme?.swatchBorderRadius ?? 4.0;
    final expandTooltip = paletteTheme?.expandTooltip ?? 'Expand controls';
    final collapseTooltip = paletteTheme?.collapseTooltip ?? 'Collapse controls';

    return Material(
      type: MaterialType.transparency,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (_expanded) ...[
            if (showPalette) const ThemeColorToolbarPaletteControl(),
            if (showScalingControl) const ScalingScaleControl(),
          ],
          Tooltip(
            message: _expanded ? collapseTooltip : expandTooltip,
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
