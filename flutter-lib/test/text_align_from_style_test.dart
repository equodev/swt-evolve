// A style can carry more than one alignment bit: setAlignment keeps the ones it is given. Java answers
// getAlignment() for such a style LEFT first, then CENTER, then RIGHT, and the text is drawn by the same
// order, so a widget is aligned where the application is told it is.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart';

// An alignment no style bit maps to, so a bit that is not read shows up as this one.
const _unread = TextAlign.justify;

void main() {
  test('a single alignment bit is read as itself', () {
    expect(getTextAlignFromStyle(SWT.LEFT, _unread), TextAlign.left);
    expect(getTextAlignFromStyle(SWT.CENTER, _unread), TextAlign.center);
    expect(getTextAlignFromStyle(SWT.RIGHT, _unread), TextAlign.right);
  });

  test('two alignment bits are read LEFT first, then CENTER, then RIGHT', () {
    expect(getTextAlignFromStyle(SWT.LEFT | SWT.CENTER, _unread), TextAlign.left);
    expect(getTextAlignFromStyle(SWT.LEFT | SWT.RIGHT, _unread), TextAlign.left);
    expect(getTextAlignFromStyle(SWT.CENTER | SWT.RIGHT, _unread), TextAlign.center);
  });
}
