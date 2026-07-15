/// Non-web platforms have no `window` to expose a JS API on, so this is a no-op.
/// See `test_harness_jsapi_web.dart` for the web impl.
void registerTestQueryJsApi({
  required String? Function(int) queryStateJson,
  required String Function() queryAllStatesJson,
  required String Function() queryTreeItemsJson,
  required bool Function(String) expandTreeItem,
  required String Function() queryPrimaryFocus,
  required void Function(void Function()) scheduleFrameSync,
}) {}
