import 'package:flutter/widgets.dart';
import '../gen/browser.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';

class BrowserImpl<T extends BrowserSwt, V extends VBrowser>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    String content = state.url ?? state.text ?? "blank";
    if (content.isEmpty) {
      content = "blank";
    }
    return Text("I'm a Browser for: $content");
  }
}
