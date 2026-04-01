import 'dart:convert';

import 'package:flutter/material.dart';

import '../comm/comm.dart';
import '../theme/theme_extensions/theme_color_palette_theme_extension.dart';

const String _kEvolvePropertySetChannel = 'swt.evolve.property.set';

void _syncThemeColorPropertyToJvm(String hex6or8) {
  final trimmed = hex6or8.trim();
  if (trimmed.isEmpty) {
    return;
  }
  EquoCommService.sendPayload(
    _kEvolvePropertySetChannel,
    jsonEncode(<String, String>{'theme_color': trimmed}),
  );
}

List<String> _parseSampleHexCsv(String csv) {
  return csv
      .split(',')
      .map((s) => s.trim())
      .where((s) => s.isNotEmpty)
      .toList();
}

class ThemeColorToolbarPaletteControl extends StatefulWidget {
  const ThemeColorToolbarPaletteControl({super.key});

  @override
  State<ThemeColorToolbarPaletteControl> createState() =>
      _ThemeColorToolbarPaletteControlState();
}

class _ThemeColorToolbarPaletteControlState
    extends State<ThemeColorToolbarPaletteControl> {
  bool _expanded = false;

  @override
  Widget build(BuildContext context) {
    final palette =
        Theme.of(context).extension<ThemeColorPaletteThemeExtension>();
    if (palette == null) {
      return const SizedBox.shrink();
    }
    final samples = _parseSampleHexCsv(palette.sampleHexCsv);
    return Material(
      type: MaterialType.transparency,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          if (_expanded) ...[
            ...samples.map((hex) {
              final c = Color(
                int.parse(hex.length == 6 ? 'FF$hex' : hex, radix: 16),
              );
              return Padding(
                padding: EdgeInsets.only(left: palette.swatchSpacing),
                child: InkWell(
                  onTap: () {
                    _syncThemeColorPropertyToJvm(hex);
                    setState(() => _expanded = false);
                  },
                  borderRadius:
                      BorderRadius.circular(palette.swatchBorderRadius),
                  child: Container(
                    width: palette.swatchSize,
                    height: palette.swatchSize,
                    decoration: BoxDecoration(
                      color: c,
                      borderRadius:
                          BorderRadius.circular(palette.swatchBorderRadius),
                      border: Border.all(color: palette.swatchBorderColor),
                    ),
                  ),
                ),
              );
            }),
            SizedBox(width: palette.trailingGapAfterSwatches),
          ],
          Tooltip(
            message:
                _expanded ? palette.collapseTooltip : palette.expandTooltip,
            child: InkWell(
              onTap: () => setState(() => _expanded = !_expanded),
              borderRadius:
                  BorderRadius.circular(palette.swatchBorderRadius),
              child: Padding(
                padding: palette.chevronPadding,
                child: Icon(
                  _expanded ? Icons.chevron_right : Icons.chevron_left,
                  size: palette.chevronIconSize,
                  color: palette.chevronIconColor,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
