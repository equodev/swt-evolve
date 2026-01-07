import 'dart:convert';

import 'package:flutter/material.dart';

import '../../comm/comm.dart';
import '../../gen/swt.dart';

void showCustomDialog(BuildContext context, dynamic payload) {
  Map<String, dynamic> json = jsonDecode(payload);
  showDialog<String>(
    context: context,
    builder: (context) => AlertDialog(
      title: Text(json["title"] ?? ""),
      content: Text(json["message"] ?? ""),
      actions: getActions(context, json["style"] ?? "", json["labels"]),
    ),
  );
}

List<Widget> getActions(
  BuildContext context,
  int style,
  Map<String, dynamic>? labels,
) {
  final int cases =
      SWT.OK |
      SWT.CANCEL |
      SWT.YES |
      SWT.NO |
      SWT.ABORT |
      SWT.RETRY |
      SWT.IGNORE;

  return switch (style & cases) {
    SWT.OK => [
      createButton(context, labels?[SWT.OK.toString()] ?? "OK", SWT.OK),
    ],
    (SWT.OK | SWT.CANCEL) => [
      createButton(context, labels?[SWT.OK.toString()] ?? "OK", SWT.OK),
      createFilledButton(
        context,
        labels?[SWT.CANCEL.toString()] ?? "CANCEL",
        SWT.CANCEL,
      ),
    ],
    (SWT.YES | SWT.NO) => [
      createButton(context, labels?[SWT.YES.toString()] ?? "YES", SWT.YES),
      createFilledButton(context, labels?[SWT.NO.toString()] ?? "NO", SWT.NO),
    ],
    (SWT.YES | SWT.NO | SWT.CANCEL) => [
      createButton(context, labels?[SWT.YES.toString()] ?? "YES", SWT.YES),
      createFilledButton(context, labels?[SWT.NO.toString()] ?? "NO", SWT.NO),
      createFilledButton(
        context,
        labels?[SWT.CANCEL.toString()] ?? "CANCEL",
        SWT.CANCEL,
      ),
    ],
    (SWT.RETRY | SWT.CANCEL) => [
      createButton(
        context,
        labels?[SWT.RETRY.toString()] ?? "RETRY",
        SWT.RETRY,
      ),
      createFilledButton(
        context,
        labels?[SWT.CANCEL.toString()] ?? "CANCEL",
        SWT.CANCEL,
      ),
    ],
    (SWT.ABORT | SWT.RETRY | SWT.IGNORE) => [
      createButton(
        context,
        labels?[SWT.ABORT.toString()] ?? "ABORT",
        SWT.ABORT,
      ),
      createButton(
        context,
        labels?[SWT.RETRY.toString()] ?? "RETRY",
        SWT.RETRY,
      ),
      createFilledButton(
        context,
        labels?[SWT.IGNORE.toString()] ?? "IGNORE",
        SWT.IGNORE,
      ),
    ],
    _ => [
      // By default returns OK button
      createButton(context, labels?[SWT.OK.toString()] ?? "OK", SWT.OK),
    ],
  };
}

Widget createButton(BuildContext context, String textButton, int id) {
  return TextButton(
    onPressed: () {
      Navigator.pop(context);
      EquoCommService.sendPayload("MessageBox/close", "$id");
    },
    child: Text(textButton),
  );
}

Widget createFilledButton(
  BuildContext context,
  String filledTextButton,
  int id,
) {
  return ElevatedButton(
    onPressed: () {
      Navigator.pop(context);
      EquoCommService.sendPayload("MessageBox/close", "$id");
    },
    child: Text(filledTextButton),
  );
}
