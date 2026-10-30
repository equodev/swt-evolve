# Flutter integration harness

`FlutterHarness` boots a **real Flutter renderer** (headless Chrome, or the desktop engine) wired to a
live Java↔Flutter comm. It is the boot and transport plumbing only and assumes no widget tree;
subclasses either drive a widget tree (`WidgetFlutterHarness`, below) or speak a request/response
protocol of their own (`GenericSizeBridge`, `FontMeasureBridge`).

`WidgetFlutterHarness` adds the widget-tree half: it drives the production SWT → bridge → Flutter
path end to end for a real `Control` tree and reads back the state the client **rendered**.

State is read back on the `evolve.test.*` channels `flutter-lib/lib/test_harness.dart` registers.
Nothing sends on them in production.

## How it works

```
JUnit test ──creates──▶ Dart-backed SWT widgets (real Display/Shell)
   │                              │ markDirty
   │ WidgetFlutterHarness         ▼
   │   show(shell) ── boots ──▶ Flutter web client (headless Chrome) / desktop engine
   │   flush()     ── FlutterBridge.update() sends the changed records ──▶ Flutter builds + paints
   │   queryState(w) ──▶ "evolve.test.query" {queryId, targetId} ──▶ client finds the widget ──▶
   ◀──────── "evolve.test.queryResponse" {found, state, render} ◀───────────────────────────┘
```

Requests and replies travel as tagged values (`TaggedValues`), like any other loose payload.
`queryState(w)` answers for the widget whose id is `w.hashCode()`:
- `state`: the properties the client holds for it, by name, decoded with the wire definitions;
- `render`: facts read from the render tree, such as whether it takes input.

So a test asserts that what Java sent actually reached the client and changed what it shows.

## Usage

```java
@Tag("flutter-it")
class MyWidgetFlutterTest {
    private WidgetFlutterHarness flutter;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        flutter = new WidgetFlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setSize(300, 200);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    @Test
    void checkStaysChecked() {
        Button check = new Button(shell, SWT.CHECK);
        flutter.show(shell);                  // boot the client, wait until it is ready, first flush
        check.setSelection(true);
        flutter.flush();                      // send what changed, wait until the client painted it
        assertThat(flutter.renderedSelection(check)).isTrue();
    }
}
```

Besides `queryState`, the harness has shortcuts for common reads (`renderedSelection`,
`renderedTakesInput`) and waits (`awaitFrame`).

## Running

`flutter-it` tests are part of the default suite, which builds the Flutter web bundle and runs
against it in headless Chrome:

```bash
./gradlew :swt-evolve:swt_native:test --tests '*MyWidgetFlutterTest'
# skip the web rebuild when the bundle is current: -DskipFlutterLib
# a visible browser: -Dharness.web.headless=false ; another browser: -Dequo.swt.browser=<name or path>
# the desktop Flutter engine instead of a browser: -Dharness.client=native
```

## Backend

`test` compiles and runs against the **whole-tree-Flutter Java backend** (`src/main` + `src/native` +
`src/native<currentOs>`): every widget, Display and Shell included, is Dart-backed. The embedded
backend, where Dart and native widgets share one Display, has its own source set and task
(`embedTest`), which excludes `flutter-it`.
