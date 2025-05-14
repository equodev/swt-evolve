import 'package:flutter/material.dart';

class StyledList extends StatefulWidget {
  final List<String> items;
  final List<String> selection;
  final bool enabled;
  final bool useDarkTheme;
  final bool isMultiSelect;
  final Function(List<String>) onSelectionChanged;
  final Function(String) onItemDoubleClick;
  final VoidCallback onMouseEnter;
  final VoidCallback onMouseExit;
  final VoidCallback onFocusIn;
  final VoidCallback onFocusOut;

  const StyledList({
    Key? key,
    required this.items,
    required this.selection,
    required this.enabled,
    required this.useDarkTheme,
    required this.isMultiSelect,
    required this.onSelectionChanged,
    required this.onItemDoubleClick,
    required this.onMouseEnter,
    required this.onMouseExit,
    required this.onFocusIn,
    required this.onFocusOut,
  }) : super(key: key);

  @override
  _StyledListState createState() => _StyledListState();
}

class _StyledListState extends State<StyledList> {
  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => widget.onMouseEnter(),
      onExit: (_) => widget.onMouseExit(),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            widget.onFocusIn();
          } else {
            widget.onFocusOut();
          }
        },
        child: Container(
          decoration: BoxDecoration(
            color: widget.useDarkTheme ? const Color(0xFF1E1E1E) : Colors.white,
            border: Border.all(
              color: widget.useDarkTheme ? const Color(0xFF3C3C3C) : const Color(0xFFD4D4D4),
              width: 1,
            ),
          ),
          child: ListView.builder(
            itemCount: widget.items.length,
            itemExtent: 24,
            itemBuilder: (context, index) {
              final item = widget.items[index];
              final isSelected = widget.selection.contains(item);
              return ListItem(
                item: item,
                isSelected: isSelected,
                enabled: widget.enabled,
                useDarkTheme: widget.useDarkTheme,
                onTap: () => _handleItemTap(item),
                onDoubleTap: () => widget.onItemDoubleClick(item),
              );
            },
          ),
        ),
      ),
    );
  }

  void _handleItemTap(String item) {
    if (!widget.enabled) return;

    setState(() {
      if (widget.isMultiSelect) {
        if (widget.selection.contains(item)) {
          widget.selection.remove(item);
        } else {
          widget.selection.add(item);
        }
      } else {
        widget.selection.clear();
        widget.selection.add(item);
      }
      widget.onSelectionChanged(widget.selection);
    });
  }
}

class ListItem extends StatelessWidget {
  final String item;
  final bool isSelected;
  final bool enabled;
  final bool useDarkTheme;
  final VoidCallback onTap;
  final VoidCallback onDoubleTap;

  const ListItem({
    Key? key,
    required this.item,
    required this.isSelected,
    required this.enabled,
    required this.useDarkTheme,
    required this.onTap,
    required this.onDoubleTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final backgroundColor = isSelected
        ? (useDarkTheme ? const Color(0xFF094771) : const Color(0xFFE5F3FF))
        : (useDarkTheme ? const Color(0xFF1E1E1E) : Colors.white);

    final textColor = enabled
        ? (useDarkTheme ? Colors.white : const Color(0xFF1F1F1F))
        : (useDarkTheme ? const Color(0xFF6D6D6D) : const Color(0xFFA7A7A7));

    return GestureDetector(
      onTap: onTap,
      onDoubleTap: onDoubleTap,
      child: Container(
        height: 24,
        color: backgroundColor,
        padding: const EdgeInsets.symmetric(horizontal: 8),
        alignment: Alignment.centerLeft,
        child: Text(
          item,
          style: TextStyle(
            color: textColor,
            fontSize: 14,
            fontWeight: FontWeight.normal,
          ),
          overflow: TextOverflow.ellipsis,
        ),
      ),
    );
  }
}