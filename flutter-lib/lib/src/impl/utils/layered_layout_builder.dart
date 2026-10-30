import 'package:flutter/widgets.dart';

/// A [LayoutBuilder] whose own relayouts repaint nothing but itself.
///
/// A LayoutBuilder lays itself out again whenever any widget below it is marked dirty between two
/// frames -- a hover, a selection, an answer from Java -- and laying out repaints it. Painting
/// starts from the nearest layer above, so without one close by that is the whole shell, and it
/// reaches down to the nearest layers below, through everything the builder built. A layer on each
/// side keeps it to the builder, while what really changed below still repaints in its own layer.
class LayeredLayoutBuilder extends StatelessWidget {
  const LayeredLayoutBuilder({super.key, required this.builder});

  final Widget Function(BuildContext context, BoxConstraints constraints) builder;

  @override
  Widget build(BuildContext context) => RepaintBoundary(
        child: LayoutBuilder(
          builder: (context, constraints) => RepaintBoundary(child: builder(context, constraints)),
        ),
      );
}
