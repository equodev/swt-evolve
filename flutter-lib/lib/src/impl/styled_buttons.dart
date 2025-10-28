import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'dart:io';
import 'color_utils.dart';
import 'icons_map.dart';
import 'utils/text_utils.dart';
import 'utils/font_utils.dart';
import '../gen/color.dart';

// DROP DOWN BUTTON
class MaterialDropdownButton extends StatelessWidget {
  final String text;
  final String? image;
  final VoidCallback? onPressed;
  final bool enabled;
  final double borderRadius;
  final double height;
  final double minWidth;
  final bool useDarkTheme;
  final VFont? vFont;
  final VColor? textColor;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const MaterialDropdownButton({
    Key? key,
    required this.text,
    this.image,
    this.onPressed,
    this.enabled = true,
    this.borderRadius = 5.0,
    this.height = 30.0,
    this.minWidth = 70.0,
    this.useDarkTheme = false,
    this.vFont,
    this.textColor,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // Colors for buttons without image
    const Color darkButtonColor = Color(0xFF6366F1);
    const Color darkTextColor = Color(0xFFFFFFFF);

    const Color lightButtonColor = Color(0xFF6366F1);
    const Color lightTextColor = Color(0xFF595858);

    // Determine if we should use transparent background based on image presence
    final bool hasImage = image != null;
    final Color buttonColor = hasImage
        ? Colors.transparent
        : (useDarkTheme ? darkButtonColor : lightButtonColor);

    // Get text color from VColor or use default
    final Color defaultTextColor =
        useDarkTheme ? darkTextColor : lightTextColor;
    final Color finalTextColor =
        colorFromVColor(textColor, defaultColor: defaultTextColor);

    // Create TextStyle from VFont
    final textStyle = FontUtils.textStyleFromVFont(
      vFont,
      context,
      color: finalTextColor.withOpacity(enabled ? 1.0 : 0.5),
    );

    const double iconSize = 24.0;

    if (hasImage) {
      return MouseRegion(
          onEnter: (_) => onMouseEnter?.call(),
          onExit: (_) => onMouseExit?.call(),
          child: Focus(
            onFocusChange: (hasFocus) {
              if (hasFocus) {
                onFocusIn?.call();
              } else {
                onFocusOut?.call();
              }
            },
            child: Material(
              child: InkWell(
                onTap: enabled ? onPressed : null,
                child: Container(
                  height: height,
                  decoration: BoxDecoration(
                    color: Colors.transparent,
                    borderRadius: BorderRadius.circular(borderRadius),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      !iconMap.containsKey(image)
                          ? (image!.toLowerCase().endsWith('.svg')
                              ? SvgPicture.file(
                                  File(image!),
                                  width: iconSize * 1.3,
                                  height: iconSize * 1.3,
                                )
                              : Image.file(
                                  File(image!),
                                  width: iconSize * 1.3,
                                  height: iconSize * 1.3,
                                ))
                          : Icon(
                              getIconByName(image!),
                              size: iconSize * 1.3,
                              color: finalTextColor,
                            ),
                      const SizedBox(width: 2),
                      Text(
                        stripAccelerators(text),
                        style: textStyle,
                      ),
                      Icon(
                        Icons.keyboard_arrow_down,
                        color: finalTextColor,
                        size: 18,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ));
    }

    return MouseRegion(
      onEnter: (_) => onMouseEnter?.call(),
      onExit: (_) => onMouseExit?.call(),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            onFocusIn?.call();
          } else {
            onFocusOut?.call();
          }
        },
        child: MaterialButton(
          onPressed: enabled ? onPressed : null,
          elevation: 0,
          focusElevation: 0,
          hoverElevation: 0,
          highlightElevation: 0,
          disabledElevation: 0,
          height: height,
          minWidth: minWidth,
          padding: EdgeInsets.zero,
          color: enabled ? buttonColor : buttonColor.withOpacity(0.5),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(borderRadius),
          ),
          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                stripAccelerators(text),
                style: textStyle,
              ),
              Icon(
                Icons.keyboard_arrow_down,
                color: finalTextColor,
                size: 18,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// TOGGLE BUTTON
class SelectableButton extends StatelessWidget {
  final String? text;
  final String? image;
  final bool isSelected;
  final VoidCallback onPressed;
  final bool enabled;
  final double borderRadius;
  final double height;
  final double minWidth;
  final bool useDarkTheme;
  final Color? backgroundColor;
  final VFont? vFont;
  final VColor? textColor;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const SelectableButton({
    Key? key,
    this.text,
    required this.isSelected,
    this.image,
    required this.onPressed,
    this.enabled = true,
    this.borderRadius = 5.0,
    this.height = 30.0,
    this.minWidth = 70.0,
    this.useDarkTheme = false,
    this.backgroundColor,
    this.vFont,
    this.textColor,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color darkSelectedColor = const Color(0xFF6366F1);
    final Color darkUnselectedColor = const Color(0xFF4E4D4D);
    final Color darkSelectedTextColor = const Color(0xFFFFFFFF);
    final Color darkUnselectedTextColor = const Color(0xFFFFFFFF);

    final Color lightSelectedColor = const Color(0xFF6366F1);
    final Color lightUnselectedColor = const Color(0xFFC8C7C7);
    final Color lightSelectedTextColor = const Color(0xFFFFFFFF);
    final Color lightUnselectedTextColor = const Color(0xFF595858);

    final Color selectedColor =
        useDarkTheme ? darkSelectedColor : lightSelectedColor;
    final Color unselectedColor =
        useDarkTheme ? darkUnselectedColor : lightUnselectedColor;
    final Color selectedTextColor =
        useDarkTheme ? darkSelectedTextColor : lightSelectedTextColor;
    final Color unselectedTextColor =
        useDarkTheme ? darkUnselectedTextColor : lightUnselectedTextColor;

    // Get text color from VColor or use default
    final Color defaultTextColor =
        isSelected ? selectedTextColor : unselectedTextColor;
    final Color finalTextColor =
        colorFromVColor(textColor, defaultColor: defaultTextColor);

    // Create TextStyle from VFont
    final textStyle = FontUtils.textStyleFromVFont(
      vFont,
      context,
      color: finalTextColor,
    );

    final Color iconColor =
        isSelected ? selectedTextColor : unselectedTextColor;

    final bool isIconOnly = image != null && (text == null || text!.isEmpty);

    const double iconSize = 24.0;

    if (isIconOnly) {
      return MouseRegion(
          onEnter: (_) => onMouseEnter?.call(),
          onExit: (_) => onMouseExit?.call(),
          child: Focus(
            onFocusChange: (hasFocus) {
              if (hasFocus) {
                onFocusIn?.call();
              } else {
                onFocusOut?.call();
              }
            },
            child: Material(
              child: InkWell(
                onTap: enabled ? onPressed : null,
                borderRadius: BorderRadius.circular(iconSize),
                child: Container(
                    width: iconSize + 8,
                    height: iconSize + 8,
                    decoration: isSelected
                        ? BoxDecoration(
                            color: selectedColor.withOpacity(0.2),
                            shape: BoxShape.circle,
                          )
                        : null,
                    child: Center(
                      child: !iconMap.containsKey(image)
                          ? (image!.toLowerCase().endsWith('.svg')
                              ? SvgPicture.file(
                                  File(image!),
                                )
                              : Image.file(
                                  File(image!),
                                  width: iconSize,
                                  height: iconSize,
                                ))
                          : Icon(
                              getIconByName(image!),
                              size: iconSize,
                              color: iconColor,
                            ),
                    )),
              ),
            ),
          ));
    }

    Widget child = MouseRegion(
      onEnter: (_) => onMouseEnter?.call(),
      onExit: (_) => onMouseExit?.call(),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            onFocusIn?.call();
          } else {
            onFocusOut?.call();
          }
        },
        child: MaterialButton(
          onPressed: enabled ? onPressed : null,
          elevation: 0,
          focusElevation: 0,
          hoverElevation: 0,
          highlightElevation: 0,
          disabledElevation: 0,
          height: height,
          minWidth: minWidth,
          padding: EdgeInsets.zero,
          color: isSelected
              ? (enabled ? selectedColor : selectedColor.withOpacity(0.5))
              : unselectedColor,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(borderRadius),
          ),
          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
          child: Row(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              if (image != null) ...[
                !iconMap.containsKey(image)
                    ? (image!.toLowerCase().endsWith('.svg')
                        ? SvgPicture.file(
                            File(image!),
                          )
                        : Image.file(
                            File(image!),
                            width: iconSize - 8,
                            height: iconSize - 8,
                          ))
                    : Icon(
                        getIconByName(image!),
                        size: iconSize - 8,
                        color: iconColor,
                      ),
                const SizedBox(width: 6),
              ],
              // Display text
              Text(
                stripAccelerators(text ?? ""),
                style: textStyle,
              ),
            ],
          ),
        ),
      ),
    );

    if (backgroundColor != null) {
      return Container(
        color: backgroundColor,
        child: child,
      );
    }

    return child;
  }
}

// PUSH BUTTON
class PushButton extends StatefulWidget {
  final String? text;
  final String? image;
  final VoidCallback onPressed;
  final bool enabled;
  final double borderRadius;
  final double height;
  final double minWidth;
  final bool useDarkTheme;
  final VFont? vFont;
  final VColor? textColor;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const PushButton({
    Key? key,
    this.text,
    this.image,
    required this.onPressed,
    this.enabled = true,
    this.borderRadius = 5.0,
    this.height = 30.0,
    this.minWidth = 70.0,
    this.useDarkTheme = false,
    this.vFont,
    this.textColor,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  State<PushButton> createState() => _PushButtonState();
}

class _PushButtonState extends State<PushButton> {
  bool _isPressed = false;

  @override
  Widget build(BuildContext context) {
    final Color darkSelectedColor = const Color(0xFF6366F1);
    final Color darkUnselectedColor = const Color(0xFF6366F1);
    final Color darkSelectedTextColor = const Color(0xFFFFFFFF);
    final Color darkUnselectedTextColor = const Color(0xFFFFFFFF);

    final Color lightSelectedColor = const Color(0xFF6366F1);
    final Color lightUnselectedColor = const Color(0xFF6366F1);
    final Color lightSelectedTextColor = const Color(0xFFFFFFFF);
    final Color lightUnselectedTextColor = const Color(0xFFFFFFFF);

    final Color selectedColor =
        widget.useDarkTheme ? darkSelectedColor : lightSelectedColor;
    final Color unselectedColor =
        widget.useDarkTheme ? darkUnselectedColor : lightUnselectedColor;
    final Color selectedTextColor =
        widget.useDarkTheme ? darkSelectedTextColor : lightSelectedTextColor;
    final Color unselectedTextColor = widget.useDarkTheme
        ? darkUnselectedTextColor
        : lightUnselectedTextColor;

    // Get text color from VColor or use default
    final Color defaultTextColor =
        _isPressed ? selectedTextColor : unselectedTextColor;
    final Color finalTextColor =
        colorFromVColor(widget.textColor, defaultColor: defaultTextColor);

    // Create TextStyle from VFont
    final textStyle = FontUtils.textStyleFromVFont(
      widget.vFont,
      context,
      color: finalTextColor,
    );

    final Color iconColor =
        _isPressed ? selectedTextColor : unselectedTextColor;

    final bool isIconOnly =
        widget.image != null && (widget.text == null || widget.text!.isEmpty);
    const double iconSize = 24.0;

    if (isIconOnly) {
      return MouseRegion(
          onEnter: (_) => widget.onMouseEnter?.call(),
          onExit: (_) => widget.onMouseExit?.call(),
          child: Focus(
            onFocusChange: (hasFocus) {
              if (hasFocus) {
                widget.onFocusIn?.call();
              } else {
                widget.onFocusOut?.call();
              }
            },
            child: Material(
              child: InkWell(
                onTap: widget.enabled
                    ? () {
                        setState(() {
                          _isPressed = true;
                        });
                        widget.onPressed();
                        Future.delayed(const Duration(milliseconds: 200), () {
                          if (mounted) {
                            setState(() {
                              _isPressed = false;
                            });
                          }
                        });
                      }
                    : null,
                borderRadius: BorderRadius.circular(iconSize),
                child: Container(
                  width: iconSize + 8,
                  height: iconSize + 8,
                  decoration: _isPressed
                      ? BoxDecoration(
                          color: selectedColor.withOpacity(0.2),
                          shape: BoxShape.circle,
                        )
                      : null,
                  child: Center(
                    child: !iconMap.containsKey(widget.image)
                        ? (widget.image!.toLowerCase().endsWith('.svg')
                            ? SvgPicture.file(
                                File(widget.image!),
                              )
                            : Image.file(
                                File(widget.image!),
                                width: iconSize,
                                height: iconSize,
                              ))
                        : Icon(
                            getIconByName(widget.image!),
                            size: iconSize,
                            color: iconColor,
                          ),
                  ),
                ),
              ),
            ),
          ));
    }

    return MouseRegion(
      onEnter: (_) => widget.onMouseEnter?.call(),
      onExit: (_) => widget.onMouseExit?.call(),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            widget.onFocusIn?.call();
          } else {
            widget.onFocusOut?.call();
          }
        },
        child: MaterialButton(
          onPressed: widget.enabled
              ? () {
                  setState(() {
                    _isPressed = true;
                  });
                  widget.onPressed();
                  Future.delayed(const Duration(milliseconds: 200), () {
                    if (mounted) {
                      setState(() {
                        _isPressed = false;
                      });
                    }
                  });
                }
              : null,
          elevation: 0,
          focusElevation: 0,
          hoverElevation: 0,
          highlightElevation: 0,
          disabledElevation: 0,
          height: widget.height,
          minWidth: widget.minWidth,
          padding: EdgeInsets.zero,
          color: _isPressed
              ? (widget.enabled
                  ? selectedColor
                  : selectedColor.withOpacity(0.5))
              : unselectedColor,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(widget.borderRadius),
            side: BorderSide.none,
          ),
          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
          child: Row(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              if (widget.image != null) ...[
                !iconMap.containsKey(widget.image)
                    ? (widget.image!.toLowerCase().endsWith('.svg')
                        ? SvgPicture.file(
                            File(widget.image!),
                          )
                        : Image.file(
                            File(widget.image!),
                            width: iconSize - 8,
                            height: iconSize - 8,
                          ))
                    : Icon(
                        getIconByName(widget.image!),
                        size: iconSize - 8,
                        color: iconColor,
                      ),
                const SizedBox(width: 6),
              ],
              Transform.translate(
                offset: const Offset(0, -1.0),
                child: Text(
                  stripAccelerators(widget.text ?? ""),
                  style: textStyle,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// RADIO BUTTON
class MaterialRadioButton extends StatelessWidget {
  final String? text;
  final bool checked;
  final ValueChanged<bool>? onChanged;
  final bool useDarkTheme;
  final Color? backgroundColor;
  final VFont? vFont;
  final VColor? textColor;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const MaterialRadioButton({
    Key? key,
    this.text,
    required this.checked,
    this.onChanged,
    this.useDarkTheme = false,
    this.backgroundColor,
    this.vFont,
    this.textColor,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color activeColor = const Color(0xFF6366F1);
    final Color inactiveColor =
        useDarkTheme ? Colors.white70 : const Color(0xFF757575);

    // Get text color from VColor or use default
    final Color defaultTextColor = useDarkTheme ? Colors.white : Colors.black87;
    final Color finalTextColor =
        colorFromVColor(textColor, defaultColor: defaultTextColor);

    // Create TextStyle from VFont
    final textStyle = FontUtils.textStyleFromVFont(
      vFont,
      context,
      color: finalTextColor,
    );

    Widget child = Material(
        child: MouseRegion(
      onEnter: (_) => onMouseEnter?.call(),
      onExit: (_) => onMouseExit?.call(),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            onFocusIn?.call();
          } else {
            onFocusOut?.call();
          }
        },
        child: InkWell(
          onTap: onChanged != null ? () => onChanged!(!checked) : null,
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 4.0, horizontal: 0.0),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Container(
                  width: 20,
                  height: 20,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: checked ? activeColor : inactiveColor,
                      width: 2,
                    ),
                  ),
                  child: Center(
                    child: checked
                        ? Container(
                            width: 10,
                            height: 10,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              color: activeColor,
                            ),
                          )
                        : null,
                  ),
                ),
                const SizedBox(width: 8),
                Flexible(
                  child: Text(
                    stripAccelerators(text ?? ''),
                    style: textStyle,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    ));

    if (backgroundColor != null) {
      return Container(
        color: backgroundColor,
        child: child,
      );
    }

    return child;
  }
}

// CHECKBOX BUTTON
class MaterialCheckBox extends StatelessWidget {
  final String? text;
  final bool checked;
  final ValueChanged<bool>? onChanged;
  final bool useDarkTheme;
  final Color? backgroundColor;
  final VFont? vFont;
  final VColor? textColor;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const MaterialCheckBox({
    Key? key,
    this.text,
    required this.checked,
    this.onChanged,
    this.useDarkTheme = false,
    this.backgroundColor,
    this.vFont,
    this.textColor,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color activeColor = const Color(0xFF6366F1);
    final Color inactiveColor =
        useDarkTheme ? Colors.white70 : const Color(0xFF757575);

    // Get text color from VColor or use default
    final Color defaultTextColor = useDarkTheme ? Colors.white : Colors.black87;
    final Color finalTextColor =
        colorFromVColor(textColor, defaultColor: defaultTextColor);

    // Create TextStyle from VFont
    final textStyle = FontUtils.textStyleFromVFont(
      vFont,
      context,
      color: finalTextColor,
    );

    Widget child = MouseRegion(
        onEnter: (_) => onMouseEnter?.call(),
        onExit: (_) => onMouseExit?.call(),
        child: Focus(
          onFocusChange: (hasFocus) {
            if (hasFocus) {
              onFocusIn?.call();
            } else {
              onFocusOut?.call();
            }
          },
          child: Material(
            color: backgroundColor ?? Colors.transparent,
            child: InkWell(
              onTap: onChanged != null ? () => onChanged!(!checked) : null,
              child: Padding(
                padding:
                    const EdgeInsets.symmetric(vertical: 4.0, horizontal: 0.0),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Container(
                      width: 18,
                      height: 18,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(3),
                        border: Border.all(
                          color: checked ? activeColor : inactiveColor,
                          width: 2,
                        ),
                        color: checked ? activeColor : null,
                      ),
                      child: checked
                          ? const Icon(
                              Icons.check,
                              size: 14,
                              color: Colors.white,
                            )
                          : null,
                    ),
                    const SizedBox(width: 8),
                    Flexible(
                      child: Text(
                        stripAccelerators(text ?? ''),
                        style: textStyle,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ));

    return child;
  }
}
