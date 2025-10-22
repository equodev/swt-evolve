import 'package:flutter/material.dart';
import '../gen/control.dart';
import '../gen/group.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import 'color_utils.dart';

class GroupImpl<T extends GroupSwt, V extends VGroup>
    extends CompositeImpl<T, V> {
  FocusNode? _focusNode;

  @override
  void initState() {
    super.initState();
    _focusNode = FocusNode();
    _focusNode!.addListener(_handleFocusChange);
  }

  @override
  Widget build(BuildContext context) {
    final children = state.children;
    final text = state.text ?? '';

    return MouseRegion(
      onEnter: (_) => widget.sendMouseTrackMouseEnter(state, null),
      onExit: (_) => widget.sendMouseTrackMouseExit(state, null),
      child: Focus(
        focusNode: _focusNode,
        child: _StyledGroup(
          text: text,
          width: state.bounds?.width.toDouble(),
          height: state.bounds?.height.toDouble(),
          children: children,
        ),
      ),
    );
  }

  void _handleFocusChange() {
    if (_focusNode!.hasFocus) {
      widget.sendFocusFocusIn(state, null);
    } else {
      widget.sendFocusFocusOut(state, null);
    }
  }

  @override
  void dispose() {
    _focusNode?.removeListener(_handleFocusChange);
    _focusNode?.dispose();
    super.dispose();
  }
}

class _StyledGroup extends StatelessWidget {
  final String text;
  final double? width;
  final double? height;
  final List<VControl>? children;

  const _StyledGroup({
    Key? key,
    required this.text,
    this.width,
    this.height,
    this.children,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final borderColor = getBorderColorFocused();
    final foregroundColor = getForeground();

    // Build children widgets with horizontal layout
    List<Widget> spacedChildren = [];
    if (children != null && children!.isNotEmpty) {
      for (int i = 0; i < children!.length; i++) {
        spacedChildren.add(mapWidgetFromValue(children![i] as VWidget));
        if (i < children!.length - 1) {
          spacedChildren.add(const SizedBox(width: 8));
        }
      }
    }

    return SizedBox(
      width: width,
      height: height,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          // Title
          if (text.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(bottom: 4),
              child: Text(
                text,
                style: TextStyle(
                  color: foregroundColor,
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  letterSpacing: 0.1,
                ),
              ),
            ),
          // Border container
          Expanded(
            child: Stack(
              clipBehavior: Clip.none,
              children: [
                Container(
                  decoration: BoxDecoration(
                    border: Border.all(color: borderColor, width: 1.5),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.start,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: spacedChildren,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
