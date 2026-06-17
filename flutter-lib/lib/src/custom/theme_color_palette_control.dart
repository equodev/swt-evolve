import 'package:flutter/material.dart';

import '../comm/comm.dart';
import '../impl/widget_config.dart';
import '../theme/named_themes.dart';
import '../theme/theme_extensions/theme_color_palette_theme_extension.dart';

const String _kEvolvePropertySetChannel = 'swt.evolve.property.set';

void _syncThemeColor(String hex6or8) {
  final trimmed = hex6or8.trim();
  if (trimmed.isEmpty) return;
  EquoCommService.sendPayload(
    _kEvolvePropertySetChannel,
    <String, String>{'theme_color': trimmed},
  );
}

void _syncThemeName(String themeName, {String? forceTheme}) {
  final payload = <String, String>{'theme_name': themeName};
  if (forceTheme != null) payload['force_theme'] = forceTheme;
  EquoCommService.sendPayload(
    _kEvolvePropertySetChannel,
    payload,
  );
}

List<String> _parseSampleHexCsv(String csv) =>
    csv.split(',').map((s) => s.trim()).where((s) => s.isNotEmpty).toList();

typedef _ThemePick = ({String key, String label, String? forceTheme});

const _kNamedPicks = <_ThemePick>[
  (key: 'equo', label: 'Equo Light', forceTheme: 'light'),
  (key: 'equo', label: 'Equo Dark', forceTheme: 'dark'),
  (key: 'cursor', label: 'Cursor Dark', forceTheme: 'dark'),
];

class ThemeColorToolbarPaletteControl extends StatelessWidget {
  const ThemeColorToolbarPaletteControl({super.key});

  @override
  Widget build(BuildContext context) {
    final palette = Theme.of(context).extension<ThemeColorPaletteThemeExtension>();
    if (palette == null) return const SizedBox.shrink();

    final themeName = getConfigFlags().theme_name?.trim();
    final hasNamedTheme = themeName != null && themeName.isNotEmpty;

    return Material(
      type: MaterialType.transparency,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          if (hasNamedTheme)
            ..._buildNamedThemeSwatches(palette, Theme.of(context).brightness)
          else
            ..._buildColorSwatches(palette),
          SizedBox(width: palette.trailingGapAfterSwatches),
        ],
      ),
    );
  }

  List<Widget> _buildColorSwatches(ThemeColorPaletteThemeExtension palette) {
    return _parseSampleHexCsv(palette.sampleHexCsv).map((hex) {
      final c = Color(int.parse(hex.length == 6 ? 'FF$hex' : hex, radix: 16));
      return _swatch(
        palette: palette,
        color: c,
        label: hex,
        onTap: () => _syncThemeColor(hex),
      );
    }).toList();
  }

  List<Widget> _buildNamedThemeSwatches(
    ThemeColorPaletteThemeExtension palette,
    Brightness brightness,
  ) {
    return _kNamedPicks.expand((pick) {
      final namedTheme = kNamedThemes[pick.key];
      if (namedTheme == null) return const <Widget>[];
      final swatchBrightness = pick.forceTheme == 'light'
          ? Brightness.light
          : pick.forceTheme == 'dark'
              ? Brightness.dark
              : brightness;
      final scheme = swatchBrightness == Brightness.dark
          ? (namedTheme.darkColorScheme ?? namedTheme.lightColorScheme)
          : namedTheme.lightColorScheme;
      return [
        _swatch(
          palette: palette,
          color: scheme.primary,
          label: pick.label,
          onTap: () => _syncThemeName(pick.key, forceTheme: pick.forceTheme),
        ),
      ];
    }).toList();
  }

  Widget _swatch({
    required ThemeColorPaletteThemeExtension palette,
    required Color color,
    required String label,
    required VoidCallback onTap,
  }) =>
      Padding(
        padding: EdgeInsets.only(left: palette.swatchSpacing),
        child: Tooltip(
          message: label,
          child: InkWell(
            onTap: onTap,
            borderRadius: BorderRadius.circular(palette.swatchBorderRadius),
            child: Container(
              width: palette.swatchSize,
              height: palette.swatchSize,
              decoration: BoxDecoration(
                color: color,
                borderRadius: BorderRadius.circular(palette.swatchBorderRadius),
                border: Border.all(color: palette.swatchBorderColor),
              ),
            ),
          ),
        ),
      );
}
