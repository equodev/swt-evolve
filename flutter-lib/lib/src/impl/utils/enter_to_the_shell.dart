import 'package:flutter/services.dart' show LogicalKeyboardKey;
import 'package:flutter/widgets.dart';

/// Enter is the shell's: Java runs the Return traversal and presses the default button. Flutter's
/// own Enter activation would press a focused button or tool item as well. Space still presses it,
/// as natively.
Widget leaveEnterToTheShell(Widget pressable) => Shortcuts(
      shortcuts: const <ShortcutActivator, Intent>{
        SingleActivator(LogicalKeyboardKey.enter): DoNothingAndStopPropagationIntent(),
        SingleActivator(LogicalKeyboardKey.numpadEnter): DoNothingAndStopPropagationIntent(),
      },
      child: pressable,
    );
