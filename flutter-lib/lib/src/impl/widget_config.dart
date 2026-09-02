import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/theme/theme_extensions/color_scheme_extension.dart';

bool _useDarkTheme = false;
int? _parentBackgroundColor;

void setCurrentTheme(bool isDark) {
  _useDarkTheme = isDark;
}

/// The color scheme the running [MaterialApp] was built with, published here so the static helpers
/// that render without a [BuildContext] — [AppColors], the icon pipeline — resolve against the same
/// tokens as the widgets that do have one. Null until the first theme is built.
ColorSchemeExtension? _currentColorSchemeExtension;

void setCurrentColorSchemeExtension(ColorSchemeExtension? extension) {
  _currentColorSchemeExtension = extension;
}

ConfigFlags configFlags = ConfigFlags();
final ValueNotifier<double> appScaleNotifier = ValueNotifier<double>(1.0);

/// Bumped when the flags change; the root [MaterialApp] listens to it and rebuilds with the new theme.
final ValueNotifier<int> configFlagsVersion = ValueNotifier<int>(0);

Map<String, dynamic>? _lastAppliedConfig;

/// Applies config flags from either source: the `config` field of a `Display/{id}` update, or the
/// `swt.evolve.properties` message. The flags repeat in every Display update, so the dedup keeps an
/// unchanged payload from rebuilding the whole [MaterialApp].
void applyConfigFlags(ConfigFlags? flags) {
  if (flags == null) return;
  final json = flags.toJson();
  if (mapEquals(json, _lastAppliedConfig)) return;
  _lastAppliedConfig = json;
  setConfigFlags(flags);
  configFlagsVersion.value++;
}

/// The application menu (macOS only) carried by every `Display/{id}` update. It belongs to the
/// Display rather than to any Shell, so it reaches the menu bar the Decorations draw through here
/// instead of down the widget tree.
VMenu? _systemMenu;

void applySystemMenu(VMenu? menu) {
  _systemMenu = menu;
}

/// The application menu's top-level entries, to be shown ahead of the Shell's own menus.
List<VMenuItem> applicationMenuItems() => _systemMenu?.items ?? const [];

@visibleForTesting
void resetConfigFlags() {
  _systemMenu = null;
  configFlags = ConfigFlags();
  _lastAppliedConfig = null;
  configFlagsVersion.value = 0;
}

bool getCurrentTheme() {
  return _useDarkTheme;
}

void setParentBackgroundColor(int? color) {
  _parentBackgroundColor = color;
}

int? getCurrentParentBackgroundColor() {
  return _parentBackgroundColor;
}

ConfigFlags getConfigFlags() {
  return configFlags;
}

/// True when Evolve may substitute an application icon with one of its own — the bundled icon set
/// and the name-to-Material-icon map. An `assets_path` override applies regardless.
bool get useEvolveIcons => !(getConfigFlags().disable_evolve_icons ?? false);

/// True when an icon keeps the colors it was authored in. False (the default) tints it with the
/// theme color. Both fall back to the same value the Java side sends when no config arrived yet.
bool get preserveIconColors => getConfigFlags().preserve_icon_colors ?? false;

/// True when the bundled icon set may stand in for an image the application blits with
/// `GC#drawImage`. Off by default: the set is keyed by the bare filename stem, so an application
/// image sharing a name with one of ours would otherwise be replaced by it.
bool get gcIconsReplacement => getConfigFlags().gc_icons_replacement ?? false;

/// True when a Canvas is drawn in the theme's colors instead of the ones the application painted
/// with. `use_swt_colors` asks for the application's colors everywhere, so it wins.
bool get canvasUsesThemeColors {
  final flags = getConfigFlags();
  return (flags.disable_swt_canvas_colors ?? false) &&
      !(flags.use_swt_colors ?? false);
}

void setConfigFlags(ConfigFlags newFlags) {
  final prev = configFlags;
  String? mergeString(String? incoming, String? previous) {
    final inc = incoming?.trim();
    if (inc != null && inc.isNotEmpty) return inc;
    final prevTrim = previous?.trim();
    if (prevTrim != null && prevTrim.isNotEmpty) return prevTrim;
    return null;
  }

  newFlags.theme_name = mergeString(newFlags.theme_name, prev.theme_name);
  newFlags.force_theme = mergeString(newFlags.force_theme, prev.force_theme);
  // CSD flags arrive in the first broadcast and change rarely; preserve them across any
  // later partial re-broadcast so the controls don't flicker off/restyle mid-session.
  newFlags.csd_placement = mergeString(newFlags.csd_placement, prev.csd_placement);
  newFlags.csd_os = mergeString(newFlags.csd_os, prev.csd_os);
  configFlags = newFlags;
}

class AppSizes {
  static const double fontAwesomeIcon = 10.0;
  static const double icon = 12.0;
  static const double toolbarIconLarge = 16.0;
  static const double toolbarTextSize = 10.0;
  static const double toolbarMinSize = 20.0;
  static const double tabIconSize = 16.0;
  static const double tabTextSize = 12.0;
  static const double tabCloseIconSize = 14.0;
  static const double controlButtonSize = 16.0;
  static const double borderRadius = 4.0;
  static const double separatorWidth = 20.0;
  static const double separatorThickness = 1.0;
  static const double separatorIndent = 8.0;
}

/// The single disabled opacity. Every widget that renders a disabled state by fading it reads this,
/// so how faded "disabled" looks is one number rather than a literal per call site.
class AppOpacities {
  static const double disabled = 0.5;
}

class AppConstraints {
  static const BoxConstraints toolbarConstraints = BoxConstraints(
    minWidth: 20,
    minHeight: 20,
  );

  static const BoxConstraints toolbarSmallConstraints = BoxConstraints(
    minWidth: 10,
    minHeight: 10,
  );
}

class AppColors {
  static const Color lightDisabled = Color(0xFFBDBDBD);
  static const Color darkDisabled = Color(0xFF3D3D3D);

  static const Color lightHover = Color(0xFFE0E0E0);
  static const Color darkHover = Color(0xFF3D3D3D);

  static const Color darkBackground = Color(0xFF1E1E1E);
  static const Color lightBackground = Color(0xFFF2F2F2);

  static const Color darkSelected = Color(0xFF2D2D2D);
  static const Color lightSelected = Color(0xFFFFFFFF);

  static const Color lightBorder = Color(0xFFDDDDDD);
  static const Color darkBorder = Color(0xFF333333);
  static const Color highlight = Color(0xFF6366F1);

  static const Color darkTextColor = Color(0xFFBDBDBD);
  static const Color lightTextColor = Color(0xFF616161);

  static const Color darkSelectedTextColor = Color(0xFFFFFFFF);
  static const Color lightSelectedTextColor = Color(0xFF212121);

  static const Color darkEnabledColor = Color(0xFFF2F2F2);
  static const Color lightEnabledColor = Color(0xFF1E1E1E);

  static const Color toolbarBackground = Colors.transparent;

  static Color getHoverColor() => _useDarkTheme ? darkHover : lightHover;
  static Color getBackgroundColor() =>
      _useDarkTheme ? darkBackground : lightBackground;
  static Color getSelectedColor() =>
      _useDarkTheme ? darkSelected : lightSelected;
  static Color getBorderColor() => _useDarkTheme ? darkBorder : lightBorder;
  static Color getEnabledColor() =>
      _useDarkTheme ? darkEnabledColor : lightEnabledColor;
  /// The theme's disabled foreground, the same token every widget uses for disabled text. The
  /// constants below are the fallback for before the first theme is built.
  static Color getDisabledColor() =>
      _currentColorSchemeExtension?.onSurfaceVariantDisabled ??
      (_useDarkTheme ? darkDisabled : lightDisabled);
  static Color getTextColor() => _useDarkTheme ? darkTextColor : lightTextColor;
  static Color getColor(bool enabled) =>
      enabled ? getEnabledColor() : getDisabledColor();
  static Color getSelectedTextColor() =>
      _useDarkTheme ? darkSelectedTextColor : lightSelectedTextColor;
}
