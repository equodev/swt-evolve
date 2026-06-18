# Flutter integration harness

`FlutterHarness` is a JUnit 5 extension that boots a **real Flutter renderer** (web browser, or the
native engine) wired to a live Java↔Flutter comm, so a test can drive the production
SWT → bridge → Flutter path end to end and then read back the **rendered** widget state for
assertions.

Boot/transport plumbing is adapted from `dev.equo.swt.bench.BenchBridge`. State read-back uses a
dormant `evolve.test.query` channel registered by Flutter's `main()`
(`flutter-lib/lib/test_harness.dart`) — inert in production (nothing sends on it there).

## How it works

```
JUnit test ──creates──▶ Dart-backed SWT widgets (mocked Display/Shell)
   │                              │ getValue()/dirty
   │ FlutterHarness (the bridge)  ▼
   │   show(root) ── boots ──▶ Flutter web build (Chrome) / native engine
   │   flush()   ── FlutterBridge.update() pushes the tree ──▶ Flutter paints
   │   queryState(w) ──▶ "evolve.test.query" ──▶ walk element tree ──▶
   ◀──────────────── "evolve.test.queryResponse" {state: V*.toJson()} ◀─┘
```

`queryState(w)` returns the live `V*.toJson()` of the widget whose id is `w.hashCode()`, so a test
can assert that state pushed from Java actually reached and updated the rendered widget.

## Usage

```java
@RegisterExtension static Mocks mocks = Mocks.withNativeBridge(); // mocked Display/Shell, real bridge
@RegisterExtension static FlutterHarness flutter = new FlutterHarness();

Composite group = new Composite(Mocks.swtShell(), SWT.NONE);
Button r1 = new Button(group, SWT.RADIO);
flutter.show(group);                  // boot renderer, await ClientReady, initial flush
((DartButton) r1.getImpl()).selectRadio();
flutter.flush();                      // push pending state, let Flutter paint
assertThat(flutter.renderedSelection(r1)).isTrue();
```

Tag such tests `@Tag("flutter-it")` — that tag is excluded from the default test run.

## Running

The `webTest` Gradle task (modelled on `webBenchmark`) builds the Flutter web app and runs the
`flutter-it`-tagged tests against it in a headless browser:

```bash
./gradlew :swt-evolve:swt_native:webTest          # builds webFlutterLib first
# headful: -Dharness.web.headless=false ; custom chrome: -Dequo.swt.browser=/path/to/chrome
# skip the web rebuild if already built: -DskipFlutterLib
```

Native engine instead of a browser (no web build / Chrome needed):

```bash
./gradlew :swt-evolve:swt_native:test --tests '*RadioGroupFlutterTest' -DincludeTags=flutter-it -Dharness.client=native
```

(`flutter-it` is excluded from the default `test` run.)

## Backend

`webTest` compiles the harness + `flutter-it` tests against the **web Java backend**
(`src/main + src/webMain + src/web<currentOs>`) via its own source set — so the real `webMain`
server code runs, not the native impl. (The rest of `src/test/java` can't compile against the web
backend: `Mocks`/`SerializeTestBase` import native-only `Swt*` classes; hence the `include` filter.)

## Does it reproduce issue #597?

No — and that's worth stating plainly. The harness compiles, boots a real Flutter web client, renders
the widgets, and reads their state back (the sample **passes**). But #597 is an **intermittent timing
race** in the Display event loop, and the sample passes with **both** the fixed (synchronous) and the
old (`asyncExec`) `selectRadio` parent-dirty — verified by reverting the fix and re-running. Driving
`selectRadio()` + a synchronous `flush()` doesn't recreate the async interleaving; even a
`readAndDispatch` pump self-delivers the deselection either way. So this is solid **infrastructure**
for deterministic full-stack assertions, but it does not (yet) give a fails-before/passes-after guard
for that specific race — that would require forcing the lifecycle edge where the deferred parent dirty
is dropped.
