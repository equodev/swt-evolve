import 'package:flutter/material.dart';

import '../../theme/theme_extensions/button_theme_extension.dart';
import 'quiet_animations.dart';

/// A push button of an Evolve dialog, drawn from the Button theme; disabled while [onPressed] is
/// null.
class DialogButton extends StatefulWidget {
  final String label;
  final bool isPrimary;
  final VoidCallback? onPressed;

  const DialogButton({
    super.key,
    required this.label,
    required this.isPrimary,
    required this.onPressed,
  });

  @override
  State<DialogButton> createState() => _DialogButtonState();
}

class _DialogButtonState extends State<DialogButton> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<ButtonThemeExtension>()!;
    final enabled = widget.onPressed != null;
    final primary = widget.isPrimary;
    final Color bgColor;
    final Color textColor;
    final Color borderColor;
    if (!enabled) {
      bgColor = theme.pushButtonDisabledColor;
      textColor = theme.disabledForegroundColor;
      borderColor = theme.pushButtonDisabledColor;
    } else {
      bgColor = _isHovering
          ? (primary ? theme.pushButtonHoverColor : theme.secondaryButtonHoverColor)
          : (primary ? theme.pushButtonColor : theme.secondaryButtonColor);
      textColor = primary ? theme.pushButtonTextColor : theme.secondaryButtonTextColor;
      borderColor = primary ? theme.pushButtonBorderColor : theme.secondaryButtonBorderColor;
    }
    final radius = BorderRadius.circular(theme.pushButtonBorderRadius);

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovering = true),
      onExit: (_) => setState(() => _isHovering = false),
      child: Material(
        color: bgColor,
        borderRadius: radius,
        child: InkWell(
          onTap: widget.onPressed,
          borderRadius: radius,
          splashColor: theme.splashColor,
          highlightColor: theme.highlightColor,
          child: QuietAnimatedContainer(
            duration: theme.buttonPressDelay,
            decoration: BoxDecoration(
              borderRadius: radius,
              border: Border.all(color: borderColor, width: theme.pushButtonBorderWidth),
            ),
            padding: theme.pushButtonPadding,
            child: Text(
              widget.label,
              style: (theme.pushButtonFontStyle ?? const TextStyle()).copyWith(color: textColor),
            ),
          ),
        ),
      ),
    );
  }
}
