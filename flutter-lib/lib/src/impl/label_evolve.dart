import 'package:flutter/material.dart';
import '../gen/label.dart';
import '../gen/swt.dart';
import '../impl/control_evolve.dart';
import '../impl/color_utils.dart';
import '../impl/widget_config.dart';
import './utils/image_utils.dart';
import './utils/font_utils.dart';
import './utils/text_utils.dart';
import './utils/widget_utils.dart';
import '../theme/theme_extensions/label_theme_extension.dart';

class LabelImpl<T extends LabelSwt, V extends VLabel>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<LabelThemeExtension>()!;

    final enabled = state.enabled ?? true;

    // Handle separator style
    if (hasStyle(state.style, SWT.SEPARATOR)) {
      return _buildSeparator(context, widgetTheme);
    }

    // Handle regular label
    return _buildLabel(context, widgetTheme, enabled);
  }

  Widget _buildSeparator(
    BuildContext context,
    LabelThemeExtension widgetTheme,
  ) {
    final isVertical = hasStyle(state.style, SWT.VERTICAL);
    final separatorColor = widgetTheme.borderColor;
    final thickness = widgetTheme.borderWidth;

    final hasValidBounds = hasBounds(state.bounds);
    final constraints = hasValidBounds
        ? getConstraintsFromBounds(state.bounds)
        : isVertical
        ? BoxConstraints(maxHeight: 7, maxWidth: thickness)
        : BoxConstraints(maxWidth: 7, maxHeight: thickness);

    Widget separator;
    if (isVertical) {
      separator = VerticalDivider(
        color: separatorColor,
        indent: 0,
        endIndent: 0,
      );
    } else {
      separator = Divider(color: separatorColor, indent: 0, endIndent: 0);
    }

    return MouseRegion(
      onEnter: (_) => widget.sendMouseTrackMouseEnter(state, null),
      onExit: (_) => widget.sendMouseTrackMouseExit(state, null),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            widget.sendFocusFocusIn(state, null);
          } else {
            widget.sendFocusFocusOut(state, null);
          }
        },
        child: wrap(
          constraints != null
              ? SizedBox(
                  width: constraints.maxWidth,
                  height: constraints.maxHeight,
                  child: separator,
                )
              : separator,
        ),
      ),
    );
  }

  Widget _buildLabel(
    BuildContext context,
    LabelThemeExtension widgetTheme,
    bool enabled,
  ) {
    final text = stripAccelerators(state.text);
    final textAlign = getTextAlignFromStyle(state.style, widgetTheme.textAlign);
    // state.background already resolves inheritance (DartControl.getExplicitBackground(), Java
    // side); ParentBackgroundScope would unconditionally shadow it with a stale ancestor value.
    final backgroundColor =
        getBackgroundColor(background: state.background, defaultColor: null);
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);

    final textStyle = _labelTextStyle(context, widgetTheme, enabled);

    EdgeInsets padding = widgetTheme.padding.resolve(TextDirection.ltr);
    var fitTextToBounds = false;
    final shouldWrap = shouldWrapText(
      style: state.style,
      hasValidBounds: hasValidBounds,
      text: text,
    );
    // Squeezing a too-long line into its bounds is a single-line measurement:
    // on multi-line text it would only ever see the first delimiter, and Java
    // has already sized the label for every line.
    if (hasValidBounds &&
        !shouldWrap &&
        !hasLineDelimiter(text) &&
        !hasStyle(state.style, SWT.VERTICAL) &&
        text.isNotEmpty &&
        state.image == null) {
      final painter = TextPainter(
        text: TextSpan(text: text, style: textStyle),
        maxLines: 1,
        textDirection: TextDirection.ltr,
      )..layout();
      final boundsWidth = state.bounds!.width.toDouble();
      if (painter.width > boundsWidth - padding.horizontal) {
        padding = EdgeInsets.only(top: padding.top, bottom: padding.bottom);
        if (painter.width > boundsWidth &&
            painter.width <= boundsWidth * 1.4) {
          fitTextToBounds = true;
        }
      }
    }

    final child = _buildLabelContent(
      context,
      widgetTheme,
      enabled,
      text,
      textAlign,
      hasValidBounds,
      textStyle,
      fitTextToBounds,
    );

    return Focus(
      onFocusChange: (hasFocus) {
        if (hasFocus) {
          widget.sendFocusFocusIn(state, null);
        } else {
          widget.sendFocusFocusOut(state, null);
        }
      },
      child: Opacity(
        opacity: enabled ? 1.0 : widgetTheme.disabledOpacity,
        child: wrap(
          Container(
            constraints: constraints,
            padding: padding,
            margin: widgetTheme.margin,
            decoration: backgroundColor != null
                ? BoxDecoration(
                    color: backgroundColor,
                    borderRadius: BorderRadius.circular(
                      widgetTheme.borderRadius,
                    ),
                    border: widgetTheme.borderColor != null
                        ? Border.all(
                            color: widgetTheme.borderColor!,
                            width: widgetTheme.borderWidth,
                          )
                        : null,
                  )
                : null,
            child: hasValidBounds
                ? Align(
                    alignment: getAlignmentFromTextAlign(textAlign),
                    child: child,
                  )
                : child,
          ),
        ),
      ),
    );
  }

  TextStyle _labelTextStyle(
    BuildContext context,
    LabelThemeExtension widgetTheme,
    bool enabled,
  ) {
    final textColor = enabled
        ? getForegroundColor(
            foreground: state.foreground,
            defaultColor: widgetTheme.primaryTextColor,
          )
        : widgetTheme.disabledTextColor;

    final baseTextStyle = enabled
        ? widgetTheme.primaryTextStyle
        : widgetTheme.disabledTextStyle;

    return getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: baseTextStyle,
    );
  }

  Widget _buildLabelContent(
    BuildContext context,
    LabelThemeExtension widgetTheme,
    bool enabled,
    String text,
    TextAlign textAlign,
    bool hasValidBounds,
    TextStyle textStyle,
    bool fitTextToBounds,
  ) {
    final isVertical = hasStyle(state.style, SWT.VERTICAL);

    final shouldWrap = shouldWrapText(
      style: state.style,
      hasValidBounds: hasValidBounds,
      text: text,
    );

    // Without SWT.WRAP the label never wraps on its own, but it still breaks on
    // the delimiters the text already carries -- clamping to a single line
    // would drop every line after the first.
    final isMultiLine = hasLineDelimiter(text);

    Widget textWidget = Text(
      text,
      textAlign: textAlign,
      softWrap: shouldWrap,
      overflow: shouldWrap ? TextOverflow.visible : TextOverflow.ellipsis,
      maxLines: shouldWrap || isMultiLine ? null : 1,
      style: textStyle,
    );

    if (fitTextToBounds) {
      textWidget = FittedBox(
        fit: BoxFit.scaleDown,
        alignment: getAlignmentFromTextAlign(textAlign),
        child: textWidget,
      );
    }

    if (isVertical) {
      textWidget = RotatedBox(quarterTurns: 3, child: textWidget);
    }

    // Handle image
    Widget? imageWidget;
    if (state.image != null) {
      final imgW = state.image?.imageData?.width?.toDouble() ?? widgetTheme.iconSize;
      final imgH = state.image?.imageData?.height?.toDouble() ?? widgetTheme.iconSize;
      imageWidget = SizedBox(
        width: imgW,
        height: imgH,
        child: ImageUtils.buildVImage(
          state.image,
          width: imgW,
          height: imgH,
          enabled: enabled,
          constraints: null,
          useBinaryImage: true,
          renderAsIcon: true,
        ),
      );
    }

    // Build content row
    return Row(
      mainAxisSize: hasValidBounds ? MainAxisSize.max : MainAxisSize.min,
      mainAxisAlignment: getMainAxisAlignmentFromTextAlign(
        textAlign,
        widgetTheme.mainAxisAlignment,
      ),
      crossAxisAlignment: widgetTheme.crossAxisAlignment,
      children: [
        if (imageWidget != null) ...[
          imageWidget,
          if (text.isNotEmpty) SizedBox(width: widgetTheme.iconTextSpacing),
        ],
        if (text.isNotEmpty)
          Flexible(
            child: hasValidBounds
                ? ConstrainedBox(
                    constraints: BoxConstraints(
                      maxWidth: state.bounds!.width.toDouble(),
                      maxHeight: state.bounds!.height.toDouble(),
                    ),
                    child: textWidget,
                  )
                : textWidget,
          ),
      ],
    );
  }
}
