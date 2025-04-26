import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'dart:io';
import 'icons_map.dart';

// DROP DOWN BUTTON
class MaterialDropdownButton extends StatelessWidget {
  final String text;
  final VoidCallback? onPressed;
  final bool enabled;
  final double borderRadius;
  final double height;
  final double minWidth;
  final bool useDarkTheme;

  const MaterialDropdownButton({
    Key? key,
    required this.text,
    this.onPressed,
    this.enabled = true,
    this.borderRadius = 5.0,
    this.height = 30.0,
    this.minWidth = 70.0,
    this.useDarkTheme = false,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {

    const Color darkButtonColor = Color(0xFF6366F1);
    const Color darkTextColor = Color(0xFFFFFFFF);

    const Color lightButtonColor = Color(0xFF6366F1);
    const Color lightTextColor = Color(0xFF595858);

    final Color buttonColor = useDarkTheme ? darkButtonColor : lightButtonColor;
    final Color textColor = useDarkTheme ? darkTextColor : lightTextColor;

    return MaterialButton(
      onPressed: enabled ? onPressed : null,
      elevation: 0,
      focusElevation: 0,
      hoverElevation: 0,
      highlightElevation: 0,
      disabledElevation: 0,
      height: height,
      minWidth: minWidth,
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      color: enabled ? buttonColor : buttonColor.withOpacity(0.5),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(borderRadius),
      ),
      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            text,
            style: TextStyle(
              color: textColor.withOpacity(enabled ? 1.0 : 0.5),
              fontWeight: FontWeight.w500,
            ),
          ),
          Icon(
            Icons.arrow_drop_down,
            color: textColor.withOpacity(enabled ? 1.0 : 0.5),
            size: 20,
          ),
        ],
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
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    print('');
    final Color darkSelectedColor = const Color(0xFF6366F1);
    final Color darkUnselectedColor = const Color(0xFF4E4D4D);
    final Color darkSelectedTextColor = const Color(0xFFFFFFFF);
    final Color darkUnselectedTextColor = const Color(0xFFFFFFFF);

    final Color lightSelectedColor = const Color(0xFF6366F1);
    final Color lightUnselectedColor = const Color(0xFFC8C7C7);
    final Color lightSelectedTextColor = const Color(0xFFFFFFFF);
    final Color lightUnselectedTextColor = const Color(0xFF595858);

    final Color selectedColor = useDarkTheme ? darkSelectedColor : lightSelectedColor;
    final Color unselectedColor = useDarkTheme ? darkUnselectedColor : lightUnselectedColor;
    final Color selectedTextColor = useDarkTheme ? darkSelectedTextColor : lightSelectedTextColor;
    final Color unselectedTextColor = useDarkTheme ? darkUnselectedTextColor : lightUnselectedTextColor;


    final Color iconColor = isSelected ? selectedTextColor : unselectedTextColor;


    final bool isIconOnly = image != null && (text == null || text!.isEmpty);


    const double iconSize = 24.0;


    if (isIconOnly) {
      return InkWell(
        onTap: enabled ? onPressed : null,
        borderRadius: BorderRadius.circular(iconSize),
        child: Container(
            width: iconSize + 8,
            height: iconSize + 8,
            decoration: isSelected ? BoxDecoration(
              color: selectedColor.withOpacity(0.2),
              shape: BoxShape.circle,
            ) : null,
            child: Center(
              child: !materialIconMap.containsKey(image)
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
                getMaterialIconByName(image!),
                size: iconSize,
                color: iconColor,
              ),
            )),);
    }


    return MaterialButton(
      onPressed: enabled ? onPressed : null,
      elevation: 0,
      focusElevation: 0,
      hoverElevation: 0,
      highlightElevation: 0,
      disabledElevation: 0,
      height: height,
      minWidth: minWidth,
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
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
            !materialIconMap.containsKey(image)
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
              getMaterialIconByName(image!),
              size: iconSize - 8,
              color: iconColor,
            ),
            const SizedBox(width: 6),
          ],
          // Display text
          Text(
            text ?? "",
            style: TextStyle(
              color: isSelected ? selectedTextColor : unselectedTextColor,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
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
    final Color darkSelectedTextColor = const Color(0xFF6366F1);
    final Color darkUnselectedTextColor = const Color(0xFF6366F1);

    final Color lightSelectedColor = const Color(0xFF6366F1);
    final Color lightUnselectedColor = const Color(0xFF6366F1);
    final Color lightSelectedTextColor = const Color(0xFF6366F1);
    final Color lightUnselectedTextColor = const Color(0xFF6366F1);

    final Color selectedColor = widget.useDarkTheme ? darkSelectedColor : lightSelectedColor;
    final Color unselectedColor = widget.useDarkTheme ? darkUnselectedColor : lightUnselectedColor;
    final Color selectedTextColor = widget.useDarkTheme ? darkSelectedTextColor : lightSelectedTextColor;
    final Color unselectedTextColor = widget.useDarkTheme ? darkUnselectedTextColor : lightUnselectedTextColor;

    final Color iconColor = _isPressed ? selectedTextColor : unselectedTextColor;

    final bool isIconOnly = widget.image != null && (widget.text == null || widget.text!.isEmpty);
    const double iconSize = 24.0;

    // Para botones de solo ícono
    if (isIconOnly) {
      return InkWell(
        onTap: widget.enabled ? () {
          setState(() {
            _isPressed = true;
          });
          widget.onPressed();
          // Resetear el estado después de un breve delay para mostrar el efecto visual
          Future.delayed(const Duration(milliseconds: 200), () {
            if (mounted) {
              setState(() {
                _isPressed = false;
              });
            }
          });
        } : null,
        borderRadius: BorderRadius.circular(iconSize),
        child: Container(
          width: iconSize + 8,
          height: iconSize + 8,
          decoration: _isPressed ? BoxDecoration(
            color: selectedColor.withOpacity(0.2),
            shape: BoxShape.circle,
          ) : null,
          child: Center(
            child: !materialIconMap.containsKey(widget.image)
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
              getMaterialIconByName(widget.image!),
              size: iconSize,
              color: iconColor,
            ),
          ),
        ),
      );
    }

    return MaterialButton(
      onPressed: widget.enabled ? () {
        setState(() {
          _isPressed = true;
        });
        widget.onPressed();
        // Resetear el estado después de un breve delay
        Future.delayed(const Duration(milliseconds: 200), () {
          if (mounted) {
            setState(() {
              _isPressed = false;
            });
          }
        });
      } : null,
      elevation: 0,
      focusElevation: 0,
      hoverElevation: 0,
      highlightElevation: 0,
      disabledElevation: 0,
      height: widget.height,
      minWidth: widget.minWidth,
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      color: _isPressed
          ? (widget.enabled ? selectedColor : selectedColor.withOpacity(0.5))
          : unselectedColor,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(widget.borderRadius),
      ),
      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          if (widget.image != null) ...[
            !materialIconMap.containsKey(widget.image)
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
              getMaterialIconByName(widget.image!),
              size: iconSize - 8,
              color: iconColor,
            ),
            const SizedBox(width: 6),
          ],
          Text(
            widget.text ?? "",
            style: TextStyle(
              color: _isPressed ? selectedTextColor : unselectedTextColor,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }
}