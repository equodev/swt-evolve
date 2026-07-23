package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.TextLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Owner-draw capture through a {@link TextLayout} (issue #818, Eclipse "Find Actions").
 *
 * <p>Eclipse's {@code QuickAccessEntry.paint()} does not draw its cell text with {@code gc.drawText}
 * — it draws it through a {@link TextLayout}. So capturing owner-drawn text at the GC alone is not
 * enough: {@code DartTextLayout.draw(...)} must feed the capture too, otherwise suppressing the
 * item's model text (which {@code erase()} asks for by clearing {@link SWT#FOREGROUND}) would wipe
 * every label instead of just the repeated category.
 *
 * <p>This lives in a {@code *FlutterTest} because only the whole-tree-Flutter backend constructs a
 * {@code DartTextLayout}; the embedded backend (the default {@code test} task) builds the native
 * {@code SwtTextLayout}. Runs via {@code nativeTest}. The GC-level captures are covered by
 * {@code TableOwnerDrawSerializeTest}.
 */
@Tag("flutter-it")
class TableOwnerDrawTextLayoutFlutterTest {

    private Display display;

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
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    /**
     * The QuickAccess shape: the category is set on every item but only drawn on the first row of a
     * group, and every cell's text is drawn through a TextLayout. The captured drawing — not the
     * model — is what must be serialized.
     */
    @Test
    void textDrawnThroughATextLayout_replacesTheSuppressedModelText() {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.NONE);
        new TableColumn(table, SWT.LEFT);
        new TableColumn(table, SWT.LEFT);

        table.addListener(SWT.EraseItem, event -> event.detail &= ~SWT.FOREGROUND);
        table.addListener(SWT.PaintItem, event -> {
            TableItem painted = (TableItem) event.item;
            boolean firstInCategory = Boolean.TRUE.equals(painted.getData("first"));
            // Column 0 (the category) is drawn only for the first row of the group.
            if (event.index == 0 && !firstInCategory) {
                return;
            }
            TextLayout layout = new TextLayout(display);
            layout.setText(event.index == 0 ? "Views" : "Package Explorer");
            layout.draw(event.gc, 2, 2);
            layout.dispose();
        });

        TableItem first = new TableItem(table, SWT.NONE);
        first.setData("first", Boolean.TRUE);
        first.setText(new String[] { "Views", "Package Explorer" });

        TableItem repeated = new TableItem(table, SWT.NONE);
        repeated.setText(new String[] { "Views", "Package Explorer" });

        String[] firstTexts = ((DartTableItem) first.getImpl()).getTexts();
        String[] repeatedTexts = ((DartTableItem) repeated.getImpl()).getTexts();

        assertThat(firstTexts[0])
                .as("the first row of a category draws it, so the drawn text is captured")
                .isEqualTo("Views");
        assertThat(repeatedTexts[0])
                .as("a repeated category draws nothing, so the suppressed model text must not leak")
                .isEmpty();
        assertThat(repeatedTexts[1])
                .as("the label is drawn through the TextLayout and must survive")
                .isEqualTo("Package Explorer");
    }
}
