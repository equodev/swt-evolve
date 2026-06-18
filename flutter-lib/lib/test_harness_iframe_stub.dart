/// Non-web platforms have no embedding `<iframe>` to observe, so the iframe
/// load probe is a no-op. See `test_harness_iframe_web.dart` for the web impl.
void registerIframeLoadProbe() {}
