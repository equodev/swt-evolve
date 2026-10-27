import 'package:flutter/material.dart';

/// A named set of type sizes.
///
/// Named rather than anonymous because the sizes exist on both sides of the bridge: Flutter draws
/// with them and the Java size model mirrors them for `computeSize`. The mirror is generated once
/// per scale, so several themes wanting the same sizes cost one measurement, not one each — which
/// is the whole reason this is a scale and not a per-theme field.
class TypeScale {
  /// Matches the Java `Themes.Theme` member and the measurement's `ThemeConfig` entry. The
  /// generator renders both from this string, so it is the name the two sides agree on.
  final String name;

  /// The sizes this scale wants, given the shared scale to adjust.
  final TextTheme Function(TextTheme base) apply;

  const TypeScale(this.name, this.apply);
}

/// The scale every theme draws in unless it says otherwise. Its sizes are the shared ones, so it
/// applies nothing; it exists to give that case a name the Java side can be keyed by.
final TypeScale kStandardTypeScale = TypeScale('NonDefault', (base) => base);

/// The shared scale's roles at the sizes native SWT draws them.
///
/// Spelled out per role rather than derived from a single factor, so a role that turns out to need
/// its own value can get one without moving the rest. The body roles carry the constraint: a JFace
/// PreferenceDialog gives its page-list tree a fixed 180px, of which the row's own padding and
/// reserved expander take ~54px, leaving ~126px for the label — which the longest page names of a
/// real product only fit at 10px.
final TypeScale kCompactTypeScale = TypeScale('Compact', (base) => base.copyWith(
  displayLarge: base.displayLarge?.copyWith(fontSize: 34.0),
  headlineLarge: base.headlineLarge?.copyWith(fontSize: 23.0),
  titleLarge: base.titleLarge?.copyWith(fontSize: 17.0),
  titleMedium: base.titleMedium?.copyWith(fontSize: 12.0),
  titleSmall: base.titleSmall?.copyWith(fontSize: 10.0),
  labelMedium: base.labelMedium?.copyWith(fontSize: 10.0),
  labelSmall: base.labelSmall?.copyWith(fontSize: 9.0),
  bodyLarge: base.bodyLarge?.copyWith(fontSize: 10.0),
  bodyMedium: base.bodyMedium?.copyWith(fontSize: 10.0),
  bodySmall: base.bodySmall?.copyWith(fontSize: 9.0),
));

/// Every scale that exists, by name. The measurement walks this to sample one theme per scale, and
/// the generator renders the Java enum from the same names.
final Map<String, TypeScale> kTypeScales = {
  kStandardTypeScale.name: kStandardTypeScale,
  kCompactTypeScale.name: kCompactTypeScale,
};
