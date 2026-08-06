package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("flutter-it")
class TextSearchCancelClearsFlutterTest {

    private RecordingBridge bridge;
    private Display display;
    private final StringBuilder filter = new StringBuilder();
    private int modifyCount;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    private void pump() {
        for (int i = 0; i < 100 && display.readAndDispatch(); i++) {
        }
    }

    private Text filteredTreeSearchField(Shell shell) {
        Text filterText = new Text(shell, SWT.SEARCH | SWT.ICON_CANCEL);
        ModifyListener modify = e -> {
            modifyCount++;
            filter.setLength(0);
            filter.append(filterText.getText());
        };
        filterText.addModifyListener(modify);
        return filterText;
    }

    private void clickCancelIcon() {
        Event cancel = new Event();
        cancel.detail = SWT.ICON_CANCEL;
        bridge.comm.fireContaining("Selection/DefaultSelection", cancel);
        pump();
    }

    @Test
    void clickingTheCancelIconClearsTextAndResetsFilter() {
        Shell shell = new Shell(display);
        Text filterText = filteredTreeSearchField(shell);

        assertThat(filterText.getStyle() & SWT.ICON_CANCEL)
                .as("a SWT.SEARCH|SWT.ICON_CANCEL Text must report ICON_CANCEL in its style")
                .isEqualTo(SWT.ICON_CANCEL);

        filterText.setText("foo");
        pump();
        assertThat(filterText.getText()).isEqualTo("foo");
        assertThat(filter.toString()).isEqualTo("foo");

        int modifiesBeforeCancel = modifyCount;

        clickCancelIcon();

        assertThat(filterText.getText())
                .as("clicking the cancel icon must clear the search field's own text")
                .isEmpty();
        assertThat(modifyCount)
                .as("clicking the cancel icon must fire a Modify so the filter re-runs")
                .isGreaterThan(modifiesBeforeCancel);
        assertThat(filter.toString())
                .as("clicking the cancel icon must reset the filter to empty")
                .isEmpty();
    }
}
