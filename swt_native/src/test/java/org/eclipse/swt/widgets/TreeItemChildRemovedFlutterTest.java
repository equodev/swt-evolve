package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A viewer gives a collapsed item a placeholder child so it shows an arrow, and disposes it when the
 * item expands. The item's own list of children has to change with it: left unsent, the client goes
 * on drawing the placeholder as a blank row under an item that has no children.
 */
@Tag("flutter-it")
class TreeItemChildRemovedFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void disposingAnItemsLastChildChangesTheItemsChildren() {
        display = RecordingWebBridge.install().display;
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem project = new TreeItem(tree, SWT.NONE);
        project.setText("Project");
        TreeItem placeholder = new TreeItem(project, SWT.NONE);
        VTreeItem value = ((DartTreeItem) project.getImpl()).getValue();
        value.clearDirty();

        placeholder.dispose();

        assertThat(value.changedKeys().contains(VTreeItem.ITEMS))
                .as("the item's children are sent again, now without the placeholder")
                .isTrue();
        assertThat(value.changedKeys().contains(VTreeItem.ITEM_COUNT)).isTrue();
    }
}
