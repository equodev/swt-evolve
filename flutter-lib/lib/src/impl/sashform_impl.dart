import 'dart:core';

import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/styles.dart';
import 'package:swtflutter/src/swt/composite.dart';
import 'package:swtflutter/src/swt/swt.dart';
import 'package:swtflutter/src/widgets.dart';

import '../swt/sashform.dart';
import '../swt/widget.dart';

import '../impl/composite_impl.dart';

class SashFormImpl<T extends SashFormSwt, V extends SashFormValue>
    extends CompositeImpl<T, V> {
  final double mouseLineLength = 4.0;
  final double minSize = 10.0;
  List<GlobalKey> childKeys = [];
  List<double> childWidth = [];
  List<double> childHeight = [];

  double _paneWidth0 = -1;
  double _paneWidth1 = -1;
  double _paneHeight0 = -1;
  double _paneHeight1 = -1;
  double parentWidth = -1;
  double parentHeight = -1;
  final GlobalKey _childKey0 = GlobalKey();
  final GlobalKey _childKey1 = GlobalKey();

  @override
  Widget build(BuildContext context) {
    final style = StyleBits(state.style);
    var children = getChildrenAsWidget();
    var length = 2; // should be children.length

    return LayoutBuilder(builder: (builder, constraints) {
      var childContext0 = _childKey0.currentContext;

      if (parentWidth != constraints.maxWidth) {
        parentWidth = constraints.maxWidth;

        if (childContext0 != null) {
          _paneWidth0 = _paneWidth0.clamp(
              minSize, parentWidth - mouseLineLength - minSize);
          _paneWidth1 = parentWidth - _paneWidth0 - mouseLineLength - 1;
        } else {
          _paneWidth0 = constraints.maxWidth / length - mouseLineLength;
          _paneWidth1 = constraints.maxWidth / length - 1;
        }
      }
      if (parentHeight != constraints.maxHeight) {
        parentHeight = constraints.maxHeight;

        if (childContext0 != null) {
          _paneHeight0 = _paneHeight0.clamp(
              minSize, parentHeight - mouseLineLength - minSize);
          _paneHeight1 = parentHeight - _paneHeight0 - mouseLineLength - 1;
        } else {
          _paneHeight0 = constraints.maxHeight / length - mouseLineLength;
          _paneHeight1 = constraints.maxHeight / length - 1;
        }
      }
      if (style.has(SWT.VERTICAL)) {
        return Column(
          children: [
            SizedBox(
              key: _childKey0,
              height: _paneHeight0,
              child: children[0],
            ),
            GestureDetector(
              behavior: HitTestBehavior.opaque,
              onVerticalDragUpdate: (details) {
                final RenderBox topRenderBox =
                _childKey0.currentContext!.findRenderObject() as RenderBox;
                final RenderBox bottomRenderBox =
                _childKey1.currentContext!.findRenderObject() as RenderBox;

                final double topHeight = topRenderBox.size.height;
                final double bottomHeight = bottomRenderBox.size.height;
                var combinedHeight = topHeight + bottomHeight;

                setState(() {
                  _paneHeight0 += details.delta.dy;
                  _paneHeight0 = _paneHeight0.clamp(
                      minSize, combinedHeight - minSize - mouseLineLength);
                  _paneHeight1 = combinedHeight - _paneHeight0;
                });
              },
              child: MouseRegion(
                cursor: SystemMouseCursors.resizeRow,
                child: Container(
                  height: mouseLineLength,
                ),
              ),
            ),
            SizedBox(
              key: _childKey1,
              height: _paneHeight1,
              child: children[1],
            ),
          ],
        );
      }
      return Row(
        children: [
          SizedBox(
            key: _childKey0,
            width: _paneWidth0,
            child: children[0],
          ),
          GestureDetector(
            behavior: HitTestBehavior.opaque,
            onHorizontalDragUpdate: (details) {
              final RenderBox leftRenderBox =
              _childKey0.currentContext!.findRenderObject() as RenderBox;
              final RenderBox rightRenderBox =
              _childKey1.currentContext!.findRenderObject() as RenderBox;

              final double leftWidth = leftRenderBox.size.width;
              final double rightWidth = rightRenderBox.size.width;
              var combinedWidth = leftWidth + rightWidth;

              setState(() {
                _paneWidth0 += details.delta.dx;
                _paneWidth0 = _paneWidth0.clamp(
                    minSize, combinedWidth - minSize - mouseLineLength);
                _paneWidth1 = combinedWidth - _paneWidth0;
              });
            },
            child: MouseRegion(
              cursor: SystemMouseCursors.resizeColumn,
              child: Container(
                width: mouseLineLength,
              ),
            ),
          ),
          SizedBox(
            key: _childKey1,
            width: _paneWidth1,
            child: children[1],
          ),
        ],
      );
    });
  }

  List<Widget> getChildrenAsWidget() {
    if (state.children == null) {
      return [];
    }
    return state.children!
        .whereType<WidgetValue>()
        .map(mapWidgetFromValue)
        .map((w) => SizedBox(child: w))
        .toList();
  }
}
