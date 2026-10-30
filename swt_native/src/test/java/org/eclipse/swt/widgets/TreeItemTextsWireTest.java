package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/** The texts a tree row sends: left out when they say only what its text says, the client reading column 0 from text. */
@ExtendWith(Mocks.class)
class TreeItemTextsWireTest {

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a row whose only text is its text sends no texts")
    void onlyTextIsLeftOut() {
        TreeItem item = new TreeItem(new Tree(Mocks.shell(), SWT.NONE), SWT.NONE);
        item.setText("a");
        assertThat(TreeHelper.getTexts((DartTreeItem) item.getImpl())).isNull();
    }

    @Test
    @DisplayName("a row of several columns sends every text")
    void severalColumnsAreSent() {
        Tree tree = new Tree(Mocks.shell(), SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(new String[] {"a", "b"});
        assertThat(TreeHelper.getTexts((DartTreeItem) item.getImpl())).containsExactly("a", "b");
    }

    @Test
    @DisplayName("a row left with one text that is not its text sends it")
    void oneTextOtherThanTheItemsIsSent() {
        Tree tree = new Tree(Mocks.shell(), SWT.NONE);
        TreeColumn first = new TreeColumn(tree, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText("a");
        // The second column, which becomes the only one, was never given a text.
        first.dispose();
        assertThat(item.getText()).isEmpty();
        assertThat(TreeHelper.getTexts((DartTreeItem) item.getImpl())).containsExactly((String) null);
    }
}
