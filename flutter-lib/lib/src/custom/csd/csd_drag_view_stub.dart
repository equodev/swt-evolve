import 'package:flutter/widgets.dart';

/// Off-web there is no DOM/host window to drag, so the drag surface is an inert box.
class CsdDragView extends StatelessWidget {
  const CsdDragView({super.key});

  @override
  Widget build(BuildContext context) => const SizedBox.expand();
}
