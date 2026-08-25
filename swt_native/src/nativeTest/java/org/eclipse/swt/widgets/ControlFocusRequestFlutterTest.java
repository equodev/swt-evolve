package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focus Java takes on its own has to reach the render side. On the whole-tree surface the client owns
 * the real keyboard focus, so tracking the focus holder in Java alone leaves a control Java focused
 * — a JFace cell editor opened by {@code TableViewer.editElement}, a part activation — rendered but
 * unable to receive a keystroke until the user clicks it.
 *
 * <p>Same shape as {@link DisplayConfigFlagsFlutterTest}: a {@code @Tag("flutter-it")} test driven by
 * the {@code nativeTest} task, with a {@link RecordingComm} standing in for the Flutter client.
 */
@Tag("flutter-it")
class ControlFocusRequestFlutterTest {

    private static final String FOCUS_CHANNEL = "swt.evolve.focus";

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aCellEditorAsksTheClientToMoveKeyboardFocus() {
        TestWebBridge web = install(TestWebBridge::new);
        Text editor = cellEditorOn(newTable(), 0);
        web.comm.sent.clear();

        assertThat(editor.setFocus()).as("the editor accepts focus").isTrue();

        assertThat(focusRequests(web))
                .as("the client is told to move keyboard focus onto the cell editor")
                .containsExactly(String.valueOf(editor.hashCode()));
    }

    @Test
    void anOrdinaryFieldIsLeftToTheRenderSide() {
        TestWebBridge web = install(TestWebBridge::new);
        Shell shell = openShell();
        Text plain = new Text(shell, SWT.SINGLE);
        web.comm.sent.clear();

        plain.setFocus();

        // Pushing focus at every control Java focuses breaks dialogs that manage their own: the
        // Eclipse IDE New Project wizard drops from 10/10 to 5 failures, its field's keystrokes
        // landing out of order. Only the cell editor -- created, placed and focused in one
        // event-loop pass, with no gesture the render side could have seen -- is pushed.
        assertThat(focusRequests(web))
                .as("an ordinary field is not pushed at the client")
                .isEmpty();
    }

    @Test
    void focusRequestFollowsTheEditorJavaFocusesLast() {
        TestWebBridge web = install(TestWebBridge::new);
        Table table = newTable();
        Text first = cellEditorOn(table, 0);
        Text second = cellEditorOn(table, 1);
        first.setFocus();
        web.comm.sent.clear();

        second.setFocus();

        assertThat(focusRequests(web))
                .as("only the newly focused editor is requested")
                .containsExactly(String.valueOf(second.hashCode()));
    }

    // ---- harness ----------------------------------------------------------------------------------

    /** The control ids named by every {@code swt.evolve.focus} frame sent so far, in order. */
    private static java.util.List<String> focusRequests(TestWebBridge web) {
        return web.comm.sent.stream()
                .filter(f -> f.event.equals(FOCUS_CHANNEL))
                .map(f -> f.json.replaceAll(".*\"id\"\\s*:\\s*(-?\\d+).*", "$1"))
                .toList();
    }

    private Table newTable() {
        Shell shell = openShell();
        Table table = new Table(shell, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        new TableItem(table, SWT.NONE);
        return table;
    }

    /** The Text a TableEditor places over a cell -- the shape JFace's TextCellEditor produces. */
    private Text cellEditorOn(Table table, int column) {
        Text text = new Text(table, SWT.SINGLE);
        org.eclipse.swt.custom.TableEditor editor = new org.eclipse.swt.custom.TableEditor(table);
        editor.setEditor(text, table.getItem(0), column);
        return text;
    }

    private Shell openShell() {
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        shell.open();
        return shell;
    }

    /** See {@code DisplayConfigFlagsFlutterTest.install} — a Display whose bridge is the test bridge. */
    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** A stub injected only so {@code Display.init()} skips creating a real surface bridge. */
    private static final class NoopBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();

        NoopBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }

    private static final class TestWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestWebBridge(DartDisplay display) {
            super(display);
            // Stands in for the client having connected: update() defers every pending send until
            // then, and the focus request is queued behind it.
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }
    }
}
