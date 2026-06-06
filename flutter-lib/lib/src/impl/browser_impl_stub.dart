import 'package:flutter/widgets.dart';
import '../gen/browser.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';

class BrowserImpl<T extends BrowserSwt, V extends VBrowser>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) =>
      const ColoredBox(color: Color(0xFFFFFFFF));
}
