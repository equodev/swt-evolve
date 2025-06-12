import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'dart:io';
import 'icons_map.dart';

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