import 'package:flutter/material.dart';

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
    final Color backgroundColor =
        useDarkTheme ? Color(0xFF1E1E1E) : Colors.white;
    final Color textColor = useDarkTheme ? Colors.white : Color(0xFF595858);
    final Color borderColor =
        useDarkTheme ? Color(0xFF3C3C3C) : Color(0xFFD1D5DB);

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
          height: 30,
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
                  child: Text(item, style: TextStyle(color: textColor)),
                );
              }).toList(),
              onChanged: onChanged,
              style: TextStyle(color: textColor),
              dropdownColor: backgroundColor,
              icon: Icon(Icons.arrow_drop_down, color: textColor),
            ),
          ),
        ),
      ),
    );
  }
}

class StyledSimpleCombo extends StatelessWidget {
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final String Function(String)? onTextSubmitted;
  final bool useDarkTheme;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledSimpleCombo({
    Key? key,
    required this.items,
    this.value,
    this.onChanged,
    this.onTextChanged,
    this.onTextSubmitted,
    required this.useDarkTheme,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor =
        useDarkTheme ? Color(0xFF1E1E1E) : Colors.white;
    final Color textColor = useDarkTheme ? Colors.white : Color(0xFF595858);
    final Color borderColor =
        useDarkTheme ? Color(0xFF3C3C3C) : Color(0xFFD1D5DB);

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
          height: 30,
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: BorderRadius.circular(4),
            border: Border.all(color: borderColor),
          ),
          child: Row(
            children: [
              Expanded(
                child: TextField(
                  controller: TextEditingController(text: value),
                  style: TextStyle(color: textColor),
                  decoration: InputDecoration(
                    border: InputBorder.none,
                    contentPadding: EdgeInsets.symmetric(horizontal: 8),
                  ),
                  onChanged: onTextChanged,
                  onSubmitted: onTextSubmitted,
                ),
              ),
              PopupMenuButton<String>(
                icon: Icon(Icons.arrow_drop_down, color: textColor),
                itemBuilder: (context) {
                  return items.map((String item) {
                    return PopupMenuItem<String>(
                      value: item,
                      child: Text(item, style: TextStyle(color: textColor)),
                    );
                  }).toList();
                },
                onSelected: onChanged,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class StyledEditableCombo extends StatelessWidget {
  final List<String> items;
  final String? value;
  final ValueChanged<String?>? onChanged;
  final ValueChanged<String>? onTextChanged;
  final String Function(String)? onTextSubmitted;
  final bool useDarkTheme;
  final VoidCallback? onMouseEnter;
  final VoidCallback? onMouseExit;
  final VoidCallback? onFocusIn;
  final VoidCallback? onFocusOut;

  const StyledEditableCombo({
    Key? key,
    required this.items,
    this.value,
    this.onChanged,
    this.onTextChanged,
    this.onTextSubmitted,
    required this.useDarkTheme,
    this.onMouseEnter,
    this.onMouseExit,
    this.onFocusIn,
    this.onFocusOut,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor =
        useDarkTheme ? Color(0xFF1E1E1E) : Colors.white;
    final Color textColor = useDarkTheme ? Colors.white : Color(0xFF595858);
    final Color borderColor =
        useDarkTheme ? Color(0xFF3C3C3C) : Color(0xFFD1D5DB);

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
          height: 30,
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: BorderRadius.circular(4),
            border: Border.all(color: borderColor),
          ),
          child: Row(
            children: [
              Expanded(
                child: TextField(
                  controller: TextEditingController(text: value),
                  style: TextStyle(color: textColor),
                  decoration: InputDecoration(
                    border: InputBorder.none,
                    contentPadding: EdgeInsets.symmetric(horizontal: 8),
                  ),
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
                      child: Text(item, style: TextStyle(color: textColor)),
                    );
                  }).toList(),
                  onChanged: onChanged,
                  style: TextStyle(color: textColor),
                  dropdownColor: backgroundColor,
                  icon: Icon(Icons.arrow_drop_down, color: textColor),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
