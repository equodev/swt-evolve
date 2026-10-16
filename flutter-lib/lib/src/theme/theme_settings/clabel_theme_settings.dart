import 'package:flutter/material.dart';
import '../theme_extensions/clabel_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/clabel.dart';
import '../../gen/color.dart';
import '../../impl/color_utils.dart';
import '../../impl/utils/image_utils.dart';
import '../../impl/utils/widget_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

CLabelThemeExtension getCLabelLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCLabelTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CLabelThemeExtension getCLabelDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCLabelTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CLabelThemeExtension _getCLabelTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();

  return CLabelThemeExtension(
    primaryTextColor: colorScheme.onSurface,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,

    backgroundColor: null,

    // Font styles
    primaryTextStyle: baseTextStyle.copyWith(
      color: colorScheme.onSurface,
      fontSize: 12,
    ),
    disabledTextStyle: baseTextStyle.copyWith(
      color: colorScheme.onSurface.withOpacity(0.38),
      fontSize: 12,
    ),

    // Icon properties
    iconSize: 16.0,
    iconTextSpacing: 4.0,

    // Margin
    defaultMargin: 3.0,

    // Interactive properties
    disabledOpacity: AppOpacities.disabled,

    // Alignment and positioning
    textAlign: TextAlign.left,
    mainAxisAlignment: MainAxisAlignment.start,
    crossAxisAlignment: CrossAxisAlignment.center,
  );
}

/// What `CLabel.onPaint` paints behind the content: the image when there is one, the gradient
/// otherwise, and the background only when it has neither; null when that leaves nothing to paint.
///
/// The background is the label's own colour, or [parentBackground] or the theme's when it has none.
/// A label without an image of its own stays transparent [overParentImage], so the image an
/// ancestor already painted shows through. The application chose these colours and this image for
/// the label, so `use_swt_colors` does not gate them the way it does the plain background.
BoxDecoration? getCLabelDecoration(
  CLabelThemeExtension theme,
  VCLabel state, {
  Color? parentBackground,
  bool overParentImage = false,
}) {
  final background = getBackgroundColor(
    background: state.background,
    defaultColor: parentBackground ?? theme.backgroundColor,
  );
  final image = ImageUtils.buildTiledBackgroundImage(state.backgroundImage);
  var color = state.backgroundImage == null && overParentImage ? null : background;
  Gradient? gradient;
  final colors = state.gradientColors;
  if (state.backgroundImage == null && colors != null && colors.isNotEmpty) {
    if (colors.length == 1) {
      color = colorFromVColor(colors.first);
    } else {
      gradient = _clabelGradient(
        colors: colors,
        percents: state.gradientPercents!,
        vertical: state.gradientVertical ?? false,
        background: background ?? Colors.transparent,
      );
    }
  }
  if (color == null && gradient == null && image == null) return null;
  return BoxDecoration(color: color, gradient: gradient, image: image);
}

/// What `CLabel.onPaint` draws for `setBackground(Color[], int[], boolean)` with two or more
/// colours: colour `i` runs into colour `i + 1` over the span that ends at `percents[i]` of the
/// width (of the height when [vertical]) and starts where the previous one ended, and whatever
/// lies past the last percent is the widget's [background], not the last colour.
///
/// Java has checked the percents: one fewer than the colours, ascending, from 0 to 100. A colour
/// Java was given as null is already the background by the time it is sent.
Gradient _clabelGradient({
  required List<VColor> colors,
  required List<int> percents,
  required bool vertical,
  required Color background,
}) {
  final stops = <double>[0.0, for (var i = 0; i < colors.length - 1; i++) percents[i] / 100.0];
  final painted = <Color>[for (final color in colors) colorFromVColor(color)];
  if (stops.last < 1.0) {
    // A hard edge: two stops at the same place.
    painted.add(background);
    stops.add(stops.last);
  }
  return LinearGradient(
    begin: vertical ? Alignment.topCenter : Alignment.centerLeft,
    end: vertical ? Alignment.bottomCenter : Alignment.centerRight,
    colors: painted,
    stops: stops,
  );
}
