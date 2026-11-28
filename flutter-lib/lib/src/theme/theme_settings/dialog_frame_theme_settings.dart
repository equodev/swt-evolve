import 'package:flutter/material.dart';

import '../theme_extensions/color_scheme_extension.dart';
import '../theme_extensions/display_theme_extension.dart';

/// The corner radius of a presented dialog's card.
const double dialogCardRadius = 12;

/// The shadow of a presented dialog's card, lifted further off the window than a window's.
List<BoxShadow> dialogCardShadow(DisplayThemeExtension theme) => [
      BoxShadow(color: theme.dialogShadowColor.withValues(alpha: 0.28), blurRadius: 40, offset: const Offset(0, 16)),
      BoxShadow(color: theme.dialogShadowColor.withValues(alpha: 0.14), blurRadius: 6, offset: const Offset(0, 2)),
    ];

/// The colour of secondary text in the frame (crumbs, page tabs, steps to come).
Color dialogMutedColor(BuildContext context, TextStyle base, double alpha) =>
    (base.color ?? Theme.of(context).colorScheme.onSurface).withValues(alpha: alpha);

/// The colour a page heading's message of [kind] is drawn in (DialogPageParts' MESSAGE_* numbers:
/// 1 information, 2 warning, 3 error; anything else plain).
Color dialogMessageColor(BuildContext context, int kind, TextStyle base) {
  final scheme = Theme.of(context).colorScheme;
  return switch (kind) {
    3 => scheme.error,
    2 => Theme.of(context).extension<ColorSchemeExtension>()?.warning ?? scheme.tertiary,
    1 => scheme.primary,
    _ => base.color ?? scheme.onSurface,
  };
}

/// The check mark on a wizard step that is done, drawn over the accent.
Color dialogStepDoneColor(BuildContext context) => Theme.of(context).colorScheme.onPrimary;

/// The thumb of a page that scrolls inside the editor area.
BoxDecoration dialogScrollThumbDecoration(BuildContext context) => BoxDecoration(
      color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.35),
      borderRadius: BorderRadius.circular(dialogScrollThumbWidth / 2),
    );

const double dialogScrollThumbWidth = 5;
const double dialogScrollThumbInset = 3;

/// The tile an alert's icon sits on, tinted with the icon's own colour.
BoxDecoration dialogIconTileDecoration(Color tint) => BoxDecoration(
      color: tint.withValues(alpha: 0.14),
      borderRadius: BorderRadius.circular(10),
    );

const double dialogIconTileSize = 36;
const double dialogIconSize = 20;
