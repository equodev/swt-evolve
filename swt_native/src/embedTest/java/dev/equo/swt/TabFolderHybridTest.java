package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.*;

/** An embedded TabFolder selects its first tab as the tab is added, and only its first. */
class TabFolderHybridTest {

    @RegisterExtension
    Mocks mock = Mocks.noNativeBridge();

    @BeforeEach
    void config() {
        Config.useEquo(TabFolder.class);
        Config.useEquo(TabItem.class);
    }

    @AfterEach
    void reset() {
        System.clearProperty(Config.PROPERTY_PREFIX + "TabFolder");
        System.clearProperty(Config.PROPERTY_PREFIX + "TabItem");
    }

    @Test
    void theFirstTabIsSelectedAsItIsAdded() {
        TabFolder folder = new TabFolder(shell(), SWT.NONE);
        assertThat(folder.getImpl()).isInstanceOf(DartTabFolder.class);
        TabItem first = new TabItem(folder, SWT.NONE);
        assertThat(folder.getSelectionIndex()).isEqualTo(0);
        assertThat(folder.getSelection()).containsExactly(first);
        new TabItem(folder, SWT.NONE);
        assertThat(folder.getSelection()).as("a later tab leaves the selection where it is").containsExactly(first);
    }
}
