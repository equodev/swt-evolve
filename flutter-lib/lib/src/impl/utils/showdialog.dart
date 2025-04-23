import 'dart:convert';

import 'package:fluent_ui/fluent_ui.dart';

import '../../comm/comm.dart';
import '../../swt/swt.dart';

void showCustomDialog(BuildContext context, dynamic payload) {
  Map<String, dynamic> json = jsonDecode(payload);
  showDialog<String>(
    context: context,
    builder: (context) => ContentDialog(
      title: Text(json["title"] ?? ""),
      content: Text(json["message"] ?? ""),
      actions: getActions(context, json["style"] ?? "", json["labels"]),
    ),
  );
}

List<Widget> getActions(BuildContext context, int style, Map<String, dynamic>? labels) {
  const int cases = SWT.OK | SWT.CANCEL | SWT.YES | SWT.NO | SWT.ABORT | SWT.RETRY | SWT.IGNORE;
  
  return switch (style & cases) {
    SWT.OK => [
      createButton(context, labels?[SWT.OK.toString()] ?? "OK", SWT.OK)
    ],
    (SWT.OK | SWT.CANCEL) => [
      createButton(context, labels?[SWT.OK.toString()] ?? "OK", SWT.OK),
      createFilledButton(context, labels?[SWT.CANCEL.toString()] ?? "CANCEL", SWT.CANCEL)
    ],
    (SWT.YES | SWT.NO) => [
      createButton(context, labels?[SWT.YES.toString()] ?? "YES", SWT.YES),
      createFilledButton(context, labels?[SWT.NO.toString()] ?? "NO", SWT.NO)
    ],
    (SWT.YES | SWT.NO | SWT.CANCEL) => [
      createButton(context, labels?[SWT.YES.toString()] ?? "YES", SWT.YES),
      createFilledButton(context, labels?[SWT.NO.toString()] ?? "NO", SWT.NO),
      createFilledButton(context, labels?[SWT.CANCEL.toString()] ?? "CANCEL", SWT.CANCEL)
    ],
    (SWT.RETRY | SWT.CANCEL) => [
      createButton(context, labels?[SWT.RETRY.toString()] ?? "RETRY", SWT.RETRY),
      createFilledButton(context, labels?[SWT.CANCEL.toString()] ?? "CANCEL", SWT.CANCEL)
    ],
    (SWT.ABORT | SWT.RETRY | SWT.IGNORE) => [
      createButton(context, labels?[SWT.ABORT.toString()] ?? "ABORT", SWT.ABORT),
      createButton(context, labels?[SWT.RETRY.toString()] ?? "RETRY", SWT.RETRY),
      createFilledButton(context, labels?[SWT.IGNORE.toString()] ?? "IGNORE", SWT.IGNORE)
    ],
    _ => [
      // By default returns OK button
      createButton(context, labels?[SWT.OK.toString()] ?? "OK", SWT.OK)
    ]
  };
}

Button createButton(BuildContext context, String textButton, int id) {
  return Button(
    child: Text(textButton),
    onPressed: () {
      Navigator.pop(context);
      EquoCommService.sendPayload("MessageBox/close", "$id" );
    },
  );
}

Button createFilledButton(BuildContext context, String filledTextButton, int id) {
  return FilledButton(
    child: Text(filledTextButton),
    onPressed: () {
      Navigator.pop(context);
      EquoCommService.sendPayload("MessageBox/close", "$id" );
    },
  );
}
