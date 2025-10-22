import 'package:flutter/material.dart';
import 'color_utils.dart';

class StyledDropdownButton extends StatelessWidget {
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final bool useDarkTheme;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledDropdownButton({
    Key? key,
    required this.items,
    this.value,
    this.onChanged,
    required this.useDarkTheme,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = getBackground();
    final Color textColor = getForeground();
    final Color borderColor = getBorderColor();
    final Color iconColor = getIconColor();

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
        child: Container(
          height: 32,
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: BorderRadius.circular(4),
            border: Border.all(color: borderColor),
          ),
          child: DropdownButtonHideUnderline(
            child: DropdownButton<String>(
              value: value,
              items: items.map((String item) {
                return DropdownMenuItem<String>(
                  value: item,
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    child: Text(item, style: TextStyle(color: textColor, fontSize: 12)),
                  ),
                );
              }).toList(),
              onChanged: onChanged,
              style: TextStyle(color: textColor, fontSize: 12),
              dropdownColor: backgroundColor,
              icon: Padding(
                padding: const EdgeInsets.only(right: 8),
                child: Icon(Icons.arrow_drop_down, color: iconColor, size: 20),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class StyledSimpleCombo extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final ValueChanged<String>? onTextSubmitted;
  final bool useDarkTheme;
  final int? textLimit;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledSimpleCombo({
    Key? key,
    required this.controller,
    this.focusNode,
    required this.items,
    this.value,
    this.onChanged,
    this.onTextChanged,
    this.onTextSubmitted,
    required this.useDarkTheme,
    this.textLimit,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = getBackground();
    final Color textColor = getForeground();
    final Color borderColor = getBorderColor();
    final Color iconColor = getIconColor();

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
        child: Container(
          height: 32,
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: BorderRadius.circular(4),
            border: Border.all(color: borderColor),
          ),
          child: Row(
            children: [
              Expanded(
                child: TextField(
                  controller: controller,
                  focusNode: focusNode,
                  style: TextStyle(color: textColor, fontSize: 12),
                  decoration: InputDecoration(
                    border: InputBorder.none,
                    contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    isDense: true,
                  ),
                  maxLength: textLimit,
                  onChanged: onTextChanged,
                  onSubmitted: onTextSubmitted,
                ),
              ),
              PopupMenuButton<String>(
                icon: Icon(Icons.arrow_drop_down, color: iconColor, size: 20),
                itemBuilder: (context) {
                  return items.map((String item) {
                    return PopupMenuItem<String>(
                      value: item,
                      child: Text(item, style: TextStyle(color: textColor, fontSize: 12)),
                    );
                  }).toList();
                },
                onSelected: onChanged,
                color: backgroundColor,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class StyledEditableCombo extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode? focusNode;
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final ValueChanged<String>? onTextSubmitted;
  final bool useDarkTheme;
  final int? textLimit;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledEditableCombo({
    Key? key,
    required this.controller,
    this.focusNode,
    required this.items,
    this.value,
    this.onChanged,
    this.onTextChanged,
    this.onTextSubmitted,
    required this.useDarkTheme,
    this.textLimit,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = getBackground();
    final Color textColor = getForeground();
    final Color borderColor = getBorderColor();
    final Color iconColor = getIconColor();

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
        child: Container(
          height: 32,
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: BorderRadius.circular(4),
            border: Border.all(color: borderColor),
          ),
          child: Row(
            children: [
              Expanded(
                child: TextField(
                  controller: controller,
                  focusNode: focusNode,
                  style: TextStyle(color: textColor, fontSize: 12),
                  decoration: InputDecoration(
                    border: InputBorder.none,
                    contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    isDense: true,
                    counterText: '',
                  ),
                  maxLength: textLimit,
                  onChanged: onTextChanged,
                  onSubmitted: onTextSubmitted,
                ),
              ),
              DropdownButtonHideUnderline(
                child: DropdownButton<String>(
                  value: value,
                  items: items.map((String item) {
                    return DropdownMenuItem<String>(
                      value: item,
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 12),
                        child: Text(item, style: TextStyle(color: textColor, fontSize: 12)),
                      ),
                    );
                  }).toList(),
                  onChanged: onChanged,
                  style: TextStyle(color: textColor, fontSize: 12),
                  dropdownColor: backgroundColor,
                  icon: Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: Icon(Icons.arrow_drop_down, color: iconColor, size: 20),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
