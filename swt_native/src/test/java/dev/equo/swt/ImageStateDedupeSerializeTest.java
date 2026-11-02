package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.*;

/**
 * A tree whose many items all show the same icon must serialize that icon's pixels once, not once
 * per item: identical content travels by name after the first copy. Before, the frame grew linearly
 * with the item count even though the content was identical -- an editor with tens of thousands of
 * icon-bearing nodes could turn a single live-state read into hundreds of MB.
 */
class ImageStateDedupeSerializeTest extends dev.equo.swt.SerializeTestBase {

    /** A fresh Image object carrying a given icon's pixels: distinct identity, identical content. */
    private Image icon() {
        return icon("collapseall.png");
    }

    /** A fresh Image object carrying the pixels of the named test resource. */
    private Image icon(String resource) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            ImageData data = new ImageData(in);
            return new Image(device(), data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** The whole frame describing {@code tree} to a fresh client, as bytes on the wire. */
    private int frameBytes(Tree tree) {
        Serializer.Tape tape = new Serializer.Tape(0);
        try {
            tape.describe((DartWidget) tree.getImpl());
            return tape.frame().payload().length;
        } finally {
            Serializer.discardWritten();
            tape.release();
        }
    }

    /** How many image records in the frame carry pixel bytes (a full copy), rather than a name. */
    private long fullImagePayloads(Tree tree) {
        return fullImagePayloads(tree, 0);
    }

    /** As {@link #fullImagePayloads(Tree)}, for a frame bound to a given client connection. */
    private long fullImagePayloads(Tree tree, int connection) {
        Serializer.Tape tape = new Serializer.Tape(connection);
        try {
            tape.describe((DartWidget) tree.getImpl());
            List<dev.equo.swt.harness.WireFrames.Record> records =
                    dev.equo.swt.harness.WireFrames.records(tape.frame().payload());
            return records.stream().filter(r -> carriesImageBytes(r.state())).count();
        } finally {
            Serializer.discardWritten();
            tape.release();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean carriesImageBytes(Object node) {
        if (node instanceof java.util.Map<?, ?> map) {
            Object img = map.get("imageData");
            if (img instanceof java.util.Map<?, ?> data && data.get("data") != null) return true;
            for (Object v : map.values()) if (carriesImageBytes(v)) return true;
        } else if (node instanceof java.util.List<?> list) {
            for (Object v : list) if (carriesImageBytes(v)) return true;
        }
        return false;
    }

    private Tree tree(int itemCount, boolean withIcon) {
        Tree tree = new Tree(shell(), SWT.NONE);
        for (int i = 0; i < itemCount; i++) {
            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText("row " + i);
            if (withIcon) item.setImage(icon());
        }
        return tree;
    }

    private Tree treeWithIdenticalIcons(int itemCount) {
        return tree(itemCount, true);
    }

    @Test
    void an_extra_node_showing_the_same_icon_costs_a_name_not_its_pixels() {
        int oneIconCost = frameBytes(tree(1, true)) - frameBytes(tree(1, false));
        int perExtraWithIcon = (frameBytes(tree(200, true)) - frameBytes(tree(100, true))) / 100;
        int perExtraNoIcon = (frameBytes(tree(200, false)) - frameBytes(tree(100, false))) / 100;
        int iconCostPerExtraNode = perExtraWithIcon - perExtraNoIcon;

        System.out.printf("[image-dedupe] one icon: %d bytes; each extra node showing it adds: %d bytes%n",
                oneIconCost, iconCostPerExtraNode);

        // Before the fix each node re-embedded the whole icon, so every extra node cost ~one icon.
        // Deduped, an extra node adds only a name of the icon already in the frame -- far less than
        // half its pixels.
        assertThat(iconCostPerExtraNode)
                .as("an extra node sharing the icon must not re-embed its pixels")
                .isLessThan(oneIconCost / 2);
    }

    @Test
    void only_one_full_copy_of_a_shared_icon_travels() {
        long full = fullImagePayloads(treeWithIdenticalIcons(200));

        System.out.printf("[image-dedupe] full image payloads for 200 identical icons: %d%n", full);

        assertThat(full)
                .as("one shared icon's pixels should travel once, the rest by name")
                .isLessThanOrEqualTo(1);
    }

    @Test
    void genuinely_different_icons_each_travel_in_full() {
        Tree tree = new Tree(shell(), SWT.NONE);
        for (int i = 0; i < 10; i++) {
            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setImage(i % 2 == 0 ? icon("collapseall.png") : icon("paisaje-og.png"));
        }

        long full = fullImagePayloads(tree);
        System.out.printf("[image-dedupe] full image payloads for two distinct icons x5 each: %d%n", full);

        // Dedupe keys on content: it must never merge two genuinely different icons into one, or
        // half the rows would paint the wrong picture.
        assertThat(full).as("two distinct icons must each keep their own pixels").isEqualTo(2);
    }

    @Test
    void a_fresh_client_gets_the_icon_in_full_even_after_another_already_received_it() {
        Tree tree = treeWithIdenticalIcons(50);

        long firstClient = fullImagePayloads(tree, 1);
        long secondClient = fullImagePayloads(tree, 2);

        // The map lives for one frame: it must not leak across passes, or a second client -- which
        // never received the first client's canonical copy -- would be handed a name it cannot
        // resolve. A shared/static map would starve it to zero here.
        assertThat(firstClient).as("the first client gets the icon once").isEqualTo(1);
        assertThat(secondClient).as("a second, independent client still gets it once").isEqualTo(1);
    }

    @Test
    void the_svg_key_is_stable_and_never_collides_with_a_raster_key() {
        String svgA = "<svg xmlns='http://www.w3.org/2000/svg'><rect width='10' height='10'/></svg>";
        String svgB = "<svg xmlns='http://www.w3.org/2000/svg'><circle r='5'/></svg>";
        ImageData raster = new ImageData(16, 16, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));

        assertThat(ImageDataCodec.contentKey(svgA)).isEqualTo(ImageDataCodec.contentKey(svgA));
        assertThat(ImageDataCodec.contentKey(svgA)).isNotEqualTo(ImageDataCodec.contentKey(svgB));
        // An SVG key carries the high bit, a raster key never does -- the two spaces cannot overlap.
        assertThat(ImageDataCodec.contentKey(svgA)).isNegative();
        assertThat(ImageDataCodec.contentKey(raster)).isNotNegative();
    }
}
