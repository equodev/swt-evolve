/// Lets an integration test compare what a [State] paints with its Java widget. A test contract:
/// it names what is on screen, not how the renderer computes it.
abstract class RenderFactsSource {
  Map<String, dynamic> debugRenderFacts();
}
