package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focus reports from the client that SWT would never act on: a shown popup menu takes the keyboard
 * without moving the focus control, a disabled control never holds the focus, and a report that was
 * already in flight when Java asked for a different control is older than that request.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*ClientFocusReportsFlutterTest'</pre>
 */
@Tag("flutter-it")
class ClientFocusReportsFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell shell;
    private Text first;
    private Text field;
    private final List<String> events = new ArrayList<>();

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void openShell() {
        web = install(TestWebBridge::new);
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        first = new Text(shell, SWT.SINGLE);
        field = new Text(shell, SWT.SINGLE);
        shell.setSize(400, 300);
        shell.open();
        clientReports(field, "Focus/FocusIn");
        field.addListener(SWT.FocusOut, e -> events.add("field FocusOut"));
        first.addListener(SWT.FocusIn, e -> events.add("first FocusIn"));
    }

    private void clientReports(Widget widget, String suffix) {
        web.comm.fireContaining(channel(widget, suffix), new Event());
        while (display.readAndDispatch()) {
        }
    }

    @Test
    void aShownPopupMenuLeavesTheFocusControlWhereItWas() {
        openShell();
        Menu menu = new Menu(shell, SWT.POP_UP);
        new MenuItem(menu, SWT.PUSH).setText("Open");
        menu.setVisible(true);
        while (display.readAndDispatch()) {
        }

        clientReports(field, "Focus/FocusOut");

        assertThat(display.getFocusControl()).isSameAs(field);
        assertThat(events).isEmpty();
    }

    @Test
    void theFocusControlIsStillTheFieldOnceTheMenuHides() {
        openShell();
        Menu menu = new Menu(shell, SWT.POP_UP);
        new MenuItem(menu, SWT.PUSH).setText("Open");
        menu.setVisible(true);
        while (display.readAndDispatch()) {
        }
        clientReports(field, "Focus/FocusOut");

        clientReports(menu, "Menu/Hide");
        clientReports(field, "Focus/FocusIn");

        assertThat(display.getFocusControl()).isSameAs(field);
        assertThat(events).isEmpty();
    }

    @Test
    void withNoMenuShownAFocusLossStillHandsTheFocusOn() {
        openShell();

        clientReports(field, "Focus/FocusOut");

        assertThat(display.getFocusControl()).isNotSameAs(field);
    }

    @Test
    void aDisabledControlTheClientFocusedDoesNotTakeTheFocus() {
        openShell();
        Combo disabled = new Combo(shell, SWT.READ_ONLY);
        disabled.setEnabled(false);
        disabled.addListener(SWT.FocusIn, e -> events.add("disabled FocusIn"));

        clientReports(disabled, "Focus/FocusIn");

        assertThat(display.getFocusControl()).isSameAs(field);
        assertThat(events).isEmpty();
    }

    @Test
    void aFocusReportOlderThanTheRequestItAnswersDoesNotUndoIt() {
        web = install(TestWebBridge::new);
        Shell shell = openBareShell();
        Tree tree = new Tree(shell, SWT.NONE);
        Text editor = cellEditorOn(newTable(shell), 0);
        clientReports(tree, "Focus/FocusIn");

        assertThat(editor.setFocus()).as("the editor accepts focus").isTrue();
        // The report the client had already sent when the request went out: it still names the
        // control the client held before being asked, so applying it moves focus straight back off
        // the editor -- which closes it, the editor committing on focus lost.
        clientReports(tree, "Focus/FocusIn");

        assertThat(display.getFocusControl()).isSameAs(editor);
    }

    @Test
    void onlyTheOneReportInFlightIsDroppedSoTheNextOneIsStillTracked() {
        web = install(TestWebBridge::new);
        Shell shell = openBareShell();
        Tree tree = new Tree(shell, SWT.NONE);
        Text editor = cellEditorOn(newTable(shell), 0);
        clientReports(tree, "Focus/FocusIn");
        editor.setFocus();
        clientReports(tree, "Focus/FocusIn");

        // A request stops standing once a report answers it, whichever control that report named.
        clientReports(tree, "Focus/FocusIn");

        assertThat(display.getFocusControl()).isSameAs(tree);
    }

    // ---- harness ----------------------------------------------------------------------------------

    private Shell openBareShell() {
        Shell bare = new Shell(display);
        bare.setSize(400, 300);
        bare.open();
        return bare;
    }

    private static Table newTable(Shell parent) {
        Table table = new Table(parent, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        new TableItem(table, SWT.NONE);
        return table;
    }

    /** See {@code ControlFocusRequestFlutterTest.cellEditorOn} -- the only shape focus is pushed for. */
    private static Text cellEditorOn(Table table, int column) {
        Text text = new Text(table, SWT.SINGLE);
        org.eclipse.swt.custom.TableEditor editor = new org.eclipse.swt.custom.TableEditor(table);
        editor.setEditor(text, table.getItem(0), column);
        return text;
    }

    private static String channel(Widget widget, String suffix) {
        return "/" + widget.hashCode() + "/" + suffix;
    }

    /** See {@code ControlFocusRequestFlutterTest.install} — a Display whose bridge is the test bridge. */
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
            // Stands in for the client having connected: update() defers every pending send until then.
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
