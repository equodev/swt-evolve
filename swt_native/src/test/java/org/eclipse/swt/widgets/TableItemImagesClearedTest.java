package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireFrames;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A row with no image leaves its list of images out, and the client draws none from no list. A row
 * whose image is taken away has to say so: the change carries the list as none.
 */
@ExtendWith(Mocks.class)
class TableItemImagesClearedTest {

    private static final int CLIENT = 1;

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
        Serializer.enterConnection(CLIENT);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    private static Map<String, Object> itemRecord(TableItem item) {
        for (WireFrames.Record r : WireFrames.frame((DartWidget) item.getImpl(), CLIENT)) {
            if (r.channel().equals("TableItem/" + item.hashCode())) return r.state();
        }
        return null;
    }

    @Test
    void anImageTakenAwayIsSentAsNone() {
        Table table = new Table(Mocks.shell(), SWT.NONE);
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText("row");
        item.setImage(new Image(table.getDisplay(), 16, 16));
        assertThat(itemRecord(item)).containsKey("images");

        item.setImage((Image) null);
        Map<String, Object> change = itemRecord(item);

        assertThat(change).containsEntry("images", null);
    }
}
