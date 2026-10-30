import 'package:flutter/widgets.dart';

import '../../gen/event.dart';

/// Reports the caret and the selection the user leaves in a text field to Java, as a Verify that
/// proposes no edit: the caret (the selection's moving end) in `start` and `end`, the anchor in
/// `index`. Java answers getCaretPosition() and getSelection() from it.
mixin ClientCaretReport {
  bool get mounted;

  /// The caret last reported to Java or applied from it: the widget's `caretPosition`.
  int? get reportedCaret;
  set reportedCaret(int? caret);

  void sendCaretReport(VEvent event);

  /// True while the controller is being set from a value Java pushed: the caret it lands on is
  /// Java's own, so it is recorded but not reported back as a move.
  bool _applyingJavaValue = false;

  /// The selection anchor last reported to Java, alongside [reportedCaret].
  int? _reportedAnchor;

  void fromJava(void Function() apply) {
    _applyingJavaValue = true;
    try {
      apply();
    } finally {
      _applyingJavaValue = false;
    }
  }

  /// Reports [controller]'s caret and anchor when they moved. [editPending] holds the report back
  /// for a widget whose edit reaches Java later and resets its selection there; the widget reports
  /// again once it has sent that edit.
  void reportCaret(TextEditingController controller, {bool editPending = false}) =>
      reportSelection(controller.selection, editPending: editPending);

  /// [reportCaret] for a field whose controller is not a [TextEditingController].
  void reportSelection(TextSelection selection, {bool editPending = false}) {
    if (!mounted || editPending) return;
    final caret = selection.extentOffset;
    final anchor = selection.baseOffset;
    if (caret < 0 || anchor < 0) return;
    if (reportedCaret == caret && _reportedAnchor == anchor) return;
    reportedCaret = caret;
    _reportedAnchor = anchor;
    if (_applyingJavaValue) return;
    sendCaretReport(VEvent()
      ..start = caret
      ..end = caret
      ..index = anchor
      ..text = "");
  }
}
