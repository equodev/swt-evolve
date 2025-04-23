import 'package:flutter/material.dart';
import 'package:swtflutter/src/impl/scrollable_impl.dart';

import '../swt/text.dart';

class TextImpl<T extends TextSwt<V>, V extends TextValue>
    extends ScrollableImpl<T, V> {
  late TextEditingController _controller;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
  }

  @override
  void extraSetState() {
    _controller.text = state.text ?? "";
  }

  @override
  Widget build(BuildContext context) {
    return TextField(controller: _controller);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}
