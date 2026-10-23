/// Reports a control's own focus to Java once per change, however many focus notifications the
/// client's nodes raise for it (a descendant focusing, moving between the control's own nodes).
class FocusReport {
  bool _reported = false;

  /// Whether the last report was a FocusIn.
  bool get reported => _reported;

  /// Reports [own] when it differs from the last report: [focusIn] when the control took the
  /// focus, [focusOut] when it lost it.
  void update(bool own, {required void Function() focusIn, required void Function() focusOut}) {
    if (own == _reported) return;
    _reported = own;
    if (own) {
      focusIn();
    } else {
      focusOut();
    }
  }
}
