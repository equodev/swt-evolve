package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DartImage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.ResourceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An image an application decodes from a resource stream and caches under the resource's location
 * keeps that resource's name in any cell of a Tree or Table, not only in the first column.
 */
@Tag("native-unit")
class CachedResourceCellImageNameTest {

    private static final byte[] PNG_1x1 = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==");

    private ConfigFlags savedFlags;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        savedFlags = Config.getConfigFlags();
        Config.setConfigFlags(new ConfigFlags());
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        shell = DartMocks.dartShell(display);
    }

    @AfterEach
    void tearDown() {
        ResourceManager.disposeImages();
        Config.setConfigFlags(savedFlags);
        FlutterBridge.set(null);
    }

    private static String nameOf(Image image) {
        return ((DartImage) image.getImpl())._filename();
    }

    @Test
    void aTreeCellImageIsNamedAfterItsResource() {
        Tree tree = new Tree(shell, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);

        item.setImage(1, ResourceManager.getPluginImage("bundleresource://7.fwk1/resources/ui/doc_co.png", PNG_1x1));

        assertThat(nameOf(item.getImage(1))).isEqualTo("doc_co");
    }

    @Test
    void aTableCellImageIsNamedAfterItsResource() {
        Table table = new Table(shell, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        TableItem item = new TableItem(table, SWT.NONE);

        item.setImage(1, ResourceManager.getPluginImage("bundleresource://7.fwk1/resources/ui/extract.png", PNG_1x1));

        assertThat(nameOf(item.getImage(1))).isEqualTo("extract");
    }
}
