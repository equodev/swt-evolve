import 'dart:convert';

import 'package:flutter/material.dart';

import '../comm/comm.dart';
import '../theme/named_themes.dart';
import '../theme/theme_extensions/theme_color_palette_theme_extension.dart';

const String _kEvolvePropertySetChannel = 'swt.evolve.property.set';

void _syncThemeColor(String hex6or8) {
  final trimmed = hex6or8.trim();
  if (trimmed.isEmpty) return;
  EquoCommService.sendPayload(
    _kEvolvePropertySetChannel,
    jsonEncode(<String, String>{'theme_color': trimmed}),
  );
}

void _syncThemeName(String themeName, {String? forceTheme}) {
  final payload = <String, String>{'theme_name': themeName};
  if (forceTheme != null) payload['force_theme'] = forceTheme;
  EquoCommService.sendPayload(
    _kEvolvePropertySetChannel,
    jsonEncode(payload),
  );
}

typedef _ThemePick = ({String key, String label, String? forceTheme});

final _kNamedPicks = <_ThemePick>[
  (key: 'cursor', label: 'Cursor', forceTheme: 'dark'),
  (key: 'equo', label: 'Equo Light', forceTheme: 'light'),
  (key: 'equo', label: 'Equo Dark', forceTheme: 'dark'),
];

List<String> _parseSampleHexCsv(String csv) =>
    csv.split(',').map((s) => s.trim()).where((s) => s.isNotEmpty).toList();

enum _PickerMode { colorPalette, nameSelector }

class ThemeColorToolbarPaletteControl extends StatelessWidget {
  const ThemeColorToolbarPaletteControl({super.key});

  @override
  Widget build(BuildContext context) =>
      const _ThemeToolbarPicker(mode: _PickerMode.colorPalette);
}

class ThemeNameSelectorControl extends StatelessWidget {
  const ThemeNameSelectorControl({super.key});

  @override
  Widget build(BuildContext context) =>
      const _ThemeToolbarPicker(mode: _PickerMode.nameSelector);
}

class _ThemeToolbarPicker extends StatefulWidget {
  final _PickerMode mode;

  const _ThemeToolbarPicker({required this.mode});

  @override
  State<_ThemeToolbarPicker> createState() => _ThemeToolbarPickerState();
}

class _ThemeToolbarPickerState extends State<_ThemeToolbarPicker> {
  bool _expanded = false;

  @override
  Widget build(BuildContext context) {
    final palette = Theme.of(context).extension<ThemeColorPaletteThemeExtension>();
    if (palette == null) return const SizedBox.shrink();

    final brightness = Theme.of(context).brightness;

    return Material(
      type: MaterialType.transparency,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          if (_expanded) ...[
            ..._buildSwatches(palette, brightness),
            SizedBox(width: palette.trailingGapAfterSwatches),
          ],
          Tooltip(
            message: _expanded ? palette.collapseTooltip : palette.expandTooltip,
            child: InkWell(
              onTap: () => setState(() => _expanded = !_expanded),
              borderRadius: BorderRadius.circular(palette.swatchBorderRadius),
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

  List<Widget> _buildSwatches(
    ThemeColorPaletteThemeExtension palette,
    Brightness brightness,
  ) {
    if (widget.mode == _PickerMode.colorPalette) {
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
            onTap: () {
              onTap();
              setState(() => _expanded = false);
            },
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
