package org.eclipse.swt.widgets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.delivery.DeliveryAudit;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What reaches the client about the children of an item in a {@code SWT.VIRTUAL} tree.
 *
 * <p>The calls are the ones a JFace lazy viewer makes - the Eclipse Debug view's launch tree among
 * them: the child count goes in through {@link TreeItem#setItemCount}, a child is created when the
 * viewer asks for it by index, and the item is opened with {@link TreeItem#setExpanded}. A native
 * tree answers all three on its own, asking for the count to draw the expander and for each row's
 * data as it paints. Here the client can only draw what it is sent, so each step has to say so.
 */
@Tag("flutter-it")
class TreeVirtualChildDeliveryTest {

    private Display display;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        FlutterBridge.setSendObserver(null);
    }

    @Test
    @DisplayName("a collapsed virtual item tells the client it has children, so it can be expanded")
    void collapsedItemSendsItsChildCount() {
        TreeItem launch = virtualTreeWithOneRow();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            launch.setItemCount(2);
            FlutterBridge.update();

            assertThat(sentFor(audit, launch))
                    .as("no child exists yet, so the count is the only thing that can give the row "
                            + "an expander")
                    .anySatisfy(frame -> assertThat(frame.get("itemCount").getAsInt()).isEqualTo(2));
        }
    }

    @Test
    @DisplayName("expanding a virtual item delivers the children its SetData filled")
    void expandingDeliversTheChildren() {
        TreeItem launch = virtualTreeWithOneRow();
        launch.setItemCount(2);
        FlutterBridge.update();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            launch.setExpanded(true);
            FlutterBridge.update();

            assertThat(childTextsSentFor(audit, launch))
                    .as("the children exist in Java once the item is open; an update that names "
                            + "only `expanded` leaves an open row with nothing under it")
                    .containsExactly("row0/0", "row0/1");
        }
    }

    @Test
    @DisplayName("rows added under an open virtual item are filled and delivered")
    void rowsAddedUnderAnOpenItemAreDelivered() {
        TreeItem launch = virtualTreeWithOneRow();
        launch.setItemCount(1);
        launch.setExpanded(true);
        FlutterBridge.update();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            launch.setItemCount(3);
            FlutterBridge.update();

            assertThat(childTextsSentFor(audit, launch))
                    .as("the row is already open, so nothing else is going to ask for the new ones")
                    .containsExactly("row0/0", "row0/1", "row0/2");
        }
    }

    @Test
    @DisplayName("a child the viewer creates by index reaches the client")
    void childCreatedByIndexIsDelivered() {
        TreeItem launch = virtualTreeWithOneRow();
        launch.setItemCount(20);
        launch.setExpanded(true);
        FlutterBridge.update();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            // Past the page an expand fills, the way a viewer's replace() reaches a row.
            launch.getItem(18).setText("replaced");
            FlutterBridge.update();

            assertThat(childTextsSentFor(audit, launch))
                    .as("the parent's item list gained a row")
                    .contains("replaced");
        }
    }

    // ---- harness ----

    /** A VIRTUAL tree whose rows SetData fills, with one top-level row already on the client. */
    private TreeItem virtualTreeWithOneRow() {
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.VIRTUAL | SWT.BORDER);
        tree.addListener(SWT.SetData, e -> {
            TreeItem item = (TreeItem) e.item;
            TreeItem parent = item.getParentItem();
            item.setText((parent == null ? "row" : parent.getText() + "/") + e.index);
        });
        shell.open();
        FlutterBridge.update();
        // After the tree is on the client, like a launch added to a Debug view already showing:
        // a row created before the first flush travels whole, children and all, and hides the bug.
        tree.setItemCount(1);
        FlutterBridge.update();
        return tree.getItem(0);
    }

    /** Every description of {@code item} that went out, on its own channel or inside another. */
    private static List<JsonObject> sentFor(DeliveryAudit audit, TreeItem item) {
        List<JsonObject> found = new ArrayList<>();
        for (DeliveryAudit.Frame frame : audit.frames()) {
            collect(JsonParser.parseString(frame.json()), item.hashCode(), found);
        }
        return found;
    }

    private static void collect(JsonElement node, int id, List<JsonObject> found) {
        if (node.isJsonArray()) {
            for (JsonElement e : node.getAsJsonArray()) collect(e, id, found);
        } else if (node.isJsonObject()) {
            JsonObject o = node.getAsJsonObject();
            if (o.has("id") && o.get("id").getAsInt() == id) found.add(o);
            for (var e : o.entrySet()) collect(e.getValue(), id, found);
        }
    }

    /**
     * The texts of the children listed in the last item list sent for {@code parent}. A child sent
     * as a name is one the client already holds, so it reads as the text Java has for it.
     */
    private static List<String> childTextsSentFor(DeliveryAudit audit, TreeItem parent) {
        JsonArray items = null;
        for (JsonObject frame : sentFor(audit, parent)) {
            if (frame.has("items")) items = frame.getAsJsonArray("items");
        }
        List<String> texts = new ArrayList<>();
        if (items == null) return texts;
        for (JsonElement child : items) {
            JsonObject c = child.getAsJsonObject();
            if (c.has("text")) texts.add(c.get("text").getAsString());
            else if (c.has("_r")) texts.add(knownChildText(parent, c.get("id").getAsInt()));
            else texts.add("<" + c + ">");
        }
        return texts;
    }

    private static String knownChildText(TreeItem parent, int id) {
        for (TreeItem child : ((DartTreeItem) parent.getImpl()).items) {
            if (child != null && child.hashCode() == id) return child.getText();
        }
        return "<unknown " + id + ">";
    }
}
