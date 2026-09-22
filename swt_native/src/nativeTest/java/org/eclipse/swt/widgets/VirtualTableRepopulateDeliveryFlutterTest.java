package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A {@code SWT.VIRTUAL} table refilled from a shorter list has to reach the client with every row
 * it now shows. The rows an autocomplete popup offers are exactly this: the proposal list is
 * replaced on each keystroke through {@code setItemCount} + {@code clearAll}, and a row the client
 * is never told about renders blank — visibly, a leftover of the list that was there before, until
 * something unrelated forces a repaint.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest</pre>
 */
@Tag("flutter-it")
class VirtualTableRepopulateDeliveryFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Table table;

    /** What the SetData listener fills rows from; swapped to refill the table. */
    private List<String> rows = List.of("[literal]", "[local]", "[config]", "[liveshot]", "[ext]");

    @BeforeEach
    void setUp() {
        web = install(TestWebBridge::new);
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        shell.open();
        table = new Table(shell, SWT.VIRTUAL);
        new TableColumn(table, SWT.NONE);
        table.addListener(SWT.SetData, e -> {
            TableItem item = (TableItem) e.item;
            int index = e.index;
            item.setText(index < rows.size() ? rows.get(index) : "");
        });
        refill();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    /** What the popup does on every keystroke: resize to the new match count, then re-read them. */
    private void refill() {
        table.setItemCount(rows.size());
        table.clearAll();
        FlutterBridge.update();
    }

    /** Every row text the client was sent, in order, across all frames — state travels batched,
     *  so the widget's own channel name is not what carries it. */
    private List<String> textsDeliveredToTheClient() {
        StringBuilder all = new StringBuilder();
        for (RecordingComm.Frame f : web.comm.sent) all.append(f.json);
        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("\"texts\":\\[\"([^\"]*)\"").matcher(all);
        List<String> found = new java.util.ArrayList<>();
        while (m.find()) found.add(m.group(1));
        return found;
    }

    @Test
    @DisplayName("a virtual table refilled from a shorter list sends every row it now shows")
    void refillSendsEveryRow() {
        assertThat(textsDeliveredToTheClient())
                .as("precondition: the first fill reached the client")
                .contains("[literal]", "[local]", "[config]", "[liveshot]", "[ext]");

        // The popup narrows to a single "no matches" row before the new proposals arrive, so the
        // row that grows the list back is a freshly created one -- which is the one that went blank.
        rows = List.of("(No matches)");
        refill();

        web.comm.sent.clear();
        rows = List.of("/", "~/");
        refill();

        assertThat(table.getItemCount()).as("Java's own view of the refilled table").isEqualTo(2);
        assertThat(table.getItem(1).getText()).as("Java has the second row's text").isEqualTo("~/");
        assertThat(textsDeliveredToTheClient())
                .as("every row the table now shows reaches the client")
                .contains("/", "~/");
    }

    // ---- harness ----------------------------------------------------------------------------------

    /** See {@code ControlFocusRequestFlutterTest.install}. */
    private <B extends DisplayBridge> B install(java.util.function.Function<DartDisplay, B> factory) {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

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
