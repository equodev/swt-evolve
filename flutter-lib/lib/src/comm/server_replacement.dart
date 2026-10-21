/// Whether the server answering on this page's origin is no longer the one that served the page.
///
/// [servedBy] is the instance the page was served by, [answer] what the origin's instance endpoint
/// returns now. Anything other than another instance id — a missing id, an error page, a host that
/// answers every path with its own HTML — leaves the page where it is.
bool isServedByAnotherInstance(String servedBy, String? answer) {
  if (!_instanceId.hasMatch(servedBy) || answer == null) return false;
  final current = answer.trim();
  return _instanceId.hasMatch(current) && current != servedBy;
}

final RegExp _instanceId = RegExp(
    r'^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$');

/// How long to wait before asking the origin again, [attempt] asks after the comm dropped.
///
/// A relaunched application comes back within seconds, so the first minute is asked every
/// second; a page whose application simply ended keeps asking, but rarely.
Duration replacementPollDelay(int attempt) =>
    attempt < 60 ? const Duration(seconds: 1) : const Duration(seconds: 10);
