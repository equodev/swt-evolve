/// The path prefix the application shell is served under, taken from the shell document's own
/// base URI. A reverse proxy that mounts the shell below a prefix (`/app/`, `/s/<id>/`) rewrites
/// the shell's `<base href>` to match; every URL the Browser widget builds against its own server
/// (`/local-file/`, `/proxy`, `/equo-browser-function`) has to start at that prefix too, or it
/// lands on whatever answers the origin's root instead of on the server that owns the content.
library;

/// Root-absolute directory of [baseUri], always with leading and trailing `/`: `/` when the shell
/// is served at the origin, `/prefix/` otherwise.
String appBasePath(String baseUri) {
  final uri = Uri.tryParse(baseUri);
  if (uri == null) return '/';
  var path = uri.path;
  if (!path.endsWith('/')) {
    final cut = path.lastIndexOf('/');
    path = cut < 0 ? '/' : path.substring(0, cut + 1);
  }
  if (!path.startsWith('/')) path = '/$path';
  return path;
}

/// [rootRelative] (`/local-file/...`, `/proxy?url=...`) placed under [basePath].
String underAppBase(String basePath, String rootRelative) {
  final rel = rootRelative.startsWith('/')
      ? rootRelative.substring(1)
      : rootRelative;
  return '$basePath$rel';
}

/// Moves every root-absolute `/local-file/` reference in [html] under [basePath]. The server
/// rewrites `file:` sub-resources to root-absolute paths, and a root-absolute path ignores the
/// document's `<base>`, so they must carry the prefix themselves.
String prefixLocalFilePaths(String html, String basePath) {
  if (basePath == '/') return html;
  return html.replaceAll('/local-file/', '${basePath}local-file/');
}
