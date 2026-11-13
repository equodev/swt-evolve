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

/// Every scale that exists, by name. The measurement walks this to sample one theme per scale, and
/// the generator renders the Java enum from the same names. An app built on this one adds the
/// scales its themes draw in.
final Map<String, TypeScale> kTypeScales = {
  kStandardTypeScale.name: kStandardTypeScale,
};
