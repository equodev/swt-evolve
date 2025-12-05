import 'package:flutter/material.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/label.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/image.dart';
import '../impl/control_evolve.dart';
import '../impl/color_utils.dart';
import '../styles.dart';
import './utils/image_utils.dart';
import './utils/font_utils.dart';

class LabelImpl<T extends LabelSwt, V extends VLabel>
    extends ControlImpl<T, V> {
  /// Helper method to build an image widget from VImage using ImageUtils
  Widget? _buildImageWidget(VImage? image, bool enabled) {
    return ImageUtils.buildVImage(
      image,
      width: null,
      height: null,
      enabled: enabled,
      constraints: null,
      useBinaryImage: true,
      renderAsIcon: false,
    );
  }

  @override
  Widget build(BuildContext context) {
    final text = state.text ?? '';
    final enabled = state.enabled ?? true;
    final backgroundColor = _getBackgroundColor(context);
    final image = state.image ?? '';

    Widget child;

    if (state.style.has(SWT.SEPARATOR)) {
      child = SeparatorLabel(
        direction:
            state.style.has(SWT.VERTICAL) ? Axis.vertical : Axis.horizontal,
        backgroundColor: backgroundColor,
      );
    } else {
      child = TextLabel(
        text: text,
        alignment: _getAlignment(),
        wrap: state.style.has(SWT.WRAP),
        vertical: state.style.has(SWT.VERTICAL),
        image: state.image,
        backgroundColor: backgroundColor,
        enabled: enabled,
        buildImageWidget: _buildImageWidget,
        vFont: state.font,
        textColor: state.foreground,
      );
    }

    return MouseRegion(
      onEnter: (_) => _handleMouseEnter(),
      onExit: (_) => _handleMouseExit(),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            _handleFocusIn();
          } else {
            _handleFocusOut();
          }
        },
        child: Opacity(
          opacity: enabled ? 1.0 : 0.5,
          child: wrap(Container(
            padding: EdgeInsets.zero,
            margin: EdgeInsets.zero,
            alignment: Alignment.centerLeft,
            // Don't set background color on the container for separators
            color: state.style.has(SWT.SEPARATOR) ? null : backgroundColor,
            child: child,
          )),
        ),
      ),
    );
  }

  TextAlign _getAlignment() {
    if (state.style.has(SWT.CENTER)) return TextAlign.center;
    if (state.style.has(SWT.RIGHT)) return TextAlign.right;
    return TextAlign.left;
  }

  Color? _getBackgroundColor(BuildContext context) {
    // If there's a background color configured from Java, use it
    return colorFromVColor(state.background, defaultColor: Colors.transparent);
  }

  void _handleMouseEnter() {
    widget.sendMouseTrackMouseEnter(state, null);
  }

  void _handleMouseExit() {
    widget.sendMouseTrackMouseExit(state, null);
  }

  void _handleFocusIn() {
    widget.sendFocusFocusIn(state, null);
  }

  void _handleFocusOut() {
    widget.sendFocusFocusOut(state, null);
  }
}

class SeparatorLabel extends StatelessWidget {
  final Axis direction;
  final Color? backgroundColor;

  const SeparatorLabel({
    super.key,
    required this.direction,
    this.backgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    final separatorColor = Color(0xFFA0A0A0);

    if (direction == Axis.horizontal) {
      // Horizontal separator
      return Center(
        child: Container(
          height: 1,
          color: separatorColor,
        ),
      );
    } else {
      // Vertical separator
      return Center(
        child: Container(
          width: 1,
          color: separatorColor,
        ),
      );
    }
  }
}

class TextLabel extends StatelessWidget {
  final String text;
  final TextAlign alignment;
  final bool wrap;
  final bool vertical;
  final VImage? image;
  final Color? backgroundColor;
  final bool enabled;
  final Widget? Function(VImage?, bool)? buildImageWidget;
  final VFont? vFont;
  final VColor? textColor;

  const TextLabel({
    super.key,
    required this.text,
    required this.alignment,
    required this.wrap,
    required this.vertical,
    this.image,
    this.backgroundColor,
    this.enabled = true,
    this.buildImageWidget,
    this.vFont,
    this.textColor,
  });

  @override
  Widget build(BuildContext context) {
    // Get text color from VColor or use theme default
    final color = colorFromVColor(textColor,
        defaultColor: Theme.of(context).textTheme.bodyMedium?.color);

    // Create TextStyle from VFont using FontUtils or use defaults
    final textStyle = FontUtils.textStyleFromVFont(
      vFont,
      context,
      color: color,
    );

    // Print font data for debugging (optional)
    if (vFont != null) {
      FontUtils.printFontData(vFont, context: 'Label');
    }

    Widget textWidget = Text(
      text,
      textAlign: alignment,
      softWrap: wrap,
      overflow: wrap ? TextOverflow.visible : TextOverflow.ellipsis,
      style: textStyle,
    );

    if (vertical) {
      textWidget = RotatedBox(
        quarterTurns: 3,
        child: textWidget,
      );
    }

    if (image != null && image is VImage) {
      final imageWidget = buildImageWidget?.call(image, enabled);
      if (imageWidget != null) {
        return Row(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            imageWidget,
            SizedBox(width: 8),
            textWidget,
          ],
        );
      }
    }

    return textWidget;
  }
}
