import 'package:flutter/material.dart';
import 'dart:typed_data';
import '../gen/button.dart';
import '../gen/swt.dart';
import '../impl/control_evolve.dart';
import '../theme/theme_extensions/button_theme_extension.dart';
import '../theme/theme_settings/button_theme_settings.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'widget_config.dart';

class ButtonImpl<T extends ButtonSwt, V extends VButton>
    extends ControlImpl<T, V> {

  static IconData _getArrowIcon(int alignment) {
    switch (alignment) {
      case SWT.UP:
        return Icons.keyboard_arrow_up;
      case SWT.DOWN:
        return Icons.keyboard_arrow_down;
      case SWT.LEFT:
        return Icons.keyboard_arrow_left;
      case SWT.RIGHT:
        return Icons.keyboard_arrow_right;
      default:
        return Icons.keyboard_arrow_down;
    }
  }

  Widget _wrapWithSwtBackground(BuildContext context, Widget button) {
    final useSwtColors = getConfigFlags().use_swt_colors ?? false;
    
    if (!useSwtColors) {
      return button;
    }
    
    final swtBackgroundColor = getSwtBackgroundColor(context);
    if (swtBackgroundColor != null) {
      return Container(
        color: swtBackgroundColor,
        child: button,
      );
    }
    
    return button;
  }



  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ButtonThemeExtension>()!;

    final enabled = state.enabled ?? true;
    final text = stripAccelerators(state.text);

    if (hasStyle(state.style, SWT.CHECK)) {
      return _buildCheckBox(context, widgetTheme, enabled, text);
    } else if (hasStyle(state.style, SWT.RADIO)) {
      return _buildRadioButton(context, widgetTheme, enabled, text);
    } else if (hasStyle(state.style, SWT.DROP_DOWN)) {
      return _buildDropdownButton(context, widgetTheme, enabled, text);
    } else if (hasStyle(state.style, SWT.TOGGLE)) {
      return _buildToggleButton(context, widgetTheme, enabled, text);
    } else if (hasStyle(state.style, SWT.ARROW)) {
      return _buildArrowButton(context, widgetTheme, enabled, text);
    } else {
      return _buildPushButton(context, widgetTheme, enabled, text);
    }
  }

  Widget _buildPushButton(BuildContext context, ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    final isPressed = state.selection ?? false;
    final hasFlat = hasStyle(state.style, SWT.FLAT);
    final isPrimary = state.primary ?? false;
    
    final constraints = getConstraintsFromBounds(state.bounds);
    final hasValidBounds = hasBounds(state.bounds);
    
    final baseBackgroundColor = getButtonBackgroundColor(
      state,
      widgetTheme,
      null,
      enabled: enabled,
      isPrimary: isPrimary,
    );
    
    final hoverBackgroundColor = getPushButtonHoverBackgroundColor(
      state,
      widgetTheme,
      enabled: enabled,
      isPrimary: isPrimary,
    );
    
    final pressedBackgroundColor = isPrimary
        ? widgetTheme.pushButtonPressedColor
        : widgetTheme.secondaryButtonPressedColor;
    
    final borderColor = getPushButtonBorderColor(
      state,
      widgetTheme,
      isPrimary: isPrimary,
    );
    
    Widget button = _HoverableButton(
      baseBackgroundColor: baseBackgroundColor,
      hoverBackgroundColor: hoverBackgroundColor,
      pressedBackgroundColor: pressedBackgroundColor,
      borderColor: borderColor,
      isPressed: isPressed,
      hasFlat: hasFlat,
      enabled: enabled,
      constraints: constraints,
      widgetTheme: widgetTheme,
      onPressed: enabled ? _onPressed : null,
      onHover: _onHover,
      onFocusChange: _onFocusChange,
      isPrimary: isPrimary,
      child: _buildButtonContent(
        context,
        text,
        widgetTheme,
        enabled,
        enabled
            ? (isPrimary ? widgetTheme.pushButtonTextColor : widgetTheme.secondaryButtonTextColor)
            : widgetTheme.disabledForegroundColor,
        widgetTheme.pushButtonFontStyle,
        hasBounds: hasValidBounds,
      ),
    );
    
    return wrap(_wrapWithSwtBackground(context, button));
  }

  Widget _buildToggleButton(BuildContext context, ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    final isSelected = state.selection ?? false;
    
    final backgroundColor = getButtonBackgroundColor(
      state,
      widgetTheme,
      isSelected 
          ? widgetTheme.selectableButtonColor
          : widgetTheme.toggleButtonColor,
      enabled: enabled,
    );
    
    final constraints = getConstraintsFromBounds(state.bounds);
    final hasValidBounds = hasBounds(state.bounds);
    
    Widget button = Material(
      color: backgroundColor,
      borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
      child: InkWell(
        onTap: enabled ? _onPressed : null,
        onHover: _onHover,
        onFocusChange: _onFocusChange,
        borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
        splashColor: widgetTheme.splashColor,
        highlightColor: widgetTheme.highlightColor,
        child: Container(
          constraints: constraints,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
            border: Border.all(
              color: widgetTheme.toggleButtonBorderColor,
              width: widgetTheme.pushButtonBorderWidth,
            ),
          ),
          child: _buildButtonContent(
            context,
            text,
            widgetTheme,
            enabled,
            enabled
                ? widgetTheme.pushButtonTextColor
                : widgetTheme.disabledForegroundColor,
            widgetTheme.pushButtonFontStyle,
            hasBounds: hasValidBounds,
          ),
        ),
      ),
    );
    
    return wrap(_wrapWithSwtBackground(context, button));
  }

  Widget _buildCheckBox(BuildContext context, ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    final isChecked = state.selection ?? false;
    final isGrayed = state.grayed ?? false;
    
    final constraints = getConstraintsFromBounds(state.bounds);
    final hasValidBounds = hasBounds(state.bounds);
    
    final checkboxSize = getCheckboxSize(state, widgetTheme.checkboxFontStyle?.fontSize);
    
    final checkboxWidget = _HoverableCheckboxRadio(
      size: checkboxSize,
      enabled: enabled,
      isSelected: isChecked || isGrayed,
      isGrayed: isGrayed,
      baseColor: enabled
          ? (isChecked || isGrayed
              ? widgetTheme.checkboxSelectedColor
              : widgetTheme.checkboxColor)
          : widgetTheme.disabledBackgroundColor,
      hoverColor: widgetTheme.checkboxHoverColor,
      borderColor: enabled
          ? (isChecked || isGrayed
              ? widgetTheme.checkboxSelectedColor
              : widgetTheme.checkboxBorderColor)
          : widgetTheme.disabledForegroundColor,
      borderRadius: widgetTheme.checkboxBorderRadius,
      borderWidth: widgetTheme.checkboxBorderWidth,
      checkmarkSize: checkboxSize * widgetTheme.checkboxCheckmarkSizeMultiplier,
      checkmarkColor: widgetTheme.checkboxCheckmarkColor,
      grayedMargin: checkboxSize * widgetTheme.checkboxGrayedMarginMultiplier,
      grayedBorderRadius: widgetTheme.checkboxGrayedBorderRadius,
      duration: widgetTheme.buttonPressDelay,
      onTap: enabled ? _onPressed : null,
      onHover: _onHover,
      onFocusChange: _onFocusChange,
    );
    
    Widget button = GestureDetector(
      onTap: enabled ? _onPressed : null,
      child: Focus(
        onFocusChange: _onFocusChange,
        child: Container(
          constraints: constraints,
          child: _buildButtonContent(
            context,
            text,
            widgetTheme,
            enabled,
            enabled
                ? widgetTheme.checkboxTextColor
                : widgetTheme.disabledForegroundColor,
            widgetTheme.checkboxFontStyle,
            hasBounds: hasValidBounds,
            leadingWidget: checkboxWidget,
            leadingSpacing: widgetTheme.radioButtonTextSpacing,
          ),
        ),
      ),
    );
    
    return wrap(_wrapWithSwtBackground(context, button));
  }

  Widget _buildRadioButton(BuildContext context, ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    final isSelected = state.selection ?? false;
    
    final constraints = getConstraintsFromBounds(state.bounds);
    final hasValidBounds = hasBounds(state.bounds);
    
    final radioButtonSize = widgetTheme.radioButtonSize;
    final radioButtonInnerSize = widgetTheme.radioButtonInnerSize;
    final radioButtonBorderRadius = widgetTheme.radioButtonBorderRadius;
    
    final radioWidget = _HoverableCheckboxRadio(
      size: radioButtonSize,
      enabled: enabled,
      isSelected: isSelected,
      isGrayed: false,
      baseColor: enabled
          ? (isSelected
              ? widgetTheme.radioButtonSelectedColor
              : widgetTheme.dropdownButtonColor)
          : widgetTheme.disabledBackgroundColor,
      hoverColor: widgetTheme.radioButtonHoverColor,
      selectedHoverColor: widgetTheme.radioButtonSelectedHoverColor,
      borderColor: enabled
          ? (isSelected
              ? widgetTheme.radioButtonSelectedColor
              : widgetTheme.radioButtonBorderColor)
          : widgetTheme.disabledForegroundColor,
      borderRadius: radioButtonBorderRadius,
      borderWidth: isSelected
          ? widgetTheme.radioButtonSelectedBorderWidth
          : widgetTheme.radioButtonBorderWidth,
      isCircle: true,
      innerSize: isSelected ? radioButtonInnerSize : null,
      innerColor: enabled 
          ? widgetTheme.dropdownButtonColor
          : widgetTheme.disabledForegroundColor,
      duration: widgetTheme.buttonPressDelay,
      onTap: enabled ? _onPressed : null,
      onHover: _onHover,
      onFocusChange: _onFocusChange,
    );
    
    Widget button = GestureDetector(
      onTap: enabled ? _onPressed : null,
      child: Focus(
        onFocusChange: _onFocusChange,
        child: Container(
          constraints: constraints,
          child: _buildButtonContent(
            context,
            text,
            widgetTheme,
            enabled,
            enabled
                ? widgetTheme.radioButtonTextColor
                : widgetTheme.disabledForegroundColor,
            widgetTheme.radioButtonFontStyle,
            hasBounds: hasValidBounds,
            leadingWidget: radioWidget,
            leadingSpacing: widgetTheme.radioButtonTextSpacing,
          ),
        ),
      ),
    );
    
    return wrap(_wrapWithSwtBackground(context, button));
  }

  Widget _buildDropdownButton(BuildContext context, ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    
    final constraints = getConstraintsFromBounds(state.bounds);
    final hasValidBounds = hasBounds(state.bounds);
    
    Widget button = Material(
      borderRadius: BorderRadius.circular(widgetTheme.dropdownButtonBorderRadius),
      child: InkWell(
        onTap: enabled ? _onPressed : null,
        onHover: _onHover,
        onFocusChange: _onFocusChange,
        borderRadius: BorderRadius.circular(widgetTheme.dropdownButtonBorderRadius),
        child: AnimatedContainer(
          duration: widgetTheme.buttonPressDelay,
          constraints: constraints,
          decoration: BoxDecoration(
            color: enabled 
                ? widgetTheme.dropdownButtonColor
                : widgetTheme.disabledBackgroundColor,
            borderRadius: BorderRadius.circular(widgetTheme.dropdownButtonBorderRadius),
            border: Border.all(
              color: widgetTheme.dropdownButtonBorderColor,
              width: widgetTheme.dropdownButtonBorderWidth,
            ),
          ),
          child: _buildButtonContent(
            context,
            text,
            widgetTheme,
            enabled,
            enabled
                ? widgetTheme.dropdownButtonTextColor
                : widgetTheme.disabledForegroundColor,
            widgetTheme.dropdownButtonFontStyle,
            hasBounds: hasValidBounds,
            trailingWidget: Icon(
              Icons.arrow_drop_down,
              size: widgetTheme.dropdownButtonIconSize,
              color: enabled 
                  ? widgetTheme.dropdownButtonIconColor
                  : widgetTheme.disabledForegroundColor,
            ),
          ),
        ),
      ),
    );
    
    return wrap(_wrapWithSwtBackground(context, button));
  }

  Widget _buildArrowButton(BuildContext context, ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    final alignment = state.alignment ?? SWT.CENTER;
    final arrowIcon = _getArrowIcon(alignment);
    
    final constraints = getConstraintsFromBounds(state.bounds);
    final hasValidBounds = hasBounds(state.bounds);
    
    Widget button = Material(
      borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
      child: InkWell(
        onTap: enabled ? _onPressed : null,
        onHover: _onHover,
        onFocusChange: _onFocusChange,
        borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
        child: AnimatedContainer(
          duration: widgetTheme.buttonPressDelay,
          constraints: constraints,
          decoration: BoxDecoration(
            color: enabled 
                ? widgetTheme.pushButtonColor
                : widgetTheme.pushButtonDisabledColor,
            borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
            border: Border.all(
              color: widgetTheme.pushButtonBorderColor,
              width: widgetTheme.pushButtonBorderWidth,
            ),
          ),
          child: Icon(
            arrowIcon,
            size: widgetTheme.dropdownButtonIconSize,
            color: enabled 
                ? getForegroundColor(
                    foreground: state.foreground,
                    defaultColor: widgetTheme.pushButtonTextColor,
                  )
                : widgetTheme.disabledForegroundColor,
          ),
        ),
      ),
    );
    
    return wrap(_wrapWithSwtBackground(context, button));
  }

  Widget _buildButtonContent(
    BuildContext context,
    String? text,
    ButtonThemeExtension widgetTheme,
    bool enabled,
    Color defaultTextColor,
    TextStyle? baseTextStyle, {
    bool hasBounds = false,
    Widget? leadingWidget,
    double? leadingSpacing,
    Widget? trailingWidget,
  }) {
    final swtAlignment = state.alignment ?? SWT.CENTER;
    final alignment = swtAlignment == SWT.LEFT
        ? MainAxisAlignment.start
        : swtAlignment == SWT.RIGHT
            ? MainAxisAlignment.end
            : MainAxisAlignment.center;

    final textColor = getForegroundColor(
      foreground: state.foreground,
      defaultColor: defaultTextColor,
    );
    
    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: textColor,
      baseTextStyle: baseTextStyle,
    );
    
    final shouldWrap = shouldWrapText(
      style: state.style,
      hasValidBounds: hasBounds,
      text: text ?? '',
    );
    
    return Row(
      mainAxisSize: hasBounds ? MainAxisSize.max : MainAxisSize.min,
      mainAxisAlignment: alignment,
      children: [
        if (leadingWidget != null) ...[
          leadingWidget,
          if (text?.isNotEmpty == true && leadingSpacing != null)
            SizedBox(width: leadingSpacing),
        ],
        if (state.image != null) ...[
          _buildImageWidget(),
          if (text?.isNotEmpty == true) 
            SizedBox(width: widgetTheme.imageTextSpacing),
        ],
          if (text?.isNotEmpty == true)
          Flexible(
            child: Text(
              text!,
              style: textStyle,
              overflow: shouldWrap ? TextOverflow.visible : TextOverflow.ellipsis,
              maxLines: shouldWrap ? null : 1,
            ),
          ),
        if (trailingWidget != null) ...[
          SizedBox(width: widgetTheme.imageTextSpacing),
          trailingWidget,
        ],
      ],
    );
  }

  Widget _buildImageWidget() {
    if (state.image?.imageData?.data == null) return const SizedBox.shrink();
    
    return Image.memory(
      Uint8List.fromList(state.image!.imageData!.data!),
      fit: BoxFit.contain,
    );
  }

  void _onPressed() {
    if (hasStyle(state.style, SWT.CHECK) || hasStyle(state.style, SWT.RADIO) || hasStyle(state.style, SWT.TOGGLE)) {
      setState(() {
        state.selection = !(state.selection ?? false);
      });
    }
    
    widget.sendSelectionSelection(state, null);
  }

  void _onHover(bool isHovering) {
    if (isHovering) {
      widget.sendMouseTrackMouseEnter(state, null);
    } else {
      widget.sendMouseTrackMouseExit(state, null);
    }
  }

  void _onFocusChange(bool hasFocus) {
    if (hasFocus) {
      widget.sendFocusFocusIn(state, null);
    } else {
      widget.sendFocusFocusOut(state, null);
    }
  }
}

class _HoverableButton extends StatefulWidget {
  final Color baseBackgroundColor;
  final Color hoverBackgroundColor;
  final Color pressedBackgroundColor;
  final Color borderColor;
  final bool isPressed;
  final bool hasFlat;
  final bool enabled;
  final BoxConstraints? constraints;
  final ButtonThemeExtension widgetTheme;
  final VoidCallback? onPressed;
  final ValueChanged<bool> onHover;
  final ValueChanged<bool> onFocusChange;
  final bool isPrimary;
  final Widget child;

  const _HoverableButton({
    required this.baseBackgroundColor,
    required this.hoverBackgroundColor,
    required this.pressedBackgroundColor,
    required this.borderColor,
    required this.isPressed,
    required this.hasFlat,
    required this.enabled,
    this.constraints,
    required this.widgetTheme,
    this.onPressed,
    required this.onHover,
    required this.onFocusChange,
    required this.isPrimary,
    required this.child,
  });

  @override
  State<_HoverableButton> createState() => _HoverableButtonState();
}

class _HoverableButtonState extends State<_HoverableButton> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    final backgroundColor = widget.enabled 
        ? (widget.isPressed 
            ? widget.pressedBackgroundColor
            : (_isHovering 
                ? widget.hoverBackgroundColor
                : widget.baseBackgroundColor))
        : widget.widgetTheme.pushButtonDisabledColor;

    return MouseRegion(
      onEnter: (_) {
        setState(() {
          _isHovering = true;
        });
        widget.onHover(true);
      },
      onExit: (_) {
        setState(() {
          _isHovering = false;
        });
        widget.onHover(false);
      },
      child: Material(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(widget.widgetTheme.pushButtonBorderRadius),
        child: InkWell(
          onTap: widget.onPressed,
          onFocusChange: widget.onFocusChange,
          borderRadius: BorderRadius.circular(widget.widgetTheme.pushButtonBorderRadius),
          splashColor: widget.widgetTheme.splashColor,
          highlightColor: widget.widgetTheme.highlightColor,
          child: AnimatedContainer(
            duration: widget.widgetTheme.buttonPressDelay,
            constraints: widget.constraints,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(widget.widgetTheme.pushButtonBorderRadius),
              border: Border.all(
                color: widget.borderColor,
                width: widget.widgetTheme.pushButtonBorderWidth,
              ),
            ),
            child: widget.child,
          ),
        ),
      ),
    );
  }
}

class _HoverableCheckboxRadio extends StatefulWidget {
  final double size;
  final bool enabled;
  final bool isSelected;
  final bool isGrayed;
  final Color baseColor;
  final Color hoverColor;
  final Color? selectedHoverColor;
  final Color borderColor;
  final double borderRadius;
  final double borderWidth;
  final double? checkmarkSize;
  final Color? checkmarkColor;
  final double? grayedMargin;
  final double? grayedBorderRadius;
  final bool isCircle;
  final double? innerSize;
  final Color? innerColor;
  final Duration duration;
  final VoidCallback? onTap;
  final ValueChanged<bool> onHover;
  final ValueChanged<bool> onFocusChange;

  const _HoverableCheckboxRadio({
    required this.size,
    required this.enabled,
    required this.isSelected,
    required this.isGrayed,
    required this.baseColor,
    required this.hoverColor,
    this.selectedHoverColor,
    required this.borderColor,
    required this.borderRadius,
    required this.borderWidth,
    this.checkmarkSize,
    this.checkmarkColor,
    this.grayedMargin,
    this.grayedBorderRadius,
    this.isCircle = false,
    this.innerSize,
    this.innerColor,
    required this.duration,
    this.onTap,
    required this.onHover,
    required this.onFocusChange,
  });

  @override
  State<_HoverableCheckboxRadio> createState() => _HoverableCheckboxRadioState();
}

class _HoverableCheckboxRadioState extends State<_HoverableCheckboxRadio> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    Color backgroundColor;
    if (widget.enabled && _isHovering) {
      if (widget.isSelected && widget.selectedHoverColor != null) {
        backgroundColor = widget.selectedHoverColor!;
      } else if (!widget.isSelected) {
        backgroundColor = widget.hoverColor;
      } else {
        backgroundColor = widget.baseColor;
      }
    } else {
      backgroundColor = widget.baseColor;
    }

    Widget content;
    
    if (widget.isCircle) {
      // Radio button
      content = AnimatedContainer(
        duration: widget.duration,
        width: widget.size,
        height: widget.size,
        decoration: BoxDecoration(
          color: backgroundColor,
          shape: BoxShape.circle,
          border: Border.all(
            color: widget.borderColor,
            width: widget.borderWidth,
          ),
        ),
        child: widget.isSelected && widget.innerSize != null
            ? Center(
                child: Container(
                  width: widget.innerSize,
                  height: widget.innerSize,
                  decoration: BoxDecoration(
                    color: widget.innerColor,
                    shape: BoxShape.circle,
                  ),
                ),
              )
            : null,
      );
    } else {
      // Checkbox
      content = AnimatedContainer(
        duration: widget.duration,
        width: widget.size,
        height: widget.size,
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular(widget.borderRadius),
          border: Border.all(
            color: widget.borderColor,
            width: widget.borderWidth,
          ),
        ),
        child: widget.isSelected
            ? Icon(
                Icons.check,
                size: widget.checkmarkSize,
                color: widget.checkmarkColor,
              )
            : (widget.isGrayed
                ? Container(
                    margin: EdgeInsets.all(widget.grayedMargin ?? 0),
                    decoration: BoxDecoration(
                      color: widget.checkmarkColor,
                      borderRadius: BorderRadius.circular(widget.grayedBorderRadius ?? 0),
                    ),
                  )
                : null),
      );
    }

    return MouseRegion(
      onEnter: (_) {
        setState(() {
          _isHovering = true;
        });
        widget.onHover(true);
      },
      onExit: (_) {
        setState(() {
          _isHovering = false;
        });
        widget.onHover(false);
      },
      child: GestureDetector(
        onTap: widget.onTap,
        child: Focus(
          onFocusChange: widget.onFocusChange,
          child: content,
        ),
      ),
    );
  }
}
