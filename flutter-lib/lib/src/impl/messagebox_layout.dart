import '../gen/swt.dart';

/// How a message box lays out a set of buttons: [order] left to right, the [defaultId] Return
/// presses and the [escapeId] Escape presses, if any.
typedef MessageBoxLayout = ({List<int> order, int defaultId, int? escapeId});

/// The same layout on every OS.
///
/// The order is Evolve's own: Cancel, OK, No, Yes, Ignore, Retry, Abort, left to right, so the
/// affirmative button sits on the right, where Cocoa and GTK put it. The default button is the
/// one Cocoa and Win32 make the default. Escape presses Cancel where there is one, and nothing
/// in a box with a lone OK, as on Cocoa and GTK.
const Map<int, MessageBoxLayout> _layouts = {
  SWT.OK: (order: [SWT.OK], defaultId: SWT.OK, escapeId: null),
  SWT.CANCEL: (order: [SWT.CANCEL], defaultId: SWT.CANCEL, escapeId: SWT.CANCEL),
  SWT.YES: (order: [SWT.YES], defaultId: SWT.YES, escapeId: null),
  SWT.NO: (order: [SWT.NO], defaultId: SWT.NO, escapeId: null),
  SWT.OK | SWT.CANCEL: (order: [SWT.CANCEL, SWT.OK], defaultId: SWT.OK, escapeId: SWT.CANCEL),
  SWT.YES | SWT.NO: (order: [SWT.NO, SWT.YES], defaultId: SWT.YES, escapeId: null),
  SWT.YES | SWT.NO | SWT.CANCEL: (order: [SWT.CANCEL, SWT.NO, SWT.YES], defaultId: SWT.YES, escapeId: SWT.CANCEL),
  SWT.RETRY | SWT.CANCEL: (order: [SWT.CANCEL, SWT.RETRY], defaultId: SWT.RETRY, escapeId: SWT.CANCEL),
  SWT.ABORT | SWT.RETRY | SWT.IGNORE: (order: [SWT.IGNORE, SWT.RETRY, SWT.ABORT], defaultId: SWT.ABORT, escapeId: null),
};

const int _buttonMask = SWT.OK | SWT.CANCEL | SWT.YES | SWT.NO | SWT.ABORT | SWT.RETRY | SWT.IGNORE;

/// The layout of the buttons of a box of [style]. A style without a valid set of buttons gets an
/// OK button, as SWT gives it.
MessageBoxLayout messageBoxLayout(int style) => _layouts[style & _buttonMask] ?? _layouts[SWT.OK]!;
