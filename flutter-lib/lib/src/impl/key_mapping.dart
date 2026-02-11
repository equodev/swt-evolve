import 'package:flutter/services.dart';
import '../gen/event.dart';
import '../gen/swt.dart';

/// Maps a Flutter [RawKeyEvent] to a [VEvent] with SWT-compatible
/// keyCode, character and stateMask values.
VEvent mapKeyEventToSwt(RawKeyEvent event) {
  int keyCode = 0;
  int character = 0;

  final key = event.logicalKey;
  if (key == LogicalKeyboardKey.enter) {
    keyCode = SWT.CR;
    character = SWT.CR;
  } else if (key == LogicalKeyboardKey.escape) {
    keyCode = SWT.ESC;
    character = SWT.ESC;
  } else if (key == LogicalKeyboardKey.backspace) {
    keyCode = SWT.BS;
    character = SWT.BS;
  } else if (key == LogicalKeyboardKey.delete) {
    keyCode = SWT.DEL;
    character = SWT.DEL;
  } else if (key == LogicalKeyboardKey.tab) {
    keyCode = SWT.TAB;
    character = SWT.TAB;
  } else if (key == LogicalKeyboardKey.arrowUp) {
    keyCode = SWT.ARROW_UP;
  } else if (key == LogicalKeyboardKey.arrowDown) {
    keyCode = SWT.ARROW_DOWN;
  } else if (key == LogicalKeyboardKey.arrowLeft) {
    keyCode = SWT.ARROW_LEFT;
  } else if (key == LogicalKeyboardKey.arrowRight) {
    keyCode = SWT.ARROW_RIGHT;
  } else if (key == LogicalKeyboardKey.home) {
    keyCode = SWT.HOME;
  } else if (key == LogicalKeyboardKey.end) {
    keyCode = SWT.END;
  } else if (event.character != null && event.character!.isNotEmpty) {
    character = event.character!.codeUnitAt(0);
    keyCode = character;
  }

  int stateMask = 0;
  if (event.data.isControlPressed) stateMask |= SWT.CTRL;
  if (event.data.isShiftPressed) stateMask |= SWT.SHIFT;
  if (event.data.isAltPressed) stateMask |= SWT.ALT;
  if (event.data.isMetaPressed) stateMask |= SWT.COMMAND;

  return VEvent()
    ..keyCode = keyCode
    ..character = character
    ..stateMask = stateMask;
}