import 'package:flutter/material.dart';
import '../gen/label.dart';
import '../gen/swt.dart';
import '../impl/control_evolve.dart';
import '../impl/color_utils.dart';
import '../impl/widget_config.dart';
import './utils/image_utils.dart';
import './utils/font_utils.dart';
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

  Widget _buildSeparator(BuildContext context, LabelThemeExtension widgetTheme) {
    
    final isVertical = hasStyle(state.style, SWT.VERTICAL);
    final separatorColor = widgetTheme.borderColor;
    final thickness = widgetTheme.borderWidth;
    
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);
    
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
          Container(
            constraints: constraints,
            child: isVertical
                ? Container(
                    width: thickness,
                    color: separatorColor,
                  )
                : Container(
                    height: thickness,
                    color: separatorColor,
                  ),
          ),
        ),
      ),
    );
  }

  Widget _buildLabel(BuildContext context, LabelThemeExtension widgetTheme, bool enabled) {
    final text = state.text ?? '';
    final textAlign = getTextAlignFromStyle(state.style, widgetTheme.textAlign);
    final backgroundColor = getBackgroundColor(
      background: state.background,
      defaultColor: widgetTheme.backgroundColor,
    );
    final hasValidBounds = hasBounds(state.bounds);
    final constraints = getConstraintsFromBounds(state.bounds);
    
    final child = _buildLabelContent(context, widgetTheme, enabled, text, textAlign, hasValidBounds);

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
        child: Opacity(
          opacity: enabled ? 1.0 : widgetTheme.disabledOpacity,
          child: wrap(
            Container(
              constraints: constraints,
              padding: widgetTheme.padding,
              margin: widgetTheme.margin,
              decoration: backgroundColor != null ? BoxDecoration(
                color: backgroundColor,
                borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
                border: widgetTheme.borderColor != null 
                    ? Border.all(
                        color: widgetTheme.borderColor!,
                        width: widgetTheme.borderWidth,
                      )
                    : null,
              ) : null,
              child: hasValidBounds
                  ? Align(
                      alignment: getAlignmentFromTextAlign(textAlign),
                      child: child,
                    )
                  : child,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildLabelContent(
    BuildContext context,
    LabelThemeExtension widgetTheme,
    bool enabled,
    String text,
    TextAlign textAlign,
    bool hasValidBounds,
  ) {
    final isVertical = hasStyle(state.style, SWT.VERTICAL);
    
    final textColor = enabled 
        ? getForegroundColor(
            foreground: state.foreground,
            defaultColor: widgetTheme.primaryTextColor,
          )
        : widgetTheme.disabledTextColor;
    
    final baseTextStyle = enabled
        ? widgetTheme.primaryTextStyle
        : widgetTheme.disabledTextStyle;
    
    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: baseTextStyle,
    );

    final shouldWrap = shouldWrapText(
      style: state.style,
      hasValidBounds: hasValidBounds,
      text: text,
    );
    
    Widget textWidget = Text(
      text,
      textAlign: textAlign,
      softWrap: shouldWrap,
      overflow: shouldWrap ? TextOverflow.visible : TextOverflow.ellipsis,
      maxLines: shouldWrap ? null : 1,
      style: textStyle,
    );

    if (isVertical) {
      textWidget = RotatedBox(
        quarterTurns: 3,
        child: textWidget,
      );
    }

    // Handle image
    Widget? imageWidget;
    if (state.image != null) {
      imageWidget = ImageUtils.buildVImage(
        state.image,
        width: widgetTheme.iconSize,
        height: widgetTheme.iconSize,
        enabled: enabled,
        constraints: null,
        useBinaryImage: true,
        renderAsIcon: false,
      );
    }

    // Build content row
    return Row(
      mainAxisSize: hasValidBounds ? MainAxisSize.max : MainAxisSize.min,
      mainAxisAlignment: getMainAxisAlignmentFromTextAlign(textAlign, widgetTheme.mainAxisAlignment),
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