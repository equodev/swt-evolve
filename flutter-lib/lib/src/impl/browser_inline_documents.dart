/// The `setText` documents a web Browser serves from the app's own origin (see
/// `browserInlineDocumentUrl`). A document gets its URL from a round trip to the server, so a
/// later `setText` or `setUrl` can replace it before it has one.
class InlineDocuments {
  InlineDocuments(this._create, this._release, this._endpoint);

  final Future<String?> Function(String html, String? baseUrl) _create;
  final void Function(String url) _release;
  // Where the app server serves every document; empty where there is no such server.
  final String _endpoint;

  int _requests = 0;
  bool _pending = false;
  String? _current;

  /// The last document made. A `setUrl` keeps it, so Back can return to it.
  String? get current => _current;

  /// Whether a document's URL is still being made, so nothing the frame loads meanwhile is it.
  bool get pending => _pending;

  /// Makes the URL for [html] the [current] document and releases the one it replaces. Completes
  /// with false when a later [make] or [cancel] superseded it, and then releases its URL unused.
  Future<bool> make(String html, String? baseUrl) async {
    final request = ++_requests;
    _pending = true;
    final url = await _create(html, baseUrl);
    if (request != _requests) {
      if (url != null) _release(url);
      return false;
    }
    _pending = false;
    final replaced = _current;
    _current = url;
    if (replaced != null) _release(replaced);
    return true;
  }

  /// A `setUrl` replaced the document being made.
  void cancel() {
    _requests++;
    _pending = false;
  }

  void dispose() {
    cancel();
    final url = _current;
    _current = null;
    if (url != null) _release(url);
  }

  /// Whether [url] is a `setText` document: the current one, or any the app server serves.
  bool isDocument(String url) =>
      url == _current || (_endpoint.isNotEmpty && url.startsWith('$_endpoint/'));
}
