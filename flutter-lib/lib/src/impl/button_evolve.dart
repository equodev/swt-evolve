import 'package:flutter/material.dart';
import 'dart:typed_data';
import '../gen/button.dart';
import '../gen/swt.dart';
import '../impl/control_evolve.dart';
import '../theme/button_theme_extension.dart';
import '../theme/button_theme_settings.dart';
import 'utils/text_utils.dart';
import 'widget_config.dart';

class ButtonImpl<T extends ButtonSwt, V extends VButton>
    extends ControlImpl<T, V> {

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    final widgetTheme = Theme.of(context).extension<ButtonThemeExtension>()!;

    bool hasStyle(int style) => (state.style & style) != 0;
    final enabled = state.enabled ?? true;
    final text = stripAccelerators(state.text);

    if (hasStyle(SWT.CHECK)) {
      return _buildCheckBox(context, colorScheme, textTheme, widgetTheme, enabled, text);
    } else if (hasStyle(SWT.RADIO)) {
      return _buildRadioButton(context, colorScheme, textTheme, widgetTheme, enabled, text);
    } else if (hasStyle(SWT.DROP_DOWN)) {
      return _buildDropdownButton(context, colorScheme, textTheme, widgetTheme, enabled, text);
    } else if (hasStyle(SWT.TOGGLE)) {
      return _buildToggleButton(context, colorScheme, textTheme, widgetTheme, enabled, text);
    } else if (hasStyle(SWT.ARROW)) {
      return _buildArrowButton(context, colorScheme, textTheme, widgetTheme, enabled, text);
    } else {
      return _buildPushButton(context, colorScheme, textTheme, widgetTheme, enabled, text);
    }
  }

  Widget _buildPushButton(BuildContext context, ColorScheme colorScheme, TextTheme textTheme, 
      ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    final isPressed = state.selection ?? false;
    final hasFlat = (state.style & SWT.FLAT) != 0;
    
    final backgroundColor = enabled 
        ? (isPressed 
            ? getButtonBackgroundColor(
                state,
                widgetTheme,
                colorScheme,
                widgetTheme.pushButtonHoverColor,
              )
            : getButtonBackgroundColor(
                state,
                widgetTheme,
                colorScheme,
                widgetTheme.pushButtonColor ?? colorScheme.primary,
              ))
        : widgetTheme.pushButtonDisabledColor;
    
    final hasBounds = state.bounds != null && state.bounds!.width > 0 && state.bounds!.height > 0;
    final constraints = hasBounds
        ? BoxConstraints(
            minWidth: state.bounds!.width.toDouble(),
            maxWidth: state.bounds!.width.toDouble(),
            minHeight: state.bounds!.height.toDouble(),
            maxHeight: state.bounds!.height.toDouble(),
          )
        : null;
    
    return wrap(
      Material(
        color: backgroundColor,
        elevation: hasFlat ? 0.0 : widgetTheme.pushButtonElevation,
        borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
        child: InkWell(
          onTap: enabled ? _onPressed : null,
          onHover: _onHover,
          onFocusChange: _onFocusChange,
          borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
          splashColor: Colors.transparent,
          highlightColor: Colors.transparent,
          child: Container(
            constraints: constraints,
            padding: widgetTheme.pushButtonPadding,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
              border: Border.all(
                color: widgetTheme.pushButtonBorderColor,
                width: widgetTheme.pushButtonBorderWidth,
              ),
            ),
            child: _buildButtonContent(context, text, widgetTheme, colorScheme, textTheme, enabled, 'push', hasBounds: hasBounds),
          ),
        ),
      ),
    );
  }

  Widget _buildToggleButton(BuildContext context, ColorScheme colorScheme, TextTheme textTheme,
      ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    final isSelected = state.selection ?? false;
    
    final backgroundColor = enabled 
        ? (isSelected 
            ? getButtonBackgroundColor(
                state,
                widgetTheme,
                colorScheme,
                widgetTheme.selectableButtonColor ?? colorScheme.primary,
              )
            : getButtonBackgroundColor(
                state,
                widgetTheme,
                colorScheme,
                widgetTheme.pushButtonColor ?? colorScheme.surface,
              ))
        : widgetTheme.pushButtonDisabledColor;
    
    final hasBounds = state.bounds != null && state.bounds!.width > 0 && state.bounds!.height > 0;
    final constraints = hasBounds
        ? BoxConstraints(
            minWidth: state.bounds!.width.toDouble(),
            maxWidth: state.bounds!.width.toDouble(),
            minHeight: state.bounds!.height.toDouble(),
            maxHeight: state.bounds!.height.toDouble(),
          )
        : null;
    
    return wrap(
      Material(
        color: backgroundColor,
        elevation: isSelected ? widgetTheme.pushButtonElevation : 0.0,
        borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
        child: InkWell(
          onTap: enabled ? _onPressed : null,
          onHover: _onHover,
          onFocusChange: _onFocusChange,
          borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
          splashColor: Colors.transparent,
          highlightColor: Colors.transparent,
          child: Container(
            constraints: constraints,
            padding: widgetTheme.pushButtonPadding,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
              border: Border.all(
                color: widgetTheme.pushButtonBorderColor,
                width: widgetTheme.pushButtonBorderWidth,
              ),
            ),
            child: _buildButtonContent(context, text, widgetTheme, colorScheme, textTheme, enabled, 'toggle', selected: isSelected, hasBounds: hasBounds),
          ),
        ),
      ),
    );
  }

  Widget _buildCheckBox(BuildContext context, ColorScheme colorScheme, TextTheme textTheme,
      ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    final isChecked = state.selection ?? false;
    final isGrayed = state.grayed ?? false;
    
    final hasBounds = state.bounds != null && state.bounds!.width > 0 && state.bounds!.height > 0;
    final constraints = hasBounds
        ? BoxConstraints(
            minWidth: state.bounds!.width.toDouble(),
            maxWidth: state.bounds!.width.toDouble(),
            minHeight: state.bounds!.height.toDouble(),
            maxHeight: state.bounds!.height.toDouble(),
          )
        : null;
    
    return wrap(
      InkWell(
        onTap: enabled ? _onPressed : null,
        onHover: _onHover,
        onFocusChange: _onFocusChange,
        borderRadius: BorderRadius.circular(widgetTheme.checkboxBorderRadius),
        child: Container(
          constraints: constraints,
          padding: widgetTheme.checkboxPadding,
          child: Row(
            mainAxisSize: hasBounds ? MainAxisSize.max : MainAxisSize.min,
            children: [
              AnimatedContainer(
                duration: widgetTheme.buttonPressDelay,
                width: widgetTheme.checkboxSize,
                height: widgetTheme.checkboxSize,
                decoration: BoxDecoration(
                  color: enabled
                      ? (isChecked || isGrayed
                          ? widgetTheme.checkboxSelectedColor
                          : getButtonBackgroundColor(
                              state,
                              widgetTheme,
                              colorScheme,
                              widgetTheme.checkboxColor ?? colorScheme.surface,
                            ))
                      : getDisabledBackgroundColor(widgetTheme, colorScheme),
                  borderRadius: BorderRadius.circular(widgetTheme.checkboxBorderRadius),
                  border: Border.all(
                    color: enabled
                        ? (isChecked || isGrayed
                            ? widgetTheme.checkboxSelectedColor
                            : widgetTheme.checkboxBorderColor)
                        : getDisabledForegroundColor(widgetTheme, colorScheme),
                    width: widgetTheme.checkboxBorderWidth,
                  ),
                ),
                child: isChecked
                    ? Icon(
                        Icons.check,
                        size: widgetTheme.checkboxSize * widgetTheme.checkboxCheckmarkSizeMultiplier,
                        color: widgetTheme.checkboxCheckmarkColor,
                      )
                    : (isGrayed
                        ? Container(
                            margin: EdgeInsets.all(widgetTheme.checkboxSize * widgetTheme.checkboxGrayedMarginMultiplier),
                            decoration: BoxDecoration(
                              color: widgetTheme.checkboxCheckmarkColor,
                              borderRadius: BorderRadius.circular(widgetTheme.checkboxGrayedBorderRadius),
                            ),
                          )
                        : null),
              ),
              if (text?.isNotEmpty == true) ...[
                SizedBox(width: widgetTheme.checkboxTextSpacing),
                Text(
                  text!,
                  style: getButtonTextStyle(
                    context,
                    state,
                    widgetTheme,
                    colorScheme,
                    textTheme,
                    getButtonForegroundColor(
                      state,
                      widgetTheme,
                      colorScheme,
                      enabled 
                          ? widgetTheme.checkboxTextColor
                          : getDisabledForegroundColor(widgetTheme, colorScheme),
                    ),
                    widgetTheme.checkboxFontSize,
                    widgetTheme.checkboxFontWeight,
                    widgetTheme.checkboxFontFamily,
                    widgetTheme.checkboxLetterSpacing,
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildRadioButton(BuildContext context, ColorScheme colorScheme, TextTheme textTheme,
      ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    final isSelected = state.selection ?? false;
    
    final hasBounds = state.bounds != null && state.bounds!.width > 0 && state.bounds!.height > 0;
    final constraints = hasBounds
        ? BoxConstraints(
            minWidth: state.bounds!.width.toDouble(),
            maxWidth: state.bounds!.width.toDouble(),
            minHeight: state.bounds!.height.toDouble(),
            maxHeight: state.bounds!.height.toDouble(),
          )
        : null;
    
    return wrap(
      InkWell(
        onTap: enabled ? _onPressed : null,
        onHover: _onHover,
        onFocusChange: _onFocusChange,
        borderRadius: BorderRadius.circular(widgetTheme.radioButtonSize),
        child: Container(
          constraints: constraints,
          padding: widgetTheme.radioButtonPadding,
          child: Row(
            mainAxisSize: hasBounds ? MainAxisSize.max : MainAxisSize.min,
            children: [
              AnimatedContainer(
                duration: widgetTheme.buttonPressDelay,
                width: widgetTheme.radioButtonSize,
                height: widgetTheme.radioButtonSize,
                decoration: BoxDecoration(
                  color: enabled
                      ? (isSelected
                          ? widgetTheme.radioButtonSelectedColor
                          : getButtonBackgroundColor(
                              state,
                              widgetTheme,
                              colorScheme,
                              colorScheme.surface,
                            ))
                      : getDisabledBackgroundColor(widgetTheme, colorScheme),
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: enabled
                        ? (isSelected
                            ? widgetTheme.radioButtonSelectedColor
                            : widgetTheme.radioButtonBorderColor)
                        : getDisabledForegroundColor(widgetTheme, colorScheme),
                    width: isSelected
                        ? widgetTheme.radioButtonSelectedBorderWidth
                        : widgetTheme.radioButtonBorderWidth,
                  ),
                ),
                child: isSelected
                    ? Center(
                        child: Container(
                          width: widgetTheme.radioButtonInnerSize,
                          height: widgetTheme.radioButtonInnerSize,
                          decoration: BoxDecoration(
                            color: enabled 
                                ? colorScheme.surface
                                : getDisabledForegroundColor(widgetTheme, colorScheme),
                            shape: BoxShape.circle,
                          ),
                        ),
                      )
                    : null,
              ),
              if (text?.isNotEmpty == true) ...[
                SizedBox(width: widgetTheme.radioButtonTextSpacing),
                Text(
                  text!,
                  style: getButtonTextStyle(
                    context,
                    state,
                    widgetTheme,
                    colorScheme,
                    textTheme,
                    getButtonForegroundColor(
                      state,
                      widgetTheme,
                      colorScheme,
                      enabled 
                          ? widgetTheme.radioButtonTextColor
                          : getDisabledForegroundColor(widgetTheme, colorScheme),
                    ),
                    widgetTheme.radioButtonFontSize,
                    widgetTheme.radioButtonFontWeight,
                    widgetTheme.radioButtonFontFamily,
                    widgetTheme.radioButtonLetterSpacing,
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildDropdownButton(BuildContext context, ColorScheme colorScheme, TextTheme textTheme,
      ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    
    final hasBounds = state.bounds != null && state.bounds!.width > 0 && state.bounds!.height > 0;
    final constraints = hasBounds
        ? BoxConstraints(
            minWidth: state.bounds!.width.toDouble(),
            maxWidth: state.bounds!.width.toDouble(),
            minHeight: state.bounds!.height.toDouble(),
            maxHeight: state.bounds!.height.toDouble(),
          )
        : null;
    
    return wrap(
      Material(
        borderRadius: BorderRadius.circular(widgetTheme.dropdownButtonBorderRadius),
        child: InkWell(
          onTap: enabled ? _onPressed : null,
          onHover: _onHover,
          onFocusChange: _onFocusChange,
          borderRadius: BorderRadius.circular(widgetTheme.dropdownButtonBorderRadius),
          child: AnimatedContainer(
            duration: widgetTheme.buttonPressDelay,
            constraints: constraints,
            padding: widgetTheme.dropdownButtonPadding,
            decoration: BoxDecoration(
              color: enabled 
                  ? getButtonBackgroundColor(
                      state,
                      widgetTheme,
                      colorScheme,
                      widgetTheme.dropdownButtonColor ?? colorScheme.surface,
                    )
                  : getDisabledBackgroundColor(widgetTheme, colorScheme),
              borderRadius: BorderRadius.circular(widgetTheme.dropdownButtonBorderRadius),
              border: Border.all(
                color: widgetTheme.dropdownButtonBorderColor,
                width: widgetTheme.dropdownButtonBorderWidth,
              ),
            ),
            child: Row(
              mainAxisSize: hasBounds ? MainAxisSize.max : MainAxisSize.min,
              children: [
                if (state.image != null) ...[
                  _buildImageWidget(widgetTheme),
                  if (text?.isNotEmpty == true) 
                    SizedBox(width: widgetTheme.imageTextSpacing),
                ],
                if (text?.isNotEmpty == true)
                  Text(
                    text!,
                    style: getButtonTextStyle(
                      context,
                      state,
                      widgetTheme,
                      colorScheme,
                      textTheme,
                      getButtonForegroundColor(
                        state,
                        widgetTheme,
                        colorScheme,
                        enabled 
                            ? widgetTheme.dropdownButtonTextColor
                            : getDisabledForegroundColor(widgetTheme, colorScheme),
                      ),
                      widgetTheme.dropdownButtonFontSize,
                      widgetTheme.dropdownButtonFontWeight,
                      widgetTheme.dropdownButtonFontFamily,
                      widgetTheme.dropdownButtonLetterSpacing,
                    ),
                  ),
                const SizedBox(width: 8.0),
                Icon(
                  Icons.arrow_drop_down,
                  size: widgetTheme.dropdownButtonIconSize,
                  color: enabled 
                      ? widgetTheme.dropdownButtonIconColor
                      : getDisabledForegroundColor(widgetTheme, colorScheme),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildArrowButton(BuildContext context, ColorScheme colorScheme, TextTheme textTheme,
      ButtonThemeExtension widgetTheme, bool enabled, String? text) {
    final alignment = state.alignment ?? SWT.CENTER;
    IconData arrowIcon;
    
    switch (alignment) {
      case SWT.UP:
        arrowIcon = Icons.keyboard_arrow_up;
        break;
      case SWT.DOWN:
        arrowIcon = Icons.keyboard_arrow_down;
        break;
      case SWT.LEFT:
        arrowIcon = Icons.keyboard_arrow_left;
        break;
      case SWT.RIGHT:
        arrowIcon = Icons.keyboard_arrow_right;
        break;
      default:
        arrowIcon = Icons.keyboard_arrow_down;
    }
    
    final hasBounds = state.bounds != null && state.bounds!.width > 0 && state.bounds!.height > 0;
    final constraints = hasBounds
        ? BoxConstraints(
            minWidth: state.bounds!.width.toDouble(),
            maxWidth: state.bounds!.width.toDouble(),
            minHeight: state.bounds!.height.toDouble(),
            maxHeight: state.bounds!.height.toDouble(),
          )
        : null;
    
    return wrap(
      Material(
        elevation: widgetTheme.pushButtonElevation,
        borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
        child: InkWell(
          onTap: enabled ? _onPressed : null,
          onHover: _onHover,
          onFocusChange: _onFocusChange,
          borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
          child: AnimatedContainer(
            duration: widgetTheme.buttonPressDelay,
            constraints: constraints,
            padding: widgetTheme.pushButtonPadding,
            decoration: BoxDecoration(
              color: enabled 
                  ? getButtonBackgroundColor(
                      state,
                      widgetTheme,
                      colorScheme,
                      widgetTheme.pushButtonColor ?? colorScheme.primary,
                    )
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
                  ? getButtonForegroundColor(
                      state,
                      widgetTheme,
                      colorScheme,
                      widgetTheme.pushButtonTextColor,
                    )
                  : getDisabledForegroundColor(widgetTheme, colorScheme),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildButtonContent(BuildContext context, String? text, ButtonThemeExtension widgetTheme, 
      ColorScheme colorScheme, TextTheme textTheme, bool enabled, String buttonType, {bool selected = false, bool hasBounds = false}) {
    
    MainAxisAlignment alignment = MainAxisAlignment.center;
    final alignmentValue = state.alignment ?? SWT.CENTER;
    
    switch (alignmentValue) {
      case SWT.LEFT:
        alignment = MainAxisAlignment.start;
        break;
      case SWT.RIGHT:
        alignment = MainAxisAlignment.end;
        break;
      default:
        alignment = MainAxisAlignment.center;
    }

    Color defaultTextColor;
    double fontSize;
    FontWeight fontWeight;
    String? fontFamily;
    double letterSpacing;

    switch (buttonType) {
      case 'push':
        defaultTextColor = enabled 
            ? widgetTheme.pushButtonTextColor
            : getDisabledForegroundColor(widgetTheme, colorScheme);
        fontSize = widgetTheme.pushButtonFontSize;
        fontWeight = widgetTheme.pushButtonFontWeight;
        fontFamily = widgetTheme.pushButtonFontFamily;
        letterSpacing = widgetTheme.pushButtonLetterSpacing;
        break;
      case 'toggle':
        defaultTextColor = enabled 
            ? widgetTheme.pushButtonTextColor
            : getDisabledForegroundColor(widgetTheme, colorScheme);
        fontSize = widgetTheme.pushButtonFontSize;
        fontWeight = widgetTheme.pushButtonFontWeight;
        fontFamily = widgetTheme.pushButtonFontFamily;
        letterSpacing = widgetTheme.pushButtonLetterSpacing;
        break;
      default:
        defaultTextColor = enabled 
            ? widgetTheme.pushButtonTextColor
            : getDisabledForegroundColor(widgetTheme, colorScheme);
        fontSize = widgetTheme.pushButtonFontSize;
        fontWeight = widgetTheme.pushButtonFontWeight;
        fontFamily = widgetTheme.pushButtonFontFamily;
        letterSpacing = widgetTheme.pushButtonLetterSpacing;
    }
    
    final textColor = getButtonForegroundColor(
      state,
      widgetTheme,
      colorScheme,
      defaultTextColor,
    );
    
    final textStyle = getButtonTextStyle(
      context,
      state,
      widgetTheme,
      colorScheme,
      textTheme,
      textColor,
      fontSize,
      fontWeight,
      fontFamily,
      letterSpacing,
    );
    
    return Row(
      mainAxisSize: hasBounds ? MainAxisSize.max : MainAxisSize.min,
      mainAxisAlignment: alignment,
      children: [
        if (state.image != null) ...[
          _buildImageWidget(widgetTheme),
          if (text?.isNotEmpty == true) 
            SizedBox(width: widgetTheme.imageTextSpacing),
        ],
        if (text?.isNotEmpty == true)
          Flexible(
            child: Text(
              text!,
              style: textStyle,
              overflow: (state.style & SWT.WRAP) != 0 ? TextOverflow.visible : TextOverflow.ellipsis,
              maxLines: (state.style & SWT.WRAP) != 0 ? null : 1,
            ),
          ),
      ],
    );
  }

  Widget _buildImageWidget(ButtonThemeExtension widgetTheme) {
    if (state.image?.imageData?.data == null) return const SizedBox.shrink();
    
    final imageSize = widgetTheme.pushButtonHeight * widgetTheme.buttonImageSizeMultiplier;
    
    return Image.memory(
      Uint8List.fromList(state.image!.imageData!.data!),
      width: imageSize,
      height: imageSize,
      fit: BoxFit.contain,
    );
  }

  void _onPressed() {
    bool hasStyle(int style) => (state.style & style) != 0;
    
    if (hasStyle(SWT.CHECK) || hasStyle(SWT.RADIO) || hasStyle(SWT.TOGGLE)) {
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
